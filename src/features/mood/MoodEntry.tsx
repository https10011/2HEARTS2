/**
 * MoodEntry (Phase 15).
 *
 * Mood selection screen with emoji grid and optional note.
 * Uses real persisted data via MoodService.
 */

import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { MoodRepository } from '../../repositories/moodRepository.ts';
import { MoodService } from '../../services/mood/moodService.ts';
import { AppError } from '../../services/errors/appError.ts';
import {
  type MoodValue,
  MOOD_EMOJI,
  MOOD_LABELS,
  MOOD_VALUES,
} from '../../data/mood/moodTypes.ts';

let _moodService: MoodService | null = null;
function getMoodService(adapter?: unknown): MoodService {
  if (!_moodService) {
    const repo = new MoodRepository(adapter as never);
    _moodService = new MoodService(repo);
  }
  return _moodService;
}

function todayKey(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export function MoodEntryScreen() {
  const navigate = useNavigate();
  const { entryId } = useParams<{ entryId: string }>();
  const isEditing = Boolean(entryId);

  const [selectedMood, setSelectedMood] = useState<MoodValue | null>(null);
  const [note, setNote] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [loadingEntry, setLoadingEntry] = useState(isEditing);

  useEffect(() => {
    if (!entryId) return;
    const load = async () => {
      try {
        const service = getMoodService();
        const entry = await service.getById(entryId);
        if (entry) {
          setSelectedMood(entry.moodValue);
          setNote(entry.note ?? '');
        }
      } catch {
        setError('Could not load mood entry.');
      } finally {
        setLoadingEntry(false);
      }
    };
    load();
  }, [entryId]);

  const handleSubmit = async () => {
    if (!selectedMood) {
      setError('Please select a mood.');
      return;
    }
    setError('');
    setSaving(true);

    try {
      const service = getMoodService();
      const profileId = 'owner'; // Placeholder — real app uses profile context
      const today = todayKey();

      if (isEditing && entryId) {
        await service.update(entryId, {
          moodValue: selectedMood,
          note: note || null,
        });
      } else {
        await service.record({
          moodValue: selectedMood,
          note: note || null,
          profileId,
          entryDate: today,
        });
      }
      navigate(RoutePath.appMood);
    } catch (err) {
      if (err instanceof AppError) {
        setError(err.userMessage);
      } else {
        setError('An unexpected error occurred.');
      }
    } finally {
      setSaving(false);
    }
  };

  if (loadingEntry) {
    return (
      <div className="th-content-pad">
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-4)' }}>
        {isEditing ? 'Change Mood' : 'How Are You Feeling?'}
      </h1>

      {error && (
        <div className="th-error-banner" style={{ marginBottom: 'var(--th-space-4)' }}>
          {error}
        </div>
      )}

      {/* Mood selection grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 'var(--th-space-3)', marginBottom: 'var(--th-space-6)' }}>
        {MOOD_VALUES.map((mood) => {
          const isSelected = selectedMood === mood;
          return (
            <button
              key={mood}
              onClick={() => setSelectedMood(mood)}
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: 'var(--th-space-1)',
                padding: 'var(--th-space-3)',
                borderRadius: 'var(--th-radius-md)',
                border: isSelected ? '2px solid var(--th-primary)' : '2px solid var(--th-border)',
                background: isSelected ? 'var(--th-primary-light, rgba(106, 27, 43, 0.08))' : 'var(--th-surface)',
                cursor: 'pointer',
                transition: 'all 0.15s ease',
                transform: isSelected ? 'scale(1.05)' : 'scale(1)',
              }}
            >
              <span style={{ fontSize: '2rem' }}>{MOOD_EMOJI[mood]}</span>
              <span style={{ fontSize: 'var(--th-text-xs)', fontWeight: isSelected ? 600 : 400 }}>
                {MOOD_LABELS[mood]}
              </span>
            </button>
          );
        })}
      </div>

      {/* Note */}
      <div style={{ marginBottom: 'var(--th-space-4)' }}>
        <label className="th-label">Add a note (optional)</label>
        <textarea
          className="th-input"
          value={note}
          onChange={(e) => setNote(e.target.value)}
          placeholder="What's on your mind?"
          rows={3}
          maxLength={500}
          style={{ resize: 'vertical' }}
        />
        <div style={{ fontSize: 'var(--th-text-xs)', color: 'var(--th-text-tertiary)', textAlign: 'right', marginTop: 'var(--th-space-1)' }}>
          {note.length}/500
        </div>
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', gap: 'var(--th-space-3)' }}>
        <button
          className="th-btn th-btn--outline"
          onClick={() => navigate(-1)}
          style={{ flex: 1 }}
        >
          Cancel
        </button>
        <button
          className="th-btn th-btn--primary"
          onClick={handleSubmit}
          disabled={saving || !selectedMood}
          style={{ flex: 1 }}
        >
          {saving ? 'Saving...' : isEditing ? 'Update Mood' : 'Save Mood'}
        </button>
      </div>
    </div>
  );
}
