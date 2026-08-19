/**
 * Timestamp conventions (Phase 2).
 *
 * Every domain timestamp is an ISO 8601 string in UTC, produced by
 * `Date.prototype.toISOString()` (e.g. "2026-08-19T08:54:52.224Z").
 *
 * One canonical string format is used across every layer (domain object,
 * repository, database row, export payload) so future V2 synchronization can
 * compare and order timestamps without parsing ambiguity. Timezone offsets
 * are never stored; local rendering is a UI concern only.
 */

const ISO_PATTERN = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z$/;

/** Injectable clock (defaults to system time) for deterministic tests. */
export type Clock = () => Date;

export const systemClock: Clock = () => new Date();

export function nowIso(clock: Clock = systemClock): string {
  return clock().toISOString();
}

/** Returns true when value is a UTC ISO 8601 timestamp string. */
export function isValidIsoTimestamp(value: string): boolean {
  if (!ISO_PATTERN.test(value)) return false;
  return Number.isFinite(Date.parse(value));
}

/** Lexicographic comparison is chronological for this fixed UTC format. */
export function compareIso(a: string, b: string): number {
  return a < b ? -1 : a > b ? 1 : 0;
}

/**
 * Local calendar-date convention (Phase 4).
 *
 * Relationship dates (start date, birthdays, important dates) are LOCAL
 * calendar days with no time component — a couples app celebrates "the
 * day", not an instant. They persist as `yyyy-mm-dd` keys (matching
 * `toLocalDateKey` in services/datetime). Timestamps (createdAt etc.)
 * remain UTC ISO 8601 as defined above; the two conventions never mix.
 */

const DATE_KEY_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

/** Returns true when value is a `yyyy-mm-dd` key for a REAL calendar date. */
export function isValidDateKey(value: string): boolean {
  if (!DATE_KEY_PATTERN.test(value)) return false;
  const [year, month, day] = value.split('-').map(Number);
  const date = new Date(Date.UTC(year, month - 1, day));
  return (
    date.getUTCFullYear() === year &&
    date.getUTCMonth() === month - 1 &&
    date.getUTCDate() === day
  );
}
