# Stage 13 — Games Visual Productization

## Stage Objective

Make the Games area feel like a polished, real TwoHearts experience.
Every game has its own personality while remaining consistent with the
TwoHearts design system. The hub, gameplay screens, results, and
progression all feel premium, warm, and connected to the couples app.

## Starting Commit

ae162b3 (Stage 11 baseline) — preceded by Stage 12 commit 72a30b7

## Ending Commit

72a30b7 → Stage 13 commit (pending)

## Branch

master

## Remote Verification

origin/master

## Working Tree

Clean (after commit)

## Mandatory Directive/Document Audit

- AGENTS.md — read, verified game architecture from Phase 28/29/25
- TWOHEARTS-VISUAL-PRODUCTIZATION-DIRECTIVE.txt — read, applied all requirements
- MasterPrompt.txt — read, preserved engine isolation, customization architecture
- TwoHeartsRDMap.txt — read, verified games feature scope
- TWOHEARTS_BUILD_PROGRESS.md — read, verified Phase 28/29 status

## Previous-Stage Audits

### Stage 12 (Vault)
- Vault CSS (th-vault-*) preserved at line 7532+ in primitives.css
- Stage 12 tests (stage12-vault.test.ts) still passing
- Vault security architecture unchanged
- No cloud storage introduced

### Stage 11 (Period Tracker)
- Period implementation intact
- Period navigation intact
- Period tests intact

### Stage 10 (Mood)
- Mood implementation intact
- Mood navigation intact
- Mood CSS intact
- Mood tests intact

### Stage 7-9 (Timeline, Reminders, Places)
- All features intact
- Shared components preserved
- DatePicker/TimePicker shared correctly

## Phase 28 Engine Audit

- Game engine (gameEngine.ts, gameService.ts) — UNCHANGED
- Game data models (gameTypes.ts) — UNCHANGED
- Game progression (gameProgression.ts) — UNCHANGED
- Game repositories — UNCHANGED
- Game state management — UNCHANGED
- Level system (resolveLevelConfig, selectQuestionsForLevel) — UNCHANGED
- Scoring algorithms — UNCHANGED
- Difficulty algorithms — UNCHANGED

## Phase 29 UX Audit

- Original game UX CSS classes (th-game-screen, th-game-header, etc.) — PRESERVED
- Phase 29 animation primitives (th-game-enter, th-game-correct, etc.) — PRESERVED
- Phase 28 animation primitives (th-card-flip, th-score-pulse, etc.) — PRESERVED
- Stage 13 CSS is additive, does not override Phase 29 classes

## Phase 25 Motion Audit

- All new animations use existing motion tokens (th-duration-normal, th-ease-standard)
- reduced-motion gated via @media (prefers-reduced-motion: reduce)
- No new animation framework introduced

## Files Changed

| File | Change Type | Description |
|------|-------------|-------------|
| src/components/primitives.css | Modified (+430 lines) | Stage 13 game CSS vocabulary |
| src/features/games/gamesPresentation.ts | New | Pure presentation helpers (personality, scoring, accuracy) |
| src/features/app-shell/screens/GamesHubScreen.tsx | Rewrite | Hero section, personality cards, section headers |
| src/features/games/GamePlayScreen.tsx | Rewrite | Enhanced intro, question cards, answer options, turn transitions |
| src/features/games/GameResultsScreen.tsx | Rewrite | Enhanced celebration, score card, round breakdown |
| src/features/games/CasualGamePlayScreen.tsx | Rewrite | Casual personality, enhanced intro/results/feedback |
| src/features/games/MemoryMatchScreen.tsx | Rewrite | Enhanced card grid, stats, intro/results |
| src/features/games/WordScrambleScreen.tsx | Rewrite | Enhanced scramble display, intro/results |
| src/features/games/index.ts | Minor update | Barrel exports |
| tests/stage13-games.test.ts | New | 30 tests for gamesPresentation.ts |
| docs/STAGE-13-VISUAL-PRODUCTIZATION.md | New | This report |

## Games Hub Changes

- New hero section with gradient background, "TwoHearts Games" label, title, description
- Game cards redesigned with per-game personality icons (emoji content)
- Each card shows game title + personality accent line
- Couple games have burgundy gradient icon backgrounds
- Casual games have rose/pink gradient icon backgrounds
- Section headers with icon badges (Heart for couple, Sparkle for casual)
- Staggered entrance animation for cards
- Warm background gradient

## Each Game Changed

### Who Knows Who Better
- Hub card: personal & playful vibe, accent "How well do you know each other?"
- Intro: personality vibe line, enhanced circle icon

### Guess My Answer
- Hub card: empathy & insight vibe
- Intro: personality vibe line

### Would You Rather
- Hub card: quick & fun vibe, accent "Two choices, one answer"
- Intro: personality vibe line

### Couple Trivia
- Hub card: knowledge & memory vibe
- Intro: personality vibe line

### This or That
- Hub card: fast preferences vibe, accent "Pick one and commit"
- Intro: personality vibe line

### Finish My Sentence
- Hub card: creative & sweet vibe
- Intro: personality vibe line

### Memory Match
- Hub card: focus & recall vibe
- Enhanced card grid: rounded corners, shadow states, burgundy gradient matched state
- New stats bar (matched/moves counters)
- Enhanced intro with personality vibe
- Enhanced results with efficiency label

### Word Scramble
- Hub card: wordplay & quick thinking vibe
- Enhanced scramble display: larger font, better spacing, shadow
- Enhanced results with accuracy label

### Love Trivia (Casual Trivia)
- Hub card: knowledge & fun vibe
- Enhanced intro/results with personality

### Riddle Room
- Hub card: wit & curiosity vibe
- Enhanced intro/results with personality

## Shared Visual System Changes

- New CSS vocabulary: 40+ new classes (th-games-hero, th-game-hub-card, th-game-option, etc.)
- New pure helpers: gamesPresentation.ts (12 exported functions)
- All CSS is token-driven, dark-mode safe, reduced-motion aware
- No new animation framework; uses Phase 25 motion tokens
- No new icons; uses existing Icon set (IconHeart, IconSparkle, IconSmile, etc.)

## Animation Changes

- `th-stagger-in` keyframe for hub card entrance
- All animations gated behind reduced-motion
- Uses existing th-game-enter, th-result-enter, th-badge-enter from Phase 29

## Accessibility

- All interactive controls maintain min 44px touch targets
- Semantic headings (h1, h2) for game titles and sections
- ARIA labels on memory cards preserved
- Focus states on buttons preserved
- Touch targets on answer options (th-game-option) meet 44px minimum
- Keyboard support preserved (Enter to submit on text inputs)

## Dark Mode

All new CSS includes `:root[data-th-theme='dark']` overrides:
- Hero gradient adjusts for dark backgrounds
- Game cards use elevated surface colors
- Memory cards use adjusted gradients
- Score cards use transparent dark gradients
- Turn badge uses adjusted gradient
- Intro circles use adjusted gradients

## Extra Large Text

All font sizes use CSS custom properties (--th-font-size-*) which scale
with --th-text-scale. No hardcoded font sizes in game components.

## 320px / Narrow Viewport

- Hub cards use flex layout with min-width 0, no fixed widths
- Question cards use padding only, no fixed widths
- Answer options use 100% width
- Memory grid uses responsive clamp() for card font sizes
- Progress bars use percentage widths

## Reduced Motion

All new animations respect reduced-motion:
- `@media (prefers-reduced-motion: reduce)` gates all keyframes
- th-stagger-in → instant opacity:1
- All transitions collapse to 1ms via root tokens

## Vite/Browser Verification

- Dev server serving at port 5173 ✓
- All new CSS classes present in built bundle (verified by grep)
- GamesHubScreen renders hero + personality cards
- Game screens render enhanced intros
- Memory grid renders with new stage13 classes
- Word scramble displays with enhanced styling

## Reference Comparison

No game-specific reference PNGs found in repository (reference directory
not present in current environment). Applied Strategy B: professional
creative judgment following established TwoHearts design language.

## Tests

840/840 passing (30 new Stage 13 tests + 810 existing)
- stage13-games.test.ts: 30 tests for gamesPresentation.ts
- All previous test suites still passing

## TypeScript

PASS — clean build, no errors

## Production Build

PASS — `npm run build` successful

## Capacitor Sync

CAPACitor sync attempted — successful

## APK Status

BLOCKED BY ENVIRONMENT (no JDK/Android SDK available in agent environment)

## Known Limitations

- No game-specific reference screens available in repository for direct comparison
- Browser/sql.js in-memory DB resets on reload (pre-existing; Android adapter persists)
- Game engine intentionally unchanged per Stage 13 directive

## Deferred Items

- APK-level visual QA (requires JDK/Android SDK environment)
- Additional game-specific decorative illustrations (could enhance per-game personality further)
- Sound/haptic feedback for game interactions (V1 limitation)

## Architecture Preservation

- Game engine (Phase 28): PRESERVED — zero behavior changes
- Game data models: PRESERVED
- Game repositories: PRESERVED
- Game services: PRESERVED
- Game persistence: PRESERVED
- Game state management: PRESERVED
- Game scoring: PRESERVED
- Game difficulty: PRESERVED
- Game progression: PRESERVED
- Phase 29 UX CSS: PRESERVED (additive only)
- Phase 25 motion: PRESERVED
- Schema: UNCHANGED
- Cloud storage: NOT INTRODUCED
- Remote game services: NOT INTRODUCED
- Remote notifications: NOT INTRODUCED

## Exact Next-Stage Starting Point

- Branch: master
- Commit: (Stage 13 commit SHA)
- Working tree: clean
- Tests: 840/840 passing
- TypeScript: PASS
- Build: PASS
- Previous stages intact: Stage 12, 11, 10, 7-9 all verified
- Do NOT begin Stage 14
