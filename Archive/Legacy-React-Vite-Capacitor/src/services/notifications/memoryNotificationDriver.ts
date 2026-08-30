/**
 * Memory notification driver (Phase 3) — tests & web/dev fallback.
 *
 * Mirrors the platform contract exactly: schedule/cancel/pending with
 * integer ids. Never fires anything; used for unit tests and browser dev
 * (where scheduling is a documented no-op).
 */

import type { NotificationDriver, NotificationChannelSpec } from './notificationService.ts';

interface PendingNotification {
  id: number;
  channelId: string;
  title: string;
  body: string;
  fireAt: Date;
  exact: boolean;
}

export class MemoryNotificationDriver implements NotificationDriver {
  channels: NotificationChannelSpec[] = [];
  pending = new Map<number, PendingNotification>();

  async ensureChannels(specs: NotificationChannelSpec[]): Promise<void> {
    for (const spec of specs) {
      if (!this.channels.some((c) => c.id === spec.id)) this.channels.push({ ...spec });
    }
  }

  async schedule(options: PendingNotification): Promise<void> {
    this.pending.set(options.id, { ...options, fireAt: new Date(options.fireAt.getTime()) });
  }

  async cancel(ids: number[]): Promise<void> {
    for (const id of ids) this.pending.delete(id);
  }

  async cancelAll(): Promise<void> {
    this.pending.clear();
  }

  async pendingIds(): Promise<number[]> {
    return [...this.pending.keys()].sort((a, b) => a - b);
  }

  /** Test helper: simulates OS delivery/removal without service knowledge. */
  simulateDelivered(id: number): void {
    this.pending.delete(id);
  }
}
