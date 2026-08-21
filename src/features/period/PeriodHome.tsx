/**
 * PeriodHome (Phase 16).
 *
 * Main period tracker dashboard. Shows current cycle status,
 * estimated next period, quick log action, and recent entries.
 * Uses real persisted data via PeriodService.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { getDatabase } from '../../data/database/connection.ts';
import { PeriodRepository } from '../../repositories/periodRepository.ts';
import { PeriodService } from '../../services/period/periodService.ts';
import type { PeriodSummary } from '../../services/period/periodService.ts';

let _periodService: PeriodService | null = null;
async function getPeriodService(): Promise<PeriodService> {
  if (!_periodService) {
    const repo = new PeriodRepository(await getDatabase());
    _periodService = new PeriodService(repo);
  }
  return _periodService;
}

function formatDate(dateStr: string): string {
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
  });
}

export function PeriodHome() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState<PeriodSummary | null>(null);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const service = await getPeriodService();
      const profileId = 'owner';
      const data = await service.getSummary(profileId);
      setSummary(data);
    } catch {
      setSummary(null);
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
          <p>Loading period data...</p>
        </div>
      </div>
    );
  }

  const cycle = summary?.cycleInfo;

  return (
    <div className="th-content-pad">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <h1 className="th-screen-title">Period Tracker</h1>
        <div style={{ display: 'flex', gap: 'var(--th-space-2)', alignItems: 'center' }}>
          <button
            className="th-btn th-btn--ghost"
            onClick={() => navigate(RoutePath.appPeriodSettings)}
            style={{ minWidth: '44px', padding: 'var(--th-space-2)' }}
            aria-label="Period settings"
          >
            ⚙️
          </button>
          <button
            className="th-btn th-btn--primary th-btn--sm"
            onClick={() => navigate(RoutePath.appPeriodLog)}
          >
            Log Period
          </button>
        </div>
      </div>

      {/* Cycle status card */}
      <div className="th-card" style={{ padding: 'var(--th-space-4)', marginBottom: 'var(--th-space-4)' }}>
        {cycle ? (
          <div>
            {cycle.inPeriod ? (
              <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-3)' }}>
                <div style={{ fontSize: '2rem', marginBottom: 'var(--th-space-1)' }}>🔴</div>
                <div style={{ fontWeight: 600, fontSize: 'var(--th-text-lg)' }}>Day {cycle.currentPeriodDay} of Period</div>
                <div style={{ color: 'var(--th-text-secondary)', fontSize: 'var(--th-text-sm)' }}>
                  Started {formatDate(cycle.lastPeriodStart)}
                </div>
              </div>
            ) : (
              <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-3)' }}>
                <div style={{ fontSize: '2rem', marginBottom: 'var(--th-space-1)' }}>📊</div>
                <div style={{ fontWeight: 600, fontSize: 'var(--th-text-lg)' }}>Cycle Day {cycle.currentCycleDay}</div>
                <div style={{ color: 'var(--th-text-secondary)', fontSize: 'var(--th-text-sm)' }}>
                  {cycle.daysUntilNext > 0
                    ? `Next period in ~${cycle.daysUntilNext} days`
                    : 'Period expected soon'}
                </div>
              </div>
            )}

            {/* Progress bar */}
            <div style={{ marginTop: 'var(--th-space-3)' }}>
              <div style={{
                height: '8px',
                borderRadius: '4px',
                background: 'var(--th-border)',
                overflow: 'hidden',
              }}>
                <div style={{
                  height: '100%',
                  width: `${Math.min(100, (cycle.currentCycleDay / cycle.cycleLength) * 100)}%`,
                  borderRadius: '4px',
                  background: cycle.inPeriod ? 'var(--th-primary)' : 'var(--th-accent, #e8a0b4)',
                  transition: 'width 0.3s ease',
                }} />
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 'var(--th-space-1)' }}>
                <span style={{ fontSize: 'var(--th-text-xs)', color: 'var(--th-text-tertiary)' }}>Day 1</span>
                <span style={{ fontSize: 'var(--th-text-xs)', color: 'var(--th-text-tertiary)' }}>Day {cycle.cycleLength}</span>
              </div>
            </div>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: 'var(--th-space-4) 0' }}>
            <div style={{ fontSize: '3rem', marginBottom: 'var(--th-space-2)' }}>📅</div>
            <h3 style={{ marginBottom: 'var(--th-space-2)' }}>No period data yet</h3>
            <p style={{ color: 'var(--th-text-secondary)', marginBottom: 'var(--th-space-4)' }}>
              Start tracking to see your cycle information.
            </p>
            <button
              className="th-btn th-btn--primary"
              onClick={() => navigate(RoutePath.appPeriodLog)}
            >
              Log Your First Period
            </button>
          </div>
        )}
      </div>

      {/* Quick actions */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--th-space-3)', marginBottom: 'var(--th-space-4)' }}>
        <button
          className="th-card th-card--clickable"
          onClick={() => navigate(RoutePath.appPeriodCalendar)}
          style={{ padding: 'var(--th-space-3)', textAlign: 'center' }}
        >
          <div style={{ fontSize: '1.5rem', marginBottom: 'var(--th-space-1)' }}>📆</div>
          <div style={{ fontWeight: 500, fontSize: 'var(--th-text-sm)' }}>Calendar</div>
        </button>
        <button
          className="th-card th-card--clickable"
          onClick={() => navigate(RoutePath.appPeriodHistory)}
          style={{ padding: 'var(--th-space-3)', textAlign: 'center' }}
        >
          <div style={{ fontSize: '1.5rem', marginBottom: 'var(--th-space-1)' }}>📋</div>
          <div style={{ fontWeight: 500, fontSize: 'var(--th-text-sm)' }}>History</div>
        </button>
      </div>

      {/* Recent entries */}
      {summary && summary.totalCycles > 0 && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--th-space-3)' }}>
            <h2 style={{ fontSize: 'var(--th-text-base)', fontWeight: 600 }}>Recent Entries</h2>
            <button
              className="th-btn th-btn--outline th-btn--sm"
              onClick={() => navigate(RoutePath.appPeriodHistory)}
            >
              View All
            </button>
          </div>

          {/* Stats row */}
          <div style={{ display: 'flex', gap: 'var(--th-space-3)', marginBottom: 'var(--th-space-3)' }}>
            {summary.averageCycleLength && (
              <div className="th-card" style={{ flex: 1, padding: 'var(--th-space-3)', textAlign: 'center' }}>
                <div style={{ fontSize: 'var(--th-text-xs)', color: 'var(--th-text-secondary)' }}>Avg Cycle</div>
                <div style={{ fontWeight: 600 }}>{summary.averageCycleLength} days</div>
              </div>
            )}
            {summary.averagePeriodDuration && (
              <div className="th-card" style={{ flex: 1, padding: 'var(--th-space-3)', textAlign: 'center' }}>
                <div style={{ fontSize: 'var(--th-text-xs)', color: 'var(--th-text-secondary)' }}>Avg Duration</div>
                <div style={{ fontWeight: 600 }}>{summary.averagePeriodDuration} days</div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
