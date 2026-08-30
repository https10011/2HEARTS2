/**
 * Memory domain model + serializer (Phase 7).
 *
 * Memories are the core content entity: a titled moment with optional
 * caption, date, and associated media. Follows Phase 2 entity conventions:
 * UUID v4 id, UTC ISO 8601 timestamps, tombstone for soft delete.
 *
 * Media references are stored in the `memory_media` join table, NOT inline
 * in the memory row. This keeps the memory entity clean and allows the
 * media lifecycle to be managed independently.
 */

import { assertEntityConventions, type TombstonedEntity } from '../model/entity.ts';
import {
  optionalString,
  requireString,
  type EntitySerializer,
  type Params,
  type Row,
} from '../serialization/entitySerializer.ts';
import { isValidDateKey } from '../../utils/time.ts';

// ---------------------------------------------------------------------------
// Memory
// ---------------------------------------------------------------------------

export interface Memory extends TombstonedEntity {
  title: string;
  /** User-provided caption/details. Nullable. */
  caption: string | null;
  /** Local calendar day `yyyy-mm-dd` or null when unset. */
  memoryDate: string | null;
  /** Sort order for list presentation (lower = earlier). */
  sortOrder: number;
}

export function assertMemory(memory: Memory): void {
  assertEntityConventions(memory);
  if (memory.title.trim().length === 0) {
    throw new Error('memory.title must not be empty');
  }
  if (memory.memoryDate !== null && !isValidDateKey(memory.memoryDate)) {
    throw new Error('memory.memoryDate must be a yyyy-mm-dd calendar date');
  }
}

export const MEMORY_COLUMNS = [
  'id',
  'title',
  'caption',
  'memory_date',
  'sort_order',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const memorySerializer: EntitySerializer<Memory> = {
  columns: MEMORY_COLUMNS,
  toParams(memory: Memory): Params {
    return [
      memory.id,
      memory.title,
      memory.caption,
      memory.memoryDate,
      memory.sortOrder,
      memory.createdAt,
      memory.updatedAt,
      memory.deletedAt,
    ];
  },
  fromRow(row: Row): Memory {
    return {
      id: requireString(row, 'id'),
      title: requireString(row, 'title'),
      caption: optionalString(row, 'caption'),
      memoryDate: optionalString(row, 'memory_date'),
      sortOrder: typeof row['sort_order'] === 'number' ? row['sort_order'] as number : 0,
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};

// ---------------------------------------------------------------------------
// MemoryMedia (join table)
// ---------------------------------------------------------------------------

export interface MemoryMedia {
  id: string;
  memoryId: string;
  mediaAssetId: string;
  sortOrder: number;
  createdAt: string;
}

export const MEMORY_MEDIA_COLUMNS = [
  'id',
  'memory_id',
  'media_asset_id',
  'sort_order',
  'created_at',
] as const;

export const memoryMediaSerializer: EntitySerializer<MemoryMedia> = {
  columns: MEMORY_MEDIA_COLUMNS,
  toParams(entity: MemoryMedia): Params {
    return [
      entity.id,
      entity.memoryId,
      entity.mediaAssetId,
      entity.sortOrder,
      entity.createdAt,
    ];
  },
  fromRow(row: Row): MemoryMedia {
    return {
      id: requireString(row, 'id'),
      memoryId: requireString(row, 'memory_id'),
      mediaAssetId: requireString(row, 'media_asset_id'),
      sortOrder: typeof row['sort_order'] === 'number' ? row['sort_order'] as number : 0,
      createdAt: requireString(row, 'created_at'),
    };
  },
};
