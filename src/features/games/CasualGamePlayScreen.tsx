/**
 * CasualGamePlayScreen (Phase 12).
 *
 * Shared play screen for casual single-player games (trivia, riddle room).
 * Uses the same question/answer model as couple games but with single-player
 * mechanics — no turn transitions, just sequential questions.
 *
 * Reuses: recordCasualAnswer, completeCasualGame from the game engine.
 */

import { useState, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { createSession, recordCasualAnswer, completeCasualGame } from '../../services/game/gameEngine.ts';
import type { GameSession, GameType } from '../../data/game/gameTypes.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';
import { IconBack } from '../../components/index.ts';

type GamePhase = 'intro' | 'playing' | 'results';

export function CasualGamePlayScreen() {
  const navigate = useNavigate();
  const { gameType } = useParams<{ gameType: string }>();
  const gt = gameType as GameType | undefined;
  const definition = gt ? getGameDefinition(gt) : undefined;

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

    // Validate
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
        setPhase('results');
      }
    }, 1200);
  }, [session, gt, definition, selectedOption, textAnswer]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === 'Enter' && !feedback) {
        handleSubmit();
      }
    },
    [handleSubmit, feedback],
  );

  if (!definition || !gt) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state">
          <h3>Game not found</h3>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appGames)}>
            Back to Games
          </button>
        </div>
      </div>
    );
  }

  if (phase === 'intro') {
    return (
      <div className="th-content-pad" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60dvh' }}>
        <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-8)' }}>
          <h1 style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-2xl)', color: 'var(--th-color-text-primary)', marginBottom: 'var(--th-space-3)' }}>
            {definition.title}
          </h1>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-md)', maxWidth: '36ch', margin: '0 auto' }}>
            {definition.description}
          </p>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)', marginTop: 'var(--th-space-3)' }}>
            {definition.questionsPerRound} questions
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
    const total = definition.questionsPerRound;
    const accuracy = total > 0 ? Math.round((score / total) * 100) : 0;

    let message: string;
    if (accuracy >= 90) message = 'Outstanding!';
    else if (accuracy >= 70) message = 'Well done!';
    else if (accuracy >= 50) message = 'Good effort!';
    else message = 'Keep trying!';

    return (
      <div className="th-content-pad">
        <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-6)' }}>
          <h1 style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-2xl)', color: 'var(--th-color-text-primary)', marginBottom: 'var(--th-space-2)' }}>
            Game Complete!
          </h1>
          <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-md)' }}>{definition.title}</p>
        </div>

        <div className="th-relationship-card" style={{ marginBottom: 'var(--th-space-6)', textAlign: 'center' }}>
          <div style={{ fontSize: 'var(--th-font-size-3xl)', fontWeight: 'var(--th-font-weight-bold)', fontFamily: 'var(--th-font-family-display)', marginBottom: 'var(--th-space-2)' }}>
            {score} / {total}
          </div>
          <div style={{ fontSize: 'var(--th-font-size-sm)', opacity: 0.85 }}>correct ({accuracy}% accuracy)</div>
          <div style={{ marginTop: 'var(--th-space-3)', fontSize: 'var(--th-font-size-md)', opacity: 0.9 }}>
            {message}
          </div>
        </div>

        {/* Round breakdown */}
        <h2 style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-3)' }}>
          Round Breakdown
        </h2>
        <div className="th-hub-grid" style={{ marginBottom: 'var(--th-space-6)' }}>
          {session?.rounds
            .filter((r) => r.complete)
            .map((r, i) => {
              const a = r.answers[0];
              const isCorrect = a?.matched ?? false;
              return (
                <div
                  key={i}
                  className="th-feature-card"
                  style={{ cursor: 'default', borderLeftColor: isCorrect ? 'var(--th-color-burgundy)' : undefined }}
                >
                  <div className="th-feature-card__body">
                    <div className="th-feature-card__title" style={{ fontSize: 'var(--th-font-size-sm)' }}>
                      {isCorrect ? '✅' : '❌'} Q{i + 1}: {r.question.text.length > 50 ? r.question.text.slice(0, 50) + '...' : r.question.text}
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
  const questionIndex = session?.currentRound ?? 0;
  const currentQuestion = definition.questions[questionIndex];
  const progress = (questionIndex / definition.questionsPerRound) * 100;

  if (!currentQuestion) {
    const { result } = completeCasualGame(session!);
    navigate(`${RoutePath.appGames}/${gt}/results`, {
      state: { result, gameType: gt },
    });
    return null;
  }

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
            {definition.title}
          </span>
        </div>
        <span style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', minWidth: '40px', textAlign: 'right' }}>
          {questionIndex + 1}/{definition.questionsPerRound}
        </span>
      </div>

      {/* Progress bar */}
      <div className="th-progress-bar" style={{ marginBottom: 'var(--th-space-6)' }}>
        <div className="th-progress-bar__fill" style={{ width: `${progress}%` }} />
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
          {feedback === 'correct' ? 'Correct!' : `Wrong! The answer was: ${currentQuestion.correctAnswer ?? currentQuestion.options?.[0]}`}
        </div>
      )}

      {/* Question */}
      <div style={{ marginBottom: 'var(--th-space-6)', textAlign: 'center' }}>
        <h2 style={{
          fontFamily: 'var(--th-font-family-display)',
          fontSize: 'var(--th-font-size-xl)',
          color: 'var(--th-color-text-primary)',
          lineHeight: 'var(--th-line-height-tight)',
        }}>
          {currentQuestion.text}
        </h2>
        {currentQuestion.category && (
          <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', marginTop: 'var(--th-space-2)' }}>
            {currentQuestion.category}
          </span>
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

          <button className="th-btn th-btn--primary th-btn--full" onClick={handleSubmit}>
            Submit
          </button>
        </>
      )}

      {/* Score so far */}
      <div style={{ textAlign: 'center', marginTop: 'var(--th-space-4)' }}>
        <span style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)' }}>
          Score: {session?.casualScore ?? 0} / {questionIndex + (feedback === 'correct' ? 1 : 0)}
        </span>
      </div>
    </div>
  );
}
