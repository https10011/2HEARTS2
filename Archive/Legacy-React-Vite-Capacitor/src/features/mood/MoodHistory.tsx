/**
 * MoodHistory (Phase 15, productized in Stage 10).
 *
 * The couple's mood story over time — range filters (week / month / all),
 * a summary card with the real mood distribution, and a chronological
 * timeline grouped by month. Every count is computed from persisted
 * entries; nothing is fabricated. Rows open the check-in for editing.
 */

import { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { useMoodService } from './useMoodService.ts';
import { MoodIcon } from './moodMeta.tsx';
import {
  type MoodRange,
  buildMoodMonths,
  filterByRange,
  formatMoodDay,
  localDateKey,
  summarizeMoods,
  summaryHeadline,
} from './moodPresentation.ts';
import type { MoodEntry } from '../../data/mood/moodTypes.ts';
import { MOOD_LABELS } from '../../data/mood/moodTypes.ts';
import {
  Button,
  IconBack,
  IconButton,
  IconChevronRight,
  IconHeart,
  IconPlus,
  LoadingState,
  RoseLilyDecoration,
} from '../../components/index.ts';

const RANGE_OPTIONS: Array<{ value: MoodRange; label: string }> = [
  { value: 'week', label: 'This week' },
  { value: 'month', label: 'This month' },
  { value: 'all', label: 'All time' },
];

export function MoodHistory() {
  const navigate = useNavigate();
  const service = useMoodService();

  const [entries, setEntries] = useState<MoodEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [range, setRange] = useState<MoodRange>('all');

  const today = localDateKey();

  const loadData = useCallback(async () => {
    if (!service) return;
    setLoading(true);
    setError(null);
    try {
      const data = await service.list();
      setEntries(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load mood history.');
    } finally {
      setLoading(false);
    }
  }, [service]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const visible = useMemo(
    () => filterByRange(entries, range, today),
    [entries, range, today],
  );
  const summary = useMemo(() => summarizeMoods(visible), [visible]);
  const months = useMemo(() => buildMoodMonths(visible), [visible]);
  const headline = summaryHeadline(summary);

  if (loading || !service) {
    return <LoadingState label="Gathering your check-ins…" />;
  }

  const header = (
    <header className="th-mood-header">
      <IconButton label="Go back" onClick={() => navigate(-1)}>
        <IconBack />
      </IconButton>
      <div className="th-mood-header__copy">
        <h1 className="th-mood-title">Mood history</h1>
        <p className="th-mood-subtitle">How you&apos;ve been feeling, day by day.</p>
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
          <p>We couldn&apos;t load your history just now.</p>
          <Button variant="secondary" onClick={loadData}>Try again</Button>
        </div>
      </div>
    );
  }

  if (entries.length === 0) {
    return (
      <div className="th-content-pad th-screen-warm">
        <RoseLilyDecoration variant={5} size={100} position="bottom-left" opacity={0.1} />
        {header}
        <div className="th-empty-emotional">
          <div className="th-empty-emotional__visual th-scale-in">
            <IconHeart size={38} />
          </div>
          <h3 className="th-empty-emotional__title">Your story starts here</h3>
          <p className="th-empty-emotional__message">
            Check in each day and this page becomes a gentle record of how
            you&apos;ve both been feeling.
          </p>
          <div className="th-empty-emotional__action">
            <Button variant="primary" onClick={() => navigate(RoutePath.appMoodAdd)}>
              <IconPlus size={18} /> Make your first check-in
            </Button>
          </div>
        </div>
      </div>
    );
  }

  const maxCount = summary.distribution[0]?.count ?? 0;

  return (
    <div className="th-content-pad th-screen-warm">
      <RoseLilyDecoration variant={14} size={110} position="top-right" opacity={0.1} />
      {header}

      {/* Range filter */}
      <div className="th-mood-chips" role="group" aria-label="Filter by time range">
        {RANGE_OPTIONS.map((option) => (
          <button
            key={option.value}
            type="button"
            className={`th-option-chip ${range === option.value ? 'th-option-chip--active' : ''}`}
            aria-pressed={range === option.value}
            onClick={() => setRange(option.value)}
          >
            {option.label}
          </button>
        ))}
      </div>

      {visible.length === 0 ? (
        <p className="th-mood-filter-empty">No check-ins in this period yet.</p>
      ) : (
        <>
          {/* Real distribution summary */}
          <section className="th-mood-summary th-stagger-item" aria-label="Mood summary">
            {headline && <h2 className="th-mood-summary__headline">{headline}</h2>}
            <p className="th-mood-summary__count">
              {summary.total} {summary.total === 1 ? 'check-in' : 'check-ins'}
            </p>
            <div className="th-mood-dist">
              {summary.distribution.map(({ mood, count }) => (
                <div className="th-mood-dist__row" key={mood}>
                  <span className="th-mood-dist__label">{MOOD_LABELS[mood]}</span>
                  <span className="th-mood-dist__track" aria-hidden="true">
                    <span
                      className="th-mood-dist__fill"
                      style={{ width: `${maxCount > 0 ? (count / maxCount) * 100 : 0}%` }}
                    />
                  </span>
                  <span className="th-mood-dist__count">{count}</span>
                </div>
              ))}
            </div>
          </section>

          {/* Timeline */}
          {months.map((group) => (
            <section key={group.monthLabel} aria-label={group.monthLabel}>
              <h2 className="th-mood-month">{group.monthLabel}</h2>
              <div className="th-mood-list">
                {group.entries.map((entry) => (
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
                        <span className="th-mood-row__date">
                          {formatMoodDay(entry.entryDate, today)}
                        </span>
                      </span>
                      {entry.note && (
                        <span className="th-mood-row__note">&ldquo;{entry.note}&rdquo;</span>
                      )}
                    </span>
                    <span className="th-mood-row__chevron" aria-hidden="true">
                      <IconChevronRight size={18} />
                    </span>
                  </button>
                ))}
              </div>
            </section>
          ))}
        </>
      )}

      {/* Closing invitation */}
      <section className="th-mood-cta th-stagger-item">
        <div className="th-mood-cta__copy">
          <h2 className="th-mood-cta__title">How do you feel right now?</h2>
          <p className="th-mood-cta__text">Add today&apos;s check-in — it only takes a moment.</p>
        </div>
        <Button variant="primary" onClick={() => navigate(RoutePath.appMoodAdd)}>
          Check in
        </Button>
      </section>
    </div>
  );
}
