/**
 * Notification Center repository (Phase 18).
 *
 * Manages local notification history records for the Notification Center.
 * These are distinct from the notification_registry (OS-level scheduling).
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { newId } from '../utils/ids.ts';
import { nowIso, systemClock, type Clock } from '../utils/time.ts';
import {
  NOTIFICATION_CENTER_COLUMNS,
  notificationCenterSerializer,
  type NotificationCenterEntry,
  type NewNotificationCenterEntry,
} from '../data/notification/notificationCenterTypes.ts';

export class NotificationCenterRepository {
  constructor(
    private readonly adapter: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {}

  async create(data: NewNotificationCenterEntry): Promise<NotificationCenterEntry> {
    const now = nowIso(this.clock);
    const entry: NotificationCenterEntry = {
      id: newId(),
      ...data,
      read: false,
      createdAt: now,
      updatedAt: now,
    };
    const params = notificationCenterSerializer.toParams(entry);
    const columns = NOTIFICATION_CENTER_COLUMNS.join(', ');
    const placeholders = NOTIFICATION_CENTER_COLUMNS.map(() => '?').join(', ');
    await this.adapter.run(
      `INSERT INTO notification_center (${columns}) VALUES (${placeholders})`,
      params,
    );
    return entry;
  }

  async getById(id: string): Promise<NotificationCenterEntry | null> {
    const rows = await this.adapter.query<Row>(
      `SELECT ${NOTIFICATION_CENTER_COLUMNS.join(', ')} FROM notification_center WHERE id = ?`,
      [id],
    );
    return rows[0] ? notificationCenterSerializer.fromRow(rows[0]) : null;
  }

  async list(): Promise<NotificationCenterEntry[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT ${NOTIFICATION_CENTER_COLUMNS.join(', ')} FROM notification_center ORDER BY created_at DESC`,
    );
    return rows.map((r) => notificationCenterSerializer.fromRow(r));
  }

  async listUnread(): Promise<NotificationCenterEntry[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT ${NOTIFICATION_CENTER_COLUMNS.join(', ')} FROM notification_center WHERE read = 0 ORDER BY created_at DESC`,
    );
    return rows.map((r) => notificationCenterSerializer.fromRow(r));
  }

  async count(): Promise<number> {
    const rows = await this.adapter.query<{ c: number }>(
      `SELECT COUNT(*) AS c FROM notification_center`,
    );
    return rows[0]?.c ?? 0;
  }

  async countUnread(): Promise<number> {
    const rows = await this.adapter.query<{ c: number }>(
      `SELECT COUNT(*) AS c FROM notification_center WHERE read = 0`,
    );
    return rows[0]?.c ?? 0;
  }

  async markAsRead(id: string): Promise<boolean> {
    const now = nowIso(this.clock);
    const result = await this.adapter.run(
      `UPDATE notification_center SET read = 1, updated_at = ? WHERE id = ?`,
      [now, id],
    );
    return (result.changes ?? 0) > 0;
  }

  async markAllAsRead(): Promise<number> {
    const now = nowIso(this.clock);
    const result = await this.adapter.run(
      `UPDATE notification_center SET read = 1, updated_at = ? WHERE read = 0`,
      [now],
    );
    return result.changes ?? 0;
  }

  async delete(id: string): Promise<boolean> {
    const result = await this.adapter.run(
      `DELETE FROM notification_center WHERE id = ?`,
      [id],
    );
    return (result.changes ?? 0) > 0;
  }

  async clearAll(): Promise<number> {
    const result = await this.adapter.run(
      `DELETE FROM notification_center`,
    );
    return result.changes ?? 0;
  }
}
