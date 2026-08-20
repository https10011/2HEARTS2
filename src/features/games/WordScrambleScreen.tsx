/**
 * WordScrambleScreen (Phase 12).
 *
 * Word Scramble — unscramble love-themed words one at a time.
 * Uses createWordScrambleSession + scrambleWord + validateScrambleGuess
 * from the shared game engine.
 */

import { useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  createWordScrambleSession,
  scrambleWord,
  validateScrambleGuess,
} from '../../services/game/gameEngine.ts';
import type { GameSession } from '../../data/game/gameTypes.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';
import { IconBack } from '../../components/index.ts';

type GamePhase = 'intro' | 'playing' | 'results';

export function WordScrambleScreen() {
  const navigate = useNavigate();
  const definition = getGameDefinition('word-scramble');
  const questions = definition?.questions ?? [];
  const wordCount = 10;

  const [phase, setPhase] = useState<GamePhase>('intro');
  const [session, setSession] = useState<GameSession | null>(null);
  const [guess, setGuess] = useState('');
  const [feedback, setFeedback] = useState<'correct' | 'incorrect' | null>(null);
  const [errors, setErrors] = useState<string[]>([]);

  const currentWordIndex = session?.scrambleState?.currentWordIndex ?? 0;
  const currentQuestion = questions[currentWordIndex];
  const correctAnswer = currentQuestion?.correctAnswer ?? '';

  const scrambled = useMemo(() => {
    return correctAnswer ? scrambleWord(correctAnswer) : '';
  }, [correctAnswer]); // Recomputes when word changes

  const startGame = useCallback(() => {
    const s = createWordScrambleSession(wordCount);
    setSession(s);
    setPhase('playing');
    setGuess('');
    setFeedback(null);
    setErrors([]);
  }, [wordCount]);

  const handleSubmit = useCallback(() => {
    if (!session || !correctAnswer) return;

    const trimmed = guess.trim();
    if (!trimmed) {
      setErrors(['Please enter your guess.']);
      return;
    }

    setErrors([]);

    const result = validateScrambleGuess(session, trimmed, correctAnswer);
    if (!result) return;

    setSession(result.session);
    setFeedback(result.correct ? 'correct' : 'incorrect');

    // Brief delay before advancing
    setTimeout(() => {
      setFeedback(null);
      setGuess('');

      if (result.gameOver) {
        setPhase('results');
      }
    }, 1200);
  }, [session, guess, correctAnswer]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === 'Enter' && !feedback) {
        handleSubmit();
      }
    },
    [handleSubmit, feedback],
  );

  if (phase === 'intro') {
    return (
      <div className="th-content-pad" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60dvh' }}>
        <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-8)' }}>
          <h1 style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-2xl)', color: 'var(--th-color-text-primary)', marginBottom: 'var(--th-space-3)' }}>
            Word Scramble
          </h1>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-md)', maxWidth: '36ch', margin: '0 auto' }}>
            Unscramble love-themed words!
          </p>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)', marginTop: 'var(--th-space-3)' }}>
            {wordCount} words to unscramble
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
    const score = session?.casualScore ?? 0;
    const accuracy = wordCount > 0 ? Math.round((score / wordCount) * 100) : 0;

    let message: string;
    if (accuracy >= 90) message = 'Word wizard!';
    else if (accuracy >= 70) message = 'Great vocabulary!';
    else if (accuracy >= 50) message = 'Nice effort!';
    else message = 'Words are tricky!';

    return (
      <div className="th-content-pad">
        <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-6)' }}>
          <h1 style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-2xl)', color: 'var(--th-color-text-primary)', marginBottom: 'var(--th-space-2)' }}>
            Game Complete!
          </h1>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-md)' }}>Word Scramble</p>
        </div>

        <div className="th-relationship-card" style={{ marginBottom: 'var(--th-space-6)', textAlign: 'center' }}>
          <div style={{ fontSize: 'var(--th-font-size-3xl)', fontWeight: 'var(--th-font-weight-bold)', fontFamily: 'var(--th-font-family-display)', marginBottom: 'var(--th-space-2)' }}>
            {score} / {wordCount}
          </div>
          <div style={{ fontSize: 'var(--th-font-size-sm)', opacity: 0.85 }}>words unscrambled ({accuracy}% accuracy)</div>
          <div style={{ marginTop: 'var(--th-space-3)', fontSize: 'var(--th-font-size-md)', opacity: 0.9 }}>
            {message}
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
          <button className="th-btn th-btn--primary th-btn--full" onClick={startGame}>
            Play Again
          </button>
          <button className="th-btn th-btn--secondary th-btn--full" onClick={() => navigate(RoutePath.appGames)}>
            Back to Games
          </button>
        </div>
      </div>
    );
  }

  // Playing phase
  const progress = (currentWordIndex / wordCount) * 100;

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
            Word Scramble
          </span>
        </div>
        <span style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', minWidth: '40px', textAlign: 'right' }}>
          {currentWordIndex + 1}/{wordCount}
        </span>
      </div>

      {/* Progress bar */}
      <div className="th-progress-bar" style={{ marginBottom: 'var(--th-space-6)' }}>
        <div className="th-progress-bar__fill" style={{ width: `${progress}%` }} />
      </div>

      {/* Scrambled word */}
      <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-8)' }}>
        <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)', marginBottom: 'var(--th-space-2)' }}>
          Unscramble this word:
        </p>
        <div
          className="th-scramble-display"
          style={{
            fontFamily: 'var(--th-font-family-display)',
            fontSize: 'var(--th-font-size-2xl)',
            fontWeight: 'var(--th-font-weight-bold)',
            color: 'var(--th-color-text-primary)',
            letterSpacing: '8px',
            padding: 'var(--th-space-4)',
            background: 'var(--th-color-surface-secondary)',
            borderRadius: 'var(--th-radius-md)',
          }}
        >
          {scrambled.toUpperCase()}
        </div>
        {currentQuestion?.category && (
          <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', marginTop: 'var(--th-space-2)', display: 'block' }}>
            {currentQuestion.category}
          </span>
        )}
      </div>

      {/* Feedback */}
      {feedback && (
        <div
          style={{
            textAlign: 'center',
            padding: 'var(--th-space-3)',
            borderRadius: 'var(--th-radius-md)',
            marginBottom: 'var(--th-space-4)',
            fontWeight: 'var(--th-font-weight-semibold)',
            background: feedback === 'correct' ? 'var(--th-color-success-bg, #e8f5e9)' : 'var(--th-color-error-bg, #fce4ec)',
            color: feedback === 'correct' ? 'var(--th-color-success, #2e7d32)' : 'var(--th-color-error, #c62828)',
          }}
        >
          {feedback === 'correct' ? 'Correct!' : `The answer was: ${correctAnswer}`}
        </div>
      )}

      {/* Answer input */}
      {!feedback && (
        <div style={{ marginBottom: 'var(--th-space-6)' }}>
          <input
            className="th-input"
            type="text"
            value={guess}
            onChange={(e) => { setGuess(e.target.value); setErrors([]); }}
            onKeyDown={handleKeyDown}
            placeholder="Type your answer..."
            autoFocus
            autoComplete="off"
          />
        </div>
      )}

      {/* Errors */}
      {errors.length > 0 && (
        <div className="th-form-errors" style={{ marginBottom: 'var(--th-space-4)' }}>
          {errors.map((e, i) => <p key={i} className="th-form-error">{e}</p>)}
        </div>
      )}

      {/* Submit */}
      {!feedback && (
        <button
          className="th-btn th-btn--primary th-btn--full"
          onClick={handleSubmit}
        >
          Submit
        </button>
      )}

      {/* Score so far */}
      <div style={{ textAlign: 'center', marginTop: 'var(--th-space-4)' }}>
        <span style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)' }}>
          Score: {session?.casualScore ?? 0} / {currentWordIndex + (feedback === 'correct' ? 1 : 0)}
        </span>
      </div>
    </div>
  );
}
