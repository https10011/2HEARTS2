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
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-2)' }}>Games</h1>
      <p className="th-screen-subtitle" style={{ marginBottom: 'var(--th-space-6)' }}>
        Play together and have fun
      </p>

      {/* Couple Games */}
      <h2 style={{
        fontSize: 'var(--th-font-size-sm)',
        fontWeight: 'var(--th-font-weight-semibold)',
        color: 'var(--th-color-text-secondary)',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
        marginBottom: 'var(--th-space-3)',
      }}>
        Couple Games
      </h2>
      <div className="th-hub-grid" style={{ marginBottom: 'var(--th-space-8)' }}>
        {COUPLE_GAME_DEFINITIONS.map((game) => (
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

      {/* Casual Games */}
      <h2 style={{
        fontSize: 'var(--th-font-size-sm)',
        fontWeight: 'var(--th-font-weight-semibold)',
        color: 'var(--th-color-text-secondary)',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
        marginBottom: 'var(--th-space-3)',
      }}>
        Fun & Casual
      </h2>
      <div className="th-hub-grid">
        {CASUAL_GAME_DEFINITIONS.map((game) => (
          <Link
            key={game.type}
            to={CASUAL_GAME_ROUTES[game.type] ?? `${RoutePath.appGames}/${game.type}`}
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
