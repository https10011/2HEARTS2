/**
 * HomeScreen (Phase 6, redesigned Phase 24).
 *
 * Home is the couple's personal space — not a dashboard of every feature.
 *
 * Relationship header: the owner's avatar on the left, the official
 * TwoHearts branding in the center, the partner's avatar on the right — the
 * two people visually connected around the brand. Avatars are rendered from
 * the existing profile domain (initial-based; tasteful IconSmile fallback
 * when a profile is missing) and stay live as profiles change.
 *
 * Primary content is the curated everyday set (navConfig.HOME_PRIMARY_ITEMS):
 * Notes · Reminders · Us · Games. Relationship archives (memories, timeline,
 * places, mood, period, vault) live in the central TwoHearts hub instead.
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService, RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import type { Profile } from '../../../data/relationship/relationshipTypes.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import { BrandLogo, IconSmile, LoadingState, RoseLilyDecoration } from '../../../components/index.ts';
import { HOME_PRIMARY_ITEMS } from '../navConfig.ts';
import { NavIcon } from '../navIcons.tsx';

interface AvatarChipProps {
  profile: Profile | null;
  fallbackName: string;
  to: string;
  label: string;
}

/** Relationship avatar: first letter of the name, or a soft face fallback. */
function AvatarChip({ profile, fallbackName, to, label }: AvatarChipProps) {
  const name = profile?.displayName?.trim() || fallbackName;
  return (
    <Link to={to} className="th-home-avatar" aria-label={label}>
      <span className="th-home-avatar__circle">
        {profile ? (
          <span className="th-home-avatar__initial">{name.charAt(0).toUpperCase()}</span>
        ) : (
          <IconSmile size={26} />
        )}
      </span>
      <span className="th-home-avatar__name">{name}</span>
    </Link>
  );
}

export function HomeScreen() {
  const [summary, setSummary] = useState<RelationshipSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    const loadDashboard = async () => {
      const svc = coreServices.relationship as RelationshipService | undefined;
      if (!svc) {
        if (!cancelled) setLoading(false);
        return;
      }
      try {
        const s = await svc.getSummary();
        if (!cancelled) setSummary(s);
      } catch {
        // Graceful degradation — Home still renders with avatar fallbacks.
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
  const anniversaryNote =
    summary?.daysUntilNextAnniversary === 0
      ? 'Today is your anniversary!'
      : summary?.daysUntilNextAnniversary !== null && summary?.daysUntilNextAnniversary !== undefined && summary.daysUntilNextAnniversary > 0
        ? `Anniversary in ${summary.daysUntilNextAnniversary} ${summary.daysUntilNextAnniversary === 1 ? 'day' : 'days'}`
        : null;

  return (
    <div className="th-home th-screen-warm">
      {/* Subtle corner floral — one variation, kept quiet (Phase 23 decor) */}
      <RoseLilyDecoration variant={2} size={118} position="top-right" opacity={0.35} />
      {/* Ambient floral bottom-left for warmth (Phase 27) */}
      <RoseLilyDecoration variant={7} size={90} position="bottom-left" opacity={0.18} animated />

      {/* Relationship header: the two people around the official branding */}
      <header className="th-home-header th-couple-header-backdrop">
        <AvatarChip
          profile={summary?.owner ?? null}
          fallbackName="You"
          to={RoutePath.appMoreSettingsProfile}
          label="Your profile"
        />
        <div className="th-home-header__brand">
          <BrandLogo variant="brand" size={92} title="TwoHearts" />
          <p className="th-home-header__line" aria-live="polite">
            {ageParts.length > 0
              ? `${ageParts.join(', ')} together${anniversaryNote ? ` · ${anniversaryNote}` : ''}`
              : 'Your private couple space'}
          </p>
        </div>
        <AvatarChip
          profile={summary?.partner ?? null}
          fallbackName="Partner"
          to={RoutePath.appMoreSettingsRelationship}
          label="Partner profile"
        />
      </header>

      {/* Everyday actions — curated, calm, exactly four (Phase 26 enhanced) */}
      <div className="th-home-grid">
        {HOME_PRIMARY_ITEMS.map((item, i) => (
          <Link key={item.id} to={item.route} className="th-home-card th-home-card--enhanced th-stagger-item" style={{ animationDelay: `${i * 50}ms` }}>
            <span className="th-home-card__icon">
              <NavIcon icon={item.icon} size={24} />
            </span>
            <span className="th-home-card__title">{item.label}</span>
            <span className="th-home-card__caption">{item.caption}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
