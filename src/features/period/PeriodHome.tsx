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
import { IconSettings, IconCalendar, IconFileText } from '../../components/index.ts';

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
            <IconSettings size={20} />
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
                <span
                  aria-hidden="true"
                  style={{
                    display: 'inline-block',
                    width: '16px',
                    height: '16px',
                    borderRadius: 'var(--th-radius-circle)',
                    background: 'var(--th-color-burgundy)',
                    marginBottom: 'var(--th-space-1)',
                  }}
                />
                <div style={{ fontWeight: 'var(--th-font-weight-semibold)', fontSize: 'var(--th-font-size-lg)' }}>Day {cycle.currentPeriodDay} of Period</div>
                <div style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)' }}>
                  Started {formatDate(cycle.lastPeriodStart)}
                </div>
              </div>
            ) : (
              <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-3)' }}>
                <span
                  aria-hidden="true"
                  style={{
                    display: 'inline-block',
                    width: '16px',
                    height: '16px',
                    borderRadius: 'var(--th-radius-circle)',
                    background: 'var(--th-color-rose-muted)',
                    marginBottom: 'var(--th-space-1)',
                  }}
                />
                <div style={{ fontWeight: 'var(--th-font-weight-semibold)', fontSize: 'var(--th-font-size-lg)' }}>Cycle Day {cycle.currentCycleDay}</div>
                <div style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)' }}>
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
                background: 'var(--th-color-border)',
                overflow: 'hidden',
              }}>
                <div style={{
                  height: '100%',
                  width: `${Math.min(100, (cycle.currentCycleDay / cycle.cycleLength) * 100)}%`,
                  borderRadius: '4px',
                  background: cycle.inPeriod ? 'var(--th-color-burgundy)' : 'var(--th-color-pink)',
                  transition: 'width var(--th-motion-standard)',
                }} />
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 'var(--th-space-1)' }}>
                <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>Day 1</span>
                <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>Day {cycle.cycleLength}</span>
              </div>
            </div>
          </div>
        ) : (
          <div className="th-empty-emotional" style={{ marginTop: 'var(--th-space-4)' }}>
            <div className="th-empty-emotional__visual th-scale-in">
              <IconCalendar size={36} />
            </div>
            <h3 className="th-empty-emotional__title">No period data yet</h3>
            <p className="th-empty-emotional__message">
              Start tracking to see your cycle information.
            </p>
            <div className="th-empty-emotional__action">
              <button
                className="th-btn th-btn--primary"
                onClick={() => navigate(RoutePath.appPeriodLog)}
              >
                Log Your First Period
              </button>
            </div>
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
          <div style={{ marginBottom: 'var(--th-space-1)', color: 'var(--th-color-burgundy)' }}>
            <IconCalendar size={24} />
          </div>
          <div style={{ fontWeight: 'var(--th-font-weight-medium)', fontSize: 'var(--th-font-size-sm)' }}>Calendar</div>
        </button>
        <button
          className="th-card th-card--clickable"
          onClick={() => navigate(RoutePath.appPeriodHistory)}
          style={{ padding: 'var(--th-space-3)', textAlign: 'center' }}
        >
          <div style={{ marginBottom: 'var(--th-space-1)', color: 'var(--th-color-burgundy)' }}>
            <IconFileText size={24} />
          </div>
          <div style={{ fontWeight: 'var(--th-font-weight-medium)', fontSize: 'var(--th-font-size-sm)' }}>History</div>
        </button>
      </div>

      {/* Recent entries */}
      {summary && summary.totalCycles > 0 && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--th-space-3)' }}>
            <h2 style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)' }}>Recent Entries</h2>
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
                <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>Avg Cycle</div>
                <div style={{ fontWeight: 'var(--th-font-weight-semibold)' }}>{summary.averageCycleLength} days</div>
              </div>
            )}
            {summary.averagePeriodDuration && (
              <div className="th-card" style={{ flex: 1, padding: 'var(--th-space-3)', textAlign: 'center' }}>
                <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>Avg Duration</div>
                <div style={{ fontWeight: 'var(--th-font-weight-semibold)' }}>{summary.averagePeriodDuration} days</div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
