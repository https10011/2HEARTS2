/**
 * Migration 001 — initial schema (schema version 1).
 *
 * Establishes the Phase 2 foundation tables:
 *
 * - `media_assets`: metadata + safe relative-path references for locally
 *   stored photos/videos. Binaries NEVER live in this table; the filesystem
 *   path convention is owned by src/data/media. Tombstone (`deleted_at`)
 *   prepares the entity for the future V2 sync boundary.
 *
 * Nothing here is feature data — feature tables (memories, notes, …) are
 * added by their owning phases as further migrations.
 */

import type { Migration } from './types.ts';

export const initialSchemaMigration: Migration = {
  id: 1,
  name: 'initial-schema',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS media_assets (
        id TEXT PRIMARY KEY NOT NULL,
        kind TEXT NOT NULL CHECK (kind IN ('photo', 'video')),
        mime_type TEXT NOT NULL,
        relative_path TEXT NOT NULL UNIQUE,
        size_bytes INTEGER,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        deleted_at TEXT
      );
    `);
  },
};
