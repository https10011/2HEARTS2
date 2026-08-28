/**
 * HomeScreen (Phase 6, redesigned Phase 24, productized Stage 3).
 *
 * Home is the couple's personal space — not a dashboard of every feature.
 *
 * Composition (reference 09-Home.png, adapted to the Phase 24 architecture):
 *   1. Greeting row — time-aware "Good morning, {name}" + date, with a
 *      notification bell (unread badge) at the top right.
 *   2. Couple centerpiece — two prominent avatar circles flanking the official
 *      TwoHearts brand mark, with names beneath and the relationship counter
 *      in a warm pill. The avatars feel like PEOPLE; the mark connects them.
 *   3. Everyday actions — the curated set (navConfig.HOME_PRIMARY_ITEMS) as
 *      horizontal cards: icon tile, title + caption, chevron.
 *   4. "From your story" — live previews (latest note, upcoming reminder,
 *      recent memory) deep-linking to their existing detail screens, or a
 *      warm invitation when the story is still empty. Vault is never shown.
 *
 * Stage 3 redesign: couple header now uses the BrandLogo mark as the visual
 * connector between two large avatars. The composition communicates
 * "TWO PEOPLE + THEIR RELATIONSHIP + THEIR SHARED SPACE".
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService, RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import {
  BrandLogo,
  IconBell,
  IconChevronRight,
  IconHeart,
  ProfileAvatar,
  LoadingState,
  RoseLilyDecoration,
} from '../../../components/index.ts';
import { HOME_PRIMARY_ITEMS } from '../navConfig.ts';
import { NavIcon } from '../navIcons.tsx';
import { greetingForHour } from '../homeHighlights.ts';
import { useHomeHighlights } from '../useHomeHighlights.ts';
import { useProfilePhotos } from '../useProfilePhotos.ts';

export function HomeScreen() {
  const [summary, setSummary] = useState<RelationshipSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const { highlights, unreadNotifications } = useHomeHighlights();
  const { ownerUrl, partnerUrl } = useProfilePhotos();

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

      {/* Couple centerpiece — two people flanking the brand mark */}
      <header className="th-home-header th-couple-header-backdrop">
        <div className="th-home-couple">
          <Link
            to={RoutePath.appMoreSettingsProfile}
            className="th-home-couple__avatar"
            aria-label="Your profile"
          >
            <ProfileAvatar
              name={summary?.owner?.displayName?.trim() || 'You'}
              photoUrl={ownerUrl}
              size={80}
            />
            <span className="th-home-couple__name">
              {summary?.owner?.displayName?.trim() || 'You'}
            </span>
          </Link>

          <div className="th-home-couple__mark" aria-hidden="true">
            <BrandLogo variant="mark" size={44} />
          </div>

          <Link
            to={RoutePath.appMoreSettingsRelationship}
            className="th-home-couple__avatar"
            aria-label="Partner profile"
          >
            <ProfileAvatar
              name={summary?.partner?.displayName?.trim() || 'Partner'}
              photoUrl={partnerUrl}
              size={80}
            />
            <span className="th-home-couple__name">
              {summary?.partner?.displayName?.trim() || 'Partner'}
            </span>
          </Link>
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
