# TWOHEARTS — Master Audit, Architecture Documentation & Migration Roadmap

> **Document purpose:** Comprehensive audit of the current TwoHearts V1 application
> and complete roadmap for future migration work. Based on actual repository inspection.
>
> **Generated from:** Repository audit of all source files, configs, tests, documentation.
>
> **Current schema version:** 13 migrations (v13)
>
> **Test suite:** 55 test files, 948+ tests passing
>
> **Total source files:** ~265 TypeScript/TSX files, 25 SVG assets, 10,700+ lines CSS

---

# PART 1 — TWOHEARTS OVERVIEW

## What TwoHearts Is

TwoHearts is a **private, offline-first, local-first couples app for Android**. It provides a shared digital space for two people in a relationship to:

- Record and share **memories** (photos, videos, captions)
- Write **notes** (love letters, gratitude, ideas, private thoughts)
- Build a **timeline** of their story (milestones, events)
- Set **reminders** for each other (with local notifications)
- Track **important dates** (anniversaries, birthdays)
- Save meaningful **places** (with photos and descriptions)
- Log daily **mood** check-ins
- Track **menstrual cycles** (period tracker)
- Maintain a **private vault** (PIN-protected content)
- Interact with **Yuki**, a virtual companion cat
- Play **couples games** (word scramble, memory match, trivia, etc.)
- Use a full **settings system** (appearance, notifications, security, data management)

## Core Principles

1. **Offline-first**: Every V1 feature works without network connectivity
2. **Local-first**: All data stays on the device — no cloud, no sync, no remote servers
3. **Privacy by architecture**: No analytics, no telemetry, no remote calls
4. **Two-person model**: One device, two profiles (owner + partner), one couple relationship
5. **Real persistence**: SQLite database with 13 migration versions, not mocked or in-memory-only

## What the Finished Product Currently Provides

The app has **77 approved screen references**, of which:
- 67 are implemented as real screens with persistence
- 10 are covered by sections of other implemented screens
- 8 are design-only game references intentionally not built in V1

The complete user experience runs from first launch (onboarding with profile setup, relationship setup, personalization, app lock configuration) through the full application (home dashboard, relationship hub, all feature screens, settings, search, notifications center, vault, companion cat).

---

# PART 2 — COMPLETE CURRENT SYSTEM AUDIT

## 2.1 Technology Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| UI Framework | React | 18.3.x | Component rendering |
| Language | TypeScript | 5.6.x | Type safety, strict mode |
| Bundler | Vite | 5.4.x | Dev server + production build |
| Routing | React Router DOM | 6.26.x | Client-side routing |
| Mobile Shell | Capacitor | 6.2.x | Android WebView packaging |
| Database (Android) | @capacitor-community/sqlite | 6.0.x | Native SQLite |
| Database (Web/Test) | sql.js | 1.14.x | SQLite WASM for dev/tests |
| Secure Storage | @aparajita/capacitor-secure-storage | 6.0.x | Android Keystore-backed PIN storage |
| File System | @capacitor/filesystem | 6.0.x | Private app file storage |
| Notifications | @capacitor/local-notifications | 6.1.x | Local scheduled notifications |
| Device Info | @capacitor/device | 6.0.x | Device capabilities detection |
| App Lifecycle | @capacitor/app | 6.0.x | Foreground/background/back events |
| Status Bar | @capacitor/status-bar | 6.0.x | Android status bar styling |
| State Management | useSyncExternalStore | (React built-in) | External store subscriptions |
| CSS | Custom CSS (no Tailwind) | — | Design tokens + primitives |

**Notable absences (by design):**
- No Firebase, Supabase, or cloud database
- No FCM/remote push notifications
- No remote authentication
- No analytics or telemetry libraries
- No CSS framework (Tailwind, etc.)
- No state management library (Redux, Zustand, etc.)
- No ORM (raw SQL through adapter)
- No date library (native Date + custom helpers)

## 2.2 Build System

### Scripts (package.json)

| Command | What it does |
|---------|-------------|
| `npm run dev` | Vite dev server on port 5173 |
| `npm run build` | `tsc -b && vite build` → `dist/` |
| `npm run typecheck` | `tsc -b --noEmit` |
| `npm test` | Node 22.7+ `--experimental-transform-types` runs `tests/**/*.test.ts` |
| `npm run cap:sync:android` | `cap sync android` — copies web assets to Android |
| `npm run android:build` | Full Android APK build pipeline |

### Vite Configuration (`vite.config.ts`)

- Base path: `./` (relative, for APK bundling)
- Path aliases: `@/*`, `@components/*`, `@theme/*`, `@navigation/*`, `@core/*`, `@customization/*`
- Output: `dist/`, ES2020 target, no sourcemaps
- HMR disabled in production (Freebuff platform constraint)

### TypeScript Configuration

- Target: ES2022, JSX: react-jsx
- Strict mode with `noUnusedLocals`, `noUnusedParameters`, `noFallthroughCasesInSwitch`
- Project references: `tsconfig.app.json` (src/) + `tsconfig.node.json`
- Path aliases mirror Vite config

### Capacitor Configuration (`capacitor.config.ts`)

- App ID: `com.twohearts.app`
- Web directory: `dist/`
- Android background color: `#FDF6F0` (cream)
- StatusBar: DARK style, burgundy background

### GitHub Actions (`.github/workflows/build-android.yml`)

- Triggers: push to master/main, manual workflow_dispatch
- Steps: checkout → Node 22 → npm ci → npm test → npm run build → Java 21 → Android SDK 34 → Capacitor sync → Gradle assembleDebug → Upload APK artifact
- Artifact retention: 30 days

## 2.3 Complete Project Structure

```
twohearts/
├── .github/workflows/          # CI/CD
│   └── build-android.yml       # Android APK build pipeline
├── docs/                       # 39 documentation files
│   ├── design-system.md        # Design token documentation
│   ├── persistence.md          # Phase 2 architecture decisions
│   ├── core-services.md        # Phase 3 service architecture
│   ├── screens.md              # 77-screen reference mapping
│   ├── app-shell.md            # Phase 24 navigation architecture
│   ├── memories.md             # Phase 7 feature documentation
│   ├── onboarding.md           # Phase 5 onboarding documentation
│   ├── relationship-state.md   # Phase 4 relationship architecture
│   ├── settings.md             # Phase 19 settings documentation
│   └── STAGE-*-*.md            # 21 stage visual productization reports
├── src/
│   ├── main.tsx                # Entry point + AppGate bootstrap
│   ├── App.tsx                 # Root component (providers + router)
│   ├── vite-env.d.ts           # Vite type declarations
│   ├── assets/
│   │   ├── branding/           # 3 SVGs (logo, mark, app-icon)
│   │   ├── decorations/        # 20 rose-lily SVGs
│   │   ├── images/             # 1 onboarding welcome photo SVG
│   │   ├── yuki/               # 1 cat SVG
│   │   └── archive/            # Legacy asset archive
│   ├── components/             # 19 shared UI components + primitives.css
│   ├── config/                 # App info + persistence config
│   ├── core/                   # AppRootProvider, ErrorBoundary, settings, UI state
│   ├── customization/          # Owner defaults, theme, game content
│   ├── data/                   # Domain models, database, media, serialization
│   ├── features/               # Feature modules (app-shell, games, memories, etc.)
│   ├── navigation/             # AppRouter + routes
│   ├── repositories/           # 14 repository classes
│   ├── services/               # 25+ service modules
│   ├── styles/                 # global.css
│   ├── theme/                  # tokens.css, tokens.ts, components.ts
│   └── utils/                  # ids, time, base64 utilities
├── tests/                      # 55 test files
├── package.json
├── tsconfig.json               # Project references root
├── tsconfig.app.json           # Main TS config (strict)
├── tsconfig.node.json          # Node TS config for build tooling
├── vite.config.ts
├── capacitor.config.ts
└── index.html
```

## 2.4 Application Architecture — Complete Data Flow

### Entry Point Flow

```
index.html
  └── src/main.tsx
       ├── Imports global.css (design tokens + primitives)
       ├── Sets sqlWasmUrl on globalThis (for browser sql.js)
       ├── Renders <AppGate> inside React.StrictMode
       │    ├── Shows <SplashView> (BrandLogo) during bootstrap
       │    ├── Calls bootstrapApp() — ordered initialization pipeline
       │    │    ├── Stage 1: persistence (CRITICAL) — initializeDatabase()
       │    │    │    ├── Opens adapter (CapacitorSqliteAdapter or SqlJsAdapter)
       │    │    │    └── Runs 13 pending migrations
       │    │    ├── Stage 2: schema-verify (CRITICAL) — verifySchemaVersion()
       │    │    ├── Stage 3: device-capabilities — DeviceCapabilities.initialize()
       │    │    ├── Stage 4: lifecycle — appLifecycle.start()
       │    │    ├── Stage 5: notifications — NotificationService.initialize()
       │    │    ├── Stage 6: app-lock — AppLockService.initialize()
       │    │    └── Stage 7: application-state — AppStateService + RelationshipService + MediaStorage + DataManagementService
       │    ├── On success → renders <App />
       │    └── On failure → renders retry UI
       └── <App>
            ├── <ErrorBoundary>
            ├── <AppRootProvider> (text-size, theme, lifecycle)
            ├── <AppLockGate> (PIN overlay when locked)
            └── <AppRouter> (React Router 6)
```

### UI → Components → Services → Data → Storage (Complete Path)

```
┌─────────────────────────────────────────────────────────┐
│                     UI LAYER                             │
│  React components (*.tsx) in src/features/              │
│  Read-only hooks: useSyncExternalStore, useState, etc.   │
│  NO direct imports of: Capacitor plugins, DB, storage    │
└─────────────────┬───────────────────────────────────────┘
                  │ calls service methods
┌─────────────────▼───────────────────────────────────────┐
│                   SERVICE LAYER                          │
│  src/services/**/*.ts                                    │
│  - Input validation (src/services/validation/)           │
│  - Business logic + coordination                         │
│  - Error normalization (AppError with safe user msgs)    │
│  - Each Capacitor plugin imported in ONE driver file     │
└─────────────────┬───────────────────────────────────────┘
                  │ calls repository methods
┌─────────────────▼───────────────────────────────────────┐
│                 REPOSITORY LAYER                         │
│  src/repositories/*.ts (14 repositories)                │
│  - BaseRepository<T> provides CRUD + tombstones          │
│  - Domain objects only (never raw rows/SQL)              │
│  - Transactions via DatabaseAdapter.transaction()        │
└─────────────────┬───────────────────────────────────────┘
                  │ uses serializers + adapter
┌─────────────────▼───────────────────────────────────────┐
│                  DATA LAYER                              │
│  src/data/                                               │
│  ├── model/entity.ts (Entity, TombstonedEntity, NewEntity)│
│  ├── serialization/entitySerializer.ts (toParams/fromRow)│
│  ├── database/adapter.ts (DatabaseAdapter interface)     │
│  ├── database/connection.ts (initializeDatabase, getDatabase)│
│  ├── database/migrations/ (13 migrations, schema v13)   │
│  └── media/mediaStorage.ts (file lifecycle)              │
└─────────────────┬───────────────────────────────────────┘
                  │ uses adapter implementations
┌─────────────────▼───────────────────────────────────────┐
│                   STORAGE LAYER                           │
│  Android: @capacitor-community/sqlite (native SQLite)    │
│  Web/Test: sql.js (SQLite WASM) — same schema            │
│  Media: @capacitor/filesystem (private app directory)     │
│  Settings: localStorage (via SettingsStorage abstraction) │
│  Secrets: @aparajita/capacitor-secure-storage (Keystore)  │
└─────────────────────────────────────────────────────────┘
```

---

# PART 3 — COMPLETE FEATURE INVENTORY

## 3.1 Onboarding System

**Files:** `src/features/onboarding/` (10 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| SplashScreen | (pre-route) | Brand logo during bootstrap |
| WelcomeScreen | `/onboarding/welcome` | First-launch welcome |
| ProfileSetupScreen | `/onboarding/profile` | Owner name + birthday |
| RelationshipSetupScreen | `/onboarding/relationship` | Partner name + start date |
| PersonalizationSetupScreen | `/onboarding/personalization` | Theme + text size |
| AppLockSetupScreen | `/onboarding/app-lock` | Optional PIN setup |
| SetupCompleteScreen | `/onboarding/complete` | Celebration + entry to app |
| OnboardingGate | `/` | Evaluates state, redirects |
| useOnboarding | (hook) | Stage progression logic |
| OnboardingLayout | (layout) | Shared onboarding shell |

**Flow:** SplashScreen → AppGate → OnboardingGate evaluates `onboardingStage` → routes to correct step → SetupCompleteScreen → `/app/home`

**Persistence:** `appSettings.onboardingStage` (fresh → owner → relationship → personalization → complete) in localStorage; domain truth from database (profiles + couple relationship must exist).

## 3.2 Home Screen

**Files:** `src/features/app-shell/screens/HomeScreen.tsx`

**Route:** `/app/home`

**Content:**
- Couple header with owner/partner avatars (ProfileAvatar)
- TwoHearts brand logo (BrandLogo component)
- Personalized greeting
- "Our story together" counter (days since start date)
- 4 primary action cards: Notes, Reminders, Us, Yuki
- Rose-lily corner decoration

## 3.3 Us / Relationship Hub

**Files:** `src/features/app-shell/screens/UsScreen.tsx`

**Route:** `/app/us`

**Content:**
- Couple pair display (owner + partner avatars side by side)
- "Our Story" group: Memories, Timeline, Important Dates
- "Our World" group: Places, Mood, Period Tracker, Vault
- Navigation cards to each sub-feature

## 3.4 Bottom Navigation

**Files:** `src/features/app-shell/BottomNav.tsx`, `navConfig.ts`, `navIcons.tsx`

**5 positions:** Home · Notifications · **TWOHEARTS (center, elevated)** · Notes · More

**Design:** Pill-shaped floating bar with backdrop blur, elevated center brand button (BrandLogo mark), active state indicators.

## 3.5 Memories

**Files:** `src/features/memories/` (6 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| MemoriesHome | `/app/memories` | Grid gallery of memory cards |
| AddMemory | `/app/memories/add`, `/:memoryId/edit` | Create/edit with photo picker |
| MemoryDetail | `/app/memories/:memoryId` | Full view with photo gallery |

**Data model:** `Memory` (title, caption, memoryDate, sortOrder) + `MemoryMedia` join table + `MediaAsset` (photo/video metadata).

**Service:** `MemoryService` — validation, media coordination, orphan cleanup.

**Repository:** `MemoryRepository` extends `BaseRepository<Memory>`.

**Database:** `memories` + `memory_media` tables (migration 004).

## 3.6 Notes

**Files:** `src/features/notes/` (7 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| NotesHome | `/app/notes` | Paper-styled note cards with category icons |
| NoteEditor | `/app/notes/add`, `/:noteId/edit` | Writing surface with paper feel |
| NoteDetail | `/app/notes/:noteId` | Serif reading view |

**Categories:** general, shared, private, love-letter, gratitude, idea, reminder

**Data model:** `Note` (title, content, category, timestamps, tombstone).

**Database:** `notes` table (migration 005).

## 3.7 Timeline

**Files:** `src/features/timeline/` (5 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| TimelineHome | `/app/timeline` | "Our story" narrative view with spine |
| AddEvent | `/app/timeline/add`, `/:eventId/edit` | Event composer with date picker |
| EventDetail | `/app/timeline/:eventId` | Story page with chapter system |

**Data model:** `TimelineEvent` (title, eventDate, description).

**Database:** `timeline_events` table (migration 006).

## 3.8 Reminders

**Files:** `src/features/reminders/` (5 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| RemindersHome | `/app/reminders` | Filtered list with "next up" hero |
| CreateReminder | `/app/reminders/add`, `/:reminderId/edit` | Composer with TimePicker + recurrence |
| ReminderDetail | `/app/reminders/:reminderId` | Moment card with delete |

**Data model:** `Reminder` (title, description, scheduledDate, scheduledTime, recurrence, status, notificationEnabled).

**Recurrence:** none, daily, weekly, monthly, yearly.

**Database:** `reminders` table (migration 007).

**Integration:** ReminderService coordinates with NotificationService for local notification scheduling.

## 3.9 Places

**Files:** `src/features/places/` (5 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| PlacesHome | `/app/places` | Hero band + grid + category chips + search |
| CreatePlace | `/app/places/add`, `/:placeId/edit` | Composer with photo dropzone |
| PlaceDetail | `/app/places/:placeId` | Photo hero + story cards |

**Data model:** `Place` (name, address, city, state, country, lat/lng, notes, category, photoRef, memoryId).

**Database:** `places` table (migration 008).

## 3.10 Mood

**Files:** `src/features/mood/` (6 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| MoodHome | `/app/mood` | Today's feeling card + quick selector + streak |
| MoodEntryScreen | `/app/mood/add`, `/:entryId/edit` | Icon mood grid with optional note |
| MoodHistory | `/app/mood/history` | Week/month/all-time distribution |

**Moods:** happy, love, excited, calm, grateful, neutral, tired, sad, anxious, stressed (10 total, each with icon, emoji, label).

**Data model:** `MoodEntry` (moodValue, moodEmoji, note, profileId, entryDate).

**Database:** `mood_entries` table (migration 009).

## 3.11 Period Tracker

**Files:** `src/features/period/` (7 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| PeriodHome | `/app/period` | Cycle status card + log entry |
| LogPeriod | `/app/period/log`, `/:entryId/edit` | Start/end date, flow level, note |
| PeriodCalendarScreen | `/app/period/calendar` | Calendar view |
| CycleHistory | `/app/period/history` | Past cycles list |
| PeriodSettingsScreen | `/app/period/settings` | Cycle/period length configuration |

**Data model:** `PeriodEntry` (startDate, endDate, flowLevel, note, profileId) + `PeriodSettings` (cycleLengthDays, periodLengthDays).

**Flow levels:** light, medium, heavy.

**Database:** `period_entries` + `period_settings` tables (migration 010).

## 3.12 Vault

**Files:** `src/features/vault/` (9 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| VaultEntryRoute | `/app/vault` | Entry gate (locked/unlocked) |
| VaultLocked | (inside vault) | PIN entry for vault |
| VaultHome | `/app/vault` | Content grid (when unlocked) |
| AddVaultContent | `/app/vault/add` | Add content (photo/video/note/file) |
| VaultContentViewer | `/app/vault/:itemId` | View content |

**Content types:** photo, video, note, file.

**Access control:** Uses AppLockService — vault is accessible when app lock is disabled or unlocked. Separate from AppLockGate (vault doesn't lock the entire app, only vault access).

**Database:** `vault_items` table (migration 011).

## 3.13 Yuki Companion Cat

**Files:** `src/features/yuki/` (4 files) + `src/services/game/yukiService.ts` + `src/data/game/yukiTypes.ts`

**Route:** `/app/yuki`

**State model:**
- 4 needs (0-100): hunger, energy, happiness, cleanliness
- Derived mood score → mood name (happy/content/neutral/hungry/sad)
- Activity states: idle, eating, being-petted, playing, sleeping, grooming, purring
- Progression: level, XP, streak, accessories
- 5 actions: feed, pet, play, clean, sleep (each changes needs + grants XP)
- Time-based decay (needs decrease over hours without interaction)
- Accessory unlocks: bow-tie (streak 3), bandana (level 3), crown (level 5), star-badge (streak 7), heart-collar (50 interactions)

**Persistence:** localStorage (`twohearts_yuki` key) — NOT SQLite.

**Animation:** `yuki.css` with keyframe animations for each activity state.

## 3.14 Games System

**Files:** `src/features/games/` (7 files) + `src/services/game/` (4 files) + `src/customization/games/gameContent.ts`

**Note:** Yuki replaced the primary game system. Legacy game routes exist for backward compatibility but `/app/games` redirects to `/app/yuki`. Individual game routes still work.

**Game types:**
- Memory Match (`MemoryMatchScreen`)
- Word Scramble (`WordScrambleScreen`)
- Couple Trivia, Who Knows Who Better, Would You Rather, This Or That, Guess My Answer (`GamePlayScreen`)
- Casual Trivia, Riddle Room (`CasualGamePlayScreen`)
- Results screen shared across games

**Engine:** `GameService` → `GameEngine` with level config, difficulty resolution, seeded shuffle.

**Progression:** `gameProgression.ts` — localStorage persistence for game progress.

## 3.15 Search

**Files:** `src/features/app-shell/screens/SearchScreen.tsx` + `src/services/search/` (6 files)

**Architecture:** One `SearchEngine` with registered per-feature providers:
- `noteSearchProvider` — searches notes
- `memorySearchProvider` — searches memories
- `reminderSearchProvider` — searches reminders
- `placeSearchProvider` — searches places
- `timelineSearchProvider` — searches timeline events

**Ranking:** Prefix match > word-initial > substring; ties by recency desc, then id asc.

**Normalization:** NFKD → strip diacritics → lowercase → collapse whitespace → tokenize.

**Exclusion:** Vault content is deliberately NOT included in search results.

## 3.16 Notification Center

**Files:** `src/features/notifications/NotificationCenter.tsx` + `src/repositories/notificationCenterRepository.ts` + `src/services/notification-center/notificationCenterService.ts`

**Route:** `/app/notifications`

**Data model:** `NotificationCenterEntry` (title, body, kind, originFeature, originId, channelId, read).

**Database:** `notification_center` table (migration 012).

## 3.17 Settings

**Files:** `src/features/settings/` (12 files)

| Screen | Route | Purpose |
|--------|-------|---------|
| SettingsHomeScreen | `/app/more/settings` | Settings hub |
| ProfileSettingsScreen | `.../settings/profile` | Edit profiles + photo picker |
| RelationshipSettingsScreen | `.../settings/relationship` | Edit relationship info |
| AppearanceSettingsScreen | `.../settings/appearance` | Theme + text size |
| NotificationSettingsScreen | `.../settings/notifications` | Enable/disable notifications |
| SecuritySettingsScreen | `.../settings/security` | App lock enable/disable/change PIN |
| StorageSettingsScreen | `.../settings/storage` | Storage report + cache clear + data reset |
| ImportScreen | `.../settings/import` | JSON import for notes/reminders |
| AboutScreen | `.../more/about` | App info, version, credits |
| AppLockGate | (global overlay) | Full-app lock screen when enabled |

**Settings schema (v3, localStorage):**
- textSize, themeMode, onboarded, appLockEnabled, lockTimeoutSeconds
- firstLaunchAt, onboardingStage, notificationsEnabled, remindersEnabled, reduceMotion

## 3.18 Import System

**Files:** `src/services/import/importService.ts` + `src/features/settings/ImportScreen.tsx`

**Format:** `twohearts-import` JSON format (v1) with notes and reminders arrays.

**Supported content:** Notes (title, content, category) and Reminders (title, description, date, time, recurrence, notification).

**Behavior:** All imports create NEW records (no deduplication). Partial failure tracking.

## 3.19 Permissions

**Files:** `src/features/permissions/NotificationPermissionPrompt.tsx` + `src/services/permissions/permissionService.ts`

**States:** granted, denied, prompt, unavailable.

**V1 permissions:** Notifications only (POST_NOTIFICATIONS on Android).

## 3.20 Profile Photos

**Files:** `src/services/profile/profilePhotoService.ts` + `src/features/app-shell/useProfilePhotos.ts` + `src/components/ProfileAvatar.tsx`

**Pipeline:** File input → createImageBitmap → canvas resize (400×400px, JPEG 85%) → MediaStorage.store → Profile.photoRef link.

**Resolution:** MediaStorage.resolveUrl → data: URL for display.

## 3.21 Data Management

**Files:** `src/services/maintenance/dataManagementService.ts`

**Capabilities:**
- Storage report (row counts, media bytes, pending notifications)
- Cache clear (orphan media sweep only)
- Full reset (cancel notifications → delete domain rows → sweep media → remove PIN → reset settings)

---

# PART 4 — DATA / STORAGE / SECURITY

## 4.1 Database Schema (13 Migrations)

| Migration | Name | Tables Added |
|-----------|------|-------------|
| 001 | initial | schema_migrations, settings, media_assets |
| 002 | notification_registry | notification_registry |
| 003 | relationship_foundation | profiles, couple_relationship, important_dates |
| 004 | memories | memories, memory_media |
| 005 | notes | notes |
| 006 | timeline | timeline_events |
| 007 | reminders | reminders |
| 008 | places | places |
| 009 | mood | mood_entries |
| 010 | period_tracker | period_entries, period_settings |
| 011 | vault | vault_items |
| 012 | notification_center | notification_center |
| 013 | profile_photo | (adds photo_ref column to profiles) |

## 4.2 Entity Conventions

Every persisted entity follows:
- `id: string` — UUID v4 (crypto.getRandomValues)
- `createdAt: string` — ISO 8601 UTC
- `updatedAt: string` — ISO 8601 UTC (refreshed on every update)
- `deletedAt: string | null` — optional tombstone for soft deletes
- Serializers: one `EntitySerializer<T>` per entity (column list, toParams, fromRow)
- snake_case in SQL, camelCase in TypeScript, converted exclusively through serializers

## 4.3 Storage Locations

| Data | Storage | Location |
|------|---------|----------|
| Domain data (memories, notes, etc.) | SQLite | Private app storage (`twohearts.db`) |
| Media files (photos, videos) | Filesystem | `media/` directory in private app storage |
| App settings | localStorage | `twohearts.settings.v1` key |
| Yuki companion state | localStorage | `twohearts_yuki` key |
| Game progression | localStorage | `twohearts_game_progression` key |
| PIN material (salt + verifier) | SecureStore | Android Keystore (EncryptedSharedPreferences) |

## 4.4 Security Architecture

### App Lock
- PIN: 4-8 digits, stored as PBKDF2-HMAC-SHA-256 hash (120k iterations, 128-bit random salt)
- Storage: Android Keystore via @aparajita/capacitor-secure-storage
- Lock state: MEMORY-ONLY (cold start always locks when enabled)
- Re-lock: Automatic on foreground return if background time > timeout (default 60s)
- UI: `AppLockGate` covers entire app when locked

### Vault
- Separate from App Lock — uses the same PIN but has its own access control
- Vault content never appears in search, notifications, or home previews
- Access check: `VaultService.isAccessible()` verifies lock state

### Security Rules
- React components never import plugins or storage
- Each Capacitor plugin imported in exactly ONE driver file
- PIN material never enters settings, UI state, or logs
- Logger redacts keys matching: pin, password, secret, token, body, content, vault, media
- No secrets, credentials, or API keys anywhere in the codebase
- No network calls — truly offline-first

## 4.5 Offline-First Architecture

- All assets are local SVGs (25 total, ~few KB each)
- No remote image URLs, no CDN dependencies
- Database is local SQLite (native) / sql.js (web)
- Media storage is local filesystem
- Game state is localStorage
- Settings are localStorage
- No FCM, no push notifications, no cloud sync
- The APK bundles ALL web assets for offline use
- `base: './'` in Vite config ensures relative asset paths in the APK

---

# PART 5 — UI / UX / DESIGN SYSTEM

## 5.1 Design Tokens (`src/theme/tokens.css`)

### Color System
- **Brand primary:** Burgundy (#6A1B2B) with extended scale (50-900)
- **Neutrals:** Cream (#FDF6F0), Blush (#F6E1DE), Rose-muted (#C9808B), Pink (#E8A0B4), Beige (#EDE0D4), Charcoal (#2B2420), Warm-ivory, Dusty-rose, Plum, Sage
- **Surfaces:** Surface (#FFFFFF), Surface-elevated (#FFFDFB), Surface-warm (#FFF8F4), Surface-blush (#FFF0EC)
- **Feedback:** Success (#4F7A5A), Warning (#B07A1E), Error (#A33A2A)
- **Dark theme:** `[data-th-theme='dark']` — warm dark surfaces from charcoal/plum family, luminous burgundy accent

### Typography
- **Base:** System UI stack (Segoe UI, system-ui, -apple-system, Helvetica Neue, Roboto)
- **Display:** Georgia, Palatino, Times New Roman, serif
- **Scale:** 8 sizes (xs through 4xl), all multiplied by `--th-text-scale`
- **Text size options:** Small (0.88x), Default (1x), Large (1.12x), Extra Large (1.28x)

### Spacing
- 4pt base grid: 0, 0.25rem through 5rem

### Radii
- sm (0.5rem) through pill (9999px) and circle (50%)

### Shadows
- Warm undertones (burgundy/charcoal), never cold gray
- 5 tiers: xs, sm, md, lg, xl
- Special: nav, focus, glow, card-hover

### Motion
- 6 durations: instant (1ms), fast (100ms), normal (200ms), slow (320ms), drift (6400ms), spin (600ms)
- 6 easings: standard, decelerate, accelerate, emphasized, press, spring
- Semantic pairs: fast, standard, slow, entrance, exit, press, modal, drift, spring
- Reduced motion: all durations collapse to 1ms via `@media (prefers-reduced-motion)` and `data-th-motion="reduced"`

### Component Dimensions
- Touch target minimum: 44px
- Header height: 56px
- Bottom nav height: 64px
- Screen max width: 480px
- Nav center size: 58px
- Avatar large: 72px

## 5.2 Component Library (`src/components/`)

| Component | File | Purpose |
|-----------|------|---------|
| Button | Button.tsx | Primary/secondary/ghost/danger variants, full-width option |
| Card | Card.tsx | Container with elevation |
| Input | Input.tsx | Text input with label |
| Header | Header.tsx | Screen header with back button |
| IconButton | IconButton.tsx | Icon-only button |
| Divider | Divider.tsx | Visual separator |
| EmptyState | EmptyState.tsx | Empty feature state with illustration |
| LoadingState | LoadingState.tsx | Spinner + caption |
| Modal | Modal.tsx | Centered dialog + bottom sheet |
| ConfirmDialog | ConfirmDialog.tsx | Destructive action confirmation |
| StatusBanner | StatusBanner.tsx | Status messages |
| Switch | Switch.tsx | Toggle switch |
| DatePicker | DatePicker.tsx | Branded date picker |
| TimePicker | TimePicker.tsx | Wheel time picker |
| Toast | toast.tsx | System-wide toast notifications |
| BrandLogo | BrandLogo.tsx | Brand mark/full logo (one source) |
| ProfileAvatar | ProfileAvatar.tsx | Profile photo with fallback |
| RoseLilyDecoration | decorations.tsx | 14 approved floral variants |
| OnboardingArt | decorations.tsx | 7 onboarding illustration variants |
| Icon set | Icon.tsx | 30+ stroke-based SVG icons |

**CSS:** All primitives styled via `src/components/primitives.css` (10,047 lines).

## 5.3 Animations & Motion

- **Entrance:** `th-scale-in` (empty states), `th-dialog-in` (modals), `th-slide-up` (bottom sheets)
- **Theme transition:** Surface color animation on theme flip
- **Toast:** Fade + slide, auto-dismiss at 2.4s
- **Route transition:** `.th-route-transition` fade + rise on navigation
- **Yuki:** Per-activity CSS keyframe animations (eating, playing, sleeping, etc.)
- **Press feedback:** `.th-pressable` scale effect
- All compositor-friendly (transform + opacity only)
- Reduced motion: all animations disabled via OS preference or in-app setting

## 5.4 Responsive Behavior

- Mobile-first design (portrait orientation)
- Safe areas via `env(safe-area-inset-*)` for notched devices
- `min-height: 100dvh` for proper viewport on mobile
- Text scaling via `--th-text-scale` multiplier
- No media query breakpoints (single-column mobile layout)

## 5.5 Dark Mode

- Controlled via `data-th-theme="dark"` on document root
- Three options: light, dark, system (follows OS preference)
- All dark tokens defined in `tokens.css` under `[data-th-theme='dark']`
- Brand logo auto-recolors via CSS rule `.th-brand-logo--light` in dark theme
- Theme transitions animate surface colors

---

# PART 6 — ANDROID / BUILD / DEVELOPMENT

## 6.1 Capacitor Integration

- **Capacitor version:** 6.2.x
- **Android platform:** 34 (Android 14)
- **AGP:** 8.2.1
- **Gradle:** 8.2.1
- **JDK:** 21 (Temurin)
- **WebView:** Chrome (bundled in APK)

### Capacitor Plugins Used

| Plugin | Package | Purpose |
|--------|---------|---------|
| SQLite | @capacitor-community/sqlite | Local database |
| Filesystem | @capacitor/filesystem | Media file storage |
| Local Notifications | @capacitor/local-notifications | Scheduled reminders |
| Device | @capacitor/device | Device info + capabilities |
| App | @capacitor/app | Lifecycle events (foreground/background/back) |
| StatusBar | @capacitor/status-bar | Android status bar styling |
| Secure Storage | @aparajita/capacitor-secure-storage | PIN storage via Keystore |

### Android Manifest Permissions
- `POST_NOTIFICATIONS` — Local notifications
- `SCHEDULE_EXACT_ALARM` — Precise reminder timing

## 6.2 Build Pipeline

```
Source (TypeScript + React)
  → tsc -b (typecheck)
  → Vite build (dist/)
  → cap sync android (copy dist/ to android/app/src/main/assets/public/)
  → Gradle assembleDebug
  → APK at android/app/build/outputs/apk/debug/app-debug.apk
```

## 6.3 CI/CD

GitHub Actions workflow (`.github/workflows/build-android.yml`):
1. Checkout → Node 22 → npm ci → npm test
2. Vite production build
3. Java 21 + Android SDK 34
4. Capacitor sync → Gradle debug build
5. Upload APK artifact (30-day retention)

---

# PART 7 — DEPENDENCIES AND INTEGRATIONS

## 7.1 Runtime Dependencies (10)

| Package | Version | Purpose | Justification |
|---------|---------|---------|---------------|
| react | ^18.3.1 | UI framework | Core |
| react-dom | ^18.3.1 | DOM renderer | Core |
| react-router-dom | ^6.26.2 | Client routing | Core |
| @capacitor/core | ^6.2.0 | Capacitor runtime | Core |
| @capacitor/android | ^6.2.0 | Android platform | Core |
| @capacitor-community/sqlite | ^6.0.1 | Native SQLite | Phase 2: local-first DB |
| sql.js | ^1.14.2 | SQLite WASM | Phase 2: dev/test DB |
| @capacitor/filesystem | ^6.0.4 | File storage | Phase 2: media storage |
| @capacitor/local-notifications | ^6.1.3 | Local notifications | Phase 3: reminder scheduling |
| @aparajita/capacitor-secure-storage | ^6.0.1 | Keystore storage | Phase 3: PIN security |
| @capacitor/app | ^6.0.2 | App lifecycle | Phase 3: foreground/back |
| @capacitor/device | ^6.0.3 | Device info | Phase 3: capabilities |
| @capacitor/status-bar | ^6.0.2 | Status bar | Phase 6: Android UI |

## 7.2 Dev Dependencies (7)

| Package | Version | Purpose |
|---------|---------|---------|
| typescript | ^5.6.3 | Type checking |
| vite | ^5.4.11 | Build tooling |
| @vitejs/plugin-react | ^4.3.4 | React Fast Refresh |
| @capacitor/cli | ^6.2.0 | Capacitor CLI |
| @types/react | ^18.3.12 | React types |
| @types/react-dom | ^18.3.1 | ReactDOM types |
| @types/sql.js | ^1.4.11 | sql.js types |

## 7.3 External Integrations

**None.** TwoHearts V1 is a completely standalone offline application. No API keys, no third-party services, no network calls.

---

# PART 8 — LEGACY / TECHNICAL DEBT / RISKS

## 8.1 Legacy Systems Still in Codebase

### Games System (Partially Superseded)
- **Status:** Yuki companion replaced games as the primary engagement feature (Stage 8)
- **Remaining code:** Full game engine, game screens, game content, progression system
- **Routes:** `/app/games` redirects to `/app/yuki`, but individual game routes still work (`/app/games/memory-match`, etc.)
- **Risk:** Low — code is functional but the navigation entry point redirects away from it
- **Migration note:** Games can be either archived or maintained as a secondary feature

### Old Game System in AppRouter
- **Status:** Legacy game routes imported in `AppRouter.tsx` (GamePlayScreen, GameResultsScreen, MemoryMatchScreen, WordScrambleScreen, CasualGamePlayScreen)
- **Risk:** These imports add to bundle size even though `/app/games` redirects to Yuki
- **Migration note:** Remove unused imports or keep for backward compatibility

## 8.2 Technical Debt

### Yuki Persistence Split
- **Issue:** Yuki state is in localStorage while everything else is in SQLite
- **Risk:** Medium — creates a two-system persistence model; Yuki data is not included in export/import or data management reset
- **Migration note:** Consider migrating Yuki to SQLite for consistency

### CSS File Size
- **Issue:** `primitives.css` is 10,047 lines; `global.css` is 694 lines
- **Risk:** Low for mobile (bundled) but affects maintainability
- **Migration note:** Consider CSS modules or component-scoped styles in migration

### Inline Styles in Some Components
- **Issue:** Some components (AppLockGate, ErrorBoundary, main.tsx SplashView) use inline styles instead of CSS classes
- **Risk:** Low — functionally correct but inconsistent with the design token system
- **Migration note:** Convert inline styles to CSS classes using design tokens

### Missing Export System
- **Issue:** Import system exists (JSON format for notes/reminders) but no export system
- **Risk:** Low — user data is trapped on device with no backup mechanism
- **Migration note:** The `src/services/backup/exportFormat.ts` defines the envelope format but no UI triggers it

### Profile Photo Service — No Native Camera Integration
- **Issue:** Uses HTML file input with `capture="environment"` attribute, not the Capacitor Camera plugin
- **Risk:** Low — works on Android WebView but offers a less native-feeling camera experience
- **Migration note:** Could integrate @capacitor/camera for a more native photo picker

### Test Coverage Gaps
- **Issue:** 55 test files, 948+ tests, but tests are primarily on services/repositories/data layer
- **Risk:** Low — the critical data pipeline is well-tested; UI components are not unit-tested
- **Migration note:** Consider adding component tests (React Testing Library) in migration

## 8.3 Known Limitations

1. **No cloud sync or backup** — data loss if device is lost/wiped
2. **No partner sync** — each partner must use the same device (or manually transfer data)
3. **No real-time features** — no live updates between devices
4. **No export UI** — export format exists in code but no screen to trigger it
5. **No video playback enhancements** — basic HTML5 video only
6. **No batch operations** — no bulk select/delete for any feature
7. **No accessibility audit tools** — manual verification only
8. **No analytics** — no visibility into feature usage or crashes
9. **Period tracker is single-profile** — only the owner tracks, partner cannot
10. **Games have no persistent leaderboard** — results are per-session only

---

# PART 9 — MIGRATION PRINCIPLES

The following principles MUST be followed by any agent performing the migration:

## P1: Migrate the Product, Not the Code
The objective is to preserve and improve the TwoHearts product experience. Internal implementation may be entirely different in the new stack. What matters is that the app provides the same (or better) functionality, data integrity, and user experience.

## P2: Understand Before Converting
For each system, the migration agent must:
1. Read the existing implementation
2. Read its documentation
3. Understand its data and behavior
4. Determine the correct equivalent in the new architecture
5. Recreate that feature appropriately
6. Preserve important functionality
7. Document the architectural decision

## P3: Do Not Get Stuck on Direct Translation
If something cannot be reasonably migrated line-for-line, recreate it. The new implementation does not need to resemble the old implementation internally. It needs to provide the correct TwoHearts experience.

## P4: Preserve Offline-First
TwoHearts is fundamentally an offline-first application. The migration must maintain this property. Do not introduce cloud dependencies that break offline functionality.

## P5: Preserve Privacy
No analytics, no telemetry, no remote data transmission. The app's data stays on-device unless the user explicitly exports it.

## P6: Preserve Data Integrity
All existing user data must be preserved or safely migratable. The schema must maintain backwards compatibility or provide a clear migration path.

## P7: Preserve Security
PIN hashing (PBKDF2), secure storage (Keystore), vault access control, and all security properties must be maintained or improved in the migration.

## P8: Preserve Accessibility
Text scaling, reduced motion, touch targets, screen reader support, and contrast ratios must be maintained.

## P9: Preserve the Core Brand Identity
The TwoHearts name, official TwoHearts logo, core brand identity, and couples-oriented purpose must be preserved. The current visual implementation (CSS, components, Rose/Lily SVGs, icons, 77 screen designs) should be treated as a reference for product intent — not as a mandatory visual specification for the native Android application.

## P10: The Agent Has Full Authority
See the detailed "Future Migration Authority" section (after Part 9) for the complete scope of the agent's authority.

## P11: Clean Architecture
The migration should improve code organization, reduce technical debt (Yuki localStorage, CSS file size), and establish a cleaner architecture for future development.

## P12: Preserve or Improve All 77 Screens
Every screen that exists in V1 must have an equivalent in the migrated version. The migration may improve screens but must not remove functionality.

## P13: Archive, Don't Delete
Old code and assets should be archived (moved to Archive/ folder), not deleted, during migration. This preserves reference material and prevents data loss. See Part 13 for complete archive rules.

---

# PART 9A — FUTURE MIGRATION AUTHORITY

The future agent(s) performing the TwoHearts migration have **full authority** over every aspect of the migration:

- **Full engineering authority** — choose the best architecture, frameworks, tools, and patterns for the native Android application.
- **Full architectural authority** — redesign the data layer, service layer, UI layer, and navigation as needed.
- **Full creative authority** — redesign layouts, visual presentation, interaction patterns, and user experience.
- **Full UI/UX design authority** — create modern Android-native UI patterns, components, navigation, animations, typography, layouts, icons, illustrations, graphics, materials, motion, and interaction patterns.
- **Full asset-selection authority** — source, create, or replace any visual assets.
- **Freedom to recreate systems** when direct conversion from the React/Vite/Capacitor implementation is inappropriate, difficult, or would produce an inferior result.
- **Freedom to replace outdated implementations** that do not serve the product well.
- **Freedom to redesign features** for the new native Android environment.

The future agent must migrate the **TwoHearts product**, not blindly reproduce the current implementation.

If an existing implementation is difficult, obsolete, poorly designed, or unsuitable for the new architecture, the agent may build a better native equivalent while preserving the intended functionality and product purpose.

**This is NOT a one-to-one code translation.**

The existing repository, feature documentation, historical screens, and this master roadmap exist to help the agent understand what TwoHearts is. The agent should use its own engineering and design judgment to determine how TwoHearts should be implemented natively.

---

# PART 9B — DESIGN / ASSET DIRECTIVE

## Current Implementation Is a Reference, Not a Specification

The current TwoHearts visual implementation must **not** be treated as the permanent design system for the future native Android application.

The current UI was created around the React/Vite/Capacitor environment and should therefore be treated as a **reference for existing product intent and functionality**, not as a mandatory visual specification.

The future native Android application **may** use modern Android-native:

- UI patterns, components, and layouts
- Navigation patterns (Android standard navigation)
- Animations and transitions
- Typography and text rendering
- Icons and iconography
- Illustrations and graphics
- Materials and elevation
- Motion and interaction patterns
- Accessibility patterns

## Brand Exception — Non-Negotiable

The following remain **fixed product identity elements** unless the master product requirements explicitly change them:

- **TwoHearts name**
- **Official TwoHearts logo**
- **Core TwoHearts identity**
- **Couples-oriented purpose**

Everything else in the visual implementation **may** be reconsidered during migration if doing so produces a substantially better native Android experience.

## Asset Freedom

- The existing **Rose/Lily SVG collection may be replaced**.
- The existing **77 screen visual designs** may be treated as historical/reference material rather than mandatory designs.
 - **Existing icons may be replaced**.
- **Existing decorative assets may be replaced**.
- **Existing UI components may be redesigned**.
- **New SVGs/assets may be created** specifically for the native Android application.
- Appropriate **free/open-license assets may be sourced** when useful.
 - Any externally sourced asset **must** have a license compatible with use and redistribution in TwoHearts.
 - Do **not** use assets with unclear licensing.
 - Do **not** introduce remote asset dependencies.

## Important: This Is Not a "React → Native" Translation

The future migration is **not** an exercise in:

> "React/Vite/Capacitor → native Android one-to-one translation."

It **is**:

> "Existing TwoHearts product → best possible native Android implementation."

The future agent should use the existing repository, feature documentation, historical screens, and this master roadmap to understand what TwoHearts is, while using its own engineering and design judgment to determine how TwoHearts should be implemented natively.

---

# PART 10 — COMPLETE MIGRATION STAGE ROADMAP

## Stage 0 — Reconnaissance & Setup

**Objective:** Establish the new project foundation and complete understanding of what needs to migrate.

**Systems involved:** All — full codebase audit.

**What needs to be converted:** Nothing yet. This stage is understanding + planning.

**Dependencies:** None.

**Risks:** Underestimating the scope of the existing application.

**Recommended approach:**
1. Read this master document thoroughly
2. Run the existing test suite to establish a passing baseline
3. Document the target stack (new framework, new database, new build system)
4. Create the new project scaffold alongside the existing code (monorepo or separate directory)
5. Verify the new project builds and runs before beginning migration

**What must remain unchanged:** The existing V1 codebase must not be modified during this stage.

**Verification requirements:**
- New project scaffold builds successfully
- Existing tests still pass (no regressions)
- Migration plan reviewed and approved

**Completion criteria:** New project scaffold exists, builds, and the migration plan is documented.

**Next stage depends on:** Stage 0 completion.

---

## Stage 1 — Design System & Token Migration

**Objective:** Recreate the TwoHearts design system in the new stack so all UI work from this point forward uses the correct visual language.

**Systems involved:** Theme tokens, CSS, components, brand assets.

**What needs to be converted:**
- `tokens.css` → new token system (CSS variables, Tailwind theme, or equivalent)
- `tokens.ts` → typed constants in new language/framework
- `primitives.css` component classes → new component styling approach
- `global.css` → global styles in new framework
- Brand assets (3 SVGs) → same SVGs, new import mechanism
- Decorations (20 rose-lily SVGs) → same SVGs, new import mechanism
- Icon set (30+ icons) → same icons, new component format

**Dependencies:** Stage 0.

**Risks:**
- Losing design fidelity during token translation
- CSS class naming conflicts if mixing old and new

**Recommended approach:**
1. Copy brand SVGs and decoration SVGs to new project
2. Recreate tokens as CSS custom properties (framework-agnostic, portable)
3. Port typed token mirror
4. Port global CSS (reset, typography, safe areas)
5. Port primitives CSS component classes (button, card, input, etc.)
6. Verify dark mode works in new stack
7. Verify text scaling works in new stack
8. Verify reduced motion works in new stack

**What must remain unchanged:** All SVG assets, all token values, all semantic color/spacing/motion names.

**Verification requirements:**
- All design tokens render correctly in the new stack
- Dark mode toggle works
- Text size scaling works
- Reduced motion mode works
- Component styles match V1 visually

**Completion criteria:** The new project renders a styled page with correct TwoHearts colors, typography, and spacing.

**Next stage depends on:** Stage 1.

---

## Stage 2 — Component Library Migration

**Objective:** Recreate all shared UI components in the new framework.

**Systems involved:** All 19 components in `src/components/`.

**What needs to be converted:**
- Button, Card, Input, Header, IconButton, Divider
- EmptyState, LoadingState, Modal, ConfirmDialog, StatusBanner
- Switch, DatePicker, TimePicker
- BrandLogo, ProfileAvatar, RoseLilyDecoration, OnboardingArt
- Icon set (30+ icons)
- Toast system (ToastProvider, useToast)
- Decorations system (RoseLilyDecoration, OnboardingArt)

**Dependencies:** Stage 1 (design tokens must exist).

**Risks:**
- Component API differences between React and target framework
- Toast provider architecture may differ

**Recommended approach:**
1. Start with the simplest components (Button, Divider, Input)
2. Build up to containers (Card, Modal, Screen)
3. Port the Icon set (SVG components)
4. Port BrandLogo (references branding SVGs)
5. Port ProfileAvatar (needs photo resolution)
6. Port decorations (references rose-lily SVGs)
7. Port Toast system
8. Port complex components (DatePicker, TimePicker)

**What must remain unchanged:** Component APIs, prop interfaces, visual appearance, accessibility attributes.

**Verification requirements:**
- All components render correctly
- All component props work as expected
- Accessibility attributes (aria-label, role, etc.) are preserved

**Completion criteria:** All shared components available and working in the new stack.

**Next stage depends on:** Stage 2.

---

## Stage 3 — Data Layer Migration

**Objective:** Recreate the entire persistence architecture in the new stack.

**Systems involved:** Database adapter, connection, migrations, entity model, serializers, repositories, settings storage, media storage.

**What needs to be converted:**
- `DatabaseAdapter` interface → new adapter (or same interface in new language)
- `connection.ts` → initialization + singleton pattern
- All 13 migrations → equivalent in new database
- `Entity`, `TombstonedEntity`, `NewEntity` → equivalent type system
- `EntitySerializer` pattern → equivalent serialization
- `BaseRepository<T>` → generic repository base class
- All 14 repositories → new implementations
- `SettingsStorage` → equivalent for app settings
- `MediaStorage` + `MediaFileSystem` → equivalent media pipeline
- `PERSISTENCE_CONFIG` → equivalent configuration

**Dependencies:** Stage 0 (new project must exist).

**Risks:**
- Database engine differences (SQL syntax, type handling)
- Migration compatibility (must preserve data format for existing users)
- Media storage API differences between platforms

**Recommended approach:**
1. Choose the database engine for the new stack (SQLite via better-sqlite3? Drizzle ORM? Prisma?)
2. Define the entity model types
3. Port the serialization pattern
4. Create the adapter interface
5. Port all 13 migrations (or equivalent schema definition)
6. Port the connection/initialization logic
7. Port BaseRepository with CRUD + tombstones
8. Port all 14 repositories
9. Port settings storage (localStorage → equivalent)
10. Port media storage + filesystem adapter
11. Run the existing migration tests against the new implementation

**What must remain unchanged:** All SQL schema definitions, all entity field names, all timestamp conventions (ISO 8601 UTC), all UUID conventions (v4), all tombstone behavior.

**Verification requirements:**
- All 13 migrations run successfully
- All repositories perform CRUD correctly
- Tombstone semantics work (soft delete, exclude deleted by default)
- Media storage + resolve works
- Settings read/write works
- All existing tests pass (adapted to new stack)

**Completion criteria:** Complete data layer functional with all repositories, migrations, and storage working.

**Next stage depends on:** Stage 3.

---

## Stage 4 — Core Services Migration

**Objective:** Recreate the infrastructure services that sit between UI and data.

**Systems involved:** All services in `src/services/`.

**What needs to be converted:**
- Bootstrap pipeline (ordered initialization stages)
- Error taxonomy (AppError with categories, codes, safe messages)
- Logger (leveled, scoped, with redaction)
- Validators (pure functions returning {ok, errors})
- DateTime helpers (age calculation, anniversary math, local-day diffs)
- Device capabilities
- Permission service
- Lifecycle service (foreground/background/back events)
- File service (non-media files)
- Media utilities (MIME validation, size limits, content sniffing)
- Search engine (normalization, providers, ranking)
- Security (AppLockService, pinHash, SecureStore)
- Notification service (channels, scheduling, registry)
- Relationship service
- AppState service
- DataManagement service

**Dependencies:** Stage 3 (data layer must exist).

**Risks:**
- Capacitor plugin APIs may differ in the new framework
- WebCrypto availability for PIN hashing
- Lifecycle event differences between frameworks

**Recommended approach:**
1. Port error taxonomy and normalization
2. Port logger with redaction rules
3. Port validators (pure functions, easy to port)
4. Port DateTime helpers (pure functions)
5. Port DeviceCapabilities
6. Port PermissionService
7. Port LifecycleService
8. Port FileService + file adapters
9. Port MediaStorage + media utils
10. Port Security services (AppLockService, pinHash, SecureStore)
11. Port NotificationService + drivers
12. Port Bootstrap pipeline
13. Port SearchEngine + normalization
14. Port RelationshipService + AppStateService
15. Port DataManagementService
16. Port import/export services

**What must remain unchanged:** All security properties (PBKDF2 parameters, constant-time comparison), all service boundaries, all error codes and user messages.

**Verification requirements:**
- Bootstrap pipeline completes successfully
- AppLockService works (enable, unlock, lock, disable, re-lock on foreground)
- NotificationService schedules and reconciles
- Search engine returns correct results
- Data management reset works
- All existing tests pass

**Completion criteria:** All core services functional and tested.

**Next stage depends on:** Stage 3, Stage 4.

---

## Stage 5 — Onboarding Flow Migration

**Objective:** Recreate the first-launch experience.

**Systems involved:** OnboardingGate, all onboarding screens, useOnboarding hook, OnboardingLayout.

**What needs to be converted:**
- SplashScreen (splash during bootstrap)
- WelcomeScreen (first-launch welcome)
- ProfileSetupScreen (owner name + birthday)
- RelationshipSetupScreen (partner name + start date)
- PersonalizationSetupScreen (theme + text size)
- AppLockSetupScreen (optional PIN setup)
- SetupCompleteScreen (celebration + navigation to app)
- OnboardingGate (state evaluation + routing)
- useOnboarding hook (stage progression)
- OnboardingLayout (shared shell with step indicator)

**Dependencies:** Stage 2 (components), Stage 3 (data layer for profiles/couple), Stage 4 (core services for bootstrap).

**Risks:**
- Onboarding flow depends on multiple services (RelationshipService, AppStateService, AppLockService)
- Route guard logic must be correct to prevent incomplete users from accessing the app

**Recommended approach:**
1. Port OnboardingLayout (step indicator, back navigation)
2. Port WelcomeScreen (simple, mostly static)
3. Port ProfileSetupScreen (form + validation)
4. Port RelationshipSetupScreen (form + date picker + validation)
5. Port PersonalizationSetupScreen (theme + text size selectors)
6. Port AppLockSetupScreen (PIN creation)
7. Port SetupCompleteScreen (celebration + CTA)
8. Port OnboardingGate (state evaluation + redirect logic)
9. Port useOnboarding hook
10. Verify the complete flow from fresh install to app entry

**What must remain unchanged:** All onboarding steps, all validation rules, all persistence behavior (onboardingStage tracking), the SetupComplete celebration UX.

**Verification requirements:**
- Fresh install enters onboarding
- Each step validates correctly
- AppLock setup creates a working PIN
- Completing onboarding navigates to /app/home
- Returning to the app after kill resumes onboarding if incomplete
- Theme and text size selections apply immediately

**Completion criteria:** Complete onboarding flow works from fresh install to app entry.

**Next stage depends on:** Stage 2, Stage 3, Stage 4.

---

## Stage 6 — App Shell & Navigation Migration

**Objective:** Recreate the main application layout, bottom navigation, and all routing.

**Systems involved:** AppShell, BottomNav, AppRouter, routes, navConfig, navIcons.

**What needs to be converted:**
- AppShell (content area + bottom nav + toast host + back button handling)
- BottomNav (5-position floating pill nav with center brand button)
- AppRouter (all routes)
- RoutePath constants
- navConfig (destination vocabulary)
- navIcons (icon bridge)
- Route transitions (fade + rise animation)

**Dependencies:** Stage 2 (components), Stage 5 (onboarding flow).

**Risks:**
- Route structure must exactly match V1 to preserve deep links/bookmarks
- Back button behavior must be correct (deep navigation vs. home root)

**Recommended approach:**
1. Define all routes (copy RoutePath constants exactly)
2. Port the route map structure
3. Port AppShell layout (scrollable content + bottom nav)
4. Port BottomNav (5-position with center brand button)
5. Port navConfig and navIcons
6. Port Android back button behavior
7. Port route transitions
8. Verify all routes navigate correctly

**What must remain unchanged:** All route paths, all navigation behavior, the 5-position bottom nav design, the elevated center brand button, back button behavior rules.

**Verification requirements:**
- All routes navigate correctly
- Bottom nav highlights active position
- Center brand button navigates to Us hub
- Back button works correctly (deep → previous, home root → no-op, no history → home)
- Route transitions animate correctly
- Toast host survives navigation

**Completion criteria:** Full navigation shell with all routes functional.

**Next stage depends:** Stage 2, Stage 5.

---

## Stage 7 — Home, Us, & Core Hub Screens Migration

**Objective:** Recreate the main dashboard and relationship hub.

**Systems involved:** HomeScreen, UsScreen, MoreScreen, relationship counter, couple pair, home highlights, profile photos.

**What needs to be converted:**
- HomeScreen (couple header, greeting, counter, 4 action cards, decorations)
- UsScreen (couple pair display, "Our Story" group, "Our World" group)
- MoreScreen (settings, search, about)
- CouplePair component
- relationshipCounter helper
- useHomeHighlights hook
- useProfilePhotos hook
- ProfileAvatar integration with MediaStorage
- ProfilePhotoService (pick, process, store, link, resolve, remove)

**Dependencies:** Stage 2 (components), Stage 3 (data layer), Stage 4 (services), Stage 6 (navigation).

**Risks:**
- HomeScreen depends on RelationshipService for couple data
- Profile photos need MediaStorage + canvas resize pipeline

**Recommended approach:**
1. Port CouplePair component
2. Port relationship counter logic
3. Port HomeScreen with all sections
4. Port UsScreen with grouped navigation cards
5. Port MoreScreen with utility links
6. Port ProfileAvatar with photo resolution
7. Port useProfilePhotos hook
8. Port ProfilePhotoService (file input → canvas resize → MediaStorage)
9. Verify all data flows correctly

**What must remain unchanged:** Home screen layout, couple header design, action card layout, Us hub grouping, profile photo pipeline behavior.

**Verification requirements:**
- Home screen shows correct couple data
- Profile photos display correctly
- Navigation to all features works from Home
- Us hub shows all feature groups
- More screen has correct links

**Completion criteria:** Home, Us, and More screens fully functional.

**Next stage depends on:** Stage 6.

---

## Stage 8 — Content Features Migration (Notes, Memories, Timeline)

**Objective:** Recreate the three main content creation features.

**Systems involved:** Notes, Memories, Timeline — all screens, services, repositories, types.

**What needs to be converted:**
- **Notes:** NotesHome, NoteEditor, NoteDetail, useNoteService, categoryMeta, categoryIcons, noteTime helpers
- **Memories:** MemoriesHome, AddMemory, MemoryDetail, useMemoryService, memoryFilters
- **Timeline:** TimelineHome, AddEvent, EventDetail, useTimelineService, timelineStory helpers

**Dependencies:** Stage 3 (repositories), Stage 4 (services), Stage 6 (navigation).

**Risks:**
- Memories depend on MediaStorage for photo/video handling
- Timeline has chapter system (chapterOf helper)
- Notes have 7 categories with specific visual treatment

**Recommended approach:**
1. Port Notes types + repository + service + hooks
2. Port Notes screens (Home, Editor, Detail)
3. Port Notes category system (meta + icons)
4. Port Notes time helpers
5. Port Memories types + repository + service + hooks
6. Port Memories screens (Home, Add, Detail)
7. Port Memories media pipeline integration
8. Port Memories filters
9. Port Timeline types + repository + service + hooks
10. Port Timeline screens (Home, Add, Detail)
11. Port Timeline story helpers (formatEventDate, buildStoryRows, chapterOf)
12. Verify all CRUD operations + toasts

**What must remain unchanged:** All note categories, memory media pipeline, timeline chapter system, all CRUD behavior.

**Verification requirements:**
- Notes CRUD with all 7 categories works
- Memories with photo upload/display works
- Timeline story view with chapters works
- Toast notifications fire on save/update/delete
- Empty states display correctly

**Completion criteria:** All three content features fully functional with persistence.

**Next stage depends on:** Stage 3, Stage 6.

---

## Stage 9 — Relationship Features Migration (Reminders, Places, Mood, Period, Important Dates)

**Objective:** Recreate the relationship-oriented features.

**Systems involved:** Reminders, Places, Mood, Period Tracker, Important Dates, Relationship Counter.

**What needs to be converted:**
- **Reminders:** RemindersHome, CreateReminder, ReminderDetail, useReminderService, reminderSchedule
- **Places:** PlacesHome, CreatePlace, PlaceDetail, usePlaceService, placePresentation
- **Mood:** MoodHome, MoodEntry, MoodHistory, useMoodService, moodPresentation, moodMeta
- **Period:** PeriodHome, LogPeriod, PeriodCalendarScreen, CycleHistory, PeriodSettingsScreen, usePeriodService, periodPresentation, flowMeta
- **Important Dates:** ImportantDatesScreen (us/reminders route)

**Dependencies:** Stage 3 (repositories), Stage 4 (services including NotificationService), Stage 6 (navigation).

**Risks:**
- Reminders coordinate with NotificationService for local scheduling
- Period tracker has complex cycle calculations
- Places support photo storage

**Recommended approach:**
1. Port Reminder types + repository + service + hooks + schedule helpers
2. Port Reminder screens with TimePicker integration
3. Port Place types + repository + service + hooks + presentation helpers
4. Port Place screens with photo support
5. Port Mood types + repository + service + hooks + presentation helpers
6. Port Mood screens with icon grid + history
7. Port Period types + repository + service + hooks + presentation helpers
8. Port Period screens with calendar view + settings
9. Port Important Dates screen
10. Verify notification scheduling integration
11. Verify all toast feedback

**What must remain unchanged:** Reminder scheduling + notification coordination, period cycle calculations, mood distribution statistics, place photo pipeline.

**Verification requirements:**
- Reminders schedule local notifications correctly
- Places store and display photos
- Mood check-ins record and display correctly
- Period tracker calculates cycles correctly
- Important dates display with recurrence

**Completion criteria:** All relationship features fully functional.

**Next stage depends on:** Stage 4, Stage 6.

---

## Stage 10 — Vault & Security Features Migration

**Objective:** Recreate the private vault and verify all security properties.

**Systems involved:** Vault (all screens + service), AppLock (gate + settings), Security settings.

**What needs to be converted:**
- **Vault:** VaultEntryRoute, VaultLocked, VaultHome, AddVaultContent, VaultContentViewer, useVaultService, vaultPresentation, vaultRoutes, contentTypeMeta
- **App Lock:** AppLockGate (full-app lock overlay), SecuritySettingsScreen
- **Vault access control:** VaultService.isAccessible(), lock state integration

**Dependencies:** Stage 4 (AppLockService, VaultService), Stage 6 (navigation), Stage 2 (components).

**Risks:**
- Vault access control depends on AppLockService lock state
- Vault content must NOT appear in search, notifications, or home previews
- PIN verification must use the same security properties

**Recommended approach:**
1. Port Vault types + repository + service
2. Port VaultEntryRoute (checks lock state, shows locked/unlocked)
3. Port VaultLocked (PIN entry UI)
4. Port VaultHome (content grid)
5. Port AddVaultContent (create flow)
6. Port VaultContentViewer (display flow)
7. Port contentTypeMeta (visual treatment per type)
8. Port AppLockGate (full-app lock overlay)
9. Port SecuritySettingsScreen (enable/disable/change PIN)
10. Verify vault content is excluded from search
11. Verify vault content is excluded from notifications
12. Verify lock/unlock behavior

**What must remain unchanged:** All security properties (PBKDF2 parameters, constant-time comparison, Keystore storage), vault exclusion from search/notifications, lock state memory-only behavior.

**Verification requirements:**
- App lock enables/disables correctly
- PIN verification works with correct/incorrect PINs
- Lock state persists across app restarts (re-locks on cold start)
- Foreground re-lock works after timeout
- Vault requires unlock when app lock is enabled
- Vault content doesn't appear in search
- Vault content doesn't appear in notifications

**Completion criteria:** Vault and security features fully functional with correct access control.

**Next stage depends on:** Stage 4, Stage 6.

---

## Stage 11 — Yuki Companion Migration

**Objective:** Recreate the virtual companion cat system.

**Systems involved:** Yuki state model, service, screens, animations, accessories.

**What needs to be converted:**
- `yukiTypes.ts` (state model, needs, moods, actions, accessories, decay, XP)
- `yukiService.ts` (load/save, decay, action processing, leveling, streaks, accessories)
- YukiScreen, YukiCharacter, YukiActions
- yuki.css (activity animations)
- Accessory system (equip, unlock)

**Dependencies:** Stage 2 (components), Stage 6 (navigation).

**Risks:**
- Yuki uses localStorage (not SQLite) — migration to SQLite would improve consistency
- Animation system may differ between frameworks
- Accessory unlock logic is feature-specific

**Recommended approach:**
1. Port Yuki state model types
2. Port Yuki service (state management, decay, actions, leveling)
3. Port YukiScreen (main display)
4. Port YukiCharacter (visual rendering with activity animations)
5. Port YukiActions (action buttons + feedback)
6. Port yuki.css animations
7. Port accessory system (equip, unlock, display)
8. Consider migrating to SQLite for consistency (optional improvement)

**What must remain unchanged:** All need decay rates, XP calculations, level thresholds, accessory unlock conditions, mood computation formula, action effects.

**Verification requirements:**
- Yuki loads correctly from localStorage
- Time decay works correctly
- Each action changes needs + grants XP + triggers activity animation
- Leveling works with correct XP thresholds
- Streaks track correctly
- Accessories unlock at correct thresholds
- Accessory equip/display works

**Completion criteria:** Yuki companion fully functional with all interactions, animations, and progression.

**Next stage depends on:** Stage 2, Stage 6.

---

## Stage 12 — Games System Migration (Optional)

**Objective:** Migrate the legacy games system if desired.

**Systems involved:** Game engine, game screens, game content, progression.

**What needs to be converted:**
- GameEngine, GameService, gameProgression
- GamePlayScreen, GameResultsScreen, MemoryMatchScreen, WordScrambleScreen, CasualGamePlayScreen
- gameContent (questions, words, trivia)
- gamesPresentation helpers

**Dependencies:** Stage 2, Stage 4, Stage 6.

**Risks:**
- Games are legacy (Yuki replaced them as the primary engagement feature)
- The `/app/games` route redirects to `/app/yuki`
- Low priority unless the migration agent decides to keep them

**Recommended approach:**
- Evaluate whether games should be migrated or archived
- If migrated: port the engine, screens, and content
- If archived: move to Archive/ folder and remove routes

**What must remain unchanged:** Game mechanics, question content, scoring rules.

**Verification requirements (if migrated):**
- Games load and play correctly
- Results display correctly
- Game progression persists

**Completion criteria:** Games either migrated and functional, or archived with clear documentation.

**Next stage depends on:** Stage 6.

---

## Stage 13 — Settings & Utilities Migration

**Objective:** Recreate all settings screens and utility features.

**Systems involved:** Settings hub, all settings sub-screens, search, notification center, about, import.

**What needs to be converted:**
- SettingsHomeScreen (hub with navigation to sub-screens)
- ProfileSettingsScreen (edit profiles + photo picker)
- RelationshipSettingsScreen (edit relationship info)
- AppearanceSettingsScreen (theme + text size)
- NotificationSettingsScreen (enable/disable notifications)
- StorageSettingsScreen (storage report + cache clear + data reset)
- ImportScreen (JSON import flow)
- SearchScreen + SearchEngine integration
- NotificationCenter
- AboutScreen

**Dependencies:** Stage 2 (components), Stage 4 (services), Stage 6 (navigation).

**Risks:**
- StorageSettingsScreen triggers destructive reset (data wipe)
- ImportScreen depends on import service
- SearchScreen depends on per-feature search providers

**Recommended approach:**
1. Port SettingsHomeScreen hub
2. Port ProfileSettingsScreen (with photo picker integration)
3. Port RelationshipSettingsScreen
4. Port AppearanceSettingsScreen (theme + text size selectors)
5. Port NotificationSettingsScreen
6. Port StorageSettingsScreen (with destructive reset confirmation)
7. Port ImportScreen (JSON file picker + validation + import)
8. Port SearchScreen with all providers
9. Port NotificationCenter
10. Port AboutScreen

**What must remain unchanged:** All settings schema, all destructive action confirmations, import format compatibility.

**Verification requirements:**
- All settings save and apply correctly
- Theme toggle works
- Text size scaling works
- Storage report shows correct data
- Destructive reset works (with confirmation)
- Import works for notes and reminders
- Search returns correct results from all providers
- Notification center displays entries

**Completion criteria:** All settings and utility features fully functional.

**Next stage depends on:** Stage 4, Stage 8, Stage 9.

---

## Stage 14 — Integration Testing & Polish

**Objective:** Verify the complete application works end-to-end.

**Systems involved:** All.

**What needs to be converted:** Nothing — this stage is verification and polish.

**Dependencies:** All previous stages.

**Risks:**
- Integration issues between independently migrated features
- Missing toast feedback, empty states, or error handling

**Recommended approach:**
1. Run through every user flow from fresh install
2. Verify onboarding → home → every feature → settings
3. Verify all CRUD operations across all features
4. Verify all navigation paths
5. Verify dark mode across all screens
6. Verify text scaling across all screens
7. Verify reduced motion across all screens
8. Verify profile photos display correctly everywhere
9. Verify search results across all features
10. Verify notification scheduling and display
11. Verify vault access control
12. Verify data management (cache clear, full reset)
13. Verify import functionality
14. Run the full test suite
15. Fix any visual inconsistencies
16. Fix any interaction bugs
17. Add missing toast feedback
18. Add missing empty states
19. Add missing loading states

**What must remain unchanged:** All existing functionality.

**Verification requirements:**
- All 77 screens render correctly
- All user flows complete without errors
- All features maintain data correctly
- No regressions from V1 behavior
- All tests pass

**Completion criteria:** Complete application is functional, polished, and tested.

**Next stage depends on:** All previous stages.

---

## Stage 15 — Final Build & Release Preparation

**Objective:** Prepare the migrated application for release.

**Systems involved:** Build system, CI/CD, documentation, packaging.

**What needs to be converted:**
- Build pipeline (Vite/config → new build system)
- CI/CD workflow
- Capacitor configuration
- Android project setup
- APK build process
- Documentation updates

**Dependencies:** Stage 14.

**Risks:**
- Build configuration differences between old and new stacks
- Capacitor integration may need adjustment

**Recommended approach:**
1. Verify production build works
2. Configure Capacitor for the new project
3. Set up Android project
4. Verify APK builds successfully
5. Update GitHub Actions workflow
6. Update documentation
7. Run final test suite
8. Archive old code (Archive/ folder)
9. Final review

**What must remain unchanged:** App ID (`com.twohearts.app`), app name, offline-first behavior.

**Verification requirements:**
- Production build succeeds
- APK builds successfully
- CI/CD pipeline runs green
- Documentation is complete and accurate
- Archive folder contains old code with documentation

**Completion criteria:** Application is ready for release.

**Next stage depends on:** Stage 14.

---

# PART 11 — STAGE DEPENDENCIES

```
Stage 0 (Reconnaissance)
  │
  ├── Stage 1 (Design Tokens) ──┐
  │                              ├── Stage 2 (Components) ──┐
  │                              │                          │
Stage 3 (Data Layer) ───────────┤                          │
  │                              │                          │
  ├── Stage 4 (Core Services) ──┤                          │
  │                              │                          │
  │                              ├── Stage 5 (Onboarding) ─┤
  │                              │                          │
  │                              ├── Stage 6 (Navigation) ─┤
  │                              │                          │
  │                              ├── Stage 7 (Home/Us/More) │
  │                              │                          │
  │                              ├── Stage 8 (Notes/Memories/Timeline)
  │                              │                          │
  │                              ├── Stage 9 (Reminders/Places/Mood/Period)
  │                              │                          │
  │                              ├── Stage 10 (Vault/Security)
  │                              │                          │
  │                              ├── Stage 11 (Yuki)
  │                              │                          │
  │                              ├── Stage 12 (Games - Optional)
  │                              │                          │
  │                              └── Stage 13 (Settings/Utilities)
  │                                       │
  │                              Stage 14 (Integration Testing)
  │                                       │
  │                              Stage 15 (Build & Release)
```

**Critical path:** Stage 0 → Stage 1 → Stage 2 → Stage 3 → Stage 4 → Stage 6 → Stage 14 → Stage 15

**Parallelizable after Stage 6:**
- Stage 7, 8, 9, 10, 11, 12, 13 can be done in any order
- Stage 5 (onboarding) can be done in parallel with Stage 6 (navigation)

---

# PART 12 — MIGRATION ACCEPTANCE CRITERIA

The entire migration is considered complete when ALL of the following are true:

## Functionality
- [ ] Onboarding flow works from fresh install (all 6 steps)
- [ ] Home screen displays couple data correctly
- [ ] Us hub navigates to all relationship features
- [ ] Bottom navigation works with all 5 positions
- [ ] Notes CRUD works with all 7 categories
- [ ] Memories CRUD works with photo/video upload
- [ ] Timeline works with chapter system
- [ ] Reminders work with local notification scheduling
- [ ] Places work with photo storage
- [ ] Mood check-ins work with history and statistics
- [ ] Period tracker works with cycle calculations and calendar
- [ ] Vault works with PIN-protected access
- [ ] Yuki companion works with all actions, progression, and accessories
- [ ] Games work (if migrated) or are archived
- [ ] Search works across all features
- [ ] Notification center displays entries
- [ ] All settings save and apply correctly
- [ ] Import system works for notes and reminders
- [ ] Profile photos work (pick, process, store, display)
- [ ] Data management (cache clear, full reset) works
- [ ] App lock works (enable, disable, re-lock on foreground)

## Design
- [ ] All 77 screen references have implemented equivalents
- [ ] Burgundy brand identity is preserved
- [ ] Dark mode works across all screens
- [ ] Text scaling works across all screens
- [ ] Reduced motion works across all screens
- [ ] Rose-lily decorations are preserved
- [ ] Toast feedback works for all save/update/delete actions
- [ ] Empty states display correctly for all features
- [ ] Loading states display correctly

## Security
- [ ] PIN hashing uses PBKDF2-HMAC-SHA-256 (120k iterations)
- [ ] PIN storage uses platform-secure storage (Keystore/equivalent)
- [ ] Lock state is memory-only (re-locks on cold start)
- [ ] Vault content excluded from search, notifications, home
- [ ] No secrets or credentials in source code
- [ ] Logger redacts sensitive keys
- [ ] Error messages never expose internals

## Architecture
- [ ] React components never import plugins or storage directly
- [ ] Each plugin imported in exactly one driver file
- [ ] Services own all plugin call sites
- [ ] Repository layer provides domain objects (never raw rows)
- [ ] Entity conventions preserved (UUID v4, ISO 8601 UTC, tombstones)
- [ ] All 13 migrations work correctly

## Quality
- [ ] TypeScript clean (zero errors)
- [ ] All tests pass (adapted to new stack)
- [ ] Production build succeeds
- [ ] APK builds successfully (if Android target)
- [ ] No regressions from V1 behavior

---

# PART 13 — ARCHIVE POLICY

## What Should Be Archived

After migration replaces old systems, the following should be moved to the `Archive/` folder:

### Definitely Archive
- Old SVG assets that are replaced by new asset pipeline
- Old screen implementations that are completely rewritten
- Legacy game system code (if games are not migrated)
- Old CSS files (primitives.css, global.css) after new styling system is in place
- Old component implementations after new component library is complete
- Old repository implementations after new data layer is complete
- Old service implementations after new services are complete

### Preserve as Reference
- All `docs/` files — historical records of development decisions
- `MasterPrompt.txt` and `TwoHeartsRDMap.txt` — original specifications
- `TWOHEARTS-MASTER-AUTONOMY-AND-FINAL-PRODUCT-REBUILD-DIRECTIVE.txt` — development directive
- All `STAGE-*-*.md` reports — development history
- This migration document itself

### Do NOT Archive (Yet)
- Brand SVG assets until the migration agent has decided whether to use them or replace them
- Test files that are still relevant to the active application
- Configuration files that are still used by the active application
- Any file that is still imported by active code in the current repository

**Important:** The future migration agent has full authority to archive brand assets (branding/, decorations/), legacy UI assets, and old icon sets if the agent has replaced them with new native Android assets. Do not archive files that are still referenced by the active V1 application, but once the native application no longer depends on them, they may be moved to Archive/ as historical reference.

## Archive Structure

```
Archive/
├── README.md                          # What was archived and why
├── src/
│   ├── components/                    # Old component implementations
│   ├── features/                      # Old feature implementations
│   ├── services/                      # Old service implementations
│   ├── repositories/                  # Old repository implementations
│   ├── data/                          # Old data models
│   └── styles/                        # Old CSS files
├── tests/                             # Old test files
└── docs/                              # Historical documentation
```

## Archive Rules

1. **Verify before archiving**: Ensure the archived code is no longer imported by any active code
2. **Document why**: Each archived file/folder must have a note explaining why it was archived
3. **Keep accessible**: Archived code should be readable for reference but not runnable
4. **No duplicates**: Do not archive files that are still in use
5. **Mark for review**: If uncertain whether a file is still needed, mark it for review instead of archiving

---

# APPENDIX A — COMPLETE FILE INDEX

## Source Files by Category

### Entry & Configuration
- `src/main.tsx` — Application entry point with bootstrap gate
- `src/App.tsx` — Root component (providers + router)
- `src/vite-env.d.ts` — Vite type declarations
- `src/config/appInfo.ts` — App identity (name, version, appId)
- `src/config/persistence.ts` — Database name, schema version, media root

### Core
- `src/core/AppRootProvider.tsx` — Foundation provider (text-size, theme, lifecycle)
- `src/core/ErrorBoundary.tsx` — Global error boundary
- `src/core/appSettings.ts` — Persistent settings store (localStorage)
- `src/core/uiState.ts` — Ephemeral UI state
- `src/core/useAppLifecycle.ts` — Capacitor app lifecycle hook
- `src/core/index.ts` — Barrel export

### Theme
- `src/theme/tokens.css` — Design tokens (CSS custom properties)
- `src/theme/tokens.ts` — Typed token mirror (JS constants)
- `src/theme/components.ts` — Component class name strings
- `src/theme/index.ts` — Barrel export

### Styles
- `src/styles/global.css` — Global styles (reset, typography, utilities)

### Components (19)
- `src/components/BrandLogo.tsx` — Brand mark/full logo
- `src/components/Button.tsx` — Button variants
- `src/components/Card.tsx` — Card container
- `src/components/ConfirmDialog.tsx` — Confirmation dialog
- `src/components/DatePicker.tsx` — Date picker
- `src/components/Divider.tsx` — Visual divider
- `src/components/EmptyState.tsx` — Empty feature state
- `src/components/Header.tsx` — Screen header
- `src/components/Icon.tsx` — 30+ SVG icons
- `src/components/IconButton.tsx` — Icon button
- `src/components/Input.tsx` — Text input
- `src/components/LoadingState.tsx` — Loading spinner
- `src/components/Modal.tsx` — Dialog/bottom sheet
- `src/components/ProfileAvatar.tsx` — Profile photo display
- `src/components/Screen.tsx` — Screen wrapper
- `src/components/StatusBanner.tsx` — Status messages
- `src/components/Switch.tsx` — Toggle switch
- `src/components/TimePicker.tsx` — Time picker
- `src/components/decorations.tsx` — Rose-lily + onboarding art
- `src/components/toast.tsx` — Toast notification system
- `src/components/index.ts` — Barrel export
- `src/components/primitives.css` — All component styles (10,047 lines)

### Navigation
- `src/navigation/AppRouter.tsx` — Route definitions + router
- `src/navigation/routes.ts` — RoutePath constants + ONBOARDING_STEPS
- `src/navigation/index.ts` — Barrel export

### Data Layer
- `src/data/model/entity.ts` — Entity, TombstonedEntity, NewEntity
- `src/data/serialization/entitySerializer.ts` — Serializer interface + helpers
- `src/data/database/adapter.ts` — DatabaseAdapter interface
- `src/data/database/connection.ts` — Database initialization + singleton
- `src/data/database/errors.ts` — PersistenceError
- `src/data/database/sqlJsAdapter.ts` — Web/test SQLite adapter
- `src/data/database/capacitorSqliteAdapter.ts` — Native SQLite adapter
- `src/data/database/migrations/` — 13 migration files + types + runner + index
- `src/data/media/` — MediaStorage, MediaFileSystem, CapacitorFileSystem, MemoryFileSystem, resolveMediaFileSystem
- `src/data/settings/settingsStorage.ts` — localStorage abstraction
- `src/data/relationship/relationshipTypes.ts` — Profile, CoupleRelationship, ImportantDate
- `src/data/memory/memoryTypes.ts` — Memory, MemoryMedia
- `src/data/note/noteTypes.ts` — Note, NoteCategory
- `src/data/timeline/timelineTypes.ts` — TimelineEvent
- `src/data/reminder/reminderTypes.ts` — Reminder, recurrence, status
- `src/data/place/placeTypes.ts` — Place
- `src/data/mood/moodTypes.ts` — MoodEntry, MoodValue
- `src/data/period/periodTypes.ts` — PeriodEntry, PeriodSettings, FlowLevel
- `src/data/vault/vaultTypes.ts` — VaultItem, VaultContentType
- `src/data/game/yukiTypes.ts` — YukiState, needs, moods, actions, accessories
- `src/data/game/gameTypes.ts` — Game types (legacy)
- `src/data/notification/notificationCenterTypes.ts` — NotificationCenterEntry

### Repositories (14)
- `src/repositories/repository.ts` — BaseRepository<T> + Repository<T> interface
- `src/repositories/profileRepository.ts`
- `src/repositories/coupleRepository.ts` (singleton: get/save only)
- `src/repositories/importantDateRepository.ts`
- `src/repositories/memoryRepository.ts`
- `src/repositories/noteRepository.ts`
- `src/repositories/timelineRepository.ts`
- `src/repositories/reminderRepository.ts`
- `src/repositories/placeRepository.ts`
- `src/repositories/moodRepository.ts`
- `src/repositories/periodRepository.ts`
- `src/repositories/vaultRepository.ts`
- `src/repositories/mediaAssetRepository.ts`
- `src/repositories/notificationCenterRepository.ts`

### Services (25+)
- `src/services/bootstrap/appBootstrap.ts` — Initialization pipeline + coreServices
- `src/services/errors/appError.ts` — AppError taxonomy
- `src/services/logging/logger.ts` — Leveled logger with redaction
- `src/services/validation/validators.ts` — Pure validators
- `src/services/datetime/datetime.ts` — Date/time helpers
- `src/services/device/deviceCapabilities.ts` — Device capability matrix
- `src/services/permissions/permissionService.ts` — Permission state machine
- `src/services/lifecycle/appLifecycleService.ts` — App lifecycle events
- `src/services/files/fileService.ts` — Generic file utilities
- `src/services/files/fileAdapters.ts` — File system adapters
- `src/services/media/mediaUtils.ts` — Media validation + sniffing
- `src/services/search/normalize.ts` — Query normalization
- `src/services/search/searchEngine.ts` — Search engine + providers
- `src/services/search/memorySearchProvider.ts`
- `src/services/search/noteSearchProvider.ts`
- `src/services/search/placeSearchProvider.ts`
- `src/services/search/reminderSearchProvider.ts`
- `src/services/search/timelineSearchProvider.ts`
- `src/services/security/secureStore.ts` — SecureStore interface
- `src/services/security/capacitorSecureStore.ts` — Keystore driver
- `src/services/security/pinHash.ts` — PBKDF2 PIN hashing
- `src/services/security/appLockService.ts` — App lock state machine
- `src/services/notifications/notificationService.ts` — Notification scheduling
- `src/services/notifications/capacitorNotificationDriver.ts` — Native driver
- `src/services/notifications/memoryNotificationDriver.ts` — Test driver
- `src/services/notifications/notificationRegistryRepository.ts` — Registry
- `src/services/notification-center/notificationCenterService.ts` — Center service
- `src/services/memory/memoryService.ts` — Memory CRUD + media coordination
- `src/services/note/noteService.ts` — Note CRUD
- `src/services/timeline/timelineService.ts` — Timeline CRUD
- `src/services/reminder/reminderService.ts` — Reminder CRUD + notification
- `src/services/place/placeService.ts` — Place CRUD + media
- `src/services/mood/moodService.ts` — Mood CRUD
- `src/services/period/periodService.ts` — Period CRUD + calculations
- `src/services/vault/vaultService.ts` — Vault CRUD + access control
- `src/services/relationship/relationshipService.ts` — Profile + couple management
- `src/services/state/appStateService.ts` — App state + onboarding stage
- `src/services/profile/profilePhotoService.ts` — Photo lifecycle
- `src/services/game/yukiService.ts` — Yuki state management
- `src/services/game/gameService.ts` — Game engine (legacy)
- `src/services/game/gameEngine.ts` — Game mechanics (legacy)
- `src/services/game/gameProgression.ts` — Game progress (legacy)
- `src/services/import/importService.ts` — JSON import
- `src/services/backup/exportFormat.ts` — Export envelope format
- `src/services/maintenance/dataManagementService.ts` — Storage report + reset

### Utils
- `src/utils/ids.ts` — UUID v4 generation + validation
- `src/utils/time.ts` — ISO timestamp helpers + date key utilities
- `src/utils/base64.ts` — Base64 encode/decode

### Customization
- `src/customization/defaults/ownerDefaults.ts` — Default owner data
- `src/customization/theme/ownerTheme.ts` — Owner theme overrides
- `src/customization/games/gameContent.ts` — Game question/word content
- `src/customization/index.ts` — Barrel export

### Assets
- `src/assets/branding/` — 3 SVGs (logo, mark, app-icon)
- `src/assets/decorations/` — 20 rose-lily SVGs
- `src/assets/images/` — 1 onboarding welcome photo SVG
- `src/assets/yuki/` — 1 cat SVG
- `src/assets/archive/` — Legacy asset archive

## Test Files (55)

All in `tests/` directory, using Node's `--experimental-transform-types` test runner on real sql.js.

---

# APPENDIX B — EXISTING DOCUMENTATION INDEX

| File | Contents |
|------|----------|
| `docs/design-system.md` | Design token architecture, branding, motion, icons |
| `docs/persistence.md` | Phase 2 database architecture decisions |
| `docs/core-services.md` | Phase 3 service architecture documentation |
| `docs/screens.md` | 77-screen reference mapping with status |
| `docs/app-shell.md` | Phase 24 navigation architecture |
| `docs/memories.md` | Phase 7 Memories feature documentation |
| `docs/onboarding.md` | Phase 5 onboarding documentation |
| `docs/relationship-state.md` | Phase 4 relationship architecture |
| `docs/settings.md` | Phase 19 settings documentation |
| `docs/phase-22-release.md` | Phase 22 release report |
| `docs/phase26-screen-audit.md` | Phase 26 screen audit |
| `docs/phase31-ux-consistency-audit.md` | Phase 31 UX consistency |
| `docs/phase32-performance-accessibility-audit.md` | Phase 32 performance/accessibility |
| `docs/phase33-final-visual-qa.md` | Phase 33 final visual QA |
| `docs/FINAL-77-SCREEN-VISUAL-STATUS.md` | Final 77-screen status |
| `docs/FINAL-TWOHEARTS-ACCEPTANCE-REPORT.md` | Final acceptance report |
| `docs/FINAL-V1-VISUAL-PRODUCTIZATION-REPORT.md` | Final visual productization |
| `docs/STAGE-1 through STAGE-24` | 21 stage visual productization reports |
| `MasterPrompt.txt` | 79-section implementation/design rules |
| `TwoHeartsRDMap.txt` | Feature architecture & V1 scope |
| `TWOHEARTS-MASTER-AUTONOMY-AND-FINAL-PRODUCT-REBUILD-DIRECTIVE.txt` | Development directive |
| `TWOHEARTS-VISUAL-PRODUCTIZATION-DIRECTIVE.txt` | Visual productization directive |
| `TwoHearts-Post-V1-UI-UX_Experience-Ovehaul-RoadMap.txt` | Post-V1 roadmap |
| `TWOHEARTS_CUSTOMIZATION_GUIDE.md` | Customization guide |
| `AGENTS.md` | Agent instructions |
| `README.md` | Project readme |

---

# APPENDIX C — VERIFICATION CHECKLIST

Before marking any migration stage as complete, verify:

1. **TypeScript compiles cleanly** (`tsc -b --noEmit`)
2. **All tests pass** (adapted to new stack)
3. **No secrets or credentials** in the diff
4. **No accidental generated files** staged
5. **All imports resolve** (no broken imports)
6. **All routes navigate** correctly
7. **All CRUD operations** work end-to-end
8. **All toast feedback** fires on save/update/delete
9. **Empty states** display for all features
10. **Loading states** display during async operations
11. **Dark mode** renders correctly
12. **Text scaling** works correctly
13. **Reduced motion** works correctly
14. **Profile photos** display correctly
15. **Brand assets** render correctly
16. **Bottom navigation** works with all 5 positions
17. **Back button** behavior is correct
18. **Search** returns correct results
19. **Notifications** schedule correctly
20. **Vault access control** works correctly

---

*Document generated from comprehensive repository audit. All information is based on actual source code inspection, not assumptions.*
