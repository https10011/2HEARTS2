/**
 * ReminderDetail (Stage 8).
 *
 * One reminder as a finished product page: centered bell identity, a
 * date-forward schedule card (relative day + large time), repeat and
 * notification cards, the note, and deliberate actions — primary Edit,
 * quiet "Mark as done", and a centralized-Modal delete confirmation.
 * Status changes and notification rescheduling flow through
 * ReminderService unchanged.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  IconBack,
  IconBell,
  IconBellOff,
  IconCalendar,
  IconCheck,
  IconTrash,
  IconRepeat,
  IconButton,
  LoadingState,
  Modal,
  Switch,
  useToast,
  RoseLilyDecoration,
} from '../../components/index.ts';
import type { Reminder } from '../../data/reminder/reminderTypes.ts';
import { useReminderService } from './useReminderService.ts';
import {
  formatReminderDate,
  formatReminderTime,
  historyStatusLabel,
  isHistoryStatus,
  relativeDayLabel,
  RECURRENCE_LABELS,
  STATUS_LABELS,
} from './reminderSchedule.ts';

export function ReminderDetail() {
  const navigate = useNavigate();
  const { reminderId } = useParams<{ reminderId: string }>();
  const service = useReminderService();
  const toast = useToast();
  const [reminder, setReminder] = useState<Reminder | null>(null);
  const [loading, setLoading] = useState(true);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);

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
    setDeleting(true);
    try {
      await service.delete(reminderId);
      toast.success('Reminder deleted');
      navigate(RoutePath.appReminders);
    } catch {
      setDeleting(false);
      setConfirmDelete(false);
      toast.error('Could not delete reminder');
    }
  }, [service, reminderId, navigate, toast]);

  const handleToggleNotification = useCallback(async (next: boolean) => {
    if (!service || !reminder) return;
    try {
      const updated = await service.update(reminder.id, { notificationEnabled: next });
      setReminder(updated);
      toast.success(next ? 'Notification on' : 'Notification off');
    } catch {
      toast.error('Could not update notification');
    }
  }, [service, reminder, toast]);

  const handleComplete = useCallback(async () => {
    if (!service || !reminder) return;
    try {
      const updated = await service.complete(reminder.id);
      setReminder(updated);
      toast.success('Marked as done');
    } catch {
      toast.error('Could not update reminder');
    }
  }, [service, reminder, toast]);

  if (loading) {
    return <LoadingState label="Opening reminder…" />;
  }

  if (!reminder) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-emotional">
          <div className="th-empty-emotional__visual">
            <IconBell size={42} />
          </div>
          <h3 className="th-empty-emotional__title">Reminder not found</h3>
          <p className="th-empty-emotional__message">
            It may have been removed already.
          </p>
          <div className="th-empty-emotional__action">
            <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appReminders)}>
              Back to Reminders
            </button>
          </div>
        </div>
      </div>
    );
  }

  const isHistory = isHistoryStatus(reminder.status);

  return (
    <div className="th-content-pad th-screen-warm">
      <RoseLilyDecoration variant={11} size={110} position="bottom-right" opacity={0.1} />

      {/* Branded header */}
      <header className="th-rem-header">
        <IconButton label="Go back" onClick={() => navigate(RoutePath.appReminders)}>
          <IconBack />
        </IconButton>
        <div className="th-rem-header__copy">
          <h1 className="th-rem-title">Reminder</h1>
          <p className="th-rem-subtitle">{STATUS_LABELS[reminder.status]}</p>
        </div>
        <IconButton label="Delete reminder" onClick={() => setConfirmDelete(true)}>
          <IconTrash />
        </IconButton>
      </header>

      {/* Identity */}
      <div className="th-rem-detail__identity th-stagger-item">
        <span className="th-rem-detail__bell" aria-hidden="true">
          <IconBell size={26} />
        </span>
        <h2 className="th-rem-detail__title">{reminder.title}</h2>
      </div>

      {/* Schedule card — date-forward */}
      <section className="th-rem-detail__schedule th-stagger-item" aria-label="Schedule">
        <div className="th-rem-detail__day">{relativeDayLabel(reminder.scheduledDate)}</div>
        <div className="th-rem-detail__time">{formatReminderTime(reminder.scheduledTime)}</div>
        <div className="th-rem-detail__date">
          <IconCalendar size={15} />
          <span>{formatReminderDate(reminder.scheduledDate)}</span>
        </div>
      </section>

      {/* Repeat */}
      <section className="th-rem-detail__row th-stagger-item" aria-label="Repeat">
        <span className="th-rem-detail__row-icon" aria-hidden="true">
          <IconRepeat size={18} />
        </span>
        <span className="th-rem-detail__row-label">Repeat</span>
        <span className="th-rem-detail__row-value">{RECURRENCE_LABELS[reminder.recurrence]}</span>
      </section>

      {/* Notification */}
      <section className="th-rem-detail__row th-rem-detail__row--stacked th-stagger-item" aria-label="Notification">
        <div className="th-rem-detail__row-top">
          <span className="th-rem-detail__row-icon" aria-hidden="true">
            {reminder.notificationEnabled ? <IconBell size={18} /> : <IconBellOff size={18} />}
          </span>
          <span className="th-rem-detail__row-label">Notification</span>
          <Switch
            checked={reminder.notificationEnabled}
            onChange={handleToggleNotification}
            label="Notification for this reminder"
          />
        </div>
        <p className="th-rem-detail__row-sub">
          {reminder.notificationEnabled
            ? 'Your phone will gently nudge you when it’s time.'
            : 'Quiet — it stays on your list without a nudge.'}
        </p>
      </section>

      {/* Note */}
      {reminder.description && (
        <section className="th-rem-detail__note th-stagger-item" aria-label="Note">
          <div className="th-rem-detail__note-label">Note</div>
          <p className="th-rem-detail__note-text">{reminder.description}</p>
        </section>
      )}

      {/* Status line */}
      <p className="th-rem-detail__status">
        {isHistory ? historyStatusLabel(reminder.status) : STATUS_LABELS[reminder.status]}
      </p>

      {/* Actions */}
      <div className="th-rem-detail__actions">
        <button
          className="th-btn th-btn--primary th-btn--full"
          onClick={() => navigate(RoutePath.appRemindersEdit.replace(':reminderId', reminderId ?? ''))}
        >
          Edit Reminder
        </button>
        {reminder.status === 'active' && (
          <button
            className="th-btn th-btn--secondary th-btn--full"
            onClick={handleComplete}
          >
            <IconCheck size={18} /> Mark as done
          </button>
        )}
        <button
          className="th-btn th-btn--ghost th-btn--full th-rem-detail__delete"
          onClick={() => setConfirmDelete(true)}
        >
          <IconTrash size={16} /> Delete this reminder
        </button>
      </div>

      {/* Delete confirmation — centralized Modal bottom sheet */}
      <Modal open={confirmDelete} onClose={() => !deleting && setConfirmDelete(false)} label="Delete reminder">
        <div className="th-rem-delete-sheet">
          <h3 className="th-date-picker__title">Delete this reminder?</h3>
          <p className="th-rem-delete-sheet__text">
            “{reminder.title}” will be removed for good.
          </p>
          <div className="th-rem-delete-sheet__actions">
            <button
              className="th-btn th-btn--danger th-btn--full"
              onClick={handleDelete}
              disabled={deleting}
            >
              {deleting ? 'Deleting…' : 'Delete reminder'}
            </button>
            <button
              className="th-btn th-btn--ghost th-btn--full"
              onClick={() => setConfirmDelete(false)}
              disabled={deleting}
            >
              Keep it
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
