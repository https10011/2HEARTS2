/**
 * NotificationCenter (Stage 14 — Search + Notification Center Visual Productization).
 *
 * Displays local notification history with polished unread/read states,
 * timestamps, kind badges, and warm empty state. Architecture unchanged.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Screen } from '../../components/Screen.tsx';
import { Header } from '../../components/Header.tsx';
import { IconButton } from '../../components/IconButton.tsx';
import { IconCheck, IconBell, IconHeart, IconInfo, useToast } from '../../components/index.ts';
import type { NotificationCenterEntry } from '../../data/notification/notificationCenterTypes.ts';
import {
  unreadCountText,
  notificationKindLabel,
  notificationEmptyTitle,
  notificationEmptyDescription,
  formatRelativeTime,
  markAllReadToast,
  clearAllToast,
} from './searchNotificationPresentation.ts';

interface NotificationCenterRepo {
  list(): Promise<NotificationCenterEntry[]>;
  markAsRead(id: string): Promise<boolean>;
  markAllAsRead(): Promise<number>;
  clearAll(): Promise<number>;
}

interface NotificationCenterProps {
  repo?: NotificationCenterRepo | null;
}

/** Feature-to-route mapping for navigation targets. */
function resolveRoute(entry: NotificationCenterEntry): string | null {
  if (!entry.originId) return null;
  switch (entry.originFeature) {
    case 'reminder':
      return `/app/reminders/${entry.originId}`;
    case 'memory':
      return `/app/memories/${entry.originId}`;
    case 'timeline':
      return `/app/timeline/${entry.originId}`;
    case 'note':
      return `/app/notes/${entry.originId}`;
    default:
      return null;
  }
}

/** Notification kind → icon component. */
function NotifKindIcon({ kind }: { kind: string }) {
  switch (kind) {
    case 'reminder':
      return <IconBell size={18} />;
    case 'anniversary':
      return <IconHeart size={18} />;
    default:
      return <IconInfo size={18} />;
  }
}

/** Notification kind → CSS class suffix. */
function notifIconClass(kind: string): string {
  if (kind === 'reminder') return 'th-notif-icon--reminder';
  if (kind === 'anniversary') return 'th-notif-icon--anniversary';
  return 'th-notif-icon--system';
}

export function NotificationCenter({ repo }: NotificationCenterProps = {}) {
  const toast = useToast();
  const [entries, setEntries] = useState<NotificationCenterEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const loadEntries = useCallback(async () => {
    if (!repo) {
      setLoading(false);
      return;
    }
    try {
      const items = await repo.list();
      setEntries(items);
    } catch {
      setEntries([]);
    } finally {
      setLoading(false);
    }
  }, [repo]);

  useEffect(() => {
    void loadEntries();
  }, [loadEntries]);

  const handleEntryPress = useCallback(async (entry: NotificationCenterEntry) => {
    if (repo) await repo.markAsRead(entry.id);

    const route = resolveRoute(entry);
    if (route) {
      navigate(route);
    }

    setEntries((prev) =>
      prev.map((e) => (e.id === entry.id ? { ...e, read: true } : e)),
    );
  }, [repo, navigate]);

  const handleMarkAllRead = useCallback(async () => {
    if (repo) await repo.markAllAsRead();
    setEntries((prev) => prev.map((e) => ({ ...e, read: true })));
    toast.info(markAllReadToast());
  }, [repo, toast]);

  const handleClearAll = useCallback(async () => {
    if (repo) await repo.clearAll();
    setEntries([]);
    toast.success(clearAllToast());
  }, [repo, toast]);

  const unreadCount = entries.filter((e) => !e.read).length;

  return (
    <Screen>
      <Header
        title="Notifications"
        left={
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            ←
          </IconButton>
        }
      />

      <div className="th-content-pad">
        {/* Loading */}
        {loading && (
          <div className="th-notif-loading">
            <div className="th-search-loading__spinner" />
            <span>Loading notifications...</span>
          </div>
        )}

        {/* Empty state */}
        {!loading && entries.length === 0 && (
          <div className="th-notif-empty th-game-enter">
            <div className="th-notif-empty__icon">
              <IconBell size={28} />
            </div>
            <h3 className="th-notif-empty__title">{notificationEmptyTitle()}</h3>
            <p className="th-notif-empty__desc">{notificationEmptyDescription()}</p>
          </div>
        )}

        {/* Notification list */}
        {!loading && entries.length > 0 && (
          <>
            {/* Header bar */}
            <div className="th-notif-header">
              <span className={`th-notif-header__count ${unreadCount > 0 ? 'th-notif-header__count--unread' : ''}`}>
                {unreadCountText(unreadCount)}
              </span>
              <div className="th-notif-header__actions">
                {unreadCount > 0 && (
                  <button
                    className="th-notif-header__btn th-notif-header__btn--read"
                    onClick={() => void handleMarkAllRead()}
                  >
                    <IconCheck size={14} />
                    Mark all read
                  </button>
                )}
                <button
                  className="th-notif-header__btn th-notif-header__btn--clear"
                  onClick={() => void handleClearAll()}
                >
                  Clear all
                </button>
              </div>
            </div>

            {/* Cards */}
            <div className="th-notif-list">
              {entries.map((entry, i) => (
                <button
                  key={entry.id}
                  onClick={() => void handleEntryPress(entry)}
                  className={`th-notif-card ${!entry.read ? 'th-notif-card--unread' : ''} th-game-stagger`}
                  style={{ animationDelay: `${i * 30}ms` }}
                >
                  {/* Unread dot */}
                  <div className={`th-notif-dot ${entry.read ? 'th-notif-dot--read' : ''}`} />

                  {/* Kind icon */}
                  <div className={`th-notif-icon ${notifIconClass(entry.kind)}`}>
                    <NotifKindIcon kind={entry.kind} />
                  </div>

                  {/* Body */}
                  <div className="th-notif-body">
                    <div className="th-notif-body__meta">
                      <span className="th-notif-body__kind">
                        {notificationKindLabel(entry.kind)}
                      </span>
                      <span className="th-notif-body__time">
                        {formatRelativeTime(entry.createdAt)}
                      </span>
                    </div>
                    <div className="th-notif-body__title">{entry.title}</div>
                    {entry.body && (
                      <div className="th-notif-body__desc">{entry.body}</div>
                    )}
                  </div>
                </button>
              ))}
            </div>
          </>
        )}
      </div>
    </Screen>
  );
}
