/**
 * Migration 002 — notification registry (schema version 2).
 *
 * @capacitor/local-notifications identifies scheduled notifications by
 * INTEGER ids, while TwoHearts domain identity is UUID-based (Phase 2) and
 * future features cancel/update by their OWN logical reference (a reminder
 * id, an anniversary id, …). This table is the bridge:
 *
 * - stable mapping logical owner ref ↔ platform notification id,
 * - `owner_ref` UNIQUE prevents duplicate scheduling for one logical event,
 * - metadata (`meta_json`) travels with the schedule so updates/reconciliation
 *   never re-read feature tables,
 * - `scheduled_at` is ISO 8601 UTC (Phase 2 convention).
 */

import type { Migration } from './types.ts';

export const notificationRegistryMigration: Migration = {
  id: 2,
  name: 'notification-registry',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS notification_registry (
        notification_id INTEGER PRIMARY KEY NOT NULL,
        owner_ref TEXT NOT NULL UNIQUE,
        channel_id TEXT NOT NULL,
        title TEXT NOT NULL,
        body TEXT NOT NULL,
        scheduled_at TEXT NOT NULL,
        meta_json TEXT NOT NULL DEFAULT '{}',
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL
      );
      CREATE INDEX IF NOT EXISTS idx_notification_registry_owner
        ON notification_registry (owner_ref);
    `);
  },
};
