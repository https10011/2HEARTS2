/**
 * Timeline event domain model (Phase 9).
 *
 * Relationship history events — milestones and moments in the couple's story.
 * All data stays on-device (V1 local-first). No cloud storage, no sync.
 */

import { assertEntityConventions, type TombstonedEntity } from '../model/entity.ts';
import {
  optionalString,
  requireString,
  type EntitySerializer,
  type Params,
  type Row,
} from '../serialization/entitySerializer.ts';
import { isValidDateKey } from '../../utils/time.ts';

/** Timeline event entity. */
export interface TimelineEvent extends TombstonedEntity {
  /** Event title / name (e.g. "First Date", "Got Engaged"). */
  title: string;
  /** Local calendar day `yyyy-mm-dd`. */
  eventDate: string;
  /** User-provided description/details. Nullable. */
  description: string | null;
}

export function assertTimelineEvent(event: TimelineEvent): void {
  assertEntityConventions(event);
  if (event.title.trim().length === 0) {
    throw new Error('timelineEvent.title must not be empty');
  }
  if (!isValidDateKey(event.eventDate)) {
    throw new Error('timelineEvent.eventDate must be a yyyy-mm-dd calendar date');
  }
}

/** snake_case columns in serializer order. */
export const TIMELINE_COLUMNS = [
  'id',
  'title',
  'event_date',
  'description',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const timelineSerializer: EntitySerializer<TimelineEvent> = {
  columns: TIMELINE_COLUMNS,
  toParams(event: TimelineEvent): Params {
    return [
      event.id,
      event.title,
      event.eventDate,
      event.description,
      event.createdAt,
      event.updatedAt,
      event.deletedAt,
    ];
  },
  fromRow(row: Row): TimelineEvent {
    return {
      id: requireString(row, 'id'),
      title: requireString(row, 'title'),
      eventDate: requireString(row, 'event_date'),
      description: optionalString(row, 'description'),
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};
