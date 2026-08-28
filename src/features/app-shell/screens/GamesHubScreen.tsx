/**
 * GamesHubScreen (Stage 13 — Games Visual Productization).
 *
 * Games hub with personality-driven card layout. Each game gets
 * its own icon, accent, and vibe. Couple and casual sections are
 * visually distinguished. TwoHearts design language preserved.
 */

import { Link } from 'react-router-dom';
import { RoutePath } from '../../../navigation/routes.ts';
import {
  IconChevronRight,
  IconHeart,
  IconSparkle,
} from '../../../components/index.ts';
import {
  COUPLE_GAME_DEFINITIONS,
  CASUAL_GAME_DEFINITIONS,
} from '../../../customization/games/gameContent.ts';
import {
  getGamePersonality,
} from '../../games/gamesPresentation.ts';
import type { GameType } from '../../../data/game/gameTypes.ts';

/** Routes for casual games that use specialized screens. */
const CASUAL_GAME_ROUTES: Partial<Record<GameType, string>> = {
  'memory-match': `${RoutePath.appGames}/memory-match`,
  'word-scramble': `${RoutePath.appGames}/word-scramble`,
  'casual-trivia': `${RoutePath.appGames}/casual-trivia`,
  'riddle-room': `${RoutePath.appGames}/riddle-room`,
};

/** Game type → hub card emoji (content emoji per MasterPrompt §22 is allowed). */
const GAME_EMOJI: Record<string, string> = {
  'who-knows-who-better': '\u{1F9E0}',
  'guess-my-answer': '\u{1F4AD}',
  'would-you-rather': '\u{2753}',
  'couple-trivia': '\u{1F9E0}',
  'this-or-that': '\u{2694}\u{FE0F}',
  'finish-my-sentence': '\u{270D}\u{FE0F}',
  'memory-match': '\u{1F0CF}',
  'word-scramble': '\u{1F524}',
  'casual-trivia': '\u{2728}',
  'riddle-room': '\u{1F9E9}',
};

export function GamesHubScreen() {
  return (
    <div className="th-content-pad th-screen-warm th-game-screen--warm">
      {/* Hero */}
      <div className="th-games-hero th-game-enter">
        <div className="th-games-hero__label">TwoHearts Games</div>
        <h1 className="th-games-hero__title">Play Together</h1>
        <p className="th-games-hero__desc">
          Fun games designed for couples — discover, laugh, and connect.
        </p>
      </div>

      {/* Couple Games */}
      <div className="th-games-section-header">
        <div className="th-games-section-header__icon">
          <IconHeart size={18} />
        </div>
        <h2 className="th-games-section-header__text">Couple Games</h2>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)', marginBottom: 'var(--th-space-8)' }}>
        {COUPLE_GAME_DEFINITIONS.map((game, i) => {
          const personality = getGamePersonality(game.type);
          return (
            <Link
              key={game.type}
              to={`${RoutePath.appGames}/${game.type}`}
              className="th-game-hub-card th-game-stagger"
              style={{ animationDelay: `${i * 50}ms` }}
            >
              <div className="th-game-hub-card__icon th-game-hub-card__icon--couple">
                {GAME_EMOJI[game.type] ?? '\u{2665}\u{FE0F}'}
              </div>
              <div className="th-game-hub-card__body">
                <div className="th-game-hub-card__title">{game.title}</div>
                <div className="th-game-hub-card__desc">{personality.accent}</div>
              </div>
              <IconChevronRight size={18} className="th-game-hub-card__chevron" />
            </Link>
          );
        })}
      </div>

      {/* Casual Games */}
      <div className="th-games-section-header">
        <div className="th-games-section-header__icon">
          <IconSparkle size={18} />
        </div>
        <h2 className="th-games-section-header__text">Fun & Casual</h2>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)', paddingBottom: 'var(--th-space-8)' }}>
        {CASUAL_GAME_DEFINITIONS.map((game, i) => {
          const personality = getGamePersonality(game.type);
          return (
            <Link
              key={game.type}
              to={CASUAL_GAME_ROUTES[game.type] ?? `${RoutePath.appGames}/${game.type}`}
              className="th-game-hub-card th-game-stagger"
              style={{ animationDelay: `${i * 50}ms` }}
            >
              <div className="th-game-hub-card__icon th-game-hub-card__icon--casual">
                {GAME_EMOJI[game.type] ?? '\u{2665}\u{FE0F}'}
              </div>
              <div className="th-game-hub-card__body">
                <div className="th-game-hub-card__title">{game.title}</div>
                <div className="th-game-hub-card__desc">{personality.accent}</div>
              </div>
              <IconChevronRight size={18} className="th-game-hub-card__chevron" />
            </Link>
          );
        })}
      </div>
    </div>
  );
}
