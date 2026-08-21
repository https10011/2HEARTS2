/**
 * GamesHubScreen (Phase 11 + 12).
 *
 * Games hub — navigation structure for couple games and casual games.
 * Couple games link to the shared GamePlayScreen with their game type.
 * Casual games link to specialized screens (MemoryMatch, WordScramble,
 * CasualGamePlay).
 */

import { Link } from 'react-router-dom';
import { RoutePath } from '../../../navigation/routes.ts';
import { IconChevronRight } from '../../../components/index.ts';
import {
  COUPLE_GAME_DEFINITIONS,
  CASUAL_GAME_DEFINITIONS,
} from '../../../customization/games/gameContent.ts';
import type { GameType } from '../../../data/game/gameTypes.ts';

/** Routes for casual games that use specialized screens. */
const CASUAL_GAME_ROUTES: Partial<Record<GameType, string>> = {
  'memory-match': `${RoutePath.appGames}/memory-match`,
  'word-scramble': `${RoutePath.appGames}/word-scramble`,
  'casual-trivia': `${RoutePath.appGames}/casual-trivia`,
  'riddle-room': `${RoutePath.appGames}/riddle-room`,
};

export function GamesHubScreen() {
  return (
    <div className="th-content-pad">
      <div className="th-screen-header--enhanced">
        <div>
          <h1 className="th-screen-title">Games</h1>
          <p className="th-screen-subtitle" style={{ marginTop: 'var(--th-space-1)' }}>
            Play together and have fun
          </p>
        </div>
      </div>

      {/* Couple Games */}
      <h2 className="th-hub-section-title" style={{ marginTop: 'var(--th-space-4)' }}>
        Couple Games
      </h2>
      <div className="th-hub-grid--enhanced" style={{ marginBottom: 'var(--th-space-8)' }}>
        {COUPLE_GAME_DEFINITIONS.map((game, i) => (
          <Link
            key={game.type}
            to={`${RoutePath.appGames}/${game.type}`}
            className="th-feature-card th-feature-card--enhanced th-stagger-item"
            style={{ animationDelay: `${i * 40}ms` }}
          >
            <div className="th-feature-card__body">
              <div className="th-feature-card__title">{game.title}</div>
              <div className="th-feature-card__desc">{game.description}</div>
            </div>
            <IconChevronRight size={18} className="th-feature-card__chevron" />
          </Link>
        ))}
      </div>

      {/* Casual Games */}
      <h2 className="th-hub-section-title">
        Fun & Casual
      </h2>
      <div className="th-hub-grid--enhanced">
        {CASUAL_GAME_DEFINITIONS.map((game, i) => (
          <Link
            key={game.type}
            to={CASUAL_GAME_ROUTES[game.type] ?? `${RoutePath.appGames}/${game.type}`}
            className="th-feature-card th-feature-card--enhanced th-stagger-item"
            style={{ animationDelay: `${i * 40}ms` }}
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
