/**
 * CycleHistory (Phase 16).
 *
 * Displays the complete period history, sorted by date descending.
 * Shows entries with dates, flow level, and cycle length.
 * Uses real persisted data via PeriodService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { PeriodRepository } from '../../repositories/periodRepository.ts';
import { PeriodService } from '../../services/period/periodService.ts';
import type { PeriodEntry } from '../../data/period/periodTypes.ts';
import { diffDays } from '../../data/period/periodTypes.ts';

let _periodService: PeriodService | null = null;
function getPeriodService(adapter?: unknown): PeriodService {
  if (!_periodService) {
    const repo = new PeriodRepository(adapter as never);
    _periodService = new PeriodService(repo);
  }
  return _periodService;
}

function formatDate(dateStr: string): string {
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

const FLOW_EMOJI: Record<string, string> = {
  light: '💧',
  medium: '🩸',
  heavy: '🔴',
};

export function CycleHistory() {
  const navigate = useNavigate();
  const [entries, setEntries] = useState<PeriodEntry[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const service = getPeriodService();
      const profileId = 'owner';
      const data = await service.listEntries(profileId);
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
          <p>Loading history...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-4)' }}>
        Cycle History
      </h1>

      {entries.length === 0 ? (
        <div className="th-card" style={{ padding: 'var(--th-space-6)', textAlign: 'center' }}>
          <div style={{ fontSize: '3rem', marginBottom: 'var(--th-space-2)' }}>📅</div>
          <h3 style={{ marginBottom: 'var(--th-space-2)' }}>No period entries yet</h3>
          <p style={{ color: 'var(--th-text-secondary)', marginBottom: 'var(--th-space-4)' }}>
            Start logging to see your cycle history.
          </p>
          <button
            className="th-btn th-btn--primary"
            onClick={() => navigate(RoutePath.appPeriodLog)}
          >
            Log Your First Period
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
          {entries.map((entry, index) => {
            const duration = entry.endDate
              ? diffDays(entry.startDate, entry.endDate) + 1
              : null;
            const cycleLength = index < entries.length - 1
              ? diffDays(entries[index + 1].startDate, entry.startDate)
              : null;

            return (
              <div
                key={entry.id}
                className="th-card"
                style={{ padding: 'var(--th-space-4)' }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--th-space-2)' }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>
                      {formatDate(entry.startDate)}
                      {entry.endDate ? ` — ${formatDate(entry.endDate)}` : ' — Ongoing'}
                    </div>
                    <div style={{ display: 'flex', gap: 'var(--th-space-3)', marginTop: 'var(--th-space-1)' }}>
                      <span style={{ fontSize: 'var(--th-text-sm)', color: 'var(--th-text-secondary)' }}>
                        {FLOW_EMOJI[entry.flowLevel]} {entry.flowLevel}
                      </span>
                      {duration && (
                        <span style={{ fontSize: 'var(--th-text-sm)', color: 'var(--th-text-secondary)' }}>
                          {duration} days
                        </span>
                      )}
                    </div>
                  </div>
                  {cycleLength && cycleLength > 15 && cycleLength < 60 && (
                    <span className="th-badge" style={{ fontSize: 'var(--th-text-xs)' }}>
                      {cycleLength}d cycle
                    </span>
                  )}
                </div>
                {entry.note && (
                  <p style={{ fontSize: 'var(--th-text-sm)', color: 'var(--th-text-secondary)', fontStyle: 'italic', margin: 0 }}>
                    "{entry.note}"
                  </p>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
