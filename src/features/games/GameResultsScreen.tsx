/**
 * GameResultsScreen (Phase 11).
 *
 * Displays game results: score comparison, per-round breakdown,
 * fun result message, and replay option.
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
    // Calculate a score for persistence (match count for couple games, casual score for casual games)
    const score = result.casualResult?.score ?? result.player1Score ?? 0;
    recordLevelCompletion(gt, currentLevel, score);
  }, [gt, result, currentLevel]);

  const progress = gt ? getGameProgressSummary(gt) : null;
  const streak = progress?.streak ?? 0;

  if (!result || !definition) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual">
            <span style={{ fontSize: '2rem' }}>?</span>
          </div>
          <h3 className="th-empty-state__title">No results to display</h3>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appGames)}>
            Back to Games
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad th-screen-warm">
      <RoseLilyDecoration variant={4} size={90} position="top-right" opacity={0.15} animated />
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
        <div style={{ display: 'flex', gap: 'var(--th-space-2)', justifyContent: 'center', marginTop: 'var(--th-space-2)' }}>
          <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-burgundy)', fontWeight: 'var(--th-font-weight-semibold)', textTransform: 'uppercase', letterSpacing: '0.5px', padding: '2px 8px', borderRadius: 'var(--th-radius-sm)', background: 'var(--th-color-blush)' }}>
            Level {currentLevel}
          </span>
          <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', padding: '2px 8px', borderRadius: 'var(--th-radius-sm)', background: 'var(--th-color-neutral-soft)' }}>
            {levelConfig.difficulty}
          </span>
          {streak > 0 && (
            <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-success)', fontWeight: 'var(--th-font-weight-semibold)', padding: '2px 8px', borderRadius: 'var(--th-radius-sm)', background: 'var(--th-color-success-bg)' }}>
              {streak} streak
            </span>
          )}
        </div>
      </div>

      {/* Score card */}
      <div className="th-relationship-card th-relationship-card--enhanced" style={{ marginBottom: 'var(--th-space-6)', textAlign: 'center' }}>
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
            className="th-feature-card th-feature-card--enhanced th-stagger-item"
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

      {/* Actions */}        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
        <button
          className="th-btn th-btn--primary th-btn--full"
          onClick={() => navigate(`${RoutePath.appGames}/${gt}`, { replace: true, state: { level: currentLevel + 1 } })}
        >
          Next Level (Level {currentLevel + 1})
        </button>
        <button
          className="th-btn th-btn--secondary th-btn--full"
          onClick={() => navigate(`${RoutePath.appGames}/${gt}`, { replace: true, state: { level: currentLevel } })}
        >
          Replay Level {currentLevel}
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
