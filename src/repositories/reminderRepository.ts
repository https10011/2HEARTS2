/**
 * Reminder repository (Phase 13).
 *
 * CRUD + query operations for the local reminder system.
 * Follows the existing repository architecture (Phase 2+).
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { newId } from '../utils/ids.ts';
import { nowIso, systemClock, type Clock } from '../utils/time.ts';
import {
  type Reminder,
  type NewReminder,
  REMINDER_COLUMNS,
  reminderSerializer,
} from '../data/reminder/reminderTypes.ts';

export class ReminderRepository {
  constructor(
    private readonly adapter: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {}

  // -----------------------------------------------------------------------
  // Create
  // -----------------------------------------------------------------------

  async create(data: NewReminder): Promise<Reminder> {
    const now = nowIso(this.clock);
    const reminder: Reminder = {
      id: newId(),
      ...data,
      createdAt: now,
      updatedAt: now,
      deletedAt: null,
    };
    const params = reminderSerializer.toParams(reminder);
    const placeholders = REMINDER_COLUMNS.map(() => '?').join(', ');
    const columns = REMINDER_COLUMNS.join(', ');
    await this.adapter.run(
      `INSERT INTO reminders (${columns}) VALUES (${placeholders})`,
      params,
    );
    return reminder;
  }

  // -----------------------------------------------------------------------
  // Read
  // -----------------------------------------------------------------------

  async getById(id: string): Promise<Reminder | null> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM reminders WHERE id = ? AND deleted_at IS NULL`,
      [id],
    );
    return rows[0] ? reminderSerializer.fromRow(rows[0]) : null;
  }

  async list(): Promise<Reminder[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM reminders WHERE deleted_at IS NULL ORDER BY scheduled_date ASC, scheduled_time ASC`,
    );
    return rows.map((r) => reminderSerializer.fromRow(r));
  }

  async listByStatus(status: string): Promise<Reminder[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM reminders WHERE status = ? AND deleted_at IS NULL ORDER BY scheduled_date ASC, scheduled_time ASC`,
      [status],
    );
    return rows.map((r) => reminderSerializer.fromRow(r));
  }

  async listUpcoming(): Promise<Reminder[]> {
    const today = this.localDateKey();
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM reminders WHERE scheduled_date >= ? AND status = 'active' AND deleted_at IS NULL ORDER BY scheduled_date ASC, scheduled_time ASC`,
      [today],
    );
    return rows.map((r) => reminderSerializer.fromRow(r));
  }

  async listOverdue(): Promise<Reminder[]> {
    const today = this.localDateKey();
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM reminders WHERE scheduled_date < ? AND status = 'active' AND deleted_at IS NULL ORDER BY scheduled_date ASC, scheduled_time ASC`,
      [today],
    );
    return rows.map((r) => reminderSerializer.fromRow(r));
  }

  async count(): Promise<number> {
    const rows = await this.adapter.query<Row>(
      `SELECT COUNT(*) AS c FROM reminders WHERE deleted_at IS NULL`,
    );
    const row = rows[0];
    return (row?.['c'] as number) ?? 0;
  }

  // -----------------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------------

  async update(id: string, data: Partial<NewReminder>): Promise<Reminder | null> {
    const existing = await this.getById(id);
    if (!existing) return null;

    const now = nowIso(this.clock);
    const setClauses: string[] = [];
    const params: unknown[] = [];
    for (const [key, value] of Object.entries(data)) {
      if (value === undefined) continue;
      const column = this.camelToSnake(key);
      if (column === 'notification_enabled') {
        setClauses.push(`${column} = ?`);
        params.push(value ? 1 : 0);
      } else {
        setClauses.push(`${column} = ?`);
        params.push(value);
      }
    }
    setClauses.push('updated_at = ?');
    params.push(now);
    params.push(id);

    await this.adapter.run(
      `UPDATE reminders SET ${setClauses.join(', ')} WHERE id = ?`,
      params as (string | number | null)[],
    );
    return this.getById(id);
  }

  // -----------------------------------------------------------------------
  // Delete (soft)
  // -----------------------------------------------------------------------

  async delete(id: string): Promise<boolean> {
    const now = nowIso(this.clock);
    const result = await this.adapter.run(
      `UPDATE reminders SET deleted_at = ?, updated_at = ? WHERE id = ? AND deleted_at IS NULL`,
      [now, now, id],
    );
    return (result.changes ?? 0) > 0;
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  private localDateKey(): string {
    const now = new Date(this.clock());
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  private camelToSnake(str: string): string {
    return str.replace(/[A-Z]/g, (c) => `_${c.toLowerCase()}`);
  }
}
