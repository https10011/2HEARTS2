/**
 * Mood repository (Phase 15).
 *
 * CRUD + query operations for the local mood tracking system.
 * Follows the existing repository architecture (Phase 2+).
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { newId } from '../utils/ids.ts';
import { nowIso, systemClock, type Clock } from '../utils/time.ts';
import {
  type MoodEntry,
  type NewMoodEntry,
  MOOD_COLUMNS,
  moodSerializer,
} from '../data/mood/moodTypes.ts';

export class MoodRepository {
  constructor(
    private readonly adapter: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {}

  // -----------------------------------------------------------------------
  // Create (upsert: one mood per profile per day)
  // -----------------------------------------------------------------------

  async createOrUpdate(data: NewMoodEntry): Promise<MoodEntry> {
    // Check for existing entry on this date for this profile
    const existing = await this.getByProfileAndDate(data.profileId, data.entryDate);
    if (existing) {
      return this.update(existing.id, {
        moodValue: data.moodValue,
        moodEmoji: data.moodEmoji,
        note: data.note,
      }) as Promise<MoodEntry>;
    }

    const now = nowIso(this.clock);
    const entry: MoodEntry = {
      id: newId(),
      ...data,
      createdAt: now,
      updatedAt: now,
      deletedAt: null,
    };
    const params = moodSerializer.toParams(entry);
    const placeholders = MOOD_COLUMNS.map(() => '?').join(', ');
    const columns = MOOD_COLUMNS.join(', ');
    await this.adapter.run(
      `INSERT INTO mood_entries (${columns}) VALUES (${placeholders})`,
      params,
    );
    return entry;
  }

  // -----------------------------------------------------------------------
  // Read
  // -----------------------------------------------------------------------

  async getById(id: string): Promise<MoodEntry | null> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM mood_entries WHERE id = ? AND deleted_at IS NULL`,
      [id],
    );
    return rows[0] ? moodSerializer.fromRow(rows[0]) : null;
  }

  async getByProfileAndDate(profileId: string, date: string): Promise<MoodEntry | null> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM mood_entries WHERE profile_id = ? AND entry_date = ? AND deleted_at IS NULL`,
      [profileId, date],
    );
    return rows[0] ? moodSerializer.fromRow(rows[0]) : null;
  }

  async list(): Promise<MoodEntry[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM mood_entries WHERE deleted_at IS NULL ORDER BY entry_date DESC, created_at DESC`,
    );
    return rows.map((r) => moodSerializer.fromRow(r));
  }

  async listByProfile(profileId: string, limit = 30): Promise<MoodEntry[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM mood_entries WHERE profile_id = ? AND deleted_at IS NULL ORDER BY entry_date DESC LIMIT ?`,
      [profileId, limit],
    );
    return rows.map((r) => moodSerializer.fromRow(r));
  }

  async listRecent(limit = 10): Promise<MoodEntry[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM mood_entries WHERE deleted_at IS NULL ORDER BY entry_date DESC, created_at DESC LIMIT ?`,
      [limit],
    );
    return rows.map((r) => moodSerializer.fromRow(r));
  }

  async count(): Promise<number> {
    const rows = await this.adapter.query<Row>(
      `SELECT COUNT(*) AS c FROM mood_entries WHERE deleted_at IS NULL`,
    );
    const row = rows[0];
    return (row?.['c'] as number) ?? 0;
  }

  // -----------------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------------

  async update(id: string, data: Partial<NewMoodEntry>): Promise<MoodEntry | null> {
    const existing = await this.getById(id);
    if (!existing) return null;

    const now = nowIso(this.clock);
    const setClauses: string[] = [];
    const params: unknown[] = [];
    for (const [key, value] of Object.entries(data)) {
      if (value === undefined) continue;
      const column = this.camelToSnake(key);
      setClauses.push(`${column} = ?`);
      params.push(value);
    }
    setClauses.push('updated_at = ?');
    params.push(now);
    params.push(id);

    if (setClauses.length === 1) return existing;

    await this.adapter.run(
      `UPDATE mood_entries SET ${setClauses.join(', ')} WHERE id = ?`,
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
      `UPDATE mood_entries SET deleted_at = ?, updated_at = ? WHERE id = ? AND deleted_at IS NULL`,
      [now, now, id],
    );
    return (result.changes ?? 0) > 0;
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  private camelToSnake(str: string): string {
    return str.replace(/[A-Z]/g, (c) => `_${c.toLowerCase()}`);
  }
}
