/**
 * GamesHubScreen (Phase 11).
 *
 * Games hub — navigation structure for couple games.
 * Each game links to the shared GamePlayScreen with its game type.
 */

import { Link } from 'react-router-dom';
import { RoutePath } from '../../../navigation/routes.ts';
import { IconChevronRight } from '../../../components/index.ts';
import { ALL_GAME_DEFINITIONS } from '../../../customization/games/gameContent.ts';

export function GamesHubScreen() {
  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-2)' }}>Games</h1>
      <p className="th-screen-subtitle" style={{ marginBottom: 'var(--th-space-6)' }}>
        Play together and learn more about each other
      </p>

      <div className="th-hub-grid">
        {ALL_GAME_DEFINITIONS.map((game) => (
          <Link
            key={game.type}
            to={`${RoutePath.appGames}/${game.type}`}
            className="th-feature-card"
          >
            <div className="th-feature-card__body">
              <div className="th-feature-card__title">{game.title}</div>
              <div className="th-feature-card__desc">{game.description}</div>
            </div>
            <IconChevronRight size={18} className="th-feature-card__chevron" />
          </Link>
        ))}
      </div>
    </div>
  );
}
