/**
 * Migration 006 — timeline events (schema version 6).
 *
 * Phase 9 relationship timeline table:
 *
 * - `timeline_events`: core event entity with title, event_date,
 *   description, and standard entity conventions (UUID, timestamps, tombstone).
 */

import type { Migration } from './types.ts';

export const timelineMigration: Migration = {
  id: 6,
  name: 'timeline-events',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS timeline_events (
        id TEXT PRIMARY KEY NOT NULL,
        title TEXT NOT NULL,
        event_date TEXT NOT NULL,
        description TEXT,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        deleted_at TEXT
      );
      CREATE INDEX IF NOT EXISTS idx_timeline_events_date
        ON timeline_events (event_date ASC);
      CREATE INDEX IF NOT EXISTS idx_timeline_events_deleted
        ON timeline_events (deleted_at);
      CREATE INDEX IF NOT EXISTS idx_timeline_events_updated
        ON timeline_events (updated_at DESC);
    `);
  },
};
