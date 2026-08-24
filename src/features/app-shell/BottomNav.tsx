/**
 * BottomNav (Phase 6, redesigned Phase 24).
 *
 * Five-position global navigation:
 *   Home · Notifications · TWOHEARTS · Notes · More
 *
 * The CENTER position is the relationship itself — an elevated circular
 * button carrying the official TwoHearts mark (BrandLogo, one source) that
 * opens the couple hub. Destinations come from navConfig.ts (one vocabulary
 * source); icons route through navIcons.tsx (one bridge to the Icon set).
 */

import { NavLink } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { BrandLogo } from '../../components/BrandLogo.tsx';
import { BOTTOM_NAV_ITEMS, type NavDestination } from './navConfig.ts';
import { NavIcon } from './navIcons.tsx';

const CENTER_ARIA = 'TwoHearts — your relationship space';

function SideNavItem({ item }: { item: NavDestination }) {
  return (
    <NavLink
      to={item.route}
      end={item.route === RoutePath.appHome}
      className={({ isActive }) =>
        `th-bottom-nav-item${isActive ? ' th-bottom-nav-item--active' : ''}`
      }
    >
      <span className="th-bottom-nav-icon" aria-hidden="true">
        <NavIcon icon={item.icon} size={22} />
      </span>
      <span className="th-bottom-nav-label">{item.label}</span>
      <span className="th-bottom-nav-dot" aria-hidden="true" />
    </NavLink>
  );
}

function CenterNavItem({ item }: { item: NavDestination }) {
  return (
    <NavLink
      to={item.route}
      aria-label={CENTER_ARIA}
      className={({ isActive }) =>
        `th-bottom-nav-item th-bottom-nav-item--center${
          isActive ? ' th-bottom-nav-item--active' : ''
        }`
      }
    >
      <span className="th-bottom-nav-center__ring">
        <BrandLogo variant="mark" size={32} tone="light" />
      </span>
      <span className="th-bottom-nav-label">{item.label}</span>
    </NavLink>
  );
}

export function BottomNav() {
  return (
    <nav className="th-bottom-nav" role="navigation" aria-label="Main navigation">
      {BOTTOM_NAV_ITEMS.map((item) =>
        item.center ? (
          <CenterNavItem key={item.id} item={item} />
        ) : (
          <SideNavItem key={item.id} item={item} />
        ),
      )}
    </nav>
  );
}
