/**
 * Migration 008 — Places (Phase 14).
 *
 * Creates the `places` table for storing the couple's meaningful locations.
 */

import type { Migration } from './types.ts';

export const placesMigration: Migration = {
  id: 8,
  name: 'places',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS places (
        id              TEXT PRIMARY KEY NOT NULL,
        name            TEXT NOT NULL,
        address         TEXT,
        city            TEXT,
        state           TEXT,
        country         TEXT,
        latitude        REAL,
        longitude       REAL,
        notes           TEXT,
        category        TEXT,
        photo_ref       TEXT,
        memory_id       TEXT,
        created_at      TEXT NOT NULL,
        updated_at      TEXT NOT NULL,
        deleted_at      TEXT
      );
    `);

    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_places_name ON places(name) WHERE deleted_at IS NULL;`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_places_city ON places(city) WHERE deleted_at IS NULL;`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_places_category ON places(category) WHERE deleted_at IS NULL;`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_places_deleted ON places(deleted_at);`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_places_updated ON places(updated_at);`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_places_memory ON places(memory_id) WHERE deleted_at IS NULL;`);
  },
};
