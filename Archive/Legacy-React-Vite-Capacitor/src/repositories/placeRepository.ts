/**
 * Place repository (Phase 14).
 *
 * CRUD + query operations for the local places system.
 * Follows the existing repository architecture (Phase 2+).
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { newId } from '../utils/ids.ts';
import { nowIso, systemClock, type Clock } from '../utils/time.ts';
import {
  type Place,
  type NewPlace,
  PLACE_COLUMNS,
  placeSerializer,
} from '../data/place/placeTypes.ts';

export class PlaceRepository {
  constructor(
    private readonly adapter: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {}

  // -----------------------------------------------------------------------
  // Create
  // -----------------------------------------------------------------------

  async create(data: NewPlace): Promise<Place> {
    const now = nowIso(this.clock);
    const place: Place = {
      id: newId(),
      ...data,
      createdAt: now,
      updatedAt: now,
      deletedAt: null,
    };
    const params = placeSerializer.toParams(place);
    const placeholders = PLACE_COLUMNS.map(() => '?').join(', ');
    const columns = PLACE_COLUMNS.join(', ');
    await this.adapter.run(
      `INSERT INTO places (${columns}) VALUES (${placeholders})`,
      params,
    );
    return place;
  }

  // -----------------------------------------------------------------------
  // Read
  // -----------------------------------------------------------------------

  async getById(id: string): Promise<Place | null> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM places WHERE id = ? AND deleted_at IS NULL`,
      [id],
    );
    return rows[0] ? placeSerializer.fromRow(rows[0]) : null;
  }

  async list(): Promise<Place[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM places WHERE deleted_at IS NULL ORDER BY name ASC`,
    );
    return rows.map((r) => placeSerializer.fromRow(r));
  }

  async listByCategory(category: string): Promise<Place[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM places WHERE category = ? AND deleted_at IS NULL ORDER BY name ASC`,
      [category],
    );
    return rows.map((r) => placeSerializer.fromRow(r));
  }

  async search(query: string): Promise<Place[]> {
    const pattern = `%${query}%`;
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM places WHERE deleted_at IS NULL AND (name LIKE ? OR city LIKE ? OR address LIKE ? OR notes LIKE ?) ORDER BY name ASC`,
      [pattern, pattern, pattern, pattern],
    );
    return rows.map((r) => placeSerializer.fromRow(r));
  }

  async count(): Promise<number> {
    const rows = await this.adapter.query<Row>(
      `SELECT COUNT(*) AS c FROM places WHERE deleted_at IS NULL`,
    );
    const row = rows[0];
    return (row?.['c'] as number) ?? 0;
  }

  // -----------------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------------

  async update(id: string, data: Partial<NewPlace>): Promise<Place | null> {
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

    if (setClauses.length === 1) return existing; // nothing to update

    await this.adapter.run(
      `UPDATE places SET ${setClauses.join(', ')} WHERE id = ?`,
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
      `UPDATE places SET deleted_at = ?, updated_at = ? WHERE id = ? AND deleted_at IS NULL`,
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
