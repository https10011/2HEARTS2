# TwoHearts Archive

This archive preserves the **complete legacy React/Vite/Capacitor TwoHearts
implementation** and all associated historical documentation for reference
during the native Android migration.

---

## Purpose

The archive exists so that:

- **Nothing is lost.** Every file from the completed V1 implementation is
  preserved in Git history and accessible here.
- **Future agents can reference it.** When building the native Android
  application, agents can inspect archived code to understand how features
  worked, what data models were used, how persistence was handled, and what
  design decisions were made.
- **The active repository stays clean.** The root of the repository now
  contains only the native Android project and migration documentation.

---

## ⚠️ Important Rules

1. **Do NOT treat archived files as the active application.**
   The active TwoHearts codebase is the **native Kotlin/Android/Jetpack**
   project at the repository root. The archived materials are reference only.

2. **Do NOT delete archived files.** Archiving is permanent preservation,
   not disposal.

3. **Do NOT modify archived source code.** Archived files are preserved
   exactly as they were at the time of archiving.

4. **Do NOT import from the archive in active code.** The archive is
   read-only reference material.

5. **Future migration agents may consult the archive** when they need
   to understand how the legacy implementation worked.

---

## Archive Structure

```
Archive/
├── README.md                                  ← This file
└── Legacy-React-Vite-Capacitor/
    ├── src/                                   ← Complete React source code (~265 files)
    │   ├── main.tsx                           ← App entry point
    │   ├── App.tsx                            ← Root component
    │   ├── components/                        ← 19 shared UI components + primitives.css
    │   ├── features/                          ← All feature modules
    │   │   ├── app-shell/                     ← App shell, bottom nav, screens
    │   │   ├── games/                         ← Game screens
    │   │   ├── memories/                      ← Memories feature
    │   │   ├── mood/                          ← Mood feature
    │   │   ├── notes/                         ← Notes feature
    │   │   ├── notifications/                 ← Notification center
    │   │   ├── onboarding/                    ← Onboarding flow
    │   │   ├── period/                        ← Period tracker
    │   │   ├── permissions/                   ← Permission prompts
    │   │   ├── places/                        ← Places feature
    │   │   ├── reminders/                     ← Reminders feature
    │   │   ├── settings/                      ← Settings screens
    │   │   ├── timeline/                      ← Timeline feature
    │   │   ├── vault/                         ← Private vault
    │   │   └── yuki/                          ← Yuki companion cat
    │   ├── services/                          ← 25+ service modules
    │   ├── repositories/                      ← 14 repository classes
    │   ├── data/                              ← Database, models, serialization, media
    │   ├── navigation/                        ← App router + routes
    │   ├── theme/                             ← Design tokens (CSS + TypeScript)
    │   ├── styles/                            ← Global CSS
    │   ├── customization/                     ← Owner customization (theme, games, defaults)
    │   ├── assets/                            ← SVG assets (branding, decorations, images)
    │   ├── config/                            ← App config + persistence config
    │   ├── core/                              ← App root provider, error boundary, settings
    │   └── utils/                             ← Shared utilities
    ├── tests/                                 ← 56 test files (948+ tests)
    ├── package.json                           ← npm dependencies
    ├── package-lock.json                      ← npm lockfile
    ├── index.html                             ← Vite entry point
    ├── vite.config.ts                         ← Vite configuration
    ├── capacitor.config.ts                    ← Capacitor configuration
    ├── tsconfig.json                          ← TypeScript root config
    ├── tsconfig.app.json                      ← TypeScript app config
    ├── tsconfig.node.json                     ← TypeScript node config
    ├── android/                               ← Capacitor Android project
    ├── scripts/                               ← Design asset scripts
    ├── AGENTS.md                              ← Agent memory (legacy context)
    ├── README.md                              ← Legacy project README
    ├── .github-workflows/                     ← Legacy GitHub Actions workflow
    │   └── build-android.yml                  ← Capacitor APK build pipeline
    ├── Architecture-Docs/                     ← 14 architecture documentation files
    ├── Stage-Reports/                         ← 21 visual productization reports
    ├── Final-Reports/                         ← 3 final completion reports
    ├── Directive-Guidelines/                  ← 10 stage development directives
    ├── Directives/                            ← 5 original build/roadmap .txt files
    ├── Guides/                                ← Owner customization guide
    ├── Legacy-Vectors/                        ← 21 SVG source files
    │   ├── Rose Lily Vectors/                 ← 20 rose-lily SVGs
    │   └── TwoHearts-Logo-BrandName/          ← Logo SVG
    ├── Screen-References/                     ← 77 approved PNG visual references
    └── Legacy-Archived-Assets-README.md       ← Original src/assets/archive notes
```

---

## What Each Category Contains

### src/
The complete React/Vite/Capacitor application source code. ~265 TypeScript/TSX
files implementing all 77 screens across 15 feature modules. This is the
primary reference for understanding how TwoHearts V1 worked.

### tests/
56 test files with 948+ tests covering services, repositories, data layer,
migrations, and feature integration. Uses Node's test runner on real sql.js.

### Package & Build Files
`package.json`, `package-lock.json`, `index.html`, `vite.config.ts`,
`capacitor.config.ts`, `tsconfig*.json` — the complete web build toolchain.

### android/
The Capacitor Android project (Gradle build, manifest, native plugins).

### .github-workflows/
The legacy GitHub Actions workflow that built the Capacitor APK (Node →
Vite build → Capacitor sync → Gradle → APK).

### Architecture-Docs/
Detailed architecture documentation: persistence, core services, navigation,
memories, settings, onboarding, design system, 77-screen mapping.

### Stage-Reports/
21 stage-by-stage visual productization reports documenting the progressive
UI/UX rebuild of TwoHearts.

### Final-Reports/
Authoritative final status: 77-screen visual status, acceptance report,
V1 visual productization completion.

### Directive-Guidelines/
10 stage development directives (STAGE-00 through STAGE-09) with
problems found and solutions applied.

### Directives/
Original authoritative build documents: MasterPrompt.txt (79 sections),
TwoHeartsRDMap.txt (roadmap), and other specification files.

### Screen-References/
77 approved PNG visual reference images (1080×2400 portrait) that served
as the design foundation for the V1 implementation.

### Legacy-Vectors/
Source SVG files: 20 rose-lily decorative vectors and the TwoHearts logo.

---

## Legacy Implementation Summary

| Component | Description |
|-----------|------------|
| **Language** | TypeScript (strict mode) |
| **UI Framework** | React 18 |
| **Build Tool** | Vite 5.4 |
| **Mobile Shell** | Capacitor 6.2 |
| **Database** | SQLite (13 migrations, schema v13) |
| **Tests** | 948+ tests, 56 test files |
| **Screens** | 77 approved references, 67 implemented |
| **CSS** | 10,700+ lines custom CSS |
| **Assets** | 25 SVGs, 77 PNG references |
| **Features** | Onboarding, Home, Memories, Notes, Timeline, Games, Reminders, Places, Mood, Period Tracker, Vault, Yuki, Search, Notifications, Settings |

---

## Migration Directory

The `Migration/` directory at the repository root is **NOT** part of
this archive. It is the dedicated location for the native Android
migration planning and documentation. See
`Migration/TWOHEARTS-MASTER-AUDIT-AND-MIGRATION-ROADMAP.md` for the
comprehensive migration roadmap.
