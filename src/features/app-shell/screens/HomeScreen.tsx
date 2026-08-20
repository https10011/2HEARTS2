/**
 * HomeScreen (Phase 6).
 *
 * Main dashboard shown after onboarding completes. Displays a greeting,
 * relationship summary card, and quick-access feature cards.
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService, RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import { IconHeart, IconGamepad, IconFileText, IconChevronRight, IconCalendar, LoadingState } from '../../../components/index.ts';

export function HomeScreen() {
  const [greeting, setGreeting] = useState<string | null>(null);
  const [summary, setSummary] = useState<RelationshipSummary | null>(null);
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
        const s = await svc.getSummary();
        if (cancelled) return;
        setSummary(s);
        if (s.owner) {
          setGreeting(`Hi, ${s.owner.displayName}!`);
        } else {
          setGreeting('Welcome to TwoHearts');
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

  const age = summary?.decomposedAge;
  const ageParts: string[] = [];
  if (age) {
    if (age.years > 0) ageParts.push(`${age.years} ${age.years === 1 ? 'year' : 'years'}`);
    if (age.months > 0) ageParts.push(`${age.months} ${age.months === 1 ? 'month' : 'months'}`);
    if (ageParts.length === 0 || age.days > 0) ageParts.push(`${age.days} ${age.days === 1 ? 'day' : 'days'}`);
  }

  return (
    <div className="th-content-pad">
      {/* Greeting */}
      <div className="th-home-greeting">
        <h1 className="th-home-greeting__name">{greeting}</h1>
        <p className="th-home-greeting__subtitle">Your private couple space</p>
      </div>

      {/* Relationship summary card */}
      {age && (
        <div className="th-relationship-card" style={{ marginBottom: 'var(--th-space-6)' }}>
          <div className="th-relationship-card__title">
            {ageParts.join(', ')} together
          </div>
          <div className="th-relationship-card__subtitle">
            {summary?.nextAnniversary && summary.daysUntilNextAnniversary !== null && summary.daysUntilNextAnniversary > 0
              ? `Next anniversary in ${summary.daysUntilNextAnniversary} ${summary.daysUntilNextAnniversary === 1 ? 'day' : 'days'}`
              : summary?.daysUntilNextAnniversary === 0
                ? 'Today is your anniversary!'
                : 'Your journey continues every day'
            }
          </div>
        </div>
      )}

      {/* Upcoming important dates */}
      {summary && summary.upcomingDates.length > 0 && (
        <div className="th-home-section" style={{ marginBottom: 'var(--th-space-6)' }}>
          <h2 className="th-screen-subtitle" style={{ marginBottom: 'var(--th-space-3)' }}>Upcoming dates</h2>
          <div className="th-hub-grid">
            {summary.upcomingDates.slice(0, 3).map((d) => (
              <div key={d.date + d.title} className="th-feature-card" style={{ cursor: 'default' }}>
                <div className="th-feature-card__icon">
                  <IconCalendar size={20} />
                </div>
                <div className="th-feature-card__body">
                  <div className="th-feature-card__title">{d.title}</div>
                  <div className="th-feature-card__desc">
                    {d.daysUntil === 0 ? 'Today!' : `In ${d.daysUntil} ${d.daysUntil === 1 ? 'day' : 'days'}`}
                    {d.recurrence === 'yearly' ? ' · yearly' : ''}
                  </div>
                </div>
              </div>
            ))}
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
