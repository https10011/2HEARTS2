/**
 * NotificationCenter screen (Phase 18).
 *
 * Displays local notification history with read/unread state
 * and navigation to originating content.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Screen } from '../../components/Screen.tsx';
import { Header } from '../../components/Header.tsx';
import { IconButton } from '../../components/IconButton.tsx';
import { IconCheck, useToast } from '../../components/index.ts';
import { EmptyState } from '../../components/EmptyState.tsx';
import type { NotificationCenterEntry } from '../../data/notification/notificationCenterTypes.ts';

interface NotificationCenterRepo {
  list(): Promise<NotificationCenterEntry[]>;
  markAsRead(id: string): Promise<boolean>;
  markAllAsRead(): Promise<number>;
  clearAll(): Promise<number>;
}

interface NotificationCenterProps {
  repo?: NotificationCenterRepo | null;
}

const KIND_LABELS: Record<string, string> = {
  reminder: 'Reminder',
  anniversary: 'Anniversary',
  system: 'System',
};

/** Simple feature-to-route mapping for navigation targets. */
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
    toast.info('All marked as read');
  }, [repo, toast]);

  const handleClearAll = useCallback(async () => {
    if (repo) await repo.clearAll();
    setEntries([]);
    toast.success('Notifications cleared');
  }, [repo, toast]);

  return (
    <Screen>
      <Header
        title="Notifications"
        left={
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            ←
          </IconButton>
        }
        right={
          entries.some((e) => !e.read) ? (
            <IconButton label="Mark all as read" onClick={() => void handleMarkAllRead()}>
              <IconCheck size={18} />
            </IconButton>
          ) : undefined
        }
      />

      <div style={{ padding: '16px' }}>
        {loading && (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--th-color-text-secondary)' }}>
            Loading notifications...
          </div>
        )}

        {!loading && entries.length === 0 && (
          <EmptyState
            title="No notifications"
            description="You're all caught up!"
          />
        )}

        {!loading && entries.length > 0 && (
          <>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '16px',
            }}>
              <span style={{
                fontSize: '13px',
                color: 'var(--th-color-text-secondary)',
              }}>
                {entries.filter((e) => !e.read).length} unread
              </span>
              <button
                onClick={() => void handleClearAll()}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--th-color-error)',
                  fontSize: '13px',
                  cursor: 'pointer',
                  padding: '4px 8px',
                }}
              >
                Clear all
              </button>
            </div>

            <div className="th-hub-grid--enhanced">
              {entries.map((entry) => (
                <button
                  key={entry.id}
                  onClick={() => void handleEntryPress(entry)}
                  className="th-feature-card th-feature-card--enhanced th-stagger-item"
                  style={{
                    alignItems: 'flex-start',
                    textAlign: 'left',
                    background: entry.read ? undefined : 'var(--th-color-blush)',
                  }}
                >
                  <span style={{ fontSize: '20px', marginTop: '2px' }}>
                    {!entry.read ? '●' : '○'}
                  </span>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      marginBottom: '4px',
                    }}>
                      <span style={{
                        fontSize: '11px',
                        color: 'var(--th-color-burgundy)',
                        fontWeight: 500,
                      }}>
                        {KIND_LABELS[entry.kind] ?? entry.kind}
                      </span>
                      <span style={{
                        fontSize: '11px',
                        color: 'var(--th-color-text-secondary)',
                      }}>
                        {formatTimeAgo(entry.createdAt)}
                      </span>
                    </div>
                    <div style={{
                      fontWeight: entry.read ? 400 : 600,
                      fontSize: '14px',
                      color: 'var(--th-color-text-primary)',
                    }}>
                      {entry.title}
                    </div>
                    {entry.body && (
                      <div style={{
                        fontSize: '12px',
                        color: 'var(--th-color-text-secondary)',
                        marginTop: '4px',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                      }}>
                        {entry.body}
                      </div>
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

function formatTimeAgo(isoDate: string): string {
  const now = Date.now();
  const then = new Date(isoDate).getTime();
  const diffMs = now - then;
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return 'Just now';
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffHr = Math.floor(diffMin / 60);
  if (diffHr < 24) return `${diffHr}h ago`;
  const diffDay = Math.floor(diffHr / 24);
  if (diffDay < 7) return `${diffDay}d ago`;
  return new Date(isoDate).toLocaleDateString();
}
