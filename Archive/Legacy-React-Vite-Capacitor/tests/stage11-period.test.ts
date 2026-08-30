/**
 * Stage 11 — Period Tracker presentation tests.
 *
 * Covers the pure presentation helpers behind the productized Period
 * screens: warm/relative date labels, month grouping, entry durations,
 * cycle-length derivation, flow labels, and calendar cell status. All
 * values derive from real persisted entry/settings shapes — nothing
 * fabricated.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

import type { PeriodEntry } from '../src/data/period/periodTypes.ts';
import {
  OWNER_PROFILE_ID,
  buildPeriodMonths,
  cellStatus,
  cycleLengthToPrevious,
  dayDiff,
  entryDurationDays,
  flowLabel,
  flowOptions,
  formatLogDate,
  formatPeriodDay,
  formatPeriodMonth,
  isInMonth,
  localDateKey,
  shiftDateKey,
} from '../src/features/period/periodPresentation.ts';

let seq = 0;
function makeEntry(overrides: Partial<PeriodEntry> = {}): PeriodEntry {
  seq += 1;
  return {
    id: overrides.id ?? `entry-${seq}`,
    startDate: '2026-08-20',
    endDate: '2026-08-24',
    flowLevel: overrides.flowLevel ?? 'medium',
    note: null,
    profileId: OWNER_PROFILE_ID,
    createdAt: '2026-08-20T00:00:00.000Z',
    updatedAt: '2026-08-20T00:00:00.000Z',
    deletedAt: null,
    ...overrides,
  };
}

// ---------------------------------------------------------------------------
// Flow metadata
// ---------------------------------------------------------------------------

describe('flow helpers', () => {
  it('labels every flow level', () => {
    assert.equal(flowLabel('light'), 'Light');
    assert.equal(flowLabel('medium'), 'Medium');
    assert.equal(flowLabel('heavy'), 'Heavy');
  });

  it('returns the three flow options in stable order', () => {
    const options = flowOptions();
    assert.deepEqual(
      options.map((o) => o.value),
      ['light', 'medium', 'heavy'],
    );
    assert.equal(options[0]?.label, 'Light');
  });
});

// ---------------------------------------------------------------------------
// Date helpers
// ---------------------------------------------------------------------------

describe('date helpers', () => {
  it('localDateKey emits yyyy-mm-dd for a known date', () => {
    assert.equal(localDateKey(new Date(2026, 7, 27)), '2026-08-27');
  });

  it('shiftDateKey moves across month boundaries', () => {
    assert.equal(shiftDateKey('2026-08-31', 1), '2026-09-01');
    assert.equal(shiftDateKey('2026-08-01', -1), '2026-07-31');
  });

  it('dayDiff computes whole-day deltas', () => {
    assert.equal(dayDiff('2026-08-27', '2026-08-20'), 7);
    assert.equal(dayDiff('2026-08-20', '2026-08-27'), -7);
  });

  it('formatPeriodDay gives Today / Yesterday / weekday', () => {
    assert.equal(formatPeriodDay('2026-08-27', '2026-08-27'), 'Today');
    assert.equal(formatPeriodDay('2026-08-26', '2026-08-27'), 'Yesterday');
    const label = formatPeriodDay('2026-08-20', '2026-08-27');
    assert.match(label, /Thu/i);
  });

  it('formatPeriodMonth labels the month with year', () => {
    assert.equal(formatPeriodMonth('2026-08-20'), 'August 2026');
  });

  it('isInMonth matches the month grid', () => {
    assert.equal(isInMonth('2026-08-05', 2026, 7), true);
    assert.equal(isInMonth('2026-08-05', 2026, 6), false);
    assert.equal(isInMonth('2026-09-05', 2026, 7), false);
  });

  it('formatLogDate labels today clearly', () => {
    assert.match(formatLogDate('2026-08-27', '2026-08-27'), /Today/);
    assert.match(formatLogDate('2026-08-27', '2026-08-27'), /August 27/);
    assert.equal(formatLogDate('2026-08-20', '2026-08-27').startsWith('Today'), false);
  });
});

// ---------------------------------------------------------------------------
// Entry presentation
// ---------------------------------------------------------------------------

describe('entry duration + cycle length', () => {
  it('computes inclusive duration for dated entries', () => {
    const entry = makeEntry({ startDate: '2026-08-20', endDate: '2026-08-24' });
    assert.equal(entryDurationDays(entry), 5);
  });

  it('returns null duration for ongoing entries', () => {
    const entry = makeEntry({ startDate: '2026-08-20', endDate: null });
    assert.equal(entryDurationDays(entry), null);
  });

  it('derives a sane cycle length to a previous start', () => {
    assert.equal(cycleLengthToPrevious(makeEntry({ startDate: '2026-08-20' }), '2026-07-23'), 28);
  });

  it('returns null cycle length without a previous start', () => {
    assert.equal(cycleLengthToPrevious(makeEntry(), null), null);
  });
});

// ---------------------------------------------------------------------------
// History grouping
// ---------------------------------------------------------------------------

describe('buildPeriodMonths', () => {
  it('groups date-desc entries by month, preserving order', () => {
    const entries = [
      makeEntry({ startDate: '2026-08-20' }),
      makeEntry({ startDate: '2026-08-05' }),
      makeEntry({ startDate: '2026-07-28' }),
    ];
    const groups = buildPeriodMonths(entries);
    assert.equal(groups.length, 2);
    assert.equal(groups[0]!.monthLabel, 'August 2026');
    assert.equal(groups[0]!.entries.length, 2);
    assert.equal(groups[1]!.monthLabel, 'July 2026');
    assert.equal(groups[1]!.entries.length, 1);
  });

  it('handles an empty list', () => {
    assert.deepEqual(buildPeriodMonths([]), []);
  });
});

// ---------------------------------------------------------------------------
// Calendar cell status
// ---------------------------------------------------------------------------

describe('cellStatus', () => {
  const period = new Set<string>(['2026-08-20', '2026-08-21']);
  const predicted = new Set<string>(['2026-09-19']);

  it('marks logged period days', () => {
    const s = cellStatus('2026-08-20', '2026-08-27', period, predicted);
    assert.equal(s.fill, 'period');
    assert.equal(s.isToday, false);
  });

  it('marks predicted days only when not logged', () => {
    const s = cellStatus('2026-09-19', '2026-08-27', period, predicted);
    assert.equal(s.fill, 'predicted');
    assert.equal(s.isToday, false);
  });

  it('logged period day wins over predicted/today fill', () => {
    const both = new Set<string>(['2026-08-27']);
    const s = cellStatus('2026-08-27', '2026-08-27', both, predicted);
    assert.equal(s.fill, 'period');
    assert.equal(s.isToday, true);
  });

  it('flags today separately when otherwise unmarked', () => {
    const s = cellStatus('2026-08-27', '2026-08-27', period, predicted);
    assert.equal(s.fill, 'none');
    assert.equal(s.isToday, true);
  });

  it('returns none for ordinary days', () => {
    const s = cellStatus('2026-08-10', '2026-08-27', period, predicted);
    assert.equal(s.fill, 'none');
    assert.equal(s.isToday, false);
  });
});