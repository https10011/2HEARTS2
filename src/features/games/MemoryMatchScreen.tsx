/**
 * MemoryMatchScreen (Phase 12, Phase 29 visual polish).
 *
 * Memory Match card game — flip cards to find matching pairs.
 * Uses createMemoryMatchSession + flipCard from the shared game engine.
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
      <div className="th-content-pad th-game-intro th-game-enter">
        <div className="th-game-intro__icon">
          <IconHeart size={28} />
        </div>
        <h1 className="th-game-intro__title">Memory Match</h1>
        <div className="th-game-badge-group" style={{ marginBottom: 'var(--th-space-3)' }}>
          <span className="th-game-level-badge th-badge-enter">Level {currentLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>
        <p className="th-game-intro__desc">Flip cards and find all matching pairs!</p>
        <p className="th-game-intro__meta">{pairCount} pairs to find</p>
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
    const efficiency = pairs > 0 ? Math.round((pairs / Math.max(moves, pairs)) * 100) : 0;

    let message: string;
    if (efficiency >= 80) message = 'Incredible memory!';
    else if (efficiency >= 60) message = 'Great job!';
    else if (efficiency >= 40) message = 'Not bad at all!';
    else message = 'Practice makes perfect!';

    return (
      <div className="th-content-pad th-game-screen">
        {/* Level-up celebration */}
        <div className="th-game-level-up th-result-enter">
          <div className="th-game-level-up__icon th-badge-enter"><IconHeart size={28} /></div>
          <div className="th-game-level-up__text">Level Complete!</div>
          <div className="th-game-level-up__sub">Memory Match</div>
        </div>

        <div className="th-game-badge-group th-result-enter-delayed" style={{ marginBottom: 'var(--th-space-4)' }}>
          <span className="th-game-level-badge">Level {currentLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>

        {/* Score card */}
        <div className="th-game-result-card th-result-enter-delayed">
          <div className="th-game-result-card__score">{pairs} / {pairs}</div>
          <div className="th-game-result-card__label">pairs found in {moves} moves</div>
          <div className="th-game-result-card__message">{message}</div>
        </div>

        {/* Actions */}
        <div className="th-game-actions th-result-enter-delayed-2">
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
    <div className="th-content-pad th-game-screen">
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
      <div className="th-progress-bar" style={{ marginBottom: 'var(--th-space-3)' }}>
        <div
          className="th-progress-bar__fill"
          style={{ width: `${(board.matchedPairs / board.totalPairs) * 100}%` }}
        />
      </div>

      {/* Moves counter */}
      <div className="th-game-score">
        Moves: <span className="th-game-score__number">{board.moves}</span>
      </div>

      {/* Card grid */}
      <div className="th-memory-grid th-memory-grid--enhanced" style={{ marginTop: 'var(--th-space-4)' }}>
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

  return (
    <button
      className={`th-memory-card ${isVisible ? 'th-memory-card--revealed' : ''} ${card.matched ? 'th-memory-card--matched' : ''}`}
      onClick={onClick}
      disabled={card.revealed || card.matched}
      aria-label={isVisible ? `Card: ${card.symbol}` : 'Hidden card'}
    >
      <span className="th-memory-card__face">
        {isVisible ? card.symbol : '?'}
      </span>
    </button>
  );
}
