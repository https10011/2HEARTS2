/**
 * GameResultsScreen (Phase 11).
 *
 * Displays game results: score comparison, per-round breakdown,
 * fun result message, and replay option.
 */

import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconHeart, IconClose } from '../../components/index.ts';
import type { GameResult, GameType } from '../../data/game/gameTypes.ts';

import { getGameDefinition } from '../../customization/games/gameContent.ts';

export function GameResultsScreen() {
  const navigate = useNavigate();
  const location = useLocation();
  const { gameType } = useParams<{ gameType: string }>();
  const result = (location.state as { result?: GameResult })?.result;
  const gt = (gameType ?? result?.gameType) as GameType | undefined;
  const definition = gt ? getGameDefinition(gt) : undefined;

  if (!result || !definition) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state">
          <h3>No results to display</h3>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appGames)}>
            Back to Games
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      {/* Title */}
      <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-6)' }}>
        <h1 style={{
          fontFamily: 'var(--th-font-family-display)',
          fontSize: 'var(--th-font-size-2xl)',
          color: 'var(--th-color-text-primary)',
          marginBottom: 'var(--th-space-2)',
        }}>
          Game Complete!
        </h1>
        <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-md)' }}>
          {definition.title}
        </p>
      </div>

      {/* Score card */}
      <div className="th-relationship-card" style={{ marginBottom: 'var(--th-space-6)', textAlign: 'center' }}>
        <div style={{ display: 'flex', justifyContent: 'space-around', alignItems: 'center' }}>
          <div>
            <div style={{ fontSize: 'var(--th-font-size-sm)', opacity: 0.85, marginBottom: 'var(--th-space-1)' }}>Partner 1</div>
            <div style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-2xl)', fontWeight: 'var(--th-font-weight-bold)' }}>
              {result.player1Score}
            </div>
          </div>
          <div style={{ fontSize: 'var(--th-font-size-xl)', opacity: 0.6 }}>vs</div>
          <div>
            <div style={{ fontSize: 'var(--th-font-size-sm)', opacity: 0.85, marginBottom: 'var(--th-space-1)' }}>Partner 2</div>
            <div style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-2xl)', fontWeight: 'var(--th-font-weight-bold)' }}>
              {result.player2Score}
            </div>
          </div>
        </div>
        <div style={{ marginTop: 'var(--th-space-3)', fontSize: 'var(--th-font-size-md)', opacity: 0.9 }}>
          {result.message}
        </div>
      </div>

      {/* Round breakdown */}
      <h2 style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-3)' }}>
        Round Breakdown
      </h2>
      <div className="th-hub-grid" style={{ marginBottom: 'var(--th-space-6)' }}>
        {result.rounds.map((r, i) => (
          <div
            key={i}
            className="th-feature-card"
            style={{
              cursor: 'default',
              borderLeftColor: r.matched ? 'var(--th-color-burgundy)' : undefined,
            }}
          >
            <div className="th-feature-card__body">
              <div className="th-feature-card__title" style={{ fontSize: 'var(--th-font-size-sm)' }}>
                <span style={{ color: r.matched ? 'var(--th-color-burgundy)' : 'var(--th-color-text-secondary)', display: 'inline-flex', verticalAlign: '-2px', marginRight: 'var(--th-space-1)' }}>
                  {r.matched ? <IconHeart size={14} /> : <IconClose size={14} />}
                </span>Q{i + 1}: {r.question.length > 50 ? r.question.slice(0, 50) + '...' : r.question}
              </div>
              <div className="th-feature-card__desc">
                P1: {r.player1Answer || '(no answer)'} · P2: {r.player2Answer || '(no answer)'}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
        <button
          className="th-btn th-btn--primary th-btn--full"
          onClick={() => navigate(`${RoutePath.appGames}/${gt}`, { replace: true })}
        >
          Play Again
        </button>
        <button
          className="th-btn th-btn--secondary th-btn--full"
          onClick={() => navigate(RoutePath.appGames)}
        >
          Back to Games
        </button>
      </div>
    </div>
  );
}
