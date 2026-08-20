/**
 * Migration 005 — notes (schema version 5).
 *
 * Phase 8 local notes tables:
 *
 * - `notes`: core note entity with title, content, category,
 *   and standard entity conventions (UUID, timestamps, tombstone).
 */

import type { Migration } from './types.ts';

export const notesMigration: Migration = {
  id: 5,
  name: 'notes',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS notes (
        id TEXT PRIMARY KEY NOT NULL,
        title TEXT NOT NULL,
        content TEXT NOT NULL DEFAULT '',
        category TEXT NOT NULL DEFAULT 'general',
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        deleted_at TEXT
      );
      CREATE INDEX IF NOT EXISTS idx_notes_category
        ON notes (category);
      CREATE INDEX IF NOT EXISTS idx_notes_deleted
        ON notes (deleted_at);
      CREATE INDEX IF NOT EXISTS idx_notes_updated
        ON notes (updated_at DESC);
    `);
  },
};
