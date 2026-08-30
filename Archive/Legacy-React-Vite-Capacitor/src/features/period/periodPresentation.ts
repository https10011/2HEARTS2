/**
 * Period presentation helpers (Stage 11).
 *
 * Pure, framework-free logic for the productized Period experience:
 * warm relative date labels, calendar-readiness helpers, history month
 * grouping, and a benign cycle-status description. Everything is derived
 * from real persisted entries/settings — nothing fabricated, nothing
 * that expands the health domain model.
 *
 * Node-test reachable: relative imports with .ts extensions only.
 */

import type { PeriodEntry, FlowLevel } from '../../data/period/periodTypes.ts';
import { diffDays } from '../../data/period/periodTypes.ts';
import { FLOW_META, FLOW_ORDER } from './flowMeta.ts';

// ---------------------------------------------------------------------------
// Profile conventions (repo-wide placeholder id — see vault/period features)
// ---------------------------------------------------------------------------

export const OWNER_PROFILE_ID = 'owner';

// ---------------------------------------------------------------------------
// Date helpers (startDate / endDate are LOCAL `yyyy-mm-dd` calendar keys)
// ---------------------------------------------------------------------------

/** Local calendar key for a Date. */
export function localDateKey(date: Date = new Date()): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function keyToUtcDate(key: string): Date {
  const [y, m, d] = key.split('-').map(Number);
  return new Date(Date.UTC(y ?? 1970, (m ?? 1) - 1, d ?? 1));
}

/** Whole-day difference between two `yyyy-mm-dd` keys (a - b). */
export function dayDiff(a: string, b: string): number {
  return Math.round(
    (keyToUtcDate(a).getTime() - keyToUtcDate(b).getTime()) / 86_400_000,
  );
}

/** Shifts a `yyyy-mm-dd` key by whole days. */
export function shiftDateKey(key: string, days: number): string {
  const d = keyToUtcDate(key);
  d.setUTCDate(d.getUTCDate() + days);
  const y = d.getUTCFullYear();
  const m = String(d.getUTCMonth() + 1).padStart(2, '0');
  const day = String(d.getUTCDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/** True when the key falls within the current month grid (same month/year). */
export function isInMonth(key: string, year: number, month: number): boolean {
  return key.slice(0, 4) === String(year) && key.slice(5, 7) === String(month + 1).padStart(2, '0');
}

/** "Today" / "Yesterday" / "Mon, Aug 25" (year only when not current). */
export function formatPeriodDay(entryDate: string, today: string): string {
  const diff = dayDiff(today, entryDate);
  if (diff === 0) return 'Today';
  if (diff === 1) return 'Yesterday';
  const d = keyToUtcDate(entryDate);
  const sameYear = entryDate.slice(0, 4) === today.slice(0, 4);
  return d.toLocaleDateString(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    timeZone: 'UTC',
    ...(sameYear ? {} : { year: 'numeric' }),
  });
}

/** "August 2026" month label for a `yyyy-mm-dd` key. */
export function formatPeriodMonth(entryDate: string): string {
  return keyToUtcDate(entryDate).toLocaleDateString(undefined, {
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  });
}

/** "August 27 · 4 days" style chip for the Log Period composer. */
export function formatLogDate(startDate: string, today: string): string {
  const label = keyToUtcDate(startDate).toLocaleDateString(undefined, {
    month: 'long',
    day: 'numeric',
    timeZone: 'UTC',
  });
  return startDate === today ? `Today · ${label}` : label;
}

// ---------------------------------------------------------------------------
// Flow metadata
// ---------------------------------------------------------------------------

export function flowLabel(level: FlowLevel): string {
  const label = FLOW_META[level]?.label;
  return label ?? level;
}

/** Ordered flow options for selectors (value + label). */
export function flowOptions(): Array<{ value: FlowLevel; label: string }> {
  return FLOW_ORDER.map((value) => ({ value, label: FLOW_META[value].label }));
}

// ---------------------------------------------------------------------------
// History grouping
// ---------------------------------------------------------------------------

export interface PeriodMonthGroup {
  monthLabel: string;
  entries: PeriodEntry[];
}

/** Groups entries (assumed date-desc) into month sections, order preserved. */
export function buildPeriodMonths(entries: PeriodEntry[]): PeriodMonthGroup[] {
  const groups: PeriodMonthGroup[] = [];
  let currentKey = '';
  for (const entry of entries) {
    const key = entry.startDate.slice(0, 7);
    if (key !== currentKey) {
      currentKey = key;
      groups.push({ monthLabel: formatPeriodMonth(entry.startDate), entries: [] });
    }
    groups[groups.length - 1]!.entries.push(entry);
  }
  return groups;
}

// ---------------------------------------------------------------------------
// Entry presentation (dates, durations — derived from existing fields only)
// ---------------------------------------------------------------------------

/** Whole-day duration of an entry (end - start + 1), or null when ongoing. */
export function entryDurationDays(entry: PeriodEntry): number | null {
  if (!entry.endDate) return null;
  const dur = diffDays(entry.startDate, entry.endDate) + 1;
  return dur > 0 ? dur : 1;
}

/** Distance from this entry to the previous start (cycle length), if sane. */
export function cycleLengthToPrevious(entry: PeriodEntry, previousStart: string | null): number | null {
  if (!previousStart) return null;
  const diff = diffDays(previousStart, entry.startDate);
  return diff > 0 ? diff : null;
}

// ---------------------------------------------------------------------------
// Calendar cell status
// ---------------------------------------------------------------------------

export type PeriodCellFill = 'none' | 'period' | 'predicted';

/** Combine the calendar markers for one day and the resulting filled dot. */
export function cellStatus(
  dayKey: string,
  today: string,
  periodDays: Set<string>,
  predictedDays: Set<string>,
): { fill: PeriodCellFill; isToday: boolean } {
  const isToday = dayKey === today;
  const isPeriod = periodDays.has(dayKey);
  const isPredicted = !isPeriod && predictedDays.has(dayKey);
  return {
    fill: isPeriod ? 'period' : isPredicted ? 'predicted' : 'none',
    isToday,
  };
}