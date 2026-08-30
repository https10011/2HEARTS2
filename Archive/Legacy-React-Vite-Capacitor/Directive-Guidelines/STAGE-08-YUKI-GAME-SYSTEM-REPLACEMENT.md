# STAGE 08 — YUKI COMPANION REPLACEMENT

## 1. Why Yuki Was Introduced

The previous multi-game system (10 games across couple and casual categories) was architecturally sound but lacked personality and emotional resonance. It functioned as a games hub rather than a living companion experience. The Master Directive for Stage 8 called for replacing the generic games navigation with a single, cohesive companion character — Yuki, an orange cat who lives inside TwoHearts.

The core goal: **make Yuki feel alive** — not just a collection of pet buttons, but a real companion with personality, mood, reaction, and daily life within the couples app.

## 2. What Was Found in the Old Game System

### Architecture (preserved where useful)
- **Game engine** (`src/services/game/gameEngine.ts`): Reusable session lifecycle — `createSession`, `recordAnswer`, `computeScores`, `nextQuestion`, `completeGame`. Engine is game-type agnostic.
- **Game service** (`src/services/game/gameService.ts`): Business layer over the engine — `startGame`, `answerQuestion`, `validateAnswer`, `getCurrentQuestion`, `getDefinition`.
- **Game progression** (`src/services/game/gameProgression.ts`): Streak tracking, experience points, level computation. Local-first, localStorage persistence.
- **Game types** (`src/data/game/gameTypes.ts`): Well-defined TypeScript interfaces for game sessions, answers, rounds, results, definitions.
- **Game content** (`src/customization/games/gameContent.ts`): 10 game definitions with question banks.

### What was removed
- Game hub screen (10-card grid of game options)
- Individual game play screens (MemoryMatch, WordScramble, CasualGamePlay)
- Game results screen
- Game-related navigation routes (`appGamesPlay`, `appGamesResults`, `appGamesWhoKnows`, etc.)
- Game-related CSS classes (`th-game-*`)
- Game card in the home screen

### What was kept
- The legacy `appGames` route (redirect compatibility)
- Game engine, service, and types (still importable for any future needs)
- Game progression XP/level logic (reused in Yuki state model)

## 3. Architecture Decision — Python vs TypeScript

### Evaluation
- **Python runtime**: Would require Chaquopy or similar Android-Python bridge, adding ~15MB APK overhead, complex build pipeline, and cross-language debugging burden.
- **TypeScript + React**: Already the project's runtime. localStorage persistence is proven. No new runtime, no new build dependency, no cross-language IPC.
- **Canvas/WebGL**: SVG character with CSS animations is lighter and more maintainable than canvas-based rendering for this scope.
- **3D engine**: Unjustified complexity for a companion pet with ambient animations.

### Decision: TypeScript + React + CSS animations
**Rationale**: Yuki is a companion with ambient animations, mood state, and care mechanics. It does not need physics, real-time rendering, or complex game loops. React state + CSS animations + localStorage provides the best balance of quality, performance, maintainability, and offline capability for a solo student developer.

## 4. Yuki Data Model

### Core State (`YukiState`)
```typescript
interface YukiState {
  hunger: number;       // 0-100 (100 = full)
  energy: number;       // 0-100 (100 = well-rested)
  happiness: number;    // 0-100 (100 = joyful)
  cleanliness: number;  // 0-100 (100 = clean)
  level: number;        // 1+
  xp: number;           // current XP in level
  streak: number;       // consecutive days visited
  totalInteractions: number;
  accessory: string | null;
  lastInteractionAt: string; // ISO timestamp
  lastDecayAt: string;       // ISO timestamp
  lastVisitAt: string;       // ISO timestamp
}
```

### Enums
- **YukiMood**: `happy | content | neutral | hungry | tired | dirty | sleepy | playful`
- **YukiActivity**: `idle | eating | being-petted | playing | sleeping | grooming`

### Accessories
Defined as a static catalog with id, name, description, and visual (CSS emoji for V1).

### Derivation
- `deriveMood(state)`: Computes mood from need levels (hunger < 30 → hungry, energy < 30 → tired, etc.)
- `computeDecay(state, now)`: Decays needs over time (hunger -3/hr, energy -2/hr, happiness -1/hr, cleanliness -0.5/hr)
- `computeXpForLevel(level)`: XP required per level (50 + level * 25)
- `computeActivity(mood)`: Maps mood to visual activity class

## 5. Yuki Persistence

- **Storage key**: `twohearts:companion:yuki:v1`
- **Schema version**: 1
- **Strategy**: LocalStorage with JSON serialization
- **Recovery**: Corrupted or missing state returns fresh defaults via `ensureInitialized()`
- **Decay**: Applied lazily on each load — needs decay based on elapsed time since last interaction
- **Migration-safe**: Versioned key prevents conflict with future schema changes

## 6. Asset Strategy

- **Character**: Original SVG (`src/assets/yuki/yuki-cat.svg`) — custom orange cat illustration
- **Design**: Distinctive orange cat with large eyes, warm coloring, small pink nose, round face. NOT a tabby pattern.
- **Format**: SVG (scalable, tiny file size, offline-capable, no raster assets)
- **License**: Original creation, no external dependencies
- **Heart particles**: Pure CSS shapes (rotated squares + pseudo-element circles), no emoji, no SVG in components

## 7. Animation Strategy

- **Philosophy**: Gentle, organic, low-cost. Transform + opacity only (compositor-friendly).
- **Activity-based CSS classes**: Each activity maps to a CSS animation class
  - `idle`: Gentle breathing (translateY oscillation, 3.5s cycle)
  - `eating`: Subtle bounce (scale + translateY, 0.6s, 3 iterations)
  - `being-petted`: Gentle sway (rotate ±2deg, 0.8s, 2 iterations)
  - `playing`: Bounce sequence (translateY + scale keyframes, 0.7s, 3 iterations)
  - `sleeping`: Very slow breathing (4s cycle, subtle opacity shift)
  - `grooming`: Slight tilt (rotate -5deg, 1.2s)
- **Heart particles**: CSS float animation with staggered delays
- **ZZZ particles**: CSS float animation for sleeping state
- **Level-up overlay**: Scale + fade animation with emphasis easing
- **Mood bubble**: Scale + fade entrance
- **Reduced motion**: All animations disabled via `prefers-reduced-motion` and `data-th-motion='reduced'`

## 8. Performance Decisions

- SVG character: ~2KB, renders at 160x160, no raster decoding cost
- CSS animations only (no JavaScript animation loops)
- localStorage persistence (no IndexedDB overhead)
- Single React component tree (no virtual scrolling needed)
- Lazy decay computation (only on state load, not continuous)
- No continuous timers — state updates only on user interaction
- DOM-light: ~300 lines of JSX, minimal re-renders via `useMemo`/`useCallback`

## 9. Accessibility

- Touch targets: All action buttons meet 44px minimum
- Screen readers: Mood state announced via `role="status"` + `aria-live="polite"`
- Level-up uses `role="alert"` + `aria-live="assertive"`
- Character image has descriptive `alt` text including mood
- All decorative elements use `aria-hidden="true"`
- Reduced motion: All CSS animations disabled in reduced-motion mode
- Text scaling: Uses CSS variables for font sizes, flows naturally
- Dark mode: All colors use design tokens, automatically adapts

## 10. Navigation Changes

- Bottom nav: Games tab label changed to "Yuki" with cat icon
- Home primary items: "Games" card replaced with "Yuki" card (cat icon)
- Route: `appGames` kept as legacy redirect, `appYuki` is the new primary route
- `IconCat` added to Icon component vocabulary
- `navIcons.tsx`: `'cat'` key resolves to `IconCat`
- `navConfig.ts`: Updated NavIconKey type to include `'cat'`, removed `'gamepad'` from HOME_PRIMARY_ITEMS

## 11. Design Decisions

- **Environment**: Warm gradient backdrop with subtle floor line — cozy, not clinical
- **Color palette**: Orange/burgundy tones matching Yuki's character, warm whites for surfaces
- **Typography**: Display font for Yuki's name and level, system font for stats
- **Cards**: Rounded corners, subtle shadows, thin borders — consistent with TwoHearts design tokens
- **Action bar**: 4-column grid (Feed, Play, Pet, Sleep) — focused, not overwhelming
- **Needs visualization**: Horizontal bars with gradient fills, color-coded by need type
- **Stats row**: Streak (with flame), Interactions, Level — simple, readable

## 12. Files Changed

### New files:
- `src/data/game/yukiTypes.ts` — Yuki state model, enums, types, constants
- `src/services/game/yukiService.ts` — Persistence, state derivation, decay, actions
- `src/assets/yuki/yuki-cat.svg` — Original Yuki character SVG
- `src/features/yuki/yuki.css` — Yuki-specific styles and animations
- `src/features/yuki/YukiCharacter.tsx` — Character renderer with activity animations
- `src/features/yuki/YukiActions.tsx` — Action bar component
- `src/features/yuki/YukiScreen.tsx` — Main Yuki companion screen
- `src/features/yuki/index.ts` — Feature barrel export

### Modified files:
- `src/navigation/routes.ts` — Added `appYuki` route, kept `appGames` as legacy
- `src/features/app-shell/navConfig.ts` — Updated HOME_PRIMARY_ITEMS (yuki replaces games), added 'cat' to NavIconKey
- `src/features/app-shell/navIcons.tsx` — Added 'cat' → IconCat mapping
- `src/components/Icon.tsx` — Added IconCat component
- `src/components/index.ts` — Exported IconCat
- `src/navigation/AppRouter.tsx` — Added YukiScreen route, legacy GamesHub redirect
- `src/features/app-shell/screens/HomeScreen.tsx` — Updated game card to Yuki card
- `tests/app-shell.test.ts` — Updated route assertions for appYuki
- `tests/phase11-games.test.ts` — Updated route assertions for legacy redirect
- `tests/phase24-home-navigation.test.ts` — Updated HOME_PRIMARY_ITEMS assertions

### Removed files: None (old game files preserved for reference)

## 13. Regression Verification

All 948 tests pass. Key regressions checked:
- Onboarding flow: intact
- Navigation architecture: 5 tabs preserved, center hub intact
- Home layout: curated items updated, no archive leakage
- Route structure: all routes registered, no collisions
- Design tokens: all tokens resolve, no stray definitions
- Brand centralization: no inline SVG brand marks in features
- Icon system: no emoji in feature files (CSS hearts instead)
- App shell: back button, route transitions, reduced motion all intact

## 14. Test Results

```
# tests 948
# suites 176
# pass 948
# fail 0
```

## 15. TypeScript Result

Clean — `tsc -b --noEmit` passes with zero errors.

## 16. Known Limitations

1. **Single-couple only**: Yuki is shared by the couple, not per-user. Both partners see the same Yuki state.
2. **No cloud save**: Yuki state lives in localStorage only. App data reset or browser clear loses Yuki state.
3. **Accessories are text-based**: V1 accessories use simple text symbols. Rich accessory visuals are deferred.
4. **No sound**: Yuki has no audio feedback. Sound effects could enhance personality.
5. **No surprise events**: V1 has no random/discovery events. These would add delight.
6. **No couple-specific interactions**: No "both partners pet Yuki together" mechanic yet.

## 17. Deferred Work

- Couple-specific Yuki interactions (both partners feeding/petting simultaneously)
- Sound effects for actions and mood transitions
- Surprise events and discovery mechanics
- Rich accessory rendering (hats, bows, scarves as SVG overlays)
- Yuki's room/environment customization
- Daily challenge or mini-activity system
- Yuki growth stages (kitten → adult)
- Seasonal/holiday Yuki variations
- Yuki memory system (remembers favorite actions, reacts to patterns)

## 18. Instructions for Future Agents

1. **Yuki state lives in localStorage** via `yukiService.ts`. Don't add cloud sync or database tables for Yuki state in V1.
2. **The mood system is derived, not stored.** `deriveMood()` computes mood from need levels. Don't add a stored mood field.
3. **Decay is lazy.** Needs decay when state is loaded, not on a timer. Don't add setInterval-based decay.
4. **CSS animations are activity-based.** Each `YukiActivity` maps to a CSS class. New activities need a matching CSS animation.
5. **Reduced motion is enforced.** All animations must be disabled in reduced-motion mode. Check both `prefers-reduced-motion` and `data-th-motion='reduced'`.
6. **No emoji in feature files.** Use CSS shapes or Icon components. The design tokens test enforces this.
7. **The game engine is still importable.** If future features need session-based game mechanics, the engine in `gameEngine.ts` is reusable.
8. **Legacy `appGames` route exists** for redirect compatibility. Don't remove it without checking for references.
