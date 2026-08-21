/**
 * CreateReminder (Phase 13).
 *
 * Form to create or edit a reminder. Handles validation, date/time input,
 * recurrence selection, and notification toggle.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconBack, IconBell, IconBellOff, useToast } from '../../components/index.ts';
import type { ReminderRecurrence } from '../../data/reminder/reminderTypes.ts';
import { REMINDER_RECURRENCES } from '../../data/reminder/reminderTypes.ts';
import { useReminderService } from './useReminderService.ts';

const RECURRENCE_LABELS: Record<ReminderRecurrence, string> = {
  none: 'One-time',
  daily: 'Daily',
  weekly: 'Weekly',
  monthly: 'Monthly',
  yearly: 'Yearly',
};

export function CreateReminder() {
  const navigate = useNavigate();
  const { reminderId } = useParams<{ reminderId: string }>();
  const isEditing = Boolean(reminderId);
  const service = useReminderService();

  const today = new Date();
  const defaultDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
  const defaultTime = `${String(today.getHours()).padStart(2, '0')}:${String(today.getMinutes()).padStart(2, '0')}`;

  const toast = useToast();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [scheduledDate, setScheduledDate] = useState(defaultDate);
  const [scheduledTime, setScheduledTime] = useState(defaultTime);
  const [recurrence, setRecurrence] = useState<ReminderRecurrence>('none');
  const [notificationEnabled, setNotificationEnabled] = useState(true);
  const [errors, setErrors] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);

  // Edit mode: prefill the form from the persisted reminder.
  useEffect(() => {
    if (!service || !reminderId) return;
    let cancelled = false;
    service.getById(reminderId).then((existing) => {
      if (cancelled || !existing) return;
      setTitle(existing.title);
      setDescription(existing.description ?? '');
      setScheduledDate(existing.scheduledDate);
      setScheduledTime(existing.scheduledTime);
      setRecurrence(existing.recurrence);
      setNotificationEnabled(existing.notificationEnabled);
    }).catch(() => {
      if (!cancelled) setErrors(['Could not load this reminder.']);
    });
    return () => { cancelled = true; };
  }, [service, reminderId]);

  const handleSave = useCallback(async () => {
    const validationErrors: string[] = [];
    if (!title.trim()) validationErrors.push('Title is required.');
    if (title.trim().length > 200) validationErrors.push('Title must be 200 characters or less.');
    if (!/^\d{4}-\d{2}-\d{2}$/.test(scheduledDate)) validationErrors.push('Invalid date format.');
    if (!/^\d{2}:\d{2}$/.test(scheduledTime)) validationErrors.push('Invalid time format.');

    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      return;
    }

    setErrors([]);
    setSaving(true);

    try {
      if (!service) throw new Error('Reminders unavailable');
      const data = {
        title: title.trim(),
        description: description.trim() || null,
        scheduledDate,
        scheduledTime,
        recurrence,
        notificationEnabled,
      };

      if (isEditing && reminderId) {
        await service.update(reminderId, data);
        toast.success('Reminder updated');
      } else {
        await service.create(data);
        toast.success('Reminder saved');
      }
      navigate(RoutePath.appReminders);
    } catch {
      setErrors(['Failed to save reminder. Please try again.']);
      toast.error('Could not save reminder');
    } finally {
      setSaving(false);
    }
  }, [service, title, description, scheduledDate, scheduledTime, recurrence, notificationEnabled, navigate, isEditing, reminderId, toast]);

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
        <h1 className="th-screen-title" style={{ flex: 1 }}>
          {isEditing ? 'Edit Reminder' : 'New Reminder'}
        </h1>
      </div>

      {/* Form */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-5)' }}>
        {/* Title */}
        <div>
          <label style={{ display: 'block', fontSize: 'var(--th-font-size-sm)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-2)', color: 'var(--th-color-text-primary)' }}>
            Title *
          </label>
          <input
            className="th-input"
            type="text"
            value={title}
            onChange={(e) => { setTitle(e.target.value); setErrors([]); }}
            placeholder="What do you want to be reminded about?"
            maxLength={200}
            autoFocus
          />
        </div>

        {/* Description */}
        <div>
          <label style={{ display: 'block', fontSize: 'var(--th-font-size-sm)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-2)', color: 'var(--th-color-text-primary)' }}>
            Notes
          </label>
          <textarea
            className="th-textarea"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Add details (optional)"
            rows={3}
          />
        </div>

        {/* Date */}
        <div>
          <label style={{ display: 'block', fontSize: 'var(--th-font-size-sm)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-2)', color: 'var(--th-color-text-primary)' }}>
            Date *
          </label>
          <input
            className="th-input"
            type="date"
            value={scheduledDate}
            onChange={(e) => { setScheduledDate(e.target.value); setErrors([]); }}
          />
        </div>

        {/* Time */}
        <div>
          <label style={{ display: 'block', fontSize: 'var(--th-font-size-sm)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-2)', color: 'var(--th-color-text-primary)' }}>
            Time *
          </label>
          <input
            className="th-input"
            type="time"
            value={scheduledTime}
            onChange={(e) => { setScheduledTime(e.target.value); setErrors([]); }}
          />
        </div>

        {/* Recurrence */}
        <div>
          <label style={{ display: 'block', fontSize: 'var(--th-font-size-sm)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-2)', color: 'var(--th-color-text-primary)' }}>
            Repeat
          </label>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--th-space-2)' }}>
            {REMINDER_RECURRENCES.map((r) => (
              <button
                key={r}
                className={`th-option-chip ${recurrence === r ? 'th-option-chip--active' : ''}`}
                onClick={() => setRecurrence(r)}
                type="button"
              >
                {RECURRENCE_LABELS[r]}
              </button>
            ))}
          </div>
        </div>

        {/* Notification toggle */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)', color: 'var(--th-color-text-primary)' }}>
              Notification
            </div>
            <div style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)' }}>
              {notificationEnabled ? 'You will be notified' : 'Notifications disabled'}
            </div>
          </div>
          <button
            className={`th-btn ${notificationEnabled ? 'th-btn--primary' : 'th-btn--secondary'}`}
            onClick={() => setNotificationEnabled(!notificationEnabled)}
            type="button"
            style={{ minWidth: '48px', padding: 'var(--th-space-2)' }}
          >
            {notificationEnabled ? <IconBell size={18} /> : <IconBellOff size={18} />}
          </button>
        </div>

        {/* Errors */}
        {errors.length > 0 && (
          <div className="th-form-errors">
            {errors.map((e, i) => <p key={i} className="th-form-error">{e}</p>)}
          </div>
        )}

        {/* Save */}
        <button
          className="th-btn th-btn--primary th-btn--full"
          onClick={handleSave}
          disabled={saving}
        >
          {saving ? 'Saving...' : isEditing ? 'Update Reminder' : 'Create Reminder'}
        </button>
      </div>
    </div>
  );
}
