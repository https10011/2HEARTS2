/**
 * BottomNav (Phase 6).
 *
 * Five-tab bottom navigation: Home, Us, Games, Notes, More.
 * Uses NavLink for automatic active-state highlighting.
 */

import { NavLink } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  IconHome,
  IconHeart,
  IconGamepad,
  IconFileText,
  IconMenu,
} from '../../components/index.ts';

interface NavItem {
  to: string;
  label: string;
  icon: typeof IconHome;
}

const NAV_ITEMS: NavItem[] = [
  { to: RoutePath.appHome, label: 'Home', icon: IconHome },
  { to: RoutePath.appUs, label: 'Us', icon: IconHeart },
  { to: RoutePath.appGames, label: 'Games', icon: IconGamepad },
  { to: RoutePath.appNotes, label: 'Notes', icon: IconFileText },
  { to: RoutePath.appMore, label: 'More', icon: IconMenu },
];

export function BottomNav() {
  return (
    <nav className="th-bottom-nav" role="navigation" aria-label="Main navigation">
      {NAV_ITEMS.map((item) => {
        const Icon = item.icon;
        return (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `th-bottom-nav-item${isActive ? ' th-bottom-nav-item--active' : ''}`
            }
            end={item.to === RoutePath.appHome}
          >
            <Icon size={22} />
            <span className="th-bottom-nav-label">{item.label}</span>
          </NavLink>
        );
      })}
    </nav>
  );
}
