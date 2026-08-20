/**
 * Vault repository (Phase 17).
 *
 * CRUD + query operations for the private vault system.
 * Vault items are protected content — they must NOT appear in global search
 * or ordinary feature lists.
 * Follows the existing repository architecture (Phase 2+).
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { newId } from '../utils/ids.ts';
import { nowIso, systemClock, type Clock } from '../utils/time.ts';
import {
  type VaultItem,
  type NewVaultItem,
  type VaultContentType,
  VAULT_ITEM_COLUMNS,
  vaultItemSerializer,
} from '../data/vault/vaultTypes.ts';

export class VaultRepository {
  constructor(
    private readonly adapter: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {}

  // -----------------------------------------------------------------------
  // Create
  // -----------------------------------------------------------------------

  async create(data: NewVaultItem): Promise<VaultItem> {
    const now = nowIso(this.clock);
    const item: VaultItem = {
      id: newId(),
      ...data,
      createdAt: now,
      updatedAt: now,
      deletedAt: null,
    };
    const params = vaultItemSerializer.toParams(item);
    const placeholders = VAULT_ITEM_COLUMNS.map(() => '?').join(', ');
    const columns = VAULT_ITEM_COLUMNS.join(', ');
    await this.adapter.run(
      `INSERT INTO vault_items (${columns}) VALUES (${placeholders})`,
      params,
    );
    return item;
  }

  // -----------------------------------------------------------------------
  // Read
  // -----------------------------------------------------------------------

  async getById(id: string): Promise<VaultItem | null> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM vault_items WHERE id = ? AND deleted_at IS NULL`,
      [id],
    );
    return rows[0] ? vaultItemSerializer.fromRow(rows[0]) : null;
  }

  async list(profileId: string): Promise<VaultItem[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM vault_items WHERE profile_id = ? AND deleted_at IS NULL ORDER BY created_at DESC`,
      [profileId],
    );
    return rows.map((r) => vaultItemSerializer.fromRow(r));
  }

  async listByType(profileId: string, contentType: VaultContentType): Promise<VaultItem[]> {
    const rows = await this.adapter.query<Row>(
      `SELECT * FROM vault_items WHERE profile_id = ? AND content_type = ? AND deleted_at IS NULL ORDER BY created_at DESC`,
      [profileId, contentType],
    );
    return rows.map((r) => vaultItemSerializer.fromRow(r));
  }

  async count(profileId: string): Promise<number> {
    const rows = await this.adapter.query<Row>(
      `SELECT COUNT(*) AS c FROM vault_items WHERE profile_id = ? AND deleted_at IS NULL`,
      [profileId],
    );
    const row = rows[0];
    return (row?.['c'] as number) ?? 0;
  }

  // -----------------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------------

  async update(id: string, data: Partial<NewVaultItem>): Promise<VaultItem | null> {
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
      `UPDATE vault_items SET ${setClauses.join(', ')} WHERE id = ?`,
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
      `UPDATE vault_items SET deleted_at = ?, updated_at = ? WHERE id = ? AND deleted_at IS NULL`,
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
