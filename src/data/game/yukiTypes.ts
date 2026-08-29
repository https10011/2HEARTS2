/**
 * Yuki State Model (Stage 8).
 *
 * The companion cat that lives inside TwoHearts.
 * All state is local-first, localStorage-persisted, offline-compatible.
 *
 * Architecture: YukiState → needs decay → mood computation → visual expression.
 */

// ---------------------------------------------------------------------------
// Core need levels (0–100 scale)
// ---------------------------------------------------------------------------

/** How fed Yuki is. 100 = perfectly full, 0 = starving. */
export type NeedLevel = number;

// ---------------------------------------------------------------------------
// Mood system
// ---------------------------------------------------------------------------

export type YukiMood =
  | 'happy'
  | 'content'
  | 'neutral'
  | 'hungry'
  | 'sleepy'
  | 'playful'
  | 'loved'
  | 'sad';

/** Maps a numeric mood score (0-100) to a mood name. */
export function resolveMood(moodScore: number): YukiMood {
  if (moodScore >= 85) return 'happy';
  if (moodScore >= 70) return 'content';
  if (moodScore >= 55) return 'neutral';
  if (moodScore >= 40) return 'neutral';
  if (moodScore >= 25) return 'hungry';
  return 'sad';
}

// ---------------------------------------------------------------------------
// Activity / action types
// ---------------------------------------------------------------------------

export type YukiAction = 'feed' | 'pet' | 'play' | 'clean' | 'sleep';

/** What Yuki is currently doing (visual + behavioral state). */
export type YukiActivity =
  | 'idle'
  | 'eating'
  | 'being-petted'
  | 'playing'
  | 'sleeping'
  | 'grooming'
  | 'purring';

/** Maps action to the activity it triggers. */
export function actionToActivity(action: YukiAction): YukiActivity {
  switch (action) {
    case 'feed':
      return 'eating';
    case 'pet':
      return 'being-petted';
    case 'play':
      return 'playing';
    case 'clean':
      return 'grooming';
    case 'sleep':
      return 'sleeping';
  }
}

// ---------------------------------------------------------------------------
// Accessory system
// ---------------------------------------------------------------------------

export interface YukiAccessory {
  id: string;
  name: string;
  /** Visual hint for rendering (emoji or description). */
  visual: string;
  /** How it was unlocked. */
  source: 'default' | 'streak' | 'level' | 'interaction';
}

/** All available Yuki accessories. */
export const YUKI_ACCESSORIES: readonly YukiAccessory[] = [
  { id: 'none', name: 'Nothing', visual: '', source: 'default' },
  { id: 'bow-tie', name: 'Bow Tie', visual: '🎀', source: 'streak' },
  { id: 'bandana', name: 'Bandana', visual: '🧣', source: 'level' },
  { id: 'crown', name: 'Crown', visual: '👑', source: 'level' },
  { id: 'heart-collar', name: 'Heart Collar', visual: '💖', source: 'interaction' },
  { id: 'star-badge', name: 'Star Badge', visual: '⭐', source: 'streak' },
] as const;

export function getAccessoryById(id: string | null): YukiAccessory | undefined {
  if (!id) return undefined;
  return YUKI_ACCESSORIES.find((a) => a.id === id);
}

// ---------------------------------------------------------------------------
// Yuki state (the full persistent model)
// ---------------------------------------------------------------------------

export interface YukiState {
  /** Display name. */
  name: string;

  // Needs (0–100; higher = better)
  hunger: NeedLevel;
  energy: NeedLevel;
  happiness: NeedLevel;
  cleanliness: NeedLevel;

  /** Derived mood score (0–100). Computed from needs. */
  moodScore: number;

  /** Current visual/behavioral activity. */
  activity: YukiActivity;

  // Progression
  level: number;
  experience: number;
  /** XP needed for next level. */
  xpToNextLevel: number;

  // Streak / daily engagement
  streak: number;
  /** ISO yyyy-mm-dd of last interaction. */
  lastInteractionDate: string | null;

  // Stats
  totalInteractions: number;
  totalFeedings: number;
  totalPets: number;
  totalPlays: number;

  // Customization
  accessory: string | null;
  ownedAccessories: string[];

  // Timestamps
  createdAt: string;
  updatedAt: string;
}

// ---------------------------------------------------------------------------
// State creation / defaults
// ---------------------------------------------------------------------------

export function createDefaultYukiState(name = 'Yuki'): YukiState {
  const now = new Date().toISOString();
  return {
    name,
    hunger: 80,
    energy: 90,
    happiness: 85,
    cleanliness: 90,
    moodScore: 85,
    activity: 'idle',
    level: 1,
    experience: 0,
    xpToNextLevel: 100,
    streak: 0,
    lastInteractionDate: null,
    totalInteractions: 0,
    totalFeedings: 0,
    totalPets: 0,
    totalPlays: 0,
    accessory: null,
    ownedAccessories: ['none'],
    createdAt: now,
    updatedAt: now,
  };
}

// ---------------------------------------------------------------------------
// Computed mood from needs
// ---------------------------------------------------------------------------

/**
 * Compute Yuki's mood score from current needs.
 * Weighted average with happiness having slightly more influence.
 */
export function computeMoodScore(
  hunger: NeedLevel,
  energy: NeedLevel,
  happiness: NeedLevel,
  cleanliness: NeedLevel,
): number {
  const weighted =
    hunger * 0.25 +
    energy * 0.2 +
    happiness * 0.35 +
    cleanliness * 0.2;
  return Math.round(Math.max(0, Math.min(100, weighted)));
}

// ---------------------------------------------------------------------------
// XP / leveling
// ---------------------------------------------------------------------------

/** XP required for level N → N+1. */
export function xpForLevel(level: number): number {
  return Math.round(80 + level * 20);
}

/** Action XP rewards. */
export const ACTION_XP: Record<YukiAction, number> = {
  feed: 15,
  pet: 10,
  play: 20,
  clean: 8,
  sleep: 5,
};

/** Action need changes (positive = improvement). */
export const ACTION_NEED_CHANGES: Record<YukiAction, {
  hunger: number;
  energy: number;
  happiness: number;
  cleanliness: number;
}> = {
  feed: { hunger: 30, energy: 5, happiness: 10, cleanliness: 0 },
  pet: { hunger: 0, energy: 5, happiness: 25, cleanliness: 0 },
  play: { hunger: -10, energy: -20, happiness: 30, cleanliness: -5 },
  clean: { hunger: 0, energy: -5, happiness: 10, cleanliness: 35 },
  sleep: { hunger: -5, energy: 40, happiness: 5, cleanliness: 0 },
};

// ---------------------------------------------------------------------------
// Need decay (time-based)
// ---------------------------------------------------------------------------

/** How many hours before a full need cycle. */
const DECAY_CYCLE_HOURS = 8;

/** Per-cycle decay amount. */
const DECAY_AMOUNT = 15;

/**
 * Apply time-based decay to Yuki's needs based on hours since last update.
 * Returns the decayed needs (clamped 0–100).
 */
export function applyTimeDecay(
  state: YukiState,
  now: Date,
): Pick<YukiState, 'hunger' | 'energy' | 'happiness' | 'cleanliness'> {
  const lastUpdate = new Date(state.updatedAt);
  const hoursElapsed = Math.max(0, (now.getTime() - lastUpdate.getTime()) / (1000 * 60 * 60));
  const cycles = Math.floor(hoursElapsed / DECAY_CYCLE_HOURS);

  if (cycles <= 0) {
    return {
      hunger: state.hunger,
      energy: state.energy,
      happiness: state.happiness,
      cleanliness: state.cleanliness,
    };
  }

  // Hunger decays fastest, then energy, then cleanliness, happiness decays slowest
  const hungerDecay = Math.min(state.hunger, cycles * DECAY_AMOUNT);
  const energyDecay = Math.min(state.energy, cycles * (DECAY_AMOUNT * 0.8));
  const cleanlinessDecay = Math.min(state.cleanliness, cycles * (DECAY_AMOUNT * 0.6));
  const happinessDecay = Math.min(state.happiness, cycles * (DECAY_AMOUNT * 0.5));

  return {
    hunger: Math.max(0, Math.round(state.hunger - hungerDecay)),
    energy: Math.max(0, Math.round(state.energy - energyDecay)),
    happiness: Math.max(0, Math.round(state.happiness - happinessDecay)),
    cleanliness: Math.max(0, Math.round(state.cleanliness - cleanlinessDecay)),
  };
}

// ---------------------------------------------------------------------------
// Mood descriptions for UI
// ---------------------------------------------------------------------------

export const MOOD_DESCRIPTIONS: Record<YukiMood, string> = {
  happy: 'Yuki is purring with joy!',
  content: 'Yuki looks content and relaxed.',
  neutral: 'Yuki is quietly observing.',
  hungry: 'Yuki\'s tummy is rumbling...',
  sleepy: 'Yuki\'s eyelids are getting heavy.',
  playful: 'Yuki wants to play!',
  loved: 'Yuki is soaking up the affection!',
  sad: 'Yuki needs some attention...',
};

export const MOOD_EMOJI: Record<YukiMood, string> = {
  happy: '😊',
  content: '😌',
  neutral: '😐',
  hungry: '🍖',
  sleepy: '😴',
  playful: '🐱',
  loved: '🥰',
  sad: '😿',
};
