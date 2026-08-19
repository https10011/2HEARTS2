/**
 * Repository conventions (Phase 2).
 *
 * Repositories are the ONLY layer feature code talks to. They expose domain
 * objects — never rows, never SQL — and they compose transactions via the
 * adapter, so atomicity stays an infrastructure detail.
 *
 * Rules:
 * - `create` assigns the UUID + timestamps (injectable clock for tests),
 * - `update` refreshes `updatedAt` and cannot change identity fields,
 * - `delete` returns whether anything was removed; soft-delete-capable
 *   entities use tombstones (TombstonedEntity) so a future V2 sync layer
 *   can propagate deletions,
 * - list order is deterministic (created_at, then id).
 */

import type { DatabaseAdapter, TransactionContext } from '../data/database/adapter.ts';
import type { Entity, NewEntity } from '../data/model/entity.ts';
import { nowIso, type Clock, systemClock } from '../utils/time.ts';
import { newId } from '../utils/ids.ts';

export interface Repository<T extends Entity> {
  getById(id: string, options?: { includeDeleted?: boolean }): Promise<T | null>;
  list(options?: { includeDeleted?: boolean }): Promise<T[]>;
  create(input: NewEntity<T>): Promise<T>;
  update(id: string, changes: Partial<NewEntity<T>>): Promise<T>;
  delete(id: string): Promise<boolean>;
  exists(id: string): Promise<boolean>;
}

/**
 * Shared base for SQL-backed repositories: identity + timestamp assignment
 * and the standard CRUD against one table, via the per-entity serializer.
 */
export abstract class BaseRepository<T extends Entity> implements Repository<T> {
  constructor(
    protected readonly db: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {}

  /** SELECT column list for this entity, snake_case in serializer order. */
  protected abstract readonly table: string;
  protected abstract readonly serializer: {
    columns: readonly string[];
    toParams(entity: T): (string | number | boolean | null)[];
    fromRow(row: Record<string, unknown>): T;
  };

  async getById(id: string, options: { includeDeleted?: boolean } = {}): Promise<T | null> {
    const tombstoneFilter = this.hasTombstone() && !options.includeDeleted ? ' AND deleted_at IS NULL' : '';
    const rows = await this.db.query<Record<string, unknown>>(
      `SELECT ${this.serializer.columns.join(', ')} FROM ${this.table} WHERE id = ?${tombstoneFilter}`,
      [id],
    );
    if (rows.length === 0) return null;
    return this.serializer.fromRow(rows[0]);
  }

  async list(options: { includeDeleted?: boolean } = {}): Promise<T[]> {
    const tombstoneFilter = this.hasTombstone() && !options.includeDeleted ? ' WHERE deleted_at IS NULL' : '';
    const rows = await this.db.query<Record<string, unknown>>(
      `SELECT ${this.serializer.columns.join(', ')} FROM ${this.table}${tombstoneFilter} ORDER BY created_at ASC, id ASC`,
    );
    return rows.map((row) => this.serializer.fromRow(row));
  }

  async create(input: NewEntity<T>): Promise<T> {
    const timestamp = nowIso(this.clock);
    const entity = {
      ...input,
      id: newId(),
      createdAt: timestamp,
      updatedAt: timestamp,
      ...(this.hasTombstone() ? { deletedAt: null } : {}),
    } as T;
    await this.db.run(
      `INSERT INTO ${this.table} (${this.serializer.columns.join(', ')}) VALUES (${this.serializer.columns.map(() => '?').join(', ')})`,
      this.serializer.toParams(entity),
    );
    return entity;
  }

  async update(id: string, changes: Partial<NewEntity<T>>): Promise<T> {
    const existing = await this.getById(id);
    if (!existing) {
      throw new Error('Entity not found');
    }
    const updated = { ...existing, ...changes, updatedAt: nowIso(this.clock) } as T;
    const assignments = this.serializer.columns
      .filter((column) => column !== 'id')
      .map((column) => `${column} = ?`)
      .join(', ');
    const params = this.serializer.toParams(updated);
    await this.db.run(
      `UPDATE ${this.table} SET ${assignments} WHERE id = ?`,
      [...params.slice(1), params[0]],
    );
    return updated;
  }

  async delete(id: string): Promise<boolean> {
    if (this.hasTombstone()) {
      const existing = await this.getById(id);
      if (!existing || (existing as unknown as { deletedAt?: string | null }).deletedAt) return false;
      // `NewEntity` keeps deletedAt; the guard above picks tombstoned entities.
      const tombstone = { deletedAt: nowIso(this.clock) } as unknown as Partial<NewEntity<T>>;
      return (await this.update(id, tombstone)) !== null;
    }
    const result = await this.db.run(`DELETE FROM ${this.table} WHERE id = ?`, [id]);
    return result.changes > 0;
  }

  async exists(id: string): Promise<boolean> {
    const tombstoneFilter = this.hasTombstone() ? ' AND deleted_at IS NULL' : '';
    const rows = await this.db.query<{ n: number }>(
      `SELECT COUNT(1) AS n FROM ${this.table} WHERE id = ?${tombstoneFilter}`,
      [id],
    );
    return (rows[0]?.n ?? 0) > 0;
  }

  /** Combines multiple writes atomically. Compose at the repository layer. */
  protected transaction<R>(fn: (tx: TransactionContext) => Promise<R>): Promise<R> {
    return this.db.transaction(fn);
  }

  private hasTombstone(): boolean {
    return this.serializer.columns.includes('deleted_at');
  }
}
