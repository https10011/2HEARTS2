/**
 * MoreScreen (Phase 6).
 *
 * More menu — additional app functions: settings, search, vault,
 * reminders, about. Feature implementations arrive in later phases;
 * this provides the navigation structure and visual consistency.
 */

import { Link } from 'react-router-dom';
import { RoutePath } from '../../../navigation/routes.ts';
import {
  IconSettings,
  IconSearch,
  IconHeart,
  IconMapPin,
  IconChevronRight,
  IconBell,
} from '../../../components/index.ts';

export function MoreScreen() {
  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-6)' }}>More</h1>

      <div style={{ display: 'flex', flexDirection: 'column' }}>
        <Link to={RoutePath.appMoreSettings} className="th-more-item">
          <div className="th-more-item__icon">
            <IconSettings size={20} />
          </div>
          <span className="th-more-item__label">Settings</span>
          <IconChevronRight size={18} className="th-more-item__chevron" />
        </Link>

        <Link to={RoutePath.appPlaces} className="th-more-item">
          <div className="th-more-item__icon">
            <IconMapPin size={20} />
          </div>
          <span className="th-more-item__label">Our Places</span>
          <IconChevronRight size={18} className="th-more-item__chevron" />
        </Link>

        <Link to={RoutePath.appNotifications} className="th-more-item">
          <div className="th-more-item__icon">
            <IconBell size={20} />
          </div>
          <span className="th-more-item__label">Notifications</span>
          <IconChevronRight size={18} className="th-more-item__chevron" />
        </Link>

        <Link to={RoutePath.appMoreSearch} className="th-more-item">
          <div className="th-more-item__icon">
            <IconSearch size={20} />
          </div>
          <span className="th-more-item__label">Search</span>
          <IconChevronRight size={18} className="th-more-item__chevron" />
        </Link>

        <Link to={RoutePath.appMoreVault} className="th-more-item">
          <div className="th-more-item__icon">
            <IconHeart size={20} />
          </div>
          <span className="th-more-item__label">Vault</span>
          <IconChevronRight size={18} className="th-more-item__chevron" />
        </Link>

        <Link to={RoutePath.appMoreAbout} className="th-more-item">
          <div className="th-more-item__icon">
            <IconHeart size={20} />
          </div>
          <span className="th-more-item__label">About</span>
          <IconChevronRight size={18} className="th-more-item__chevron" />
        </Link>
      </div>
    </div>
  );
}
