/**
 * HomeScreen (Phase 6).
 *
 * Main dashboard shown after onboarding completes. Displays a greeting,
 * relationship summary card, and quick-access feature cards.
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService } from '../../../services/relationship/relationshipService.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import { IconHeart, IconGamepad, IconFileText, IconChevronRight, LoadingState } from '../../../components/index.ts';

export function HomeScreen() {
  const [greeting, setGreeting] = useState<string | null>(null);
  const [relationshipDays, setRelationshipDays] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    const loadDashboard = async () => {
      const svc = coreServices.relationship as RelationshipService | undefined;
      if (!svc) {
        if (!cancelled) {
          setGreeting('Welcome to TwoHearts');
          setLoading(false);
        }
        return;
      }

      try {
        const summary = await svc.getSummary();
        if (cancelled) return;
        if (summary.owner) {
          setGreeting(`Hi, ${summary.owner.displayName}!`);
        } else {
          setGreeting('Welcome to TwoHearts');
        }
        if (summary.ageDays !== null) {
          setRelationshipDays(summary.ageDays);
        }
      } catch {
        if (!cancelled) setGreeting('Welcome to TwoHearts');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    loadDashboard();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return <LoadingState label="Loading your space…" />;
  }

  return (
    <div className="th-content-pad">
      {/* Greeting */}
      <div className="th-home-greeting">
        <h1 className="th-home-greeting__name">{greeting}</h1>
        <p className="th-home-greeting__subtitle">Your private couple space</p>
      </div>

      {/* Relationship summary card */}
      {relationshipDays !== null && (
        <div className="th-relationship-card" style={{ marginBottom: 'var(--th-space-6)' }}>
          <div className="th-relationship-card__title">
            {relationshipDays} {relationshipDays === 1 ? 'day' : 'days'} together
          </div>
          <div className="th-relationship-card__subtitle">
            Your journey continues every day
          </div>
        </div>
      )}

      {/* Quick-access feature cards */}
      <div className="th-hub-grid">
        <Link to={RoutePath.appUs} className="th-feature-card">
          <div className="th-feature-card__icon">
            <IconHeart size={22} />
          </div>
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Us</div>
            <div className="th-feature-card__desc">Your relationship hub</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>

        <Link to={RoutePath.appGames} className="th-feature-card">
          <div className="th-feature-card__icon">
            <IconGamepad size={22} />
          </div>
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Games</div>
            <div className="th-feature-card__desc">Play together</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>

        <Link to={RoutePath.appNotes} className="th-feature-card">
          <div className="th-feature-card__icon">
            <IconFileText size={22} />
          </div>
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Notes</div>
            <div className="th-feature-card__desc">Private notes &amp; thoughts</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>
      </div>
    </div>
  );
}
