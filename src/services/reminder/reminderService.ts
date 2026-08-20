/**
 * Reminder service (Phase 13).
 *
 * Application-facing boundary for the local reminder system.
 * Manages: create, update, delete, status transitions, recurrence,
 * notification scheduling/cancellation, validation, error normalization.
 *
 * Architecture: UI → ReminderService → ReminderRepository → Local persistence.
 * Notification scheduling: ReminderService → NotificationService → Plugin.
 */

import { AppError, normalizeAppError } from '../errors/appError.ts';
import { createLogger } from '../logging/logger.ts';
import {
  type Reminder,
  type NewReminder,
  type ReminderRecurrence,
  type ReminderStatus,
  nextOccurrence,
} from '../../data/reminder/reminderTypes.ts';
import { ReminderRepository } from '../../repositories/reminderRepository.ts';
import type { NotificationService } from '../notifications/notificationService.ts';

const log = createLogger('reminders');

export interface CreateReminderInput {
  title: string;
  description?: string | null;
  scheduledDate: string;
  scheduledTime: string;
  recurrence: ReminderRecurrence;
  notificationEnabled: boolean;
}

export interface UpdateReminderInput {
  title?: string;
  description?: string | null;
  scheduledDate?: string;
  scheduledTime?: string;
  recurrence?: ReminderRecurrence;
  status?: ReminderStatus;
  notificationEnabled?: boolean;
}

export class ReminderService {
  constructor(
    private readonly repository: ReminderRepository,
    private readonly notificationService: NotificationService | null,
  ) {}

  // -----------------------------------------------------------------------
  // Create
  // -----------------------------------------------------------------------

  async create(input: CreateReminderInput): Promise<Reminder> {
    this.validateInput(input);

    const ownerRef = `reminder-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;

    const data: NewReminder = {
      title: input.title.trim(),
      description: input.description?.trim() ?? null,
      scheduledDate: input.scheduledDate,
      scheduledTime: input.scheduledTime,
      recurrence: input.recurrence,
      status: 'active',
      notificationOwnerRef: ownerRef,
      notificationEnabled: input.notificationEnabled,
    };

    try {
      const reminder = await this.repository.create(data);

      if (input.notificationEnabled) {
        await this.scheduleNotification(reminder);
      }

      return reminder;
    } catch (cause) {
      throw normalizeAppError(cause, 'validation', 'create-failed', {
        recoverable: false,
        userMessage: 'Could not create reminder.',
      });
    }
  }

  // -----------------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------------

  async update(id: string, input: UpdateReminderInput): Promise<Reminder> {
    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Reminder not found.',
      });
    }

    if (input.title !== undefined) this.validateTitle(input.title);
    if (input.scheduledDate !== undefined) this.validateDate(input.scheduledDate);
    if (input.scheduledTime !== undefined) this.validateTime(input.scheduledTime);

    try {
      const updated = await this.repository.update(id, {
        ...input,
        title: input.title?.trim(),
        description: input.description?.trim(),
      });
      if (!updated) throw new Error('Update returned null');

      // Re-schedule notification if enabled
      if (updated.notificationEnabled) {
        await this.scheduleNotification(updated);
      } else {
        await this.cancelNotification(updated);
      }

      return updated;
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'validation', 'update-failed', {
        recoverable: false,
        userMessage: 'Could not update reminder.',
      });
    }
  }

  // -----------------------------------------------------------------------
  // Delete
  // -----------------------------------------------------------------------

  async delete(id: string): Promise<void> {
    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Reminder not found.',
      });
    }

    try {
      await this.cancelNotification(existing);
      await this.repository.delete(id);
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'validation', 'delete-failed', {
        recoverable: false,
        userMessage: 'Could not delete reminder.',
      });
    }
  }

  // -----------------------------------------------------------------------
  // Read
  // -----------------------------------------------------------------------

  async getById(id: string): Promise<Reminder | null> {
    return this.repository.getById(id);
  }

  async list(): Promise<Reminder[]> {
    return this.repository.list();
  }

  async listUpcoming(): Promise<Reminder[]> {
    return this.repository.listUpcoming();
  }

  async listOverdue(): Promise<Reminder[]> {
    return this.repository.listOverdue();
  }

  // -----------------------------------------------------------------------
  // Status transitions
  // -----------------------------------------------------------------------

  async complete(id: string): Promise<Reminder> {
    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Reminder not found.',
      });
    }

    await this.cancelNotification(existing);

    if (existing.recurrence !== 'none') {
      // Recurring: schedule next occurrence
      const nextDate = nextOccurrence(existing.scheduledDate, existing.recurrence);
      if (nextDate) {
        const updated = await this.repository.update(id, {
          scheduledDate: nextDate,
          status: 'active',
        });
        if (updated && updated.notificationEnabled) {
          await this.scheduleNotification(updated);
        }
        return updated ?? existing;
      }
    }

    const updated = await this.repository.update(id, { status: 'completed' });
    return updated ?? existing;
  }

  async dismiss(id: string): Promise<Reminder> {
    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Reminder not found.',
      });
    }

    await this.cancelNotification(existing);
    const updated = await this.repository.update(id, { status: 'dismissed' });
    return updated ?? existing;
  }

  // -----------------------------------------------------------------------
  // Missed reminder handling
  // -----------------------------------------------------------------------

  async reconcileOverdue(): Promise<number> {
    const overdue = await this.repository.listOverdue();
    let count = 0;

    for (const reminder of overdue) {
      if (reminder.recurrence !== 'none') {
        // Move to next occurrence
        const nextDate = nextOccurrence(reminder.scheduledDate, reminder.recurrence);
        if (nextDate) {
          await this.repository.update(reminder.id, { scheduledDate: nextDate });
          if (reminder.notificationEnabled) {
            const updated = await this.repository.getById(reminder.id);
            if (updated) await this.scheduleNotification(updated);
          }
          count++;
          continue;
        }
      }

      // One-time: mark as missed
      await this.repository.update(reminder.id, { status: 'missed' });
      await this.cancelNotification(reminder);
      count++;
    }

    if (count > 0) log.info('Reconciled overdue reminders.', { count });
    return count;
  }

  // -----------------------------------------------------------------------
  // Notification coordination
  // -----------------------------------------------------------------------

  private async scheduleNotification(reminder: Reminder): Promise<void> {
    if (!this.notificationService || !reminder.notificationOwnerRef) return;

    try {
      const fireAt = this.computeFireAt(reminder.scheduledDate, reminder.scheduledTime);
      if (!fireAt || fireAt.getTime() < Date.now()) return; // Past or invalid

      await this.notificationService.schedule({
        ownerRef: reminder.notificationOwnerRef,
        channelId: 'reminders',
        title: reminder.title,
        body: reminder.description ?? reminder.title,
        fireAt,
        exact: false,
      });
    } catch (cause) {
      log.warn('Failed to schedule notification for reminder.', { reminderId: reminder.id, cause });
    }
  }

  private async cancelNotification(reminder: Reminder): Promise<void> {
    if (!this.notificationService || !reminder.notificationOwnerRef) return;

    try {
      await this.notificationService.cancelByOwner(reminder.notificationOwnerRef);
    } catch (cause) {
      log.warn('Failed to cancel notification for reminder.', { reminderId: reminder.id, cause });
    }
  }

  /**
   * Combines a local date + time into a UTC Date for notification scheduling.
   * Uses the device's local timezone offset.
   */
  private computeFireAt(dateStr: string, timeStr: string): Date | null {
    const [year, month, day] = dateStr.split('-').map(Number);
    const [hours, minutes] = timeStr.split(':').map(Number);
    if (!year || !month || !day || hours === undefined || minutes === undefined) return null;

    // Create in local timezone
    const local = new Date(year, month - 1, day, hours, minutes, 0, 0);
    return local;
  }

  // -----------------------------------------------------------------------
  // Validation
  // -----------------------------------------------------------------------

  private validateInput(input: CreateReminderInput): void {
    this.validateTitle(input.title);
    this.validateDate(input.scheduledDate);
    this.validateTime(input.scheduledTime);
  }

  private validateTitle(title: string): void {
    if (!title.trim()) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Reminder title is required.',
      });
    }
    if (title.trim().length > 200) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Reminder title is too long (max 200 characters).',
      });
    }
  }

  private validateDate(date: string): void {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Invalid date format. Use YYYY-MM-DD.',
      });
    }
  }

  private validateTime(time: string): void {
    if (!/^\d{2}:\d{2}$/.test(time)) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Invalid time format. Use HH:MM.',
      });
    }
  }
}
