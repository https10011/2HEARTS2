/**
 * Period Tracker domain models + serializers (Phase 16).
 *
 * Local-only period tracking for the owner. All data stays on-device.
 * Follows Phase 2 Entity conventions (UUID v4, UTC ISO timestamps, tombstone).
 *
 * Two tables:
 * - `period_entries`  — individual period logs (start, end, flow, notes).
 * - `period_settings` — user preferences (cycle length, period length).
 *
 * Conventions:
 * - `startDate / endDate` — LOCAL calendar days `yyyy-mm-dd`.
 * - `createdAt / updatedAt` — UTC ISO 8601 (Entity standard).
 * - `deletedAt` — tombstone for soft deletes.
 * - Settings row: singleton (one row per owner profile).
 */

import { assertEntityConventions, type TombstonedEntity } from '../model/entity.ts';
import {
  optionalNumber,
  optionalString,
  requireEnum,
  requireString,
  type EntitySerializer,
  type Params,
  type Row,
} from '../serialization/entitySerializer.ts';
import { isValidDateKey } from '../../utils/time.ts';

// ---------------------------------------------------------------------------
// Flow levels
// ---------------------------------------------------------------------------

export const FLOW_LEVELS = ['light', 'medium', 'heavy'] as const;
export type FlowLevel = (typeof FLOW_LEVELS)[number];

// ---------------------------------------------------------------------------
// Period entry
// ---------------------------------------------------------------------------

export interface PeriodEntry extends TombstonedEntity {
  /** Local calendar day the period started `yyyy-mm-dd`. */
  startDate: string;
  /** Local calendar day the period ended `yyyy-mm-dd`. Null if ongoing. */
  endDate: string | null;
  /** Flow level. */
  flowLevel: FlowLevel;
  /** Optional personal note. */
  note: string | null;
  /** Profile id of the owner tracking this. */
  profileId: string;
}

export type NewPeriodEntry = Omit<PeriodEntry, 'id' | 'createdAt' | 'updatedAt' | 'deletedAt'>;

export function assertPeriodEntry(entry: PeriodEntry): void {
  assertEntityConventions(entry);
  if (!isValidDateKey(entry.startDate)) {
    throw new Error('periodEntry.startDate must be a yyyy-mm-dd calendar date');
  }
  if (entry.endDate && !isValidDateKey(entry.endDate)) {
    throw new Error('periodEntry.endDate must be a yyyy-mm-dd calendar date');
  }
  if (!FLOW_LEVELS.includes(entry.flowLevel)) {
    throw new Error(`flowLevel must be one of: ${FLOW_LEVELS.join(', ')}`);
  }
  if (!entry.profileId) {
    throw new Error('periodEntry.profileId is required');
  }
}

// ---------------------------------------------------------------------------
// Period settings (singleton per profile)
// ---------------------------------------------------------------------------

export interface PeriodSettings {
  id: string;
  profileId: string;
  /** Typical cycle length in days (default 28). */
  cycleLengthDays: number;
  /** Typical period duration in days (default 5). */
  periodLengthDays: number;
  createdAt: string;
  updatedAt: string;
}

export const DEFAULT_CYCLE_LENGTH = 28;
export const DEFAULT_PERIOD_LENGTH = 5;

// ---------------------------------------------------------------------------
// Serializers
// ---------------------------------------------------------------------------

export const PERIOD_ENTRY_COLUMNS = [
  'id',
  'start_date',
  'end_date',
  'flow_level',
  'note',
  'profile_id',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const periodEntrySerializer: EntitySerializer<PeriodEntry> = {
  columns: PERIOD_ENTRY_COLUMNS,
  toParams(entry: PeriodEntry): Params {
    return [
      entry.id,
      entry.startDate,
      entry.endDate,
      entry.flowLevel,
      entry.note,
      entry.profileId,
      entry.createdAt,
      entry.updatedAt,
      entry.deletedAt,
    ];
  },
  fromRow(row: Row): PeriodEntry {
    return {
      id: requireString(row, 'id'),
      startDate: requireString(row, 'start_date'),
      endDate: optionalString(row, 'end_date'),
      flowLevel: requireEnum(row, 'flow_level', FLOW_LEVELS),
      note: optionalString(row, 'note'),
      profileId: requireString(row, 'profile_id'),
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};

export const PERIOD_SETTINGS_COLUMNS = [
  'id',
  'profile_id',
  'cycle_length_days',
  'period_length_days',
  'created_at',
  'updated_at',
] as const;

export const periodSettingsSerializer: EntitySerializer<PeriodSettings> = {
  columns: PERIOD_SETTINGS_COLUMNS,
  toParams(s: PeriodSettings): Params {
    return [
      s.id,
      s.profileId,
      s.cycleLengthDays,
      s.periodLengthDays,
      s.createdAt,
      s.updatedAt,
    ];
  },
  fromRow(row: Row): PeriodSettings {
    return {
      id: requireString(row, 'id'),
      profileId: requireString(row, 'profile_id'),
      cycleLengthDays: optionalNumber(row, 'cycle_length_days') ?? DEFAULT_CYCLE_LENGTH,
      periodLengthDays: optionalNumber(row, 'period_length_days') ?? DEFAULT_PERIOD_LENGTH,
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
    };
  },
};

// ---------------------------------------------------------------------------
// Date calculation helpers
// ---------------------------------------------------------------------------

/** Add days to a yyyy-mm-dd date string. */
export function addDays(dateStr: string, days: number): string {
  const d = new Date(dateStr + 'T00:00:00');
  d.setDate(d.getDate() + days);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/** Difference in days between two yyyy-mm-dd dates (b - a). */
export function diffDays(a: string, b: string): number {
  const da = new Date(a + 'T00:00:00');
  const db = new Date(b + 'T00:00:00');
  return Math.round((db.getTime() - da.getTime()) / (1000 * 60 * 60 * 24));
}
