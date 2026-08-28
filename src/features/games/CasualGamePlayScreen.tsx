/**
 * CasualGamePlayScreen (Stage 13 — Games Visual Productization).
 *
 * Shared play screen for casual single-player games (trivia, riddle room).
 * Enhanced: improved intro, question cards, answer options, feedback,
 * and results with casual personality. Engine unchanged.
 */

import { useState, useCallback } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconCheck, IconClose, IconBack, IconSmile, IconSparkle } from '../../components/index.ts';
import { createSession, recordCasualAnswer, completeCasualGame } from '../../services/game/gameEngine.ts';
import type { GameSession, GameType } from '../../data/game/gameTypes.ts';
import { resolveLevelConfig } from '../../data/game/gameTypes.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';
import { recordLevelCompletion } from '../../services/game/gameProgression.ts';
import {
  getGamePersonality,
  accuracyPercent,
  accuracyLabel,
  scoreDisplay,
  roundProgress,
} from './gamesPresentation.ts';

type GamePhase = 'intro' | 'playing' | 'results';

export function CasualGamePlayScreen() {
  const navigate = useNavigate();
  const { gameType } = useParams<{ gameType: string }>();
  const location = useLocation();
  const gt = gameType as GameType | undefined;
  const definition = gt ? getGameDefinition(gt) : undefined;
  const personality = gt ? getGamePersonality(gt) : undefined;
  const passedLevel = (location.state as { level?: number })?.level ?? 1;
  const levelConfig = resolveLevelConfig(passedLevel);

  const [phase, setPhase] = useState<GamePhase>('intro');
  const [session, setSession] = useState<GameSession | null>(null);
  const [selectedOption, setSelectedOption] = useState<number | undefined>(undefined);
  const [textAnswer, setTextAnswer] = useState('');
  const [feedback, setFeedback] = useState<'correct' | 'incorrect' | null>(null);
  const [errors, setErrors] = useState<string[]>([]);

  const startGame = useCallback(() => {
    if (!gt) return;
    const s = createSession(gt);
    setSession(s);
    setPhase('playing');
    setSelectedOption(undefined);
    setTextAnswer('');
    setFeedback(null);
  }, [gt]);

  const handleSubmit = useCallback(() => {
    if (!session || !gt || !definition) return;

    const questionIndex = session.currentRound;
    const question = definition.questions[questionIndex];
    if (!question) return;

    if (definition.scoringType === 'choice') {
      if (selectedOption === undefined) {
        setErrors(['Please select an option.']);
        return;
      }
    } else {
      if (!textAnswer.trim()) {
        setErrors(['Please enter your answer.']);
        return;
      }
    }

    setErrors([]);

    const answerText = definition.scoringType === 'choice'
      ? (question.options?.[selectedOption ?? 0] ?? '')
      : textAnswer.trim();

    const result = recordCasualAnswer(session, definition, questionIndex, answerText, selectedOption);
    if (!result) return;

    setSession(result.session);
    setFeedback(result.correct ? 'correct' : 'incorrect');

    setTimeout(() => {
      setFeedback(null);
      setSelectedOption(undefined);
      setTextAnswer('');

      if (result.gameOver) {
        if (gt) {
          const score = result.session.casualScore ?? 0;
          recordLevelCompletion(gt, passedLevel, score);
        }
        setPhase('results');
      }
    }, 1200);
  }, [session, gt, definition, selectedOption, textAnswer, passedLevel]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === 'Enter' && !feedback) {
        handleSubmit();
      }
    },
    [handleSubmit, feedback],
  );

  // --- Not found state ---
  if (!definition || !gt) {
    return (
      <div className="th-content-pad th-game-screen th-game-screen--warm">
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual"><span style={{ fontSize: 'var(--th-font-size-2xl)' }}>?</span></div>
          <h3 className="th-empty-state__title">Game not found</h3>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appGames)}>Back to Games</button>
        </div>
      </div>
    );
  }

  // --- Intro phase ---
  if (phase === 'intro') {
    return (
      <div className="th-content-pad th-game-intro th-game-intro--enhanced th-game-enter th-game-screen--warm">
        <div className="th-game-intro__circle">
          <IconSmile size={32} />
        </div>
        <h1 className="th-game-intro__title">{definition.title}</h1>
        {personality && (
          <div className="th-game-intro__vibe">{personality.vibe}</div>
        )}
        <div className="th-game-badge-group" style={{ margin: 'var(--th-space-4) 0' }}>
          <span className="th-game-level-badge th-badge-enter">Level {passedLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>
        <p className="th-game-intro__desc">{definition.description}</p>
        <p className="th-game-intro__meta" style={{ marginTop: 'var(--th-space-2)' }}>
          {definition.questionsPerRound} questions
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
    const total = definition.questionsPerRound;
    const accuracy = accuracyPercent(score, total);

    return (
      <div className="th-content-pad th-game-screen th-game-screen--warm">
        {/* Level-up celebration header */}
        <div className="th-game-result-hero th-result-enter">
          <div className="th-game-result-hero__ring th-badge-enter">
            <IconSparkle size={32} />
          </div>
          <div className="th-game-result-hero__title">Level Complete!</div>
          <div className="th-game-result-hero__subtitle">{definition.title}</div>
        </div>

        <div className="th-game-badge-group th-result-enter-delayed" style={{ marginBottom: 'var(--th-space-4)' }}>
          <span className="th-game-level-badge">Level {passedLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>

        {/* Score card */}
        <div className="th-game-score-card--enhanced th-result-enter-delayed">
          <div className="th-game-score-card__value">
            {scoreDisplay(score, total)}
          </div>
          <div className="th-game-score-card__label">
            correct ({accuracy}% accuracy)
          </div>
          <div className="th-game-score-card__message">
            {accuracyLabel(accuracy)}
          </div>
        </div>

        {/* Round breakdown */}
        <h2 className="th-game-breakdown-title th-result-enter-delayed-2">
          Round Breakdown
        </h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-2)', marginBottom: 'var(--th-space-6)' }}>
          {session?.rounds
            .filter((r) => r.complete)
            .map((r, i) => {
              const a = r.answers[0];
              const isCorrect = a?.matched ?? false;
              return (
                <div
                  key={i}
                  className={`th-game-round--enhanced ${isCorrect ? 'th-game-round--correct-enhanced' : 'th-game-round--incorrect-enhanced'} th-game-stagger`}
                  style={{ animationDelay: `${i * 30}ms`, background: 'var(--th-color-surface)', borderRadius: 'var(--th-radius-md)' }}
                >
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: 'var(--th-space-2)' }}>
                    <span style={{ color: isCorrect ? 'var(--th-color-success)' : 'var(--th-color-error)', flexShrink: 0, marginTop: '2px' }}>
                      {isCorrect ? <IconCheck size={14} /> : <IconClose size={14} />}
                    </span>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontSize: 'var(--th-font-size-sm)', fontWeight: 'var(--th-font-weight-semibold)', color: 'var(--th-color-text-primary)', marginBottom: '2px' }}>
                        Q{i + 1}: {r.question.text.length > 50 ? r.question.text.slice(0, 50) + '...' : r.question.text}
                      </div>
                      <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
                        Your answer: {a?.answer || '(none)'}
                        {!isCorrect && (
                          <span> · Correct: {r.question.correctAnswer ?? r.question.options?.[0]}</span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
        </div>

        {/* Actions */}
        <div className="th-game-actions--enhanced">
          <button className="th-btn th-btn--primary th-btn--full th-pressable" onClick={() => navigate(`${RoutePath.appGames}/${gt}`, { replace: true, state: { level: passedLevel + 1 } })}>
            Next Level
          </button>
          <button className="th-btn th-btn--secondary th-btn--full th-pressable" onClick={() => navigate(`${RoutePath.appGames}/${gt}`, { replace: true, state: { level: passedLevel } })}>
            Replay Level {passedLevel}
          </button>
          <button className="th-btn th-btn--secondary th-btn--full th-pressable" onClick={() => navigate(RoutePath.appGames)}>
            Back to Games
          </button>
        </div>
      </div>
    );
  }

  // --- Playing phase ---
  const questionIndex = session?.currentRound ?? 0;
  const currentQuestion = definition.questions[questionIndex];
  const progress = roundProgress(questionIndex, definition.questionsPerRound);

  if (!currentQuestion) {
    const { result } = completeCasualGame(session!);
    if (gt) {
      const score = result.casualResult?.score ?? 0;
      recordLevelCompletion(gt, passedLevel, score);
    }
    navigate(`${RoutePath.appGames}/${gt}/results`, {
      state: { result, gameType: gt, level: passedLevel },
    });
    return null;
  }

  return (
    <div className="th-content-pad th-game-screen th-game-screen--warm">
      {/* Header */}
      <div className="th-game-header">
        <button className="th-btn th-btn--ghost th-game-header__back" onClick={() => navigate(RoutePath.appGames)}>
          <IconBack size={20} />
        </button>
        <div className="th-game-header__title">{definition.title}</div>
        <span className="th-game-header__counter">
          {questionIndex + 1}/{definition.questionsPerRound}
        </span>
      </div>

      {/* Progress bar */}
      <div className="th-game-progress--enhanced">
        <div className="th-game-progress--enhanced__fill" style={{ width: `${progress}%` }} />
      </div>

      {/* Feedback */}
      {feedback && (
        <div className={`th-game-feedback--enhanced ${feedback === 'correct' ? 'th-game-feedback--correct-enhanced' : 'th-game-feedback--incorrect-enhanced'} th-game-enter`}>
          {feedback === 'correct' ? (
            <><IconCheck size={16} /> Correct!</>
          ) : (
            <><IconClose size={16} /> Wrong! The answer was: {currentQuestion.correctAnswer ?? currentQuestion.options?.[0]}</>
          )}
        </div>
      )}

      {/* Question card */}
      <div className={`th-game-question th-game-question--enhanced ${!feedback ? 'th-game-enter' : ''}`} key={questionIndex}>
        <span className="th-game-question__number">Question {questionIndex + 1}</span>
        <h2 className="th-game-question__text">{currentQuestion.text}</h2>
        {currentQuestion.category && (
          <span className="th-game-question__category">{currentQuestion.category}</span>
        )}
      </div>

      {/* Answer input */}
      {!feedback && (
        <>
          {definition.scoringType === 'choice' && currentQuestion.options ? (
            <div className="th-game-options">
              {currentQuestion.options.map((opt, i) => (
                <button
                  key={i}
                  className={`th-game-option ${selectedOption === i ? 'th-game-option--selected' : ''}`}
                  onClick={() => { setSelectedOption(i); setErrors([]); }}
                >
                  {opt}
                </button>
              ))}
            </div>
          ) : (
            <div style={{ marginBottom: 'var(--th-space-6)' }}>
              <input
                className="th-input"
                type="text"
                value={textAnswer}
                onChange={(e) => { setTextAnswer(e.target.value); setErrors([]); }}
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

          <button className="th-btn th-btn--primary th-btn--full th-pressable" onClick={handleSubmit}>
            Submit
          </button>
        </>
      )}

      {/* Score */}
      <div className="th-game-score-footer">
        Score: <span className="th-game-score-footer__number">{session?.casualScore ?? 0}</span> / {questionIndex + (feedback === 'correct' ? 1 : 0)}
      </div>
    </div>
  );
}
