/**
 * Migration 011 — Private Vault (Phase 17).
 *
 * Creates the `vault_items` table for protected content storage.
 * Vault content must NOT appear in global search, notifications,
 * or home previews.
 */

import type { Migration } from './types.ts';

export const vaultMigration: Migration = {
  id: 11,
  name: 'vault-items',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS vault_items (
        id              TEXT PRIMARY KEY NOT NULL,
        title           TEXT NOT NULL,
        content_type    TEXT NOT NULL,
        media_ref       TEXT,
        file_path       TEXT,
        content         TEXT,
        description     TEXT,
        profile_id      TEXT NOT NULL,
        created_at      TEXT NOT NULL,
        updated_at      TEXT NOT NULL,
        deleted_at      TEXT
      );
    `);

    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_vault_items_profile ON vault_items(profile_id) WHERE deleted_at IS NULL;`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_vault_items_type ON vault_items(content_type) WHERE deleted_at IS NULL;`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_vault_items_deleted ON vault_items(deleted_at);`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_vault_items_updated ON vault_items(updated_at);`);
  },
};
