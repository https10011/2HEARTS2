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
- Phase 23+: NOT STARTED

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
