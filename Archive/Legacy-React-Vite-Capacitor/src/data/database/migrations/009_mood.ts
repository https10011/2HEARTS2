/**
 * Migration 009 — Mood (Phase 15).
 *
 * Creates the `mood_entries` table for tracking how each person is feeling.
 */

import type { Migration } from './types.ts';

export const moodMigration: Migration = {
  id: 9,
  name: 'mood-entries',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS mood_entries (
        id              TEXT PRIMARY KEY NOT NULL,
        mood_value      TEXT NOT NULL,
        mood_emoji      TEXT NOT NULL,
        note            TEXT,
        profile_id      TEXT NOT NULL,
        entry_date      TEXT NOT NULL,
        created_at      TEXT NOT NULL,
        updated_at      TEXT NOT NULL,
        deleted_at      TEXT
      );
    `);

    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_mood_entries_profile ON mood_entries(profile_id) WHERE deleted_at IS NULL;`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_mood_entries_date ON mood_entries(entry_date) WHERE deleted_at IS NULL;`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_mood_entries_deleted ON mood_entries(deleted_at);`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_mood_entries_updated ON mood_entries(updated_at);`);
    // One mood per person per day — unique partial index
    await tx.exec(`CREATE UNIQUE INDEX IF NOT EXISTS idx_mood_entries_unique_per_day ON mood_entries(profile_id, entry_date) WHERE deleted_at IS NULL;`);
  },
};
