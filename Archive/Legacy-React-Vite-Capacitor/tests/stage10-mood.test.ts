/**
 * Stage 10 — Mood Experience tests.
 *
 * Covers the pure presentation helpers behind the productized Mood
 * screens: warm day/month formatting, range filtering, month grouping,
 * real distribution summaries, consecutive-day check-in streaks, and
 * the shared/"same wavelength" helpers. All values derive from real
 * entry shapes — nothing fabricated.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

import type { MoodEntry, MoodValue } from '../src/data/mood/moodTypes.ts';
import {
  MOOD_DESCRIPTIONS,
  OWNER_PROFILE_ID,
  PARTNER_PROFILE_ID,
  buildMoodMonths,
  computeCheckInStreak,
  countSharedDays,
  dayDiff,
  filterByRange,
  findSameWavelength,
  formatCheckInDate,
  formatMoodDay,
  formatMoodMonth,
  latestForProfile,
  localDateKey,
  shiftDateKey,
  summarizeMoods,
  summaryHeadline,
} from '../src/features/mood/moodPresentation.ts';
import { MOOD_VALUES, MOOD_EMOJI } from '../src/data/mood/moodTypes.ts';

let seq = 0;
function makeEntry(overrides: Partial<MoodEntry> = {}): MoodEntry {
  seq += 1;
  const moodValue: MoodValue = overrides.moodValue ?? 'happy';
  return {
    id: overrides.id ?? `entry-${seq}`,
    moodValue,
    moodEmoji: MOOD_EMOJI[moodValue],
    note: null,
    profileId: OWNER_PROFILE_ID,
    entryDate: '2026-08-27',
    createdAt: '2026-08-27T00:00:00.000Z',
    updatedAt: '2026-08-27T00:00:00.000Z',
    deletedAt: null,
    ...overrides,
  };
}

// ---------------------------------------------------------------------------
// Mood copy coverage
// ---------------------------------------------------------------------------

describe('MOOD_DESCRIPTIONS', () => {
  it('covers every approved mood value', () => {
    for (const mood of MOOD_VALUES) {
      assert.equal(typeof MOOD_DESCRIPTIONS[mood], 'string');
      assert.ok(MOOD_DESCRIPTIONS[mood].length > 0);
    }
  });
});

// ---------------------------------------------------------------------------
// Date helpers
// ---------------------------------------------------------------------------

describe('localDateKey / dayDiff / shiftDateKey', () => {
  it('formats a local yyyy-mm-dd key', () => {
    assert.equal(localDateKey(new Date(2026, 7, 27)), '2026-08-27');
  });

  it('computes whole-day differences', () => {
    assert.equal(dayDiff('2026-08-27', '2026-08-27'), 0);
    assert.equal(dayDiff('2026-08-27', '2026-08-20'), 7);
    assert.equal(dayDiff('2026-08-20', '2026-08-27'), -7);
  });

  it('shifts keys across month boundaries', () => {
    assert.equal(shiftDateKey('2026-08-01', -1), '2026-07-31');
    assert.equal(shiftDateKey('2026-12-31', 1), '2027-01-01');
  });
});

describe('formatMoodDay', () => {
  const today = '2026-08-27';

  it('labels today and yesterday warmly', () => {
    assert.equal(formatMoodDay('2026-08-27', today), 'Today');
    assert.equal(formatMoodDay('2026-08-26', today), 'Yesterday');
  });

  it('formats older same-year dates without the year', () => {
    const label = formatMoodDay('2026-08-20', today);
    assert.ok(label.includes('Aug'));
    assert.ok(label.includes('20'));
    assert.ok(!label.includes('2026'));
  });

  it('includes the year for other years', () => {
    assert.ok(formatMoodDay('2025-12-31', today).includes('2025'));
  });
});

describe('formatMoodMonth / formatCheckInDate', () => {
  it('formats a month heading', () => {
    assert.equal(formatMoodMonth('2026-08-27'), 'August 2026');
  });

  it('prefixes the check-in chip with Today', () => {
    assert.equal(formatCheckInDate('2026-08-27'), 'Today · August 27');
  });
});

// ---------------------------------------------------------------------------
// Grouping + filtering
// ---------------------------------------------------------------------------

describe('buildMoodMonths', () => {
  it('returns no groups for no entries', () => {
    assert.deepEqual(buildMoodMonths([]), []);
  });

  it('groups date-desc entries into ordered month sections', () => {
    const groups = buildMoodMonths([
      makeEntry({ entryDate: '2026-08-27' }),
      makeEntry({ entryDate: '2026-08-01' }),
      makeEntry({ entryDate: '2026-07-15' }),
    ]);
    assert.equal(groups.length, 2);
    assert.equal(groups[0]!.monthLabel, 'August 2026');
    assert.equal(groups[0]!.entries.length, 2);
    assert.equal(groups[1]!.monthLabel, 'July 2026');
    assert.equal(groups[1]!.entries.length, 1);
  });
});

describe('filterByRange', () => {
  const today = '2026-08-27';
  const entries = [
    makeEntry({ entryDate: '2026-08-27' }),
    makeEntry({ entryDate: '2026-08-21' }),
    makeEntry({ entryDate: '2026-08-01' }),
    makeEntry({ entryDate: '2026-06-01' }),
  ];

  it('keeps the last 7 days for week', () => {
    const week = filterByRange(entries, 'week', today);
    assert.deepEqual(week.map((e) => e.entryDate), ['2026-08-27', '2026-08-21']);
  });

  it('keeps the last 30 days for month', () => {
    const month = filterByRange(entries, 'month', today);
    assert.deepEqual(
      month.map((e) => e.entryDate),
      ['2026-08-27', '2026-08-21', '2026-08-01'],
    );
  });

  it('keeps everything for all', () => {
    assert.equal(filterByRange(entries, 'all', today).length, 4);
  });
});

// ---------------------------------------------------------------------------
// Summary
// ---------------------------------------------------------------------------

describe('summarizeMoods / summaryHeadline', () => {
  it('returns an empty summary for no entries', () => {
    const summary = summarizeMoods([]);
    assert.equal(summary.total, 0);
    assert.equal(summary.top, null);
    assert.deepEqual(summary.distribution, []);
    assert.equal(summaryHeadline(summary), null);
  });

  it('counts real entries and finds the top mood', () => {
    const summary = summarizeMoods([
      makeEntry({ moodValue: 'happy' }),
      makeEntry({ moodValue: 'happy' }),
      makeEntry({ moodValue: 'tired' }),
    ]);
    assert.equal(summary.total, 3);
    assert.equal(summary.top, 'happy');
    assert.deepEqual(summary.distribution, [
      { mood: 'happy', count: 2 },
      { mood: 'tired', count: 1 },
    ]);
    assert.equal(summaryHeadline(summary), 'Mostly feeling happy');
  });

  it('uses the display label in the headline', () => {
    const summary = summarizeMoods([makeEntry({ moodValue: 'love' })]);
    assert.equal(summaryHeadline(summary), 'Mostly feeling in love');
  });
});

// ---------------------------------------------------------------------------
// Streak
// ---------------------------------------------------------------------------

describe('computeCheckInStreak', () => {
  const today = '2026-08-27';

  it('is zero with no recent check-ins', () => {
    assert.equal(computeCheckInStreak([], today), 0);
    assert.equal(
      computeCheckInStreak([makeEntry({ entryDate: '2026-08-20' })], today),
      0,
    );
  });

  it('counts consecutive days ending today', () => {
    const entries = ['2026-08-27', '2026-08-26', '2026-08-25'].map((d) =>
      makeEntry({ entryDate: d }),
    );
    assert.equal(computeCheckInStreak(entries, today), 3);
  });

  it('anchors to yesterday when today is unlogged', () => {
    const entries = ['2026-08-26', '2026-08-25'].map((d) =>
      makeEntry({ entryDate: d }),
    );
    assert.equal(computeCheckInStreak(entries, today), 2);
  });

  it('breaks on a gap', () => {
    const entries = ['2026-08-27', '2026-08-25'].map((d) =>
      makeEntry({ entryDate: d }),
    );
    assert.equal(computeCheckInStreak(entries, today), 1);
  });
});

// ---------------------------------------------------------------------------
// Together helpers
// ---------------------------------------------------------------------------

describe('countSharedDays / findSameWavelength / latestForProfile', () => {
  it('counts only days where both profiles checked in', () => {
    const entries = [
      makeEntry({ entryDate: '2026-08-27', profileId: OWNER_PROFILE_ID }),
      makeEntry({ entryDate: '2026-08-27', profileId: PARTNER_PROFILE_ID }),
      makeEntry({ entryDate: '2026-08-26', profileId: OWNER_PROFILE_ID }),
    ];
    assert.equal(countSharedDays(entries), 1);
  });

  it('finds the most recent same-mood day for both profiles', () => {
    const entries = [
      makeEntry({ entryDate: '2026-08-27', profileId: OWNER_PROFILE_ID, moodValue: 'calm' }),
      makeEntry({ entryDate: '2026-08-27', profileId: PARTNER_PROFILE_ID, moodValue: 'happy' }),
      makeEntry({ entryDate: '2026-08-26', profileId: OWNER_PROFILE_ID, moodValue: 'love' }),
      makeEntry({ entryDate: '2026-08-26', profileId: PARTNER_PROFILE_ID, moodValue: 'love' }),
    ];
    assert.deepEqual(findSameWavelength(entries), {
      entryDate: '2026-08-26',
      mood: 'love',
    });
  });

  it('returns null when moods never matched', () => {
    const entries = [
      makeEntry({ entryDate: '2026-08-27', profileId: OWNER_PROFILE_ID, moodValue: 'calm' }),
      makeEntry({ entryDate: '2026-08-27', profileId: PARTNER_PROFILE_ID, moodValue: 'sad' }),
    ];
    assert.equal(findSameWavelength(entries), null);
  });

  it('finds the latest entry per profile from a date-desc list', () => {
    const first = makeEntry({ entryDate: '2026-08-27', profileId: OWNER_PROFILE_ID });
    const partner = makeEntry({ entryDate: '2026-08-26', profileId: PARTNER_PROFILE_ID });
    const entries = [first, partner, makeEntry({ entryDate: '2026-08-25' })];
    assert.equal(latestForProfile(entries, OWNER_PROFILE_ID), first);
    assert.equal(latestForProfile(entries, PARTNER_PROFILE_ID), partner);
    assert.equal(latestForProfile(entries, 'nobody'), null);
  });
});
