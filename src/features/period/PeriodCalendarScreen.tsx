/**
 * PeriodCalendarScreen (Phase 22).
 *
 * Offline monthly calendar: logged period days, current day, previous
 * periods, estimated upcoming period, month navigation. All data comes
 * from PeriodService over the local database — no network, no server.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { getDatabase } from '../../data/database/connection.ts';
import { IconBack } from '../../components/index.ts';
import { PeriodRepository } from '../../repositories/periodRepository.ts';
import { PeriodService } from '../../services/period/periodService.ts';
import type { PeriodEntry } from '../../data/period/periodTypes.ts';

let _periodService: PeriodService | null = null;
async function getPeriodService(): Promise<PeriodService> {
  if (!_periodService) {
    const repo = new PeriodRepository(await getDatabase());
    _periodService = new PeriodService(repo);
  }
  return _periodService;
}

function dateKey(y: number, m: number, d: number): string {
  return `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
}

function todayKey(): string {
  const d = new Date();
  return dateKey(d.getFullYear(), d.getMonth(), d.getDate());
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
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth());
  const [periodDays, setPeriodDays] = useState<Set<string>>(new Set());
  const [predictedStart, setPredictedStart] = useState<string | null>(null);
  const [periodLength, setPeriodLength] = useState(5);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    try {
      const service = await getPeriodService();
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
  }, []);

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

  const today = todayKey();
  const firstOfMonth = new Date(year, month, 1);
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const leadingBlanks = firstOfMonth.getDay();
  const monthLabel = firstOfMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

  const cells: Array<{ key: string; day: number } | null> = [];
  for (let i = 0; i < leadingBlanks; i += 1) cells.push(null);
  for (let d = 1; d <= daysInMonth; d += 1) cells.push({ key: dateKey(year, month, d), day: d });

  return (
    <div className="th-content-pad">
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <button
          className="th-btn th-btn--ghost"
          onClick={() => navigate(RoutePath.appPeriod)}
          style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
        >
          <IconBack size={20} />
        </button>
        <h1 className="th-screen-title" style={{ flex: 1 }}>Calendar</h1>
      </div>

      {loading ? (
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading calendar...</p>
        </div>
      ) : (
        <>
          {/* Month navigation */}
          <div className="th-card" style={{ padding: 'var(--th-space-4)' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 'var(--th-space-3)' }}>
              <button className="th-btn th-btn--ghost" onClick={prevMonth} style={{ minWidth: '44px' }} aria-label="Previous month">‹</button>
              <div style={{ fontWeight: 600, fontSize: 'var(--th-font-size-md)' }}>{monthLabel}</div>
              <button className="th-btn th-btn--ghost" onClick={nextMonth} style={{ minWidth: '44px' }} aria-label="Next month">›</button>
            </div>

            {/* Weekday header */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '2px', marginBottom: 'var(--th-space-1)' }}>
              {WEEKDAY_LABELS.map((w) => (
                <div key={w} style={{ textAlign: 'center', fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', padding: 'var(--th-space-1)' }}>
                  {w}
                </div>
              ))}
            </div>

            {/* Day cells */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '2px' }}>
              {cells.map((cell, i) => {
                if (!cell) return <div key={`blank-${i}`} />;
                const isPeriod = periodDays.has(cell.key);
                const isPredicted = !isPeriod && predictedDays.has(cell.key);
                const isToday = cell.key === today;
                return (
                  <div
                    key={cell.key}
                    style={{
                      textAlign: 'center',
                      padding: 'var(--th-space-2) 0',
                      borderRadius: 'var(--th-radius-circle)',
                      fontSize: 'var(--th-font-size-sm)',
                      fontWeight: isToday ? 700 : 400,
                      background: isPeriod
                        ? 'var(--th-color-burgundy)'
                        : isPredicted
                          ? 'var(--th-color-pink)'
                          : 'transparent',
                      color: isPeriod || isPredicted ? 'var(--th-color-text-on-accent)' : 'var(--th-color-text-primary)',
                      outline: isToday ? '2px solid var(--th-color-burgundy)' : 'none',
                      outlineOffset: '-2px',
                      opacity: isPredicted ? 0.7 : 1,
                    }}
                  >
                    {cell.day}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Legend */}
          <div className="th-card" style={{ marginTop: 'var(--th-space-4)', padding: 'var(--th-space-4)' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-2)', fontSize: 'var(--th-font-size-sm)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--th-space-2)' }}>
                <span style={{ width: 16, height: 16, borderRadius: '50%', background: 'var(--th-color-burgundy)', display: 'inline-block' }} />
                Logged period day
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--th-space-2)' }}>
                <span style={{ width: 16, height: 16, borderRadius: '50%', background: 'var(--th-color-pink)', opacity: 0.7, display: 'inline-block' }} />
                Estimated upcoming period
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--th-space-2)' }}>
                <span style={{ width: 16, height: 16, borderRadius: '50%', outline: '2px solid var(--th-color-burgundy)', outlineOffset: '-2px', display: 'inline-block' }} />
                Today
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
