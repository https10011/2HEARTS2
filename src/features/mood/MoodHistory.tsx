/**
 * MoodHistory (Phase 15).
 *
 * Displays the complete mood history, sorted by date descending.
 * Shows mood entries with emoji, label, date, and optional note.
 * Uses real persisted data via MoodService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { MoodRepository } from '../../repositories/moodRepository.ts';
import { MoodService } from '../../services/mood/moodService.ts';
import type { MoodEntry } from '../../data/mood/moodTypes.ts';
import { MOOD_LABELS } from '../../data/mood/moodTypes.ts';

let _moodService: MoodService | null = null;
function getMoodService(adapter?: unknown): MoodService {
  if (!_moodService) {
    const repo = new MoodRepository(adapter as never);
    _moodService = new MoodService(repo);
  }
  return _moodService;
}

/** Groups mood entries by month for display. */
function groupByMonth(entries: MoodEntry[]): Array<{ month: string; entries: MoodEntry[] }> {
  const groups = new Map<string, MoodEntry[]>();
  for (const entry of entries) {
    const d = new Date(entry.entryDate + 'T00:00:00');
    const monthKey = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
    if (!groups.has(monthKey)) {
      groups.set(monthKey, []);
    }
    groups.get(monthKey)!.push(entry);
  }
  return Array.from(groups.entries()).map(([, items]) => ({
    month: items[0] ? new Date(items[0].entryDate + 'T00:00:00').toLocaleDateString('en-US', { month: 'long', year: 'numeric' }) : '',
    entries: items,
  }));
}

export function MoodHistory() {
  const navigate = useNavigate();
  const [entries, setEntries] = useState<MoodEntry[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const service = getMoodService();
      const data = await service.list();
      setEntries(data);
    } catch {
      setEntries([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (loading) {
    return (
      <div className="th-content-pad">
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading mood history...</p>
        </div>
      </div>
    );
  }

  const grouped = groupByMonth(entries);

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-4)' }}>
        Mood History
      </h1>

      {entries.length === 0 ? (
        <div className="th-card" style={{ padding: 'var(--th-space-6)', textAlign: 'center' }}>
          <div style={{ fontSize: '3rem', marginBottom: 'var(--th-space-2)' }}>📝</div>
          <h3 style={{ marginBottom: 'var(--th-space-2)' }}>No mood entries yet</h3>
          <p style={{ color: 'var(--th-text-secondary)', marginBottom: 'var(--th-space-4)' }}>
            Start tracking how you feel each day.
          </p>
          <button
            className="th-btn th-btn--primary"
            onClick={() => navigate(RoutePath.appMoodAdd)}
          >
            Record Your Mood
          </button>
        </div>
      ) : (
        <div>
          {grouped.map((group) => (
            <div key={group.month} style={{ marginBottom: 'var(--th-space-6)' }}>
              <h2 style={{ fontSize: 'var(--th-text-sm)', color: 'var(--th-text-secondary)', marginBottom: 'var(--th-space-3)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                {group.month}
              </h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-2)' }}>
                {group.entries.map((entry) => (
                  <div
                    key={entry.id}
                    className="th-card"
                    style={{ padding: 'var(--th-space-3)', display: 'flex', alignItems: 'center', gap: 'var(--th-space-3)' }}
                  >
                    <span style={{ fontSize: '1.75rem' }}>{entry.moodEmoji}</span>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ fontWeight: 500 }}>{MOOD_LABELS[entry.moodValue]}</span>
                        <span style={{ fontSize: 'var(--th-text-xs)', color: 'var(--th-text-secondary)' }}>
                          {new Date(entry.entryDate + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}
                        </span>
                      </div>
                      {entry.note && (
                        <p style={{ fontSize: 'var(--th-text-sm)', color: 'var(--th-text-secondary)', margin: 'var(--th-space-1) 0 0', fontStyle: 'italic' }}>
                          "{entry.note}"
                        </p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
