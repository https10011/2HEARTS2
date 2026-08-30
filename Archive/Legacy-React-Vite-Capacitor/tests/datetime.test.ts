import { test } from 'node:test';
import assert from 'node:assert';
import {
  addMonthsPreservingDay,
  addYearsPreservingDay,
  anniversaryInYear,
  daysUntilAnniversary,
  decomposeCalendarAge,
  diffLocalDays,
  durationHuman,
  isValidDateValue,
  parseDate,
  relationshipAgeDays,
  startOfLocalDay,
  toLocalDateKey,
} from '../src/services/datetime/datetime.ts';

test('parseDate accepts ISO and rejects invalid dates (no NaN leakage)', () => {
  assert.ok(isValidDateValue(parseDate('2024-02-29') as Date)); // leap day
  assert.strictEqual(parseDate('2023-02-29', true), null); // invalid calendar date
  assert.strictEqual(parseDate('not-a-date'), null);
  assert.strictEqual(parseDate('2024-13-01', true), null); // month 13
});

test('anniversaryInYear maps Feb 29 to Feb 28 in non-leap years', () => {
  const start = parseDate('2020-02-29T12:00:00Z') as Date;
  assert.strictEqual(anniversaryInYear(start, 2021).getMonth(), 1);
  assert.strictEqual(anniversaryInYear(start, 2021).getDate(), 28);
  assert.strictEqual(anniversaryInYear(start, 2024).getDate(), 29); // leap again
});

test('addYearsPreservingDay handles leap boundaries', () => {
  const start = parseDate('2020-02-29T00:00:00Z') as Date;
  const one = addYearsPreservingDay(start, 1);
  assert.strictEqual(one.getFullYear(), 2021);
  assert.strictEqual(one.getMonth(), 1);
  assert.strictEqual(one.getDate(), 28);
});

test('addMonthsPreservingDay clamps day overflow', () => {
  const jan31 = new Date(2024, 0, 31);
  const result = addMonthsPreservingDay(jan31, 1);
  assert.strictEqual(result.getMonth(), 1); // February
  assert.strictEqual(result.getDate(), 29); // 2024 is a leap year
  const result2023 = addMonthsPreservingDay(new Date(2023, 0, 31), 1);
  assert.strictEqual(result2023.getMonth(), 1);
  assert.strictEqual(result2023.getDate(), 28);
});

test('diffLocalDays is midnight-safe and DST-tolerant', () => {
  const a = new Date(2024, 2, 10, 0, 0, 0); // local midnight
  const b = new Date(2024, 2, 12, 23, 59, 59);
  assert.strictEqual(diffLocalDays(a, b), 2);
  assert.strictEqual(diffLocalDays(b, a), -2);
  assert.strictEqual(diffLocalDays(a, a), 0);
});

test('relationshipAgeDays counts full local calendar days from start', () => {
  const start = new Date(2024, 0, 1);
  const now = new Date(2024, 0, 1, 23, 59);
  assert.strictEqual(relationshipAgeDays(start, now), 0);
  assert.strictEqual(relationshipAgeDays(start, new Date(2024, 0, 2)), 1);
});

test('decomposeCalendarAge returns years/months/days', () => {
  const start = new Date(2020, 5, 15);
  const now = new Date(2024, 7, 19);
  assert.deepStrictEqual(decomposeCalendarAge(start, now), { years: 4, months: 2, days: 4 });
  assert.strictEqual(decomposeCalendarAge(now, start), null); // reversed → null
});

test('daysUntilAnniversary wraps into next year', () => {
  const start = new Date(2020, 11, 25); // Dec 25
  const now = new Date(2024, 11, 24);
  assert.strictEqual(daysUntilAnniversary(start, now), 1);
  const after = new Date(2024, 11, 26);
  assert.strictEqual(daysUntilAnniversary(start, after), 364); // 2025 not leap for Feb 29 path
});

test('startOfLocalDay and toLocalDateKey normalize to local calendar', () => {
  const date = new Date(2024, 4, 10, 15, 30, 45);
  const start = startOfLocalDay(date);
  assert.strictEqual(start.getHours(), 0);
  assert.strictEqual(start.getMinutes(), 0);
  assert.strictEqual(toLocalDateKey(date), '2024-05-10');
});

test('durationHuman converts spans to hours/minutes/seconds', () => {
  assert.deepStrictEqual(durationHuman(3_661_000), { hours: 1, minutes: 1, seconds: 1 });
  assert.deepStrictEqual(durationHuman(0), { hours: 0, minutes: 0, seconds: 0 });
});
