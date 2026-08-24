/**
 * HomeScreen (Phase 6, redesigned Phase 24, productized Stage 3).
 *
 * Home is the couple's personal space — not a dashboard of every feature.
 *
 * Composition (reference 09-Home.png, adapted to the Phase 24 architecture):
 *   1. Greeting row — time-aware "Good morning, {name}" + date, with a
 *      notification bell (unread badge) at the top right.
 *   2. Couple centerpiece — the official TwoHearts lockup above the two
 *      partners' avatars, joined by a small burgundy heart; the relationship
 *      counter lives in a warm pill beneath (the emotional center of the app
 *      stays the two people).
 *   3. Everyday actions — the curated set (navConfig.HOME_PRIMARY_ITEMS) as
 *      horizontal cards: icon tile, title + caption, chevron.
 *   4. "From your story" — live previews (latest note, upcoming reminder,
 *      recent memory) deep-linking to their existing detail screens, or a
 *      warm invitation when the story is still empty. Vault is never shown.
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService, RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import type { Profile } from '../../../data/relationship/relationshipTypes.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import {
  BrandLogo,
  IconBell,
  IconChevronRight,
  IconHeart,
  IconSmile,
  LoadingState,
  RoseLilyDecoration,
} from '../../../components/index.ts';
import { HOME_PRIMARY_ITEMS } from '../navConfig.ts';
import { NavIcon } from '../navIcons.tsx';
import { greetingForHour } from '../homeHighlights.ts';
import { useHomeHighlights } from '../useHomeHighlights.ts';

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
          <IconSmile size={28} />
        )}
      </span>
      <span className="th-home-avatar__name">{name}</span>
    </Link>
  );
}

export function HomeScreen() {
  const [summary, setSummary] = useState<RelationshipSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const { highlights, unreadNotifications } = useHomeHighlights();

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

  const ownerName = summary?.owner?.displayName?.trim() ?? '';
  const now = new Date();
  const greeting = `${greetingForHour(now.getHours())}${ownerName ? `, ${ownerName}` : ''}`;
  const todayLine = now.toLocaleDateString(undefined, {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
  });

  return (
    <div className="th-home th-screen-warm">
      {/* Signature florals — one greeting the couple section, one ambient */}
      <RoseLilyDecoration
        variant={2}
        size={118}
        position="top-right"
        opacity={0.35}
        className="th-home-floral-top"
      />
      <RoseLilyDecoration variant={7} size={90} position="bottom-left" opacity={0.18} animated />

      {/* Greeting row — personal, time-aware, with notification access */}
      <div className="th-home-greeting">
        <div className="th-home-greeting__text">
          <p className="th-home-greeting__hello">{greeting}</p>
          <p className="th-home-greeting__date">{todayLine}</p>
        </div>
        <Link
          to={RoutePath.appNotifications}
          className="th-home-greeting__bell"
          aria-label={
            unreadNotifications > 0
              ? `Notifications, ${unreadNotifications} unread`
              : 'Notifications'
          }
        >
          <IconBell size={20} />
          {unreadNotifications > 0 && (
            <span className="th-home-greeting__bell-badge" aria-hidden="true" />
          )}
        </Link>
      </div>

      {/* Couple centerpiece — the two people joined around the brand */}
      <header className="th-home-header th-couple-header-backdrop">
        <BrandLogo variant="brand" size={92} title="TwoHearts" />
        <div className="th-home-couple-avatars">
          <AvatarChip
            profile={summary?.owner ?? null}
            fallbackName="You"
            to={RoutePath.appMoreSettingsProfile}
            label="Your profile"
          />
          <span className="th-home-couple-avatars__heart" aria-hidden="true">
            <IconHeart size={16} />
          </span>
          <AvatarChip
            profile={summary?.partner ?? null}
            fallbackName="Partner"
            to={RoutePath.appMoreSettingsRelationship}
            label="Partner profile"
          />
        </div>
        <p className="th-home-pill" aria-live="polite">
          {ageParts.length > 0
            ? `${ageParts.join(', ')} together${anniversaryNote ? ` · ${anniversaryNote}` : ''}`
            : 'Your private couple space'}
        </p>
      </header>

      {/* Everyday actions — curated, calm, exactly four */}
      <div className="th-home-grid">
        {HOME_PRIMARY_ITEMS.map((item, i) => (
          <Link key={item.id} to={item.route} className="th-home-card th-home-card--enhanced th-stagger-item" style={{ animationDelay: `${i * 50}ms` }}>
            <span className="th-home-card__icon">
              <NavIcon icon={item.icon} size={22} />
            </span>
            <span className="th-home-card__body">
              <span className="th-home-card__title">{item.label}</span>
              <span className="th-home-card__caption">{item.caption}</span>
            </span>
            <IconChevronRight size={16} className="th-home-card__chevron" />
          </Link>
        ))}
      </div>

      {/* From your story — living content previews, or a warm first invitation */}
      <section className="th-home-highlights" aria-label="From your story">
        <h2 className="th-home-highlights__title">From your story</h2>
        {highlights.length > 0 ? (
          <div className="th-home-highlights__list">
            {highlights.map((highlight, i) => (
              <Link
                key={`${highlight.kind}-${highlight.id}`}
                to={highlight.to}
                className="th-feature-card th-feature-card--enhanced th-stagger-item"
                style={{ animationDelay: `${i * 40}ms` }}
              >
                <div className="th-feature-card__icon">
                  <NavIcon icon={highlight.icon} size={20} />
                </div>
                <div className="th-feature-card__body">
                  <div className="th-home-highlight__label">{highlight.label}</div>
                  <div className="th-feature-card__title">{highlight.title}</div>
                  <div className="th-feature-card__desc">{highlight.meta}</div>
                </div>
                <IconChevronRight size={18} className="th-feature-card__chevron" />
              </Link>
            ))}
          </div>
        ) : (
          <Link to={RoutePath.appNotesAdd} className="th-home-invite th-stagger-item">
            <span className="th-home-invite__icon" aria-hidden="true">
              <IconHeart size={22} />
            </span>
            <span className="th-home-invite__body">
              <span className="th-home-invite__title">Your story begins here</span>
              <span className="th-home-invite__text">
                Save your first note, plan or memory — it will bloom right here.
              </span>
            </span>
            <IconChevronRight size={18} className="th-home-invite__chevron" />
          </Link>
        )}
      </section>
    </div>
  );
}
