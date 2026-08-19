/**
 * @capacitor/local-notifications driver (Phase 3) — Android production path.
 *
 * This file is the ONLY place that imports the plugin. All plugin-typed
 * values (importance enums, Schedule shapes) are translated here so the
 * service layer stays platform-agnostic.
 */

import { LocalNotifications } from '@capacitor/local-notifications';
import type { NotificationDriver, NotificationChannelSpec } from './notificationService.ts';
import { channelImportance } from './notificationService.ts';

export const capacitorNotificationDriver: NotificationDriver = {
  async ensureChannels(specs: NotificationChannelSpec[]): Promise<void> {
    for (const spec of specs) {
      await LocalNotifications.createChannel({
        id: spec.id,
        name: spec.name,
        description: spec.description,
        importance: channelImportance(spec),
      });
    }
  },
  async schedule(options): Promise<void> {
    await LocalNotifications.schedule({
      notifications: [
        {
          id: options.id,
          channelId: options.channelId,
          title: options.title,
          body: options.body,
          schedule: {
            at: options.fireAt,
            allowWhileIdle: options.exact,
          },
        },
      ],
    });
  },
  async cancel(ids: number[]): Promise<void> {
    if (ids.length === 0) return;
    await LocalNotifications.cancel({ notifications: ids.map((id) => ({ id })) });
  },
  async cancelAll(): Promise<void> {
    const pending = await LocalNotifications.getPending();
    if (pending.notifications.length === 0) return;
    await LocalNotifications.cancel({ notifications: pending.notifications.map((n) => ({ id: n.id })) });
  },
  async pendingIds(): Promise<number[]> {
    const pending = await LocalNotifications.getPending();
    return pending.notifications.map((n) => n.id);
  },
};
