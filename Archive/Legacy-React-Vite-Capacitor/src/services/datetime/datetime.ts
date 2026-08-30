/**
 * Centralized date/time utilities (Phase 3).
 *
 * Conventions (Phase 2): persistence stores ISO 8601 UTC strings; UI
 * libraries should pass `Date` objects or ISO strings through THESE helpers
 * rather than reconstructing/parsing ad hoc. All functions are pure and
 * take an explicit `now` where "today" matters (deterministic tests).
 *
 * Handles: leap years, month lengths, midnight boundaries, LOCAL timezone
 * (anniversaries/counters are calendar-local by definition), DST via
 * calendar (not fixed-offset) arithmetic, and invalid dates (return null
 * instead of throwing; validators in services/validation report them).
 */

const FORMATTER_DATE = new Intl.DateTimeFormat(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
const FORMATTER_TIME = new Intl.DateTimeFormat(undefined, { hour: '2-digit', minute: '2-digit' });

/** Strict parse of an ISO 8601 date; null when invalid (never NaN-Date). */
export function parseDate(value: string, strictDateOnly = false): Date | null {
  if (strictDateOnly && !/^\d{4}-\d{2}-\d{2}$/.test(value)) return null;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;
  if (strictDateOnly && date.toISOString().slice(0, 10) !== value) return null;
  return date;
}

export function isValidDateValue(value: unknown): value is Date {
  return value instanceof Date && !Number.isNaN(value.getTime());
}

export function formatDate(date: Date | null): string {
  return isValidDateValue(date) ? FORMATTER_DATE.format(date) : '';
}

export function formatTime(date: Date | null): string {
  return isValidDateValue(date) ? FORMATTER_TIME.format(date) : '';
}

/** yyyy-mm-dd in the LOCAL calendar (not UTC — a calendar day is local). */
export function toLocalDateKey(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

/** Local midnight for the same calendar day (DST-aware by construction). */
export function startOfLocalDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

/** Whole calendar days between two dates, local calendar basis. */
export function diffLocalDays(from: Date, to: Date): number {
  const a = startOfLocalDay(from).getTime();
  const b = startOfLocalDay(to).getTime();
  const probe = b - a;
  // Days are 23–25h under DST; round via average step for robustness.
  return Math.round(probe / MS_PER_DAY);
}

export const MS_PER_DAY = 86_400_000;

export function diffInMilliseconds(from: Date, to: Date): number {
  return to.getTime() - from.getTime();
}

export function compareDateDesc(a: Date, b: Date): number {
  return b.getTime() - a.getTime();
}

/**
 * Adds whole years while preserving the calendar day when possible.
 * Feb 29 → Feb 28 in non-leap years (documented convention; alternatives
 * are feature choices, not utility bugs).
 */
export function addYearsPreservingDay(date: Date, years: number): Date {
  const next = new Date(date.getTime());
  next.setFullYear(date.getFullYear() + years);
  // Feb 29 + n years spills into Mar 1 on non-leap targets — clamp back.
  if (next.getMonth() !== date.getMonth()) next.setDate(0);
  return next;
}

/** Adds whole months; day-of-month clamps to the target month length. */
export function addMonthsPreservingDay(date: Date, months: number): Date {
  const target = new Date(date.getFullYear(), date.getMonth() + months, date.getDate());
  // If the day spilled (e.g. Jan 31 + 1 month → Mar 3), clamp back.
  if (target.getDate() !== date.getDate()) {
    return new Date(date.getFullYear(), date.getMonth() + months + 1, 0);
  }
  return target;
}

/** Anniversary of `start` that occurs in `year` (Feb 29 → Feb 28 rule). */
export function anniversaryInYear(start: Date, year: number): Date {
  const candidate = addYearsPreservingDay(start, year - start.getFullYear());
  return candidate;
}

/**
 * Relationship counter convention: FROM the relationship start date TO
 * `now`, in full local calendar days (day 0 on the start date; day 1 the
 * next day). Negative when `now` precedes start.
 */
export function relationshipAgeDays(start: Date, now: Date): number {
  return diffLocalDays(start, now);
}

/** Years/months/days decomposition of an anniversary-style age. */
export function decomposeCalendarAge(start: Date, now: Date): { years: number; months: number; days: number } | null {
  if (!isValidDateValue(start) || !isValidDateValue(now)) return null;
  if (now.getTime() < start.getTime()) return null;
  let years = now.getFullYear() - start.getFullYear();
  let months = now.getMonth() - start.getMonth();
  let days = now.getDate() - start.getDate();
  if (days < 0) {
    months -= 1;
    const previousMonth = new Date(now.getFullYear(), now.getMonth(), 0);
    days += previousMonth.getDate();
  }
  if (months < 0) {
    years -= 1;
    months += 12;
  }
  return { years, months, days };
}

/**
 * Duration in whole human units: converts a millisecond span into hours/
 * minutes (no days-field drift because spans are clock-based, not calendar).
 */
export function durationHuman(milliseconds: number): { hours: number; minutes: number; seconds: number } {
  const totalSeconds = Math.floor(Math.abs(milliseconds) / 1000);
  return {
    hours: Math.floor(totalSeconds / 3600),
    minutes: Math.floor((totalSeconds % 3600) / 60),
    seconds: totalSeconds % 60,
  };
}

/** Days until the next occurrence of a monthly-dated anniversary (local). */
export function daysUntilAnniversary(start: Date, now: Date): number {
  if (!isValidDateValue(start) || !isValidDateValue(now)) return Number.NaN;
  const today = startOfLocalDay(now);
  let next = anniversaryInYear(start, now.getFullYear());
  if (startOfLocalDay(next).getTime() < today.getTime()) {
    next = anniversaryInYear(start, now.getFullYear() + 1);
  }
  return diffLocalDays(today, next);
}
