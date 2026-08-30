# TwoHearts Archive

This archive preserves the **complete legacy React/Vite/Capacitor TwoHearts
implementation** and all associated historical documentation for reference
during the future native Android migration.

---

## Purpose

The archive exists so that:

- **Nothing is lost.** Every file from the completed V1 implementation is
  preserved in Git history and accessible here.
- **Future agents can reference it.** When migrating to native Android,
  agents can inspect archived code to understand how features worked, what
  data models were used, how persistence was handled, and what design
  decisions were made.
- **The active repository stays clean.** The root of the repository now
  contains only what is needed for the active build workflow and the
  future migration.

---

## ⚠️ Important Rules

1. **Do NOT treat archived files as the active application.**
   The active TwoHearts codebase lives at the repository root (`src/`,
   `tests/`, `package.json`, etc.). The archived materials are reference
   only.

2. **Do NOT delete archived files.** Archiving is permanent preservation,
   not disposal. If something is in the archive, it stays there.

3. **Do NOT modify archived source code.** Archived files are preserved
   exactly as they were at the time of archiving. Do not rewrite,
   refactor, or cosmetically alter them.

4. **Do NOT import from the archive in active code.** The archive is
   read-only reference material.

5. **Future migration agents may consult the archive** when they need
   to understand how the legacy implementation worked.

---

## Archive Structure

```
Archive/
├── README.md                                  ← This file
├── Legacy-React-Vite-Capacitor/
│   ├── Stage-Reports/                         ← Visual productization stage reports
│   │   ├── STAGE-1-VISUAL-PRODUCTIZATION.md
│   │   ├── STAGE-2-VISUAL-PRODUCTIZATION.md
│   │   ├── ...
│   │   └── STAGE-24-FINAL-GIT-CHECKPOINT.md
│   ├── Architecture-Docs/                     ← Architecture & feature documentation
│   │   ├── app-shell.md                       ← Phase 24 navigation architecture
│   │   ├── core-services.md                   ← Phase 3 core services
│   │   ├── design-system.md                   ← Design token documentation
│   │   ├── memories.md                        ← Phase 7 memories feature
│   │   ├── onboarding.md                      ← Phase 5 onboarding
│   │   ├── persistence.md                     ← Phase 2 database architecture
│   │   ├── relationship-state.md              ← Phase 4 relationship foundation
│   │   ├── screens.md                         ← 77-screen reference mapping
│   │   ├── settings.md                        ← Phase 19 settings architecture
│   │   ├── phase-22-release.md                ← V1 release readiness
│   │   ├── phase26-screen-audit.md            ← Visual experience overhaul
│   │   ├── phase31-ux-consistency-audit.md    ← UX consistency audit
│   │   ├── phase32-performance-accessibility-audit.md
│   │   └── phase33-final-visual-qa.md
│   ├── Final-Reports/                         ← Final completion reports
│   │   ├── FINAL-77-SCREEN-VISUAL-STATUS.md   ← Authoritative 77-screen status
│   │   ├── FINAL-TWOHEARTS-ACCEPTANCE-REPORT.md
│   │   └── FINAL-V1-VISUAL-PRODUCTIZATION-REPORT.md
│   ├── Directive-Guidelines/                  ← Stage-by-stage development directives
│   │   ├── STAGE-00-MASTER-RECONNAISSANCE.md
│   │   ├── STAGE-01-FIRST-LAUNCH-ONBOARDING-REPAIR.md
│   │   ├── ...
│   │   └── STAGE-09-COMPLETE-VISUAL-PRODUCTIZATION.md
│   ├── Screen-References/                     ← 77 approved PNG visual references
│   │   ├── 01-SplashScreen.png
│   │   ├── 02-Welcome-FirstLaunch.png
│   │   ├── ...
│   │   └── 77-VaultContentView.png
│   ├── Directives/                            ← Original build/roadmap directives
│   │   ├── MasterPrompt.txt                   ← Original master build prompt (79 sections)
│   │   ├── TwoHeartsRDMap.txt                 ← Authoritative V1 roadmap
│   │   ├── TWOHEARTS-MASTER-AUTONOMY-AND-FINAL-PRODUCT-REBUILD-DIRECTIVE.txt
│   │   ├── TWOHEARTS-VISUAL-PRODUCTIZATION-DIRECTIVE.txt
│   │   └── TwoHearts-Post-V1-UI-UX_Experience-Ovehaul-RoadMap.txt
│   ├── Guides/                                ← Legacy customization documentation
│   │   └── TWOHEARTS_CUSTOMIZATION_GUIDE.md
│   ├── Legacy-Vectors/                        ← Original SVG source vectors
│   │   ├── Rose Lily Vectors/                 ← 20 rose-lily SVG source files
│   │   └── TwoHearts-Logo-BrandName/          ← Logo/brand name SVG source
│   └── Legacy-Archived-Assets-README.md       ← Original src/assets/archive/ notes
```

---

## What Each Category Contains

### Stage-Reports/
The 21 stage-by-stage visual productization reports documenting the
progressive UI/UX rebuild of TwoHearts through Stages 1–22 (with
Stage 24 as the final git checkpoint). These are historical records
of what was done at each stage.

### Architecture-Docs/
Detailed architecture documentation for each major system: database
persistence, core services, relationship state, onboarding, app shell
navigation, memories, settings, design system, and the 77-screen
reference mapping. These contain the technical decisions and patterns
used in the legacy implementation.

### Final-Reports/
The authoritative final status reports: 77-screen visual status,
acceptance report, and V1 visual productization completion report.

### Directive-Guidelines/
The 10 stage-by-stage development directives (STAGE-00 through
STAGE-09) that guided the Freebuff platform's implementation work.
These document what was planned, what problems were found, and how
they were resolved.

### Screen-References/
The 77 approved PNG visual reference images (1080×2400 portrait)
used as the visual foundation for the TwoHearts V1 implementation.
These are the original design targets that the implementation
was built against.

### Directives/
The original authoritative build documents:
- **MasterPrompt.txt** — The 79-section master build prompt defining
  every requirement for TwoHearts V1.
- **TwoHeartsRDMap.txt** — The authoritative V1 feature roadmap with
  screen numbering and implementation status.
- **TWOHEARTS-MASTER-AUTONOMY-AND-FINAL-PRODUCT-REBUILD-DIRECTIVE.txt**
  — The master autonomy directive for the final rebuild cycle.
- **TWOHEARTS-VISUAL-PRODUCTIZATION-DIRECTIVE.txt** — The visual
  productization directive.
- **TwoHearts-Post-V1-UI-UX_Experience-Ovehaul-RoadMap.txt** — The
  post-V1 UI/UX overhaul roadmap.

### Guides/
The legacy owner customization guide explaining how to modify the
React/Vite/Capacitor application (logos, colors, game content, etc.).

---

## Legacy Implementation Summary

The archived React/Vite/Capacitor implementation consisted of:

| Component | Description |
|-----------|------------|
| **Source** | ~265 TypeScript/TSX files across `src/` (components, features, services, repositories, data, navigation, theme, customization) |
| **Tests** | 55 test files with 948+ tests (in `tests/` at repository root) |
| **Assets** | 25 SVG assets (branding, decorations, images, Yuki character) |
| **CSS** | 10,700+ lines of custom CSS (design tokens, primitives, global styles) |
| **Build** | Vite → dist/ → Capacitor sync → Android APK via Gradle |
| **Database** | SQLite with 13 migration versions (schema v13) |
| **Features** | 77 screens: onboarding, home, memories, notes, timeline, games (6 couple + 4 casual), reminders, places, mood, period tracker, vault, Yuki companion, search, notification center, settings |
| **CI** | GitHub Actions workflow for Android APK build |

The active `src/`, `tests/`, `package.json`, `index.html`, build
configs (Vite, TypeScript, Capacitor), and the GitHub Actions workflow
remain at the repository root because they are required by the build
system. They constitute the **active legacy codebase** — still
buildable but no longer the target for new development.

---

## Migration Directory

The `Migration/` directory at the repository root is **NOT** part of
this archive. It is the dedicated location for the future native
Android migration planning and documentation. See
`Migration/TWOHEARTS-MASTER-AUDIT-AND-MIGRATION-ROADMAP.md` for the
comprehensive migration roadmap.
