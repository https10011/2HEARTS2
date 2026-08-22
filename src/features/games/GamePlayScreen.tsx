/**
 * GamePlayScreen (Phase 11, Phase 29 visual polish).
 *
 * Shared game play screen for all couple mini-games.
 * Handles: question display, answer input, turn management,
 * progress tracking, and game completion.
 *
 * Uses the GameService for all state transitions.
 */

import { useState, useCallback, useMemo } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { GameService } from '../../services/game/gameService.ts';
import { resolveLevelConfig } from '../../data/game/gameTypes.ts';
import type { GameSession, GameType, PlayerRole } from '../../data/game/gameTypes.ts';
import { IconBack, IconHeart } from '../../components/index.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';

type GamePhase = 'intro' | 'playing' | 'turn-transition' | 'results';

export function GamePlayScreen() {
  const navigate = useNavigate();
  const location = useLocation();
  const { gameType } = useParams<{ gameType: string }>();
  const service = useMemo(() => new GameService(), []);
  const passedLevel = (location.state as { level?: number })?.level ?? 1;
  const levelConfig = useMemo(() => resolveLevelConfig(passedLevel), [passedLevel]);

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
    const result = service.startGameAtLevel(gt, passedLevel);
    setSession(result.session);
    if (definition?.turnBased) {
      setPhase('turn-transition');
    } else {
      setPhase('playing');
    }
  }, [gt, definition, service, passedLevel]);

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

    const answerText = definition?.scoringType === 'choice'
      ? (q.options?.[selectedOption ?? 0] ?? '')
      : answer;

    const updated = service.answerQuestion(session, gt, currentTurn, answerText, selectedOption);

    if (updated.session.completed || session.currentRound >= (definition?.questionsPerRound ?? 10) - 1) {
      const completedRounds = updated.session.rounds.filter((r) => r.complete).length;
      if (completedRounds >= (definition?.questionsPerRound ?? 10)) {
        const finished = service.finishGame(updated.session, gt);
        navigate(`${RoutePath.appGames}/results`, {
          state: { result: finished.result, gameType: gt, level: passedLevel },
        });
        return;
      }
    }

    setSession(updated.session);

    setTimeout(() => {
      setSubmitted(false);
      setAnswer('');
      setSelectedOption(undefined);

      if (definition?.turnBased && currentTurn === 'player1') {
        setCurrentTurn('player2');
        setPhase('turn-transition');
      } else if (definition?.turnBased && currentTurn === 'player2') {
        setCurrentTurn('player1');
        const nextResult = service.advanceToNext(updated.session, gt);
        if (nextResult) {
          setSession(nextResult.session);
          setPhase('playing');
        } else {
          const finResult = service.finishGame(updated.session, gt);
          navigate(`${RoutePath.appGames}/results`, {
            state: { result: finResult.result, gameType: gt, level: passedLevel },
          });
        }
      } else {
        const currentRound = updated.session.rounds.find(
          (r) => r.questionIndex === session.currentRound,
        );
        if (currentRound?.complete) {
          const next = service.advanceToNext(updated.session, gt);
          if (next) {
            setSession(next.session);
            setPhase('playing');
          } else {
            const finished = service.finishGame(updated.session, gt);
            navigate(`${RoutePath.appGames}/results`, {
              state: { result: finished.result, gameType: gt, level: passedLevel },
            });
          }
        } else {
          setCurrentTurn('player2');
          setPhase('turn-transition');
        }
      }
    }, 600);
  }, [session, gt, definition, currentTurn, answer, selectedOption, service, navigate, passedLevel]);

  // --- Error / not found state ---
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
          <IconHeart size={28} />
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

  // --- Turn transition phase ---
  if (phase === 'turn-transition') {
    return (
      <div className="th-content-pad th-game-intro th-game-enter">
        <div className="th-game-turn-badge">
          {currentTurn === 'player1' ? "Partner 1's Turn" : "Partner 2's Turn"}
        </div>
        <p className="th-game-intro__desc" style={{ marginTop: 'var(--th-space-4)' }}>
          Pass the device to the other partner
        </p>
        <button className="th-btn th-btn--primary" style={{ marginTop: 'var(--th-space-6)' }} onClick={() => setPhase('playing')}>
          I'm Ready
        </button>
      </div>
    );
  }

  // --- Playing phase ---
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
    <div className="th-content-pad th-game-screen">
      {/* Header */}
      <div className="th-game-header">
        <button className="th-btn th-btn--ghost th-game-header__back" onClick={() => navigate(RoutePath.appGames)}>
          <IconBack size={20} />
        </button>
        <div className="th-game-header__title">{definition.title}</div>
        <span className="th-game-header__counter">
          {session!.currentRound + 1}/{definition.questionsPerRound}
        </span>
      </div>

      {/* Progress bar */}
      <div className="th-progress-bar" style={{ marginBottom: 'var(--th-space-4)' }}>
        <div className="th-progress-bar__fill" style={{ width: `${progress}%` }} />
      </div>

      {/* Turn indicator for turn-based */}
      {definition.turnBased && (
        <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-4)' }}>
          <span className="th-game-turn-badge">
            {currentTurn === 'player1' ? "Partner 1" : "Partner 2"} — Answer this question
          </span>
        </div>
      )}

      {/* Question card */}
      <div className="th-game-question th-game-enter" key={session.currentRound}>
        <h2 className="th-game-question__text">{currentQuestion.text}</h2>
        {currentQuestion.category && (
          <span className="th-game-question__category">{currentQuestion.category}</span>
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
        className="th-btn th-btn--primary th-btn--full th-pressable"
        onClick={handleAnswer}
        disabled={submitted}
      >
        {submitted ? 'Answered!' : 'Submit Answer'}
      </button>

      {/* Score */}
      <div className="th-game-score">
        Round {session.currentRound + 1} of {definition.questionsPerRound}
      </div>
    </div>
  );
}
