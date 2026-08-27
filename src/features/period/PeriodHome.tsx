/**
 * Period Tracker Home (Phase 16, productized in Stage 11).
 *
 * A calm, private dashboard for the couple's local period tracking.
 * The current cycle state leads with a quiet status band; quick actions
 * for Calendar/History, real averages, and recent entries follow. All
 * numbers come from persisted data via PeriodService — nothing fabricated.
 * Deliberately restrained (no floral decoration): this is a private,
 * health-focused corner of the shared space.
 */

import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { usePeriodService } from './usePeriodService.ts';
import {
  OWNER_PROFILE_ID,
  entryDurationDays,
  formatPeriodDay,
  localDateKey,
} from './periodPresentation.ts';
import { flowLabel } from './periodPresentation.ts';
import type { PeriodSummary } from '../../services/period/periodService.ts';
import {
  Button,
  IconBack,
  IconButton,
  IconCalendar,
  IconChevronRight,
  IconClock,
  IconFileText,
  IconInfo,
  IconSettings,
  LoadingState,
  useToast,
} from '../../components/index.ts';

export function PeriodHome() {
  const navigate = useNavigate();
  const service = usePeriodService();
  const toast = useToast();
  const [summary, setSummary] = useState<PeriodSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const today = localDateKey();

  const loadData = useCallback(async () => {
    if (!service) return;
    setLoading(true);
    setError(false);
    try {
      const data = await service.getSummary(OWNER_PROFILE_ID);
      setSummary(data);
    } catch {
      setError(true);
      toast.error('Could not load period data');
    } finally {
      setLoading(false);
    }
  }, [service, toast]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (loading || !service) {
    return <LoadingState label="Loading your cycle…" />;
  }

  const header = (
    <header className="th-period-header">
      <IconButton label="Go back" onClick={() => navigate(-1)}>
        <IconBack />
      </IconButton>
      <div className="th-period-header__copy">
        <h1 className="th-period-title">Period Tracker</h1>
        <p className="th-period-subtitle">Private. Only on this device.</p>
      </div>
      <IconButton label="Period settings" onClick={() => navigate(RoutePath.appPeriodSettings)}>
        <IconSettings />
      </IconButton>
    </header>
  );

  if (error) {
    return (
      <div className="th-content-pad th-screen-warm">
        {header}
        <div className="th-period-error" role="alert">
          <p>We couldn&apos;t load your period data just now.</p>
          <Button variant="secondary" onClick={loadData}>Try again</Button>
        </div>
      </div>
    );
  }

  const cycle = summary?.cycleInfo;
  const hasData = Boolean(summary && summary.totalCycles > 0);
  const recent = (summary?.lastEntry ? [summary.lastEntry] : []);

  return (
    <div className="th-content-pad th-screen-warm">
      {header}

      {!hasData ? (
        <>
          <div className="th-period-status">
            <div className="th-period-empty">
              <div className="th-period-empty__visual" aria-hidden="true">
                <IconCalendar size={36} />
              </div>
              <h3 className="th-period-empty__title">Nothing logged yet</h3>
              <p className="th-period-empty__text">
                Once you log a period, your cycle, calendar and history will
                appear here — humble and private.
              </p>
              <Button variant="primary" onClick={() => navigate(RoutePath.appPeriodLog)}>
                Log Your First Period
              </Button>
              <p className="th-period-empty__privacy">
                <IconInfo size={15} /> Tracked locally, never shared
              </p>
            </div>
          </div>
        </>
      ) : (
        <>
          {/* Current cycle status band */}
          {cycle && (
            <div className="th-period-status">
              <p className="th-period-status__eyebrow">Current cycle</p>
              <div className="th-period-status__identity">
                <span className="th-period-status__medallion" aria-hidden="true">
                  {cycle.inPeriod ? <IconClock size={28} /> : <IconCalendar size={28} />}
                </span>
                <div className="th-period-status__copy">
                  <div className="th-period-status__title">
                    {cycle.inPeriod
                      ? `Day ${cycle.currentPeriodDay} of your period`
                      : `Cycle day ${cycle.currentCycleDay}`}
                  </div>
                  <p className="th-period-status__desc">
                    {cycle.daysUntilNext > 0
                      ? `Next period expected in about ${cycle.daysUntilNext} days`
                      : 'Your period is expected around now'}
                  </p>
                  <p className="th-period-status__meta">
                    Started {formatPeriodDay(cycle.lastPeriodStart, today)}
                  </p>
                </div>
              </div>
              <div className="th-period-progress">
                <div className="th-period-progress__track">
                  <div
                    className="th-period-progress__bar"
                    style={{
                      width: `${Math.min(100, (cycle.currentCycleDay / cycle.cycleLength) * 100)}%`,
                    }}
                  />
                </div>
                <div className="th-period-progress__labels">
                  <span>Day 1</span>
                  <span>Day {cycle.cycleLength}</span>
                </div>
              </div>
            </div>
          )}

          {/* Quick actions */}
          <div className="th-period-actions">
            <button
              className="th-period-action"
              onClick={() => navigate(RoutePath.appPeriodCalendar)}
            >
              <span className="th-period-action__icon" aria-hidden="true"><IconCalendar size={22} /></span>
              <span className="th-period-action__label">Calendar</span>
            </button>
            <button
              className="th-period-action"
              onClick={() => navigate(RoutePath.appPeriodHistory)}
            >
              <span className="th-period-action__icon" aria-hidden="true"><IconFileText size={22} /></span>
              <span className="th-period-action__label">History</span>
            </button>
          </div>

          {/* Averages */}
          {(summary?.averageCycleLength != null || summary?.averagePeriodDuration != null) && (
            <div className="th-period-stats">
              {summary?.averageCycleLength != null && (
                <div className="th-period-stat">
                  <div className="th-period-stat__label">Avg cycle</div>
                  <div className="th-period-stat__value">{summary.averageCycleLength} days</div>
                </div>
              )}
              {summary?.averagePeriodDuration != null && (
                <div className="th-period-stat">
                  <div className="th-period-stat__label">Avg period</div>
                  <div className="th-period-stat__value">{summary.averagePeriodDuration} days</div>
                </div>
              )}
            </div>
          )}

          {/* Recent entry */}
          {recent.length > 0 && (
            <>
              <div className="th-period-section-head">
                <h2 className="th-period-section">Latest period</h2>
                <button className="th-period-see-all" onClick={() => navigate(RoutePath.appPeriodHistory)}>
                  View all
                </button>
              </div>
              <div className="th-period-list">
                <button
                  className="th-period-row"
                  onClick={() => navigate(RoutePath.appPeriodHistory)}
                >
                  <span className="th-period-row__body">
                    <span className="th-period-row__date">
                      {formatPeriodDay(recent[0].startDate, today)}
                    </span>
                    <span className="th-period-row__meta">
                      <span className="th-period-flow">
                        <span
                          aria-hidden="true"
                          style={{
                            display: 'inline-block',
                            width: '10px',
                            height: '10px',
                            borderRadius: 'var(--th-radius-circle)',
                            background: 'var(--th-color-rose-muted)',
                          }}
                        />
                        {flowLabel(recent[0].flowLevel)}
                      </span>
                      {entryDurationDays(recent[0]) && (
                        <span>{entryDurationDays(recent[0])} days</span>
                      )}
                    </span>
                  </span>
                  <span className="th-period-row__chevron" aria-hidden="true"><IconChevronRight size={20} /></span>
                </button>
              </div>
            </>
          )}

          {/* Log another period CTA */}
          <div style={{ marginTop: 'var(--th-space-6)' }}>
            <Button variant="primary" full onClick={() => navigate(RoutePath.appPeriodLog)}>
              Log Period
            </Button>
          </div>
        </>
      )}

      <p className="th-period-footer">Your periods are private and stay on this device.</p>
    </div>
  );
}
