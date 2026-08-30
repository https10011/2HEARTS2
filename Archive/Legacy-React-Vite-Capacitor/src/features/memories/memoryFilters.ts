/**
 * Memories presentation helpers (Stage 5).
 *
 * Pure, framework-free utilities for the Memories screens so the
 * filtering/formatting logic is unit-testable with node:test (no mocks).
 * All helpers accept plain data and return new values — they never mutate.
 */

export interface MemoryLike {
  /** LOCAL calendar key `yyyy-mm-dd`, or null when the user skipped it. */
  memoryDate: string | null;
}

/**
 * Sorts memories newest-first by `createdAt` (UTC ISO strings compare
 * lexicographically). Returns a new array; the repository's own ordering
 * (sortOrder = creation order) is left untouched.
 */
export function byNewestFirst<T extends { createdAt: string }>(memories: T[]): T[] {
  return [...memories].sort((a, b) => (a.createdAt < b.createdAt ? 1 : a.createdAt > b.createdAt ? -1 : 0));
}

/**
 * Collects the distinct years present across memories, newest first.
 * Memories with a missing/invalid date contribute no year.
 */
export function collectYears(memories: MemoryLike[]): number[] {
  const years = new Set<number>();
  for (const m of memories) {
    const year = extractYear(m.memoryDate);
    if (year !== null) years.add(year);
  }
  return [...years].sort((a, b) => b - a);
}

/**
 * Filters memories to a given year. `null` year means "all".
 * Memories without a valid date only survive the unfiltered view.
 */
export function filterByYear<T extends MemoryLike>(
  memories: T[],
  year: number | null,
): T[] {
  if (year === null) return memories;
  return memories.filter((m) => extractYear(m.memoryDate) === year);
}

/** Extracts the year from a `yyyy-mm-dd` date key; null when invalid. */
export function extractYear(dateKey: string | null): number | null {
  if (!dateKey) return null;
  const match = /^(\d{4})-\d{2}-\d{2}$/.exec(dateKey);
  if (!match) return null;
  const year = Number(match[1]);
  return Number.isFinite(year) ? year : null;
}

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

/**
 * Formats a LOCAL date key for display, e.g. "May 12, 2024".
 * Returns an empty string for missing/invalid values so callers can hide
 * the metadata rather than render "Invalid Date".
 */
export function formatDateKey(dateKey: string | null): string {
  const match = dateKey ? /^(\d{4})-(\d{2})-(\d{2})$/.exec(dateKey) : null;
  if (!match) return '';
  const month = Number(match[2]);
  if (month < 1 || month > 12) return '';
  return `${MONTH_NAMES[month - 1]} ${Number(match[3])}, ${match[1]}`;
}
