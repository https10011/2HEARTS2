/**
 * Reminder domain model + serializer (Phase 13).
 *
 * Local reminder system with recurrence, notification scheduling, and
 * missed-reminder handling. Follows Phase 2 Entity conventions (UUID v4,
 * UTC ISO timestamps, tombstone).
 *
 * Conventions:
 * - `scheduledDate`  — LOCAL calendar day `yyyy-mm-dd` (like ImportantDate).
 * - `scheduledTime`  — LOCAL wall-clock time `HH:mm` (24h).
 * - `fireAt`         — UTC ISO 8601 moment the notification should fire.
 * - `createdAt/updatedAt` — UTC ISO 8601 (Entity standard).
 * - `deletedAt` — tombstone for soft deletes.
 */

import { assertEntityConventions, type TombstonedEntity } from '../model/entity.ts';
import {
  optionalString,
  requireEnum,
  requireString,
  type EntitySerializer,
  type Params,
  type Row,
} from '../serialization/entitySerializer.ts';
import { isValidDateKey } from '../../utils/time.ts';

// ---------------------------------------------------------------------------
// Reminder recurrence
// ---------------------------------------------------------------------------

export const REMINDER_RECURRENCES = ['none', 'daily', 'weekly', 'monthly', 'yearly'] as const;
export type ReminderRecurrence = (typeof REMINDER_RECURRENCES)[number];

// ---------------------------------------------------------------------------
// Reminder status
// ---------------------------------------------------------------------------

export const REMINDER_STATUSES = ['active', 'completed', 'dismissed', 'missed'] as const;
export type ReminderStatus = (typeof REMINDER_STATUSES)[number];

// ---------------------------------------------------------------------------
// Reminder entity
// ---------------------------------------------------------------------------

export interface Reminder extends TombstonedEntity {
  /** Reminder title / summary. */
  title: string;
  /** Optional longer description / notes. */
  description: string | null;
  /** Local calendar day `yyyy-mm-dd`. */
  scheduledDate: string;
  /** Local wall-clock time `HH:mm` (24h). */
  scheduledTime: string;
  /** Recurrence rule. */
  recurrence: ReminderRecurrence;
  /** Current reminder status. */
  status: ReminderStatus;
  /** Notification owner ref for the NotificationService bridge. */
  notificationOwnerRef: string | null;
  /** Whether notification scheduling is enabled for this reminder. */
  notificationEnabled: boolean;
}

/**
 * Creates a new Reminder with defaults (no ID, no timestamps — entity layer
 * assigns those).
 */
export type NewReminder = Omit<Reminder, 'id' | 'createdAt' | 'updatedAt' | 'deletedAt'>;

export function assertReminder(reminder: Reminder): void {
  assertEntityConventions(reminder);
  if (reminder.title.trim().length === 0) {
    throw new Error('reminder.title must not be empty');
  }
  if (!isValidDateKey(reminder.scheduledDate)) {
    throw new Error('reminder.scheduledDate must be a yyyy-mm-dd calendar date');
  }
  if (!/^\d{2}:\d{2}$/.test(reminder.scheduledTime)) {
    throw new Error('reminder.scheduledTime must be HH:mm format');
  }
}

// ---------------------------------------------------------------------------
// Serializer
// ---------------------------------------------------------------------------

export const REMINDER_COLUMNS = [
  'id',
  'title',
  'description',
  'scheduled_date',
  'scheduled_time',
  'recurrence',
  'status',
  'notification_owner_ref',
  'notification_enabled',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const reminderSerializer: EntitySerializer<Reminder> = {
  columns: REMINDER_COLUMNS,
  toParams(reminder: Reminder): Params {
    return [
      reminder.id,
      reminder.title,
      reminder.description,
      reminder.scheduledDate,
      reminder.scheduledTime,
      reminder.recurrence,
      reminder.status,
      reminder.notificationOwnerRef,
      reminder.notificationEnabled ? 1 : 0,
      reminder.createdAt,
      reminder.updatedAt,
      reminder.deletedAt,
    ];
  },
  fromRow(row: Row): Reminder {
    return {
      id: requireString(row, 'id'),
      title: requireString(row, 'title'),
      description: optionalString(row, 'description'),
      scheduledDate: requireString(row, 'scheduled_date'),
      scheduledTime: requireString(row, 'scheduled_time'),
      recurrence: requireEnum(row, 'recurrence', REMINDER_RECURRENCES),
      status: requireEnum(row, 'status', REMINDER_STATUSES),
      notificationOwnerRef: optionalString(row, 'notification_owner_ref'),
      notificationEnabled: (row.notification_enabled as number) === 1,
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};

// ---------------------------------------------------------------------------
// Recurrence helpers
// ---------------------------------------------------------------------------

/**
 * Calculates the next occurrence date for a recurring reminder.
 * Uses simple calendar arithmetic (no date library).
 */
export function nextOccurrence(dateStr: string, recurrence: ReminderRecurrence): string | null {
  if (recurrence === 'none') return null;

  const parts = dateStr.split('-').map(Number);
  const year = parts[0]!;
  const month = parts[1]!;
  const day = parts[2]!;

  if (recurrence === 'daily' || recurrence === 'weekly') {
    // Add days — roll into next month(s) when crossing month boundaries.
    const increment = recurrence === 'daily' ? 1 : 7;
    const base = new Date(year, month - 1, day + increment);
    return formatDate(base);
  }

  if (recurrence === 'monthly') {
    // Same day next month, clamped to last day of that month.
    let nextMonth = month + 1;
    let nextYear = year;
    if (nextMonth > 12) {
      nextMonth = 1;
      nextYear += 1;
    }
    const maxDay = new Date(nextYear, nextMonth, 0).getDate();
    const nextDay = Math.min(day, maxDay);
    return `${String(nextYear).padStart(4, '0')}-${String(nextMonth).padStart(2, '0')}-${String(nextDay).padStart(2, '0')}`;
  }

  if (recurrence === 'yearly') {
    return `${String(year + 1).padStart(4, '0')}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }

  return null;
}

function formatDate(d: Date): string {
  const y = d.getFullYear();
  const m = d.getMonth() + 1;
  const day = d.getDate();
  return `${String(y).padStart(4, '0')}-${String(m).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

/**
 * Formats a date + time into a display-friendly string.
 */
export function formatReminderDateTime(dateStr: string, timeStr: string): string {
  const [y, m, d] = dateStr.split('-');
  return `${m}/${d}/${y} at ${timeStr}`;
}
