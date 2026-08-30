/**
 * Period repository (Phase 16).
 *
 * CRUD + query operations for the local period tracking system.
 * Two collections: period_entries and period_settings.
 * Follows the existing repository architecture (Phase 2+).
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { newId } from '../utils/ids.ts';
import { nowIso, systemClock, type Clock } from '../utils/time.ts';
import {
  type PeriodEntry,
  type NewPeriodEntry,
  type PeriodSettings,
  PERIOD_ENTRY_COLUMNS,
  PERIOD_SETTINGS_COLUMNS,
  periodEntrySerializer,
  periodSettingsSerializer,
} from '../data/period/periodTypes.ts';

export class PeriodRepository {
  constructor(
    private readonly adapter: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {}

  // -----------------------------------------------------------------------
  // Entry CRUD
  // -----------------------------------------------------------------------

  async createEntry(data: NewPeriodEntry): Promise<PeriodEntry> {
    const now = nowIso(this.clock);
    const entry: PeriodEntry = {
      id: newId(),
      ...data,
      createdAt: now,
      updatedAt: now,
      deletedAt: null,
    };
    const params = periodEntrySerializer.toParams(entry);
    const placeholders = PERIOD_ENTRY_COLUMNS.map(() => '?').join(', ');
    const columns = PERIOD_ENTRY_COLUMNS.join(', ');
    await this.adapter.run(
      `INSERT INTO period_entries (${columns}) VALUES (${placeholders})`,
      params,
    );
    return entry;
  }

  async getEntryById(id: string): Promise<PeriodEntry | null> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM period_entries WHERE id = ? AND deleted_at IS NULL`,
      [id],
    );
    return rows[0] ? periodEntrySerializer.fromRow(rows[0]) : null;
  }

  async listEntries(profileId: string): Promise<PeriodEntry[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM period_entries WHERE profile_id = ? AND deleted_at IS NULL ORDER BY start_date DESC`,
      [profileId],
    );
    return rows.map((r) => periodEntrySerializer.fromRow(r));
  }

  async listEntriesInRange(profileId: string, startDate: string, endDate: string): Promise<PeriodEntry[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM period_entries WHERE profile_id = ? AND deleted_at IS NULL AND start_date >= ? AND start_date <= ? ORDER BY start_date ASC`,
      [profileId, startDate, endDate],
    );
    return rows.map((r) => periodEntrySerializer.fromRow(r));
  }

  async updateEntry(id: string, data: Partial<NewPeriodEntry>): Promise<PeriodEntry | null> {
    const existing = await this.getEntryById(id);
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
      `UPDATE period_entries SET ${setClauses.join(', ')} WHERE id = ?`,
      params as (string | number | null)[],
    );
    return this.getEntryById(id);
  }

  async deleteEntry(id: string): Promise<boolean> {
    const now = nowIso(this.clock);
    const result = await this.adapter.run(
      `UPDATE period_entries SET deleted_at = ?, updated_at = ? WHERE id = ? AND deleted_at IS NULL`,
      [now, now, id],
    );
    return (result.changes ?? 0) > 0;
  }

  // -----------------------------------------------------------------------
  // Settings (singleton per profile)
  // -----------------------------------------------------------------------

  async getSettings(profileId: string): Promise<PeriodSettings | null> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM period_settings WHERE profile_id = ?`,
      [profileId],
    );
    return rows[0] ? periodSettingsSerializer.fromRow(rows[0]) : null;
  }

  async saveSettings(profileId: string, cycleDays: number, periodDays: number): Promise<PeriodSettings> {
    const existing = await this.getSettings(profileId);
    const now = nowIso(this.clock);

    if (existing) {
      await this.adapter.run(
        `UPDATE period_settings SET cycle_length_days = ?, period_length_days = ?, updated_at = ? WHERE profile_id = ?`,
        [cycleDays, periodDays, now, profileId],
      );
      return (await this.getSettings(profileId))!;
    }

    const settings: PeriodSettings = {
      id: newId(),
      profileId,
      cycleLengthDays: cycleDays,
      periodLengthDays: periodDays,
      createdAt: now,
      updatedAt: now,
    };
    const params = periodSettingsSerializer.toParams(settings);
    const placeholders = PERIOD_SETTINGS_COLUMNS.map(() => '?').join(', ');
    const columns = PERIOD_SETTINGS_COLUMNS.join(', ');
    await this.adapter.run(
      `INSERT INTO period_settings (${columns}) VALUES (${placeholders})`,
      params,
    );
    return settings;
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  private camelToSnake(str: string): string {
    return str.replace(/[A-Z]/g, (c) => `_${c.toLowerCase()}`);
  }
}
