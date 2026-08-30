/**
 * Stage 8 — Reminders + Important Dates productization.
 *
 * Locks the Stage 8 contracts on the real source (same pattern as the
 * Stage 4/Stage 7 suites):
 *   - reminderSchedule pure helpers (time/date formatting, relative day
 *     labels, grouping, filtering, recurrence/status copy);
 *   - Reminders screens use the branded controls (centralized DatePicker,
 *     branded TimePicker, centralized Modal) — no native date/time inputs;
 *   - the design system owns the Stage 8 .th-rem-* classes.
 */

import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

import {
  RECURRENCE_LABELS,
  STATUS_LABELS,
  buildReminderGroups,
  daysUntilReminder,
  filterReminderGroups,
  formatReminderDate,
  formatReminderTime,
  historyStatusLabel,
  relativeDayLabel,
  type ReminderGroups,
} from '../src/features/reminders/reminderSchedule.ts';
import type { Reminder } from '../src/data/reminder/reminderTypes.ts';

function read(path: string): string {
  return readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');
}

/* ------------------------------------------------------------------ */
/*  Fixtures                                                          */
/* ------------------------------------------------------------------ */

const NOW = new Date(2026, 7, 16, 9, 30); // Aug 16 2026, 09:30 local

let seq = 0;
function reminder(over: Partial<Reminder>): Reminder {
  seq += 1;
  return {
    id: `r-${seq}`,
    title: over.title ?? `Reminder ${seq}`,
    description: over.description ?? null,
    scheduledDate: over.scheduledDate ?? '2026-08-20',
    scheduledTime: over.scheduledTime ?? '09:00',
    recurrence: over.recurrence ?? 'none',
    status: over.status ?? 'active',
    notificationOwnerRef: over.notificationOwnerRef ?? null,
    notificationEnabled: over.notificationEnabled ?? true,
    createdAt: '2026-08-01T00:00:00.000Z',
    updatedAt: '2026-08-01T00:00:00.000Z',
    deletedAt: null,
    ...over,
  };
}

/* ------------------------------------------------------------------ */
/*  Pure helpers                                                      */
/* ------------------------------------------------------------------ */

test('formatReminderTime renders 12-hour AM/PM times', () => {
  assert.equal(formatReminderTime('09:00'), '9:00 AM');
  assert.equal(formatReminderTime('14:30'), '2:30 PM');
  assert.equal(formatReminderTime('00:15'), '12:15 AM');
  assert.equal(formatReminderTime('12:00'), '12:00 PM');
  assert.equal(formatReminderTime('23:59'), '11:59 PM');
});

test('formatReminderTime passes through malformed times unchanged', () => {
  assert.equal(formatReminderTime('not-a-time'), 'not-a-time');
  assert.equal(formatReminderTime('99:99'), '99:99');
});

test('formatReminderDate renders a long local date (UTC-safe)', () => {
  const formatted = formatReminderDate('2026-01-01');
  assert.match(formatted, /January/);
  assert.match(formatted, /2026/);
  assert.doesNotMatch(formatted, /December/);
});

test('formatReminderDate passes through malformed keys unchanged', () => {
  assert.equal(formatReminderDate('bad'), 'bad');
});

test('daysUntilReminder is timezone-safe at year boundaries', () => {
  const dec31 = new Date(2026, 11, 31, 23, 59);
  assert.equal(daysUntilReminder('2026-12-31', dec31), 0);
  assert.equal(daysUntilReminder('2027-01-01', dec31), 1);
  assert.equal(daysUntilReminder('2026-01-01', dec31), -364);
});

test('daysUntilReminder returns null for malformed keys', () => {
  assert.equal(daysUntilReminder('tomorrow', NOW), null);
});

test('relativeDayLabel resolves Today/Tomorrow/Yesterday', () => {
  assert.equal(relativeDayLabel('2026-08-16', NOW), 'Today');
  assert.equal(relativeDayLabel('2026-08-17', NOW), 'Tomorrow');
  assert.equal(relativeDayLabel('2026-08-15', NOW), 'Yesterday');
});

test('relativeDayLabel falls back to a month/day date without the year for this year', () => {
  const label = relativeDayLabel('2026-09-04', NOW);
  assert.match(label, /September/);
  assert.match(label, /4/);
  assert.doesNotMatch(label, /2026/);
});

test('relativeDayLabel includes the year for other years', () => {
  const label = relativeDayLabel('2027-01-05', NOW);
  assert.match(label, /2027/);
});

test('buildReminderGroups splits today / upcoming / history and picks next up', () => {
  const groups = buildReminderGroups(
    [
      reminder({ title: 'later', scheduledDate: '2026-08-20', scheduledTime: '18:00' }),
      reminder({ title: 'today-late', scheduledDate: '2026-08-16', scheduledTime: '20:00' }),
      reminder({ title: 'today-soon', scheduledDate: '2026-08-16', scheduledTime: '10:00' }),
      reminder({ title: 'done', status: 'completed', scheduledDate: '2026-08-01' }),
    ],
    NOW,
  );
  assert.deepEqual(groups.today.map((r) => r.title), ['today-soon', 'today-late']);
  assert.deepEqual(groups.upcoming.map((r) => r.title), ['later']);
  assert.deepEqual(groups.history.map((r) => r.title), ['done']);
  // Soonest active reminder whose moment has not yet passed.
  assert.equal(groups.next?.title, 'today-soon');
});

test('buildReminderGroups: passed moments today stay visible but are not "next"', () => {
  const groups = buildReminderGroups(
    [reminder({ title: 'missed-today', scheduledDate: '2026-08-16', scheduledTime: '07:00' })],
    NOW,
  );
  assert.equal(groups.next, null);
  assert.deepEqual(groups.today.map((r) => r.title), ['missed-today']);
});

test('filterReminderGroups narrows to one group per chip', () => {
  const groups: ReminderGroups = {
    next: reminder({ title: 'next' }),
    today: [reminder({ title: 't' })],
    upcoming: [reminder({ title: 'u' })],
    history: [reminder({ title: 'h', status: 'completed' })],
  };
  assert.equal(filterReminderGroups(groups, 'today').upcoming.length, 0);
  assert.equal(filterReminderGroups(groups, 'upcoming').today.length, 0);
  const done = filterReminderGroups(groups, 'done');
  assert.equal(done.next, null);
  assert.equal(done.today.length, 0);
  assert.equal(done.history.length, 1);
  assert.equal(filterReminderGroups(groups, 'all'), groups);
});

test('recurrence and status labels cover every model value', () => {
  for (const r of ['none', 'daily', 'weekly', 'monthly', 'yearly'] as const) {
    assert.ok(RECURRENCE_LABELS[r].length > 0);
  }
  for (const s of ['active', 'completed', 'dismissed', 'missed'] as const) {
    assert.ok(STATUS_LABELS[s].length > 0);
    assert.ok(historyStatusLabel(s).length > 0);
  }
});

/* ------------------------------------------------------------------ */
/*  Source guards — branded controls, centralized systems              */
/* ------------------------------------------------------------------ */

test('CreateReminder uses branded DatePicker + TimePicker, never native date/time inputs', () => {
  const screen = read('src/features/reminders/CreateReminder.tsx');
  assert.ok(screen.includes('DatePicker'), 'centralized DatePicker required');
  assert.ok(screen.includes('TimePicker'), 'branded TimePicker required');
  assert.ok(!screen.includes('type="date"'), 'native date input must be gone');
  assert.ok(!screen.includes('type="time"'), 'native time input must be gone');
  assert.ok(screen.includes('useToast'));
});

test('ReminderDetail uses the centralized Modal for delete confirmation', () => {
  const screen = read('src/features/reminders/ReminderDetail.tsx');
  assert.ok(screen.includes('Modal'), 'centralized Modal required');
  assert.ok(!screen.includes('th-modal-overlay'), 'no hand-rolled overlay');
});

test('RemindersHome uses the Stage 8 vocabulary', () => {
  const screen = read('src/features/reminders/RemindersHome.tsx');
  for (const cls of [
    'th-rem-header',
    'th-rem-filters',
    'th-rem-hero',
    'th-rem-section',
    'th-rem-row',
    'th-rem-footer',
  ]) {
    assert.ok(screen.includes(cls), `RemindersHome must use ${cls}`);
  }
});

test('the design system owns the Stage 8 reminder classes', () => {
  const css = read('src/components/primitives.css');
  for (const cls of [
    '.th-rem-header',
    '.th-rem-hero',
    '.th-rem-hero__when',
    '.th-rem-section__badge',
    '.th-rem-row',
    '.th-rem-row--history',
    '.th-rem-choice',
    '.th-rem-notify',
    '.th-rem-repeat-option',
    '.th-rem-detail__schedule',
    '.th-rem-detail__row',
    '.th-rem-delete-sheet__text',
  ]) {
    assert.ok(css.includes(cls), `primitives.css must define ${cls}`);
  }
});

test('TimePicker is exported from the centralized components index', () => {
  const index = read('src/components/index.ts');
  assert.ok(index.includes("export { TimePicker }"));
  const picker = read('src/components/TimePicker.tsx');
  assert.ok(picker.includes('Modal'), 'TimePicker reuses the centralized Modal');
  assert.ok(
    picker.includes('th-date-picker__wheel'),
    'TimePicker reuses the shared wheel vocabulary',
  );
});
