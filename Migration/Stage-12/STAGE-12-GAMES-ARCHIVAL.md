# Stage 12: Games System — Archived

**Date:** August 31, 2026
**Status:** Complete — Archived (not migrated)
**Next Stage:** Stage 13 — Settings & Utilities Migration

---

## Overview

This stage addresses the legacy Games System. Per the master roadmap, Stage 12
is **optional** and asks the migration agent to "evaluate whether games should
be migrated or archived."

**Decision: Archive the games.** The games system is superseded by Yuki
companion (Stage 11) and is low priority per the roadmap. All game routes
in the native Android app now redirect to the Yuki companion screen for
backward compatibility.

---

## Rationale for Archival

### 1. Games Are Legacy (Per Roadmap)

The roadmap explicitly states:
> "Games are legacy (Yuki replaced them as the primary engagement feature)"

Stage 8 of the original V1 development replaced the games system with Yuki
as the primary engagement feature. The `/app/games` route already redirected
to `/app/yuki` in the legacy implementation.

### 2. Yuki Is the Primary Engagement Feature

Yuki provides a richer, more engaging experience:
- Daily interaction through feeding, petting, playing, cleaning, sleeping
- Progression system with levels, XP, and accessories
- Time-based decay creating a reason to return
- Mood system with visual feedback
- Streak tracking for daily engagement

Games were per-session with no persistent progression (beyond localStorage
progression tracking), making them less compelling as a daily engagement hook.

### 3. Low Priority Per Roadmap

The roadmap states:
> "Low priority unless the migration agent decides to keep them"

Given the limited development time and the availability of more critical
features (Settings, Search, Notifications in Stage 13), archiving is the
pragmatic choice.

### 4. Legacy Code Is Fully Preserved

The complete legacy game implementation is preserved in the Archive:
- `Archive/Legacy-React-Vite-Capacitor/src/features/games/` — 7 screen files
- `Archive/Legacy-React-Vite-Capacitor/src/services/game/` — 3 service files
- `Archive/Legacy-React-Vite-Capacitor/src/data/game/gameTypes.ts` — Type definitions
- `Archive/Legacy-React-Vite-Capacitor/src/customization/games/gameContent.ts` — Question banks

If games are ever re-evaluated for migration in the future, all source
material is preserved and accessible.

---

## What Was Done

### Files Modified

| File | Change |
|------|--------|
| `AppRouter.kt` | Replaced 11 game route placeholders with Yuki redirects |
| `RoutePath.kt` | Added archival documentation comment block |

### Files NOT Modified

No new files were created. No files were deleted. The existing archive
structure was not altered.

---

## Architecture Decisions

### Redirect Strategy

All game routes use `LaunchedEffect(Unit)` to navigate to Yuki on
composition. This approach:

1. **Preserves route compatibility** — Any deep links, bookmarks, or
   references to `/app/games/*` still resolve without crashes
2. **Maintains back stack** — `popUpTo(APP_HOME) { saveState = true }`
   ensures clean navigation after redirect
3. **Single-top behavior** — `launchSingleTop = true` prevents duplicate
   Yuki screens if user taps rapidly
4. **User experience** — User sees a brief flash of the Yuki screen rather
   than a broken route

### Route Path Preservation

All game route constants remain in `RoutePath.kt` with their original values.
This ensures:
- No breaking changes for any code that references these paths
- Backward compatibility with any cached navigation state
- Clear documentation of what was archived via comments

### No Game Service Layer

The native Android app never had a game engine, game service, or game
content module. The legacy placeholder `Text("...")` composables in
AppRouter were the only game-related code in the native app. This
simplifies the archival — there's no service layer to remove or redirect.

---

## Legacy Game System Summary

For reference, here is what the archived game system contained:

### Game Types (10 total)

| Category | Games |
|----------|-------|
| **Couple** | Who Knows Who Better, Guess My Answer, Would You Rather, Couple Trivia, This or That, Finish My Sentence |
| **Casual** | Memory Match, Word Scramble, Love Trivia, Riddle Room |

### Game Engine (`gameEngine.ts`)

- Pure state logic (no React, no DB, no UI)
- Session management, answer recording, scoring
- Memory Match board creation and flip logic
- Word Scramble validation and scoring
- Casual trivia/riddle single-player logic
- Level-based progression with seeded shuffling

### Game Service (`gameService.ts`)

- Application-facing boundary over the engine
- Session lifecycle management
- Input validation, error normalization
- Level-based difficulty resolution

### Game Progression (`gameProgression.ts`)

- localStorage persistence (consistent with Yuki's approach)
- Per-game highest level, best score, streak tracking
- Total games played counter
- Streak reset on failure

### Game Content (`gameContent.ts`)

- 10 game definitions with question banks
- ~120 questions across all games
- Categories: favorites, personality, relationship, dreams, daily, etc.
- Mix of text input, multiple choice, and binary choice formats

### Game Types (`gameTypes.ts`)

- `GameType` union type (10 game types)
- `GameDefinition` with questions, scoring rules, turn-based flag
- `GameSession` with rounds, scores, board state, scramble state
- `LevelConfig` with difficulty, challenge count, score multiplier
- `MemoryBoard`, `MemoryCard`, `ScrambleState` for casual games

---

## What Was NOT Done (Intentionally)

### 1. No Game Engine Migration

The game engine (state machine, scoring, board logic) was not ported to
Kotlin. This is intentional — the engine is complex (~500 lines of pure
logic) and games are archived.

### 2. No Game Screens

No Jetpack Compose game screens were created. The placeholder `Text("...")`
composables were replaced with Yuki redirects instead.

### 3. No Game Content Migration

The question banks (~120 questions across 10 games) were not ported to
Kotlin. The legacy content remains in the archive for reference.

### 4. No Game Progression

No SharedPreferences-based game progression was created. Game progression
was localStorage in the legacy app and is not needed for archived games.

---

## Verification

### Route Compatibility

All 11 game routes remain defined in `RoutePath.kt`:
- `APP_GAMES` — `/app/games`
- `APP_GAMES_MEMORY_MATCH` — `/app/games/memory-match`
- `APP_GAMES_WORD_SCRAMBLE` — `/app/games/word-scramble`
- `APP_GAMES_COUPLE_TRIVIA` — `/app/games/couple-trivia`
- `APP_GAMES_WHO_KNOWS` — `/app/games/who-knows-who-better`
- `APP_GAMES_WOULD_YOU_RATHER` — `/app/games/would-you-rather`
- `APP_GAMES_THIS_OR_THAT` — `/app/games/this-or-that`
- `APP_GAMES_GUESS_MY_ANSWER` — `/app/games/guess-my-answer`
- `APP_GAMES_CASUAL_TRIVIA` — `/app/games/casual-trivia`
- `APP_GAMES_RIDDLE_ROOM` — `/app/games/riddle-room`
- `APP_GAMES_RESULTS` — `/app/games/results`

### Redirect Behavior

Each game route in `AppRouter.kt` now:
1. Composes a `LaunchedEffect(Unit)` that navigates to `APP_YUKI`
2. Uses `popUpTo(APP_HOME) { saveState = true }` for clean back stack
3. Uses `launchSingleTop = true` to prevent duplicate screens

### No Breaking Changes

- No files were deleted
- No public APIs were removed
- No route constants were renamed
- No import statements were changed (other than what was already present)

---

## Dependencies

### Upstream (Completed)

- Stage 6 (Navigation) — Route definitions and AppRouter
- Stage 11 (Yuki) — Redirect target for all game routes

### Downstream (Impact)

- Stage 13 (Settings) — No impact (games are not referenced in settings)
- Stage 14 (Integration Testing) — Game routes should redirect to Yuki
- Stage 15 (Build & Release) — No impact (no new dependencies)

---

## Future Considerations

### If Games Are Ever Re-evaluated

1. **Read the archive** — `Archive/Legacy-React-Vite-Capacitor/src/features/games/`
   and `Archive/Legacy-React-Vite-Capacitor/src/services/game/`
2. **Port the engine first** — `gameEngine.ts` is pure logic, easy to port
3. **Port game content** — `gameContent.ts` is just data, trivial to port
4. **Port screens last** — Compose UI is the most work
5. **Consider removing Yuki redirect** — Once native game screens exist

### Potential Enhancements (If Re-migrated)

- **Haptic feedback** on card flips (Memory Match)
- **Sound effects** for correct/incorrect answers
- **Difficulty scaling** based on player performance
- **Custom questions** — let couples add their own questions
- **Leaderboard** — persistent high scores across sessions
- **Multiplayer mode** — real-time two-device play (would require networking)

---

## Summary

| Metric | Value |
|--------|-------|
| Files modified | 2 |
| Files created | 0 |
| Files deleted | 0 |
| Lines added | ~60 |
| Lines removed | ~30 |
| New dependencies | 0 |
| Breaking changes | 0 |
| Decision | Archive (not migrate) |
| Reason | Games superseded by Yuki, low priority, legacy preserved in Archive |
