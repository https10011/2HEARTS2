/**
 * MoodHome (Phase 15, productized in Stage 10).
 *
 * "How are you feeling?" — the couple's daily check-in space. Today's
 * mood leads with the app's own icon language (no emoji wall); a quick
 * one-tap check-in row appears when today is still unlogged. Recent
 * check-ins, a real consecutive-day streak, and warm empty/error states
 * follow. All numbers come from persisted entries — nothing fabricated.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { useMoodService } from './useMoodService.ts';
import { MoodIcon } from './moodMeta.tsx';
import {
  MOOD_DESCRIPTIONS,
  OWNER_PROFILE_ID,
  computeCheckInStreak,
  formatMoodDay,
  localDateKey,
} from './moodPresentation.ts';
import {
  type MoodEntry,
  type MoodValue,
  MOOD_LABELS,
  MOOD_VALUES,
} from '../../data/mood/moodTypes.ts';
import {
  Button,
  IconBack,
  IconButton,
  IconChevronRight,
  IconEdit,
  IconHeart,
  IconPlus,
  LoadingState,
  RoseLilyDecoration,
  useToast,
} from '../../components/index.ts';

export function MoodHome() {
  const navigate = useNavigate();
  const service = useMoodService();
  const toast = useToast();

  const [todayMood, setTodayMood] = useState<MoodEntry | null>(null);
  const [entries, setEntries] = useState<MoodEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const today = localDateKey();

  const loadData = useCallback(async () => {
    if (!service) return;
    setLoading(true);
    setError(null);
    try {
      const existing = await service.getByProfileAndDate(OWNER_PROFILE_ID, today);
      setTodayMood(existing);
      const all = await service.list();
      setEntries(all);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load moods.');
    } finally {
      setLoading(false);
    }
  }, [service, today]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleQuickMood = async (mood: MoodValue) => {
    if (!service) return;
    setSaving(true);
    try {
      const entry = await service.record({
        moodValue: mood,
        profileId: OWNER_PROFILE_ID,
        entryDate: today,
      });
      setTodayMood(entry);
      const all = await service.list();
      setEntries(all);
      toast.success('Mood saved');
    } catch {
      toast.error('Could not save mood');
    } finally {
      setSaving(false);
    }
  };

  if (loading || !service) {
    return <LoadingState label="Loading your check-ins…" />;
  }

  const header = (
    <header className="th-mood-header">
      <IconButton label="Go back" onClick={() => navigate(-1)}>
        <IconBack />
      </IconButton>
      <div className="th-mood-header__copy">
        <h1 className="th-mood-title">Mood</h1>
        <p className="th-mood-subtitle">A little check-in, every day.</p>
      </div>
      <IconButton label="Check in" onClick={() => navigate(RoutePath.appMoodAdd)}>
        <IconPlus />
      </IconButton>
    </header>
  );

  if (error) {
    return (
      <div className="th-content-pad th-screen-warm">
        {header}
        <div className="th-mood-error" role="alert">
          <p>We couldn&apos;t load your check-ins just now.</p>
          <Button variant="secondary" onClick={loadData}>Try again</Button>
        </div>
      </div>
    );
  }

  const streak = computeCheckInStreak(entries, today);
  const recent = entries.slice(0, 5);

  return (
    <div className="th-content-pad th-screen-warm">
      <RoseLilyDecoration variant={11} size={120} position="top-right" opacity={0.12} />
      {header}

      {/* Today's check-in */}
      <section className="th-mood-today th-stagger-item" aria-label="Today's check-in">
        {todayMood ? (
          <>
            <p className="th-mood-today__eyebrow">Today you&apos;re feeling</p>
            <div className="th-mood-today__identity">
              <span className="th-mood-medallion th-mood-medallion--lg" aria-hidden="true">
                <MoodIcon mood={todayMood.moodValue} size={30} />
              </span>
              <div className="th-mood-today__copy">
                <h2 className="th-mood-today__mood">{MOOD_LABELS[todayMood.moodValue]}</h2>
                <p className="th-mood-today__desc">{MOOD_DESCRIPTIONS[todayMood.moodValue]}</p>
              </div>
            </div>
            {todayMood.note && (
              <p className="th-mood-today__note">&ldquo;{todayMood.note}&rdquo;</p>
            )}
            <div className="th-mood-today__actions">
              <Button
                variant="secondary"
                onClick={() => navigate(RoutePath.appMoodEdit.replace(':entryId', todayMood.id))}
              >
                <IconEdit size={16} /> Update today&apos;s check-in
              </Button>
            </div>
          </>
        ) : (
          <>
            <h2 className="th-mood-today__prompt">How are you feeling today?</h2>
            <p className="th-mood-today__prompt-sub">
              One tap is enough — add a note anytime.
            </p>
            <div className="th-mood-select" role="group" aria-label="Choose today's mood">
              {MOOD_VALUES.map((mood) => (
                <button
                  key={mood}
                  type="button"
                  className="th-mood-option"
                  onClick={() => handleQuickMood(mood)}
                  disabled={saving}
                >
                  <span className="th-mood-option__icon" aria-hidden="true">
                    <MoodIcon mood={mood} size={20} />
                  </span>
                  <span className="th-mood-option__label">{MOOD_LABELS[mood]}</span>
                </button>
              ))}
            </div>
          </>
        )}
      </section>

      {/* Real consecutive-day streak */}
      {streak >= 2 && (
        <div className="th-mood-streak th-stagger-item">
          <IconHeart size={16} />
          {streak} days checked in a row
        </div>
      )}

      {/* Recent check-ins */}
      <div className="th-mood-section-head th-stagger-item">
        <h2 className="th-mood-section">Recent check-ins</h2>
        {entries.length > 0 && (
          <button
            type="button"
            className="th-mood-see-all"
            onClick={() => navigate(RoutePath.appMoodHistory)}
          >
            View history
          </button>
        )}
      </div>

      {recent.length === 0 ? (
        <div className="th-empty-emotional">
          <RoseLilyDecoration variant={18} size={90} position="bottom-right" opacity={0.1} />
          <div className="th-empty-emotional__visual th-scale-in">
            <IconHeart size={38} />
          </div>
          <h3 className="th-empty-emotional__title">No check-ins yet</h3>
          <p className="th-empty-emotional__message">
            Start with today — a small daily note about how you feel becomes
            a beautiful way to understand each other.
          </p>
          <div className="th-empty-emotional__action">
            <Button variant="primary" onClick={() => navigate(RoutePath.appMoodAdd)}>
              <IconPlus size={18} /> Check in now
            </Button>
          </div>
        </div>
      ) : (
        <div className="th-mood-list">
          {recent.map((entry) => (
            <button
              key={entry.id}
              type="button"
              className="th-mood-row th-stagger-item"
              onClick={() => navigate(RoutePath.appMoodEdit.replace(':entryId', entry.id))}
            >
              <span className="th-mood-medallion" aria-hidden="true">
                <MoodIcon mood={entry.moodValue} size={20} />
              </span>
              <span className="th-mood-row__body">
                <span className="th-mood-row__top">
                  <span className="th-mood-row__label">{MOOD_LABELS[entry.moodValue]}</span>
                  <span className="th-mood-row__date">{formatMoodDay(entry.entryDate, today)}</span>
                </span>
                {entry.note && <span className="th-mood-row__note">&ldquo;{entry.note}&rdquo;</span>}
              </span>
              <span className="th-mood-row__chevron" aria-hidden="true">
                <IconChevronRight size={18} />
              </span>
            </button>
          ))}
        </div>
      )}

      <p className="th-mood-footer">Small feelings, shared daily, add up to a lot.</p>
    </div>
  );
}
