/**
 * Notification registry repository (Phase 3).
 *
 * Persists the owner-ref ↔ platform-id mapping used by NotificationService.
 * Rows are NOT Phase 2 `Entity` objects: platform notification ids are
 * INTEGER keys owned by the OS scheduler (stable while scheduled), so the
 * generic UUID repository does not apply here. Serialization stays explicit
 * (no ad-hoc JSON outside this file).
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import { isValidIsoTimestamp } from '../../utils/time.ts';

export interface NotificationRegistryEntry {
  /** Platform notification id (integer, OS-owned). */
  notificationId: number;
  /** Logical owner reference (feature-provided, unique per logical event). */
  ownerRef: string;
  channelId: string;
  title: string;
  body: string;
  /** ISO 8601 UTC schedule moment. */
  scheduledAt: string;
  /** Deterministically serialized metadata (feature-owned shape). */
  metadata: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

interface RegistryRow {
  notification_id: number;
  owner_ref: string;
  channel_id: string;
  title: string;
  body: string;
  scheduled_at: string;
  meta_json: string;
  created_at: string;
  updated_at: string;
}

function rowToEntry(row: RegistryRow): NotificationRegistryEntry {
  return {
    notificationId: row.notification_id,
    ownerRef: row.owner_ref,
    channelId: row.channel_id,
    title: row.title,
    body: row.body,
    scheduledAt: row.scheduled_at,
    metadata: parseMetadata(row.meta_json),
    createdAt: row.created_at,
    updatedAt: row.updated_at,
  };
}

function parseMetadata(raw: string): Record<string, unknown> {
  try {
    const parsed = JSON.parse(raw) as unknown;
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed)
      ? (parsed as Record<string, unknown>)
      : {};
  } catch {
    return {};
  }
}

export class NotificationRegistryRepository {
  constructor(private readonly adapter: DatabaseAdapter) {}

  async nextNotificationId(): Promise<number> {
    const rows = await this.adapter.query<{ max_id: number | null }>(
      'SELECT MAX(notification_id) AS max_id FROM notification_registry',
    );
    return (rows[0]?.max_id ?? 0) + 1;
  }

  async getByOwner(ownerRef: string): Promise<NotificationRegistryEntry | null> {
    const rows = await this.adapter.query<RegistryRow>(
      'SELECT * FROM notification_registry WHERE owner_ref = ?',
      [ownerRef],
    );
    return rows[0] ? rowToEntry(rows[0]) : null;
  }

  async getById(notificationId: number): Promise<NotificationRegistryEntry | null> {
    const rows = await this.adapter.query<RegistryRow>(
      'SELECT * FROM notification_registry WHERE notification_id = ?',
      [notificationId],
    );
    return rows[0] ? rowToEntry(rows[0]) : null;
  }

  async list(): Promise<NotificationRegistryEntry[]> {
    const rows = await this.adapter.query<RegistryRow>(
      'SELECT * FROM notification_registry ORDER BY scheduled_at ASC',
    );
    return rows.map(rowToEntry);
  }

  async upsert(entry: NotificationRegistryEntry): Promise<void> {
    if (!isValidIsoTimestamp(entry.scheduledAt)) {
      throw new Error('notification_registry.scheduled_at must be ISO 8601 UTC.');
    }
    await this.adapter.run(
      `INSERT INTO notification_registry
         (notification_id, owner_ref, channel_id, title, body, scheduled_at, meta_json, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
       ON CONFLICT(owner_ref) DO UPDATE SET
         notification_id = excluded.notification_id,
         channel_id = excluded.channel_id,
         title = excluded.title,
         body = excluded.body,
         scheduled_at = excluded.scheduled_at,
         meta_json = excluded.meta_json,
         updated_at = excluded.updated_at`,
      [
        entry.notificationId,
        entry.ownerRef,
        entry.channelId,
        entry.title,
        entry.body,
        entry.scheduledAt,
        JSON.stringify(entry.metadata ?? {}),
        entry.createdAt,
        entry.updatedAt,
      ],
    );
  }

  async deleteByOwner(ownerRef: string): Promise<void> {
    await this.adapter.run('DELETE FROM notification_registry WHERE owner_ref = ?', [ownerRef]);
  }

  async deleteById(notificationId: number): Promise<void> {
    await this.adapter.run('DELETE FROM notification_registry WHERE notification_id = ?', [notificationId]);
  }

  async count(): Promise<number> {
    const rows = await this.adapter.query<{ c: number }>('SELECT COUNT(*) AS c FROM notification_registry');
    return rows[0]?.c ?? 0;
  }
}
