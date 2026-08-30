/**
 * Local notification foundation (Phase 3).
 *
 * LOCAL ONLY — @capacitor/local-notifications schedules on the device; no
 * FCM/remote push exists anywhere in V1.
 *
 * Boundary:
 *   feature → NotificationService (this) → NotificationDriver → plugin
 * Platform specifics stay inside the driver. The service owns:
 *   - channel definitions (immutable once created on Android),
 *   - owner-ref ↔ platform-id mapping via NotificationRegistryRepository,
 *   - duplicate prevention (owner_ref unique → upsert semantics),
 *   - schedule / cancel-by-id / cancel-by-owner / cancel-all,
 *   - reconciliation between the DB registry and the OS pending list.
 *
 * Android constraints callers must respect (documented for future features):
 * - exact alarms (schedule.exact semantics) need the OS-level exact-alarm
 *   permission on API 31+; the service requests inexact scheduling unless a
 *   feature explicitly demands exactness,
 * - channel importance/vibration/sound are fixed at channel creation time,
 * - pending-notification count is bounded by the OS; features must cancel,
 * - scheduled notifications are lost across app reinstall/restore flows.
 */

import type { Channel } from '@capacitor/local-notifications';
import { AppError, normalizeAppError } from '../errors/appError.ts';
import { createLogger } from '../logging/logger.ts';
import type { Clock } from '../../utils/time.ts';
import { systemClock } from '../../utils/time.ts';
import { NotificationRegistryRepository, type NotificationRegistryEntry } from './notificationRegistryRepository.ts';

const log = createLogger('notifications');

export interface NotificationChannelSpec {
  id: string;
  name: string;
  description: string;
  importance: 'default' | 'high' | 'low';
}

/** V1 channel set — extend here ONLY (channels are immutable on Android). */
export const NOTIFICATION_CHANNELS: NotificationChannelSpec[] = [
  { id: 'reminders', name: 'Reminders', description: 'Reminders and scheduled notes.', importance: 'high' },
  { id: 'anniversaries', name: 'Anniversaries', description: 'Relationship anniversaries and dates.', importance: 'default' },
  { id: 'general', name: 'General', description: 'General TwoHearts notifications.', importance: 'default' },
];

export interface ScheduleRequest {
  /** Logical owner reference; one pending notification per owner. */
  ownerRef: string;
  channelId: string;
  title: string;
  body: string;
  /** Local wall-clock moment to fire. */
  fireAt: Date;
  metadata?: Record<string, unknown>;
  /** When true, schedule with OS exactness (subject to API 31+ permission). */
  exact?: boolean;
}

/** The only platform surface the service depends on. */
export interface NotificationDriver {
  ensureChannels(specs: NotificationChannelSpec[]): Promise<void>;
  schedule(options: {
    id: number;
    channelId: string;
    title: string;
    body: string;
    fireAt: Date;
    exact: boolean;
  }): Promise<void>;
  cancel(ids: number[]): Promise<void>;
  cancelAll(): Promise<void>;
  pendingIds(): Promise<number[]>;
}

export interface ScheduledHandle {
  notificationId: number;
  ownerRef: string;
}

export class NotificationService {
  constructor(
    private readonly driver: NotificationDriver,
    private readonly registry: NotificationRegistryRepository,
    private readonly clock: Clock = systemClock,
  ) {}

  /** Creates the V1 channels; idempotent (channel re-creation is a no-op). */
  async initialize(): Promise<void> {
    try {
      await this.driver.ensureChannels(NOTIFICATION_CHANNELS);
    } catch (cause) {
      throw normalizeAppError(cause, 'notification', 'channel-init-failed', { recoverable: true });
    }
  }

  /** Schedules (or reschedules, by owner) a local notification. */
  async schedule(request: ScheduleRequest): Promise<ScheduledHandle> {
    this.assertChannel(request.channelId);
    if (!(request.fireAt instanceof Date) || Number.isNaN(request.fireAt.getTime())) {
      throw new AppError('notification', 'invalid-schedule', {
        recoverable: false,
        userMessage: 'The notification time is invalid.',
      });
    }
    try {
      const existing = await this.registry.getByOwner(request.ownerRef);
      if (existing) {
        await this.driver.cancel([existing.notificationId]);
      }
      const notificationId = await this.registry.nextNotificationId();
      await this.driver.schedule({
        id: notificationId,
        channelId: request.channelId,
        title: request.title,
        body: request.body,
        fireAt: request.fireAt,
        exact: request.exact ?? false,
      });
      const now = this.clock().toISOString();
      await this.registry.upsert({
        notificationId,
        ownerRef: request.ownerRef,
        channelId: request.channelId,
        title: request.title,
        body: request.body,
        scheduledAt: request.fireAt.toISOString(),
        metadata: request.metadata ?? {},
        createdAt: existing?.createdAt ?? now,
        updatedAt: now,
      });
      return { notificationId, ownerRef: request.ownerRef };
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'notification', 'schedule-failed', { recoverable: true });
    }
  }

  async update(request: ScheduleRequest): Promise<ScheduledHandle> {
    return this.schedule(request);
  }

  async cancelById(notificationId: number): Promise<void> {
    try {
      await this.driver.cancel([notificationId]);
      await this.registry.deleteById(notificationId);
    } catch (cause) {
      throw normalizeAppError(cause, 'notification', 'cancel-failed', { recoverable: true });
    }
  }

  async cancelByOwner(ownerRef: string): Promise<void> {
    const existing = await this.registry.getByOwner(ownerRef);
    if (!existing) return; // idempotent: nothing scheduled for this owner
    await this.cancelById(existing.notificationId);
  }

  async cancelAll(): Promise<void> {
    try {
      await this.driver.cancelAll();
      const entries = await this.registry.list();
      for (const entry of entries) {
        await this.registry.deleteById(entry.notificationId);
      }
    } catch (cause) {
      throw normalizeAppError(cause, 'notification', 'cancel-failed', { recoverable: true });
    }
  }

  /** Registry listing (feature queries; UI gets handles, not platform ids). */
  async list(): Promise<NotificationRegistryEntry[]> {
    return this.registry.list();
  }

  /**
   * Drops registry rows the OS no longer has pending (delivered, canceled by
   * system, cleared after reboot without reschedule). Called on app resume
   * by the bootstrap/lifecycle hook; cheap and idempotent.
   */
  async reconcile(): Promise<number> {
    try {
      const pending = new Set(await this.driver.pendingIds());
      const entries = await this.registry.list();
      let removed = 0;
      for (const entry of entries) {
        if (!pending.has(entry.notificationId)) {
          await this.registry.deleteById(entry.notificationId);
          removed += 1;
        }
      }
      if (removed > 0) log.info('Pruned stale notification registry entries.', { removed });
      return removed;
    } catch (cause) {
      log.warn('Notification reconciliation failed.', { cause });
      return 0; // never block app usage on reconciliation
    }
  }

  private assertChannel(channelId: string): void {
    if (!NOTIFICATION_CHANNELS.some((c) => c.id === channelId)) {
      throw new AppError('notification', 'unknown-channel', {
        recoverable: false,
        userMessage: 'The notification channel is unknown.',
      });
    }
  }
}

/** Importance mapping kept in one place (plugin enum leakage guard). */
export function channelImportance(spec: NotificationChannelSpec): Channel['importance'] {
  switch (spec.importance) {
    case 'high':
      return 4; // ChannelImportance.High
    case 'low':
      return 2; // ChannelImportance.Low
    default:
      return 3; // ChannelImportance.Default
  }
}
