/**
 * Note service (Phase 8).
 *
 * Application-facing boundary over NoteRepository.
 * Validates input, normalizes errors, provides clean state to UI.
 *
 * Layer discipline: UI → NoteService → NoteRepository → Local persistence.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import { AppError } from '../errors/appError.ts';
import {
  normalizeInput,
  textLength,
  validate,
  type ValidationResult,
} from '../validation/validators.ts';
import { NoteRepository } from '../../repositories/noteRepository.ts';
import {
  NOTE_CATEGORIES,
  type Note,
  type NoteCategory,
} from '../../data/note/noteTypes.ts';
import { systemClock, type Clock } from '../../utils/time.ts';

const MAX_TITLE_LENGTH = 200;
const MAX_CONTENT_LENGTH = 50000;

export interface NoteInput {
  title: string;
  content?: string;
  category?: NoteCategory;
}

/** Expose a safe subset of the Note for UI. */
export interface NoteView {
  id: string;
  title: string;
  content: string;
  excerpt: string;
  category: NoteCategory;
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

function toView(note: Note): NoteView {
  return {
    id: note.id,
    title: note.title,
    content: note.content,
    excerpt: NoteRepository.excerpt(note.content),
    category: note.category,
    createdAt: note.createdAt,
    updatedAt: note.updatedAt,
  };
}

export class NoteService {
  private readonly notes: NoteRepository;

  constructor(db: DatabaseAdapter, clock: Clock = systemClock) {
    this.notes = new NoteRepository(db, clock);
  }

  /** Lists all active notes. */
  async listNotes(): Promise<NoteView[]> {
    const entities = await this.notes.listNotes();
    return entities.map(toView);
  }

  /** Lists notes filtered by category. */
  async listByCategory(category: NoteCategory): Promise<NoteView[]> {
    const entities = await this.notes.listByCategory(category);
    return entities.map(toView);
  }

  /** Gets a single note by id. */
  async getNote(id: string): Promise<NoteView> {
    const note = await this.notes.getById(id);
    if (!note) {
      throw new AppError('persistence', 'not-found', {
        recoverable: false,
        userMessage: 'Note not found.',
      });
    }
    return toView(note);
  }

  /** Creates a new note. */
  async createNote(input: NoteInput): Promise<NoteView> {
    const title = normalizeInput(input.title);
    const content = input.content ?? '';
    const category: NoteCategory = input.category ?? 'general';

    // Validate
    const result = validate(
      textLength(title, 1, MAX_TITLE_LENGTH, 'Title'),
      textLength(content, 0, MAX_CONTENT_LENGTH, 'Content'),
      isValidCategory(category),
    );
    if (!result.ok) validationFailure(result.errors);

    const entity = await this.notes.create({
      title,
      content,
      category,
      deletedAt: null,
    });

    return toView(entity);
  }

  /** Updates an existing note. */
  async updateNote(id: string, input: Partial<NoteInput>): Promise<NoteView> {
    const existing = await this.notes.getById(id);
    if (!existing) {
      throw new AppError('persistence', 'not-found', {
        recoverable: false,
        userMessage: 'Note not found.',
      });
    }

    const changes: Record<string, unknown> = {};

    if (input.title !== undefined) {
      const title = normalizeInput(input.title);
      const result = validate(textLength(title, 1, MAX_TITLE_LENGTH, 'Title'));
      if (!result.ok) validationFailure(result.errors);
      changes.title = title;
    }

    if (input.content !== undefined) {
      const result = validate(
        textLength(input.content, 0, MAX_CONTENT_LENGTH, 'Content'),
      );
      if (!result.ok) validationFailure(result.errors);
      changes.content = input.content;
    }

    if (input.category !== undefined) {
      const result = validate(isValidCategory(input.category));
      if (!result.ok) validationFailure(result.errors);
      changes.category = input.category;
    }

    const updated = await this.notes.update(id, changes);
    return toView(updated);
  }

  /** Deletes a note (soft-delete). */
  async deleteNote(id: string): Promise<boolean> {
    const note = await this.notes.getById(id);
    if (!note) return false;
    return this.notes.delete(id);
  }

  /** Validates note input without persisting. */
  validateInput(input: NoteInput): ValidationResult {
    const title = normalizeInput(input.title);
    const content = input.content ?? '';
    const category: NoteCategory = input.category ?? 'general';

    return validate(
      textLength(title, 1, MAX_TITLE_LENGTH, 'Title'),
      textLength(content, 0, MAX_CONTENT_LENGTH, 'Content'),
      isValidCategory(category),
    );
  }

  /** Returns note counts by category for the hub. */
  async getCounts(): Promise<Record<string, number>> {
    const counts: Record<string, number> = {};
    for (const cat of NOTE_CATEGORIES) {
      counts[cat] = await this.notes.countByCategory(cat);
    }
    counts['all'] = await this.notes.count();
    return counts;
  }

  /** Exposes the repository search for search provider. */
  async search(query: string): Promise<Note[]> {
    return this.notes.search(query);
  }
}

function isValidCategory(category: string): ValidationResult {
  if (NOTE_CATEGORIES.includes(category as NoteCategory)) {
    return { ok: true, errors: [] };
  }
  return {
    ok: false,
    errors: [`Invalid category "${category}".`],
  };
}
