/**
 * Migration 010 — Period Tracker (Phase 16).
 *
 * Creates:
 * - `period_entries`  — individual period logs.
 * - `period_settings` — user preferences (singleton per profile).
 */

import type { Migration } from './types.ts';

export const periodTrackerMigration: Migration = {
  id: 10,
  name: 'period-tracker',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS period_entries (
        id              TEXT PRIMARY KEY NOT NULL,
        start_date      TEXT NOT NULL,
        end_date        TEXT,
        flow_level      TEXT NOT NULL DEFAULT 'medium',
        note            TEXT,
        profile_id      TEXT NOT NULL,
        created_at      TEXT NOT NULL,
        updated_at      TEXT NOT NULL,
        deleted_at      TEXT
      );
    `);

    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_period_entries_profile ON period_entries(profile_id) WHERE deleted_at IS NULL;`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_period_entries_start ON period_entries(start_date) WHERE deleted_at IS NULL;`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_period_entries_deleted ON period_entries(deleted_at);`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_period_entries_updated ON period_entries(updated_at);`);

    await tx.exec(`
      CREATE TABLE IF NOT EXISTS period_settings (
        id                  TEXT PRIMARY KEY NOT NULL,
        profile_id          TEXT NOT NULL UNIQUE,
        cycle_length_days   INTEGER NOT NULL DEFAULT 28,
        period_length_days  INTEGER NOT NULL DEFAULT 5,
        created_at          TEXT NOT NULL,
        updated_at          TEXT NOT NULL
      );
    `);
  },
};
