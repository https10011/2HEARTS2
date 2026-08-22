/**
 * GamePlayScreen (Phase 11).
 *
 * Shared game play screen for all couple mini-games.
 * Handles: question display, answer input, turn management,
 * progress tracking, and game completion.
 *
 * Uses the GameService for all state transitions.
 */

import { useState, useCallback, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { GameService } from '../../services/game/gameService.ts';
import type { GameSession, GameType, PlayerRole } from '../../data/game/gameTypes.ts';
import { IconBack } from '../../components/index.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';

type GamePhase = 'intro' | 'playing' | 'turn-transition' | 'results';

export function GamePlayScreen() {
  const navigate = useNavigate();
  const { gameType } = useParams<{ gameType: string }>();
  const service = useMemo(() => new GameService(), []);

  const [phase, setPhase] = useState<GamePhase>('intro');
  const [session, setSession] = useState<GameSession | null>(null);
  const [currentTurn, setCurrentTurn] = useState<PlayerRole>('player1');
  const [answer, setAnswer] = useState('');
  const [selectedOption, setSelectedOption] = useState<number | undefined>(undefined);
  const [errors, setErrors] = useState<string[]>([]);
  const [submitted, setSubmitted] = useState(false);

  const gt = gameType as GameType | undefined;
  const definition = gt ? getGameDefinition(gt) : undefined;

  const startGame = useCallback(() => {
    if (!gt) return;
    const result = service.startGame(gt);
    setSession(result.session);
    if (definition?.turnBased) {
      setPhase('turn-transition');
    } else {
      setPhase('playing');
    }
  }, [gt, definition, service]);

  const handleAnswer = useCallback(() => {
    if (!session || !gt) return;

    const q = service.getCurrentQuestion(session, gt);
    if (!q) return;

    if (definition?.scoringType === 'choice') {
      if (selectedOption === undefined) {
        setErrors(['Please select an option.']);
        return;
      }
    } else {
      const validation = service.validateAnswer(answer, gt);
      if (!validation.ok) {
        setErrors(validation.errors);
        return;
      }
    }

    setErrors([]);
    setSubmitted(true);

    // Record answer
    const answerText = definition?.scoringType === 'choice'
      ? (q.options?.[selectedOption ?? 0] ?? '')
      : answer;

    const updated = service.answerQuestion(session, gt, currentTurn, answerText, selectedOption);

    // Check if game is complete
    if (updated.session.completed || session.currentRound >= (definition?.questionsPerRound ?? 10) - 1) {
      // This was the last question, check if all rounds are complete
      const completedRounds = updated.session.rounds.filter((r) => r.complete).length;
      if (completedRounds >= (definition?.questionsPerRound ?? 10)) {
        const finished = service.finishGame(updated.session, gt);
        navigate(`${RoutePath.appGames}/results`, {
          state: { result: finished.result, gameType: gt },
        });
        return;
      }
    }

    setSession(updated.session);

    // After short delay, advance
    setTimeout(() => {
      setSubmitted(false);
      setAnswer('');
      setSelectedOption(undefined);

      if (definition?.turnBased && currentTurn === 'player1') {
        setCurrentTurn('player2');
        setPhase('turn-transition');
      } else if (definition?.turnBased && currentTurn === 'player2') {
        // Both answered, advance to next question
        setCurrentTurn('player1');
        const nextResult = service.advanceToNext(updated.session, gt);
        if (nextResult) {
          setSession(nextResult.session);
          setPhase('playing');
        } else {
          const finResult = service.finishGame(updated.session, gt);
          navigate(`${RoutePath.appGames}/results`, {
            state: { result: finResult.result, gameType: gt },
          });
        }
      } else {
        // Non-turn-based: check if both answered current question
        const currentRound = updated.session.rounds.find(
          (r) => r.questionIndex === session.currentRound,
        );
        if (currentRound?.complete) {
          // Both answered, advance
          const next = service.advanceToNext(updated.session, gt);
          if (next) {
            setSession(next.session);
            setPhase('playing');
          } else {
            const finished = service.finishGame(updated.session, gt);
            navigate(`${RoutePath.appGames}/results`, {
              state: { result: finished.result, gameType: gt },
            });
          }
        } else {
          setCurrentTurn('player2');
          setPhase('turn-transition');
        }
      }
    }, 600);
  }, [session, gt, definition, currentTurn, answer, selectedOption, service, navigate]);

  if (!definition || !gt) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual">
            <span style={{ fontSize: '2rem' }}>?</span>
          </div>
          <h3 className="th-empty-state__title">Game not found</h3>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appGames)}>
            Back to Games
          </button>
        </div>
      </div>
    );
  }

  // Intro phase
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

  // Turn transition phase (for turn-based games)
  if (phase === 'turn-transition') {
    return (
      <div className="th-content-pad" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60dvh' }}>
        <div style={{ textAlign: 'center' }}>
          <h2 style={{ fontFamily: 'var(--th-font-family-display)', fontSize: 'var(--th-font-size-xl)', color: 'var(--th-color-text-primary)', marginBottom: 'var(--th-space-2)' }}>
            {currentTurn === 'player1' ? "Partner 1's Turn" : "Partner 2's Turn"}
          </h2>
          <p style={{ color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-6)' }}>
            Pass the device to the other partner
          </p>
          <button className="th-btn th-btn--primary" onClick={() => setPhase('playing')}>
            I'm Ready
          </button>
        </div>
      </div>
    );
  }

  // Playing phase
  if (!session) {
    setPhase('intro');
    return null;
  }
  const currentQuestion = service.getCurrentQuestion(session, gt);
  const progress = ((session.currentRound) / definition.questionsPerRound) * 100;

  if (!currentQuestion) {
    const finished = service.finishGame(session, gt);
    navigate(`${RoutePath.appGames}/results`, {
      state: { result: finished.result, gameType: gt },
    });
    return null;
  }

  return (
    <div className="th-content-pad">
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <button className="th-btn th-btn--ghost" onClick={() => navigate(RoutePath.appGames)} style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}>
          <IconBack size={20} />
        </button>
        <div style={{ flex: 1, textAlign: 'center' }}>
          <span style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)' }}>
            {definition.title}
          </span>
        </div>
        <span style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', minWidth: '40px', textAlign: 'right' }}>
          {session!.currentRound + 1}/{definition.questionsPerRound}
        </span>
      </div>

      {/* Progress bar */}
      <div className="th-progress-bar" style={{ marginBottom: 'var(--th-space-6)' }}>
        <div className="th-progress-bar__fill" style={{ width: `${progress}%` }} />
      </div>

      {/* Turn indicator for turn-based */}
      {definition.turnBased && (
        <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-4)' }}>
          <span style={{
            fontSize: 'var(--th-font-size-sm)',
            fontWeight: 'var(--th-font-weight-semibold)',
            color: 'var(--th-color-burgundy)',
          }}>
            {currentTurn === 'player1' ? "Partner 1" : "Partner 2"} — Answer this question
          </span>
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
          <span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            {currentQuestion.category}
          </span>
        )}
      </div>

      {/* Answer input */}
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
          <textarea
            className="th-textarea"
            value={answer}
            onChange={(e) => { setAnswer(e.target.value); setErrors([]); }}
            placeholder="Type your answer..."
            rows={4}
            autoFocus
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
      <button
        className="th-btn th-btn--primary th-btn--full"
        onClick={handleAnswer}
        disabled={submitted}
      >
        {submitted ? 'Answered!' : 'Submit Answer'}
      </button>
    </div>
  );
}
