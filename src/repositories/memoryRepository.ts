/**
 * Memory repository (Phase 7).
 *
 * Standard entity CRUD via BaseRepository, plus memory-specific queries:
 * - list sorted by sort_order then created_at (newest first)
 * - list with media references
 * - media association management (add/remove)
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import {
  MEMORY_COLUMNS,
  memorySerializer,
  MEMORY_MEDIA_COLUMNS,
  memoryMediaSerializer,
  type Memory,
  type MemoryMedia,
} from '../data/memory/memoryTypes.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { newId } from '../utils/ids.ts';
import { nowIso, systemClock, type Clock } from '../utils/time.ts';
import { BaseRepository } from './repository.ts';

export class MemoryRepository extends BaseRepository<Memory> {
  protected readonly table = 'memories';
  protected readonly serializer = memorySerializer;
  private readonly myClock: Clock;

  constructor(db: DatabaseAdapter, clock: Clock = systemClock) {
    super(db, clock);
    this.myClock = clock;
  }

  /**
   * Lists active memories sorted by sort_order ASC, then created_at DESC
   * (lower sort_order first; within the same order, newer first).
   */
  async listMemories(): Promise<Memory[]> {
    const rows = await this.db.query<Row>(
      `SELECT ${MEMORY_COLUMNS.join(', ')} FROM memories
       WHERE deleted_at IS NULL
       ORDER BY sort_order ASC, created_at DESC`,
    );
    return rows.map((row) => memorySerializer.fromRow(row));
  }

  /** Returns media asset IDs associated with a memory, in order. */
  async getMemoryMediaIds(memoryId: string): Promise<string[]> {
    const rows = await this.db.query<{ media_asset_id: string }>(
      `SELECT media_asset_id FROM memory_media
       WHERE memory_id = ?
       ORDER BY sort_order ASC, created_at ASC`,
      [memoryId],
    );
    return rows.map((r) => r.media_asset_id);
  }

  /** Returns all memory-media join records for a memory. */
  async getMemoryMedia(memoryId: string): Promise<MemoryMedia[]> {
    const rows = await this.db.query<Row>(
      `SELECT ${MEMORY_MEDIA_COLUMNS.join(', ')} FROM memory_media
       WHERE memory_id = ?
       ORDER BY sort_order ASC, created_at ASC`,
      [memoryId],
    );
    return rows.map((row) => memoryMediaSerializer.fromRow(row));
  }

  /** Associates a media asset with a memory. */
  async addMediaToMemory(memoryId: string, mediaAssetId: string): Promise<MemoryMedia> {
    // Check for existing association
    const existing = await this.db.query<{ id: string }>(
      'SELECT id FROM memory_media WHERE memory_id = ? AND media_asset_id = ?',
      [memoryId, mediaAssetId],
    );
    if (existing.length > 0) {
      // Already associated — return existing
      const rows = await this.db.query<Row>(
        `SELECT ${MEMORY_MEDIA_COLUMNS.join(', ')} FROM memory_media WHERE id = ?`,
        [existing[0].id],
      );
      return memoryMediaSerializer.fromRow(rows[0]);
    }

    // Get next sort order
    const maxOrder = await this.db.query<{ max_order: number | null }>(
      'SELECT MAX(sort_order) AS max_order FROM memory_media WHERE memory_id = ?',
      [memoryId],
    );
    const nextOrder = (maxOrder[0]?.max_order ?? -1) + 1;

    const timestamp = nowIso(this.myClock);
    const entity: MemoryMedia = {
      id: newId(),
      memoryId,
      mediaAssetId,
      sortOrder: nextOrder,
      createdAt: timestamp,
    };

    await this.db.run(
      `INSERT INTO memory_media (${MEMORY_MEDIA_COLUMNS.join(', ')})
       VALUES (${MEMORY_MEDIA_COLUMNS.map(() => '?').join(', ')})`,
      memoryMediaSerializer.toParams(entity),
    );
    return entity;
  }

  /** Removes a media association from a memory. */
  async removeMediaFromMemory(memoryId: string, mediaAssetId: string): Promise<boolean> {
    const result = await this.db.run(
      'DELETE FROM memory_media WHERE memory_id = ? AND media_asset_id = ?',
      [memoryId, mediaAssetId],
    );
    return result.changes > 0;
  }

  /** Removes all media associations for a memory. */
  async clearMemoryMedia(memoryId: string): Promise<void> {
    await this.db.run('DELETE FROM memory_media WHERE memory_id = ?', [memoryId]);
  }

  /**
   * Search memories by title and caption (case-insensitive LIKE).
   */
  async search(query: string): Promise<Memory[]> {
    const pattern = `%${query}%`;
    const rows = await this.db.query<Row>(
      `SELECT ${MEMORY_COLUMNS.join(', ')} FROM memories
       WHERE deleted_at IS NULL
         AND (title LIKE ? OR caption LIKE ?)
       ORDER BY updated_at DESC, title ASC`,
      [pattern, pattern],
    );
    return rows.map((row) => memorySerializer.fromRow(row));
  }

  /** Counts active memories. */
  async count(): Promise<number> {
    const rows = await this.db.query<{ n: number }>(
      'SELECT COUNT(1) AS n FROM memories WHERE deleted_at IS NULL',
    );
    return rows[0]?.n ?? 0;
  }
}
