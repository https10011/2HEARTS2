/**
 * Migration 003 — relationship foundation (schema version 3).
 *
 * Phase 4 identity tables:
 *
 * - `profiles`: one row per person; `role` ('owner' | 'partner') is UNIQUE
 *   among live rows — the app models exactly one couple, but identity is
 *   still UUID-based for future sync. Dates are LOCAL calendar keys
 *   (`yyyy-mm-dd`), never timestamps (see utils/time).
 * - `couple_relationship`: SQL-enforced singleton (`singleton = 1`) joining
 *   owner + partner profiles and the relationship start date. References are
 *   nullable so onboarding can configure either side independently; FKs use
 *   ON DELETE SET NULL so a removed profile never orphans the couple row.
 * - `important_dates`: relationship/personal dates with recurrence for
 *   future reminder/notification scheduling; `profile_id` is nullable
 *   (null = relationship-level date).
 *
 * All three follow the Phase 2 entity conventions: UUID TEXT primary keys,
 * UTC ISO 8601 created_at/updated_at, tombstones where deletion must be
 * sync-propagatable.
 */

import type { Migration } from './types.ts';

export const relationshipFoundationMigration: Migration = {
  id: 3,
  name: 'relationship-foundation',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS profiles (
        id TEXT PRIMARY KEY NOT NULL,
        role TEXT NOT NULL CHECK (role IN ('owner', 'partner')),
        display_name TEXT NOT NULL,
        birth_date TEXT,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        deleted_at TEXT
      );
      CREATE UNIQUE INDEX IF NOT EXISTS idx_profiles_live_role
        ON profiles (role) WHERE deleted_at IS NULL;

      CREATE TABLE IF NOT EXISTS couple_relationship (
        id TEXT PRIMARY KEY NOT NULL,
        singleton INTEGER NOT NULL DEFAULT 1 UNIQUE CHECK (singleton = 1),
        owner_profile_id TEXT REFERENCES profiles (id) ON DELETE SET NULL,
        partner_profile_id TEXT REFERENCES profiles (id) ON DELETE SET NULL,
        start_date TEXT,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL
      );

      CREATE TABLE IF NOT EXISTS important_dates (
        id TEXT PRIMARY KEY NOT NULL,
        title TEXT NOT NULL,
        date TEXT NOT NULL,
        recurrence TEXT NOT NULL CHECK (recurrence IN ('none', 'yearly')),
        profile_id TEXT REFERENCES profiles (id) ON DELETE SET NULL,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        deleted_at TEXT
      );
      CREATE INDEX IF NOT EXISTS idx_important_dates_date
        ON important_dates (date);
      CREATE INDEX IF NOT EXISTS idx_important_dates_profile
        ON important_dates (profile_id);
    `);
  },
};
