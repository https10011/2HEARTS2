/**
 * Migration 004 — memories (schema version 4).
 *
 * Phase 7 memory tables:
 *
 * - `memories`: core memory entity with title, caption, memory_date,
 *   sort_order, and standard entity conventions (UUID, timestamps, tombstone).
 * - `memory_media`: join table linking memories to media_assets.
 *   One memory can have multiple media items; order is preserved via sort_order.
 *   FK references use ON DELETE CASCADE for memory deletion, and
 *   ON DELETE SET NULL for media asset deletion (memory survives missing media).
 */

import type { Migration } from './types.ts';

export const memoriesMigration: Migration = {
  id: 4,
  name: 'memories',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS memories (
        id TEXT PRIMARY KEY NOT NULL,
        title TEXT NOT NULL,
        caption TEXT,
        memory_date TEXT,
        sort_order INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        deleted_at TEXT
      );
      CREATE INDEX IF NOT EXISTS idx_memories_date
        ON memories (memory_date);
      CREATE INDEX IF NOT EXISTS idx_memories_sort
        ON memories (sort_order, created_at);
      CREATE INDEX IF NOT EXISTS idx_memories_deleted
        ON memories (deleted_at);

      CREATE TABLE IF NOT EXISTS memory_media (
        id TEXT PRIMARY KEY NOT NULL,
        memory_id TEXT NOT NULL REFERENCES memories (id) ON DELETE CASCADE,
        media_asset_id TEXT NOT NULL REFERENCES media_assets (id) ON DELETE SET NULL,
        sort_order INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL
      );
      CREATE INDEX IF NOT EXISTS idx_memory_media_memory
        ON memory_media (memory_id);
      CREATE UNIQUE INDEX IF NOT EXISTS idx_memory_media_unique
        ON memory_media (memory_id, media_asset_id);
    `);
  },
};
