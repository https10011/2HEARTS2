/**
 * MemoryMatchScreen (Phase 12).
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
import { IconBack } from '../../components/index.ts';

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
        // Brief delay before showing results
        setTimeout(() => setPhase('results'), 800);
        return;
      }

      if (board.firstFlippedIndex === null) {
        // First card of pair — just revealed, nothing to check yet
        return;
      }

      // Second card — check match
      if (result.matched) {
        // Match found — no flip back needed
      } else {
        // No match — lock and flip back after delay
        setLocked(true);
        setPendingFlipBack([board.firstFlippedIndex, index]);
      }
    },
    [session, locked],
  );

  // Handle flip-back after non-match delay
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

  if (phase === 'intro') {
    return (
      <div className="th-content-pad" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60dvh' }}>
        <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-8)' }}>
          <h1 style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-2xl)', color: 'var(--th-color-text-primary)', marginBottom: 'var(--th-space-3)' }}>
            Memory Match
          </h1>
          <div style={{ display: 'flex', gap: 'var(--th-space-2)', justifyContent: 'center', marginBottom: 'var(--th-space-2)' }}>
            <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-burgundy)', fontWeight: 'var(--th-font-weight-semibold)', textTransform: 'uppercase', letterSpacing: '0.5px', padding: '2px 8px', borderRadius: 'var(--th-radius-sm)', background: 'var(--th-color-blush)' }}>
              Level {currentLevel}
            </span>
            <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', padding: '2px 8px', borderRadius: 'var(--th-radius-sm)', background: 'var(--th-color-neutral-soft)' }}>
              {levelConfig.difficulty}
            </span>
          </div>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-md)', maxWidth: '36ch', margin: '0 auto' }}>
            Flip cards and find all matching pairs!
          </p>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)', marginTop: 'var(--th-space-3)' }}>
            {pairCount} pairs to find
          </p>
        </div>
        <button className="th-btn th-btn--primary" onClick={startGame}>
          Start Game
        </button>
        <button
          className="th-btn th-btn--secondary"
          onClick={() => navigate(RoutePath.appGames)}
          style={{ marginTop: 'var(--th-space-3)' }}
        >
          Back to Games
        </button>
      </div>
    );
  }

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
      <div className="th-content-pad">
        <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-6)' }}>
          <h1 style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-2xl)', color: 'var(--th-color-text-primary)', marginBottom: 'var(--th-space-2)' }}>
            Game Complete!
          </h1>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-md)' }}>Memory Match — Level {currentLevel}</p>
        </div>

        <div className="th-relationship-card" style={{ marginBottom: 'var(--th-space-6)', textAlign: 'center' }}>
          <div style={{ fontSize: 'var(--th-font-size-3xl)', fontWeight: 'var(--th-font-weight-bold)', fontFamily: 'var(--th-font-family-display)', marginBottom: 'var(--th-space-2)' }}>
            {pairs} / {pairs}
          </div>
          <div style={{ fontSize: 'var(--th-font-size-sm)', opacity: 0.85 }}>pairs found in {moves} moves</div>
          <div style={{ marginTop: 'var(--th-space-3)', fontSize: 'var(--th-font-size-md)', opacity: 0.9 }}>
            {message}
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
          <button className="th-btn th-btn--primary th-btn--full" onClick={goToNextLevel}>
            Next Level
          </button>
          <button className="th-btn th-btn--secondary th-btn--full" onClick={startGame}>
            Replay Level {currentLevel}
          </button>
          <button className="th-btn th-btn--secondary th-btn--full" onClick={() => navigate(RoutePath.appGames)}>
            Back to Games
          </button>
        </div>
      </div>
    );
  }

  // Playing phase
  const board = session?.board;
  if (!board) return null;

  return (
    <div className="th-content-pad">
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <button
          className="th-btn th-btn--ghost"
          onClick={() => navigate(RoutePath.appGames)}
          style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
        >
          <IconBack size={20} />
        </button>
        <div style={{ flex: 1, textAlign: 'center' }}>
          <span style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)' }}>
            Memory Match
          </span>
        </div>
        <span style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', minWidth: '40px', textAlign: 'right' }}>
          {board.matchedPairs}/{board.totalPairs}
        </span>
      </div>

      {/* Progress bar */}
      <div className="th-progress-bar" style={{ marginBottom: 'var(--th-space-4)' }}>
        <div
          className="th-progress-bar__fill"
          style={{ width: `${(board.matchedPairs / board.totalPairs) * 100}%` }}
        />
      </div>

      {/* Moves counter */}
      <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-4)' }}>
        <span style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)' }}>
          Moves: {board.moves}
        </span>
      </div>

      {/* Card grid */}
      <div className="th-memory-grid">
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
