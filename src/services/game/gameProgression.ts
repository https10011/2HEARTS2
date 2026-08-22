/**
 * Game progression persistence (Phase 28).
 *
 * Stores per-game progression (highest completed level, best score, streak)
 * in localStorage — consistent with the settings persistence boundary.
 * All game progression is session-local per V1 scope; this module simply
 * preserves the user's earned progression across app restarts.
 */

import type { GameType, LevelProgress } from '../../data/game/gameTypes.ts';

// ---------------------------------------------------------------------------
// Storage key
// ---------------------------------------------------------------------------

const STORAGE_KEY = 'twohearts_game_progression';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface GameProgression {
  /** Highest completed level per game. */
  highestCompleted: Record<string, number>;
  /** Best score per game. */
  bestScores: Record<string, number>;
  /** Current streak per game (consecutive level completions). */
  streaks: Record<string, number>;
  /** Best streak per game. */
  bestStreaks: Record<string, number>;
  /** Total games played per game type. */
  totalGamesPlayed: Record<string, number>;
}

function emptyProgression(): GameProgression {
  return {
    highestCompleted: {},
    bestScores: {},
    streaks: {},
    bestStreaks: {},
    totalGamesPlayed: {},
  };
}

// ---------------------------------------------------------------------------
// Read / Write
// ---------------------------------------------------------------------------

/** Reads progression from localStorage. Returns empty progression on failure. */
export function loadProgression(): GameProgression {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return emptyProgression();
    const parsed = JSON.parse(raw) as Partial<GameProgression>;
    return {
      highestCompleted: parsed.highestCompleted ?? {},
      bestScores: parsed.bestScores ?? {},
      streaks: parsed.streaks ?? {},
      bestStreaks: parsed.bestStreaks ?? {},
      totalGamesPlayed: parsed.totalGamesPlayed ?? {},
    };
  } catch {
    return emptyProgression();
  }
}

/** Writes progression to localStorage. No-op on failure. */
export function saveProgression(progression: GameProgression): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(progression));
  } catch {
    // localStorage may be full or unavailable — silently ignore
  }
}

// ---------------------------------------------------------------------------
// Progression helpers
// ---------------------------------------------------------------------------

/** Gets the highest completed level for a game (1-based, 0 = no completion). */
export function getHighestCompletedLevel(gameType: GameType): number {
  const p = loadProgression();
  return p.highestCompleted[gameType] ?? 0;
}

/** Gets the next unlocked level for a game (1-based, always at least 1). */
export function getNextUnlockedLevel(gameType: GameType): number {
  const highest = getHighestCompletedLevel(gameType);
  return Math.min(highest + 1, 500);
}

/** Gets the current streak for a game. */
export function getStreak(gameType: GameType): number {
  const p = loadProgression();
  return p.streaks[gameType] ?? 0;
}

/** Gets the best streak for a game. */
export function getBestStreak(gameType: GameType): number {
  const p = loadProgression();
  return p.bestStreaks[gameType] ?? 0;
}

/** Gets the best score for a game. */
export function getBestScore(gameType: GameType): number {
  const p = loadProgression();
  return p.bestScores[gameType] ?? 0;
}

/** Gets total games played for a game type. */
export function getTotalGamesPlayed(gameType: GameType): number {
  const p = loadProgression();
  return p.totalGamesPlayed[gameType] ?? 0;
}

/**
 * Records a completed game level.
 * Updates highest completed, best score, streak, best streak, and total played.
 * Does NOT overwrite higher progress — only advances.
 */
export function recordLevelCompletion(
  gameType: GameType,
  level: number,
  score: number,
): GameProgression {
  const p = loadProgression();

  // Highest completed level — only advance
  const prevHighest = p.highestCompleted[gameType] ?? 0;
  if (level > prevHighest) {
    p.highestCompleted[gameType] = level;
  }

  // Best score — only improve
  const prevBest = p.bestScores[gameType] ?? 0;
  if (score > prevBest) {
    p.bestScores[gameType] = score;
  }

  // Streak — if this is the expected next level, continue streak; otherwise reset
  const prevCompleted = prevHighest;
  if (level === prevCompleted + 1 || prevCompleted === 0) {
    // Consecutive — increment streak
    p.streaks[gameType] = (p.streaks[gameType] ?? 0) + 1;
  } else {
    // Non-consecutive — reset streak to 1
    p.streaks[gameType] = 1;
  }

  // Best streak
  const currentStreak = p.streaks[gameType] ?? 1;
  const prevBestStreak = p.bestStreaks[gameType] ?? 0;
  if (currentStreak > prevBestStreak) {
    p.bestStreaks[gameType] = currentStreak;
  }

  // Total games played
  p.totalGamesPlayed[gameType] = (p.totalGamesPlayed[gameType] ?? 0) + 1;

  saveProgression(p);
  return p;
}

/**
 * Records a failed game attempt (level not completed).
 * Resets the streak for this game.
 */
export function recordLevelFailure(gameType: GameType): GameProgression {
  const p = loadProgression();
  p.streaks[gameType] = 0;
  p.totalGamesPlayed[gameType] = (p.totalGamesPlayed[gameType] ?? 0) + 1;
  saveProgression(p);
  return p;
}

/**
 * Gets the full progression summary for a game.
 */
export function getGameProgressSummary(gameType: GameType) {
  return {
    highestCompleted: getHighestCompletedLevel(gameType),
    nextLevel: getNextUnlockedLevel(gameType),
    streak: getStreak(gameType),
    bestStreak: getBestStreak(gameType),
    bestScore: getBestScore(gameType),
    totalGamesPlayed: getTotalGamesPlayed(gameType),
  };
}

/**
 * Converts game progression to LevelProgress for engine consumption.
 */
export function toLevelProgress(): LevelProgress {
  const p = loadProgression();
  return {
    highestCompleted: { ...p.highestCompleted },
    streaks: { ...p.streaks },
    bestStreaks: { ...p.bestStreaks },
  };
}

/**
 * Resets progression for a specific game.
 */
export function resetGameProgression(gameType: GameType): void {
  const p = loadProgression();
  delete p.highestCompleted[gameType];
  delete p.bestScores[gameType];
  delete p.streaks[gameType];
  delete p.bestStreaks[gameType];
  delete p.totalGamesPlayed[gameType];
  saveProgression(p);
}

/**
 * Resets all game progression.
 */
export function resetAllProgression(): void {
  saveProgression(emptyProgression());
}
