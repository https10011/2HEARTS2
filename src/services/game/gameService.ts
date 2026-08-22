/**
 * Game service (Phase 11).
 *
 * Application-facing boundary over the game engine.
 * Manages session lifecycle, validates input, normalizes errors.
 * Keeps domain logic out of React components.
 *
 * Layer: UI → GameService → GameEngine → Local persistence.
 */

import { AppError } from '../errors/appError.ts';
import { systemClock, type Clock } from '../../utils/time.ts';
import type { GameDefinition, GameSession, GameType, PlayerRole } from '../../data/game/gameTypes.ts';
import { resolveLevelConfig } from '../../data/game/gameTypes.ts';
import {
  completeGame,
  computeScores,
  createSession,
  nextQuestion,
  recordAnswer,
} from './gameEngine.ts';
import { getGameDefinition } from '../../customization/games/gameContent.ts';

export interface GameServiceResult {
  session: GameSession;
  definition: GameDefinition;
}

export class GameService {
  private readonly clock: Clock;

  constructor(clock: Clock = systemClock) {
    this.clock = clock;
  }

  /** Starts a new game session. */
  startGame(gameType: GameType): GameServiceResult {
    const definition = getGameDefinition(gameType);
    if (!definition) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Unknown game type.',
      });
    }
    if (definition.questions.length === 0) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'No questions available for this game.',
      });
    }
    const session = createSession(gameType, this.clock);
    return { session, definition };
  }

  /** Records a player's answer and advances the game state. */
  answerQuestion(
    session: GameSession,
    gameType: GameType,
    player: PlayerRole,
    answer: string,
    selectedOption?: number,
  ): GameServiceResult {
    const definition = this.getDefinition(gameType);
    const currentQuestionIndex = session.currentRound;

    let updated = recordAnswer(session, definition, currentQuestionIndex, player, answer, selectedOption, this.clock);

    // Check if both players answered
    const currentRound = updated.rounds.find((r) => r.questionIndex === currentQuestionIndex);
    if (currentRound?.complete) {
      // Compute scores for completed rounds
      updated = computeScores(updated, definition, this.clock);
    }

    return { session: updated, definition };
  }

  /** Advances to the next question. Returns null result if game is done. */
  advanceToNext(session: GameSession, gameType: GameType): { session: GameSession; definition: GameDefinition } | null {
    const definition = this.getDefinition(gameType);
    const next = nextQuestion(session, definition);
    if (next === null) return null;

    return {
      session: { ...session, currentRound: next, updatedAt: new Date().toISOString() },
      definition,
    };
  }

  /** Completes the game and generates results. */
  finishGame(session: GameSession, gameType: GameType): { session: GameSession; result: ReturnType<typeof completeGame>['result'] } {
    const definition = this.getDefinition(gameType);
    const { session: completed, result } = completeGame(session, definition, this.clock);
    return { session: completed, result };
  }

  /** Gets the current question for display. */
  getCurrentQuestion(session: GameSession, gameType: GameType) {
    const definition = this.getDefinition(gameType);
    const idx = session.currentRound;
    if (idx >= definition.questionsPerRound) return null;
    return definition.questions[idx] ?? null;
  }

  /** Starts a new game at a specific level with appropriate difficulty. */
  startGameAtLevel(gameType: GameType, level: number): GameServiceResult & { levelConfig: ReturnType<typeof resolveLevelConfig> } {
    const definition = this.getDefinition(gameType);
    const levelConfig = resolveLevelConfig(level);
    const session = createSession(gameType, this.clock);
    session.level = level;
    session.difficulty = levelConfig.difficulty;
    session.streak = 0;
    return { session, definition, levelConfig };
  }

  /** Gets the definition for a game type. */
  getDefinition(gameType: GameType): GameDefinition {
    const def = getGameDefinition(gameType);
    if (!def) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Unknown game type.',
      });
    }
    return def;
  }

  /** Validates whether a player answer is acceptable. */
  validateAnswer(answer: string, _gameType?: GameType): { ok: boolean; errors: string[] } {
    const trimmed = answer.trim();
    if (!trimmed) return { ok: false, errors: ['Please provide an answer.'] };
    if (trimmed.length > 500) return { ok: false, errors: ['Answer is too long (max 500 characters).'] };
    return { ok: true, errors: [] };
  }
}
