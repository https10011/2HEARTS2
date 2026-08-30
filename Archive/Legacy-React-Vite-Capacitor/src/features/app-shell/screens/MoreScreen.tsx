/**
 * MoreScreen (Phase 6, refined Phase 24, productized Stage 3).
 *
 * Application utilities only: Settings, Search, About (navConfig.MORE_ITEMS).
 * Relationship destinations live in the central TwoHearts hub; everyday
 * destinations live in the bottom navigation — nothing duplicated here.
 *
 * Stage 3 presentation (reference 11-More.png, adapted): a burgundy hero
 * band with the official lily artwork, the owner's profile card (into
 * profile settings), the grouped utility rows, and a quiet version footer —
 * so More feels like an intentional part of TwoHearts, not a link list.
 */

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService, RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import { RoutePath } from '../../../navigation/routes.ts';
import { APP_INFO } from '../../../config/appInfo.ts';
import { IconChevronRight, IconSmile, RoseLilyDecoration } from '../../../components/index.ts';
import { MORE_ITEMS } from '../navConfig.ts';
import { NavIcon } from '../navIcons.tsx';

export function MoreScreen() {
  const [summary, setSummary] = useState<RelationshipSummary | null>(null);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      const svc = coreServices.relationship as RelationshipService | undefined;
      if (!svc) return;
      try {
        const s = await svc.getSummary();
        if (!cancelled) setSummary(s);
      } catch {
        // Graceful degradation — the profile card falls back to "You".
      }
    };
    load();
    return () => { cancelled = true; };
  }, []);

  const owner = summary?.owner ?? null;
  const ownerName = owner?.displayName?.trim() || 'You';

  return (
    <div className="th-more th-content-pad">
      {/* Burgundy hero band — brand-depth header with the official lily */}
      <header className="th-more-hero">
        <div className="th-more-hero__text">
          <h1 className="th-more-hero__title">More</h1>
          <p className="th-more-hero__subtitle">More ways to make TwoHearts yours</p>
        </div>
        <RoseLilyDecoration
          variant={11}
          size={96}
          position="top-right"
          opacity={0.45}
          className="th-more-hero__floral"
        />
      </header>

      {/* Owner profile card — the person behind the space */}
      <Link
        to={RoutePath.appMoreSettingsProfile}
        className="th-more-profile th-stagger-item"
        aria-label="Your profile and preferences"
      >
        <span className="th-more-profile__avatar" aria-hidden="true">
          {owner ? (
            <span className="th-more-profile__initial">{ownerName.charAt(0).toUpperCase()}</span>
          ) : (
            <IconSmile size={26} />
          )}
        </span>
        <span className="th-more-profile__body">
          <span className="th-more-profile__name">{ownerName}</span>
          <span className="th-more-profile__caption">Your TwoHearts space</span>
          <span className="th-more-profile__chip">
            <IconSmile size={13} />
            Profile &amp; Preferences
          </span>
        </span>
        <IconChevronRight size={18} className="th-more-profile__chevron" />
      </Link>

      {/* Application utilities */}
      <h2 className="th-hub-section-title" style={{ marginTop: 'var(--th-space-6)' }}>App</h2>
      <div className="th-hub-grid--enhanced">
        {MORE_ITEMS.map((item, i) => (
          <Link key={item.id} to={item.route} className="th-more-item th-more-item--enhanced th-stagger-item" style={{ animationDelay: `${i * 40}ms` }}>
            <div className="th-more-item__icon">
              <NavIcon icon={item.icon} size={20} />
            </div>
            <div className="th-more-item__body">
              <span className="th-more-item__label">{item.label}</span>
              <span className="th-more-item__caption">{item.caption}</span>
            </div>
            <IconChevronRight size={18} className="th-more-item__chevron" />
          </Link>
        ))}
      </div>

      {/* Quiet brand sign-off */}
      <footer className="th-more-footer">
        {APP_INFO.name} <span aria-hidden="true">•</span> Version {APP_INFO.version}
      </footer>
    </div>
  );
}
