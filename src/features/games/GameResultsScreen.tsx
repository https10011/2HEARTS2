/**
 * GameResultsScreen (Phase 11, Phase 29 visual polish).
 *
 * Displays game results: score comparison, per-round breakdown,
 * fun result message, level-up celebration, and replay/next-level options.
 */

import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconHeart, IconClose, RoseLilyDecoration } from '../../components/index.ts';
import type { GameResult, GameType } from '../../data/game/gameTypes.ts';
import { resolveLevelConfig } from '../../data/game/gameTypes.ts';
import { recordLevelCompletion, getGameProgressSummary } from '../../services/game/gameProgression.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';
import { useEffect, useRef } from 'react';

export function GameResultsScreen() {
  const navigate = useNavigate();
  const location = useLocation();
  const { gameType } = useParams<{ gameType: string }>();
  const state = location.state as { result?: GameResult; gameType?: GameType; level?: number; streak?: number } | null;
  const result = state?.result;
  const gt = (gameType ?? result?.gameType) as GameType | undefined;
  const definition = gt ? getGameDefinition(gt) : undefined;
  const currentLevel = state?.level ?? 1;
  const levelConfig = resolveLevelConfig(currentLevel);

  // Persist progression on mount (once)
  const persisted = useRef(false);
  useEffect(() => {
    if (persisted.current || !gt || !result) return;
    persisted.current = true;
    const score = result.casualResult?.score ?? result.player1Score ?? 0;
    recordLevelCompletion(gt, currentLevel, score);
  }, [gt, result, currentLevel]);

  const progress = gt ? getGameProgressSummary(gt) : null;
  const streak = progress?.streak ?? 0;

  // --- No results ---
  if (!result || !definition) {
    return (
      <div className="th-content-pad th-game-screen">
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual"><span style={{ fontSize: '2rem' }}>?</span></div>
          <h3 className="th-empty-state__title">No results to display</h3>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appGames)}>Back to Games</button>
        </div>
      </div>
    );
  }

  const winner = result.player1Score > result.player2Score ? 1
    : result.player2Score > result.player1Score ? 2 : 0;

  return (
    <div className="th-content-pad th-game-screen th-screen-warm">
      <RoseLilyDecoration variant={4} size={90} position="top-right" opacity={0.15} animated />

      {/* Level-up celebration */}
      <div className="th-game-level-up th-result-enter">
        <div className="th-game-level-up__icon th-badge-enter">
          {winner === 0 ? <span style={{ fontSize: '1.4rem' }}>=</span> : <IconHeart size={28} />}
        </div>
        <div className="th-game-level-up__text">
          {winner === 0 ? "It's a Tie!" : 'Level Complete!'}
        </div>
        <div className="th-game-level-up__sub">{definition.title}</div>
      </div>

      {/* Badges */}
      <div className="th-game-badge-group th-result-enter-delayed" style={{ marginBottom: 'var(--th-space-4)' }}>
        <span className="th-game-level-badge">Level {currentLevel}</span>
        <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        {streak > 0 && (
          <span className="th-game-streak-badge">{streak} streak</span>
        )}
      </div>

      {/* Score card */}
      <div className="th-game-result-card th-result-enter-delayed">
        <div style={{ display: 'flex', justifyContent: 'space-around', alignItems: 'center', position: 'relative', zIndex: 1 }}>
          <div>
            <div className="th-game-result-card__label" style={{ marginBottom: 'var(--th-space-1)' }}>Partner 1</div>
            <div className="th-game-result-card__score">{result.player1Score}</div>
          </div>
          <div style={{ fontSize: 'var(--th-font-size-xl)', opacity: 0.6, position: 'relative', zIndex: 1 }}>vs</div>
          <div>
            <div className="th-game-result-card__label" style={{ marginBottom: 'var(--th-space-1)' }}>Partner 2</div>
            <div className="th-game-result-card__score">{result.player2Score}</div>
          </div>
        </div>
        <div className="th-game-result-card__message">{result.message}</div>
      </div>

      {/* Round breakdown */}
      <h2 style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-3)' }} className="th-result-enter-delayed-2">
        Round Breakdown
      </h2>
      <div className="th-hub-grid th-result-enter-delayed-2" style={{ marginBottom: 'var(--th-space-6)' }}>
        {result.rounds.map((r, i) => (
          <div
            key={i}
            className={`th-feature-card th-feature-card--enhanced th-stagger-item th-game-round ${r.matched ? 'th-game-round--matched' : ''}`}
            style={{ cursor: 'default' }}
          >
            <div className="th-feature-card__body">
              <div className="th-feature-card__title" style={{ fontSize: 'var(--th-font-size-sm)' }}>
                <span style={{ color: r.matched ? 'var(--th-color-burgundy)' : 'var(--th-color-text-secondary)', display: 'inline-flex', verticalAlign: '-2px', marginRight: 'var(--th-space-1)' }}>
                  {r.matched ? <IconHeart size={14} /> : <IconClose size={14} />}
                </span>
                Q{i + 1}: {r.question.length > 50 ? r.question.slice(0, 50) + '...' : r.question}
              </div>
              <div className="th-feature-card__desc">
                P1: {r.player1Answer || '(no answer)'} · P2: {r.player2Answer || '(no answer)'}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Actions */}
      <div className="th-game-actions">
        <button
          className="th-btn th-btn--primary th-btn--full th-pressable"
          onClick={() => navigate(`${RoutePath.appGames}/${gt}`, { replace: true, state: { level: currentLevel + 1 } })}
        >
          Next Level (Level {currentLevel + 1})
        </button>
        <button
          className="th-btn th-btn--secondary th-btn--full th-pressable"
          onClick={() => navigate(`${RoutePath.appGames}/${gt}`, { replace: true, state: { level: currentLevel } })}
        >
          Replay Level {currentLevel}
        </button>
        <button
          className="th-btn th-btn--secondary th-btn--full th-pressable"
          onClick={() => navigate(RoutePath.appGames)}
        >
          Back to Games
        </button>
      </div>
    </div>
  );
}
