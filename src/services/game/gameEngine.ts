/**
 * Game engine (Phase 11).
 *
 * Reusable core logic for all couple mini-games. The engine is
 * data-driven: individual games provide definitions, the engine
 * processes state transitions, scoring, and result generation.
 *
 * Architecture: UI → GameService → GameEngine → Local persistence.
 * The engine owns NO React, NO DB, NO UI — pure state logic.
 */

import { newId } from '../../utils/ids.ts';
import { nowIso, systemClock, type Clock } from '../../utils/time.ts';
import type {
  GameDefinition,
  GameResult,
  GameRound,
  GameSession,
  GameType,
  PlayerAnswer,
  PlayerRole,
} from '../../data/game/gameTypes.ts';

// ---------------------------------------------------------------------------
// Session management
// ---------------------------------------------------------------------------

export function createSession(gameType: GameType, clock: Clock = systemClock): GameSession {
  const now = nowIso(clock);
  return {
    id: newId(),
    gameType,
    currentRound: 0,
    rounds: [],
    player1Score: 0,
    player2Score: 0,
    completed: false,
    createdAt: now,
    updatedAt: now,
  };
}

// ---------------------------------------------------------------------------
// Answer recording
// ---------------------------------------------------------------------------

/**
 * Records a player's answer for the current round.
 * For turn-based games (who-knows, guess-my-answer), each question
 * needs two answers (one per player). For choice games, each question
 * needs one answer per player.
 */
export function recordAnswer(
  session: GameSession,
  definition: GameDefinition,
  questionIndex: number,
  player: PlayerRole,
  answer: string,
  selectedOption?: number,
  clock: Clock = systemClock,
): GameSession {
  if (session.completed) return session;

  const question = definition.questions[questionIndex];
  if (!question) return session;

  // Find or create the round
  let round = session.rounds.find((r) => r.questionIndex === questionIndex);
  if (!round) {
    round = {
      questionIndex,
      question,
      answers: [],
      complete: false,
    };
    session.rounds.push(round);
  }

  // Remove any existing answer from this player for this question
  round.answers = round.answers.filter((a) => a.player !== player);

  const playerAnswer: PlayerAnswer = {
    questionId: question.id,
    player,
    answer: answer.trim(),
    selectedOption,
    matched: false,
  };

  round.answers.push(playerAnswer);

  // Check if round is complete
  if (definition.turnBased) {
    round.complete = round.answers.length >= 2;
  } else {
    // Non-turn-based: complete when both players have answered
    round.complete = round.answers.length >= 2;
  }

  return {
    ...session,
    updatedAt: nowIso(clock),
  };
}

// ---------------------------------------------------------------------------
// Scoring
// ---------------------------------------------------------------------------

/**
 * Computes scores for all completed rounds.
 * Returns updated session with scores.
 */
export function computeScores(
  session: GameSession,
  definition: GameDefinition,
  clock: Clock = systemClock,
): GameSession {
  let p1Score = 0;
  let p2Score = 0;

  for (const round of session.rounds) {
    if (!round.complete) continue;

    const result = scoreRound(round, definition);
    if (result.player1Matched) p1Score++;
    if (result.player2Matched) p2Score++;

    // Mark matched status on answers
    round.answers = round.answers.map((a) => ({
      ...a,
      matched: a.player === 'player1' ? result.player1Matched : result.player2Matched,
    }));
  }

  return {
    ...session,
    player1Score: p1Score,
    player2Score: p2Score,
    updatedAt: nowIso(clock),
  };
}

export interface RoundScore {
  player1Matched: boolean;
  player2Matched: boolean;
  bothMatched: boolean;
}

function scoreRound(round: GameRound, definition: GameDefinition): RoundScore {
  const p1 = round.answers.find((a) => a.player === 'player1');
  const p2 = round.answers.find((a) => a.player === 'player2');

  if (!p1 || !p2) return { player1Matched: false, player2Matched: false, bothMatched: false };

  if (definition.scoringType === 'match') {
    // For "who knows who better" / "guess my answer":
    // Check if player2's answer matches the correct answer (player1's answer)
    const correctAnswer = round.question.correctAnswer ?? p1.answer;
    const p2Match = normalizeAnswer(p2.answer) === normalizeAnswer(correctAnswer);
    // Player1 always "matches" their own answer
    return { player1Matched: true, player2Matched: p2Match, bothMatched: p2Match };
  }

  if (definition.scoringType === 'choice') {
    // For "would you rather" / "this or that":
    // Match if both chose the same option
    const bothMatched = p1.selectedOption === p2.selectedOption && p1.selectedOption !== undefined;
    return { player1Matched: bothMatched, player2Matched: bothMatched, bothMatched };
  }

  // 'none' scoring: no matching
  return { player1Matched: false, player2Matched: false, bothMatched: false };
}

function normalizeAnswer(answer: string): string {
  return answer.toLowerCase().trim().replace(/\s+/g, ' ');
}

// ---------------------------------------------------------------------------
// Game progression
// ---------------------------------------------------------------------------

/**
 * Advances to the next question. Returns null if the game is complete.
 */
export function nextQuestion(
  session: GameSession,
  definition: GameDefinition,
): number | null {
  const nextIndex = session.currentRound + 1;
  if (nextIndex >= definition.questionsPerRound) {
    return null; // Game complete
  }
  return nextIndex;
}

/**
 * Completes the game and generates the final result.
 */
export function completeGame(
  session: GameSession,
  definition: GameDefinition,
  clock: Clock = systemClock,
): { session: GameSession; result: GameResult } {
  const scored = computeScores(session, definition, clock);
  const completed: GameSession = {
    ...scored,
    completed: true,
    updatedAt: nowIso(clock),
  };

  const result = generateResult(completed, definition);
  return { session: completed, result };
}

// ---------------------------------------------------------------------------
// Result generation
// ---------------------------------------------------------------------------

function generateResult(session: GameSession, definition: GameDefinition): GameResult {
  const roundResults = session.rounds
    .filter((r) => r.complete)
    .map((r) => {
      const p1 = r.answers.find((a) => a.player === 'player1');
      const p2 = r.answers.find((a) => a.player === 'player2');
      const score = scoreRound(r, definition);
      return {
        question: r.question.text,
        player1Answer: p1?.answer ?? '',
        player2Answer: p2?.answer ?? '',
        matched: score.bothMatched,
      };
    });

  const totalQuestions = definition.questionsPerRound;
  const matchCount = roundResults.filter((r) => r.matched).length;
  const overallMatch = matchCount > totalQuestions / 2;

  return {
    gameType: session.gameType,
    totalQuestions,
    player1Score: session.player1Score,
    player2Score: session.player2Score,
    rounds: roundResults,
    overallMatch,
    message: getResultMessage(session.gameType, matchCount, totalQuestions),
  };
}

function getResultMessage(gameType: GameType, matchCount: number, total: number): string {
  const ratio = total > 0 ? matchCount / total : 0;

  if (gameType === 'would-you-rather' || gameType === 'this-or-that') {
    if (ratio >= 0.8) return "You two think alike! 💕";
    if (ratio >= 0.5) return "A good mix of similarities and differences! 🌟";
    return "Opposites attract! 💫";
  }

  if (ratio >= 0.8) return "You know each other incredibly well! 💕";
  if (ratio >= 0.5) return "Pretty good understanding of each other! 🌟";
  if (ratio >= 0.3) return "There's always more to learn about each other! 💫";
  return "Time to ask each other more questions! 😄";
}
