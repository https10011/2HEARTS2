/**
 * YukiScreen (Stage 8 — Complete Game System Replacement).
 *
 * The main Yuki companion experience. When users tap "Yuki" in Home,
 * they discover an orange cat living inside TwoHearts.
 *
 * "Wait... we have a cat?"
 *
 * This screen manages:
 * - Initial state loading with time decay
 * - Action processing with visual feedback
 * - Level-up celebrations
 * - Streak display
 * - Accessory management
 * - Speech bubbles for Yuki's reactions
 * - Activity animation lifecycle
 */

import { useState, useCallback, useEffect, useRef } from 'react';
import {
  loadYukiState,
  applyDecay,
  processAction,
  equipAccessory,
  ACTIVITY_DURATIONS,
} from '../../services/game/yukiService.ts';
import type { YukiState, YukiAction, YukiMood } from '../../data/game/yukiTypes.ts';
import { resolveMood, YUKI_ACCESSORIES } from '../../data/game/yukiTypes.ts';
import { YukiCharacter } from './YukiCharacter.tsx';
import { YukiActions } from './YukiActions.tsx';
import { RoseLilyDecoration, IconSparkle } from '../../components/index.ts';

/** Speech lines Yuki says per action. */
const ACTION_SPEECHES: Record<YukiAction, string[]> = {
  feed: ['Nom nom nom!', 'Purrr... delicious!', 'My favorite!', 'Yum!'],
  pet: ['*purrrr*', 'That feels nice!', 'More please!', 'Mrrrow~'],
  play: ['Zoomies!', 'I got it!', 'So fun!', '*pounces*'],
  clean: ['Squeaky clean!', '*shakes off*', 'All fresh!', 'Prrr~'],
  sleep: ['Zzz...', '*curls up*', 'So cozy...', '*yawn*'],
};

/** Speech lines Yuki says on mood. */
const MOOD_SPEECHES: Record<YukiMood, string[]> = {
  happy: ["I'm so happy!", 'Life is good!', 'I love you two!'],
  content: ['*content purr*', 'Everything is nice.', 'Home sweet home.'],
  neutral: ['*stretches*', 'Meow.', '*looks around*'],
  hungry: ['Feed me please!', 'My tummy rumbles...', 'Is it dinner time?'],
  sleepy: ['*yawn*...', 'So sleepy...', 'Nap time?'],
  playful: ['Let\'s play!', '*bounces*', 'I have so much energy!'],
  loved: ['*purrrr*', 'I feel so loved!', '*nuzzles*'],
  sad: ['Meow...', '*looks up sadly*', 'I miss you...'],
};

function pickRandom<T>(arr: T[]): T {
  return arr[Math.floor(Math.random() * arr.length)];
}

export function YukiScreen() {
  const [state, setState] = useState<YukiState | null>(null);
  const [speech, setSpeech] = useState<string | null>(null);
  const [activeAction, setActiveAction] = useState<YukiAction | null>(null);
  const [isAnimating, setIsAnimating] = useState(false);
  const [showLevelUp, setShowLevelUp] = useState(false);
  const [levelUpLevel, setLevelUpLevel] = useState(0);
  const [accessoryUnlocked, setAccessoryUnlocked] = useState<string | null>(null);
  const [initialized, setInitialized] = useState(false);
  const speechTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const animTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Initialize: load state, apply decay, show greeting
  useEffect(() => {
    const raw = loadYukiState();
    const decayed = applyDecay(raw);
    setState(decayed);
    setInitialized(true);

    // Show a greeting based on mood
    const mood = resolveMood(decayed.moodScore);
    const greetSpeeches = MOOD_SPEECHES[mood];
    setSpeech(pickRandom(greetSpeeches));

    const greetTimer = setTimeout(() => setSpeech(null), 3000);
    return () => {
      clearTimeout(greetTimer);
      if (speechTimerRef.current) clearTimeout(speechTimerRef.current);
      if (animTimerRef.current) clearTimeout(animTimerRef.current);
    };
  }, []);

  // Handle action
  const handleAction = useCallback((action: YukiAction) => {
    if (!state || isAnimating) return;

    const result = processAction(state, action);
    setState(result.state);

    // Set active action for visual feedback
    setActiveAction(action);
    setIsAnimating(true);

    // Show speech
    const speechText = pickRandom(ACTION_SPEECHES[action]);
    setSpeech(speechText);

    // Clear speech after delay
    if (speechTimerRef.current) clearTimeout(speechTimerRef.current);
    speechTimerRef.current = setTimeout(() => setSpeech(null), 2500);

    // Show level up if applicable
    if (result.leveledUp) {
      setLevelUpLevel(result.state.level);
      setShowLevelUp(true);
      setTimeout(() => setShowLevelUp(false), 2500);
    }

    // Show accessory unlock
    if (result.accessoryUnlocked) {
      setAccessoryUnlocked(result.accessoryUnlocked);
      setTimeout(() => setAccessoryUnlocked(null), 3000);
    }

    // Reset animation after activity duration
    const duration = ACTIVITY_DURATIONS[result.activity] || 2500;
    if (duration > 0) {
      if (animTimerRef.current) clearTimeout(animTimerRef.current);
      animTimerRef.current = setTimeout(() => {
        setActiveAction(null);
        setIsAnimating(false);
        // Reset to idle
        setState((prev) => {
          if (!prev) return prev;
          const updated = { ...prev, activity: 'idle' as const };
          return updated;
        });
      }, duration);
    } else {
      // Sleeping persists until another action
      setActiveAction(null);
      setIsAnimating(false);
    }
  }, [state, isAnimating]);

  // Handle accessory equip
  const handleAccessoryChange = useCallback((accessoryId: string) => {
    if (!state) return;
    const updated = equipAccessory(state, accessoryId);
    setState(updated);
  }, [state]);

  if (!state || !initialized) {
    return (
      <div className="yuki-screen th-screen-warm">
        <div className="yuki-environment">
          <div style={{ padding: 'var(--th-space-8)', textAlign: 'center', color: 'var(--th-color-text-secondary)' }}>
            Loading Yuki...
          </div>
        </div>
      </div>
    );
  }

  const mood = resolveMood(state.moodScore);

  return (
    <div className="yuki-screen th-screen-warm">
      {/* Subtle floral accent */}
      <RoseLilyDecoration variant={5} size={90} position="top-right" opacity={0.08} />

      {/* Accessory unlock notification */}
      {accessoryUnlocked && (
        <div
          className="yuki-speech"
          style={{ top: 'var(--th-space-2)', zIndex: 20 }}
          role="alert"
        >
          New accessory unlocked!
        </div>
      )}

      {/* Yuki's environment */}
      <div className="yuki-environment">
        <div className="yuki-floor" aria-hidden="true" />

        {/* Speech bubble */}
        {speech && (
          <div className="yuki-speech" role="status" aria-live="polite">
            {speech}
          </div>
        )}

        {/* Character */}
        <YukiCharacter
          activity={activeAction ? (
            activeAction === 'feed' ? 'eating' :
            activeAction === 'pet' ? 'being-petted' :
            activeAction === 'play' ? 'playing' :
            activeAction === 'clean' ? 'grooming' :
            'sleeping'
          ) : state.activity}
          mood={mood}
          accessory={state.accessory}
          showLevelUp={showLevelUp}
          levelUpLevel={levelUpLevel}
        />

        {/* Name and level */}
        <div className="yuki-name-row">
          <h1 className="yuki-name">{state.name}</h1>
          <span className="yuki-level-pill">
            <span className="yuki-level-pill__star" aria-hidden="true"><IconSparkle size={12} /></span>
            Level {state.level}
          </span>
        </div>

        {/* XP progress */}
        <div className="yuki-xp">
          <div className="yuki-xp__bar" role="progressbar" aria-valuenow={state.experience} aria-valuemin={0} aria-valuemax={state.xpToNextLevel}>
            <div
              className="yuki-xp__fill"
              style={{ width: `${(state.experience / state.xpToNextLevel) * 100}%` }}
            />
          </div>
          <span className="yuki-xp__label">
            {state.experience} / {state.xpToNextLevel} XP
          </span>
        </div>

        {/* Needs bars */}
        <div className="yuki-needs" aria-label="Yuki's needs">
          <YukiNeedBar label="Hunger" value={state.hunger} type="hunger" />
          <YukiNeedBar label="Energy" value={state.energy} type="energy" />
          <YukiNeedBar label="Happy" value={state.happiness} type="happiness" />
          <YukiNeedBar label="Clean" value={state.cleanliness} type="cleanliness" />
        </div>

        {/* Stats row */}
        <div className="yuki-stats-row">
          <div className="yuki-stat">
            <span className="yuki-stat__value">
              {state.streak > 0 && (
                <span className="yuki-streak-flame" aria-hidden="true">!</span>
              )}
              {state.streak}
            </span>
            <span className="yuki-stat__label">Streak</span>
          </div>
          <div className="yuki-stat">
            <span className="yuki-stat__value">{state.totalInteractions}</span>
            <span className="yuki-stat__label">Interactions</span>
          </div>
          <div className="yuki-stat">
            <span className="yuki-stat__value">{state.level}</span>
            <span className="yuki-stat__label">Level</span>
          </div>
        </div>

        {/* Action bar */}
        <YukiActions
          onAction={handleAction}
          disabled={isAnimating}
          activeAction={activeAction}
        />
      </div>

      {/* Accessories */}
      <div className="yuki-accessory-row" role="radiogroup" aria-label="Yuki accessories">
        {YUKI_ACCESSORIES.filter((a) => state.ownedAccessories.includes(a.id)).map((acc) => (
          <button
            key={acc.id}
            className={`yuki-accessory-chip ${
              (state.accessory ?? 'none') === acc.id ? 'yuki-accessory-chip--active' : ''
            }`}
            onClick={() => handleAccessoryChange(acc.id)}
            role="radio"
            aria-checked={(state.accessory ?? 'none') === acc.id}
            type="button"
          >
            {acc.visual && <span aria-hidden="true">{acc.visual}</span>}
            {acc.name}
          </button>
        ))}
      </div>
    </div>
  );
}

// ---------------------------------------------------------------------------
// Need bar sub-component
// ---------------------------------------------------------------------------

function YukiNeedBar({
  label,
  value,
  type,
}: {
  label: string;
  value: number;
  type: 'hunger' | 'energy' | 'happiness' | 'cleanliness';
}) {
  const isLow = value < 25;

  return (
    <div className="yuki-need">
      <span className="yuki-need__label" aria-hidden="true">{label}</span>
      <div
        className="yuki-need__bar"
        role="progressbar"
        aria-valuenow={value}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label={`${type}: ${value}%`}
      >
        <div
          className={`yuki-need__fill yuki-need__fill--${type} ${isLow ? 'yuki-need__fill--low' : ''}`}
          style={{ width: `${value}%` }}
        />
      </div>
      <span className="yuki-need__value">{value}</span>
    </div>
  );
}
