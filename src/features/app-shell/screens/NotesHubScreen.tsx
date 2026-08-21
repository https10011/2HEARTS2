/**
 * NotesHubScreen (Phase 6).
 *
 * Notes hub — navigation structure for private notes.
 * Full notes functionality arrives in a later phase.
 */

import { Link } from 'react-router-dom';
import { RoutePath } from '../../../navigation/routes.ts';
import { IconChevronRight } from '../../../components/index.ts';

export function NotesHubScreen() {
  return (
    <div className="th-content-pad">
      <div className="th-screen-header--enhanced">
        <div>
          <h1 className="th-screen-title">Notes</h1>
          <p className="th-screen-subtitle" style={{ marginTop: 'var(--th-space-1)' }}>
            Private notes and thoughts shared between you
          </p>
        </div>
      </div>

      <div className="th-hub-grid--enhanced" style={{ marginTop: 'var(--th-space-4)' }}>
        <Link to={RoutePath.appNotesShared} className="th-feature-card th-feature-card--enhanced th-stagger-item">
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Shared Notes</div>
            <div className="th-feature-card__desc">Notes you both can see</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>

        <Link to={RoutePath.appNotesPrivate} className="th-feature-card th-feature-card--enhanced th-stagger-item" style={{ animationDelay: '40ms' }}>
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Private Notes</div>
            <div className="th-feature-card__desc">Only you can see these</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>
      </div>
    </div>
  );
}
