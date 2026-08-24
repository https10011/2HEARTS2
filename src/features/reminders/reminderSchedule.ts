/**
 * Reminder presentation helpers (Stage 8).
 *
 * Pure logic for turning the flat reminder list into a warm, date-forward
 * render model: 12-hour time formatting, relative day labels ("Today",
 * "Tomorrow"), recurrence/status copy, home grouping (next up / today /
 * upcoming / history), and chip filtering. Kept pure so Node tests can
 * cover every rule without rendering.
 */

import type {
  Reminder,
  ReminderRecurrence,
  ReminderStatus,
} from '../../data/reminder/reminderTypes.ts';

/* ------------------------------------------------------------------ */
/*  Labels                                                            */
/* ------------------------------------------------------------------ */

export const RECURRENCE_LABELS: Record<ReminderRecurrence, string> = {
  none: 'Does not repeat',
  daily: 'Daily',
  weekly: 'Weekly',
  monthly: 'Monthly',
  yearly: 'Yearly',
};

export const STATUS_LABELS: Record<ReminderStatus, string> = {
  active: 'Upcoming',
  completed: 'Completed',
  dismissed: 'Dismissed',
  missed: 'Missed',
};

/** Past-tense label used in the history section. */
export function historyStatusLabel(status: ReminderStatus): string {
  if (status === 'completed') return 'Done';
  if (status === 'missed') return 'Missed';
  return 'Dismissed';
}

/* ------------------------------------------------------------------ */
/*  Date / time formatting                                            */
/* ------------------------------------------------------------------ */

/** Formats "HH:mm" (24h) as a 12-hour display time, e.g. "9:00 AM". */
export function formatReminderTime(timeStr: string): string {
  const match = /^(\d{2}):(\d{2})$/.exec(timeStr);
  if (!match) return timeStr;
  const hours = Number(match[1]);
  const minutes = match[2];
  if (hours > 23 || Number(minutes) > 59) return timeStr;
  const period = hours < 12 ? 'AM' : 'PM';
  const hour12 = hours % 12 === 0 ? 12 : hours % 12;
  return `${hour12}:${minutes} ${period}`;
}

/** Formats a yyyy-mm-dd calendar key as "August 16, 2026" (UTC-safe). */
export function formatReminderDate(dateKey: string): string {
  const [year, month, day] = dateKey.split('-').map(Number);
  if (!year || !month || !day) return dateKey;
  const date = new Date(Date.UTC(year, month - 1, day));
  return date.toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    timeZone: 'UTC',
  });
}

/** Timezone-safe serial day number for a yyyy-mm-dd key. */
function daySerial(dateKey: string): number | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(dateKey);
  if (!match) return null;
  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  return Math.floor(Date.UTC(year, month - 1, day) / 86400000);
}

function todayKey(now: Date): string {
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

/** Local calendar-day difference: reminder day minus today. */
export function daysUntilReminder(dateKey: string, now: Date = new Date()): number | null {
  const target = daySerial(dateKey);
  const today = daySerial(todayKey(now));
  if (target === null || today === null) return null;
  return target - today;
}

/**
 * Relative day label: "Today", "Tomorrow", "Yesterday", or a formatted
 * date ("August 16" this year, "August 16, 2027" otherwise).
 */
export function relativeDayLabel(dateKey: string, now: Date = new Date()): string {
  const diff = daysUntilReminder(dateKey, now);
  if (diff === 0) return 'Today';
  if (diff === 1) return 'Tomorrow';
  if (diff === -1) return 'Yesterday';

  const [year, month, day] = dateKey.split('-').map(Number);
  if (!year || !month || !day) return dateKey;
  const date = new Date(Date.UTC(year, month - 1, day));
  const sameYear = year === now.getFullYear();
  return date.toLocaleDateString(undefined, {
    month: 'long',
    day: 'numeric',
    ...(sameYear ? {} : { year: 'numeric' }),
    timeZone: 'UTC',
  });
}

/* ------------------------------------------------------------------ */
/*  Grouping / filtering                                              */
/* ------------------------------------------------------------------ */

export interface ReminderGroups {
  /** The soonest active reminder today-or-later — the "next up" hero. */
  next: Reminder | null;
  /** Active reminders scheduled for today. */
  today: Reminder[];
  /** Active reminders scheduled after today. */
  upcoming: Reminder[];
  /** Completed / dismissed / missed, newest schedule first. */
  history: Reminder[];
}

function sortByScheduleAsc(a: Reminder, b: Reminder): number {
  const byDate = a.scheduledDate.localeCompare(b.scheduledDate);
  if (byDate !== 0) return byDate;
  return a.scheduledTime.localeCompare(b.scheduledTime);
}

function sortByScheduleDesc(a: Reminder, b: Reminder): number {
  return sortByScheduleAsc(b, a);
}

/** Is this reminder's moment still ahead of us right now? */
function isAhead(reminder: Reminder, now: Date): boolean {
  const diff = daysUntilReminder(reminder.scheduledDate, now);
  if (diff === null) return false;
  if (diff > 0) return true;
  if (diff < 0) return false;
  // Same local day — compare wall-clock time.
  const [h, m] = reminder.scheduledTime.split(':').map(Number);
  const minutes = (h ?? 0) * 60 + (m ?? 0);
  return minutes >= now.getHours() * 60 + now.getMinutes();
}

/**
 * Groups reminders for the home screen. Active reminders are split into
 * today / upcoming (ascending); everything with a terminal status lands in
 * history (descending). `next` is the soonest active reminder whose moment
 * has not yet passed.
 */
export function buildReminderGroups(
  reminders: Reminder[],
  now: Date = new Date(),
): ReminderGroups {
  const active = reminders.filter((r) => r.status === 'active').sort(sortByScheduleAsc);
  const history = reminders.filter((r) => r.status !== 'active').sort(sortByScheduleDesc);

  const today: Reminder[] = [];
  const upcoming: Reminder[] = [];
  for (const reminder of active) {
    const diff = daysUntilReminder(reminder.scheduledDate, now);
    if (diff === 0) today.push(reminder);
    else if (diff !== null && diff > 0) upcoming.push(reminder);
    else today.push(reminder); // malformed or stale — keep it visible
  }

  const next = active.find((r) => isAhead(r, now)) ?? null;

  return { next, today, upcoming, history };
}

export type ReminderFilter = 'all' | 'today' | 'upcoming' | 'done';

/** Applies the home filter chips to the grouped model. */
export function filterReminderGroups(
  groups: ReminderGroups,
  filter: ReminderFilter,
): ReminderGroups {
  switch (filter) {
    case 'today':
      return { ...groups, upcoming: [], history: [] };
    case 'upcoming':
      return { ...groups, today: [], history: [] };
    case 'done':
      return { next: null, today: [], upcoming: [], history: groups.history };
    case 'all':
    default:
      return groups;
  }
}

/** True when the reminder is in a terminal state. */
export function isHistoryStatus(status: ReminderStatus): boolean {
  return status !== 'active';
}
