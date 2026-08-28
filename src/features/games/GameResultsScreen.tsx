/**
 * GameResultsScreen (Stage 13 — Games Visual Productization).
 *
 * Displays game results with enhanced celebration, improved score
 * presentation, and polished round breakdown. Engine unchanged.
 */

import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconHeart, IconCheck, IconClose } from '../../components/index.ts';
import type { GameResult, GameType } from '../../data/game/gameTypes.ts';
import { resolveLevelConfig } from '../../data/game/gameTypes.ts';
import { recordLevelCompletion, getGameProgressSummary } from '../../services/game/gameProgression.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';
import { useEffect, useRef } from 'react';
import {
  scoreDisplay,
  getGamePersonality,
  streakText,
} from './gamesPresentation.ts';

export function GameResultsScreen() {
  const navigate = useNavigate();
  const location = useLocation();
  const { gameType } = useParams<{ gameType: string }>();
  const state = location.state as { result?: GameResult; gameType?: GameType; level?: number; streak?: number } | null;
  const result = state?.result;
  const gt = (gameType ?? result?.gameType) as GameType | undefined;
  const definition = gt ? getGameDefinition(gt) : undefined;
  const personality = gt ? getGamePersonality(gt) : undefined;
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
      <div className="th-content-pad th-game-screen th-game-screen--warm">
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual"><span style={{ fontSize: 'var(--th-font-size-2xl)' }}>?</span></div>
          <h3 className="th-empty-state__title">No results to display</h3>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appGames)}>Back to Games</button>
        </div>
      </div>
    );
  }

  const winner = result.player1Score > result.player2Score ? 1
    : result.player2Score > result.player1Score ? 2 : 0;

  return (
    <div className="th-content-pad th-game-screen th-game-screen--warm">
      {/* Celebration header */}
      <div className="th-game-result-hero th-result-enter">
        <div className="th-game-result-hero__ring th-badge-enter">
          {winner === 0 ? (
            <span style={{ fontSize: 'var(--th-font-size-xl)' }}>=</span>
          ) : (
            <IconHeart size={32} />
          )}
        </div>
        <div className="th-game-result-hero__title">
          {winner === 0 ? "It's a Tie!" : 'Level Complete!'}
        </div>
        <div className="th-game-result-hero__subtitle">{definition.title}</div>
      </div>

      {/* Badges */}
      <div className="th-game-badge-group th-result-enter-delayed" style={{ marginBottom: 'var(--th-space-4)' }}>
        <span className="th-game-level-badge">Level {currentLevel}</span>
        <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        {streak > 0 && (
          <span className="th-game-streak-badge">{streakText(streak)}</span>
        )}
      </div>

      {/* Score card */}
      <div className="th-game-score-card--enhanced th-result-enter-delayed">
        <div className="th-game-score-card__value">
          {scoreDisplay(result.player1Score, result.player2Score)}
        </div>
        <div className="th-game-score-card__label">
          {personality?.category === 'couple' ? 'Partner 1 vs Partner 2' : 'Your score'}
        </div>
        <div className="th-game-score-card__message">{result.message}</div>
      </div>

      {/* Round breakdown */}
      <h2 className="th-game-breakdown-title th-result-enter-delayed-2">
        Round Breakdown
      </h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-2)', marginBottom: 'var(--th-space-6)' }}>
        {result.rounds.map((r, i) => (
          <div
            key={i}
            className={`th-game-round--enhanced ${r.matched ? 'th-game-round--matched-enhanced' : ''} th-game-stagger`}
            style={{ animationDelay: `${i * 30}ms`, background: 'var(--th-color-surface)', borderRadius: 'var(--th-radius-md)' }}
          >
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: 'var(--th-space-2)' }}>
              <span style={{ color: r.matched ? 'var(--th-color-burgundy)' : 'var(--th-color-text-secondary)', flexShrink: 0, marginTop: '2px' }}>
                {r.matched ? <IconCheck size={14} /> : <IconClose size={14} />}
              </span>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 'var(--th-font-size-sm)', fontWeight: 'var(--th-font-weight-semibold)', color: 'var(--th-color-text-primary)', marginBottom: '2px' }}>
                  Q{i + 1}: {r.question.length > 50 ? r.question.slice(0, 50) + '...' : r.question}
                </div>
                <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
                  P1: {r.player1Answer || '(no answer)'} · P2: {r.player2Answer || '(no answer)'}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Actions */}
      <div className="th-game-actions--enhanced">
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
