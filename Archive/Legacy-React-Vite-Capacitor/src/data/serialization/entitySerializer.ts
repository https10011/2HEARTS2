/**
 * Serialization conventions (Phase 2).
 *
 * Conversion between domain objects and database rows happens ONLY here —
 * no stray JSON.stringify / ad-hoc casts anywhere in feature code.
 *
 * Canonical rules (same in every layer, deterministic for future V2 sync):
 * - id            ↔ TEXT (UUID v4 string)
 * - timestamps    ↔ TEXT (UTC ISO 8601)
 * - boolean       ↔ INTEGER 0/1
 * - null          ↔ NULL
 * - enum/union    ↔ TEXT, validated against the union on deserialization
 * - array/object  ↔ TEXT holding stable JSON (keys sorted)
 * - binary        ↔ NEVER in tables (filesystem + safe reference records)
 *
 * Each entity defines one `EntitySerializer` next to its model. Serializers
 * throw `PersistenceError(serialization-failed)` on unexpected shapes —
 * corrupt rows must not silently become bogus domain objects.
 */

import type { SqlValue } from '../database/adapter.ts';
import { PersistenceError } from '../database/errors.ts';

export type Row = Record<string, SqlValue | undefined>;
export type Params = SqlValue[];

/** Bidirectional, explicit converter for one entity type. */
export interface EntitySerializer<T> {
  /** snake_case column list, in the exact order returned by `toParams`. */
  readonly columns: readonly string[];
  /** Domain object → ordered INSERT/UPDATE parameters (identity excluded). */
  toParams(entity: T): Params;
  /** Database row (from `SELECT *` shaped objects) → domain object. */
  fromRow(row: Row): T;
}

export function stableStringify(value: unknown): string {
  return JSON.stringify(sortDeep(value));
}

function sortDeep(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(sortDeep);
  if (value && typeof value === 'object') {
    return Object.keys(value as Record<string, unknown>)
      .sort()
      .reduce<Record<string, unknown>>((acc, key) => {
        acc[key] = sortDeep((value as Record<string, unknown>)[key]);
        return acc;
      }, {});
  }
  return value;
}

export function requireString(row: Row, column: string): string {
  const value = row[column];
  if (typeof value !== 'string' || value.length === 0) {
    throw new PersistenceError('serialization-failed', `Column ${column} must be a non-empty string.`);
  }
  return value;
}

export function optionalString(row: Row, column: string): string | null {
  const value = row[column];
  if (value === null || value === undefined) return null;
  if (typeof value !== 'string') {
    throw new PersistenceError('serialization-failed', `Column ${column} must be a string or NULL.`);
  }
  return value;
}

export function optionalNumber(row: Row, column: string): number | null {
  const value = row[column];
  if (value === null || value === undefined) return null;
  if (typeof value !== 'number' || !Number.isFinite(value)) {
    throw new PersistenceError('serialization-failed', `Column ${column} must be a number or NULL.`);
  }
  return value;
}

export function requireEnum<T extends string>(row: Row, column: string, allowed: readonly T[]): T {
  const value = requireString(row, column);
  if (!allowed.includes(value as T)) {
    throw new PersistenceError('serialization-failed', `Column ${column} has an unexpected value.`);
  }
  return value as T;
}
