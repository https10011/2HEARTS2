/**
 * Domain model conventions (Phase 2).
 *
 * Every domain entity extends `Entity`. These conventions exist so that a
 * future V2 synchronization layer can be added without reworking the local
 * schema:
 *
 * - `id`        — locally generated UUID v4 (see src/utils/ids), never the
 *                 database row number.
 * - `createdAt` — UTC ISO 8601 string (see src/utils/time), set once.
 * - `updatedAt` — UTC ISO 8601 string, refreshed on every update.
 * - `deletedAt` — optional tombstone (`null` while active). Entities that a
 *                 future sync layer must propagate deletes for use
 *                 `TombstonedEntity`; hard deletes stay possible when the
 *                 domain requires it.
 *
 * Field conventions: snake_case columns in SQL, camelCase in the domain,
 * converted exclusively through a per-entity serializer. Nullable means
 * `T | null`; optional persistence fields are still typed explicitly so V2
 * sync payloads are deterministic.
 */

import { isValidId } from '../../utils/ids.ts';
import { isValidIsoTimestamp } from '../../utils/time.ts';

export interface Entity {
  id: string;
  createdAt: string;
  updatedAt: string;
}

export interface TombstonedEntity extends Entity {
  deletedAt: string | null;
}

/**
 * Shape of a new entity before persistence assigns identity and entity
 * timestamps. Tombstone (`deletedAt`) remains editable — soft deletes set it.
 */
export type NewEntity<T extends Entity> = Omit<T, 'id' | 'createdAt' | 'updatedAt'>;

/** Validates the Entity conventions. Throws on the first violation. */
export function assertEntityConventions(entity: Entity): void {
  if (!isValidId(entity.id)) {
    throw new Error('entity.id must be a UUID v4');
  }
  if (!isValidIsoTimestamp(entity.createdAt)) {
    throw new Error('entity.createdAt must be a UTC ISO 8601 timestamp');
  }
  if (!isValidIsoTimestamp(entity.updatedAt)) {
    throw new Error('entity.updatedAt must be a UTC ISO 8601 timestamp');
  }
  const tombstoned = entity as TombstonedEntity;
  if ('deletedAt' in entity && tombstoned.deletedAt !== null && !isValidIsoTimestamp(tombstoned.deletedAt)) {
    throw new Error('entity.deletedAt must be null or a UTC ISO 8601 timestamp');
  }
}
