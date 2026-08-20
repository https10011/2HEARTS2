/**
 * Note repository (Phase 8).
 *
 * Standard entity CRUD via BaseRepository, plus note-specific queries:
 * - list sorted by updated_at DESC (newest first)
 * - list by category
 * - search by title/content (used by search provider)
 */

import {
  NOTE_COLUMNS,
  noteSerializer,
  type Note,
  type NoteCategory,
} from '../data/note/noteTypes.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { BaseRepository } from './repository.ts';

export class NoteRepository extends BaseRepository<Note> {
  protected readonly table = 'notes';
  protected readonly serializer = noteSerializer;

  /**
   * Lists active notes sorted by updated_at DESC (newest first).
   */
  async listNotes(): Promise<Note[]> {
    const rows = await this.db.query<Row>(
      `SELECT ${NOTE_COLUMNS.join(', ')} FROM notes
       WHERE deleted_at IS NULL
       ORDER BY updated_at DESC, title ASC`,
    );
    return rows.map((row) => noteSerializer.fromRow(row));
  }

  /**
   * Lists active notes filtered by category.
   */
  async listByCategory(category: NoteCategory): Promise<Note[]> {
    const rows = await this.db.query<Row>(
      `SELECT ${NOTE_COLUMNS.join(', ')} FROM notes
       WHERE deleted_at IS NULL AND category = ?
       ORDER BY updated_at DESC, title ASC`,
      [category],
    );
    return rows.map((row) => noteSerializer.fromRow(row));
  }

  /**
   * Search notes by title and content (case-insensitive LIKE).
   * Returns matches with a basic relevance score for search provider integration.
   */
  async search(query: string): Promise<Note[]> {
    const pattern = `%${query}%`;
    const rows = await this.db.query<Row>(
      `SELECT ${NOTE_COLUMNS.join(', ')} FROM notes
       WHERE deleted_at IS NULL
         AND (title LIKE ? OR content LIKE ?)
       ORDER BY updated_at DESC, title ASC`,
      [pattern, pattern],
    );
    return rows.map((row) => noteSerializer.fromRow(row));
  }

  /**
   * Returns a content excerpt (first N chars) for a note.
   */
  static excerpt(content: string, maxLength = 120): string {
    if (content.length <= maxLength) return content;
    return content.slice(0, maxLength).trimEnd() + '…';
  }

  /**
   * Counts active notes.
   */
  async count(): Promise<number> {
    const rows = await this.db.query<{ n: number }>(
      'SELECT COUNT(1) AS n FROM notes WHERE deleted_at IS NULL',
    );
    return rows[0]?.n ?? 0;
  }

  /**
   * Counts active notes by category.
   */
  async countByCategory(category: NoteCategory): Promise<number> {
    const rows = await this.db.query<{ n: number }>(
      'SELECT COUNT(1) AS n FROM notes WHERE deleted_at IS NULL AND category = ?',
      [category],
    );
    return rows[0]?.n ?? 0;
  }
}
