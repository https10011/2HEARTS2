/**
 * MoodHome (Phase 15).
 *
 * Displays the mood tracking dashboard. Shows today's mood status,
 * quick mood selection, and recent mood history.
 * Uses real persisted data via MoodService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { getDatabase } from '../../data/database/connection.ts';
import { MoodRepository } from '../../repositories/moodRepository.ts';
import { MoodService } from '../../services/mood/moodService.ts';
import { IconSmile, LoadingState, RoseLilyDecoration } from '../../components/index.ts';

import {
  type MoodEntry,
  type MoodValue,
  MOOD_EMOJI,
  MOOD_LABELS,
  MOOD_VALUES,
} from '../../data/mood/moodTypes.ts';

let _moodService: MoodService | null = null;
async function getMoodService(): Promise<MoodService> {
  if (!_moodService) {
    const repo = new MoodRepository(await getDatabase());
    _moodService = new MoodService(repo);
  }
  return _moodService;
}

/** Returns today's date in yyyy-mm-dd format. */
function todayKey(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export function MoodHome() {
  const navigate = useNavigate();
  const [todayMood, setTodayMood] = useState<MoodEntry | null>(null);
  const [recentMoods, setRecentMoods] = useState<MoodEntry[]>([]);
  const [loading, setLoading] = useState(true);

  const [saving, setSaving] = useState(false);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const service = await getMoodService();
      // Use a placeholder profileId — in real app would come from context
      const profileId = 'owner';
      const today = todayKey();
      const existing = await service.getByProfileAndDate(profileId, today);
      setTodayMood(existing);
      const recent = await service.listRecent(7);
      setRecentMoods(recent);
    } catch {
      // Silently handle
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleQuickMood = async (mood: MoodValue) => {
    setSaving(true);
    try {
      const service = await getMoodService();
      const profileId = 'owner';
      const today = todayKey();
      const entry = await service.record({
        moodValue: mood,
        profileId,
        entryDate: today,
      });
      setTodayMood(entry);
      // Refresh recent
      const recent = await service.listRecent(7);
      setRecentMoods(recent);
    } catch {
      // Silently handle
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="th-content-pad">
        <LoadingState label="Loading mood data…" />
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-4)' }}>
        How Are You?
      </h1>

      {/* Today's mood */}
      <div className="th-card" style={{ padding: 'var(--th-space-4)', marginBottom: 'var(--th-space-4)' }}>
        <h3 style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-3)' }}>
          {todayMood ? "Today's mood" : "How are you feeling today?"}
        </h3>

        {todayMood ? (
          <div style={{ textAlign: 'center', padding: 'var(--th-space-4) 0' }}>
            <div style={{ fontSize: '3rem', marginBottom: 'var(--th-space-2)' }}>
              {todayMood.moodEmoji}
            </div>
            <div style={{ fontWeight: 'var(--th-font-weight-semibold)', fontSize: 'var(--th-font-size-lg)' }}>
              {MOOD_LABELS[todayMood.moodValue]}
            </div>
            {todayMood.note && (
              <p style={{ color: 'var(--th-color-text-secondary)', marginTop: 'var(--th-space-2)', fontStyle: 'italic' }}>
                "{todayMood.note}"
              </p>
            )}
            <button
              className="th-btn th-btn--outline th-btn--sm"
              onClick={() => navigate(RoutePath.appMoodAdd)}
              style={{ marginTop: 'var(--th-space-3)' }}
            >
              Change Mood
            </button>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 'var(--th-space-2)' }}>
            {MOOD_VALUES.map((mood) => (
              <button
                key={mood}
                className="th-btn th-btn--outline"
                onClick={() => handleQuickMood(mood)}
                disabled={saving}
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: 'var(--th-space-1)',
                  padding: 'var(--th-space-2)',
                  fontSize: 'var(--th-font-size-sm)',
                }}
              >
                <span style={{ fontSize: '1.5rem' }}>{MOOD_EMOJI[mood]}</span>
                <span style={{ fontSize: 'var(--th-font-size-xs)' }}>{MOOD_LABELS[mood]}</span>
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Mood history */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--th-space-3)' }}>
        <h2 style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)' }}>Recent Moods</h2>
        <button
          className="th-btn th-btn--outline th-btn--sm"
          onClick={() => navigate(RoutePath.appMoodHistory)}
        >
          View All
        </button>
      </div>

      {recentMoods.length === 0 ? (
        <div className="th-empty-emotional" style={{ marginTop: 'var(--th-space-4)' }}>
          <RoseLilyDecoration variant={18} size={80} position="bottom-right" opacity={0.1} animated />
          <div className="th-empty-emotional__visual th-scale-in">
            <IconSmile size={36} />
          </div>
          <h3 className="th-empty-emotional__title">No moods yet</h3>
          <p className="th-empty-emotional__message">
            Start tracking how you feel — it's a beautiful way to understand each other
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-2)' }}>
          {recentMoods.slice(0, 5).map((entry) => (
            <div
              key={entry.id}
              className="th-card"
              style={{ padding: 'var(--th-space-3)', display: 'flex', alignItems: 'center', gap: 'var(--th-space-3)' }}
            >
              <span style={{ fontSize: '1.5rem' }}>{entry.moodEmoji}</span>
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 'var(--th-font-weight-medium)' }}>{MOOD_LABELS[entry.moodValue]}</div>
                <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
                  {new Date(entry.entryDate + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}
                </div>
              </div>
              {entry.note && (
                <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {entry.note}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
