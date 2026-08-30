/**
 * Game domain models (Phase 11).
 *
 * Shared types for the reusable couple mini-games engine.
 * All game logic lives in services; UI components consume view models.
 */

export type GameType =
  | 'who-knows-who-better'
  | 'guess-my-answer'
  | 'would-you-rather'
  | 'couple-trivia'
  | 'this-or-that'
  | 'finish-my-sentence'
  // Phase 12 casual games
  | 'memory-match'
  | 'word-scramble'
  | 'casual-trivia'
  | 'riddle-room';

export type Difficulty = 'easy' | 'medium' | 'hard';

export interface LevelConfig {
  /** 1-based level number (1–500). */
  level: number;
  /** Derived difficulty band from level. */
  difficulty: Difficulty;
  /** Number of questions/challenges for this level. */
  challengeCount: number;
  /** Scoring multiplier (1.0 = baseline). */
  scoreMultiplier: number;
  /** Game-specific config keyed by game type. */
  params: Record<string, unknown>;
}

export interface LevelProgress {
  /** Highest completed level per game. */
  highestCompleted: Record<string, number>;
  /** Current streak per game (consecutive levels completed). */
  streaks: Record<string, number>;
  /** Best streak per game. */
  bestStreaks: Record<string, number>;
}

/** Resolve a level number into a LevelConfig. Pure function — no side effects. */
export function resolveLevelConfig(level: number): LevelConfig {
  const clamped = Math.max(1, Math.min(500, Math.floor(level)));
  let difficulty: Difficulty;
  if (clamped <= 50) difficulty = 'easy';
  else if (clamped <= 200) difficulty = 'medium';
  else difficulty = 'hard';

  return {
    level: clamped,
    difficulty,
    challengeCount: resolveChallengeCount(clamped),
    scoreMultiplier: resolveScoreMultiplier(clamped),
    params: {},
  };
}

function resolveChallengeCount(level: number): number {
  // Base 5 at level 1, scaling up
  return Math.min(20, 5 + Math.floor(level / 25));
}

function resolveScoreMultiplier(level: number): number {
  // 1.0 at level 1, up to ~3.0 at level 500
  return Math.round((1 + (level - 1) * 0.004) * 100) / 100;
}

/** Category grouping for Games Hub display. */
export type GameCategory = 'couple' | 'casual';

export type PlayerRole = 'player1' | 'player2';

export interface GameQuestion {
  id: string;
  text: string;
  /** For choice-based games: the available options. */
  options?: string[];
  /** For who-knows/guess-my-answer: the "correct" answer (player1's answer). */
  correctAnswer?: string;
  /** Optional category/tag for filtering. */
  category?: string;
}

export interface GameDefinition {
  type: GameType;
  title: string;
  description: string;
  /** Number of questions per round. */
  questionsPerRound: number;
  /** Whether this game requires two players taking turns. */
  turnBased: boolean;
  /** Whether answers are compared for scoring. */
  scoringType: 'match' | 'choice' | 'none';
  /** The question bank for this game. */
  questions: GameQuestion[];
}

export interface PlayerAnswer {
  questionId: string;
  player: PlayerRole;
  answer: string;
  /** For choice games: the selected option index. */
  selectedOption?: number;
  /** Whether the answer matched (computed by engine). */
  matched?: boolean;
}

export interface GameRound {
  questionIndex: number;
  question: GameQuestion;
  answers: PlayerAnswer[];
  /** Whether both players have answered. */
  complete: boolean;
}

export interface GameSession {
  id: string;
  gameType: GameType;
  /** The current round index (0-based). */
  currentRound: number;
  /** All rounds played so far. */
  rounds: GameRound[];
  /** Player1's total score (match count). */
  player1Score: number;
  /** Player2's total score (match count). */
  player2Score: number;
  /** Whether the game is finished. */
  completed: boolean;
  /** ISO timestamp of session creation. */
  createdAt: string;
  /** ISO timestamp of last update. */
  updatedAt: string;
  /** Memory Match board state — only present for memory-match games. */
  board?: MemoryBoard;
  /** Word Scramble state — only present for word-scramble games. */
  scrambleState?: ScrambleState;
  /** Casual game score (single-player casual games). */
  casualScore?: number;
  /** Casual game total moves/attempts. */
  casualMoves?: number;
  /** Current level (1-based). */
  level?: number;
  /** Current difficulty. */
  difficulty?: Difficulty;
  /** Streak of consecutive level completions. */
  streak?: number;
}

export interface MemoryCard {
  id: string;
  pairId: string;
  symbol: string;
  revealed: boolean;
  matched: boolean;
}

export interface MemoryBoard {
  cards: MemoryCard[];
  /** Index of first flipped unmatched card (waiting for second flip). */
  firstFlippedIndex: number | null;
  /** Number of matched pairs found. */
  matchedPairs: number;
  /** Total pairs to find. */
  totalPairs: number;
  /** Number of moves (each pair of flips = 1 move). */
  moves: number;
}

export interface ScrambleState {
  /** The current word index in the round. */
  currentWordIndex: number;
  /** Total words in the round. */
  totalWords: number;
  /** Correct answers so far. */
  correct: number;
  /** Whether the current guess is correct (set after validation). */
  lastGuessCorrect: boolean | null;
}

export interface GameResult {
  gameType: GameType;
  totalQuestions: number;
  player1Score: number;
  player2Score: number;
  /** Per-round breakdown. */
  rounds: Array<{
    question: string;
    player1Answer: string;
    player2Answer: string;
    matched: boolean;
  }>;
  /** Whether the answers matched overall. */
  overallMatch: boolean;
  /** Fun result message based on score. */
  message: string;
  /** Casual game results (memory match, word scramble, etc.) */
  casualResult?: CasualGameResult;
}

export interface CasualGameResult {
  /** Score or pairs found. */
  score: number;
  /** Total moves/attempts. */
  moves: number;
  /** Time in seconds if tracked. */
  timeSeconds?: number;
  /** Accuracy percentage for trivia/riddles. */
  accuracy?: number;
  /** Result message. */
  message: string;
}

export interface GameState {
  session: GameSession;
  definition: GameDefinition;
  /** The currently displayed question index. */
  displayIndex: number;
  /** Current turn (for turn-based games). */
  currentTurn: PlayerRole;
  /** Whether we're in the results phase. */
  showingResults: boolean;
}

/** Content validation result. */
export interface ContentValidation {
  ok: boolean;
  errors: string[];
}

/** Game-specific level parameters for memory match. */
export interface MemoryMatchParams {
  pairs: number;
}

/** Game-specific level parameters for word scramble. */
export interface WordScrambleParams {
  wordCount: number;
  minWordLength: number;
}
