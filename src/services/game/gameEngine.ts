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
  MemoryBoard,
  MemoryCard,
  PlayerAnswer,
  PlayerRole,
  ScrambleState,
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

// ===========================================================================
// Memory Match (Phase 12)
// ===========================================================================

/** Symbols available for Memory Match cards. */
const MEMORY_MATCH_SYMBOLS = [
  '🌸', '🌻', '🌊', '🌙', '⭐', '🦋', '🎵', '🌈',
  '🍓', '🦊', '🐻', '🌊', '🍑', '🍩', '🎄', '🔥',
  '💎', '🎁', '🍀', '🐱', '🐶', '🌺', '🎈', '🎀',
];

/** Creates a shuffled Memory Match board. */
export function createMemoryBoard(pairCount: number, clock: Clock = systemClock): MemoryBoard {
  const symbols = MEMORY_MATCH_SYMBOLS.slice(0, pairCount);
  const cards: MemoryCard[] = [];

  for (const symbol of symbols) {
    const pairId = newId();
    cards.push({ id: newId(), pairId, symbol, revealed: false, matched: false });
    cards.push({ id: newId(), pairId, symbol, revealed: false, matched: false });
  }

  // Fisher-Yates shuffle
  for (let i = cards.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [cards[i], cards[j]] = [cards[j], cards[i]];
  }

  void clock; // Used for timestamping; board itself is positional

  return {
    cards,
    firstFlippedIndex: null,
    matchedPairs: 0,
    totalPairs: pairCount,
    moves: 0,
  };
}

/** Creates a Memory Match session. */
export function createMemoryMatchSession(pairCount: number, clock: Clock = systemClock): GameSession {
  const now = nowIso(clock);
  return {
    id: newId(),
    gameType: 'memory-match' as GameType,
    currentRound: 0,
    rounds: [],
    player1Score: 0,
    player2Score: 0,
    completed: false,
    createdAt: now,
    updatedAt: now,
    board: createMemoryBoard(pairCount, clock),
    casualScore: 0,
    casualMoves: 0,
  };
}

export interface FlipResult {
  session: GameSession;
  card: MemoryCard;
  matched: boolean;
  gameOver: boolean;
}

/** Flips a card by index. Returns updated session and match status. */
export function flipCard(
  session: GameSession,
  cardIndex: number,
  clock: Clock = systemClock,
): FlipResult | null {
  const board = session.board;
  if (!board || session.completed) return null;

  const card = board.cards[cardIndex];
  if (!card || card.revealed || card.matched) return null;

  // Reveal the card
  const newCards = [...board.cards];
  newCards[cardIndex] = { ...card, revealed: true };

  const newBoard: MemoryBoard = { ...board, cards: newCards };
  let matched = false;

  if (newBoard.firstFlippedIndex === null) {
    // First card of the pair
    newBoard.firstFlippedIndex = cardIndex;
  } else {
    // Second card — check match
    const firstCard = newBoard.cards[newBoard.firstFlippedIndex];
    newBoard.moves++;

    if (firstCard.pairId === card.pairId) {
      // Match!
      matched = true;
      newBoard.matchedPairs++;
      newCards[newBoard.firstFlippedIndex] = { ...firstCard, matched: true };
      newCards[cardIndex] = { ...card, revealed: true, matched: true };
      newBoard.firstFlippedIndex = null;
    } else {
      // No match — flip both back after a delay (handled in UI)
      newBoard.firstFlippedIndex = null;
      // Cards stay revealed; UI will flip them back
    }
  }

  const gameOver = newBoard.matchedPairs >= newBoard.totalPairs;

  const updated: GameSession = {
    ...session,
    board: newBoard,
    casualScore: newBoard.matchedPairs,
    casualMoves: newBoard.moves,
    completed: gameOver,
    updatedAt: nowIso(clock),
  };

  return { session: updated, card: newCards[cardIndex], matched, gameOver };
}

/** Resets non-matching cards (called by UI after a delay). */
export function resetUnmatchedCards(
  session: GameSession,
  cardIndex1: number,
  cardIndex2: number,
  clock: Clock = systemClock,
): GameSession {
  const board = session.board;
  if (!board) return session;

  const newCards = [...board.cards];
  const c1 = newCards[cardIndex1];
  const c2 = newCards[cardIndex2];
  if (c1 && !c1.matched) newCards[cardIndex1] = { ...c1, revealed: false };
  if (c2 && !c2.matched) newCards[cardIndex2] = { ...c2, revealed: false };

  return {
    ...session,
    board: { ...board, cards: newCards },
    updatedAt: nowIso(clock),
  };
}

/** Generates Memory Match result. */
export function completeMemoryMatch(
  session: GameSession,
  clock: Clock = systemClock,
): { session: GameSession; result: GameResult } {
  const board = session.board;
  const moves = board?.moves ?? 0;
  const pairs = board?.totalPairs ?? 0;

  // Score: fewer moves = better
  const efficiency = pairs > 0 ? Math.round((pairs / Math.max(moves, pairs)) * 100) : 0;

  let message: string;
  if (efficiency >= 80) message = 'Incredible memory! 🧠✨';
  else if (efficiency >= 60) message = 'Great job remembering! 🌟';
  else if (efficiency >= 40) message = 'Not bad at all! 💪';
  else message = 'Practice makes perfect! 🔄';

  const completed: GameSession = {
    ...session,
    completed: true,
    updatedAt: nowIso(clock),
  };

  return {
    session: completed,
    result: {
      gameType: 'memory-match' as GameType,
      totalQuestions: pairs,
      player1Score: pairs,
      player2Score: 0,
      rounds: [],
      overallMatch: true,
      message,
      casualResult: {
        score: pairs,
        moves,
        accuracy: efficiency,
        message,
      },
    },
  };
}

// ===========================================================================
// Word Scramble (Phase 12)
// ===========================================================================

/** Creates a Word Scramble session. */
export function createWordScrambleSession(
  wordCount: number,
  clock: Clock = systemClock,
): GameSession {
  const now = nowIso(clock);
  return {
    id: newId(),
    gameType: 'word-scramble' as GameType,
    currentRound: 0,
    rounds: [],
    player1Score: 0,
    player2Score: 0,
    completed: false,
    createdAt: now,
    updatedAt: now,
    scrambleState: {
      currentWordIndex: 0,
      totalWords: wordCount,
      correct: 0,
      lastGuessCorrect: null,
    },
    casualScore: 0,
    casualMoves: 0,
  };
}

/** Scrambles a word using Fisher-Yates. */
export function scrambleWord(word: string): string {
  const chars = word.split('');
  for (let i = chars.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [chars[i], chars[j]] = [chars[j], chars[i]];
  }
  const result = chars.join('');
  // Ensure it's actually different
  return result === word ? scrambleWord(word) : result;
}

export interface ScrambleGuessResult {
  session: GameSession;
  correct: boolean;
  gameOver: boolean;
  answer: string;
}

/** Validates a word scramble guess. */
export function validateScrambleGuess(
  session: GameSession,
  guess: string,
  answer: string,
  clock: Clock = systemClock,
): ScrambleGuessResult | null {
  const state = session.scrambleState;
  if (!state || session.completed) return null;

  const correct = normalizeAnswer(guess) === normalizeAnswer(answer);
  const newCorrect = correct ? state.correct + 1 : state.correct;
  const nextIndex = state.currentWordIndex + 1;
  const gameOver = nextIndex >= state.totalWords;

  const newState: ScrambleState = {
    ...state,
    currentWordIndex: nextIndex,
    correct: newCorrect,
    lastGuessCorrect: correct,
  };

  return {
    session: {
      ...session,
      scrambleState: newState,
      casualScore: newCorrect,
      casualMoves: (session.casualMoves ?? 0) + 1,
      completed: gameOver,
      updatedAt: nowIso(clock),
    },
    correct,
    gameOver,
    answer,
  };
}

/** Generates Word Scramble result. */
export function completeWordScramble(
  session: GameSession,
  clock: Clock = systemClock,
): { session: GameSession; result: GameResult } {
  const state = session.scrambleState;
  const correct = state?.correct ?? 0;
  const total = state?.totalWords ?? 0;
  const accuracy = total > 0 ? Math.round((correct / total) * 100) : 0;

  let message: string;
  if (accuracy >= 90) message = 'Word wizard! Almost perfect! 📝✨';
  else if (accuracy >= 70) message = 'Great vocabulary skills! 🌟';
  else if (accuracy >= 50) message = 'Nice effort! Keep going! 💪';
  else message = 'Words are tricky! Try again! 🔄';

  return {
    session: {
      ...session,
      completed: true,
      updatedAt: nowIso(clock),
    },
    result: {
      gameType: 'word-scramble' as GameType,
      totalQuestions: total,
      player1Score: correct,
      player2Score: 0,
      rounds: [],
      overallMatch: accuracy >= 50,
      message,
      casualResult: {
        score: correct,
        moves: state?.totalWords ?? 0,
        accuracy,
        message,
      },
    },
  };
}

// ===========================================================================
// Casual trivia / riddle helpers (Phase 12)
// ===========================================================================

export interface CasualGuessResult {
  session: GameSession;
  correct: boolean;
  gameOver: boolean;
}

/** Records a single-player answer for casual games (trivia, riddle). */
export function recordCasualAnswer(
  session: GameSession,
  definition: GameDefinition,
  questionIndex: number,
  answer: string,
  selectedOption?: number,
  clock: Clock = systemClock,
): CasualGuessResult | null {
  if (session.completed) return null;

  const question = definition.questions[questionIndex];
  if (!question) return null;

  let correct = false;

  if (definition.scoringType === 'choice' && question.options && selectedOption !== undefined) {
    // For multiple choice: the correct answer is the first option
    correct = selectedOption === 0;
  } else if (definition.scoringType === 'match') {
    // For text input: compare against correctAnswer
    const correctAns = question.correctAnswer ?? '';
    correct = normalizeAnswer(answer) === normalizeAnswer(correctAns);
  }

  const newScore = (session.casualScore ?? 0) + (correct ? 1 : 0);
  const nextIndex = questionIndex + 1;
  const gameOver = nextIndex >= definition.questionsPerRound;

  // Create or update round
  const round: GameRound = {
    questionIndex,
    question,
    answers: [{
      questionId: question.id,
      player: 'player1',
      answer: answer.trim(),
      selectedOption,
      matched: correct,
    }],
    complete: true,
  };

  const newRounds = [...session.rounds.filter((r) => r.questionIndex !== questionIndex), round];

  return {
    session: {
      ...session,
      rounds: newRounds,
      casualScore: newScore,
      casualMoves: (session.casualMoves ?? 0) + 1,
      currentRound: nextIndex,
      completed: gameOver,
      updatedAt: nowIso(clock),
    },
    correct,
    gameOver,
  };
}

/** Completes a casual single-player game and generates result. */
export function completeCasualGame(
  session: GameSession,
  clock: Clock = systemClock,
): { session: GameSession; result: GameResult } {
  const score = session.casualScore ?? 0;
  const total = session.casualMoves ?? 0;
  const accuracy = total > 0 ? Math.round((score / total) * 100) : 0;

  const roundResults = session.rounds
    .filter((r) => r.complete)
    .map((r) => {
      const a = r.answers[0];
      return {
        question: r.question.text,
        player1Answer: a?.answer ?? '',
        player2Answer: r.question.correctAnswer ?? r.question.options?.[0] ?? '',
        matched: a?.matched ?? false,
      };
    });

  let message: string;
  if (accuracy >= 90) message = 'Outstanding! 🌟';
  else if (accuracy >= 70) message = 'Well done! 💕';
  else if (accuracy >= 50) message = 'Good effort! 💪';
  else message = 'Keep trying! 🔄';

  return {
    session: { ...session, completed: true, updatedAt: nowIso(clock) },
    result: {
      gameType: session.gameType,
      totalQuestions: total,
      player1Score: score,
      player2Score: 0,
      rounds: roundResults,
      overallMatch: accuracy >= 50,
      message,
      casualResult: { score, moves: total, accuracy, message },
    },
  };
}
