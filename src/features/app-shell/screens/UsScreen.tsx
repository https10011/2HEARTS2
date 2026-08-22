/**
 * UsScreen (Phase 6, redesigned Phase 24) — the central TwoHearts couple hub.
 *
 * Everything primarily about the couple lives here ("Our World"):
 * the relationship counter, anniversary/important dates, profiles, and the
 * relationship-specific features — memories, timeline, places, mood, period
 * tracker, and the private vault (navConfig.COUPLE_HUB_GROUPS).
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService, RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import type { Profile } from '../../../data/relationship/relationshipTypes.ts';
import { IconChevronRight, IconCalendar, LoadingState, RoseLilyDecoration } from '../../../components/index.ts';
import { COUPLE_HUB_GROUPS } from '../navConfig.ts';
import { NavIcon } from '../navIcons.tsx';

function formatBirthDate(dateStr: string | null): string {
  if (!dateStr) return '';
  const [year, month, day] = dateStr.split('-').map(Number);
  const d = new Date(Date.UTC(year, month - 1, day));
  return d.toLocaleDateString(undefined, { month: 'long', day: 'numeric' });
}

function ProfileCard({ profile }: { profile: Profile }) {
  const initial = profile.displayName.charAt(0).toUpperCase();
  return (
    <div className="th-profile-card th-profile-card--enhanced">
      <div className="th-profile-card__avatar">
        <span className="th-profile-card__initial">{initial}</span>
      </div>
      <div className="th-profile-card__info">
        <div className="th-profile-card__name">{profile.displayName}</div>
        {profile.birthDate && (
          <div className="th-profile-card__detail">
            Birthday: {formatBirthDate(profile.birthDate)}
          </div>
        )}
      </div>
    </div>
  );
}

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
        // Graceful degradation
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

  const age = summary?.decomposedAge;
  const ageParts: string[] = [];
  if (age) {
    if (age.years > 0) ageParts.push(`${age.years} ${age.years === 1 ? 'year' : 'years'}`);
    if (age.months > 0) ageParts.push(`${age.months} ${age.months === 1 ? 'month' : 'months'}`);
    if (ageParts.length === 0 || age.days > 0) ageParts.push(`${age.days} ${age.days === 1 ? 'day' : 'days'}`);
  }

  return (
    <div className="th-content-pad th-screen-warm">
      {/* Subtle floral accent for the relationship hub (Phase 27) */}
      <RoseLilyDecoration variant={5} size={100} position="top-right" opacity={0.2} animated />

      <div className="th-screen-header--enhanced">
        <div>
          <h1 className="th-screen-title">Us</h1>
          <p className="th-screen-subtitle" style={{ marginTop: 'var(--th-space-1)' }}>
            Your relationship world
          </p>
        </div>
      </div>

      {/* Relationship counter */}
      {summary && (
        <div className="th-relationship-card th-relationship-card--enhanced th-card-emotional" style={{ marginBottom: 'var(--th-space-6)' }}>
          {summary.owner && summary.partner ? (
            <>
              <div className="th-relationship-card__title">
                {summary.owner.displayName} &amp; {summary.partner.displayName}
              </div>
              <div className="th-relationship-card__subtitle">
                {ageParts.length > 0
                  ? `${ageParts.join(', ')} together`
                  : summary.startDate
                    ? `Together since ${summary.startDate}`
                    : 'Your journey together'}
                {summary.nextAnniversary && summary.daysUntilNextAnniversary !== null && summary.daysUntilNextAnniversary > 0 && (
                  <> · Next anniversary in {summary.daysUntilNextAnniversary} {summary.daysUntilNextAnniversary === 1 ? 'day' : 'days'}</>
                )}
                {summary.daysUntilNextAnniversary === 0 && (
                  <> · Today is your anniversary!</>
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

      {/* Relationship features — grouped: Our story / Our world */}
      {COUPLE_HUB_GROUPS.map((group) => (
        <section key={group.id} style={{ marginBottom: 'var(--th-space-6)' }}>
          <h2 className="th-hub-section-title">{group.title}</h2>
          <div className="th-hub-grid">
            {group.items.map((item) => (
              <Link key={item.id} to={item.route} className="th-feature-card th-feature-card--enhanced th-stagger-item">
                <div className="th-feature-card__icon">
                  <NavIcon icon={item.icon} size={20} />
                </div>
                <div className="th-feature-card__body">
                  <div className="th-feature-card__title">{item.label}</div>
                  <div className="th-feature-card__desc">{item.caption}</div>
                </div>
                <IconChevronRight size={18} className="th-feature-card__chevron" />
              </Link>
            ))}
          </div>
        </section>
      ))}

      {/* Partner profiles */}
      {summary && (summary.owner || summary.partner) && (
        <div className="th-us-profiles" style={{ marginBottom: 'var(--th-space-6)' }}>
          <h2 className="th-hub-section-title">Profiles</h2>
          <div className="th-us-profiles-grid">
            {summary.owner && <ProfileCard profile={summary.owner} />}
            {summary.partner && <ProfileCard profile={summary.partner} />}
          </div>
        </div>
      )}

      {/* Upcoming important dates */}
      {summary && summary.upcomingDates.length > 0 && (
        <div className="th-us-dates" style={{ marginBottom: 'var(--th-space-6)' }}>
          <h2 className="th-hub-section-title">Coming up</h2>
          <div className="th-hub-grid">
            {summary.upcomingDates.slice(0, 3).map((d) => (
              <div key={d.date + d.title} className="th-feature-card th-feature-card--enhanced">
                <div className="th-feature-card__icon">
                  <IconCalendar size={20} />
                </div>
                <div className="th-feature-card__body">
                  <div className="th-feature-card__title">{d.title}</div>
                  <div className="th-feature-card__desc">
                    {d.recurrence === 'yearly' && <span className="th-milestone-dot" />}
                    {d.daysUntil === 0 ? 'Today!' : `In ${d.daysUntil} ${d.daysUntil === 1 ? 'day' : 'days'}`}
                    {d.recurrence === 'yearly' ? ' · yearly' : ''}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
