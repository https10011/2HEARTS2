/**
 * Stage 13 — Games presentation helpers.
 *
 * Pure-function helpers for the Games visual productization:
 * game personality metadata, scoring messages, accuracy labels.
 * No DOM, no sql.js, no mocks.
 */

// ---------------------------------------------------------------------------
// Game personality metadata
// ---------------------------------------------------------------------------

export interface GamePersonality {
  /** Short accent label shown in the hub card. */
  accent: string;
  /** CSS class suffix for game-specific theming. */
  theme: string;
  /** Intro copy line — helps the user understand the vibe. */
  vibe: string;
  /** Couple or casual category. */
  category: 'couple' | 'casual';
}

const GAME_PERSONALITIES: Record<string, GamePersonality> = {
  'who-knows-who-better': {
    accent: 'How well do you know each other?',
    theme: 'know',
    vibe: 'Personal & playful',
    category: 'couple',
  },
  'guess-my-answer': {
    accent: 'Put yourself in their shoes',
    theme: 'guess',
    vibe: 'Empathy & insight',
    category: 'couple',
  },
  'would-you-rather': {
    accent: 'Two choices, one answer',
    theme: 'rather',
    vibe: 'Quick & fun',
    category: 'couple',
  },
  'couple-trivia': {
    accent: 'Test your couple knowledge',
    theme: 'trivia',
    vibe: 'Knowledge & memory',
    category: 'couple',
  },
  'this-or-that': {
    accent: 'Pick one and commit',
    theme: 'thisorthat',
    vibe: 'Fast preferences',
    category: 'couple',
  },
  'finish-my-sentence': {
    accent: 'Complete the thought together',
    theme: 'sentence',
    vibe: 'Creative & sweet',
    category: 'couple',
  },
  'memory-match': {
    accent: 'Find all the matching pairs',
    theme: 'memory',
    vibe: 'Focus & recall',
    category: 'casual',
  },
  'word-scramble': {
    accent: 'Unscramble love words',
    theme: 'scramble',
    vibe: 'Wordplay & quick thinking',
    category: 'casual',
  },
  'casual-trivia': {
    accent: 'Love & romance trivia',
    theme: 'lovtrivia',
    vibe: 'Knowledge & fun',
    category: 'casual',
  },
  'riddle-room': {
    accent: 'Solve clever riddles together',
    theme: 'riddle',
    vibe: 'Wit & curiosity',
    category: 'casual',
  },
};

export function getGamePersonality(type: string): GamePersonality {
  return (
    GAME_PERSONALITIES[type] ?? {
      accent: 'Play together',
      theme: 'default',
      vibe: 'Fun & connection',
      category: 'couple',
    }
  );
}

// ---------------------------------------------------------------------------
// Score formatting helpers
// ---------------------------------------------------------------------------

/** Returns an accuracy percentage from score/total. */
export function accuracyPercent(score: number, total: number): number {
  if (total <= 0) return 0;
  return Math.round((score / total) * 100);
}

/** Returns a human-readable accuracy label. */
export function accuracyLabel(accuracy: number): string {
  if (accuracy >= 90) return 'Outstanding!';
  if (accuracy >= 75) return 'Well done!';
  if (accuracy >= 60) return 'Good effort!';
  if (accuracy >= 40) return 'Not bad!';
  return 'Keep practicing!';
}

/** Returns an efficiency label for Memory Match. */
export function efficiencyLabel(pairs: number, moves: number): string {
  if (pairs <= 0) return 'Keep trying!';
  const efficiency = Math.round((pairs / Math.max(moves, pairs)) * 100);
  if (efficiency >= 80) return 'Incredible memory!';
  if (efficiency >= 60) return 'Great recall!';
  if (efficiency >= 40) return 'Nice work!';
  return 'Practice makes perfect!';
}

/** Formats a score display like "3 / 10". */
export function scoreDisplay(score: number, total: number): string {
  return `${score} / ${total}`;
}

/** Returns "N question(s)" or "N pair(s)". */
export function itemCountText(n: number, variant: 'questions' | 'pairs' | 'words' | 'riddles'): string {
  const label = n === 1 ? variant.replace(/s$/, '') : variant;
  return `${n} ${label}`;
}

/** Returns a progress percentage for round-based games. */
export function roundProgress(currentRound: number, totalRounds: number): number {
  if (totalRounds <= 0) return 0;
  return Math.min(100, Math.round((currentRound / totalRounds) * 100));
}

/** Returns the result message for a round match (couple games). */
export function roundResultMessage(matched: boolean): string {
  return matched ? 'You matched!' : 'No match';
}

/** Returns a streak display label. */
export function streakText(streak: number): string {
  if (streak <= 0) return '';
  return `${streak} streak`;
}

/** Returns a level-up congratulatory line based on accuracy. */
export function levelUpMessage(accuracy: number): string {
  if (accuracy >= 90) return 'Brilliant performance!';
  if (accuracy >= 70) return 'Well played!';
  if (accuracy >= 50) return 'Nice round!';
  return 'You completed the level!';
}

/** Returns a game-complete congratulatory line. */
export function gameCompleteMessage(): string {
  return 'Well done!';
}
