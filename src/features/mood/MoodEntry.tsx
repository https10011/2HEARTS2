/**
 * MoodEntryScreen (Phase 15, productized in Stage 10).
 *
 * The daily check-in composer — expressive mood cards drawn from the
 * centralized icon system (no emoji wall), a clear selected state, an
 * optional note, and a quiet privacy line. Editing an existing check-in
 * reuses the same screen and adds removal through the centralized
 * Modal + toast layer.
 */

import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { AppError } from '../../services/errors/appError.ts';
import { useMoodService } from './useMoodService.ts';
import { MoodIcon } from './moodMeta.tsx';
import {
  OWNER_PROFILE_ID,
  formatCheckInDate,
  formatMoodDay,
  localDateKey,
} from './moodPresentation.ts';
import {
  type MoodValue,
  MOOD_LABELS,
  MOOD_VALUES,
} from '../../data/mood/moodTypes.ts';
import {
  Button,
  IconBack,
  IconButton,
  IconCalendar,
  IconLock,
  IconTrash,
  LoadingState,
  Modal,
  RoseLilyDecoration,
  useToast,
} from '../../components/index.ts';

const NOTE_MAX_LENGTH = 500;

export function MoodEntryScreen() {
  const navigate = useNavigate();
  const { entryId } = useParams<{ entryId: string }>();
  const service = useMoodService();
  const toast = useToast();
  const isEditing = Boolean(entryId);

  const [selectedMood, setSelectedMood] = useState<MoodValue | null>(null);
  const [note, setNote] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [loadingEntry, setLoadingEntry] = useState(isEditing);
  const [entryDate, setEntryDate] = useState<string | null>(null);
  const [showRemoveConfirm, setShowRemoveConfirm] = useState(false);
  const [removing, setRemoving] = useState(false);

  const today = localDateKey();

  useEffect(() => {
    if (!entryId || !service) return;
    let cancelled = false;
    (async () => {
      try {
        const entry = await service.getById(entryId);
        if (cancelled) return;
        if (entry) {
          setSelectedMood(entry.moodValue);
          setNote(entry.note ?? '');
          setEntryDate(entry.entryDate);
        } else {
          setError('This check-in could not be found.');
        }
      } catch {
        if (!cancelled) setError('Could not load this check-in.');
      } finally {
        if (!cancelled) setLoadingEntry(false);
      }
    })();
    return () => { cancelled = true; };
  }, [entryId, service]);

  const handleSubmit = async () => {
    if (!service) return;
    if (!selectedMood) {
      setError('Please choose how you feel.');
      return;
    }
    setError('');
    setSaving(true);
    try {
      if (isEditing && entryId) {
        await service.update(entryId, {
          moodValue: selectedMood,
          note: note.trim() || null,
        });
        toast.success('Mood updated');
      } else {
        await service.record({
          moodValue: selectedMood,
          note: note.trim() || null,
          profileId: OWNER_PROFILE_ID,
          entryDate: today,
        });
        toast.success('Mood saved');
      }
      navigate(RoutePath.appMood);
    } catch (err) {
      if (err instanceof AppError) {
        setError(err.userMessage);
      } else {
        setError('An unexpected error occurred.');
      }
      toast.error('Could not save mood');
    } finally {
      setSaving(false);
    }
  };

  const handleRemove = async () => {
    if (!service || !entryId) return;
    setRemoving(true);
    try {
      await service.delete(entryId);
      toast.success('Mood removed');
      navigate(RoutePath.appMood, { replace: true });
    } catch {
      setRemoving(false);
      setShowRemoveConfirm(false);
      toast.error('Could not remove mood');
    }
  };

  if (loadingEntry || !service) {
    return <LoadingState label="Loading your check-in…" />;
  }

  return (
    <div className="th-content-pad th-screen-warm">
      <RoseLilyDecoration variant={7} size={110} position="top-right" opacity={0.1} />

      <header className="th-mood-header">
        <IconButton label="Go back" onClick={() => navigate(-1)}>
          <IconBack />
        </IconButton>
        <div className="th-mood-header__copy">
          <h1 className="th-mood-title">{isEditing ? 'Edit check-in' : 'Check in'}</h1>
          <p className="th-mood-subtitle">
            {isEditing ? 'Update how you were feeling.' : 'How are you feeling right now?'}
          </p>
        </div>
      </header>

      <span className="th-mood-date-chip">
        <IconCalendar size={15} />
        {isEditing && entryDate ? formatMoodDay(entryDate, today) : formatCheckInDate(today)}
      </span>

      {error && (
        <div className="th-form-error th-form-error--global" role="alert">
          {error}
        </div>
      )}

      {/* Mood selection */}
      <div
        className="th-mood-select th-stagger-item"
        role="group"
        aria-label="Choose your mood"
        style={{ marginBottom: 'var(--th-space-5)' }}
      >
        {MOOD_VALUES.map((mood) => {
          const isSelected = selectedMood === mood;
          return (
            <button
              key={mood}
              type="button"
              className={`th-mood-option ${isSelected ? 'th-mood-option--selected' : ''}`}
              aria-pressed={isSelected}
              onClick={() => setSelectedMood(mood)}
            >
              <span className="th-mood-option__icon" aria-hidden="true">
                <MoodIcon mood={mood} size={20} />
              </span>
              <span className="th-mood-option__label">{MOOD_LABELS[mood]}</span>
            </button>
          );
        })}
      </div>

      {/* Note */}
      <div className="th-form-group th-stagger-item">
        <label className="th-form-label" htmlFor="mood-note">
          Add a note <span className="th-form-optional">(optional)</span>
        </label>
        <textarea
          id="mood-note"
          className="th-input"
          value={note}
          onChange={(e) => setNote(e.target.value)}
          placeholder="What's behind this feeling?"
          rows={3}
          maxLength={NOTE_MAX_LENGTH}
          style={{ resize: 'vertical' }}
        />
        <p className="th-mood-char-count">{note.length}/{NOTE_MAX_LENGTH}</p>
      </div>

      {/* Actions */}
      <div className="th-mood-actions">
        <Button variant="secondary" onClick={() => navigate(-1)}>
          Cancel
        </Button>
        <Button
          variant="primary"
          onClick={handleSubmit}
          disabled={saving || !selectedMood}
        >
          {saving ? 'Saving…' : isEditing ? 'Update check-in' : 'Save check-in'}
        </Button>
      </div>

      {isEditing && entryId && (
        <button
          type="button"
          className="th-mood-remove"
          onClick={() => setShowRemoveConfirm(true)}
        >
          <IconTrash size={16} /> Remove this check-in
        </button>
      )}

      <p className="th-mood-privacy">
        <IconLock size={13} /> Stays on this device — just for the two of you.
      </p>

      <Modal
        open={showRemoveConfirm}
        onClose={() => setShowRemoveConfirm(false)}
        label="Remove check-in"
      >
        <div style={{ padding: 'var(--th-space-2) 0' }}>
          <h3 className="th-note-confirm-title">Remove this check-in?</h3>
          <p className="th-note-confirm-copy">
            This day&apos;s mood will be removed for good. This action cannot be undone.
          </p>
          <div className="th-mood-actions">
            <Button variant="secondary" onClick={() => setShowRemoveConfirm(false)}>
              Keep it
            </Button>
            <Button variant="primary" onClick={handleRemove} disabled={removing}>
              {removing ? 'Removing…' : 'Remove'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
