/**
 * Notification Center data types (Phase 18).
 *
 * Local notification history records for the Notification Center.
 * These track app-level notification state separate from the OS-level
 * notification_registry (which maps owner-ref to platform notification IDs).
 *
 * The notification_center table stores a display-oriented history that
 * the UI uses for the Notification Center screen.
 */

import type { Row } from '../serialization/entitySerializer.ts';

export const NOTIFICATION_CENTER_COLUMNS = [
  'id',
  'title',
  'body',
  'kind',
  'origin_feature',
  'origin_id',
  'channel_id',
  'read',
  'created_at',
  'updated_at',
] as const;

export type NotificationKind = 'reminder' | 'anniversary' | 'system';

export interface NotificationCenterEntry {
  id: string;
  title: string;
  body: string;
  kind: NotificationKind;
  /** Which feature this notification originated from. */
  originFeature: string;
  /** The entity ID in the origin feature (for navigation). */
  originId: string | null;
  channelId: string;
  read: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface NewNotificationCenterEntry {
  title: string;
  body: string;
  kind: NotificationKind;
  originFeature: string;
  originId: string | null;
  channelId: string;
}

export const notificationCenterSerializer = {
  fromRow(row: Row): NotificationCenterEntry {
    return {
      id: row['id'] as string,
      title: row['title'] as string,
      body: row['body'] as string,
      kind: row['kind'] as NotificationKind,
      originFeature: row['origin_feature'] as string,
      originId: row['origin_id'] as string | null,
      channelId: row['channel_id'] as string,
      read: (row['read'] as number) === 1,
      createdAt: row['created_at'] as string,
      updatedAt: row['updated_at'] as string,
    };
  },

  toParams(entry: NotificationCenterEntry): (string | number | null)[] {
    return [
      entry.id,
      entry.title,
      entry.body,
      entry.kind,
      entry.originFeature,
      entry.originId,
      entry.channelId,
      entry.read ? 1 : 0,
      entry.createdAt,
      entry.updatedAt,
    ];
  },
};
