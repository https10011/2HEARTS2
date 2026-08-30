/**
 * UsScreen (Phase 6, redesigned Phase 24, productized Stage 4).
 *
 * The relationship hub — the couple's shared world. Composition
 * (reference 10-Us-SharedSpace.png, adapted to Phase 24 navigation):
 *   1. Couple card — the two partners' avatar circles joined by a heart,
 *      over the brand's floral signature; deep-links to each profile.
 *   2. Together card — relationship duration with human start date and
 *      next-anniversary note; opens the dedicated Relationship Counter.
 *   3. "Coming up" — the next few important dates with a see-all link, or a
 *      warm invitation to record the first date.
 *   4. Our story / Our world — the centralized couple-hub feature groups
 *      (navConfig.COUPLE_HUB_GROUPS) exactly as Phase 24 defines them.
 *   5. Our profiles — owner & partner cards with birthday where known.
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService, RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import type { Profile } from '../../../data/relationship/relationshipTypes.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import {
  IconCalendar,
  IconChevronRight,
  IconSmile,
  LoadingState,
  RoseLilyDecoration,
} from '../../../components/index.ts';
import { COUPLE_HUB_GROUPS } from '../navConfig.ts';
import { NavIcon } from '../navIcons.tsx';
import { CouplePair } from '../couplePair.tsx';

function formatBirthDate(dateStr: string | null): string {
  if (!dateStr) return '';
  const [year, month, day] = dateStr.split('-').map(Number);
  const d = new Date(Date.UTC(year, month - 1, day));
  return d.toLocaleDateString(undefined, { month: 'long', day: 'numeric' });
}

function formatStartDate(dateKey: string): string {
  const [year, month, day] = dateKey.split('-').map(Number);
  const d = new Date(Date.UTC(year, month - 1, day));
  return d.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
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
      } catch (e) {
        // Graceful degradation
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => { cancelled = true; };
  }, []);

  if (loading) {
    return <LoadingState label="Loading your world…" />;
  }

  const age = summary?.decomposedAge;
  const ageParts: string[] = [];
  if (age) {
    if (age.years > 0) ageParts.push(`${age.years} ${age.years === 1 ? 'year' : 'years'}`);
    if (age.months > 0) ageParts.push(`${age.months} ${age.months === 1 ? 'month' : 'months'}`);
    if (ageParts.length === 0 || age.days > 0) ageParts.push(`${age.days} ${age.days === 1 ? 'day' : 'days'}`);
  }
  const anniversaryNote =
    summary?.daysUntilNextAnniversary === 0
      ? 'Your anniversary is today!'
      : summary?.daysUntilNextAnniversary != null && summary.daysUntilNextAnniversary > 0
        ? `Next anniversary in ${summary.daysUntilNextAnniversary} ${summary.daysUntilNextAnniversary === 1 ? 'day' : 'days'}`
        : null;

  return (
    <div className="th-content-pad th-screen-warm">
      {/* Floral signature for the relationship hub (Phase 27) */}
      <RoseLilyDecoration variant={5} size={100} position="top-right" opacity={0.2} animated />

      <div className="th-screen-header--enhanced">
        <div>
          <h1 className="th-screen-title">Us</h1>
          <p className="th-screen-subtitle" style={{ marginTop: 'var(--th-space-1)' }}>
            Our shared world
          </p>
        </div>
      </div>

      {/* Couple card — the two people at the heart of the hub */}
      <div className="th-couple-card">
        <RoseLilyDecoration variant={3} size={72} position="bottom-right" opacity={0.28} />
        <CouplePair
          owner={summary?.owner ?? null}
          partner={summary?.partner ?? null}
          ownerTo={RoutePath.appMoreSettingsProfile}
          partnerTo={RoutePath.appMoreSettingsRelationship}
        />
      </div>

      {/* Together card — duration hero, opens the Relationship Counter */}
      <Link
        to={RoutePath.appUsCounter}
        className="th-together-hero th-card-emotional"
        aria-label="Open relationship counter"
      >
        <div className="th-together-hero__icon">
          <IconCalendar size={22} />
        </div>
        <div className="th-together-hero__body">
          {ageParts.length > 0 ? (
            <>
              <span className="th-together-hero__label">Together for</span>
              <span className="th-together-hero__days">{ageParts.join(', ')}</span>
              {summary?.startDate && (
                <span className="th-together-hero__since">Since {formatStartDate(summary.startDate)}</span>
              )}
            </>
          ) : summary?.startDate ? (
            <>
              <span className="th-together-hero__label">Your story started</span>
              <span className="th-together-hero__days">{formatStartDate(summary.startDate)}</span>
            </>
          ) : (
            <>
              <span className="th-together-hero__label">Your story starts here</span>
              <span className="th-together-hero__since">
                Set your start date to unlock the counter
              </span>
            </>
          )}
          {anniversaryNote && (
            <span className="th-together-hero__anniv">{anniversaryNote}</span>
          )}
        </div>
        <IconChevronRight size={18} className="th-together-hero__chevron" />
      </Link>

      {/* Coming up — the relationship's next dates */}
      {summary && (
        <div className="th-us-dates" style={{ marginBottom: 'var(--th-space-6)' }}>
          <div className="th-hub-section-head">
            <h2 className="th-hub-section-title">Coming up</h2>
            <Link to={RoutePath.appUsReminders} className="th-hub-see-all">
              See all <IconChevronRight size={14} />
            </Link>
          </div>
          {summary.upcomingDates.length > 0 ? (
            <div className="th-hub-grid">
              {summary.upcomingDates.slice(0, 3).map((d) => (
                <Link
                  key={d.date + d.title}
                  to={RoutePath.appUsReminders}
                  className="th-feature-card th-feature-card--enhanced"
                >
                  <div className="th-feature-card__icon">
                    <IconCalendar size={20} />
                  </div>
                  <div className="th-feature-card__body">
                    <div className="th-feature-card__title">{d.title}</div>
                    <div className="th-feature-card__desc">
                      {d.daysUntil === 0 ? 'Today!' : `In ${d.daysUntil} ${d.daysUntil === 1 ? 'day' : 'days'}`}
                      {d.recurrence === 'yearly' ? ' · Yearly' : ''}
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <Link to={RoutePath.appUsReminders} className="th-us-dates-invite">
              <span className="th-us-dates-invite__icon">
                <IconSmile size={20} />
              </span>
              <span>
                No dates yet — add the moments that matter
                <span className="th-us-dates-invite__hint">Anniversaries, birthdays, first trips…</span>
              </span>
              <IconChevronRight size={18} />
            </Link>
          )}
        </div>
      )}

      {/* Relationship features — grouped: Our story / Our world (Phase 24 contract) */}
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

      {/* Our profiles */}
      {summary && (summary.owner || summary.partner) && (
        <div className="th-us-profiles" style={{ marginBottom: 'var(--th-space-6)' }}>
          <h2 className="th-hub-section-title">Our profiles</h2>
          <div className="th-us-profiles-grid">
            {summary.owner && <ProfileCard profile={summary.owner} />}
            {summary.partner && <ProfileCard profile={summary.partner} />}
          </div>
        </div>
      )}
    </div>
  );
}
