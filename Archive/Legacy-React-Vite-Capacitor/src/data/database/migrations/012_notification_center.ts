/**
 * Migration 012 — Notification Center (Phase 18).
 *
 * Creates the `notification_center` table for local notification history.
 * Used by the Notification Center UI for displaying notification records.
 */

import type { Migration } from './types.ts';

export const notificationCenterMigration: Migration = {
  id: 12,
  name: 'notification-center',
  up: async (tx) => {
    await tx.exec(`
      CREATE TABLE IF NOT EXISTS notification_center (
        id              TEXT PRIMARY KEY NOT NULL,
        title           TEXT NOT NULL,
        body            TEXT NOT NULL,
        kind            TEXT NOT NULL DEFAULT 'system',
        origin_feature  TEXT NOT NULL DEFAULT '',
        origin_id       TEXT,
        channel_id      TEXT NOT NULL DEFAULT 'general',
        read            INTEGER NOT NULL DEFAULT 0,
        created_at      TEXT NOT NULL,
        updated_at      TEXT NOT NULL
      );
    `);

    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_nc_kind ON notification_center(kind);`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_nc_read ON notification_center(read);`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_nc_created ON notification_center(created_at DESC);`);
    await tx.exec(`CREATE INDEX IF NOT EXISTS idx_nc_origin ON notification_center(origin_feature, origin_id);`);
  },
};
