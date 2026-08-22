/**
 * CasualGamePlayScreen (Phase 12, Phase 29 visual polish).
 *
 * Shared play screen for casual single-player games (trivia, riddle room).
 * Uses the same question/answer model as couple games but with single-player
 * mechanics — no turn transitions, just sequential questions.
 *
 * Reuses: recordCasualAnswer, completeCasualGame from the game engine.
 */

import { useState, useCallback } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconCheck, IconClose, IconBack, IconSmile } from '../../components/index.ts';
import { createSession, recordCasualAnswer, completeCasualGame } from '../../services/game/gameEngine.ts';
import type { GameSession, GameType } from '../../data/game/gameTypes.ts';
import { resolveLevelConfig } from '../../data/game/gameTypes.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';
import { recordLevelCompletion } from '../../services/game/gameProgression.ts';

type GamePhase = 'intro' | 'playing' | 'results';

export function CasualGamePlayScreen() {
  const navigate = useNavigate();
  const { gameType } = useParams<{ gameType: string }>();
  const location = useLocation();
  const gt = gameType as GameType | undefined;
  const definition = gt ? getGameDefinition(gt) : undefined;
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
      <div className="th-content-pad th-game-screen">
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual"><span style={{ fontSize: '2rem' }}>?</span></div>
          <h3 className="th-empty-state__title">Game not found</h3>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appGames)}>Back to Games</button>
        </div>
      </div>
    );
  }

  // --- Intro phase ---
  if (phase === 'intro') {
    return (
      <div className="th-content-pad th-game-intro th-game-enter">
        <div className="th-game-intro__icon">
          <IconSmile size={28} />
        </div>
        <h1 className="th-game-intro__title">{definition.title}</h1>
        <div className="th-game-badge-group" style={{ marginBottom: 'var(--th-space-3)' }}>
          <span className="th-game-level-badge th-badge-enter">Level {passedLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>
        <p className="th-game-intro__desc">{definition.description}</p>
        <p className="th-game-intro__meta">{definition.questionsPerRound} questions</p>
        <div style={{ marginTop: 'var(--th-space-6)' }}>
          <button className="th-btn th-btn--primary" onClick={startGame}>Start Game</button>
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
    const accuracy = total > 0 ? Math.round((score / total) * 100) : 0;

    let message: string;
    if (accuracy >= 90) message = 'Outstanding!';
    else if (accuracy >= 70) message = 'Well done!';
    else if (accuracy >= 50) message = 'Good effort!';
    else message = 'Keep trying!';

    return (
      <div className="th-content-pad th-game-screen">
        {/* Level-up celebration header */}
        <div className="th-game-level-up th-result-enter">
          <div className="th-game-level-up__icon th-badge-enter">1</div>
          <div className="th-game-level-up__text">Level Complete!</div>
          <div className="th-game-level-up__sub">{definition.title}</div>
        </div>

        <div className="th-game-badge-group th-result-enter-delayed" style={{ marginBottom: 'var(--th-space-4)' }}>
          <span className="th-game-level-badge">Level {passedLevel}</span>
          <span className="th-game-difficulty-badge">{levelConfig.difficulty}</span>
        </div>

        {/* Score card */}
        <div className="th-game-result-card th-result-enter-delayed">
          <div className="th-game-result-card__score">{score} / {total}</div>
          <div className="th-game-result-card__label">correct ({accuracy}% accuracy)</div>
          <div className="th-game-result-card__message">{message}</div>
        </div>

        {/* Round breakdown */}
        <h2 style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-3)' }} className="th-result-enter-delayed-2">
          Round Breakdown
        </h2>
        <div className="th-hub-grid th-result-enter-delayed-2" style={{ marginBottom: 'var(--th-space-6)' }}>
          {session?.rounds
            .filter((r) => r.complete)
            .map((r, i) => {
              const a = r.answers[0];
              const isCorrect = a?.matched ?? false;
              return (
                <div
                  key={i}
                  className={`th-feature-card th-feature-card--enhanced th-stagger-item th-game-round ${isCorrect ? 'th-game-round--correct' : 'th-game-round--incorrect'}`}
                  style={{ cursor: 'default' }}
                >
                  <div className="th-feature-card__body">
                    <div className="th-feature-card__title" style={{ fontSize: 'var(--th-font-size-sm)' }}>
                      <span style={{ color: isCorrect ? 'var(--th-color-burgundy)' : 'var(--th-color-text-secondary)', display: 'inline-flex', verticalAlign: '-2px', marginRight: 'var(--th-space-1)' }}>
                        {isCorrect ? <IconCheck size={14} /> : <IconClose size={14} />}
                      </span>
                      Q{i + 1}: {r.question.text.length > 50 ? r.question.text.slice(0, 50) + '...' : r.question.text}
                    </div>
                    <div className="th-feature-card__desc">
                      Your answer: {a?.answer || '(none)'}
                      {!isCorrect && (
                        <span> · Correct: {r.question.correctAnswer ?? r.question.options?.[0]}</span>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
        </div>

        {/* Actions */}
        <div className="th-game-actions">
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
  const progress = (questionIndex / definition.questionsPerRound) * 100;

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
    <div className="th-content-pad th-game-screen">
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
      <div className="th-progress-bar" style={{ marginBottom: 'var(--th-space-4)' }}>
        <div className="th-progress-bar__fill" style={{ width: `${progress}%` }} />
      </div>

      {/* Feedback */}
      {feedback && (
        <div className={`th-game-feedback th-game-${feedback} ${feedback === 'correct' ? 'th-game-correct' : 'th-game-incorrect'}`}>
          {feedback === 'correct' ? 'Correct!' : `Wrong! The answer was: ${currentQuestion.correctAnswer ?? currentQuestion.options?.[0]}`}
        </div>
      )}

      {/* Question card */}
      <div className={`th-game-question ${!feedback ? 'th-game-enter' : ''}`} key={questionIndex}>
        <h2 className="th-game-question__text">{currentQuestion.text}</h2>
        {currentQuestion.category && (
          <span className="th-game-question__category">{currentQuestion.category}</span>
        )}
      </div>

      {/* Answer input */}
      {!feedback && (
        <>
          {definition.scoringType === 'choice' && currentQuestion.options ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)', marginBottom: 'var(--th-space-6)' }}>
              {currentQuestion.options.map((opt, i) => (
                <button
                  key={i}
                  className={`th-option-chip ${selectedOption === i ? 'th-option-chip--active' : ''}`}
                  onClick={() => { setSelectedOption(i); setErrors([]); }}
                  style={{ width: '100%', textAlign: 'center', padding: 'var(--th-space-4)' }}
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
      <div className="th-game-score">
        Score: <span className="th-game-score__number">{session?.casualScore ?? 0}</span> / {questionIndex + (feedback === 'correct' ? 1 : 0)}
      </div>
    </div>
  );
}
