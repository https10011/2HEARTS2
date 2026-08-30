/**
 * Timeline service (Phase 9).
 *
 * Application-facing boundary over TimelineRepository.
 * Validates input, normalizes errors, provides clean state to UI.
 *
 * Layer discipline: UI → TimelineService → TimelineRepository → Local persistence.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import { AppError } from '../errors/appError.ts';
import {
  normalizeInput,
  textLength,
  validIsoDate,
  validate,
  type ValidationResult,
} from '../validation/validators.ts';
import { TimelineRepository } from '../../repositories/timelineRepository.ts';
import { systemClock, type Clock } from '../../utils/time.ts';

const MAX_TITLE_LENGTH = 200;
const MAX_DESCRIPTION_LENGTH = 5000;

export interface TimelineEventInput {
  title: string;
  eventDate: string;
  description?: string | null;
}

/** Safe subset of TimelineEvent for UI consumption. */
export interface TimelineEventView {
  id: string;
  title: string;
  eventDate: string;
  description: string;
  excerpt: string;
  createdAt: string;
  updatedAt: string;
}

function validationFailure(errors: string[]): never {
  throw new AppError('validation', 'invalid-input', {
    recoverable: true,
    userMessage: 'Please check the highlighted fields.',
    cause: { errors },
  });
}

function toView(event: { id: string; title: string; eventDate: string; description: string | null; createdAt: string; updatedAt: string }): TimelineEventView {
  return {
    id: event.id,
    title: event.title,
    eventDate: event.eventDate,
    description: event.description ?? '',
    excerpt: TimelineRepository.excerpt(event.description),
    createdAt: event.createdAt,
    updatedAt: event.updatedAt,
  };
}

export class TimelineService {
  private readonly events: TimelineRepository;

  constructor(db: DatabaseAdapter, clock: Clock = systemClock) {
    this.events = new TimelineRepository(db, clock);
  }

  /** Lists all active events in chronological order. */
  async listEvents(): Promise<TimelineEventView[]> {
    const entities = await this.events.listEvents();
    return entities.map(toView);
  }

  /** Gets a single event by id. */
  async getEvent(id: string): Promise<TimelineEventView> {
    const event = await this.events.getById(id);
    if (!event) {
      throw new AppError('persistence', 'not-found', {
        recoverable: false,
        userMessage: 'Event not found.',
      });
    }
    return toView(event);
  }

  /** Creates a new timeline event. */
  async createEvent(input: TimelineEventInput): Promise<TimelineEventView> {
    const title = normalizeInput(input.title);
    const eventDate = input.eventDate;
    const description = input.description ? normalizeInput(input.description) : null;

    const result = validate(
      textLength(title, 1, MAX_TITLE_LENGTH, 'Title'),
      validIsoDate(eventDate),
      description !== null ? textLength(description, 0, MAX_DESCRIPTION_LENGTH, 'Description') : { ok: true, errors: [] },
    );
    if (!result.ok) validationFailure(result.errors);

    const entity = await this.events.create({
      title,
      eventDate,
      description,
      deletedAt: null,
    });

    return toView(entity);
  }

  /** Updates an existing timeline event. */
  async updateEvent(id: string, input: Partial<TimelineEventInput>): Promise<TimelineEventView> {
    const existing = await this.events.getById(id);
    if (!existing) {
      throw new AppError('persistence', 'not-found', {
        recoverable: false,
        userMessage: 'Event not found.',
      });
    }

    const changes: Record<string, unknown> = {};

    if (input.title !== undefined) {
      const title = normalizeInput(input.title);
      const result = validate(textLength(title, 1, MAX_TITLE_LENGTH, 'Title'));
      if (!result.ok) validationFailure(result.errors);
      changes.title = title;
    }

    if (input.eventDate !== undefined) {
      const result = validate(validIsoDate(input.eventDate));
      if (!result.ok) validationFailure(result.errors);
      changes.eventDate = input.eventDate;
    }

    if (input.description !== undefined) {
      const description = input.description ? normalizeInput(input.description) : null;
      if (description !== null) {
        const result = validate(textLength(description, 0, MAX_DESCRIPTION_LENGTH, 'Description'));
        if (!result.ok) validationFailure(result.errors);
      }
      changes.description = description;
    }

    const updated = await this.events.update(id, changes);
    return toView(updated);
  }

  /** Deletes a timeline event (soft-delete). */
  async deleteEvent(id: string): Promise<boolean> {
    const event = await this.events.getById(id);
    if (!event) return false;
    return this.events.delete(id);
  }

  /** Validates event input without persisting. */
  validateInput(input: TimelineEventInput): ValidationResult {
    const title = normalizeInput(input.title);
    const description = input.description ? normalizeInput(input.description) : null;

    return validate(
      textLength(title, 1, MAX_TITLE_LENGTH, 'Title'),
      validIsoDate(input.eventDate),
      description !== null ? textLength(description, 0, MAX_DESCRIPTION_LENGTH, 'Description') : { ok: true, errors: [] },
    );
  }

  /** Returns total event count. */
  async getCount(): Promise<number> {
    return this.events.count();
  }

  /** Exposes the repository search for search provider integration. */
  async search(query: string) {
    return this.events.search(query);
  }
}
