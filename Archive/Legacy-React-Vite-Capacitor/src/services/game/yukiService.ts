/**
 * Yuki Service (Stage 8).
 *
 * Manages Yuki's persistent state via localStorage.
 * Handles: save/load, time decay, action processing, leveling, streak tracking.
 * All operations are pure side-effects against localStorage — no DOM, no React.
 *
 * Architecture: UI → YukiService → localStorage.
 */

import type {
  YukiState,
  YukiAction,
  YukiMood,
  YukiActivity,
} from '../../data/game/yukiTypes.ts';
import {
  createDefaultYukiState,
  computeMoodScore,
  resolveMood,
  actionToActivity,
  applyTimeDecay,
  ACTION_XP,
  ACTION_NEED_CHANGES,
  xpForLevel,
} from '../../data/game/yukiTypes.ts';
import { nowIso } from '../../utils/time.ts';

// ---------------------------------------------------------------------------
// Storage
// ---------------------------------------------------------------------------

const STORAGE_KEY = 'twohearts_yuki';

// ---------------------------------------------------------------------------
// Load / Save
// ---------------------------------------------------------------------------

/** Load Yuki state from localStorage. Returns default state on failure. */
export function loadYukiState(): YukiState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return createDefaultYukiState();
    const parsed = JSON.parse(raw) as Partial<YukiState>;

    // Safe recovery: fill in any missing fields with defaults
    const defaults = createDefaultYukiState(parsed.name ?? 'Yuki');
    const state: YukiState = {
      ...defaults,
      ...parsed,
    };

    // Validate ranges
    state.hunger = clamp(state.hunger, 0, 100);
    state.energy = clamp(state.energy, 0, 100);
    state.happiness = clamp(state.happiness, 0, 100);
    state.cleanliness = clamp(state.cleanliness, 0, 100);
    state.level = Math.max(1, Math.floor(state.level));
    state.experience = Math.max(0, Math.floor(state.experience));
    state.xpToNextLevel = Math.max(1, Math.floor(state.xpToNextLevel));
    state.streak = Math.max(0, Math.floor(state.streak));
    state.totalInteractions = Math.max(0, Math.floor(state.totalInteractions));
    state.totalFeedings = Math.max(0, Math.floor(state.totalFeedings));
    state.totalPets = Math.max(0, Math.floor(state.totalPets));
    state.totalPlays = Math.max(0, Math.floor(state.totalPlays));

    // Recompute mood
    state.moodScore = computeMoodScore(
      state.hunger,
      state.energy,
      state.happiness,
      state.cleanliness,
    );

    return state;
  } catch {
    return createDefaultYukiState();
  }
}

/** Save Yuki state to localStorage. No-op on failure. */
export function saveYukiState(state: YukiState): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch {
    // localStorage may be full or unavailable — silently ignore
  }
}

// ---------------------------------------------------------------------------
// Time decay
// ---------------------------------------------------------------------------

/**
 * Apply time-based decay and return the updated state.
 * Call this when the user opens the Yuki screen.
 */
export function applyDecay(state: YukiState, now = new Date()): YukiState {
  const decayed = applyTimeDecay(state, now);
  const moodScore = computeMoodScore(
    decayed.hunger,
    decayed.energy,
    decayed.happiness,
    decayed.cleanliness,
  );

  // Reset activity to idle when applying decay (user is returning)
  const newState: YukiState = {
    ...state,
    ...decayed,
    moodScore,
    activity: 'idle',
    updatedAt: nowIso(() => now),
  };

  saveYukiState(newState);
  return newState;
}

// ---------------------------------------------------------------------------
// Action processing
// ---------------------------------------------------------------------------

export interface ActionResult {
  state: YukiState;
  xpGained: number;
  leveledUp: boolean;
  newMood: YukiMood;
  activity: YukiActivity;
  accessoryUnlocked: string | null;
}

/**
 * Process a Yuki action (feed, pet, play, clean, sleep).
 * Returns the updated state with all effects applied.
 */
export function processAction(
  state: YukiState,
  action: YukiAction,
  now = new Date(),
): ActionResult {
  const changes = ACTION_NEED_CHANGES[action];
  const xpGain = ACTION_XP[action];

  // Apply need changes
  let hunger = clamp(state.hunger + changes.hunger, 0, 100);
  let energy = clamp(state.energy + changes.energy, 0, 100);
  let happiness = clamp(state.happiness + changes.happiness, 0, 100);
  let cleanliness = clamp(state.cleanliness + changes.cleanliness, 0, 100);

  const moodScore = computeMoodScore(hunger, energy, happiness, cleanliness);
  const newMood = resolveMood(moodScore);

  // XP and leveling
  let experience = state.experience + xpGain;
  let level = state.level;
  let xpToNextLevel = state.xpToNextLevel;
  let leveledUp = false;

  while (experience >= xpToNextLevel) {
    experience -= xpToNextLevel;
    level++;
    xpToNextLevel = xpForLevel(level);
    leveledUp = true;
  }

  // Streak tracking
  const today = toDateKey(now);
  let streak = state.streak;
  let lastInteractionDate = state.lastInteractionDate;

  if (lastInteractionDate !== today) {
    // Check if yesterday was the last interaction (consecutive day)
    const yesterday = toDateKey(new Date(now.getTime() - 24 * 60 * 60 * 1000));
    if (lastInteractionDate === yesterday) {
      streak++;
    } else if (lastInteractionDate === null) {
      streak = 1;
    } else {
      // Streak broken
      streak = 1;
    }
    lastInteractionDate = today;
  }

  // Accessory unlocks
  let accessoryUnlocked: string | null = null;
  const ownedAccessories = [...state.ownedAccessories];

  if (leveledUp && level >= 3 && !ownedAccessories.includes('bandana')) {
    ownedAccessories.push('bandana');
    accessoryUnlocked = 'bandana';
  }
  if (leveledUp && level >= 5 && !ownedAccessories.includes('crown')) {
    ownedAccessories.push('crown');
    accessoryUnlocked = 'crown';
  }
  if (streak >= 3 && !ownedAccessories.includes('bow-tie')) {
    ownedAccessories.push('bow-tie');
    accessoryUnlocked = accessoryUnlocked ?? 'bow-tie';
  }
  if (streak >= 7 && !ownedAccessories.includes('star-badge')) {
    ownedAccessories.push('star-badge');
    accessoryUnlocked = accessoryUnlocked ?? 'star-badge';
  }
  if (state.totalInteractions + 1 >= 50 && !ownedAccessories.includes('heart-collar')) {
    ownedAccessories.push('heart-collar');
    accessoryUnlocked = accessoryUnlocked ?? 'heart-collar';
  }

  const activity = actionToActivity(action);

  const updated: YukiState = {
    ...state,
    hunger,
    energy,
    happiness,
    cleanliness,
    moodScore,
    activity,
    level,
    experience,
    xpToNextLevel,
    streak,
    lastInteractionDate,
    totalInteractions: state.totalInteractions + 1,
    totalFeedings: state.totalFeedings + (action === 'feed' ? 1 : 0),
    totalPets: state.totalPets + (action === 'pet' ? 1 : 0),
    totalPlays: state.totalPlays + (action === 'play' ? 1 : 0),
    ownedAccessories,
    updatedAt: nowIso(() => now),
  };

  saveYukiState(updated);

  return {
    state: updated,
    xpGained: xpGain,
    leveledUp,
    newMood,
    activity,
    accessoryUnlocked,
  };
}

// ---------------------------------------------------------------------------
// Accessory management
// ---------------------------------------------------------------------------

/** Equip an accessory. */
export function equipAccessory(state: YukiState, accessoryId: string): YukiState {
  const updated: YukiState = {
    ...state,
    accessory: accessoryId === 'none' ? null : accessoryId,
    updatedAt: nowIso(),
  };
  saveYukiState(updated);
  return updated;
}

// ---------------------------------------------------------------------------
// Activity timeout (reset to idle after animation duration)
// ---------------------------------------------------------------------------

/** Duration of each activity animation in ms. */
export const ACTIVITY_DURATIONS: Record<YukiActivity, number> = {
  idle: 0,
  eating: 2500,
  'being-petted': 2000,
  playing: 3000,
  sleeping: 0, // persistent until manually changed
  grooming: 2000,
  purring: 0,
};

// ---------------------------------------------------------------------------
// Reset
// ---------------------------------------------------------------------------

/** Reset Yuki to a brand new cat. */
export function resetYuki(name = 'Yuki'): YukiState {
  const state = createDefaultYukiState(name);
  saveYukiState(state);
  return state;
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function clamp(value: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, Math.round(value)));
}

function toDateKey(date: Date): string {
  const y = date.getUTCFullYear();
  const m = String(date.getUTCMonth() + 1).padStart(2, '0');
  const d = String(date.getUTCDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}
