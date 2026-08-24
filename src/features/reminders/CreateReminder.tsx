/**
 * CreateReminder (Stage 8).
 *
 * The reminder composer as a warm branded form: title, the centralized
 * DatePicker (future-aware), the branded TimePicker wheel, a recurrence
 * sheet through the centralized Modal, and a "Notify me" Switch. Save /
 * validation / notification scheduling flow through ReminderService
 * unchanged.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  IconBack,
  IconBell,
  IconCheck,
  IconChevronRight,
  IconRepeat,
  IconButton,
  DatePicker,
  TimePicker,
  Modal,
  Switch,
  useToast,
  RoseLilyDecoration,
} from '../../components/index.ts';
import type { ReminderRecurrence } from '../../data/reminder/reminderTypes.ts';
import { REMINDER_RECURRENCES } from '../../data/reminder/reminderTypes.ts';
import { useReminderService } from './useReminderService.ts';
import { RECURRENCE_LABELS } from './reminderSchedule.ts';

function localTodayKey(): string {
  const now = new Date();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${m}-${d}`;
}

export function CreateReminder() {
  const navigate = useNavigate();
  const { reminderId } = useParams<{ reminderId: string }>();
  const isEditing = Boolean(reminderId);
  const service = useReminderService();
  const toast = useToast();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [scheduledDate, setScheduledDate] = useState(localTodayKey());
  const [scheduledTime, setScheduledTime] = useState('09:00');
  const [recurrence, setRecurrence] = useState<ReminderRecurrence>('none');
  const [notificationEnabled, setNotificationEnabled] = useState(true);
  const [repeatOpen, setRepeatOpen] = useState(false);
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
    if (!title.trim()) validationErrors.push('Give this reminder a name.');
    if (title.trim().length > 200) validationErrors.push('Keep the name under 200 characters.');
    if (!/^\d{4}-\d{2}-\d{2}$/.test(scheduledDate)) validationErrors.push('Choose a date.');
    if (!/^\d{2}:\d{2}$/.test(scheduledTime)) validationErrors.push('Choose a time.');

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
      setErrors(['Could not save this reminder. Please try again.']);
      toast.error('Could not save reminder');
    } finally {
      setSaving(false);
    }
  }, [service, title, description, scheduledDate, scheduledTime, recurrence, notificationEnabled, navigate, isEditing, reminderId, toast]);

  const currentYear = new Date().getFullYear();

  return (
    <div className="th-content-pad th-screen-warm">
      <RoseLilyDecoration variant={11} size={110} position="top-right" opacity={0.1} />

      {/* Branded header */}
      <header className="th-rem-header">
        <IconButton label="Go back" onClick={() => navigate(RoutePath.appReminders)}>
          <IconBack />
        </IconButton>
        <div className="th-rem-header__copy">
          <h1 className="th-rem-title">{isEditing ? 'Edit Reminder' : 'New Reminder'}</h1>
          <p className="th-rem-subtitle">
            {isEditing ? 'Adjust the little details.' : 'Save a little thing worth remembering.'}
          </p>
        </div>
      </header>

      {/* Form */}
      <div className="th-rem-form">
        {/* Title */}
        <div className="th-rem-field">
          <label className="th-rem-field__label" htmlFor="reminder-title">Reminder</label>
          <input
            id="reminder-title"
            className="th-input"
            type="text"
            value={title}
            onChange={(e) => { setTitle(e.target.value); setErrors([]); }}
            placeholder="What do you need to remember?"
            maxLength={200}
            autoFocus
          />
        </div>

        {/* Date — centralized branded DatePicker (future-aware range) */}
        <div className="th-rem-field">
          <label className="th-rem-field__label" id="reminder-date-label">Date</label>
          <DatePicker
            value={scheduledDate}
            onChange={(val) => { setScheduledDate(val); setErrors([]); }}
            label="Reminder date"
            placeholder="Tap to choose a date"
            minYear={currentYear - 1}
            maxYear={currentYear + 10}
            aria-describedby="reminder-date-label"
          />
        </div>

        {/* Time — branded wheel picker */}
        <div className="th-rem-field">
          <label className="th-rem-field__label" id="reminder-time-label">Time</label>
          <TimePicker
            value={scheduledTime}
            onChange={(val) => { setScheduledTime(val); setErrors([]); }}
            label="Reminder time"
            placeholder="Tap to choose a time"
          />
        </div>

        {/* Repeat — branded row opening the centralized Modal sheet */}
        <div className="th-rem-field">
          <label className="th-rem-field__label" id="reminder-repeat-label">Repeat</label>
          <button
            type="button"
            className="th-rem-choice"
            onClick={() => setRepeatOpen(true)}
            aria-haspopup="dialog"
            aria-labelledby="reminder-repeat-label"
          >
            <span className="th-rem-choice__icon" aria-hidden="true">
              <IconRepeat size={18} />
            </span>
            <span className="th-rem-choice__text">{RECURRENCE_LABELS[recurrence]}</span>
            <IconChevronRight size={16} className="th-rem-choice__chevron" />
          </button>
        </div>

        {/* Notify me — centralized Switch */}
        <div className="th-rem-notify">
          <span className="th-rem-notify__icon" aria-hidden="true">
            <IconBell size={18} />
          </span>
          <div className="th-rem-notify__copy">
            <div className="th-rem-notify__title">Notify me</div>
            <div className="th-rem-notify__sub">
              {notificationEnabled
                ? 'Your phone will gently nudge you when it’s time.'
                : 'No notification — it stays on the list only.'}
            </div>
          </div>
          <Switch
            checked={notificationEnabled}
            onChange={setNotificationEnabled}
            label="Notify me when it’s time"
          />
        </div>

        {/* Note */}
        <div className="th-rem-field">
          <label className="th-rem-field__label" htmlFor="reminder-note">Note</label>
          <textarea
            id="reminder-note"
            className="th-textarea"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Add a little more detail…"
            rows={3}
          />
        </div>

        {/* Errors */}
        {errors.length > 0 && (
          <div className="th-form-errors" role="alert">
            {errors.map((e, i) => <p key={i} className="th-form-error">{e}</p>)}
          </div>
        )}

        {/* Actions */}
        <div className="th-rem-editor-actions">
          <button
            className="th-btn th-btn--primary th-btn--full"
            onClick={handleSave}
            disabled={saving}
          >
            {saving ? 'Saving…' : isEditing ? 'Save Changes' : 'Save Reminder'}
          </button>
          <button
            className="th-btn th-btn--ghost th-btn--full"
            onClick={() => navigate(RoutePath.appReminders)}
            disabled={saving}
          >
            Cancel
          </button>
        </div>
      </div>

      {/* Repeat options sheet — centralized Modal */}
      <Modal open={repeatOpen} onClose={() => setRepeatOpen(false)} label="Repeat">
        <div className="th-rem-repeat-sheet">
          <h3 className="th-date-picker__title">Repeat</h3>
          <div className="th-rem-repeat-sheet__options" role="listbox" aria-label="Repeat options">
            {REMINDER_RECURRENCES.map((r) => {
              const active = r === recurrence;
              return (
                <button
                  key={r}
                  type="button"
                  role="option"
                  aria-selected={active}
                  className={`th-rem-repeat-option ${active ? 'th-rem-repeat-option--active' : ''}`}
                  onClick={() => { setRecurrence(r); setRepeatOpen(false); }}
                >
                  <span className="th-rem-repeat-option__label">{RECURRENCE_LABELS[r]}</span>
                  {active && <IconCheck size={18} />}
                </button>
              );
            })}
          </div>
        </div>
      </Modal>
    </div>
  );
}
