/**
 * WordScrambleScreen (Stage 13 — Games Visual Productization).
 *
 * Word Scramble — unscramble love-themed words one at a time.
 * Enhanced: improved scramble display, intro, and results. Engine unchanged.
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
import { IconBack, IconCheck, IconClose, IconSparkle } from '../../components/index.ts';
import {
  getGamePersonality,
  accuracyPercent,
  accuracyLabel,
  scoreDisplay,
  roundProgress,
} from './gamesPresentation.ts';

type GamePhase = 'intro' | 'playing' | 'results';

export function WordScrambleScreen() {
  const navigate = useNavigate();
  const location = useLocation();
  const passedLevel = (location.state as { level?: number })?.level ?? 1;

  const [currentLevel, setCurrentLevel] = useState(passedLevel);
  const levelConfig = resolveLevelConfig(currentLevel);
  const wordCount = resolveWordScrambleCount(currentLevel);

  const definition = getGameDefinition('word-scramble');
  const personality = getGamePersonality('word-scramble');
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
      <div className="th-content-pad th-game-intro th-game-intro--enhanced th-game-enter th-game-screen--warm">
        <div className="th-game-intro__circle">
          <span style={{ fontSize: 'var(--th-font-size-xl)', fontWeight: 'var(--th-font-weight-bold)', fontFamily: 'var(--th-font-family-display)' }}>Aa</span>
        </div>
        <h1 className="th-game-intro__title">Word Scramble</h1>
        <div className="th-game-intro__vibe">{personality.vibe}</div>
        <div className="th-game-badge-group" style={{ margin: 'var(--th-space-4) 0' }}>
          <span className="th-game-level-badge th-badge-enter">Level {currentLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>
        <p className="th-game-intro__desc">Unscramble love-themed words!</p>
        <p className="th-game-intro__meta" style={{ marginTop: 'var(--th-space-2)' }}>
          {wordCount} words to unscramble
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
    const score = session?.casualScore ?? 0;
    const accuracy = accuracyPercent(score, wordCount);

    return (
      <div className="th-content-pad th-game-screen th-game-screen--warm">
        {/* Level-up celebration */}
        <div className="th-game-result-hero th-result-enter">
          <div className="th-game-result-hero__ring th-badge-enter">
            <IconSparkle size={32} />
          </div>
          <div className="th-game-result-hero__title">Level Complete!</div>
          <div className="th-game-result-hero__subtitle">Word Scramble</div>
        </div>

        <div className="th-game-badge-group th-result-enter-delayed" style={{ marginBottom: 'var(--th-space-4)' }}>
          <span className="th-game-level-badge">Level {currentLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>

        {/* Score card */}
        <div className="th-game-score-card--enhanced th-result-enter-delayed">
          <div className="th-game-score-card__value">
            {scoreDisplay(score, wordCount)}
          </div>
          <div className="th-game-score-card__label">
            words unscrambled ({accuracy}% accuracy)
          </div>
          <div className="th-game-score-card__message">
            {accuracyLabel(accuracy)}
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
  const progress = roundProgress(currentWordIndex, wordCount);

  return (
    <div className="th-content-pad th-game-screen th-game-screen--warm">
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
      <div className="th-game-progress--enhanced">
        <div className="th-game-progress--enhanced__fill" style={{ width: `${progress}%` }} />
      </div>

      {/* Scrambled word card */}
      <div className={`th-game-question th-game-question--enhanced th-game-enter ${feedback === 'correct' ? 'th-game-correct' : ''} ${feedback === 'incorrect' ? 'th-game-incorrect' : ''}`} key={currentWordIndex}>
        <span className="th-game-question__number">Word {currentWordIndex + 1}</span>
        <p className="th-scramble__hint">Unscramble this word:</p>
        <div className="th-scramble--enhanced">
          {scrambled.toUpperCase()}
        </div>
        {currentQuestion?.category && (
          <span className="th-game-question__category">{currentQuestion.category}</span>
        )}
      </div>

      {/* Feedback */}
      {feedback && (
        <div className={`th-game-feedback--enhanced ${feedback === 'correct' ? 'th-game-feedback--correct-enhanced' : 'th-game-feedback--incorrect-enhanced'} th-game-enter`}>
          {feedback === 'correct' ? (
            <><IconCheck size={16} /> Correct!</>
          ) : (
            <><IconClose size={16} /> The answer was: {correctAnswer}</>
          )}
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
      <div className="th-game-score-footer">
        Score: <span className="th-game-score-footer__number">{session?.casualScore ?? 0}</span> / {currentWordIndex + (feedback === 'correct' ? 1 : 0)}
      </div>
    </div>
  );
}
