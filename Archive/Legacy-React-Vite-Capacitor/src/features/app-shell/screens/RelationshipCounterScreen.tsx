/**
 * RelationshipCounterScreen (Stage 4) — the couple's "together" artifact.
 *
 * Composition (reference 15-Relationship-Counter.png):
 *   1. Couple header — the two partners joined by a heart over the brand's
 *      floral signature.
 *   2. Counter card — "Together for" + the full day count, the calendar
 *      decomposition (years · months · days), and the human start date.
 *   3. Stats strip — days / hours / minutes, whole-day semantics only.
 *   4. Next milestone — nearest hundred-day or round-anniversary mark with
 *      a quiet progress bar (pure helper: relationshipCounter.ts).
 *   5. Important dates link + "Add a memory" story CTA.
 *
 * Empty state (no start date): a warm invitation to set it — never a bare
 * "no data" panel.
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService, RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import {
  IconCalendar,
  IconChevronRight,
  IconHeart,
  IconImage,
  IconPlus,
  LoadingState,
  RoseLilyDecoration,
} from '../../../components/index.ts';
import { CouplePair } from '../couplePair.tsx';
import {
  decomposedSentence,
  formatDateKeyLong,
  hoursMinutesFromDays,
  nextMilestone,
} from '../relationshipCounter.ts';

export function RelationshipCounterScreen() {
  const [summary, setSummary] = useState<RelationshipSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      const svc = coreServices.relationship as RelationshipService | undefined;
      if (!svc) {
        if (!cancelled) setLoading(false);
        return;
      }
      try {
        const s = await svc.getSummary();
        if (!cancelled) setSummary(s);
      } catch {
        // Graceful degradation
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return <LoadingState label="Counting your days…" />;
  }

  const ageDays = summary?.ageDays ?? null;
  const hasStart = Boolean(summary?.startDate) && ageDays !== null;
  const milestone = hasStart ? nextMilestone(ageDays ?? 0) : null;
  const stats = hasStart && ageDays !== null ? hoursMinutesFromDays(ageDays) : null;
  const decomposition = decomposedSentence(summary?.decomposedAge ?? null);

  return (
    <div className="th-content-pad th-screen-warm th-counter-screen">
      <RoseLilyDecoration variant={6} size={110} position="top-right" opacity={0.3} animated />

      <div className="th-screen-header--enhanced">
        <div>
          <h1 className="th-screen-title">Relationship Counter</h1>
          <p className="th-screen-subtitle" style={{ marginTop: 'var(--th-space-1)' }}>
            Every day counts
          </p>
        </div>
      </div>

      {/* Couple header */}
      <div className="th-couple-card">
        <CouplePair
          owner={summary?.owner ?? null}
          partner={summary?.partner ?? null}
          ownerTo={RoutePath.appMoreSettingsProfile}
          partnerTo={RoutePath.appMoreSettingsRelationship}
        />
      </div>

      {hasStart && ageDays !== null && summary?.startDate ? (
        <>
          {/* Counter card */}
          <div className="th-counter-hero th-card-emotional">
            <span className="th-counter-hero__label">Together for</span>
            <span className="th-counter-hero__days">
              {ageDays.toLocaleString()} {ageDays === 1 ? 'day' : 'days'}
            </span>
            {decomposition && (
              <span className="th-counter-hero__decomp">{decomposition}</span>
            )}
            <span className="th-counter-hero__divider" aria-hidden="true" />
            <span className="th-counter-hero__since">
              <IconCalendar size={16} />
              Since {formatDateKeyLong(summary.startDate)}
            </span>
          </div>

          {/* Stats strip */}
          {stats && (
            <div className="th-counter-stats">
              <div className="th-counter-stats__item">
                <span className="th-counter-stats__value">{ageDays.toLocaleString()}</span>
                <span className="th-counter-stats__label">Days</span>
              </div>
              <div className="th-counter-stats__item">
                <span className="th-counter-stats__value">{stats.hours.toLocaleString()}</span>
                <span className="th-counter-stats__label">Hours</span>
              </div>
              <div className="th-counter-stats__item">
                <span className="th-counter-stats__value">{stats.minutes.toLocaleString()}</span>
                <span className="th-counter-stats__label">Minutes</span>
              </div>
            </div>
          )}

          {/* Next milestone */}
          {milestone && (
            <div className="th-counter-milestone">
              <span className="th-counter-milestone__icon" aria-hidden="true">
                <IconHeart size={20} />
              </span>
              <div className="th-counter-milestone__body">
                <span className="th-counter-milestone__title">Next milestone</span>
                <span className="th-counter-milestone__days">
                  {milestone.kind === 'anniversary' && milestone.days % 365 === 0
                    ? `${milestone.days / 365} ${milestone.days / 365 === 1 ? 'year' : 'years'}`
                    : `${milestone.days.toLocaleString()} days`}
                </span>
                <span className="th-counter-milestone__togo">
                  {milestone.daysToGo === 0
                    ? 'You reached it today — celebrate!'
                    : `${milestone.daysToGo} ${milestone.daysToGo === 1 ? 'day' : 'days'} to go`}
                </span>
                <div
                  className="th-counter-milestone__track"
                  role="progressbar"
                  aria-valuemin={0}
                  aria-valuemax={100}
                  aria-valuenow={Math.round(milestone.progress * 100)}
                  aria-label="Progress to next milestone"
                >
                  <span
                    className="th-counter-milestone__fill"
                    style={{ width: `${Math.max(4, milestone.progress * 100)}%` }}
                  />
                </div>
              </div>
            </div>
          )}

          {/* Important dates link */}
          <Link to={RoutePath.appUsReminders} className="th-counter-linkrow">
            <span className="th-counter-linkrow__icon">
              <IconCalendar size={20} />
            </span>
            <span className="th-counter-linkrow__body">
              <span className="th-counter-linkrow__title">Your important dates</span>
              <span className="th-counter-linkrow__desc">
                Keep anniversaries and meaningful dates in one place.
              </span>
            </span>
            <IconChevronRight size={18} className="th-counter-linkrow__chevron" />
          </Link>

          {/* Story CTA */}
          <div className="th-counter-cta th-card-emotional">
            <span className="th-counter-cta__icon" aria-hidden="true">
              <IconImage size={22} />
            </span>
            <h2 className="th-counter-cta__title">Keep building your story.</h2>
            <p className="th-counter-cta__desc">Add a memory to celebrate today.</p>
            <Link to={RoutePath.appMemoriesAdd} className="th-btn th-btn--primary">
              <IconPlus size={18} /> Add a memory
            </Link>
          </div>
        </>
      ) : (
        /* Empty — no start date yet */
        <div className="th-counter-empty th-card-emotional">
          <h2 className="th-counter-cta__title">Your story starts here</h2>
          <p className="th-counter-cta__desc">
            Set your relationship start date and the counter comes alive —
            every day since the day you began together.
          </p>
          <Link to={RoutePath.appMoreSettingsRelationship} className="th-btn th-btn--primary">
            <IconCalendar size={18} /> Set start date
          </Link>
        </div>
      )}
    </div>
  );
}
