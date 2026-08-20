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
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-2)' }}>Notes</h1>
      <p className="th-screen-subtitle" style={{ marginBottom: 'var(--th-space-6)' }}>
        Private notes and thoughts shared between you
      </p>

      <div className="th-hub-grid">
        <Link to={RoutePath.appNotesShared} className="th-feature-card">
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Shared Notes</div>
            <div className="th-feature-card__desc">Notes you both can see</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>

        <Link to={RoutePath.appNotesPrivate} className="th-feature-card">
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
