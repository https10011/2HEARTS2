/**
 * Migration 007 — reminders (schema version 7).
 *
 * Phase 13 local reminder system table:
 *
 * - `reminders`: title, description, scheduled_date, scheduled_time,
 *   recurrence, status, notification fields, standard entity conventions.
 */

import type { Migration } from './types.ts';

export const remindersMigration: Migration = {
  id: 7,
  name: 'reminders',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS reminders (
        id TEXT PRIMARY KEY NOT NULL,
        title TEXT NOT NULL,
        description TEXT,
        scheduled_date TEXT NOT NULL,
        scheduled_time TEXT NOT NULL,
        recurrence TEXT NOT NULL DEFAULT 'none',
        status TEXT NOT NULL DEFAULT 'active',
        notification_owner_ref TEXT,
        notification_enabled INTEGER NOT NULL DEFAULT 1,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        deleted_at TEXT
      );
      CREATE INDEX IF NOT EXISTS idx_reminders_date
        ON reminders (scheduled_date ASC);
      CREATE INDEX IF NOT EXISTS idx_reminders_status
        ON reminders (status);
      CREATE INDEX IF NOT EXISTS idx_reminders_deleted
        ON reminders (deleted_at);
      CREATE INDEX IF NOT EXISTS idx_reminders_updated
        ON reminders (updated_at DESC);
      CREATE INDEX IF NOT EXISTS idx_reminders_notification_owner
        ON reminders (notification_owner_ref);
    `);
  },
};
