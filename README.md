# TwoHearts V1

A private, offline-first, local-first couples application for Android, built with
**React + TypeScript + Capacitor**. The app runs entirely on-device — no backend,
no cloud database, no mandatory internet connection (V1 architectural boundary).

This repository contains the **Phase 1 engineering foundation**, **Phase 2
local-first persistence foundation**, **Phase 3 core services**, **Phase 4
relationship & application state foundation**,and **Phase 5 onboarding & app entry experience**, and **Phase 6 main app shell
& navigation**. Feature screens (memories, notes, games, vault, period tracker,
etc.) are added in later phases.

## Technology stack

| Layer | Choice | Why |
|---|---|---|
| Language | TypeScript | Type safety, MasterPrompt §2 |
| UI framework | React 18 | MasterPrompt §2 required stack |
| Build tool | Vite | Fast, produces a static bundle bundled into the APK (offline-first) |
| Native runtime | Capacitor 6 | Android native bridge (MasterPrompt §10) |
| Native plugins | `@capacitor/app`, `@capacitor/status-bar`, `@capacitor/filesystem`, `@capacitor-community/sqlite` | Back button + lifecycle; status bar; private media files; native SQLite — minimal, justified |
| Routing | React Router 6 | Onboarding / unlocked / feature / modal navigation |
| State | `useSyncExternalStore` (no external lib) | Avoids dependency bloat (MasterPrompt §14) |
| Persistence | SQLite (`@capacitor-community/sqlite` native, `sql.js` in browser/tests); `localStorage` for settings only | Relational domain data, transactions, migrations — see `docs/persistence.md` |
| CI | GitHub Actions | Reproducible APK generation (MasterPrompt §61) |

No Firebase, Supabase, cloud database, remote auth, FCM, or cloud storage is
introduced — these are explicitly prohibited for V1.

## Project structure

```
TwoHeartsRDMap.txt                 # Authoritative roadmap (do not modify)
MasterPrompt.txt                   # Authoritative build rules (do not modify)
TwoHeart UI Reference Screens/     # 77 approved visual references (do not modify)
src/
  components/        # Reusable UI primitives (Button, Card, Screen, Modal, Icon…)
  core/              # App root, error boundary, settings/UI state, lifecycle hook
  navigation/        # Routing foundation (onboarding/app/feature/modal)
  theme/             # Design tokens (burgundy/rose/neutral) — CSS + typed mirror
  styles/            # Global mobile-first CSS
  customization/     # ONE place for owner customization (branding/theme/defaults)
  features/          # Feature screens (added in Phase 6+ — empty now by design)
  data/              # Persistence: database/, model/, serialization/, media/, settings/
                     # + relationship/ (Phase 4 domain types)
  repositories/      # Domain repositories (BaseRepository + media + Phase 4:
                     # profiles/couple/important-dates)
  services/          # Core services (Phase 3): bootstrap/ errors/ logging/ validation/
                     # datetime/ device/ permissions/ lifecycle/ files/ media/ search/
                     # security/ notifications/ (+ backup/ export envelope)
                     # + Phase 4: relationship/ (domain boundary) state/ (setup state)
  hooks/ utils/ config/  # Shared hooks, utils (ids/time/base64), persistence config
  assets/            # Replaceable SVG logo/icon/illustration assets
android/             # Capacitor Android project (Gradle → APK)
.github/workflows/   # APK build workflow
docs/                # Project documentation
```

## Development

```bash
npm install          # install dependencies
npm run dev          # start Vite dev server (http://localhost:5173)
npm run build        # TypeScript check + production build → dist/
npm run typecheck    # TypeScript only
npm test             # Persistence unit tests (sql.js in Node, no mocks)
```

## Android / Capacitor build

The production web bundle (`dist/`) is synced into the Android project and bundled
into the APK — there is no hosted website; the app runs offline.

```bash
# Prerequisites: JDK 17+ (21 used here) and Android SDK (platform 34).
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk

npm run build
npx cap sync android            # copy web assets + update plugins
cd android
./gradlew assembleDebug        # build APK → app/build/outputs/apk/debug/
```

`android/local.properties` (git-ignored) points Gradle at the local SDK; it is
regenerated per environment from `ANDROID_HOME`.

## Building an APK via GitHub Actions (owner flow)

MasterPrompt §62: the owner can produce an installable APK without any backend or
hosting.

1. Modify TwoHearts locally (photos, colors, game questions, etc.).
2. Commit and push changes to `master` on GitHub.
3. Open the repository on GitHub → **Actions** tab.
4. Select **Build Android APK**.
5. Click **Run workflow** (also runs automatically on push to `master`).
6. Wait for the run to complete.
7. Open the completed run → download the `twohearts-debug-apk` artifact.
8. Install the `.apk` on an Android device.

No web hosting. No backend. No proprietary deployment platform.

## Customization

See **`TWOHEARTS_CUSTOMIZATION_GUIDE.md`** for how to change the logo, colors,
fonts, app name, and feature defaults without touching feature logic.

## Persistence architecture

See **`docs/persistence.md`** for the Phase 2 database decision (SQLite +
sql.js dev adapter), layers, schema versioning/migrations, settings vs domain
boundary, media storage, serialization, error handling, backup/export
boundary, and the future V2 sync seam.

See **`docs/core-services.md`** for the Phase 3 core services: bootstrap
pipeline, error taxonomy, redacted logging, validators, datetime helpers,
device capability matrix, permission service, lifecycle bus, file/media
utilities, search engine, secure storage + app lock foundation, and the
local notification architecture.

See **`docs/relationship-state.md`** for the Phase 4 relationship and
application-state foundation: profiles (owner/partner), the singleton couple
relationship, important dates with recurrence, first-launch/onboarding state,
preferences (text size + theme mode), and the setup-completion gate.

See **`docs/onboarding.md`** for the Phase 5 onboarding and app entry experience:
splash, welcome, profile setup, relationship setup, personalization, optional
app-lock, setup completion, returning-user behavior, routing architecture, and
testing status.

See **`docs/app-shell.md`** for the Phase 6 main app shell and navigation:
bottom tab navigation, home dashboard, hub screens (Us, Games, Notes, More),
feature placeholder routes, back-button behavior, and testing status.

## Authoritative specifications

- `MasterPrompt.txt` — implementation/design rules (do not modify).
- `TwoHeartsRDMap.txt` — feature architecture & V1 scope (do not modify).

## V1 / V2 boundary

V1 is offline-first and local-first. Online synchronization, two-person
communication, remote notifications, and cloud features belong to a future V2
and must remain architecturally separable from V1.
