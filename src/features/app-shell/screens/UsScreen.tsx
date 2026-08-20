/**
 * UsScreen (Phase 6).
 *
 * Relationship hub showing partner profiles, anniversary countdown,
 * and future feature entry points (memories, timeline, etc.).
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService } from '../../../services/relationship/relationshipService.ts';
import type { RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import { IconChevronRight, LoadingState } from '../../../components/index.ts';

export function UsScreen() {
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
        // Graceful degradation — empty state below
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return <LoadingState label="Loading relationship…" />;
  }

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-6)' }}>Us</h1>

      {/* Relationship card */}
      {summary && (
        <div className="th-relationship-card" style={{ marginBottom: 'var(--th-space-6)' }}>
          {summary.owner && summary.partner ? (
            <>
              <div className="th-relationship-card__title">
                {summary.owner.displayName} &amp; {summary.partner.displayName}
              </div>
              <div className="th-relationship-card__subtitle">
                {summary.ageDays !== null
                  ? `${summary.ageDays} days together`
                  : 'Your journey together'}
                {summary.nextAnniversary && summary.daysUntilNextAnniversary !== null && (
                  <> · Next anniversary in {summary.daysUntilNextAnniversary} {summary.daysUntilNextAnniversary === 1 ? 'day' : 'days'}</>
                )}
              </div>
            </>
          ) : (
            <>
              <div className="th-relationship-card__title">Your relationship</div>
              <div className="th-relationship-card__subtitle">
                {summary.startDate ? `Together since ${summary.startDate}` : 'Set up your relationship details'}
              </div>
            </>
          )}
        </div>
      )}

      {/* Future feature entries (placeholders for Phase 7+) */}
      <div className="th-hub-grid">
        <Link to={RoutePath.appUsMemories} className="th-feature-card">
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Memories</div>
            <div className="th-feature-card__desc">Photos &amp; moments together</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>

        <Link to={RoutePath.appUsTimeline} className="th-feature-card">
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Timeline</div>
            <div className="th-feature-card__desc">Your story over time</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>

        <Link to={RoutePath.appUsReminders} className="th-feature-card">
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Reminders</div>
            <div className="th-feature-card__desc">Important dates &amp; occasions</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>
      </div>
    </div>
  );
}
