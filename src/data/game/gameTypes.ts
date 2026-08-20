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
