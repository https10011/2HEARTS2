/**
 * MoreScreen (Phase 6, refined Phase 24).
 *
 * Application utilities only: Settings, Search, About (navConfig.MORE_ITEMS).
 * Relationship destinations live in the central TwoHearts hub; everyday
 * destinations live in the bottom navigation — nothing duplicated here.
 */

import { Link } from 'react-router-dom';
import { IconChevronRight } from '../../../components/index.ts';
import { MORE_ITEMS } from '../navConfig.ts';
import { NavIcon } from '../navIcons.tsx';

export function MoreScreen() {
  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-6)' }}>More</h1>

      <div style={{ display: 'flex', flexDirection: 'column' }}>
        {MORE_ITEMS.map((item) => (
          <Link key={item.id} to={item.route} className="th-more-item">
            <div className="th-more-item__icon">
              <NavIcon icon={item.icon} size={20} />
            </div>
            <span className="th-more-item__label">{item.label}</span>
            <span className="th-more-item__caption">{item.caption}</span>
            <IconChevronRight size={18} className="th-more-item__chevron" />
          </Link>
        ))}
      </div>
    </div>
  );
}
