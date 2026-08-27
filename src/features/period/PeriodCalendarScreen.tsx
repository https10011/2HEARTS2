/**
 * Period Calendar (Phase 22, productized in Stage 11).
 *
 * Offline monthly calendar: logged period days, today, and an estimated
 * upcoming window. Month navigation, clear cells, and a legend explain
 * every marker. All data comes from PeriodService over the local
 * database — no network, no server. Markers are kept understandable at a
 * glance without fabricating state.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { usePeriodService } from './usePeriodService.ts';
import { cellStatus, localDateKey, type PeriodCellFill } from './periodPresentation.ts';
import { IconBack, LoadingState } from '../../components/index.ts';
import type { PeriodEntry } from '../../data/period/periodTypes.ts';

function dateKey(y: number, m: number, d: number): string {
  return `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
}

/** All covered days of an entry (start → end inclusive, or just start). */
function entryDays(entry: PeriodEntry): string[] {
  const days: string[] = [];
  const start = new Date(entry.startDate + 'T00:00:00');
  const end = entry.endDate ? new Date(entry.endDate + 'T00:00:00') : start;
  for (let d = new Date(start); d <= end && days.length < 62; d.setDate(d.getDate() + 1)) {
    days.push(dateKey(d.getFullYear(), d.getMonth(), d.getDate()));
  }
  return days;
}

const WEEKDAY_LABELS = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];

export function PeriodCalendarScreen() {
  const navigate = useNavigate();
  const service = usePeriodService();
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth());
  const [periodDays, setPeriodDays] = useState<Set<string>>(new Set());
  const [predictedStart, setPredictedStart] = useState<string | null>(null);
  const [periodLength, setPeriodLength] = useState(5);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    if (!service) return;
    try {
      const entries = await service.listEntries('owner');
      const days = new Set<string>();
      for (const entry of entries) {
        for (const day of entryDays(entry)) days.add(day);
      }
      setPeriodDays(days);
      const [info, settings] = await Promise.all([
        service.getCycleInfo('owner'),
        service.getSettings('owner'),
      ]);
      setPredictedStart(info?.nextPeriodDate ?? null);
      setPeriodLength(settings.periodLengthDays);
    } catch {
      setPeriodDays(new Set());
    } finally {
      setLoading(false);
    }
  }, [service]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const prevMonth = () => {
    setMonth((m) => {
      if (m === 0) { setYear((y) => y - 1); return 11; }
      return m - 1;
    });
  };
  const nextMonth = () => {
    setMonth((m) => {
      if (m === 11) { setYear((y) => y + 1); return 0; }
      return m + 1;
    });
  };

  // Predicted window: estimated next start + typical period length.
  const predictedDays = new Set<string>();
  if (predictedStart) {
    const start = new Date(predictedStart + 'T00:00:00');
    for (let i = 0; i < periodLength; i += 1) {
      predictedDays.add(dateKey(start.getFullYear(), start.getMonth(), start.getDate()));
      start.setDate(start.getDate() + 1);
    }
  }

  const today = localDateKey();
  const firstOfMonth = new Date(year, month, 1);
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const leadingBlanks = firstOfMonth.getDay();
  const monthLabel = firstOfMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

  const cells: Array<{ key: string; day: number } | null> = [];
  for (let i = 0; i < leadingBlanks; i += 1) cells.push(null);
  for (let d = 1; d <= daysInMonth; d += 1) cells.push({ key: dateKey(year, month, d), day: d });

  // Determine the non-marked "today" outline class only when today is in this month.
  const todayInMonth = today && today.slice(0, 4) === String(year) && today.slice(5, 7) === String(month + 1).padStart(2, '0');

  const cellClass = (fill: PeriodCellFill, isToday: boolean): string => {
    const base = 'th-period-cell';
    const fillClass = fill === 'period' ? 'th-period-cell--period' : fill === 'predicted' ? 'th-period-cell--predicted' : '';
    const todayClass = isToday && todayInMonth ? 'th-period-cell--today' : '';
    return [base, fillClass, todayClass].filter(Boolean).join(' ');
  };

  return (
    <div className="th-content-pad th-screen-warm">
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <button
          className="th-btn th-btn--ghost"
          onClick={() => navigate(RoutePath.appPeriod)}
          style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
          aria-label="Back to period home"
        >
          <IconBack size={20} />
        </button>
        <h1 className="th-screen-title" style={{ flex: 1 }}>Calendar</h1>
      </div>

      {loading || !service ? (
        <LoadingState label="Loading calendar…" />
      ) : (
        <>
          {/* Month navigation + grid */}
          <div className="th-period-cal">
            <div className="th-period-cal__head">
              <button className="th-period-cal__nav" onClick={prevMonth} aria-label="Previous month">
                <span aria-hidden="true" style={{ fontSize: 'var(--th-font-size-xl)', lineHeight: 1 }}>‹</span>
              </button>
              <div className="th-period-cal__month">{monthLabel}</div>
              <button className="th-period-cal__nav" onClick={nextMonth} aria-label="Next month">
                <span aria-hidden="true" style={{ fontSize: 'var(--th-font-size-xl)', lineHeight: 1 }}>›</span>
              </button>
            </div>

            <div className="th-period-cal__dow">
              {WEEKDAY_LABELS.map((w) => (
                <span key={w} aria-hidden="true">{w}</span>
              ))}
            </div>

            <div className="th-period-cal__grid" role="grid" aria-label={monthLabel}>
              {cells.map((cell, i) => {
                if (!cell) {
                  return <div key={`blank-${i}`} className="th-period-cell th-period-cell--disabled" />;
                }
                const { fill, isToday } = cellStatus(cell.key, today, periodDays, predictedDays);
                return (
                  <div
                    key={cell.key}
                    className={cellClass(fill, isToday)}
                    role="gridcell"
                    aria-label={`${cell.day} ${monthLabel}${fill === 'period' ? ', logged period' : fill === 'predicted' ? ', estimated period' : ''}`}
                  >
                    <span className="th-period-cell__day">{cell.day}</span>
                    <span className="th-period-cell__dot" aria-hidden="true" />
                  </div>
                );
              })}
            </div>
          </div>

          {/* Legend */}
          <div className="th-period-legend">
            <div className="th-period-legend__list">
              <div className="th-period-legend__item">
                <span className="th-period-legend__swatch th-period-legend__swatch--period" aria-hidden="true" />
                Logged period day
              </div>
              <div className="th-period-legend__item">
                <span className="th-period-legend__swatch th-period-legend__swatch--predicted" aria-hidden="true" />
                Estimated upcoming period
              </div>
              <div className="th-period-legend__item">
                <span className="th-period-legend__swatch th-period-legend__swatch--today" aria-hidden="true" />
                Today
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
