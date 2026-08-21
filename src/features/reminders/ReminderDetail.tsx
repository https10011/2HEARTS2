/**
 * ReminderDetail (Phase 13).
 *
 * Shows complete reminder information with edit, delete, and status actions.
 * Connects to real persisted data.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconBack } from '../../components/index.ts';
import type { Reminder } from '../../data/reminder/reminderTypes.ts';
import { formatReminderDateTime } from '../../data/reminder/reminderTypes.ts';
import { useReminderService } from './useReminderService.ts';

const STATUS_LABELS: Record<string, string> = {
  active: 'Active',
  completed: 'Completed',
  dismissed: 'Dismissed',
  missed: 'Missed',
};

const RECURRENCE_LABELS: Record<string, string> = {
  none: 'One-time',
  daily: 'Daily',
  weekly: 'Weekly',
  monthly: 'Monthly',
  yearly: 'Yearly',
};

export function ReminderDetail() {
  const navigate = useNavigate();
  const { reminderId } = useParams<{ reminderId: string }>();
  const service = useReminderService();
  const [reminder, setReminder] = useState<Reminder | null>(null);
  const [loading, setLoading] = useState(true);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  useEffect(() => {
    if (!service || !reminderId) return;
    let cancelled = false;
    service.getById(reminderId)
      .then((found) => {
        if (!cancelled) {
          setReminder(found);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [service, reminderId]);

  const handleDelete = useCallback(async () => {
    if (!service || !reminderId) return;
    try {
      await service.delete(reminderId);
    } catch {
      // Already gone or unavailable — either way, leave the detail view.
    }
    navigate(RoutePath.appReminders);
  }, [service, reminderId, navigate]);

  const handleToggleNotification = useCallback(async () => {
    if (!service || !reminder) return;
    try {
      const updated = await service.update(reminder.id, {
        notificationEnabled: !reminder.notificationEnabled,
      });
      setReminder(updated);
    } catch {
      // Keep current state on failure.
    }
  }, [service, reminder]);

  const handleComplete = useCallback(async () => {
    if (!service || !reminder) return;
    try {
      const updated = await service.complete(reminder.id);
      setReminder(updated);
    } catch {
      // Keep current state on failure.
    }
  }, [service, reminder]);

  if (loading) {
    return (
      <div className="th-content-pad">
        <div className="th-loading-state">Loading reminder...</div>
      </div>
    );
  }

  if (!reminder) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state">
          <h3>Reminder not found</h3>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appReminders)}>
            Back to Reminders
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 'var(--th-space-6)' }}>
        <button
          className="th-btn th-btn--ghost"
          onClick={() => navigate(RoutePath.appReminders)}
          style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
        >
          <IconBack size={20} />
        </button>
        <h1 className="th-screen-title" style={{ flex: 1 }}>{reminder.title}</h1>
      </div>

      {/* Reminder info card */}
      <div className="th-relationship-card" style={{ marginBottom: 'var(--th-space-6)' }}>
        <div style={{ marginBottom: 'var(--th-space-4)' }}>
          <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 'var(--th-space-1)' }}>
            Scheduled
          </div>
          <div style={{ fontSize: 'var(--th-font-size-lg)', fontWeight: 'var(--th-font-weight-semibold)' }}>
            {formatReminderDateTime(reminder.scheduledDate, reminder.scheduledTime)}
          </div>
        </div>

        {reminder.description && (
          <div style={{ marginBottom: 'var(--th-space-4)' }}>
            <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 'var(--th-space-1)' }}>
              Notes
            </div>
            <div style={{ fontSize: 'var(--th-font-size-md)' }}>{reminder.description}</div>
          </div>
        )}

        <div style={{ display: 'flex', gap: 'var(--th-space-6)', flexWrap: 'wrap' }}>
          <div>
            <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 'var(--th-space-1)' }}>
              Status
            </div>
            <div style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)' }}>
              {STATUS_LABELS[reminder.status] ?? reminder.status}
            </div>
          </div>
          <div>
            <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 'var(--th-space-1)' }}>
              Repeat
            </div>
            <div style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)' }}>
              {RECURRENCE_LABELS[reminder.recurrence] ?? reminder.recurrence}
            </div>
          </div>
          <div>
            <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 'var(--th-space-1)' }}>
              Notification
            </div>
            <div style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)' }}>
              {reminder.notificationEnabled ? 'Enabled' : 'Disabled'}
            </div>
          </div>
        </div>
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
        {/* Toggle notification */}
        <button
          className="th-btn th-btn--secondary th-btn--full"
          onClick={handleToggleNotification}
        >
          {reminder.notificationEnabled ? 'Disable Notification' : 'Enable Notification'}
        </button>

        {/* Mark complete (only for active) */}
        {reminder.status === 'active' && (
          <button
            className="th-btn th-btn--primary th-btn--full"
            onClick={handleComplete}
          >
            Mark Complete
          </button>
        )}

        {/* Edit */}
        <button
          className="th-btn th-btn--secondary th-btn--full"
          onClick={() => navigate(RoutePath.appRemindersEdit.replace(':reminderId', reminderId ?? ''))}
        >
          Edit Reminder
        </button>

        {/* Delete */}
        {!showDeleteConfirm ? (
          <button
            className="th-btn th-btn--secondary th-btn--full"
            onClick={() => setShowDeleteConfirm(true)}
            style={{ color: 'var(--th-color-error)' }}
          >
            Delete Reminder
          </button>
        ) : (
          <div style={{ display: 'flex', gap: 'var(--th-space-3)' }}>
            <button
              className="th-btn th-btn--secondary"
              onClick={() => setShowDeleteConfirm(false)}
              style={{ flex: 1 }}
            >
              Cancel
            </button>
            <button
              className="th-btn th-btn--primary"
              onClick={handleDelete}
              style={{ flex: 1, background: 'var(--th-color-error)' }}
            >
              Confirm Delete
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
