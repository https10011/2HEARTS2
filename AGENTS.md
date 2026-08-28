# TwoHearts — Agent Memory

## Project context
TwoHearts V1 is a private, offline-first, local-first couples app for Android,
built with React 18 + TypeScript + Vite + Capacitor 6. No backend, no cloud
database, no FCM, no remote auth — all V1 data stays on-device. V2 online
features must remain architecturally separable from V1.

## Authoritative specs (DO NOT MODIFY)
- `MasterPrompt.txt` — implementation/design rules (79 sections)
- `TwoHeartsRDMap.txt` — feature architecture & V1 scope
- `TwoHeart UI Reference Screens/` — 77 approved PNG references

## Stack decisions
- Package manager: **npm** (pnpm's corepack prompt is interactive; avoided).
- Bundler: **Vite** (static bundle bundled into APK for offline use; base `./`).
- State: **useSyncExternalStore** (no external state lib — avoids bloat).
- Persistence (Phase 2): **SQLite** — `@capacitor-community/sqlite` on Android,
  `sql.js` (WASM) in browser/dev/tests behind one `DatabaseAdapter`.
  Settings stay in `localStorage` behind `SettingsStorage` (not domain DB).
  Media bytes in private app files (`@capacitor/filesystem`, `media/` root),
  metadata + safe refs in `media_assets` table. See `docs/persistence.md`.
- Tests: `npm test` = Node ≥22.7 `--experimental-transform-types` node:test
  on real sql.js (no mocks).
- JDK 21 + Android platform 34 + AGP 8.2.1 + Gradle 8.2.1.

## Build commands
- Web: `npm run build` (tsc -b && vite build → dist/)
- Dev: `npm run dev` (Vite, port 5173)
- Android: `npm run build && npx cap sync android && cd android && ./gradlew assembleDebug`
  - APK → `android/app/build/outputs/apk/debug/app-debug.apk`
- Env: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ANDROID_HOME=/opt/android-sdk`

## Architecture (src/)
- `components/` — UI primitives (Button, Card, Screen, Header, IconButton,
  Input, Divider, EmptyState, LoadingState, Modal, Icon set). primitives.css.
- `core/` — AppRootProvider, ErrorBoundary, appSettings (persistent, schema
  v1, localStorage), uiState (ephemeral), useAppLifecycle (Capacitor App).
- `navigation/` — AppRouter (React Router 6) + routes.ts (RoutePath typed map).
- `theme/` — tokens.css (design tokens), tokens.ts (typed mirror + TEXT_SIZE_SCALE).
- `styles/` — global.css (mobile-first, safe-area, portrait).
- `customization/` — ONE owner-customization place (theme/, defaults/, games/).
- `features/` — `app-shell/` (AppShell, BottomNav, main screens);
  `memories/` (MemoriesHome, AddMemory, MemoryDetail, useMemoryService).
  `onboarding/` (SplashScreen, Welcome, ProfileSetup, RelationshipSetup,
  PersonalizationSetup, AppLockSetup, SetupComplete, OnboardingGate, HomeScreen).
- `data/` — persistence (Phase 2): `database/` (adapter + connection +
  migrations @ schema v4), `model/entity.ts` (UUID v4 ids, ISO UTC
  createdAt/updatedAt, optional deletedAt tombstone), `serialization/`
  (one EntitySerializer per entity), `media/` (MediaFileSystem adapters +
  MediaStorage service; safe refs; orphan sweep), `settings/settingsStorage.ts`
  (localStorage abstraction; also drives `core/appSettings.ts`).
  `memory/` (Memory, MemoryMedia domain models).
- `repositories/` — BaseRepository CRUD conventions + MediaAssetRepository
  + MemoryRepository.
- `services/backup/` — versioned export envelope + validation (no UI yet).
- `services/memory/` — MemoryService: create, update, delete, list, media
  coordination, validation, error normalization.
- `hooks/ utils/ config/` — shared hooks; `utils/{ids,time,base64}.ts`;
  `config/persistence.ts` (db name, schemaVersion=1, media root).
- `assets/{branding,icons,illustrations,images}/` — replaceable SVG assets.

## Path aliases (tsconfig + vite)
`@/* @components/* @theme/* @navigation/* @core/* @customization/*`

## Design tokens
Burgundy primary (#6A1B2B) + warm cream/blush/rose/beige/charcoal neutrals.
Text-size setting (Small/Default/Large/Extra Large) scales `--th-text-scale`.
Touch target min 44px. Safe areas via env() insets. 1080×2400 = visual ref only.

## V1 prohibited (V2 boundary)
Firebase, Supabase, cloud DB, remote auth, FCM/server push, cloud storage,
online sync/chat. `google-services` plugin is on AGP classpath but NOT applied.

## Phase status
- Phase 0: Reconnaissance — COMPLETE
- Phase 1: Engineering foundation — COMPLETE (e21e523)
- Phase 2: Local-first data & persistence foundation — COMPLETE (ab1ce1d;
  docs/persistence.md). Schema v3 after Phase 4 (adds relationship tables).
- Phase 3: Core services & device foundation — COMPLETE (src/services/**;
  docs/core-services.md)
- Phase 4: Application state & user/relationship foundation — COMPLETE
  (src/data/relationship/**, src/services/relationship/**,
  src/services/state/**; docs/relationship-state.md)
- Phase 5: Onboarding & app entry experience — COMPLETE
- Phase 6: Main app shell & navigation — COMPLETE
- Phase 7: Memories — COMPLETE (docs/memories.md; schema v4)
- Phase 8: Notes & Lists — COMPLETE
- Phase 9: Timeline — COMPLETE
- Phase 10: Games (Memory Match, Word Scramble) — COMPLETE
- Phase 11: Games polish — COMPLETE
- Phase 12: Casual Games — COMPLETE
- Phase 13: Reminders — COMPLETE (ReminderService; schedules via NotificationService)
- Phase 14: Places — COMPLETE
- Phase 15: Mood — COMPLETE
- Phase 16: Period Tracker — COMPLETE
- Phase 17: Vault — COMPLETE (VaultPinGate separate from app lock; master switch)
- Phase 18: Search & Notification Center — COMPLETE (dark tokens, anniversaries/
  dates worker via ReminderNotificationDriver)
- Phase 19: Settings & App Management — COMPLETE (docs/settings.md; settings
  schema v3; DataManagementService; AppLockGate)
- Phase 20: Visual fidelity & 77-screen implementation — COMPLETE (3014dbe)
- Phase 21: Integration, QA & hardening — COMPLETE
  (tests/phase21-integration.test.ts; fixed vault route service wiring —
  router captured coreServices.appLock at module scope before bootstrap and
  never injected VaultService — and OnboardingGate/catch-all sending
  completed users back into onboarding)
- Phase 22: Final build, documentation & release — COMPLETE
  (docs/phase-22-release.md, docs/screens.md 77-reference map; fixed
  reminders UI never wired to persistence (new useReminderService hook),
  places/mood/period screens built repositories with undefined adapters
  (now getDatabase()-backed), period calendar/settings were
  PlaceholderScreen stubs (now PeriodCalendarScreen/PeriodSettingsScreen;
  PlaceholderScreen deleted), note category meta centralized in
  features/notes/categoryMeta.ts; debug console.log calls removed)
- Phase 23: Design system & branding overhaul — COMPLETE. Brand/decorations
  centralized via `BrandLogo` + `decorations.tsx` (guarded by
  `tests/designTokens.test.ts`); official owner assets regenerated into
  `src/assets/{branding,decorations}`; feature-emoji icons eliminated
  (MasterPrompt §22) — icon set in `components/Icon.tsx` (+IconSmile,
  +IconCheck, +IconMapPin, +IconCamera, +IconVideo, +IconLock, +IconFile,
  +IconFileText exported from `components/index.ts`); documents:
  `docs/design-system.md`. Feature vocab emojis (mood picker, memory-match
  tiles) remain as content. Phase 24 (Home & Global Navigation Experience) —
  COMPLETE (docs/app-shell.md): five-position bottom nav
  (Home · Notifications · Us CENTER · Notes · More) from ONE nav vocabulary
  (`features/app-shell/navConfig.ts` + `navIcons.tsx` bridge to the Icon set);
  center = elevated burgundy brand button (BrandLogo `tone="light"` → cream
  recolor via `.th-brand-logo--light`; dark theme auto-recolors all brand art
  via one rule). HomeScreen = relationship header (owner/partner avatars,
  official brand lockup, RoseLilyDecoration corner) + 4 primary actions
  (Notes/Reminders/Us/Games) — no relationship-feature duplication; UsScreen
  = couple hub (Our Story / Our World groups); MoreScreen = utilities only
  (Settings/Search/About). AppShell: back = navigate(-1) deep, no-op at Home,
  Home-fallback without in-app history; `.th-route-transition` entrance
  (fade+rise, reduceMotion-aware). tests/phase24-home-navigation.test.ts.
- Phase 25 (system-wide motion & micro-interactions) — COMPLETE
  (docs/design-system.md "Motion & feedback"; tests/phase25-motion.test.ts):
  ONE interaction layer in primitives.css (`.th-pressable`, toast viewport +
  variants, `th-scale-in` empty entrance covering `__visual`+legacy `__icon`,
  `th-dialog-in` centered-modal entrance, reduced-motion spinner freeze);
  centralized `components/toast.tsx` (ToastProvider mounted ONCE in AppShell,
  `useToast()`, auto-dismiss 2.4s + tokenized exit; host survives navigation);
  save/delete/update toasts wired into notes/memories/reminders/places/mood +
  notification center; `LoadingState` uses the single `.th-spinner` (one
  `th-spin` keyframe app-wide) + visible caption; `--th-duration-spin` token;
  theme flips transition surface colors via ONE scoped rule in global.css;
  inline hardcoded transitions eliminated (MoodEntry, PeriodHome);
  `IconInfo` added to centralized Icon set. Phase 26 (Visual Experience Overhaul)
  — COMPLETE (docs/phase26-screen-audit.md): centralized CSS enhancement layer
  in primitives.css (+453 lines, token-driven: enhanced cards, empty states,
  relationship cards, profile cards, more items, note/memory/reminder cards,
  timeline events, FABs, settings rows, screen headers, stagger items, chips,
  warm dividers, content bands); 28 files polished across all screen groups;
  77-screen audit tracked in docs/phase26-screen-audit.md. Phase 27 (decorative &
  emotional design pass) — COMPLETE (1539781). Phase 28 (games engine & gameplay
  overhaul) — COMPLETE: LevelConfig/Difficulty types, resolveLevelConfig
  (1-500 levels, easy/medium/hard bands), selectQuestionsForLevel (seeded shuffle),
  GameService.startGameAtLevel, all games level progression, gameProgression.ts
  localStorage persistence, game animation CSS primitives, 650 total tests passing.
  Phase 29 (game visual & UX polish) — COMPLETE (b01ca3b): shared game UX
  CSS primitives (intro/header/question/feedback/result/level-up/progress-dots/
  badges/round-breakdown/scramble-display), polished all 5 game screens with
  shared classes, celebration level-up results, staggered entrance, emoji→icon
  fix, 650 total tests passing. Phase 30 (settings & real-time customization)
  — COMPLETE: settings architecture audit verified (useSyncExternalStore +
  AppRootProvider + CSS tokens), added game classes to theme-transition list
  for smooth light/dark flips during gameplay, 650 total tests passing.
  Phase 31 (full UX consistency pass) — COMPLETE: standardized empty states
  across all feature home screens (RemindersHome, PeriodHome → th-empty-emotional),
  replaced 16 hardcoded font weights with CSS tokens across 8 files, verified
  typography/spacing/buttons/cards/icons/motion/feedback/theme consistency,
  docs/phase31-ux-consistency-audit.md, 650 total tests passing.
  Phase 32 (performance & accessibility polish) — COMPLETE: removed 6 unused
  Rose/Lily SVG assets from build (~900KB saved), added CSS containment to
  decorative elements, verified all 20 keyframe animations are compositor-friendly,
  reduced-motion verified, touch targets verified, contrast audited, offline-first
  preserved, docs/phase32-performance-accessibility-audit.md, 650 tests passing.
  Phase 33 (final visual QA) — COMPLETE: 77 approved screen references
  audited (67 PASS, 2 MINOR, 0 MAJOR, 8 design-only), all Phase 23–32
  systems verified intact, dark mode/text scaling/reduced motion/branding/
  games/Rose-Lily/empty states/navigation all code-verified, no major
  defects found, docs/phase33-final-visual-qa.md, 650 tests passing.
  Phase 34 (final post-V1 release polish) — COMPLETE: full test suite
  (650/650 pass), TypeScript clean, production build successful, Capacitor sync
  successful, security audit clean (no secrets, no network calls), offline-first
  verified, storage audit verified, dependency audit clean, design system intact,
  branding centralized, game engine intact, GitHub Actions workflow verified,
  Android APK not buildable in this environment (no JDK/SDK), 77-screen status
  confirmed (67 PASS, 2 MINOR, 0 MAJOR, 8 design-only), all Post-V1 phases
  23–34 COMPLETE

## Phase 3 core services (src/services/)
- bootstrap/appBootstrap.ts — ordered startup stages; critical (persistence,
  schema-verify) abort to AppGate retry UI; non-critical log-and-continue.
  `coreServices` registry: device / notifications? / appLock? (optional when
  a non-critical stage degraded).
- errors/appError.ts — AppError{category,code,recoverable}; safeUserMessage
  never leaks internals; normalizeAppError translates PersistenceError.
- logging/logger.ts — leveled, scoped; redact() strips pin/password/secret/
  token/body/content/vault/media keys + Uint8Array payloads.
- validation/validators.ts — pure {ok,errors} validators; validate() composer.
- datetime/datetime.ts — local wall clock for user-facing dates; Feb 29→Feb 28
  leap rule; DST-safe local-day diffs; no date library.
- device/deviceCapabilities.ts — one capability matrix + has(); web fallbacks.
- permissions/permissionService.ts — granted|denied|prompt|unavailable; never
  throws; check() never prompts.
- lifecycle/appLifecycleService.ts — singleton bus owning appStateChange;
  simulate() for tests; core/useAppLifecycle delegates here.
- files/fileService.ts + fileAdapters.ts — generic non-media files under
  files/ root; MemoryFileAdapter for tests.
- media/mediaUtils.ts — size limits (photo 25MB, video 500MB), magic-byte
  sniffing; Phase 2 mediaTypes/MediaStorage still own storage.
- search/ — normalize.ts + searchEngine.ts; per-feature providers query
  repositories directly (no duplicate index); deterministic ranking.
- security/ — SecureStore boundary; capacitorSecureStore is the ONLY
  @aparajita/capacitor-secure-storage import site; pinHash = PBKDF2-HMAC-
  SHA-256 (120k iters, random salt, constant-time compare); appLockService =
  memory-only lock state, relock-on-foreground via lifecycle bus.

## Phase 19 settings & app management
- Settings schema v3: + notificationsEnabled / remindersEnabled / reduceMotion
  (defaults true/true/false); pure v2→v3 migration; setTextSize/setThemeMode
  reject invalid values at the store boundary.
- features/settings/ — 8 screens (hub, profile, relationship, appearance,
  notifications, security, storage, about) + settingsUi helpers + AppLockGate
  (full-app lock overlay in App.tsx; Vault keeps its own VaultPinGate).
- services/maintenance/dataManagementService.ts — storage report, orphan-media
  cache clear, destructive resetAllLocalData (one-tx domain wipe + media sweep
  + notification cancel + PIN removal + settings reset; schema_migrations and
  firstLaunchAt preserved). Bootstrap stage 'data-management' (non-critical).
- ReminderService.scheduleNotification gates on notificationsEnabled &&
  remindersEnabled — covers the Phase 18 anniversaries/dates worker too.
- config/appInfo.ts — single source for About screen (from capacitor.config).
- No fake settings: only implemented capabilities are exposed.

## Phase 4 relationship & state foundation
- data/relationship/relationshipTypes.ts — Profile (role owner|partner,
  one LIVE per role via partial unique index), CoupleRelationship (SQL
  singleton via singleton = 1 UNIQUE), ImportantDate (recurrence none|yearly
  NOW for future reminders; nullable profile_id = relationship-level).
- utils/time.ts adds local 'yyyy-mm-dd' calendar-date conventions
  (toLocalDateKeyTo/fromLocalDateKey/isValidDateKey/compareIso); birthdays/
  startDate/anniversaries are LOCAL keys, entity metadata stays UTC ISO.
- repositories/ — profileRepository, coupleRepository (singleton: get/save
  only, NOT BaseRepository), importantDateRepository (+ listForProfile,
  listRecurring).
- services/relationship/relationshipService.ts — feature boundary; profile
  write + couple link in ONE outer transaction; summary computes age days /
  next anniversary (Feb 29 → Feb 28 rule) via Phase 3 datetime.
- services/state/appStateService.ts — first-launch stamp (survives reset),
  onboarding stage derived from DOMAIN truth (never downgrade 'complete'),
  completeSetup() refuses while domain incomplete; bootstrap stage
  'application-state' wires it (+ coreServices.appState/relationship).
- core/appSettings.ts — settings schema v2 (+ firstLaunchAt,
  onboardingStage, themeMode light|dark|system); v1→v2 migration tested via
  isolated dynamic import; applyThemeMode sets data-th-theme on root.
- 114 tests: relationship.test, appStateAndPreferences.test,
  settingsMigration.test + updated migrations/bootstrap suites.

## Phase 3 rules
- React components import NO Capacitor plugins and NO storage APIs.
- Each plugin is imported in exactly ONE driver file.
- Node tests cannot resolve Vite aliases (@theme/* etc.) — modules reachable
  from tests must use relative imports with .ts extensions.
- Settings = non-sensitive config only; PIN material lives in SecureStore;
  lock state is memory-only (cold start = locked when enabled).
- AndroidManifest: POST_NOTIFICATIONS + SCHEDULE_EXACT_ALARM (local
  notifications only — no push in V1).

## Git
- user.name=openhands, user.email=openhands@all-hands.dev (local config).
- Shallow clone; unshallow if full history needed.
- `.gitignore` covers node_modules/, dist/, android/app/build/, .gradle/, local.properties.

## Stage notes (post-Phase 34)
- Stage 2 (onboarding): branded DatePicker centralized; docs/STAGE-2-…
- Stage 3 (Home + global shell): docs/STAGE-3-…, committed def00d8..8fd067f.
- Stage 5 (Memories Experience): docs/STAGE-5-VISUAL-PRODUCTIZATION.md,
  committed 365686a — photo-first Memories Home (hero + cards + year
  filter), Add/Edit memory, Memory Detail; memoryFilters.ts helpers;
  690 tests.
- Stage 6 (Notes + Private Thoughts): docs/STAGE-6-VISUAL-PRODUCTIZATION.md
  — branded Notes Home (paper cards + category icon badges +
  love-letter keepsake variant), NoteEditor (header Save + paper
  writing surface), NoteDetail (serif reading view + centralized
  Modal delete); noteTime.ts relative-time helpers; categoryIcons.tsx
  (Record<NoteCategory,…> enforced by TS; .tsx not loadable by Node
  tests — keep icon maps out of categoryMeta.ts); obsolete Phase 8
  notes CSS replaced by Stage 6 .th-notes-* vocabulary; fixed a
  pre-existing flaky Memory Match test (random shuffle could pair
  indices 0/1); 700 tests.
- Stage 7 (Timeline + Story): docs/STAGE-7-VISUAL-PRODUCTIZATION.md
  — TimelineHome rewritten as "Our story" narrative (branded header
  with back, blush story banner, ring-marker spine, year anchors only
  for multi-year stories, "Latest" emphasis card + pill, footer line);
  AddEvent composer (native <input type="date"> replaced by the
  centralized DatePicker; toasts); EventDetail story page (date chip,
  "The story" section, "Your story · Chapter N" band via pure
  chapterOf, centralized Modal delete); timelineStory.ts pure helpers
  (formatEventDate UTC-safe, buildStoryRows, chapterOf);
  Phase 9 timeline CSS replaced by Stage 7 .th-tl-* vocabulary
  (.th-timeline-editor-actions kept — shared with Stage 4
  ImportantDatesScreen); entity/service/repository untouched; 709 tests.
- Stage 4 (Us / Relationship Experience): docs/STAGE-4-…, committed
  f0d2a83 + 08b2b78 — couple-first Us hub, new /app/us/counter route,
  shared <CouplePair>, ImportantDates branded DatePicker + badges,
  Profile/Relationship Settings pickers, 674 tests.
  Note: SqlJsAdapter (browser dev/tests) is in-memory; rendered QA must
  avoid full reloads between seeding and inspection (onboarding → SPA
  navigation only). Android uses the persistent native adapter.
- Stage 8 (Reminders + Important Dates): docs/STAGE-8-…, committed
  b87084b — Reminders Home (filter chips, blush "next up" hero,
  today/upcoming/history .th-reminders-* vocabulary), branded composer
  (centralized DatePicker + NEW centralized components/TimePicker.tsx
  wheel replacing native time input, repeat bottom sheet, Notify-me
  Switch), productized ReminderDetail (moment card + sheet delete),
  pure reminderSchedule.ts helpers (buildReminderGroups; hero = first
  moment isAhead of now), IconBell/IconRepeat/IconChevronRight added to
  the Icon set; Important Dates (Stage 4) audited, not rebuilt; local
  notification architecture untouched; 727 tests.
- Stage 9 (Places + Shared Adventures): docs/STAGE-9-… — Places Home
  rewritten (.th-places-* vocabulary: hero band + featured hero card +
  "Our places" grid + category chips + in-memory search + branded empty
  state), Create/Edit composer (photo dropzone via the existing Phase 2
  media pipeline with data: preview), PlaceDetail (photo hero/fallback,
  "Saved location" + "Why this place is special" story cards, Modal
  bottom-sheet delete). PlaceService gained optional MediaStorage
  boundary for photoRef coordination (mirrors MemoryService); shared
  src/data/media/resolveMediaFileSystem.ts resolver replaces the
  duplicated per-feature resolution (memories refactored to it); pure
  placePresentation.ts helpers; tests/stage9-places.test.ts (24 tests,
  sql.js + in-memory FS, no mocks); 751 tests. No schema change
  (photo_ref column pre-existing); no map/location SDK (V1 prohibition
  honored — "Pinpoint it" keeps optional on-device coordinates).

- Stage 10 (Mood Experience): docs/STAGE-10-… — Mood Home rewritten
  (.th-mood-* vocabulary: branded header, "Today you're feeling" card
  with per-mood copy, icon-based one-tap quick selector replacing the
  emoji wall, recent check-ins, 2+-day streak line computed from real
  entries), Check in / Edit check-in composer (icon mood grid with
  aria-pressed, optional note kept, Modal bottom-sheet remove +
  "Mood saved/updated/removed" toasts), Mood History (This week/This
  month/All time chips, real distribution summary, month-grouped
  timeline rows, emotional empty state). Eight Mood icons added to the
  central Icon set (IconLotus/Sparkle/Sun/Meh/Moon/Frown/Swirl/Pulse);
  pure moodPresentation.ts helpers (localDateKey, formatMoodDay,
  buildMoodMonths, filterByRange, summarizeMoods, computeCheckInStreak);
  shared useMoodService.ts hook; tests/stage10-mood.test.ts (25 tests);
  776 tests. No schema change; MoodService/repository untouched; no
  mood strength or fabricated partner stats (not in the data model).

## Visual Productization Status (Stages 2–22)

**COMPLETE.** The TwoHearts V1 application has been fully visual-productized
through Stages 2–21, with Stage 22 serving as the final documentation checkpoint.

- Stages 2–14: Feature-by-feature visual productization
- Stage 15: Settings + App Customization
- Stage 16: Dialogs / Modals / System States (ConfirmDialog, StatusBanner, danger Button)
- Stage 17: Branding + Asset Completeness Audit
- Stage 18: 77-Screen Reference Reconciliation
- Stage 19: Full Rendered UX Walkthrough
- Stage 20: Final Visual Productization Pass
- Stage 21: Final Performance / Accessibility / Offline Regression
- Stage 22: Final 77-Screen Status + Documentation Checkpoint

**Authoritative final document:** `docs/FINAL-77-SCREEN-VISUAL-STATUS.md`

Historical stage reports remain in `docs/STAGE-*-*.md` as records.
Do NOT rewrite historical stage reports.

**77-reference status:** 60 VERIFIED, 7 COMPLETE, 10 DESIGN-ONLY / V1 EXCLUDED,
0 MINOR ISSUE, 0 MAJOR ISSUE.

**Tests:** 948/948 passing | **TypeScript:** clean | **Build:** successful
