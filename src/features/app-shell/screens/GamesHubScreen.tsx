/**
 * GamesHubScreen (Phase 6).
 *
 * Games hub — navigation structure for couple games.
 * Actual game implementations arrive in later phases.
 */

import { Link } from 'react-router-dom';
import { RoutePath } from '../../../navigation/routes.ts';
import { IconChevronRight } from '../../../components/index.ts';

export function GamesHubScreen() {
  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-2)' }}>Games</h1>
      <p className="th-screen-subtitle" style={{ marginBottom: 'var(--th-space-6)' }}>
        Play together and learn more about each other
      </p>

      <div className="th-hub-grid">
        <Link to={RoutePath.appGamesWhoKnows} className="th-feature-card">
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Who Knows Who Better?</div>
            <div className="th-feature-card__desc">Test how well you know each other</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>

        <Link to={RoutePath.appGamesWouldYouRather} className="th-feature-card">
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">Would You Rather?</div>
            <div className="th-feature-card__desc">Fun dilemmas for couples</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>

        <Link to={RoutePath.appGamesTwentyQuestions} className="th-feature-card">
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">20 Questions</div>
            <div className="th-feature-card__desc">Deep conversations starter</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>

        <Link to={RoutePath.appGamesHowWell} className="th-feature-card">
          <div className="th-feature-card__body">
            <div className="th-feature-card__title">How Well Do You Know Each Other?</div>
            <div className="th-feature-card__desc">The ultimate couple challenge</div>
          </div>
          <IconChevronRight size={18} className="th-feature-card__chevron" />
        </Link>
      </div>
    </div>
  );
}
