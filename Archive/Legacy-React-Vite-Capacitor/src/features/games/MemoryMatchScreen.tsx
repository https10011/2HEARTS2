/**
 * MemoryMatchScreen (Stage 13 — Games Visual Productization).
 *
 * Memory Match card game — flip cards to find matching pairs.
 * Enhanced: improved card grid, intro, and results. Engine unchanged.
 */

import { useState, useCallback, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  createMemoryMatchSession,
  flipCard,
  resetUnmatchedCards,
  resolveMemoryMatchPairs,
} from '../../services/game/gameEngine.ts';
import { resolveLevelConfig } from '../../data/game/gameTypes.ts';
import type { GameSession, MemoryCard } from '../../data/game/gameTypes.ts';
import { IconBack, IconHeart } from '../../components/index.ts';
import {
  getGamePersonality,
  efficiencyLabel,
  scoreDisplay,
} from './gamesPresentation.ts';

type GamePhase = 'intro' | 'playing' | 'results';

export function MemoryMatchScreen() {
  const navigate = useNavigate();
  const location = useLocation();
  const passedLevel = (location.state as { level?: number })?.level ?? 1;

  const [phase, setPhase] = useState<GamePhase>('intro');
  const [session, setSession] = useState<GameSession | null>(null);
  const [pendingFlipBack, setPendingFlipBack] = useState<number[] | null>(null);
  const [locked, setLocked] = useState(false);
  const [currentLevel, setCurrentLevel] = useState(passedLevel);

  const levelConfig = resolveLevelConfig(currentLevel);
  const pairCount = resolveMemoryMatchPairs(currentLevel);
  const personality = getGamePersonality('memory-match');

  const startGame = useCallback(() => {
    const s = createMemoryMatchSession(pairCount);
    s.level = currentLevel;
    s.difficulty = levelConfig.difficulty;
    setSession(s);
    setPhase('playing');
  }, [pairCount, currentLevel, levelConfig.difficulty]);

  const handleCardFlip = useCallback(
    (index: number) => {
      if (!session || locked) return;

      const board = session.board;
      if (!board) return;

      const card = board.cards[index];
      if (!card || card.revealed || card.matched) return;

      const result = flipCard(session, index);
      if (!result) return;

      setSession(result.session);

      if (result.gameOver) {
        setTimeout(() => setPhase('results'), 800);
        return;
      }

      if (board.firstFlippedIndex === null) {
        return;
      }

      if (result.matched) {
        // Match found
      } else {
        setLocked(true);
        setPendingFlipBack([board.firstFlippedIndex, index]);
      }
    },
    [session, locked],
  );

  useEffect(() => {
    if (!pendingFlipBack || !session) return;

    const timer = setTimeout(() => {
      const [i1, i2] = pendingFlipBack;
      setSession(resetUnmatchedCards(session, i1, i2));
      setPendingFlipBack(null);
      setLocked(false);
    }, 800);

    return () => clearTimeout(timer);
  }, [pendingFlipBack, session]);

  const goToNextLevel = useCallback(() => {
    const next = currentLevel + 1;
    setCurrentLevel(next);
    setPhase('intro');
    setSession(null);
  }, [currentLevel]);

  // --- Intro phase ---
  if (phase === 'intro') {
    return (
      <div className="th-content-pad th-game-intro th-game-intro--enhanced th-game-enter th-game-screen--warm">
        <div className="th-game-intro__circle">
          <IconHeart size={32} />
        </div>
        <h1 className="th-game-intro__title">Memory Match</h1>
        <div className="th-game-intro__vibe">{personality.vibe}</div>
        <div className="th-game-badge-group" style={{ margin: 'var(--th-space-4) 0' }}>
          <span className="th-game-level-badge th-badge-enter">Level {currentLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>
        <p className="th-game-intro__desc">Flip cards and find all matching pairs!</p>
        <p className="th-game-intro__meta" style={{ marginTop: 'var(--th-space-2)' }}>
          {pairCount} pairs to find
        </p>
        <div style={{ marginTop: 'var(--th-space-6)' }}>
          <button className="th-btn th-btn--primary th-pressable" onClick={startGame}>Start Game</button>
        </div>
        <button className="th-btn th-btn--secondary" onClick={() => navigate(RoutePath.appGames)} style={{ marginTop: 'var(--th-space-3)' }}>
          Back to Games
        </button>
      </div>
    );
  }

  // --- Results phase ---
  if (phase === 'results') {
    const moves = session?.board?.moves ?? 0;
    const pairs = session?.board?.totalPairs ?? pairCount;

    return (
      <div className="th-content-pad th-game-screen th-game-screen--warm">
        {/* Level-up celebration */}
        <div className="th-game-result-hero th-result-enter">
          <div className="th-game-result-hero__ring th-badge-enter">
            <IconHeart size={32} />
          </div>
          <div className="th-game-result-hero__title">Level Complete!</div>
          <div className="th-game-result-hero__subtitle">Memory Match</div>
        </div>

        <div className="th-game-badge-group th-result-enter-delayed" style={{ marginBottom: 'var(--th-space-4)' }}>
          <span className="th-game-level-badge">Level {currentLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>

        {/* Score card */}
        <div className="th-game-score-card--enhanced th-result-enter-delayed">
          <div className="th-game-score-card__value">
            {scoreDisplay(pairs, pairs)}
          </div>
          <div className="th-game-score-card__label">
            pairs found in {moves} moves
          </div>
          <div className="th-game-score-card__message">
            {efficiencyLabel(pairs, moves)}
          </div>
        </div>

        {/* Actions */}
        <div className="th-game-actions--enhanced th-result-enter-delayed-2">
          <button className="th-btn th-btn--primary th-btn--full th-pressable" onClick={goToNextLevel}>
            Next Level
          </button>
          <button className="th-btn th-btn--secondary th-btn--full th-pressable" onClick={startGame}>
            Replay Level {currentLevel}
          </button>
          <button className="th-btn th-btn--secondary th-btn--full th-pressable" onClick={() => navigate(RoutePath.appGames)}>
            Back to Games
          </button>
        </div>
      </div>
    );
  }

  // --- Playing phase ---
  const board = session?.board;
  if (!board) return null;

  return (
    <div className="th-content-pad th-game-screen th-game-screen--warm">
      {/* Header */}
      <div className="th-game-header">
        <button className="th-btn th-btn--ghost th-game-header__back" onClick={() => navigate(RoutePath.appGames)}>
          <IconBack size={20} />
        </button>
        <div className="th-game-header__title">Memory Match</div>
        <span className="th-game-header__counter">
          {board.matchedPairs}/{board.totalPairs}
        </span>
      </div>

      {/* Progress bar */}
      <div className="th-game-progress--enhanced">
        <div
          className="th-game-progress--enhanced__fill"
          style={{ width: `${(board.matchedPairs / board.totalPairs) * 100}%` }}
        />
      </div>

      {/* Stats */}
      <div className="th-game-stats">
        <div className="th-game-stat">
          <div className="th-game-stat__value">{board.matchedPairs}</div>
          <div className="th-game-stat__label">matched</div>
        </div>
        <div className="th-game-stat">
          <div className="th-game-stat__value">{board.moves}</div>
          <div className="th-game-stat__label">moves</div>
        </div>
      </div>

      {/* Card grid */}
      <div className="th-memory-grid--stage13">
        {board.cards.map((card, index) => (
          <MemoryCardView
            key={card.id}
            card={card}
            onClick={() => handleCardFlip(index)}
          />
        ))}
      </div>
    </div>
  );
}

function MemoryCardView({ card, onClick }: { card: MemoryCard; onClick: () => void }) {
  const isVisible = card.revealed || card.matched;

  let className = 'th-memory-card--stage13';
  if (card.matched) {
    className += ' th-memory-card--stage13--matched';
  } else if (isVisible) {
    className += ' th-memory-card--stage13--revealed';
  } else {
    className += ' th-memory-card--stage13--hidden';
  }

  return (
    <button
      className={className}
      onClick={onClick}
      disabled={card.revealed || card.matched}
      aria-label={isVisible ? `Card: ${card.symbol}` : 'Hidden card'}
    >
      <span className="th-memory-card--stage13__face">
        {isVisible ? card.symbol : '?'}
      </span>
    </button>
  );
}
