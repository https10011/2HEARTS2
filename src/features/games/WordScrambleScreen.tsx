/**
 * WordScrambleScreen (Phase 12, Phase 29 visual polish).
 *
 * Word Scramble — unscramble love-themed words one at a time.
 * Uses createWordScrambleSession + scrambleWord + validateScrambleGuess
 * from the shared game engine.
 */

import { useState, useCallback, useMemo } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  createWordScrambleSession,
  scrambleWord,
  validateScrambleGuess,
  resolveWordScrambleCount,
  selectQuestionsForLevel,
} from '../../services/game/gameEngine.ts';
import { resolveLevelConfig } from '../../data/game/gameTypes.ts';
import type { GameSession } from '../../data/game/gameTypes.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';
import { IconBack } from '../../components/index.ts';

type GamePhase = 'intro' | 'playing' | 'results';

export function WordScrambleScreen() {
  const navigate = useNavigate();
  const location = useLocation();
  const passedLevel = (location.state as { level?: number })?.level ?? 1;

  const [currentLevel, setCurrentLevel] = useState(passedLevel);
  const levelConfig = resolveLevelConfig(currentLevel);
  const wordCount = resolveWordScrambleCount(currentLevel);

  const definition = getGameDefinition('word-scramble');
  const allQuestions = definition?.questions ?? [];
  const questions = useMemo(
    () => selectQuestionsForLevel(allQuestions, currentLevel, wordCount),
    [allQuestions, currentLevel, wordCount],
  );

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
  }, [correctAnswer]);

  const startGame = useCallback(() => {
    const s = createWordScrambleSession(wordCount);
    s.level = currentLevel;
    s.difficulty = levelConfig.difficulty;
    setSession(s);
    setPhase('playing');
    setGuess('');
    setFeedback(null);
    setErrors([]);
  }, [wordCount, currentLevel, levelConfig.difficulty]);

  const goToNextLevel = useCallback(() => {
    const next = currentLevel + 1;
    setCurrentLevel(next);
    setPhase('intro');
    setSession(null);
  }, [currentLevel]);

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

  // --- Intro phase ---
  if (phase === 'intro') {
    return (
      <div className="th-content-pad th-game-intro th-game-enter">
        <div className="th-game-intro__icon">
          <span style={{ fontSize: '1.8rem' }}>Aa</span>
        </div>
        <h1 className="th-game-intro__title">Word Scramble</h1>
        <div className="th-game-badge-group" style={{ marginBottom: 'var(--th-space-3)' }}>
          <span className="th-game-level-badge th-badge-enter">Level {currentLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>
        <p className="th-game-intro__desc">Unscramble love-themed words!</p>
        <p className="th-game-intro__meta">{wordCount} words to unscramble</p>
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
    const score = session?.casualScore ?? 0;
    const accuracy = wordCount > 0 ? Math.round((score / wordCount) * 100) : 0;

    let message: string;
    if (accuracy >= 90) message = 'Word wizard!';
    else if (accuracy >= 70) message = 'Great vocabulary!';
    else if (accuracy >= 50) message = 'Nice effort!';
    else message = 'Words are tricky!';

    return (
      <div className="th-content-pad th-game-screen">
        {/* Level-up celebration */}
        <div className="th-game-level-up th-result-enter">
          <div className="th-game-level-up__icon th-badge-enter">
            <span style={{ fontSize: '1.4rem' }}>Aa</span>
          </div>
          <div className="th-game-level-up__text">Level Complete!</div>
          <div className="th-game-level-up__sub">Word Scramble</div>
        </div>

        <div className="th-game-badge-group th-result-enter-delayed" style={{ marginBottom: 'var(--th-space-4)' }}>
          <span className="th-game-level-badge">Level {currentLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>

        {/* Score card */}
        <div className="th-game-result-card th-result-enter-delayed">
          <div className="th-game-result-card__score">{score} / {wordCount}</div>
          <div className="th-game-result-card__label">words unscrambled ({accuracy}% accuracy)</div>
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
  const progress = (currentWordIndex / wordCount) * 100;

  return (
    <div className="th-content-pad th-game-screen">
      {/* Header */}
      <div className="th-game-header">
        <button className="th-btn th-btn--ghost th-game-header__back" onClick={() => navigate(RoutePath.appGames)}>
          <IconBack size={20} />
        </button>
        <div className="th-game-header__title">Word Scramble</div>
        <span className="th-game-header__counter">
          {currentWordIndex + 1}/{wordCount}
        </span>
      </div>

      {/* Progress bar */}
      <div className="th-progress-bar" style={{ marginBottom: 'var(--th-space-6)' }}>
        <div className="th-progress-bar__fill" style={{ width: `${progress}%` }} />
      </div>

      {/* Scrambled word card */}
      <div className={`th-game-question th-game-enter ${feedback === 'correct' ? 'th-game-correct' : ''} ${feedback === 'incorrect' ? 'th-game-incorrect' : ''}`} key={currentWordIndex}>
        <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)', marginBottom: 'var(--th-space-3)' }}>
          Unscramble this word:
        </p>
        <div className="th-scramble-display--enhanced">
          {scrambled.toUpperCase()}
        </div>
        {currentQuestion?.category && (
          <span className="th-game-question__category">{currentQuestion.category}</span>
        )}
      </div>

      {/* Feedback */}
      {feedback && (
        <div className={`th-game-feedback th-game-${feedback}`}>
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
          className="th-btn th-btn--primary th-btn--full th-pressable"
          onClick={handleSubmit}
        >
          Submit
        </button>
      )}

      {/* Score */}
      <div className="th-game-score">
        Score: <span className="th-game-score__number">{session?.casualScore ?? 0}</span> / {currentWordIndex + (feedback === 'correct' ? 1 : 0)}
      </div>
    </div>
  );
}
