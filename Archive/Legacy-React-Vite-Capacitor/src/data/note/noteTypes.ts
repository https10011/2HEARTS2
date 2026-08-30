/**
 * Note domain model (Phase 8).
 *
 * Local-only notes for the TwoHearts V1 couples app.
 * All data stays on-device — no cloud storage, no network sync.
 */

import {
  requireString,
  optionalString,
  type Row,
} from '../serialization/entitySerializer.ts';
import type { EntitySerializer } from '../serialization/entitySerializer.ts';

/** Note categories — predefined list for V1. */
export type NoteCategory =
  | 'general'
  | 'shared'
  | 'private'
  | 'love-letter'
  | 'gratitude'
  | 'idea'
  | 'reminder';

export const NOTE_CATEGORIES: readonly NoteCategory[] = [
  'general',
  'shared',
  'private',
  'love-letter',
  'gratitude',
  'idea',
  'reminder',
] as const;

/** Database columns in serializer order. */
export const NOTE_COLUMNS = [
  'id',
  'title',
  'content',
  'category',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

/** Note domain entity. */
export interface Note {
  id: string;
  title: string;
  content: string;
  category: NoteCategory;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

/** Serializer: domain ↔ database rows. */
export const noteSerializer: EntitySerializer<Note> = {
  columns: NOTE_COLUMNS,
  toParams(note: Note): (string | number | null)[] {
    return [
      note.id,
      note.title,
      note.content,
      note.category,
      note.createdAt,
      note.updatedAt,
      note.deletedAt,
    ];
  },
  fromRow(row: Row): Note {
    return {
      id: requireString(row, 'id'),
      title: requireString(row, 'title'),
      content: optionalString(row, 'content') ?? '',
      category: requireString(row, 'category') as NoteCategory,
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};
