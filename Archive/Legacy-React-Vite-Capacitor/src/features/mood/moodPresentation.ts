/**
 * Mood presentation helpers (Stage 10).
 *
 * Pure, framework-free logic for the Mood experience: warm date labels,
 * month grouping, mood distribution summaries, check-in streaks, and
 * shared/"same wavelength" moments. Everything is computed from real
 * persisted entries — nothing is fabricated.
 *
 * Node-test reachable: relative imports with .ts extensions only.
 */

import type { MoodEntry, MoodValue } from '../../data/mood/moodTypes.ts';
import { MOOD_LABELS } from '../../data/mood/moodTypes.ts';

// ---------------------------------------------------------------------------
// Profile conventions (repo-wide placeholder ids — see vault/period features)
// ---------------------------------------------------------------------------

export const OWNER_PROFILE_ID = 'owner';
export const PARTNER_PROFILE_ID = 'partner';

// ---------------------------------------------------------------------------
// Warm per-mood copy (restrained; used under the day's mood identity)
// ---------------------------------------------------------------------------

export const MOOD_DESCRIPTIONS: Record<MoodValue, string> = {
  happy: 'Feeling good today.',
  love: 'Heart full today.',
  excited: 'Something to look forward to.',
  calm: 'At ease and settled.',
  grateful: 'Thankful for the little things.',
  neutral: 'An even, ordinary day.',
  tired: 'Running low on energy.',
  sad: 'A heavy-hearted day.',
  anxious: 'Carrying some worry.',
  stressed: 'Under a bit of pressure.',
};

// ---------------------------------------------------------------------------
// Date helpers (entryDate is a LOCAL `yyyy-mm-dd` calendar key)
// ---------------------------------------------------------------------------

/** Local calendar key for a Date (mirrors services/datetime toLocalDateKey). */
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
  const ms = keyToUtcDate(a).getTime() - keyToUtcDate(b).getTime();
  return Math.round(ms / 86_400_000);
}

/** "Today" / "Yesterday" / "Mon, Aug 25" (with year when not current). */
export function formatMoodDay(entryDate: string, today: string): string {
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
export function formatMoodMonth(entryDate: string): string {
  return keyToUtcDate(entryDate).toLocaleDateString(undefined, {
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  });
}

/** "Today · August 27" chip label for the check-in composer. */
export function formatCheckInDate(today: string): string {
  const label = keyToUtcDate(today).toLocaleDateString(undefined, {
    month: 'long',
    day: 'numeric',
    timeZone: 'UTC',
  });
  return `Today · ${label}`;
}

// ---------------------------------------------------------------------------
// History grouping / filtering
// ---------------------------------------------------------------------------

export interface MoodMonthGroup {
  monthLabel: string;
  entries: MoodEntry[];
}

/** Groups entries (assumed date-desc) into month sections, order preserved. */
export function buildMoodMonths(entries: MoodEntry[]): MoodMonthGroup[] {
  const groups: MoodMonthGroup[] = [];
  let currentKey = '';
  for (const entry of entries) {
    const key = entry.entryDate.slice(0, 7);
    if (key !== currentKey) {
      currentKey = key;
      groups.push({ monthLabel: formatMoodMonth(entry.entryDate), entries: [] });
    }
    groups[groups.length - 1]!.entries.push(entry);
  }
  return groups;
}

export type MoodRange = 'week' | 'month' | 'all';

/** Filters entries to the last 7 / 30 days (inclusive of today), or all. */
export function filterByRange(
  entries: MoodEntry[],
  range: MoodRange,
  today: string,
): MoodEntry[] {
  if (range === 'all') return entries;
  const windowDays = range === 'week' ? 7 : 30;
  return entries.filter((e) => {
    const diff = dayDiff(today, e.entryDate);
    return diff >= 0 && diff < windowDays;
  });
}

// ---------------------------------------------------------------------------
// Summary (real distribution — no fabricated statistics)
// ---------------------------------------------------------------------------

export interface MoodSummary {
  total: number;
  top: MoodValue | null;
  /** Distribution sorted by count desc (stable for equal counts). */
  distribution: Array<{ mood: MoodValue; count: number }>;
}

export function summarizeMoods(entries: MoodEntry[]): MoodSummary {
  const counts = new Map<MoodValue, number>();
  for (const entry of entries) {
    counts.set(entry.moodValue, (counts.get(entry.moodValue) ?? 0) + 1);
  }
  const distribution = Array.from(counts.entries())
    .map(([mood, count]) => ({ mood, count }))
    .sort((a, b) => b.count - a.count);
  return {
    total: entries.length,
    top: distribution[0]?.mood ?? null,
    distribution,
  };
}

/** "Mostly feeling happy" headline for a summary, or null when empty. */
export function summaryHeadline(summary: MoodSummary): string | null {
  if (!summary.top) return null;
  return `Mostly feeling ${MOOD_LABELS[summary.top].toLowerCase()}`;
}

// ---------------------------------------------------------------------------
// Check-in streak (consecutive days with at least one check-in,
// anchored to today or yesterday so an unfinished today doesn't break it)
// ---------------------------------------------------------------------------

export function computeCheckInStreak(entries: MoodEntry[], today: string): number {
  const days = new Set(entries.map((e) => e.entryDate));
  let anchor: string;
  if (days.has(today)) {
    anchor = today;
  } else {
    const yesterday = shiftDateKey(today, -1);
    if (!days.has(yesterday)) return 0;
    anchor = yesterday;
  }
  let streak = 0;
  let cursor = anchor;
  while (days.has(cursor)) {
    streak += 1;
    cursor = shiftDateKey(cursor, -1);
  }
  return streak;
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

// ---------------------------------------------------------------------------
// Together moments (both partners' real entries only)
// ---------------------------------------------------------------------------

/** Number of days on which two or more distinct profiles checked in. */
export function countSharedDays(entries: MoodEntry[]): number {
  const profilesByDay = new Map<string, Set<string>>();
  for (const entry of entries) {
    const set = profilesByDay.get(entry.entryDate) ?? new Set<string>();
    set.add(entry.profileId);
    profilesByDay.set(entry.entryDate, set);
  }
  let shared = 0;
  for (const set of profilesByDay.values()) {
    if (set.size >= 2) shared += 1;
  }
  return shared;
}

export interface WavelengthMoment {
  entryDate: string;
  mood: MoodValue;
}

/**
 * Most recent day on which two distinct profiles logged the SAME mood,
 * or null when it has never happened. Entries assumed date-desc.
 */
export function findSameWavelength(entries: MoodEntry[]): WavelengthMoment | null {
  const seen = new Map<string, Map<MoodValue, Set<string>>>();
  for (const entry of entries) {
    const byMood = seen.get(entry.entryDate) ?? new Map<MoodValue, Set<string>>();
    const profiles = byMood.get(entry.moodValue) ?? new Set<string>();
    profiles.add(entry.profileId);
    byMood.set(entry.moodValue, profiles);
    seen.set(entry.entryDate, byMood);
    if (profiles.size >= 2) {
      return { entryDate: entry.entryDate, mood: entry.moodValue };
    }
  }
  return null;
}

/** Latest entry for one profile from a date-desc list, or null. */
export function latestForProfile(entries: MoodEntry[], profileId: string): MoodEntry | null {
  return entries.find((e) => e.profileId === profileId) ?? null;
}
