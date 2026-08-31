# Stage 11: Yuki Companion Migration

**Date:** August 31, 2026
**Status:** Complete
**Next Stage:** Stage 12 - Games System Migration (Optional)

---

## Overview

This stage migrates the virtual companion cat system (Yuki) from the legacy React/Vite/Capacitor implementation to the native Android app. Yuki is a virtual pet that lives inside TwoHearts, providing daily engagement through feeding, petting, playing, cleaning, and sleeping interactions.

## Scope

| Feature | Components | Files | Priority |
|---------|-----------|-------|----------|
| **Yuki State Model** | Types, enums, moods, actions, accessories, XP/leveling, decay | 1 | Core |
| **Yuki Service** | Persistence, time decay, action processing, leveling, streak tracking | 1 | Core |
| **Yuki ViewModel** | UI state management, action orchestration, animation timing | 1 | Core |
| **Yuki Character** | Cat rendering with activity animations, mood bubble, particles | 1 | Core |
| **Yuki Actions** | Action bar (Feed, Pet, Play, Clean) with visual feedback | 1 | Core |
| **Yuki Screen** | Main companion screen with environment, needs, stats, accessories | 1 | Core |
| **Router Integration** | Route wiring in AppRouter | 1 | Core |

**Total:** 7 files created/modified

---

## Feature Mapping: Legacy → Native

### Yuki Types (Data Model)

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `yukiTypes.ts` | `YukiTypes.kt` |

**Key Changes:**
- TypeScript types → Kotlin data classes and enums
- `localStorage` persistence → `SharedPreferences` persistence
- JSON serialization via `org.json.JSONObject` (Android built-in) instead of Gson
- All need decay rates, XP calculations, level thresholds, and accessory unlock conditions preserved exactly

### Yuki Service (Business Logic)

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `yukiService.ts` | `YukiService.kt` |

**Key Changes:**
- `localStorage` → `SharedPreferences` with manual JSON serialization
- No external JSON library needed (uses `org.json.JSONObject`)
- All action processing, decay, leveling, streak tracking preserved exactly

### Yuki ViewModel (State Management)

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `useState` hooks in `YukiScreen.tsx` | `YukiViewModel.kt` (AndroidViewModel) |

**Key Changes:**
- React `useState`/`useEffect` → ViewModel with `StateFlow`
- `useRef` timers → `android.os.Handler` for animation timing
- Lifecycle-aware via `AndroidViewModel` + `onCleared()`

### Yuki Character (Visual Rendering)

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `YukiCharacter.tsx` | `YukiCharacter.kt` |
| `yuki.css` animations | Compose animations |

**Key Changes:**
- CSS keyframe animations → Compose `InfiniteTransition` + `animateFloat`
- SVG cat image → Cat emoji placeholder (production would use `AsyncImage` or `painterResource`)
- CSS heart/ZZZ particles → Compose `graphicsLayer` animations
- Activity-based CSS classes → Compose animation state machine

### Yuki Actions (Interaction Bar)

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `YukiActions.tsx` | `YukiActions.kt` |

**Key Changes:**
- CSS button styling → Compose `Modifier.background`, `Modifier.border`
- HTML button elements → Compose `clickable` modifier
- Disabled state → `Modifier.alpha(0.4f)` + `clickable(enabled = false)`

### Yuki Screen (Main Experience)

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `YukiScreen.tsx` | `YukiScreen.kt` |

**Key Changes:**
- CSS gradient environment → Compose `Brush.verticalGradient`
- CSS need bars → Compose `Box` with dynamic width fill
- CSS accessory chips → Compose `clickable` chips with selection state
- `RoseLilyDecoration` → Omitted (decorative, can be added later)

### Router Integration

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| Route in `AppRouter.tsx` | Route in `AppRouter.kt` |

**Key Changes:**
- `composable(RoutePath.APP_YUKI) { Text("Yuki Screen") }` → Full `YukiScreen` composable
- ViewModel instantiated via `viewModel()` delegate
- `onBack` wired to `navController.popBackStack()`

---

## Files Created

### 1. `app/src/main/java/com/twohearts/app/data/game/YukiTypes.kt` (334 lines)

Complete Yuki data model:

- **`YukiMood`** enum: HAPPY, CONTENT, NEUTRAL, HUNGRY, SLEEPY, PLAYFUL, LOVED, SAD
  - Each mood has label, description, emoji
  - `resolve(moodScore)` maps score to mood
- **`YukiAction`** enum: FEED, PET, PLAY, CLEAN, SLEEP
- **`YukiActivity`** enum: IDLE, EATING, BEING_PETTED, PLAYING, SLEEPING, GROOMING, PURRING
  - `toActivity()` extension maps action to activity
- **`YukiAccessory`** data class + `AccessorySource` enum
  - 6 accessories: none, bow-tie, bandana, crown, heart-collar, star-badge
- **`YukiState`** data class: full persistent state model
  - 4 needs (hunger, energy, happiness, cleanliness)
  - Progression (level, experience, xpToNextLevel)
  - Stats (streak, totalInteractions, totalFeedings, totalPets, totalPlays)
  - Customization (accessory, ownedAccessories)
  - Timestamps (createdAt, updatedAt)
- **`computeMoodScore()`**: weighted average formula preserved exactly
- **`xpForLevel()`**: `(80 + level * 20)` formula preserved exactly
- **`ACTION_XP`**: XP rewards per action (feed: 15, pet: 10, play: 20, clean: 8, sleep: 5)
- **`ACTION_NEED_CHANGES`**: Need deltas per action, preserved exactly
- **`applyTimeDecay()`**: Time-based decay with 8-hour cycle, 15 base decay amount
- **`ACTIVITY_DURATIONS`**: Animation durations per activity
- **`ACTION_SPEECHES`**: Speech lines per action (4 lines each)
- **`MOOD_SPEECHES`**: Speech lines per mood (3 lines each)
- Helper functions: `nowIso()`, `toDateKey()`, `clamp()`

### 2. `app/src/main/java/com/twohearts/app/services/game/YukiService.kt` (315 lines)

Complete Yuki persistence and business logic:

- **Persistence**: SharedPreferences with manual JSON serialization via `org.json.JSONObject`
  - No external Gson dependency needed
  - Falls back to default state on corruption
- **`loadYukiState()`**: Load from SharedPreferences, validate ranges, recompute mood
- **`saveYukiState()`**: Save to SharedPreferences
- **`applyDecay()`**: Apply time decay, reset activity to idle
- **`processAction()`**: Full action processing:
  - Need changes (clamped 0–100)
  - Mood computation
  - XP and leveling with multi-level-up support
  - Streak tracking with consecutive-day logic
  - Accessory unlocks at correct thresholds
  - Returns `ActionResult` with all computed values
- **`equipAccessory()`**: Change equipped accessory
- **`resetYuki()`**: Reset to brand new cat
- **`ActionResult`** data class: state, xpGained, leveledUp, newMood, activity, accessoryUnlocked

### 3. `app/src/main/java/com/twohearts/app/ui/screens/yuki/YukiViewModel.kt` (164 lines)

ViewModel for Yuki UI state management:

- Extends `AndroidViewModel` for application context access
- Uses `StateFlow` for reactive UI updates (mirrors legacy React state)
- Manages 7 state flows: state, speech, activeAction, isAnimating, showLevelUp, levelUpLevel, accessoryUnlocked
- `initialize()`: Load state, apply decay, show mood-based greeting
- `processAction()`: Process action with animation timing
- Timer management via `android.os.Handler` (mirrors legacy `useRef` timers)
- `onCleared()` for cleanup

### 4. `app/src/main/java/com/twohearts/app/ui/screens/yuki/YukiCharacter.kt` (390 lines)

Character rendering composable:

- **Activity animations**: Compose `InfiniteTransition` + `animateFloatAsState`
  - Idle: gentle breathing (translateY oscillation)
  - Eating: slight scale pulse
  - Being petted: gentle sway rotation
  - Playing: bounce translateY
  - Sleeping: slow vertical drift + opacity fade
  - Grooming: tilt rotation
- **Mood bubble**: Positioned top-right with mood description
- **Heart particles**: 3 animated hearts with staggered delays (for petting)
- **ZZZ particles**: 3 animated Z characters with staggered delays (for sleeping)
- **Level-up overlay**: Scale animation with burgundy overlay
- **Accessory overlay**: Emoji display on character

### 5. `app/src/main/java/com/twohearts/app/ui/screens/yuki/YukiActions.kt` (100 lines)

Action bar composable:

- 4-column grid: Feed, Pet, Play, Clean
- Each button: icon + label, themed background/border
- Active state: burgundy background, white text
- Disabled state: 0.4 alpha, no click
- Uses Material Icons (Favorite, Star, ThumbUp, CleaningServices)

### 6. `app/src/main/java/com/twohearts/app/ui/screens/yuki/YukiScreen.kt` (414 lines)

Main companion screen:

- **Environment**: Gradient card with warm tones (matching legacy CSS gradient)
- **Speech bubble**: Positioned above character, auto-clears after 2.5s
- **Character**: 160dp cat with activity animations
- **Name + Level pill**: Orange gradient pill with star icon
- **XP progress bar**: 200dp max-width, orange gradient fill
- **Need bars**: 4 bars (hunger, energy, happiness, cleanliness) with type-colored fills
  - Low value (<25) turns bar red
- **Stats row**: Streak, Interactions, Level with flame icon for active streaks
- **Action bar**: 4-column action buttons
- **Accessory chips**: Horizontal row of owned accessories with selection state
- **Scrollable**: Full vertical scroll for all content

### 7. `app/src/main/java/com/twohearts/app/ui/navigation/AppRouter.kt` (Modified)

Route wiring:

- Added imports: `YukiScreen`, `YukiViewModel`, `viewModel()`
- Replaced `Text("Yuki Screen")` placeholder with full `YukiScreen` composable
- ViewModel instantiated via `viewModel()` delegate
- `onBack` wired to `navController.popBackStack()`

---

## Data Dependencies

| Feature | Entity | Service | Storage |
|---------|--------|---------|---------|
| Yuki State | `YukiState` | `YukiService` | SharedPreferences |
| Yuki UI | `YukiViewModel` | `YukiService` | — |
| Yuki Screen | `YukiScreen` | `YukiViewModel` | — |

**Note:** Yuki uses SharedPreferences (not Room/SQLite), matching the legacy implementation's localStorage approach. This is an intentional design choice — Yuki's state is a single JSON blob, making SharedPreferences a natural fit.

---

## Architectural Decisions

### 1. SharedPreferences + Manual JSON (Not Room)

**Decision:** Use SharedPreferences with manual JSON serialization via `org.json.JSONObject` instead of Room.

**Rationale:**
- Yuki state is a single JSON blob (not relational data)
- SharedPreferences is the direct equivalent of legacy `localStorage`
- `org.json.JSONObject` is Android built-in (no external dependency)
- Room would be over-engineering for a single-key value store
- Matches the legacy architecture's simplicity

### 2. AndroidViewModel (Not Regular ViewModel)

**Decision:** Use `AndroidViewModel` instead of regular `ViewModel`.

**Rationale:**
- `YukiService` needs `Context` for SharedPreferences access
- `AndroidViewModel` provides `Application` context without leaking activities
- Proper lifecycle management via `onCleared()`

### 3. Handler-Based Animation Timing (Not Coroutines)

**Decision:** Use `android.os.Handler` for animation timers instead of coroutines.

**Rationale:**
- Matches legacy `setTimeout` pattern exactly
- Simple, predictable timing for animation reset
- No need for structured concurrency for simple delays
- Cleanup via `removeCallbacks()` in `onCleared()`

### 4. Emoji Placeholder for Cat Character

**Decision:** Use cat emoji (`🐱`) as placeholder instead of SVG/image.

**Rationale:**
- Legacy SVG asset (`yuki-cat.svg`) is not available in Compose
- Cat emoji provides immediate visual feedback
- Can be replaced with `AsyncImage` or `painterResource` when asset pipeline is ready
- Does not block feature completion

### 5. Material Icons for Action Buttons

**Decision:** Use Material Design icons instead of custom SVG icons.

**Rationale:**
- Legacy used custom SVG icon components (`IconHeart`, `IconSparkle`, `IconSmile`, `IconLotus`)
- Material Icons provide equivalent visual weight
- Already available in the project's dependencies
- Consistent with other screens in the app

### 6. Gradient Environment Styling

**Decision:** Recreate the warm gradient environment using Compose `Brush.verticalGradient`.

**Rationale:**
- Matches legacy CSS gradient: warm orange → cream → surface
- Uses the same color values from the legacy `yuki.css`
- Provides the cozy, warm feeling that defines Yuki's space

---

## Verification

### Files Created/Modified
- [x] `YukiTypes.kt` — Complete data model with all types, formulas, constants
- [x] `YukiService.kt` — Full persistence, decay, action processing, leveling
- [x] `YukiViewModel.kt` — UI state management with animation timing
- [x] `YukiCharacter.kt` — Character rendering with activity animations
- [x] `YukiActions.kt` — Action bar with visual feedback
- [x] `YukiScreen.kt` — Main companion screen with all sections
- [x] `AppRouter.kt` — Route wiring with ViewModel

### Integration
- [x] Navigation integrated with Stage 6 (AppRouter)
- [x] Theme tokens used from Stage 1 (TwoHeartsTokens)
- [x] Components reused from Stage 2 (Header)
- [x] Data model consistent with legacy yukiTypes.ts
- [x] Service logic mirrors legacy yukiService.ts exactly
- [x] Screen layout mirrors legacy YukiScreen.tsx + yuki.css

### Security Properties
- [x] Yuki state stored in SharedPreferences (app-private)
- [x] No network calls or remote dependencies
- [x] No secrets or credentials introduced
- [x] Offline-first architecture preserved

---

## Known Limitations

### 1. Cat Character Rendering
- **Current:** Cat emoji placeholder (`🐱`)
- **Legacy:** SVG cat image (`yuki-cat.svg`)
- **Impact:** Visual fidelity reduced but functionality preserved
- **Mitigation:** Replace with `AsyncImage` or vector drawable when asset pipeline is ready
- **Priority:** Low (does not affect gameplay)

### 2. Rose Lily Decoration
- **Current:** Not included in Yuki screen
- **Legacy:** Subtle rose-lily floral accent in corner
- **Impact:** Minor visual difference
- **Mitigation:** Can be added in future polish pass
- **Priority:** Low (decorative only)

### 3. Animation Fidelity
- **Current:** Compose animations approximate CSS keyframes
- **Legacy:** CSS `@keyframes` with exact timing
- **Impact:** Slight visual differences in animation curves
- **Mitigation:** Animations are functionally equivalent and visually similar
- **Priority:** Low (animations work correctly)

### 4. Dark Theme Gradient
- **Current:** Single gradient for both themes
- **Legacy:** Separate dark theme gradient (`rgba(100, 60, 40, 0.25)`)
- **Impact:** Environment gradient may not adapt to dark mode
- **Mitigation:** Can be enhanced with `isSystemInDarkTheme()` check
- **Priority:** Medium (affects dark mode experience)

---

## Dependencies

### Upstream (Required)
- **Stage 1 (Design Tokens):** `TwoHeartsTokens` for all colors, spacing, typography
- **Stage 2 (Components):** `Header` component for screen header
- **Stage 6 (Navigation):** `RoutePath.APP_YUKI` route, `AppRouter` integration

### Downstream (Consumed By)
- **Stage 12 (Games - Optional):** Yuki replaced games as primary engagement feature
- **Stage 13 (Settings):** Potential Yuki reset in storage settings
- **Stage 14 (Integration):** Full app integration verification

### External
- None (Yuki is fully self-contained, offline-first)

---

## Future Considerations

1. **SVG Cat Asset:** Replace emoji with proper cat SVG/vector drawable
2. **Dark Theme Support:** Adapt gradient for dark mode
3. **Yuki in Search:** Intentionally excluded (matches legacy — Yuki content not in search)
4. **Yuki in Vault:** Not applicable (Yuki is not vault content)
5. **Yuki Reset:** Could be added to StorageSettingsScreen (data management)
6. **Sound Effects:** Could add subtle sound feedback for actions
7. **Haptic Feedback:** Could add vibration on action completion
8. **Widget:** Could provide home screen widget showing Yuki's mood

---

## Files Changed Summary

| Category | Files | Lines (est.) |
|----------|-------|--------------|
| Data Layer | 1 | ~335 |
| Service Layer | 1 | ~315 |
| ViewModel | 1 | ~165 |
| UI Components | 3 | ~915 |
| Router Modified | 1 | ~10 changed |
| Documentation | 1 | ~500 |
| **Total** | **8** | **~2,240** |

---

*Stage 11 Complete. Ready for Stage 12 (Games System Migration - Optional).*
