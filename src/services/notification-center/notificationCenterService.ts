/**
 * Notification Center service (Phase 18).
 *
 * Manages the Notification Center — the local notification history.
 * This is distinct from the OS-level NotificationService (Phase 3) which
 * handles scheduling/cancellation of platform notifications.
 *
 * The Notification Center provides:
 * - History of notifications
 * - Read/unread state
 * - Navigation to originating content
 * - Integration with Reminder notifications
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import type { Row } from '../../data/serialization/entitySerializer.ts';
import { NotificationCenterRepository } from '../../repositories/notificationCenterRepository.ts';
import type { NewNotificationCenterEntry } from '../../data/notification/notificationCenterTypes.ts';

export interface NotificationNavigationTarget {
  /** The route to navigate to. */
  route: string;
  /** Whether the originating content still exists. */
  exists: boolean;
}

export class NotificationCenterService {
  private readonly repository: NotificationCenterRepository;

  constructor(db: DatabaseAdapter) {
    this.repository = new NotificationCenterRepository(db);
  }

  /** Records a new notification in the center. */
  async record(data: NewNotificationCenterEntry) {
    return this.repository.create(data);
  }

  /** Gets a notification by ID. */
  async getById(id: string) {
    return this.repository.getById(id);
  }

  /** Lists all notifications, newest first. */
  async list() {
    return this.repository.list();
  }

  /** Lists unread notifications. */
  async listUnread() {
    return this.repository.listUnread();
  }

  /** Total notification count. */
  async count() {
    return this.repository.count();
  }

  /** Unread notification count. */
  async countUnread() {
    return this.repository.countUnread();
  }

  /** Marks a single notification as read. */
  async markAsRead(id: string) {
    return this.repository.markAsRead(id);
  }

  /** Marks all notifications as read. */
  async markAllAsRead() {
    return this.repository.markAllAsRead();
  }

  /** Deletes a notification. */
  async delete(id: string) {
    return this.repository.delete(id);
  }

  /** Clears all notifications. */
  async clearAll() {
    return this.repository.clearAll();
  }

  /**
   * Resolves the navigation target for a notification.
   * Checks whether the originating content still exists in its feature repository.
   */
  async resolveNavigation(notificationId: string, db: DatabaseAdapter): Promise<NotificationNavigationTarget | null> {
    const entry = await this.repository.getById(notificationId);
    if (!entry) return null;
    return this.resolveNavigationForEntry(entry, db);
  }

  /** Resolves navigation for a given entry. */
  async resolveNavigationForEntry(
    entry: { originFeature: string; originId: string | null },
    db: DatabaseAdapter,
  ): Promise<NotificationNavigationTarget> {
    if (!entry.originId) {
      return { route: '', exists: false };
    }

    switch (entry.originFeature) {
      case 'reminder': {
        const rows = await db.query<Row>(
          `SELECT id FROM reminders WHERE id = ? AND deleted_at IS NULL`,
          [entry.originId],
        );
        const exists = rows.length > 0;
        return { route: `/app/reminders/${entry.originId}`, exists };
      }
      case 'memory': {
        const rows = await db.query<Row>(
          `SELECT id FROM memories WHERE id = ? AND deleted_at IS NULL`,
          [entry.originId],
        );
        const exists = rows.length > 0;
        return { route: `/app/memories/${entry.originId}`, exists };
      }
      case 'timeline': {
        const rows = await db.query<Row>(
          `SELECT id FROM timeline_events WHERE id = ? AND deleted_at IS NULL`,
          [entry.originId],
        );
        const exists = rows.length > 0;
        return { route: `/app/timeline/${entry.originId}`, exists };
      }
      case 'note': {
        const rows = await db.query<Row>(
          `SELECT id FROM notes WHERE id = ? AND deleted_at IS NULL`,
          [entry.originId],
        );
        const exists = rows.length > 0;
        return { route: `/app/notes/${entry.originId}`, exists };
      }
      default:
        return { route: '', exists: false };
    }
  }
}
