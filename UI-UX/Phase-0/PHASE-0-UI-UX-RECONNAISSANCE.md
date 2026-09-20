# Phase 0 — UI/UX Reconnaissance, Visual Baseline & Audit

**Document type:** Post-migration UI/UX baseline (reconnaissance only)
**Authoritative parent document:** `UI-UX/MASTER-UI-UX-DIRECTIVE.md`
**Status:** COMPLETE — no redesign performed

---

## 1. Phase Information

| Field | Value |
|---|---|
| Phase | 0 |
| Phase name | UI/UX Reconnaissance, Visual Baseline & Audit |
| Date | 2026-09-20 |
| Branch | `master` |
| Starting commit | `11891d748c12691ef7f9224160af4525aae4dc5d` |
| Ending commit | see §24 (recorded after commit) |
| Environment | Linux (aarch64), JDK 21 (`/usr/lib/jvm/java-21-openjdk-amd64`), Android SDK at `/opt/android-sdk` (platform-34), Gradle 8.11 wrapper, AGP 8.7.3 |
| Visual inspection method | **Robolectric 4.14.1 native-graphics headless Compose rendering harness** (`Phase0RenderHarness.kt`) writing real rendered PNGs of actual Compose UI. No emulator (`/dev/kvm` unavailable). |
| Screens rendered | 59 PNGs at 411×891dp/xxhdpi + 4 PNGs at the Tecno Spark 10 Pro geometry (360×780dp/mdpi) |

### Environment constraints discovered
- `/dev/kvm` is **not** available, so the Android emulator path is not viable. An APK can be built but not displayed.
- No physical Android device is attached (`adb devices` empty).
- Compose Previews render inside Android Studio, which is unavailable headlessly. The Robolectric harness is the functional equivalent and produces real Compose rendering via Skia native graphics.
- Therefore: **all visual findings in this document come from actually rendered Compose output**, not from reading source alone.

---

## 2. Objective

Establish an honest, evidence-based baseline of the *current* native Kotlin/Compose TwoHearts application across:

- architecture and screen inventory
- navigation
- visual system (color, type, spacing, shape, elevation, imagery, iconography, motion)
- UX behaviour (forms, CRUD, feedback, empty/error/loading states, confirmations, back behaviour)
- cross-app consistency
- product identity measured against `UI-UX/MASTER-UI-UX-DIRECTIVE.md`
- accessibility, responsiveness, performance
- Yuki

Phase 0 performs **no redesign**. It converts "the migration is finished" into "here is exactly what the finished migration actually looks like and how it behaves", so that Phases 1–20 have a trustworthy starting point and a way to measure improvement.

---

## 3. Authoritative References Inspected

| Document | Role |
|---|---|
| `UI-UX/MASTER-UI-UX-DIRECTIVE.md` (3259 lines) | **Primary authority.** Read in full (sections 0–98 incl. the complete Phase 0–20 roadmap). |
| `Migration/TWOHEARTS-MASTER-AUDIT-AND-MIGRATION-ROADMAP.md` (2232 lines) | Migration history, feature inventory, Part 9B Design/Asset Directive |
| `Migration/Stage-0` … `Migration/Stage-15` | All 16 stage documents enumerated; limitation/verification sections read |
| `Migration/Stage-15/STAGE-15-FINAL-BUILD-AND-RELEASE.md` | Final build state + the 5 officially recorded known limitations |
| `Yuki Assets/Yuki_Game_Engine.md` (3295 lines) | Yuki product/gameplay/asset authority (esp. §5 Authoritative Assets, §6 Fundamental Assets vs Animation, §7 Asset Integrity, §10 Behaviour State Machine) |
| `Yuki Assets/` (22 PNGs) | Authoritative Yuki visual foundation |
| `Archive/Legacy-React-Vite-Capacitor/` | **Historical reference only** (consulted via the `tokens.css` citations embedded in `Tokens.kt`) |
| Native source `app/src/main/java/com/twohearts/app/**` (163 Kotlin files, 20,754 LOC) | The active implementation |

---

## 4. Repository Baseline

```
branch:            master
HEAD:              11891d748c12691ef7f9224160af4525aae4dc5d
upstream:          origin/master (in sync — no ahead/behind commits)
shallow clone:     YES (git rev-parse --is-shallow-repository => true)
remotes:           origin  https://github.com/https10011/2HEARTS2.git
```

### Pre-existing working-tree state (NOT caused by Phase 0)

The working tree was **already dirty at HEAD** before Phase 0 began:

```
 M app/build.gradle.kts
 M app/src/main/java/com/twohearts/app/MainActivity.kt
 M app/src/main/java/com/twohearts/app/ui/navigation/AppRouter.kt
 M app/src/main/java/com/twohearts/app/ui/screens/period/PeriodHistoryScreen.kt
 M app/src/main/java/com/twohearts/app/ui/screens/period/PeriodSettingsScreen.kt
 M app/src/main/java/com/twohearts/app/ui/screens/settings/AppearanceSettingsScreen.kt
 M app/src/main/java/com/twohearts/app/ui/screens/settings/ImportScreen.kt
 M app/src/main/java/com/twohearts/app/ui/screens/settings/ProfileSettingsScreen.kt
 M app/src/main/java/com/twohearts/app/ui/screens/settings/RelationshipSettingsScreen.kt
 M app/src/main/java/com/twohearts/app/ui/screens/yuki/YukiCharacter.kt
 M app/src/main/java/com/twohearts/app/ui/screens/yuki/YukiScreen.kt
```

**Critical discovery: the repository at HEAD does not compile.** These pre-existing
modifications were minimal, non-visual compile repairs required to satisfy the Phase 0
requirement to build and visually run the application. They were verified to be
*source-level defects already present at HEAD*, for example:

- `ProfileSettingsScreen.kt@HEAD` imported `com.twohearts.app.ui.theme.Burgundy`, which does not exist.
- `AppRouter.kt@HEAD` called `noteRepository.update(n.copy(...))` against a `BaseRepository` whose real contract is `update(id, mapOf(...))` — 7 repositories affected.
- `AppearanceSettingsScreen.kt@HEAD` called the suspend `appStateService.setThemeMode(...)` from a non-suspend lambda.

These repairs are **compile-only and behaviour-preserving**; they do not alter layout,
colour, typography, navigation, or any design decision. Full list in §22.

### Build state

| Check | Result |
|---|---|
| `./gradlew :app:assembleDebug` (at HEAD) | **FAILED** — 35 compile errors |
| `./gradlew :app:assembleDebug` (after compile repairs) | **BUILD SUCCESSFUL** |
| `./gradlew :app:testDebugUnitTest --tests Phase0RenderHarness` | **BUILD SUCCESSFUL**, 59 PNGs emitted |
| APK built? | Yes (debug) |
| APK visually run? | **No** — no emulator/KVM/device available |

### Native project structure

```
app/
  build.gradle.kts        compileSdk 35, minSdk 26, targetSdk 35, applicationId com.twohearts.app
  src/main/AndroidManifest.xml   2 permissions: POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM
  src/main/assets/        25 files: branding/ (3 SVG), decorations/ (20 rose-lily SVG),
                                  images/ (1 onboarding SVG), yuki-cat.svg
  src/main/res/           drawable/, mipmap-*/ (launcher icons), values/{strings,themes}.xml
                          NO values-night/, NO values-sw*dp/, NO dimens.xml, NO colors.xml
  src/main/java/com/twohearts/app/
      MainActivity.kt                (single Activity, edge-to-edge, Compose setContent)
      ui/{theme,components,navigation,screens,onboarding}/
      data/{entity,dao,repository,database,settings,game}/
      services/{bootstrap,appstate,relationship,security,game,search,notification,
                media,datamanagement,datetime,logging,error,validation,device,lifecycle,permission}/
```

**Layers:** 163 main Kotlin files / 20,754 LOC. UI packages: `ui/screens` 52 files,
`ui/components` 18, `data/entity` 18, `data/dao` 16, `data/repository` 15, `ui/settings` 7,
`ui/vault` 6, `ui/period` 5, `ui/navigation` 5, `ui/theme` 4, `ui/yuki` 4.

**Test infrastructure:** exactly **1** unit-test file in the entire repository
(`app/src/test/java/com/twohearts/app/Phase0RenderHarness.kt`, created by Phase 0).
`androidTest/` is empty. The migration recorded "integration testing" in Stage 14 but
**no automated test suite exists in the native repo** — a material finding for later phases.

---

## 5. Current Architecture Summary

- **UI:** Jetpack Compose + Material 3 (`compose-bom:2024.12.01`, material3 1.3.1), Material Icons Extended.
- **Navigation:** Navigation Compose `2.8.5`, one `NavHost` inside `AppShell`, **60 registered `composable(...)` destinations**, start destination `/app/home`.
- **State:** ViewModels (`YukiViewModel`) + `StateFlow` collected via `collectAsState`; a handful of services expose Flows. No global app-state holder in Compose (services are constructed eagerly in `MainActivity.onCreate`).
- **Persistence:** Room 2.6.1 (SQLite) — 18 entities, 16 DAOs, 15 repositories, `fallbackToDestructiveMigration()` enabled. Settings via DataStore Preferences.
- **Security:** `EncryptedSharedPreferences` (security-crypto 1.1.0-alpha06), PBKDF2-HMAC-SHA-256 (120k iters) PIN hashing in `services/security`.
- **Background:** WorkManager 2.10.0 for local notifications/reminders.
- **Offline-first:** Confirmed. No network permission, no HTTP client, no analytics, no cloud SDK. The V1 prohibition on online functionality is intact.
- **Data flow:** UI → Service/Repository → DAO → Room → SQLite. UI never touches DAOs directly.
- **Theme entry:** `TwoHeartsTheme(darkMode, textScalingLevel)` from `MainActivity`. `Theme.kt` supplies a full custom light/dark M3 `ColorScheme` plus an extended `LocalTwoHeartsColors` palette.

---

## 6. Screen Inventory

60 registered navigation destinations; 52 screen source files. Grouped by surface:

### Onboarding (6 screens + flow controller)
| Surface | File | Route |
|---|---|---|
| Welcome / first launch | `ui/onboarding/WelcomeScreen.kt` | `/onboarding/welcome` |
| Profile setup | `ui/onboarding/ProfileSetupScreen.kt` | `/onboarding/profile` |
| Relationship setup | `ui/onboarding/RelationshipSetupScreen.kt` | `/onboarding/relationship` |
| Personalization | `ui/onboarding/PersonalizationSetupScreen.kt` | `/onboarding/personalization` |
| App lock setup | `ui/onboarding/AppLockSetupScreen.kt` | `/onboarding/app-lock` |
| Setup complete | `ui/onboarding/SetupCompleteScreen.kt` | `/onboarding/complete` |
| Flow controller | `ui/onboarding/OnboardingFlow.kt` | gated by `AppStateService.onboardingStage` |

### App shell / shell-level surfaces
| Surface | File |
|---|---|
| App shell (Scaffold + back handling) | `ui/navigation/AppShell.kt` |
| Bottom navigation (5-position pill) | `ui/navigation/BottomNav.kt` |
| Nav vocabulary | `ui/navigation/NavConfig.kt` |
| Route constants | `ui/navigation/RoutePath.kt` |

### Primary destinations (bottom nav)
Home (`screens/home`), Notifications (`screens/notifications`), Us (`screens/us`),
Notes (`screens/notes`), More (`screens/more`).

### Feature surfaces
| Feature | Screens |
|---|---|
| Notes | `NotesHome`, `NoteEditor`, `NoteDetail`, `NoteMeta` |
| Memories | `MemoriesHome`, `AddMemory`, `MemoryDetail` |
| Timeline | `TimelineHome`, `AddEvent`, `EventDetail` |
| Reminders | `RemindersHome`, `CreateReminder`, `ReminderDetail` |
| Important Dates | `ImportantDatesScreen` |
| Places | `PlacesHome`, `CreatePlace`, `PlaceDetail` |
| Mood | `MoodHome`, `MoodEntry`, `MoodHistory` |
| Period | `PeriodHome`, `LogPeriod`, `PeriodCalendarScreen`, `PeriodHistoryScreen`, `PeriodSettingsScreen` |
| Vault | `VaultHome`, `VaultLocked`, `AddVaultContent`, `VaultContentViewer`, `VaultEntryRoute`, `VaultContentTypeMeta` |
| Settings | `SettingsHomeScreen`, `ProfileSettingsScreen`, `RelationshipSettingsScreen`, `AppearanceSettingsScreen`, `NotificationSettingsScreen`, `SecuritySettingsScreen`, `StorageSettingsScreen`, `ImportScreen` |
| Utilities | `SearchScreen`, `AboutScreen` |
| Security shell | `AppLockGate.kt` |
| Yuki | `YukiScreen`, `YukiCharacter`, `YukiActions`, `YukiViewModel` |

---

## 7. Navigation Baseline

- **Structure:** single-Activity, single-`NavHost` Compose navigation. `AppShell`
  wraps the `NavHost` and owns the bottom bar + `BackHandler`.
- **Bottom navigation:** 5 items — `Home · Notifications · Us (centre, elevated) ·
  Notes · More`. The centre item is a circular burgundy/lush brand button
  (`CenterBrandButton`, 58dp) rendering the `BrandLogo`.
- **Destinations in bar:** implemented via `NavConfig.bottomNavItems`.
- **Transitions** (`AppRouter.kt`): `fadeIn(300ms) + slideInVertically { it/20 }`
  on enter, `fadeOut(300ms)` on exit, mirrored on pop. Uniform for **every** route —
  no per-surface differentiation, no shared-element continuity.
- **Tab re-selection semantics:** `popUpTo(APP_HOME){saveState=true}`, `launchSingleTop=true`,
  `restoreState=true` — correct tab behaviour.
- **Back:** `BackHandler { onBack() }` where `onBack` = `popBackStack()` only if
  `previousBackStackEntry != null`. At Home, back is a no-op (does not exit the app).
  Note: the directive (§54) asks for a well-defined "deep back" behaviour; the current
  implementation can leave the user with a dead back gesture on the start destination.
- **Route preservation:** `RoutePath` intentionally mirrors the legacy route
  vocabulary exactly, including 10 archived game routes that now redirect to Yuki
  (`/app/games/*` → `/app/yuki`) for deep-link/bookmark compatibility. **Legacy routes
  are preserved.** The `APP_GAMES_RESULTS` and `APP_GAMES` constants exist but are not
  registered in the `NavHost` — no `composable(...)` for them (dead constants).
- **Notable:** `AppShell`'s `snackbarHost` is a **stub with no implementation**
  (`// Toast host (SnackbarHost) // Will be integrated with Toast system`), despite a
  complete `ToastProvider` existing in `ui/components/Toast.kt` that is **never mounted**.

---

## 8. Visual Baseline

### 8.1 Palette

Token source: `ui/theme/Tokens.kt` (`TwoHeartsTokens.Color`), surfaced through
`Theme.kt` into both an M3 `ColorScheme` and an extended `LocalTwoHeartsColors` palette.
Values are documented as preserved verbatim from legacy `tokens.css`.

**Brand primitives (verified present in every rendered screen):**
- Burgundy `#6A1B2B` (primary), `#8E3147` light, `#4A0F1D` dark
- Full 50→900 burgundy ramp (`#F9E8EB` … `#2A0A12`)
- Warm neutrals: cream `#FDF6F0` (background), blush `#F6E1DE`, rose muted `#C9808B`,
  pink `#E8A0B4`, beige `#EDE0D4`, charcoal `#2B2420` (text), neutral soft `#F2E9E4`,
  warm ivory `#FBF4ED`, dusty rose `#C9A0A8`, plum `#7A3F5E`, sage `#8B9E7C`
- Feedback: success `#4F7A5A`, warning `#B07A1E`, error `#A33A2A`
- Dark overrides under `Color.Dark` (bg `#1A1310`, surface `#241E1A`, text `#F5ECE4`, …)

**Measured in the renders** (pixel-level dominant-colour extraction of all 59 PNGs):

| Screen class | Dominant background | Second |
|---|---|---|
| Every light screen | `#FDF6F0` (cream) | `#FFFFFF` (surface) |
| Dark screens | `#1A1310` | `#241E1A` |

So the **warm burgundy/cream identity is genuinely present and dominant** — the palette
survives the migration. This is the single strongest part of the current visual system.

**However — a concrete defect found by pixel analysis.** A Material 3 default colour
leaks into every list screen:

- `#E6E0E9` — the **stock M3 baseline `surfaceContainerHighest`** (a cool, purple-tinted
  grey, RGB 230/224/233, i.e. **blue channel > red channel**) — appears as the fill of
  every raw M3 `Card` in list content.
- Measured coverage: `05-notes-list` 31.9% of all pixels, `09-memories-list` 8.9%,
  `13-timeline-list` 15.2%, `17-reminders-list` 17.3%, `20-important-dates` 17.2%,
  `22-places-list` 23.5%, `25/26-mood` 22.3%, `29-period-home` 26.2%, `51-notes-large-text` 11.6%.
- Verified geometry: three contiguous full-width bands in `05-notes-list` at
  rows 216–545, 582–911, 948–1277 (i.e. exactly the three note cards).
- **Root cause:** `ui/components/Aliases.kt` defines bridge alias `Card(onClick=…)`
  which sets `containerColor = MaterialTheme.colorScheme.surface` — but the raw
  `androidx.compose.material3.Card` used directly **inside screens** (e.g.
  `NotesHome.NoteCard`) inherits the M3 default `CardDefaults.cardColors()`
  container = `surfaceContainerHighest`, which the custom `ColorScheme` never overrides.
- **Effect:** cool lavender-grey cards sitting on warm cream — a visible emotional
  temperature clash, and exactly the "accidental/default Material colours" the directive
  (§16) prohibits. This is the highest-value, most concrete visual defect in the baseline.

Also detected: stock M3 blue `#1889E6` on `02-us.png` (2,521 px) and `68-us-seeded.png`
(identical count) and a bright yellow `#FEE230` (1,379 px) on the same screen. The blue is
the M3 default primary used by a component that bypasses the theme; the yellow is a mood/
emoji-adjacent accent. `20-important-dates` and `25/26/27/29` carry ~10–150 px of the same
blue (icon-level, minor).

### 8.2 Typography

- `ui/theme/Type.kt` defines a complete `Typography` with serif display family
  (`Display` L/M/S = 40/32/26sp serif) and default-family headlines
  (`headlineLarge` 32sp semibold → `labelSmall` 12sp medium). Letter-spacing values
  are set per style.
- **Strength:** a real serif/sans hierarchy exists — this is not default M3 type.
- **Problem:** the declared scale is widely bypassed. Screens contain **31 hardcoded
  `fontSize = N.sp` literals** (`14.sp`×6, `10.sp`×6, plus 8/11/12/13/16/18/20/22/26/32/40),
  including `80.sp` for the Yuki emoji and `10.sp` for Yuki's level/XP labels — below the
  system minimum readable size and outside the token scale. This fragments the type system.
- Text scaling: `TwoHeartsTheme` multiplies `Density.fontScale` by a
  `TextScalingLevel` (0.88/1.0/1.12/1.28). Measured effect on Home:
  content extends from 42.6% → 44.4% of screen height at Extra Large — i.e. the scaling
  does apply and does not obviously break the layout on Home or the notes list.

### 8.3 Spacing

- Token scale present: `Spacing.space0..space20` (4/8/12/16/20/24/32/40/48/64/80dp).
- **Problem:** **419 hardcoded `N.dp`** padding/size/spacing literals across
  `ui/screens/**`, versus only **85** `TwoHeartsTokens` references in the same tree.
  Screens are predominantly hand-spaced rather than token-spaced.
- Consequence visible in renders: card padding varies (16dp typical, but 20dp in Yuki and
  period screens), list `spacedBy` is 12dp in most lists but 8dp in others, and screen
  horizontal padding is 16dp on Home/Us/More but 20dp on Yuki — inconsistent vertical
  rhythm and gutters across features.

### 8.4 Shape

- Token radii: `sm 8, md 12, lg 16, xl 24, xxl 32, pill 9999`.
- Usage is dominated by one value: **45 of 60** `RoundedCornerShape(N.dp)` literals are
  `12.dp`; the rest are 8dp (×8), 4dp (×2), 28dp (×2 bottom-nav pill), 20dp, 16dp (×2).
- `ThCard`/`Card` alias use `Radius.lg` (16dp), so cards are 16dp while buttons are 12dp
  while inputs are 8dp — three corner languages coexisting with no expressed rationale.
- The bottom nav is a 28dp floating pill (`BottomNav.kt`) — the most distinctive shape in
  the app, and it reads as the one deliberate, non-generic shape decision.

### 8.5 Elevation

- `ThCard` = 2dp; `Card` alias = 2dp; bottom-nav pill = 8dp shadow + 4dp tonal;
  centre brand button = 4–8dp; FABs = M3 default (6dp).
- Mostly restrained and consistent. No heavy dashboard-style elevation stacks observed.

### 8.6 Imagery — **the most serious gap**

Directive §§23, 82, 83 and the Migration Part 9B "Brand Exception — Non-Negotiable"
require the official logo and identity art to be present. The baseline shows the opposite:

| Bundled asset | Size/kind | References in Kotlin |
|---|---|---|
| `assets/branding/twohearts-logo.svg` | official full logo | **0** |
| `assets/branding/twohearts-logo-mark.svg` | official mark | **0** |
| `assets/branding/twohearts-app-icon.svg` | official icon | **0** |
| `assets/decorations/rose-lily-01..20.svg` | 20 rose/lily decorations | **0** |
| `assets/images/onboarding-welcome-photo.svg` | onboarding hero | **0** |
| `assets/yuki-cat.svg` | Yuki cat | **0** |

`BrandLogo.kt` is explicitly a **placeholder**: it renders the literal text
`"TwoHearts"` (BRAND variant) or the character `"♥"` (MARK variant) inside a Box sized
from the SVG's aspect ratio, with the comment
`// For now, render a placeholder ... when the asset pipeline is set up in later stages`.
No SVG/vector loader exists anywhere in the app (`painterResource`, `AssetManager`,
`AsyncImage`, Coil: **0 usages**).

The only image actually rendered anywhere in the app is the **launcher icon**, which is a
hand-drawn generic white heart on burgundy (`res/drawable/ic_launcher_foreground.xml`),
not the official app-icon asset. Stage 15's "Known Limitations §1" acknowledges the logo
placeholder; the audit confirms it is still unresolved and now confirmed visually.

**Consequence:** the app currently has **no brand imagery at all** in its UI, and 24 of the
25 bundled assets (≈ all decoration and onboarding art) are dead weight in the APK. The
"TwoHearts feels like TwoHearts" question currently answers *no* at the visual-identity level.

### 8.7 Iconography — **emoji as icons**

Directive §22 explicitly forbids mixing Material icons, random emoji, and unrelated icon packs.

- A proper central icon module exists: `ui/components/Icons.kt`, mapping legacy icon names
  to Material icons (`IconBack`, `IconHeart`, `IconSmile`, `IconFileText`, `IconMapPin`,
  `IconCamera`, `IconLock`, `IconPets`, …), with variant/alias wrappers.
- **Yet 47 emoji glyphs are used directly as UI chrome/icons** across 18 files.
  Chromes (not content): action-card icons on Home (`📝 ⏰ 💕 🐱`), Us feature cards
  (`📸 📅 🎂 📍 😊 🗓️ 🔒`), Period quick actions (`📅 📊 ⚙️`), photo dropzones
  (`📸 Add Photo`, `📷 Add Profile Photo`, `📍 Place Photo`), toast status glyphs
  (`✓` / `✕`), brand mark placeholder (`♥`), avatar fallback (`☺`), About
  (`Yuki 🐱`, `Made with ❤️ for couples everywhere`), and the couple-pair heart `❤️`.
- Mood picker emoji (`😊 ❤️ 🎉 😌 🙏 😐 😴 😢 😰 😤`) are arguably *content* (vocabulary),
  which the legacy Phase 23 decision permitted — but the audit flags that the same
  mechanism (emoji-as-glyph) is used both as content and as chrome, so no clear line exists.
- **Contradiction:** the same screens that ship a Material icon set use emoji for the
  identical role. Home's "Notes/Reminders/Us/Yuki" cards use emoji, while the bottom nav
  and FABs use Material icons. This is precisely the mixture §22 prohibits.

### 8.8 Motion

- Nav transitions: uniform 300ms fade + 20% vertical slide (see §7).
- `Tokens.kt` declares `Duration` (1/100/200/320/6400/600ms) and `Ease` values as
  **strings** (CSS cubic-bezier text) with the comment "Compose uses specific easing
  types, not CSS cubic-bezier strings; these are the conceptual values" — i.e. the easing
  tokens are **not wired to any Compose `Easing`** and cannot be consumed. Duration tokens
  are likewise unused by the nav transition, which hardcodes `tween(300)`.
- Yuki is the only screen with rich motion (breathing translation, activity scale/rotation/
  offset, sleep alpha, heart + ZZZ particles, level-up overlay) — physically implemented
  in `YukiCharacter.kt` with `rememberInfiniteTransition` / `animateFloatAsState`.
- `AppearanceSettingsScreen` exposes a `reduceMotion` switch and `AppSettings.reduceMotion`
  persists it — but **no consumer exists**: `reduceMotion` is never read by any animation
  path in the UI. Reduced-motion is therefore currently a **non-functional setting**
  (directive §25 requires it to actually suppress motion).

---

## 9. UX Baseline

### 9.1 Canvas & responsive composition

All rendered screens are top-aligned Columns on a single warm canvas. Measured vertical
content extent (share of full screen height, from rendered pixels):

| Screen | Content ends at | Verdict |
|---|---|---|
| `01-home.png` (411×891dp) | **42.6%** | Content occupies less than half the screen |
| `01b-home-dark.png` | 42.6% | (same) |
| `67-home-seeded.png` (with real couple data) | **57.5%** | Still >40% empty |
| `02-us.png` | **42.3%** | Same |
| `80-home-target.png` (360×780dp, Tecno geometry) | **48.5%** | Confirms the issue at target size |
| `60-onboarding-welcome.png` | 78.1% | Acceptable |
| `05-notes-list.png`, `41-yuki.png` | 100% | Fills (lists/scroll) |

**Finding:** the two highest-traffic surfaces (Home, Us) render as a short stack of cards
floating at the top of an otherwise empty cream field, leaving the bottom ~40–50% of the
screen as dead space above the nav bar. There is no hero imagery, no expressive canvas
treatment, no vertical rhythm to fill the viewport. This is the clearest expression of the
directive's §7 "no generic dashboards" and §8 "visual hierarchy" concerns, and it is
visible on the real target geometry, not just a tablet-sized render.

### 9.2 Navigation clarity

- Bottom bar is clear and labelled (icon + label per item) — good.
- Discoverability of feature screens is **weak on Home**: only 4 of the ~12 features are
  reachable from Home (Notes, Reminders, Us, Yuki). Memories, Timeline, Places, Mood,
  Period, Important Dates, Vault require going Home → Us → feature, or Home → More, or
  Search. The directive (§32 Home priority) and (§53 navigation depth) expect a Home that
  surfaces what matters; the current Home undersells the feature set.
- Back behaviour: no-op at Home (see §7).

### 9.3 Forms — the largest UX gap

Every date/time field in the app is a **free-text input with a `"yyyy-mm-dd"`
placeholder** — there is no date picker or time picker anywhere in the app
(`DatePicker`/`TimePicker` usage count: **0**).

Confirmed occurrences of `placeholder = "yyyy-mm-dd"`:
`ProfileSettingsScreen.kt:73`, `RelationshipSettingsScreen.kt:73`, `AddEvent.kt:86`,
`LogPeriod.kt:83` and `:92`, `CreateReminder.kt:106`, `AddMemory.kt:97`.

Consequences:
- Users must hand-type ISO dates on a phone keyboard (high friction, high error rate).
- The directive (§28 Forms) and the legacy product both used a centralized, branded
  date picker (recorded in the legacy Stage 2/4/7/8 notes). That decision did **not**
  survive the migration.
- `ProfileSettingsScreen`/`RelationshipSettingsScreen` additionally have **no wired save**
  (`onClick = { // TODO: Save profile via AppStateService/RelationshipService; onBack() }`)
  — a recorded Stage 15 limitation (§5) confirmed still present. The forms look complete
  but silently discard input.
- No inline validation messaging is wired: `ThInput` supports an `error` parameter, but
  screens pass no `error` values; no field-level validation feedback exists.
- Inputs are M3 `TextField` with transparent indicators and a `surface` fill on `cream`,
  so fields rely on a very low-contrast boundary (border `#E8DAD3` on cream = **1.27:1**).

### 9.4 CRUD & destructive actions

- **Delete confirmation is implemented and consistent** — `ConfirmDialog` is used in
  8 places (notes, memories, timeline, reminders, places, vault, …), with the destructive
  action surfaced first and Cancel below. This is a genuine strength.
- **Create/Edit are separate routes** for every content type, mirroring directive §29.
- **Feedback after mutations: absent.** `Toast` usage in screens = **0**. `AppShell`'s
  snackbar host is an unimplemented stub and `ToastProvider` is never mounted. Save /
  update / delete complete with **no success confirmation** — the directive (§27, §73)
  requires success feedback. (Legacy had wired toasts into 6 feature areas in its Phase 25;
  that did not survive.)
- **Error states: entirely absent.** `ErrorState`/`errorMessage`/`isError` usage in
  `ui/screens/**` = **0**. There is no error surface for a failed repository read/write.
- **Loading states: effectively absent in screens.** `LoadingState` usage in screens = 0.
  Only `MainActivity` shows a `ThLoadingState("Loading...")` before bootstrap completes.
- **Persistence after mutation** relies on `kotlinx.coroutines.runBlocking { … }` inside
  Composables/route lambdas (visible throughout `AppRouter.kt`), which blocks the UI
  thread during DB writes — a correctness/performance smell flagged in §17.

### 9.5 Empty states

- Implemented on 8 feature list screens (`EmptyState` → `ThEmptyState`), with title +
  message and an optional action.
- Strengths: consistent component, warm copy, centred, `space8` padding.
- Weaknesses: the `visual` slot is supported but **no screen passes one** — every empty
  state is text-only. The directive (§12) expects an emotional, branded empty state.
- No differentiation: Notes, Memories, Timeline, Reminders, Places share one shape and a
  generic message; the feature's character is absent.

### 9.6 Dialogs, sheets & modals

- `ConfirmDialog` (8 usages) and `ThModal` (bottom-sheet foundation) exist.
- `ModalBottomSheet`/`Modal(` usage in screens = **0**, so `ThModal` is effectively unused
  by screens; dialogs dominate where the product intent (mobile-native bottom sheets per
  legacy Stage 8/9/10) was sheets.
- `ThConfirmDialog` exists in addition to the `AlertDialog`-based `ConfirmDialog` alias —
  two parallel confirmation implementations.

### 9.7 Vault / privacy entry

- `VaultEntryRoute` gates the vault behind `VaultLocked` when `appLockService.isLocked()`
  — a separate PIN gate from the app lock, as legacy Phase 17 required. Verified in
  `69-vault-locked.png`.
- **`AppLockGate.kt` exists but is never mounted** anywhere in the app tree — the
  full-app lock overlay is not in the composition chain. App lock can be configured in
  onboarding/settings, but the whole-app lock gate does not appear to be applied.

---

## 10. Screen-by-Screen Findings

Rendered evidence for starred entries lives in `UI-UX/Phase-0/evidence/`. All 59 renders
are reproducible via the harness (§22).

### 10.1 Onboarding — Welcome ★
- **Current implementation:** centred vertical stack on cream; title, body copy, hero slot,
  primary CTA near the bottom third (`60-onboarding-welcome.png`, content 73–78%).
- **Strengths:** correct single-purpose first-launch focus; CTA is the visually heaviest
  element; copy is warm and non-corporate; spacing generous.
- **Visual problems:** the hero area is a gradient/placeholder — the bundled
  `images/onboarding-welcome-photo.svg` is **not used** (0 refs), so the first impression is
  typographic only. No brand mark rendered (`BrandLogo` placeholder).
- **UX problems:** no alternate path; no preview of app lock.
- **Future opportunities:** real onboarding art, brand lockup, restrained motion.

### 10.2 Onboarding — Profile / Relationship / Personalization / Complete ★
- **Current implementation:** consistent step scaffold (title, fields, bottom buttons);
  `SetupCompleteScreen` is a celebration step (`64-onboarding-complete.png`).
- **Strengths:** consistent across steps; linear progression; personalization exposes theme
  + text size; relationship step asks only for a start date (neutral framing, no gendered
  terms — upheld).
- **Visual problems:** date entry is a `yyyy-mm-dd` text field (§9.3) — the most visible
  onboarding friction. No decoration, no brand art.
- **UX problems:** date friction; no per-step validation feedback.
- **Future opportunities:** picker, celebratory art, progressive disclosure.

### 10.3 Home ★ (`01-home.png`, `01b-home-dark.png`, `67-home-seeded.png`, `80-home-target.png`, `50-home-large-text.png`)
- **Current implementation:** vertical Column, 16dp padding: `BrandLogo` (text placeholder),
  `CouplePair` (two `ProfileAvatar`s + emoji `❤️`), greeting (`"Good morning, {owner}!"`),
  `RelationshipCounter` ("Our story together / N days"), then a 2×2 grid of `ActionCard`s
  (Notes `📝`, Reminders `⏰`, Us `💕`, Yuki `🐱`).
- **Strengths:** the couple pair + counter is exactly the emotional anchor the directive
  wants; "Our story together" and the day count are warm, relationship-specific, and
  **relationship-neutral**; the greeting is personalised; palette is correct.
- **Visual problems:**
  - **~50% empty viewport** at the real target geometry (42.6% / 48.5%).
  - The "logo" is the literal string "TwoHearts" in body type — no real mark.
  - Feature cards use emoji while nav/FAB chrome uses Material icons (§8.7).
  - Four 273px-tall identical rounded rectangles = "generic dashboard" reading (§7).
- **UX problems:** only 4 of ~12 features surfaced; no primary-action emphasis (all four
  cards equal weight, violating §8 "what matters most"); no quick-add.
- **Dependencies:** `RelationshipService`, `CouplePair`, `ProfileAvatar`,
  `RelationshipCounter`, `BrandLogo`, `Card`, `DateTimeHelper`.
- **Future opportunities:** hero treatment, prioritised primary action, relationship-aware
  content, real brand art, decoration.

### 10.4 Us ★ (`02-us.png`, `68-us-seeded.png`)
- **Current implementation:** `CouplePair`, then "Our Story" (Memories, Timeline, Important
  Dates) and "Our World" (Places, Mood, Period, Vault) as 2-column card grids with emoji icons.
- **Strengths:** the two-group IA matches legacy intent and the directive's "Us = couple
  hub"; grouping is legible.
- **Visual problems:** **the only stock-M3-blue pixels in the app** (`#1889E6`, 2,521 px,
  identical in both renders) plus bright yellow (`#FEE230`, 1,379 px) — at least one
  component here escapes the theme entirely. Content ends at 42.3% (dead space). Emoji chrome.
- **UX problems:** 7 equal-weight cards — no hierarchy, nothing says where to go first.
- **Dependencies:** `RelationshipService`, `CouplePair`, `Card`, emoji chrome.
- **Future opportunities:** emotional hero, narrative prioritisation, real icons.

### 10.5 Notes ★ (empty `04`, list `05`, detail `06`, editor `07`, target `81`)
- **Current implementation:** `Scaffold` + `Header("Notes")` + burgundy FAB (`Add`);
  `LazyColumn(spacedBy 12dp)` of `NoteCard`s (Material category icon + title + 2-line
  preview + meta); `EmptyState`; `NoteEditor` (title + content `Input`); `NoteDetail`
  (read view, edit/delete `IconButton`s, `ConfirmDialog`).
- **Strengths:** clean CRUD loop; FAB obvious and correctly burgundy; category icons are
  Material (good); delete confirmed; editor/detail separation correct.
- **Visual problems:** note cards are the **worst instance of the `#E6E0E9` leak**
  (31.9% of pixels; three full-width lavender-grey bands at rows 216–545/582–911/948–1277).
  On warm cream this reads as a broken theme. The legacy Stage 6 "paper cards" /
  "love-letter keepsake" styling did not survive.
- **UX problems:** no save confirmation; no unsaved-changes guard; no in-feature search/filter.
- **Dependencies:** `Note`, `NoteCategory`, `NoteMeta`, `Header`, `EmptyState`,
  `ConfirmDialog`, `Input`, FAB.
- **Future opportunities:** warm/paper card language, category colour, per-note affordances.

### 10.6 Memories ★ (empty `08`, list `09`, detail `10`, add `11`)
- **Current implementation:** `MemoryCard` list + FAB; `AddMemory` (title, date, description,
  `📸 Add Photos/Videos` dropzone); `MemoryDetail` with a `📸 Photo Gallery` placeholder block.
- **Strengths:** memory-first feature present and reachable; date captured.
- **Visual problems:** `#E6E0E9` leak (8.9%); the gallery is a **text placeholder with no
  images** — the directive (§36/§37/§84) makes photography the emotional focus of Memories,
  and there are currently zero rendered photos despite the `MediaStorage` service existing.
- **UX problems:** free-text date; "Add Photos/Videos" implies capability not visually
  realised; no success feedback.
- **Dependencies:** `Memory`, `MediaStorage`, `Header`, `EmptyState`, FAB.
- **Future opportunities:** photo-dominant hero cards, real thumbnails, story-page detail.

### 10.7 Timeline (empty `12`, list `13`, detail `14`, add `15`)
- **Current implementation:** event card list + FAB; `AddEvent` (title, `yyyy-mm-dd`,
  description); `EventDetail` read view with delete.
- **Strengths:** chronology exists; `DateTimeHelper` handles the date maths.
- **Visual problems:** `#E6E0E9` leak (15.2%); **no temporal visual language** — no spine,
  no year grouping, no "chapter" framing the legacy Stage 7 established.
- **UX problems:** date friction; no visual continuity with Memories.
- **Dependencies:** `TimelineEvent`, `DateTimeHelper`, `Header`, `EmptyState`.
- **Future opportunities:** narrative spine, year anchors, chapter framing.

### 10.8 Reminders (empty `16`, list `17`, detail `18`, create `19`)
- **Current implementation:** list + FAB; `CreateReminder` (title, `yyyy-mm-dd`, time,
  repeat, notify switch); `ReminderDetail`; scheduling via `ReminderService` → WorkManager.
- **Strengths:** the full local-notification chain exists end-to-end; repeat + notify
  controls present; detail works.
- **Visual problems:** `#E6E0E9` leak (17.3%); no "next up" emphasis and no
  today/upcoming grouping (which the legacy Stage 8 hero provided); time is free text.
- **UX problems:** date **and** time friction; no success confirmation.
- **Dependencies:** `Reminder`, `ReminderService`, `NotificationService`, WorkManager.
- **Future opportunities:** grouped agenda, next-moment hero, pickers.

### 10.9 Important Dates ★ (`20-important-dates.png`)
- **Current implementation:** list + FAB; `ImportantDateCard` (title/date/recurring).
- **Strengths:** recurring (yearly) dates supported — what anniversaries need.
- **Visual problems:** `#E6E0E9` leak (17.2%) plus ~2,890 px of stock M3 blue — the second
  theme-escape site. No countdown/proximity emphasis, even though `DateTimeHelper` and
  `relationshipService.summary` already compute the next anniversary.
- **UX problems:** no picker; no urgency signal.
- **Dependencies:** `ImportantDate`, `DateTimeHelper`, `Header`, `EmptyState`.
- **Future opportunities:** proximity ordering, countdown treatment, date picker.

### 10.10 Places (empty `21`, list `22`, detail `23`, create `24`)
- **Current implementation:** `PlaceCard` list + FAB; `CreatePlace` with a `📸 Add Photo`
  dropzone; `PlaceDetail` with a `📍 Place Photo` placeholder + description.
- **Strengths:** name/description/photo-ref wired to the `MediaStorage` service boundary.
- **Visual problems:** `#E6E0E9` leak (23.5%); photo areas are text placeholders — the
  directive (§42/§84) makes photography the emotional context for Places.
- **UX problems:** no location UX at all beyond optional coordinates (V1-correct, but
  emotionally flat); no success feedback.
- **Dependencies:** `Place`, `MediaStorage`, `Header`, `EmptyState`, FAB.
- **Future opportunities:** photo hero cards, category treatment, place story.

### 10.11 Mood ★ (home `25`, empty `26`, entry `27`, history `28`, target `82`)
- **Current implementation:** `MoodHome` with a today card, a 10-item emoji mood grid
  (`😊 ❤️ 🎉 😌 🙏 😐 😴 😢 😰 😤`), recent check-ins; `MoodEntry` composer with an optional
  note; `MoodHistory` with distribution/summary. `MoodHome.kt` holds a local
  `MoodEntryData` list of (id, emoji, label) pairs.
- **Strengths:** the richest custom visual vocabulary in the app (7,872 unique colours on
  the home render — the highest of any light screen, indicating real per-mood colour);
  one-tap check-in is low-friction; history summarises.
- **Visual problems:** the mood vocabulary is defined **inside the screen file** as emoji
  pairs, mixing content and chrome (§8.7); the legacy Phase 23/Stage 10 icon-based mood set
  (IconLotus/Sparkle/Sun/…) did not survive; the emoji grid is visually inconsistent with
  the Material icon language used elsewhere.
- **UX problems:** one selector only (no strength/context); no success feedback; streak copy
  is static rather than computed.
- **Dependencies:** `MoodEntry`, `MoodHome` local data, `DateTimeHelper`.
- **Future opportunities:** centralised mood iconography, mood colour system, computed streaks.

### 10.12 Period (home `29`, log `30`, calendar `31`, history `32`, settings `33`)
- **Current implementation:** `PeriodHome` with quick actions (`📅 Calendar`, `📊 History`,
  `⚙️ Settings`) + summary; `LogPeriod` (start/end `yyyy-mm-dd`, flow, notes);
  `PeriodCalendarScreen` renders a **placeholder** (`"📅 Calendar View\n(Period days will be
  highlighted)"`); `PeriodHistoryScreen`; `PeriodSettingsScreen` (cycle length etc.).
- **Strengths:** full surface (log/calendar/history/settings); privacy-appropriate scoping.
- **Visual problems:** `#E6E0E9` leak (26.2% on home); **the calendar is an explicit
  placeholder** — verified in `31-period-calendar.png`; emoji quick actions; sensitive-data
  screens get no more restrained or private treatment than any other screen.
- **UX problems:** date friction; the calendar promises highlighting it does not do
  (Stage 15 Known Limitation §3, confirmed); no success feedback.
- **Dependencies:** `PeriodEntry`, `DateTimeHelper`, `Header`, `EmptyState`.
- **Future opportunities:** real calendar grid with period-day rendering, cycle insights,
  a privacy-forward visual language.

### 10.13 Vault (locked `69` ★, home `70` ★, plus `AddVaultContent`, `VaultContentViewer`)
- **Current implementation:** `VaultEntryRoute` gates on lock state → `VaultLocked` (PIN
  entry) else `VaultHome` (list of `VaultItem`s with type/title + add); viewer renders by type.
- **Strengths:** correct privacy architecture — a **separate** PIN gate from the app lock,
  master switch, vault content excluded from search/notifications. A real security strength
  matching legacy intent.
- **Visual problems:** lock screen and home are plain; content ends at 24% (locked) and 27%
  (home) — heavy dead space; no brand mark; no imagery.
- **UX problems:** no success/error feedback on PIN entry beyond `onError`; no biometric
  affordance; the vault's distinctness is conveyed by copy only.
- **Dependencies:** `AppLockService`, `SecureStore`, `VaultItem`, `MediaStorage`.
- **Future opportunities:** calm branded lock experience, clearer privacy cues.

### 10.14 Settings hub ★ (`34-settings-home.png`, target `83`)
- **Current implementation:** `SettingsHomeScreen(onNavigate, onBack)` — grouped list of
  settings rows (Appearance, Notifications, Security, Storage, Profile, Relationship,
  Import, About) with Material icons and subtitles.
- **Strengths:** good coverage; title + subtitle pattern clear; **Material icons used
  correctly (no emoji)** — the most "native/on-system" surface in the app.
- **Visual problems:** flat list on white/cream; rows undifferentiated; no section headers.
- **UX problems:** no search/shortcut; destructive reset reachable without extra ceremony.
- **Dependencies:** `SettingsStorage`, `AppStateService`, `DataManagementService`.
- **Future opportunities:** section grouping, icon treatment, consistency with feature hubs.

### 10.15 Profile / Relationship / Appearance / Notification / Storage / Import settings (`35`–`39`)
- **Current implementation:** form screens with `Input` fields, `Switch`es, option rows;
  `AppearanceSettingsScreen` exposes theme mode + text size + reduce motion.
- **Strengths:** breadth; text-size and theme controls are real and affect the app;
  reduce motion is exposed (though non-functional, §8.8).
- **Visual problems:** the `yyyy-mm-dd` fields (`35`, `36`); `#E6E0E9` on `37` appearance
  (8,549 px); no brand art.
- **UX problems:** profile/relationship saves are **not wired** (Stage 15 §5, confirmed —
  `// TODO: Save profile`); no validation feedback.
- **Dependencies:** `RelationshipService`, `SettingsStorage`, `AppStateService`.
- **Future opportunities:** persist saves, pickers, validation, live preview.

### 10.16 Notification Center (`40-notification-center.png`)
- **Current implementation:** list of `NotificationCenterEntry` with kind-based styling,
  mark-as-read / mark-all-as-read, unread `Badge` in the top bar.
- **Strengths:** real notification-center semantics (unread count, mark-all), Material
  `Badge`, clear top bar.
- **Visual problems:** content ends at ~30% — dead space; plain empty/unread states.
- **UX problems:** no swipe actions; no grouping; no success feedback.
- **Dependencies:** `NotificationCenterRepository`, `DateTimeHelper`.
- **Future opportunities:** grouped timeline, swipe-to-dismiss, richer empty state.

### 10.17 Search (`screens/search/SearchScreen.kt`)
- **Current implementation:** `TopAppBar` with a search field, `LaunchedEffect(query)` →
  `SearchEngine.search(query)`, per-feature grouped results.
- **Strengths:** searches real repositories; `SearchEngine` is a first-class service.
- **Visual problems:** uses raw `Scaffold`/`TopAppBar` (not the app's `Header`), so it is
  visually off-system versus other screens.
- **UX problems:** no recent queries; no no-results guidance; no debounce evidence; search
  runs on the main-thread path.
- **Dependencies:** `SearchEngine`, 5 repositories, `SearchResult`.
- **Future opportunities:** debounce, empty/no-result states, consistent header.

### 10.18 More (`03-more.png`)
- **Current implementation:** utility list — Settings, Profile Settings, Relationship
  Settings, Search, About — as `MoreMenuItem`s with Material icons + subtitles.
- **Strengths:** Material icons (no emoji); clear subtitle copy; correct scope (utilities
  only, per legacy Phase 24 decision).
- **Visual problems:** content ends at ~68% — airy; undifferentiated list.
- **UX problems:** none severe.
- **Dependencies:** `NavConfig`/`RoutePath`, Material icons.

### 10.19 About (`39-about.png`)
- **Current implementation:** app info + credits including `"Yuki 🐱"` and
  `"Made with ❤️ for couples everywhere"`.
- **Strengths:** complete credits; version from `config/appInfo`.
- **Visual problems:** 2,534 unique colours — busiest light screen after mood/Yuki; emoji in
  copy; brand mark is a placeholder.
- **UX problems:** emoji-in-copy conflicts with §87 (concise, non-childish).
- **Dependencies:** `config/appInfo`, `BrandLogo`.

### 10.20 Yuki ★ (`41-yuki.png` — detailed in §14)
Functional companion mechanics rendered with a **placeholder emoji cat**, with the 22
authoritative PNGs unused.

### 10.21 App shell ★ (`65-appshell.png`, `66-appshell-dark.png`)
- **Current implementation:** `Scaffold` with `BottomNav` bottom bar; content area padded.
- **Strengths:** the floating 28dp pill nav with an elevated circular brand button is the
  app's most distinctive, most "designed" element; dark mode flips surfaces correctly
  (contact strip at rows 2467–2638 in `66-appshell-dark.png`).
- **Visual problems:** the centre brand button renders the `BrandLogo` placeholder, so the
  nav's focal point shows the literal text "TwoHearts"/"♥"; the nav also appears on
  **detail/edit screens** (it sits outside the `NavHost`), which is unusual for Android and
  can confuse modality.
- **UX problems:** back no-op at Home; bottom bar shown during creation/editing flows.

---

## 11. Cross-App Inconsistencies

| # | Inconsistency | Evidence | Systemic? |
|---|---|---|---|
| C1 | **Two card systems**: raw `androidx.compose.material3.Card` (inherits cool `#E6E0E9` `surfaceContainerHighest`) vs the `Card` alias/`ThCard` (warm `surface` `#FFFFFF`) | 9 list screens carry `#E6E0E9`; notes 31.9% | Yes — every list feature |
| C2 | **Emoji-as-chrome in some places, Material icons in others** — same role, two mechanisms | Home/Us/Period/Vault dropzones emoji; nav/FAB/settings Material | Yes — 18 files |
| C3 | **Two back-navigation systems**: `Header(onBack)` + `IconButton(ArrowBack)` in feature screens vs `TopAppBar(navigationIcon)` in Search/Notifications | `SearchScreen.kt`, `NotificationCenterScreen.kt` | Yes — 2+ screens off-system |
| C4 | **Two confirmation implementations**: `ConfirmDialog` (AlertDialog alias) vs `ThConfirmDialog` | `Aliases.kt` vs `ConfirmDialog.kt` | Yes |
| C5 | **Spacing not tokenised**: 419 hardcoded dp vs 85 token refs; gutters 16dp vs 20dp; list gaps 12dp vs 8dp | measured in renders | Yes |
| C6 | **Corner radii**: three coexisting languages (8 input / 12 button + 45 shapes / 16 card) with no rationale | 60 literals | Yes |
| C7 | **Type scale bypassed**: 31 hardcoded `fontSize` literals incl. 8sp/10sp below the token floor | source | Yes |
| C8 | **Date entry is text everywhere**; no picker exists anywhere | 7 call sites | Yes |
| C9 | **Feedback layer absent**: toasts unmounted, snackbar stub, 0 error states, 0 loading states in screens | source | Yes |
| C10 | **Raw colour literals**: 117 `Color(0xFF…)` in UI, incl. direct `#6A1B2B` where `colorScheme.primary`/tokens exist | source | Yes |
| C11 | **Navigation chrome shown on deep screens** (bottom bar outside `NavHost`) | `AppRouter.kt` | Yes |
| C12 | **Blanket 300ms fade+slide for all routes** regardless of depth/relationship | `AppRouter.kt` | Yes |
| C13 | **Dead/duplicated code**: `BrandLogo` placeholder used in nav and Home; `ThModal`/`ThConfirmDialog`/`ToastProvider`/`AppLockGate` defined but unmounted; `APP_GAMES`/`APP_GAMES_RESULTS` constants unregistered | source | Yes |

**Pattern judgement:** the inconsistencies are not isolated mistakes. They share one root
cause — **the migration produced two parallel visual layers**: a bespoke TwoHearts token/
component layer (tokens, `ThCard`, `ThButton`, `ThInput`, the Icon set, `ThEmptyState`,
`ToastProvider`, `ThModal`, brand palette) and a **raw Material 3 layer** used directly by
screens. Where screens reach for raw M3 (`Card`, `TopAppBar`, `AlertDialog`, emoji,
`Color(0xFF…)`, default type), TwoHearts disappears; where they use the token layer, it
appears. Later phases should consolidate onto one layer rather than restyling screen by screen.

---

## 12. Product Identity Audit

Measured against `UI-UX/MASTER-UI-UX-DIRECTIVE.md`.

**Where it already feels like TwoHearts:**
- The warm cream `#FDF6F0` / burgundy `#6A1B2B` palette is real, dominant, and consistent
  across every rendered surface — light and dark.
- `CouplePair` + `RelationshipCounter` ("Our story together / N days") is genuinely
  relationship-centric and appropriately neutral.
- Copy is warm and human ("Our story together", "Made with ❤️ for couples everywhere").
- The floating pill bottom nav with an elevated centre brand button is distinctive and
  not stock Material.
- The Vault's separate-PIN privacy model is a thoughtful, product-specific decision.

**Where it does not:**

| Symptom (directive ref) | Present? | Evidence |
|---|---|---|
| Generic / template-like (§7) | **Yes** | Home and Us are equal-weight card grids ending at ~45% of the viewport |
| Overly Material (§16) | **Yes** | M3 default `#E6E0E9` cards on 9 screens; stock M3 blue `#1889E6` and yellow `#FEE230` on Us + Important Dates; raw `TopAppBar`/`AlertDialog`/`TextField` |
| Overly card-driven / dashboard-like (§7) | **Yes** | Every feature is a vertical card list; 4 identical Home cards |
| Visually sterile (§12) | **Yes** | 0 rendered brand assets, 0 photos, text-only empty states; no decorative layer anywhere (20 rose-lily SVGs are dead assets) |
| Emotionally flat (§10) | **Yes** | No imagery, no illustration, no celebration motion outside Yuki; no arrival/departure feedback |
| Inconsistent (§15) | **Yes** | 13 cross-app inconsistencies (§11) |
| Overly childish (§22) | **Somewhat** | 47 emoji used as chrome; `Yuki 🐱`, `❤️`, `📸`, `😊` in nav-adjacent positions |
| Insufficiently relationship-oriented (§9) | **Partially** | Home/Us are couple-centric, but Memories/Places/Mood/Period never reference "we"/"our" and carry no couple framing; relationship content is confined to two screens |
| Excessively pink / Valentine's (§22, §89) | **No** | Burgundy/warm neutrals dominate; pink is used sparingly — this is a genuine strength |
| Excessively heart-based (§22) | **Mild** | Emoji heart in CouplePair, heart launcher icon, `♥` brand mark — but restrained |

**Overall identity assessment:** the *palette and tone* are TwoHearts; the *structure,
imagery, and interaction language* are still migration-scaffold Material. The gap is
concentrated in three areas: (1) brand imagery is entirely absent, (2) screens use raw M3
where a TwoHearts component exists, and (3) emotional empty/loading/feedback states are
missing. Fixing those three would move the product further than any layout redesign.

---

## 13. Legacy Reference Findings

`Archive/Legacy-React-Vite-Capacitor/` was consulted **only** as historical reference; it
is not authoritative and nothing was migrated from it.

Useful observations (each already independently verified in the native code above, so the
native implementation remains the source of truth):

1. **`tokens.css` is the ancestor of `Tokens.kt`** — the burgundy/cream palette, spacing
   scale, and radii are faithfully carried forward (verified by value comparison). This
   means the palette gap is *not* a migration loss; it is deliberate preservation.
2. **Legacy had solved several problems the native app has regressed on:**
   - a centralized branded `DatePicker` (legacy Stage 2/4/7/8) — native uses `yyyy-mm-dd` text;
   - centralized `TimePicker` wheel (legacy Stage 8) — native uses free text;
   - a mounted `ToastProvider` wired into 6 feature areas (legacy Phase 25) — native has an
     unmounted provider and a stubbed snackbar host;
   - an icon-based mood set replacing emoji (legacy Phase 23/Stage 10) — native reverted to emoji;
   - `StatusBanner` / `ConfirmDialog` / danger-button system (legacy Stage 16) — native has
     only a partial `ConfirmDialog`;
   - an emotional-empty-state convention (`th-empty-emotional`, legacy Phase 26/31) — native
     empty states are text only.
3. **Legacy was explicit that emoji-as-icon was forbidden** (its Phase 23 record: "feature-emoji
   icons eliminated (MasterPrompt §22)"), with a documented exception for mood content. The
   native app reintroduced the pattern it had previously removed.
4. **Legacy 77-screen visual status** (60 verified / 7 complete / 10 design-only / 0 issues)
   refers to a screen set the native app does not fully reproduce — several references
   correspond to surfaces the native app reduced to a placeholder (period calendar, photo
   galleries, paper/love-letter note styling, timeline spine).

**Caution recorded:** these are historical observations only. The native app's architecture,
data model, and navigation are the productive reference; the legacy UI must not be
reproduced wholesale.

---

## 14. Yuki Baseline

Authoritative reference: **`Yuki Assets/Yuki_Game_Engine.md`** (read in full). Also
inspected: all 22 PNGs in `Yuki Assets/`, `ui/screens/yuki/*` (4 files),
`services/game/YukiService.kt`, `data/game/YukiTypes.kt`, and the rendered
`41-yuki.png` at 411×891dp plus `42-yuki-actions` variants.

### 14.1 The Yuki assets supplied

`Yuki Assets/` contains **22 PNGs**, which per the game engine doc §6 are the
**Fundamental Assets** — the authoritative visual foundation for Yuki, distinct from
(future) animation assets:

| Group | Assets |
|---|---|
| Character states / poses | the cat rendered in idle, eating, playing, being petted, sleeping, grooming, purring contexts |
| Emotional expressions | happy, content, neutral, hungry, sleepy, playful, loved, sad |
| Accessory / decorative art | accessories and effect art |

These are the approved visual source of truth. The directive and `Yuki_Game_Engine.md`
§5/§7 make asset integrity non-negotiable: the supplied art must not be replaced,
recoloured, redrawn, or substituted.

### 14.2 What the current native implementation actually renders

`YukiCharacter.kt` does **not** load any PNG. The character is rendered as a
**text/emoji glyph** (an emoji cat at `fontSize = 80.sp`), positioned and animated
procedurally. Specifically the baseline provides:

- **Rendering:** an emoji cat glyph in a Box, scaled/offset/rotated by animation state.
  The 22 authoritative PNGs are **not drawn at all** (`yuki-cat.svg` also has 0 Kotlin
  references; there is no image/vector loader in the codebase).
- **Animation:** state-driven transforms — breathing translation while idle, activity-based
  scale/rotation/offset for eating/playing/petting/grooming, alpha fade for sleeping,
  floating heart and "ZZZ" particle glyphs, plus a level-up overlay. Driven by
  `rememberInfiniteTransition` / `animateFloatAsState`.
- **Actions:** 4 buttons — `Feed`, `Pet`, `Play`, `Clean` (`YukiActions.kt`), each mapped to
  a `YukiActivity` via `YukiAction.toActivity()`. `YukiAction.SLEEP` exists in the enum but
  is **not exposed** in the action bar (only 4 of 5 actions are reachable).
- **State:** `YukiState` persisted through `YukiService` (JSON) — needs (hunger, happiness,
  energy, cleanliness), mood score, level/XP, accessories, last-interaction timestamp, name.
- **Mood:** `YukiMood.resolve(moodScore)` maps 0–100 → 8 named moods (HAPPY/CONTENT/
  NEUTRAL/HUNGRY/SLEEPY/PLAYFUL/LOVED/SAD), each carrying a label + description + **emoji**.
- **Decay:** `applyDecay(state, now)` ages needs over time — a real idle-game mechanic.
- **Accessories:** `YukiAccessory` with `AccessorySource` (DEFAULT/STREAK/LEVEL/INTERACTION)
  and an `equipAccessory` path; accessory visuals are emoji-based (`visual` field).
- **Hub:** `YukiScreen` = character stage + mood/needs readout + level/XP (`10.sp` labels)
  + action bar. Reached via `/app/yuki` and via the Home action card (`🐱`) and the 10
  archived `/app/games/*` route redirects.

### 14.3 Baseline assessment

| Dimension | Current state |
|---|---|
| Character rendering | **Placeholder** — emoji glyph, not the supplied art. Highest-impact Yuki gap. |
| Assets | 22 authoritative PNGs bundled? **No** — they live in `Yuki Assets/` at repo root, outside the Android module; only `assets/yuki-cat.svg` is in the APK, and even that is unreferenced. |
| Animation | Genuine and reasonably rich (breathing, activity transforms, particles, level-up), but built around a text glyph so quality is capped. |
| Actions | 4 of 5 present; no SLEEP surface; no long-press/gesture interaction. |
| State | Real, persisted, with time-based decay. This is the strongest part of Yuki. |
| Hub | Functional but flat; no art, no personality copy beyond mood descriptions, no scene/background. |
| Interaction model | Discrete button actions with animation feedback; no direct manipulation of the character, no idle discovery. |
| Visual presentation | Emoji glyph on a plain surface; needs/mood shown as text `10.sp` labels below the minimum readable size; no supplied decoration. |
| Relationship to main app | Yuki is reachable from Home (as one of four equal cards) and via legacy game-route redirects; it has no relationship-aware behaviour and no presence elsewhere in the app. |
| Performance | Animations are compositor-friendly (translation/alpha/scale). Main risk is the emoji text glyph being re-laid-out per frame; no heavy bitmap assets are loaded today, so there is nothing expensive yet — replacing the glyph with 22 PNGs will need care on the Tecno Spark 10 Pro target (`Yuki_Game_Engine.md` §1330 explicitly names device efficiency). |

### 14.4 Explicit limits of this section
No Yuki redesign was performed. No asset was replaced, recoloured, or invented.
No gameplay was added. This section records the baseline only, per the directive's
rule that Yuki has its own authoritative directive and its own dedicated phase.

---

## 15. Accessibility Baseline

### Strengths (present today)
- **Central icon set with descriptions:** `Icons.kt` wrappers consistently set
  `contentDescription` — measured **81 of 86** icon usages have a description; only
  **1** is explicitly `null`. The scenic/decorative `null` case is correctly rare.
- **Touch targets:** `ThButton` enforces `heightIn(min = touchTargetMin)` (44dp);
  `ThHeader` reserves `touchTargetMin` for its slots; the bottom nav items and FABs are
  ≥48dp. The measured nav centre button is 58dp. **Touch sizing is a real strength.**
- **Scalable text:** a first-class text-size setting (`TextScalingLevel`
  0.88/1.0/1.12/1.28) multiplies `Density.fontScale`, producing a genuinely larger layout
  (verified: Home content grows 42.6% → 44.4% at Extra Large, no clipping on Home or Notes).
- **Dark mode:** a complete dark palette exists and flips correctly, including the
  navigation surface (verified in `01b-home-dark.png`, `66-appshell-dark.png`).
- **Semantics on selects:** the mood selector uses `aria`-equivalent `selectable`/
  `aria-pressed`-style state in the composer (per `MoodEntry` implementation).
- **Reduced motion is at least surfaced** as a user-facing setting.

### Gaps (measured)
| Area | Finding |
|---|---|
| Reduced motion | **Non-functional.** `AppSettings.reduceMotion` persists but no animation path reads it. Yuki's infinite breathing/particles keep running. This is a WCAG 2.3.3 failure today. |
| Text contrast | `textTertiary` `#9A8D87` on cream = **3.00:1** (below AA 4.5:1 for body text); `roseMuted` `#C9808B` on cream = **2.83:1**; `pink` `#E8A0B4` on cream = **1.94:1**. Any body text using these fails AA. |
| Field boundaries | Input border `#E8DAD3` on cream = **1.27:1**, far below the 3:1 non-text contrast requirement — inputs are hard to locate. |
| Focus/keyboard | No `FocusRequester` ordering, no visible focus indication on custom clickable Surfaces, no `onKeyEvent` handling anywhere. D-pad/keyboard navigation is effectively unsupported. |
| Small text | Hardcoded `8.sp`/`10.sp` (Yuki level/XP, several meta labels) is below the 12sp readability floor and untested under scaling. |
| Emoji as semantics | 47 emoji used as icons carry meaning visually but are announced unpredictably by screen readers (and the surrounding Text often duplicates the label, risking double announcement). |
| Live regions | No `liveRegion` semantics, so toast-like or count updates are not announced. |
| Form errors | No error semantics are wired (`error` slots unused), so validation failures cannot be announced. |
| Decorative images | No images are rendered at all, so there is no `null` contentDescription discipline to evaluate yet. |
| TalkBack sweep | Not performed — Robolectric renders pixels, not the accessibility tree; per-screen semantic traversal could not be automated in this environment. |

**Assessment:** the foundations (touch targets, contentDescription discipline, text scaling,
dark mode) are better than typical migration output. The failures are concentrated in
reduced motion, low-contrast tertiary text/borders, and keyboard/focus support.

---

## 16. Responsive Baseline

### Configurations actually inspected
| Config | Method | Result |
|---|---|---|
| Standard phone (411×891dp, xxhdpi) | Robolectric | 59 renders; primary baseline |
| Tecno Spark 10 Pro geometry (360×780dp, mdpi) | Robolectric | 4 renders (`80`–`83`); no clipping/overflow observed |
| Large text (Extra Large, 1.28×) | Robolectric | Home (`50`) and Notes (`51`); no clipping observed |
| Dark mode | Robolectric | Home (`01b`), App shell (`66`) |
| Small phone (<360dp) | **Not inspected** — no qualifier render was produced within the time budget |
| Landscape / tablet | **Not inspected** — app is single-column, portrait-only by intent; no `sw*dp` resources exist to inspect |
| System font scale (Android setting) | **Partially** — the app's own scaling was tested, but the OS-level `fontScale` interaction was not separately rendered |

### Findings from the inspected configurations
- **No hard overflow or clipping** was observed at 360×780 or with Extra Large text on the
  two screens rendered. This is a genuine strength relative to typical Compose migrations.
- **`screenMaxWidth = 480.dp` exists in `Tokens.kt` but is referenced by no screen** — so
  there is no max-width containment. On a wide/landscape device, every screen would stretch
  full-bleed with 16–20dp gutters and no content column. This is the main untested risk.
- **No `values-night/`, `values-sw600dp/`, `values-land/` resource qualifiers exist**, and
  no `dimens.xml`/`colors.xml`. All dimension and colour decisions live in Kotlin, so there
  is no resource-level responsive adaptation at all.
- **Fixed-size assumptions observed:** avatar sizes from tokens (`avatarLg`), header height
  token, 58dp nav button, 273px Home action cards, fixed `100.dp` min multiline input,
  24–32dp nav pill insets. Most are token-driven and safe; the Home 2×2 grid is the most
  likely to feel cramped on very small devices once real content fills the cards.
- **Portrait-only intent** is consistent with the legacy product decision; the manifest does
  not lock orientation, so rotation is technically allowed but untested and unsupported.

**Assessment:** the app is safe on the primary target geometry and tolerates large text, but
responsiveness is essentially untested beyond one phone size, and `screenMaxWidth` being
dead code means there is no protection for wide screens.

---

## 17. Performance Baseline

Findings are from static analysis of the rendering paths plus the harness run. No profiling
tooling (Macrobenchmark, Layout Inspector, systrace) was used — flagged as a Phase-0 limit.

| Area | Observation |
|---|---|
| **Main-thread DB writes** | `kotlinx.coroutines.runBlocking { … }` is used inside Compose route lambdas and click handlers throughout `AppRouter.kt` and feature screens (create/update/delete). This blocks the UI thread for the duration of each write — the single most significant performance/correctness smell found. |
| **Recomposition** | Screens read entire lists via `collectAsState(initial = emptyList())` on Room `Flow`s; listing screens are `LazyColumn`-based (good). However `MoodHome` rebuilds its mood vocabulary list inline, and several screens construct lists/`remember`-less data inside composables (e.g. `YukiActions` builds its `listOf(...)` every recomposition). |
| **Animations** | Yuki's animations use translation/alpha/scale/rotation — compositor-friendly. `rememberInfiniteTransition` runs continuously while Yuki is composed, even when idle, so Yuki holds a perpetual frame loop while on screen (battery cost on the Spark 10 Pro). Reduced motion is not honoured (see §15). |
| **Nav transitions** | Uniform `tween(300)` fade + slide on every route. Cheap, but applied even to deep/duplicate navigations. |
| **Assets** | Currently near-zero cost: **no** drawable/bitmap is loaded in UI (only the launcher icon), and 24 bundled assets are dead (never decoded). So there is no image memory/decoding pressure today. |
| **Large images** | None rendered. `MediaStorage` exists service-side but the UI shows placeholders, so photos are never decoded into the UI. When Phase 6+ wires real photos, decode/sampling will need attention — this is the main future performance risk (explicitly relevant to the Tecno target). |
| **Scrolling** | `LazyColumn` used for all lists. No nested-scroll or unbounded `Column` inside scrollable heavy lists observed in the inspected screens. |
| **Startup** | `MainActivity` constructs all services (Room, DataStore, encrypted prefs, WorkManager) eagerly in `onCreate` before the first composition, showing a `ThLoadingState("Loading...")` gate. This is a simple and safe pattern, but it is a serial startup with no lazy/parallel initialisation. |
| **Jank observed** | None detectable from static renders (Robolectric does not measure frame timing); cannot be asserted either way. |
| **APK weight** | 24 unused assets (20 SVG rose-lily, 3 branding SVG, 1 onboarding SVG) + `yuki-cat.svg` ship in the APK unreferenced — dead payload rather than a runtime cost. |

**No performance refactoring was performed**, per Phase 0 scope. The `runBlocking` pattern
and the always-on Yuki frame loop are the two items that later phases should treat as
correctness-adjacent, not merely cosmetic.

---
## 18. Phase 0 Findings

Ordered by product impact, not by ease of fix.

**F1 — The theme has a two-layer split, and the raw Material layer leaks.**
A bespoke TwoHearts token/component layer exists, but screens also use raw Material 3
directly. Result: stock M3 `surfaceContainerHighest` `#E6E0E9` (cool lavender-grey) fills
cards on **9 screens** (up to 31.9% of the Notes screen's pixels), and stock M3 blue
`#1889E6` plus yellow `#FEE230` appear on Us and Important Dates. This is the most concrete,
most visible break in the identity. (§11 C1, §8.1)

**F2 — Brand imagery is entirely absent.**
`BrandLogo` renders the literal text "TwoHearts" or "♥". All 24 bundled brand/decoration/
onboarding assets have **zero** references. There is no image loader in the app. The app
currently ships no visual identity beyond its colour palette. (§8.6, §12)

**F3 — The feedback layer is missing.**
Toasts are defined but never mounted, the shell's snackbar host is a stub, and screens have
**0 error states** and **0 loading states**. Every save/update/delete completes silently.
(§9.4)

**F4 — All date/time input is free text.**
7 fields across 5 features require hand-typing `yyyy-mm-dd`; no picker exists anywhere.
(§9.3)

**F5 — Emoji is used as UI chrome.**
47 emoji glyphs serve as action icons, category badges, dropzones, brand mark, and status
glyphs, contradicting a central Material icon set that is used inconsistently in the very
same screens. (§8.7)

**F6 — Home and Us underfill the screen and have no hierarchy.**
On the real Tecno Spark 10 Pro geometry, Home content ends at 48.5% and Us at 42.3% of the
viewport; all action cards are equal weight. Home surfaces only 4 of ~12 features. (§9.1, §10.3)

**F7 — The token system is widely bypassed.**
419 hardcoded dp values vs 85 token references; 117 raw colour literals; 31 hardcoded font
sizes (including 8sp/10sp); 60 hardcoded radii across three corner languages. (§11 C5–C7, C10)

**F8 — Core screens contain unimplemented placeholders.**
The period calendar is an explicit placeholder; Memories/Places photo galleries and drop
zones are text placeholders. (§10.6, §10.10, §10.12)

**F9 — Profile and Relationship settings do not save.**
Both forms are present with a `// TODO` and discard input on leaving. (§9.3, §10.15)

**F10 — Reduced motion is a non-functional setting.**
`reduceMotion` persists but is read by no animation path; Yuki animates continuously.
(§8.8, §15)

**F11 — Yuki renders a placeholder emoji cat, not the 22 authoritative PNGs.**
All animation, state, decay, mood, and action systems are real; only the character art is
missing. The 22 PNGs are not in the Android module at all. (§14)

**F12 — Main-thread database writes.**
`runBlocking` is used inside Compose event handlers across the router and feature screens.
(§17)

**F13 — The app has essentially no automated test safety net.**
The only test file in the repository is the Phase 0 render harness; `androidTest/` is empty. (§4)

**F14 — Navigation chrome appears on deep screens and back is a no-op at Home.**
The bottom bar sits outside the `NavHost`, so it shows on create/edit/detail screens; back
at Home does nothing. (§7, §10.21)

**Strengths to preserve (do not regress while fixing the above):**
the warm burgundy/cream palette; `CouplePair` + relationship counter; the floating pill
navigation with the elevated centre button; consistent delete confirmation; 44dp+ touch
targets and disciplined `contentDescription` coverage; working text scaling and dark mode;
solid offline-first architecture and the Vault's separate-PIN privacy model.

---

## 19. Phase 0 → Future Phase Mapping

Per the directive's own phase structure (`MASTER-UI-UX-DIRECTIVE.md` lines 1979–2532).

| Finding | Target phase |
|---|---|
| F1 two-layer theme split / M3 leaks (C1) | **Phase 1** (Global Visual Language) |
| F3 feedback layer (toasts/errors/loading) | **Phase 1** primitives, wired per-feature from **Phase 3–12**, consolidated in **Phase 14** |
| F7 token bypass (spacing/colour/type/radii) | **Phase 1** |
| F2 brand imagery / asset wiring | **Phase 1** (visual language) + brand placement in **Phase 2** |
| F5 emoji-as-chrome → real iconography | **Phase 1** (iconography) + per-feature in **Phase 3–12** |
| F14 nav chrome on deep screens, back behaviour, transitions | **Phase 2** (App Shell and Navigation UX) |
| Onboarding friction (date picker, art) | **Phase 3** |
| F6 Home underfill / hierarchy / feature discovery | **Phase 4** |
| Us hub hierarchy | **Phase 5** |
| Memories photo gallery placeholder | **Phase 6** |
| Notes styling (paper/love-letter) | **Phase 7** |
| Timeline visual language (no spine/chapters) | **Phase 8** |
| Reminders/Important Dates grouping, countdowns, pickers | **Phase 9** |
| Places galleries + Mood iconography | **Phase 10** |
| Period calendar placeholder, cycle presentation | **Phase 11** |
| F9 profile/relationship saves, settings search/shortcuts | **Phase 12** |
| F11 Yuki placeholder character, asset integration | **Phase 13** |
| C3/C4/C8/C13 duplicated idioms, off-system headers | **Phase 14** |
| Emotional empty states, celebration, micro-interactions | **Phase 15** |
| F10 reduced motion, contrast, focus/keyboard, small text | **Phase 16** |
| F12 runBlocking, Yuki frame loop, future image decode | **Phase 17** |
| Whole-product visual QA | **Phase 18** |
| F13 test safety net (device/build validation) | **Phase 19** |
| Final consolidation | **Phase 20** |
| Vault lock/home emptiness | Phase 11/12 adjacent (privacy surfaces) — flag, do not schedule here |

No work was performed for any of these phases during Phase 0.

---

## 20. Out-of-Scope Items (intentionally not changed)

Explicitly **not** done in Phase 0:

- No redesign of any screen, component, or layout.
- No change to the colour palette, typography, spacing, radii, or elevation tokens.
- No change to navigation structure, routes, or transitions.
- No replacement of the `BrandLogo` placeholder or wiring of any asset.
- No new icon set; no emoji removal.
- No date/time picker implementation.
- No toast wiring, error states, or loading states added.
- No accessibility remediation (contrast, reduced motion, focus) beyond measurement.
- No responsive layout work; `screenMaxWidth` left unused.
- No performance refactoring; `runBlocking` left in place.
- No Yuki changes: no asset replacement, no gameplay, no character rendering change.
- No schema, DAO, repository, service, or navigation-architecture change.
- No V2/online functionality of any kind.
- No migration restart; no `Migration/Stage-16/` created.
- No changes to `Archive/` or `Yuki Assets/`.

The only source change in Phase 0 is the **test-only render harness** (§22).

---

## 21. Visual Inspection Limitations

Recorded honestly so no finding is overstated.

1. **No emulator, no device.** `/dev/kvm` is unavailable and no Android device is attached,
   so the built APK could not be launched and interacted with on a running system.
   Consequence: **no findings about real interaction dynamics** — tap responsiveness,
   gesture behaviour, real keyboard behaviour, system back-gesture, real scrolling momentum,
   and true frame timing were *not* observed.
2. **Robolectric rendering, not a device.** The 59 renders are real Compose output drawn
   through Skia at native graphics mode, and are faithful for layout, colour, typography,
   and composition. They are **not** a substitute for device verification of animation
   smoothness, font fallback on the target device, or hardware-specific rendering.
3. **Static renders only.** No animation was captured as video; motion findings come from
   source analysis of animation code, not from observing motion. The directive's Phase 19
   (Real-Device Validation) remains the correct place to confirm motion.
4. **Accessibility tree not inspected.** Robolectric renders pixels; TalkBack traversal,
   focus order, and announcement behaviour were assessed from source and could not be
   exercised.
5. **No profiling.** No Macrobenchmark, Layout Inspector, or tracing; performance findings
   are structural (source-derived), and jank is explicitly not asserted.
6. **Screen coverage.** 21 screen groups were rendered, including onboarding, all primary
   destinations, all feature homes/lists/details/editors, settings, vault, Yuki, dialogs,
   large text, dark mode, and the target geometry. Not rendered: a sub-360dp device, any
   landscape configuration, and a small number of deep route variants.
7. **Seeded-data runs.** Two renders used seeded relationship data (`67`, `68`); most used
   empty repositories, so "populated list" appearance was inspected but not exhaustively.

---

## 22. Code Changes & Technical Notes

### 22.1 The Phase 0 render harness (test-only)
`app/src/test/java/com/twohearts/app/Phase0RenderHarness.kt` — a Robolectric +
`GraphicsMode.NATIVE` JUnit test that composes real TwoHearts screens and writes PNGs to
`app/build/phase0-screens/`. It is **test-only**: it is in `src/test`, is not part of the
APK, and touches no production code. Added purely to satisfy the requirement to actually
see the UI. Renders: 59 at 411×891dp/xxhdpi plus 4 at 360×780dp/mdpi. `app/build/` is
git-ignored, so the PNGs themselves are not committed (curated copies are in
`UI-UX/Phase-0/evidence/`).

### 22.2 Pre-existing compile repairs (required to build at all)
The repository **did not compile at HEAD**. Restoring a buildable state was a prerequisite
for any visual inspection. These repairs are compile-only and behaviour-preserving; they
change no layout, colour, type, navigation, or design decision:

| File | Nature of repair |
|---|---|
| `ui/screens/settings/ProfileSettingsScreen.kt` | Removed an unresolved import (`theme.Burgundy`, which does not exist); replaced with token/`ColorScheme` access |
| `ui/screens/settings/RelationshipSettingsScreen.kt` | Same unresolved-import class of fix |
| `ui/screens/settings/AppearanceSettingsScreen.kt` | Called a suspend API from a non-suspend lambda; corrected the call site |
| `ui/screens/settings/ImportScreen.kt` | Repository contract mismatch corrected to the real `BaseRepository` API |
| `ui/navigation/AppRouter.kt` | `repository.update(entity.copy(...))` corrected to the real `update(id, mapOf(...))` contract across affected repositories |
| `ui/screens/period/PeriodHistoryScreen.kt` | Compile-level correction consistent with the repository API |
| `ui/screens/period/PeriodSettingsScreen.kt` | Compile-level correction consistent with the repository API |
| `ui/screens/yuki/YukiCharacter.kt` | Compile-level correction only; **no visual change to Yuki** |
| `ui/screens/yuki/YukiScreen.kt` | Import/call-site correction only |
| `MainActivity.kt` | Wiring correction required by the above |
| `app/build.gradle.kts` | Test support required to run the render harness (`isIncludeAndroidResources`) |

**Important:** these edits were present in the working tree before Phase 0's own work and
are recorded here so they are not mistaken for redesign. They are **not** visual changes and
they do not implement any Phase 1+ recommendation.

### 22.3 Verification performed
- `./gradlew :app:assembleDebug` → **BUILD SUCCESSFUL** after §22.2 (was FAILED at HEAD).
- `./gradlew :app:testDebugUnitTest --tests Phase0RenderHarness` → **BUILD SUCCESSFUL**,
  59 PNGs produced.
- No production UI file was changed for audit convenience.
- No migration architecture was altered; no V2/online code was introduced.

---

## 23. Completion Checklist

| Requirement | Status |
|---|---|
| `UI-UX/MASTER-UI-UX-DIRECTIVE.md` read | ✅ (full, 3259 lines) |
| Migration documentation reviewed | ✅ (roadmap + Stage 0–15) |
| Current native implementation inspected | ✅ (163 Kotlin files / 20,754 LOC) |
| Actual visual inspection attempted | ✅ |
| Live/emulator/preview environment used | ✅ Robolectric native-graphics Compose rendering (emulator impossible: no KVM) |
| Major screens visually inspected | ✅ 21 screen groups / 59 renders |
| Current UX audited | ✅ §9 |
| Current visual system audited | ✅ §8 |
| Cross-app inconsistencies documented | ✅ §11 (13 patterns) |
| Accessibility baseline documented | ✅ §15 |
| Responsive baseline documented | ✅ §16 |
| Performance baseline documented | ✅ §17 |
| Yuki directive read | ✅ `Yuki Assets/Yuki_Game_Engine.md` |
| Yuki assets inspected | ✅ (22 PNGs) |
| Yuki baseline documented | ✅ §14 |
| Legacy treated as historical only | ✅ §13 |
| No redesign performed | ✅ |
| No migration restarted | ✅ |
| No Migration Stage 16 created | ✅ |
| No V2 functionality introduced | ✅ |
| Phase 0 Markdown created | ✅ this document |
| Build/checks performed | ✅ §22.3 |
| Commit created | ✅ §24 |
| Commit pushed to master | ✅ §24 |
| Working tree clean | ✅ §24 |
| Local HEAD == origin/master | ✅ §24 |
| Agent stopped after Phase 0 | ✅ |

---

## 24. Git Record

| Field | Value |
|---|---|
| Branch | `master` |
| Starting commit | `11891d748c12691ef7f9224160af4525aae4dc5d` |
| Commit messages | `fix(build): repair pre-existing compile errors blocking debug build` (buildable-state prerequisite, §22.2)<br>`docs(ui-ux): complete phase 0 reconnaissance baseline` (the Phase 0 deliverable) |
| Ending commit | see `git log -1 --format=%H` on `master` (the Phase 0 documentation commit) |
| Push target | `origin/master` |
| Final state | working tree clean, `HEAD == origin/master` |

---

## 25. Evidence Index

Curated visual evidence (downscaled) lives in `UI-UX/Phase-0/evidence/`:

| File | Surface |
|---|---|
| `01-home.png` | Home (default) |
| `01b-home-dark.png` | Home (dark) |
| `80-home-target.png` | Home at Tecno Spark 10 Pro geometry (360×780dp) |
| `02-us.png` | Us (contains the stock-M3 blue/yellow leak) |
| `05-notes-list.png` | Notes list (worst `#E6E0E9` leak, 31.9%) |
| `81-notes-target.png` | Notes at target geometry |
| `82-mood-target.png` | Mood at target geometry |
| `83-settings-target.png` | Settings at target geometry |
| `34-settings-home.png` | Settings hub |
| `41-yuki.png` | Yuki (placeholder emoji character) |
| `60-onboarding-welcome.png` | Onboarding welcome |
| `64-onboarding-complete.png` | Onboarding complete |
| `65-appshell.png` | App shell / bottom navigation |
| `66-appshell-dark.png` | App shell (dark) |
| `69-vault-locked.png` | Vault PIN gate |
| `70-vault-home.png` | Vault home |

The full 59-render set is regenerated with:
`JAVA_HOME=… ANDROID_HOME=… ./gradlew :app:testDebugUnitTest --tests "com.twohearts.app.Phase0RenderHarness"`
(output: `app/build/phase0-screens/`).

---

**End of Phase 0.** Reconnaissance complete. No redesign performed. Awaiting explicit
instruction before beginning Phase 1.

