/**
 * RemindersHome (Phase 13).
 *
 * Lists all reminders with upcoming/overdue sections.
 * Connects to real persisted data via ReminderRepository.
 */

import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconBack, IconChevronRight, IconBell } from '../../components/index.ts';
import type { Reminder } from '../../data/reminder/reminderTypes.ts';
import { formatReminderDateTime } from '../../data/reminder/reminderTypes.ts';
import { useReminderService } from './useReminderService.ts';

export function RemindersHome() {
  const navigate = useNavigate();
  const service = useReminderService();
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [loading, setLoading] = useState(true);

  const loadReminders = useCallback(async () => {
    if (!service) return;
    try {
      const list = await service.list();
      setReminders(list);
    } catch {
      // Graceful degradation — show empty state
    } finally {
      setLoading(false);
    }
  }, [service]);

  useEffect(() => {
    loadReminders();
  }, [loadReminders]);

  const activeReminders = reminders.filter((r) => r.status === 'active');
  const completedReminders = reminders.filter((r) => r.status === 'completed' || r.status === 'dismissed' || r.status === 'missed');

  if (loading) {
    return (
      <div className="th-content-pad">
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
          <button
            className="th-btn th-btn--ghost"
            onClick={() => navigate(RoutePath.appUs)}
            style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
          >
            <IconBack size={20} />
          </button>
          <h1 className="th-screen-title" style={{ flex: 1 }}>Reminders</h1>
        </div>
        <div className="th-loading-state">Loading reminders...</div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <button
          className="th-btn th-btn--ghost"
          onClick={() => navigate(RoutePath.appUs)}
          style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
        >
          <IconBack size={20} />
        </button>
        <h1 className="th-screen-title" style={{ flex: 1 }}>Reminders</h1>
        <Link
          to={RoutePath.appRemindersAdd}
          className="th-btn th-btn--primary"
          style={{ fontSize: 'var(--th-font-size-sm)', padding: 'var(--th-space-2) var(--th-space-3)' }}
        >
          + New
        </Link>
      </div>

      {/* Empty state */}
      {activeReminders.length === 0 && completedReminders.length === 0 && (
        <div className="th-empty-emotional">
          <div className="th-empty-emotional__visual th-scale-in">
            <IconBell size={36} />
          </div>
          <h3 className="th-empty-emotional__title">No reminders yet</h3>
          <p className="th-empty-emotional__message">
            Create a reminder to never miss an important moment
          </p>
          <div className="th-empty-emotional__action">
            <Link to={RoutePath.appRemindersAdd} className="th-btn th-btn--primary">
              Create Reminder
            </Link>
          </div>
        </div>
      )}

      {/* Active reminders */}
      {activeReminders.length > 0 && (
        <div style={{ marginBottom: 'var(--th-space-6)' }}>
          <h2 style={{
            fontSize: 'var(--th-font-size-sm)',
            fontWeight: 'var(--th-font-weight-semibold)',
            color: 'var(--th-color-text-secondary)',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            marginBottom: 'var(--th-space-3)',
          }}>
            Upcoming
          </h2>
          <div className="th-hub-grid">
            {activeReminders.map((reminder) => (
              <Link
                key={reminder.id}
                to={`${RoutePath.appRemindersDetail.replace(':reminderId', reminder.id)}`}
                className="th-feature-card th-reminder-card--enhanced th-stagger-item"
              >
                <div className="th-feature-card__body">
                  <div className="th-feature-card__title">{reminder.title}</div>
                  <div className="th-feature-card__desc">
                    {formatReminderDateTime(reminder.scheduledDate, reminder.scheduledTime)}
                    {reminder.recurrence !== 'none' && (
                      <span style={{ marginLeft: 'var(--th-space-2)', opacity: 0.7 }}>
                        · {reminder.recurrence}
                      </span>
                    )}
                  </div>
                  {reminder.description && (
                    <div className="th-feature-card__desc" style={{ marginTop: 'var(--th-space-1)' }}>
                      {reminder.description.length > 60
                        ? reminder.description.slice(0, 60) + '...'
                        : reminder.description}
                    </div>
                  )}
                </div>
                <IconChevronRight size={18} className="th-feature-card__chevron" />
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Completed/dismissed/missed reminders */}
      {completedReminders.length > 0 && (
        <div>
          <h2 style={{
            fontSize: 'var(--th-font-size-sm)',
            fontWeight: 'var(--th-font-weight-semibold)',
            color: 'var(--th-color-text-secondary)',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            marginBottom: 'var(--th-space-3)',
          }}>
            History
          </h2>
          <div className="th-hub-grid">
            {completedReminders.map((reminder) => (
              <div
                key={reminder.id}
                className="th-feature-card"
                style={{ opacity: 0.6, cursor: 'default' }}
              >
                <div className="th-feature-card__body">
                  <div className="th-feature-card__title" style={{ textDecoration: 'line-through' }}>
                    {reminder.title}
                  </div>
                  <div className="th-feature-card__desc">
                    {reminder.status === 'completed' ? 'Completed' :
                     reminder.status === 'missed' ? 'Missed' : 'Dismissed'}
                    {' · '}
                    {formatReminderDateTime(reminder.scheduledDate, reminder.scheduledTime)}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
