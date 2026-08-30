/**
 * Cycle History (Phase 16, productized in Stage 11).
 *
 * The complete period history, grouped by month and ordered by date
 * descending. Each entry is a calm card showing the date range, flow
 * level, duration, and a cycle-length badge when derivable. All values
 * come from persisted entries via PeriodService — metrics are only ever
 * computed from what the domain model already stores.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { usePeriodService } from './usePeriodService.ts';
import {
  OWNER_PROFILE_ID,
  buildPeriodMonths,
  cycleLengthToPrevious,
  entryDurationDays,
  flowLabel,
} from './periodPresentation.ts';
import type { PeriodEntry } from '../../data/period/periodTypes.ts';
import {
  Button,
  IconBack,
  IconCalendar,
  IconButton,
  IconChevronRight,
  LoadingState,
  useToast,
} from '../../components/index.ts';

/** Format a start date as a short readable range e.g. "Aug 25". */
function formatShort(dateStr: string): string {
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
  });
}

function formatYear(dateStr: string): string {
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', {
    year: 'numeric',
  });
}

function flowDotColor(level: PeriodEntry['flowLevel']): string {
  if (level === 'light') return 'var(--th-color-blush)';
  if (level === 'medium') return 'var(--th-color-rose-muted)';
  return 'var(--th-color-burgundy)';
}

export function CycleHistory() {
  const navigate = useNavigate();
  const service = usePeriodService();
  const toast = useToast();
  const [entries, setEntries] = useState<PeriodEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const loadData = useCallback(async () => {
    if (!service) return;
    setLoading(true);
    setError(false);
    try {
      const data = await service.listEntries(OWNER_PROFILE_ID);
      setEntries(data);
    } catch {
      setError(true);
      toast.error('Could not load history');
    } finally {
      setLoading(false);
    }
  }, [service, toast]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (loading || !service) {
    return <LoadingState label="Loading your history…" />;
  }

  const header = (
    <header className="th-period-header">
      <IconButton label="Go back" onClick={() => navigate(-1)}>
        <IconBack />
      </IconButton>
      <div className="th-period-header__copy">
        <h1 className="th-period-title">Cycle History</h1>
        <p className="th-period-subtitle">Your period history, in one place.</p>
      </div>
    </header>
  );

  if (error) {
    return (
      <div className="th-content-pad th-screen-warm">
        {header}
        <div className="th-period-error" role="alert">
          <p>We couldn&apos;t load your history just now.</p>
          <Button variant="secondary" onClick={loadData}>Try again</Button>
        </div>
      </div>
    );
  }

  if (entries.length === 0) {
    return (
      <div className="th-content-pad th-screen-warm">
        {header}
        <div className="th-period-empty">
          <div className="th-period-empty__visual" aria-hidden="true"><IconCalendar size={36} /></div>
          <h3 className="th-period-empty__title">No history yet</h3>
          <p className="th-period-empty__text">
            When you log a period, its dates and details will appear here.
          </p>
          <Button variant="primary" onClick={() => navigate(RoutePath.appPeriodLog)}>
            Log Your First Period
          </Button>
        </div>
        <p className="th-period-footer">Your periods are private and stay on this device.</p>
      </div>
    );
  }

  const groups = buildPeriodMonths(entries);

  return (
    <div className="th-content-pad th-screen-warm">
      {header}
      <div className="th-period-history-list">
        {groups.map((group) => (
          <div key={group.monthLabel}>
            <h2 className="th-period-month">{group.monthLabel}</h2>
            {group.entries.map((entry) => {
              const duration = entryDurationDays(entry);
              const flatIndex = entries.indexOf(entry);
              const prevStart = flatIndex >= 0 && flatIndex + 1 < entries.length
                ? entries[flatIndex + 1]!.startDate
                : null;
              const cycleLen = cycleLengthToPrevious(entry, prevStart);
              const safeCycle = cycleLen != null && cycleLen > 15 && cycleLen < 60 ? cycleLen : null;
              return (
                <button
                  key={entry.id}
                  className="th-period-history-row"
                  onClick={() => navigate(RoutePath.appPeriodEdit.replace(':entryId', entry.id))}
                >
                  <span className="th-period-history-row__body">
                    <span className="th-period-history-row__dates">
                      <span className="th-period-history-row__range">
                        {formatShort(entry.startDate)}
                      </span>
                      {entry.endDate ? (
                        <span className="th-period-history-row__end">
                          — {formatShort(entry.endDate)}{formatYear(entry.startDate) !== formatYear(entry.endDate) ? `, ${formatYear(entry.endDate)}` : ''}
                        </span>
                      ) : (
                        <span className="th-period-history-row__end">— Ongoing</span>
                      )}
                    </span>
                    <span className="th-period-history-row__meta">
                      <span className="th-period-flow">
                        <span
                          aria-hidden="true"
                          style={{
                            display: 'inline-block',
                            width: '10px',
                            height: '10px',
                            borderRadius: 'var(--th-radius-circle)',
                            background: flowDotColor(entry.flowLevel),
                          }}
                        />
                        {flowLabel(entry.flowLevel)}
                      </span>
                      {duration != null && <span>{duration} days</span>}
                    </span>
                    {entry.note && (
                      <span className="th-period-history-row__note">&ldquo;{entry.note}&rdquo;</span>
                    )}
                  </span>
                  {safeCycle != null && (
                    <span className="th-period-cycle-badge">{safeCycle}d cycle</span>
                  )}
                  <span className="th-period-history-row__chevron" aria-hidden="true">
                    <IconChevronRight size={20} />
                  </span>
                </button>
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
}
