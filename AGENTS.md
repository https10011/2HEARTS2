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
- `features/` — empty by design; populated Phase 6+.
- `data/` — persistence (Phase 2): `database/` (adapter + connection +
  migrations @ schema v1), `model/entity.ts` (UUID v4 ids, ISO UTC
  createdAt/updatedAt, optional deletedAt tombstone), `serialization/`
  (one EntitySerializer per entity), `media/` (MediaFileSystem adapters +
  MediaStorage service; safe refs; orphan sweep), `settings/settingsStorage.ts`
  (localStorage abstraction; also drives `core/appSettings.ts`).
- `repositories/` — BaseRepository CRUD conventions + MediaAssetRepository.
- `services/backup/` — versioned export envelope + validation (no UI yet).
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
- Phase 2: Local-first data & persistence foundation — COMPLETE
  (branch `phase-2-local-first-persistence`; docs/persistence.md)
- Phase 3+: features (onboarding, memories, notes, …) — NOT STARTED

## Git
- user.name=openhands, user.email=openhands@all-hands.dev (local config).
- Shallow clone; unshallow if full history needed.
- `.gitignore` covers node_modules/, dist/, android/app/build/, .gradle/, local.properties.
