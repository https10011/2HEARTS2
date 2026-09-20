# Phase 1 — Global Visual Language & Design System

## 1. Phase Information

| | |
|---|---|
| Phase | 1 — Global Visual Language |
| Date | 2026-09-20 |
| Branch | `master` |
| Starting commit | `7a2b75a` (`docs(ui-ux): complete phase 0 reconnaissance baseline`) |
| Device target | Tecno Spark 10 Pro class — 720×1612, ~360dp wide, Android 13+ |
| Build verified | `:app:assembleDebug` → `app-debug.apk` (23,986,633 bytes) |
| Tests | `:app:testDebugUnitTest` — 3/3 passing (`Phase0RenderHarness`) |
| Visual evidence | `UI-UX/Phase-1/evidence/` (rendered via the Robolectric harness) |

## 2. Objective

Phase 1 establishes the visual foundation — colours, typography, spacing,
shape, elevation, iconography, motion, surface treatment, and common
controls — so the product stops reading as "generic Material UI with a
burgundy colour applied".

Phase 1 deliberately does **not** redesign individual screens. Per §130 of
the directive, work belonging to later phases (Home, Notes, Yuki, etc.) was
recorded rather than performed. See §12 for the deferred list.

## 3. Authoritative References

- `UI-UX/MASTER-UI-UX-DIRECTIVE.md` — §22 Iconography, §98 Phase Roadmap,
  §130 Phase Execution Rules, §149 Documentation.
- `UI-UX/Phase-0/PHASE-0-UI-UX-RECONNAISSANCE.md` — the baseline this phase
  acts on.
- `Yuki Assets/Yuki Game Engine.md` — consulted for asset/motion constraints;
  no Yuki work performed (belongs to Phase 13).

## 4. The Central Finding

Phase 0 documented that the migrated app *has* a design system on paper —
`Tokens.kt`, `Theme.kt`, a token-driven `TwoHeartsColors` — but that the
system was **frequently bypassed at the point of use**, and in several
cases **not actually connected at all**.

Phase 1's job was therefore less "invent a visual language" and more
"**make the existing language real**". Four defects dominated:

1. **The theme was disconnected from user settings.** `MainActivity`
   hardcoded `darkMode = false` and `TextScalingLevel.DEFAULT`. The
   Appearance screen wrote to DataStore, but nothing read it back, so
   Dark mode, all four text sizes, and Reduce Motion were inert.
2. **The brand was absent.** `BrandLogo` rendered the literal string
   `"TwoHearts"` (or `"♥"`) as a placeholder. The official artwork was in
   the repo, unused.
3. **Every toast was a silent no-op.** `ToastProvider` was never mounted,
   and `LocalToastApi` falls back to a no-op — so ~dozens of
   `toast.success(...)` confirmations compiled and did nothing.
4. **Material defaults leaked through.** `dynamicColor` defaulted to
   `true`, which on Android 12+ replaces the burgundy palette with
   wallpaper-derived colours. The `ColorScheme` was also only partially
   populated, so unspecified slots fell back to stock Material purple.

## 5. Files Changed

### Modified

| File | Change |
|---|---|
| `ui/theme/Tokens.kt` | Added `inkBase`, semantic spacing (`screenGutter`, `itemGap`, `sectionGap`, `cardPadding`, `labelGap`), a unified `Duration` scale, restored `Dimensions`, added `Border`. |
| `ui/theme/Theme.kt` | Filled all M3 `ColorScheme` slots; removed non-existent `*Fixed` roles; `dynamicColor` now opt-in/default-off; added `LocalTwoHeartsMotion` + reduced-motion wiring; added settings-driven `TwoHeartsTheme` overload and `textScalingLevelFor`; wired `TwoHeartsShapes`; added `MaterialTheme.thMotion`. |
| `ui/theme/Type.kt` | Raised the readable floor (body small 13→14sp, label medium 12→13sp); separated hierarchy by *shape* (serif display vs sans UI); added `ThTextStyles` (`numeral`, `reading`, `eyebrow`). |
| `ui/components/Icons.kt` | Replaced the Material-2-era partial list with the canonical 68-icon `ThIcons` vocabulary and a single outlined stroke family. |
| `ui/components/BrandLogo.kt` | Renders the official rasterized artwork instead of a text placeholder; `LIGHT` tone tints for dark/burgundy surfaces. |
| `ui/components/Screen.kt` | `ThScreen` now applies `WindowInsets.safeDrawing` and uses `screenGutter`. |
| `ui/components/AppShell.kt` (→ navigation) | Mounts `ToastProvider`. |
| `ui/components/Toast.kt` | Info variant uses `inkBase`; viewport overlays content via its own `Box`. |
| `ui/components/Button.kt`, `Input.kt`, `StatusBanner.kt`, `ProfileAvatar.kt`, `Aliases.kt` | Token adoption; glyph/emoji leaks removed. |
| `ui/components/Switch.kt`, `IconButton.kt` | Fixed silently-dropped `label` (accessibility defect). |
| `MainActivity.kt` | Reads persisted `themeMode` / `textSize` / `reduceMotion` from `AppStateService`; resolved dark mode incl. `system`; removed duplicate service construction. |
| `services/appstate/AppStateService.kt` | Added `reduceMotion` flow, `setReduceMotion`, and reset coverage. |
| `res/mipmap-*` | Real launcher icons replacing the 167-byte placeholder stubs. |

### Added

| File | Purpose |
|---|---|
| `ui/theme/Shape.kt` | `TwoHeartsShapes` M3 shape scale. |
| `ui/components/Shadow.kt` | `Modifier.thSoftShadow` — warm, low-alpha shadow for genuinely floating surfaces. |
| `ui/components/SurfaceCard.kt` | The single card container + `ThEyebrow`, `ThCard`, `ThTintedBand`. |
| `res/drawable-nodpi/brand_logo.png`, `brand_logo_mark.png`, `ic_launcher_foreground.png` | Rasterized from the official SVGs. |
| `tools/generate-brand-assets.mjs` | Reproducible SVG → drawable/launcher pipeline. |

### Deleted

| File | Reason |
|---|---|
| `ui/components/Card.kt` | Superseded by `ThSurfaceCard`; two competing card implementations were the direct cause of inconsistent card treatment. |
| `ui/navigation/AppShell.kt` (old) | Rewritten in place to mount the toast host. |

## 6. Design Decisions

### 6.1 Brand identity must not be wallpaper-dependent
`dynamicColor` now defaults to **off**. Material You is a legitimate
default for a generic app, but for TwoHearts it replaces the burgundy
identity with colours derived from the user's wallpaper — the exact
"no longer feels like TwoHearts" failure the directive warns against. A
future phase may expose it as a deliberate opt-in; it must never be the
default.

### 6.2 Typography separates by *shape*, not only size
The migrated scale was a single sans-serif family across 15 slots, which is
why screens read as an undifferentiated grid. The system now reserves the
serif face for emotional moments (splash, onboarding titles, the
relationship counter, memory/story reading views) and the system sans for
UI chrome. Size alone separates within each family.

The readability floor was raised: body copy at 13sp was too small on a
720p-class 6.6" display. Nothing in the app is now below 12sp, and 12sp is
reserved for eyebrows and dense metadata.

### 6.3 Depth comes from borders and surface tone, not shadows
This is a warm, paper-like product. The elevation scale
(`flat / hair / raise / lift`) is deliberately shallow, most surfaces use
`hairline` borders or tonal separation, and the one warm shadow helper is
reserved for genuinely floating elements (FABs, sheets). Stock Material
shadows are cold-grey against cream and charcoal.

### 6.4 One card, one icon family
Two competing card implementations and three interchangeable icon families
(`Default`/`Filled`/`Outlined`) were the dominant sources of
cross-app inconsistency. Phase 1 collapsed cards to one container and made
`ThIcons` the single auditable vocabulary: outlined for actions and
navigation, filled reserved for destructive affordances and the
"meaningful" heart.

### 6.5 Shape vocabulary
Corners are grouped by component family (chips 6dp, fields/large controls
8dp, buttons 12dp, cards 16dp, sheets/heroes 24dp) so components remain
visibly related without everything being the same rounded rectangle.

## 7. UX Decisions

- **Safe areas.** `enableEdgeToEdge()` was active but `ThScreen` applied no
  insets, so content ran under the status bar and gesture nav bar. `ThScreen`
  now consumes `safeDrawing` itself, fixing every screen at once.
- **Feedback honesty.** Mounting `ToastProvider` restores confirmation after
  create/update/delete across the app. This is a behaviour change users will
  notice immediately and treats a class of silent failures as fixed.
- **Settings that work.** Appearance settings now take effect on the running
  app instead of requiring a restart. `textScalingLevelFor` accepts both
  `extra_large` (what the Appearance screen writes) and `extra-large`, so
  the largest setting is no longer inert.
- **Reduced motion honoured.** `LocalTwoHeartsMotion` lets screens scale or
  suppress decorative entrances; the preference is now read from settings.
- **Labelled controls.** The two primitives that accepted and dropped
  `label` now publish it to accessibility services.

## 8. Screens Affected

No screen was redesigned. Every screen is *affected* because the changes are
foundational:

| Area | Effect |
|---|---|
| All screens | Safe-area insets; settings-driven text size; shared gutter; warm background. |
| All cards | Now one container treatment (white, hairline border, no Material elevation). |
| All icon-only actions | Labelled for assistive technology via `ThIconButton`. |
| All toasts | Now actually visible. |
| Onboarding (all 6) | Real brand lockup instead of placeholder text. |
| Home, App shell | Real brand mark; dark mode now reachable. |
| Appearance settings | Controls now visibly work. |
| Dark theme (all) | Fully-specified palette instead of partial + Material purple. |

## 9. Visual QA Performed

Rendered via the existing Robolectric harness
(`app/src/test/java/com/twohearts/app/Phase0RenderHarness.kt`), which draws
real Compose trees to bitmaps at both 411×891dp and the Tecno-class
360×780dp qualifier, in light and dark, at Default and Extra Large text.

Verified:

- **Palette integrity** — sampled screenshots are dominated by cream
  `#FDF6F0` and burgundy `#6A1B2B`; no Material purple appears in any
  rendered surface.
- **Brand restoration** — the welcome and setup-complete screens now contain
  multi-colour brand artwork (~1,130 distinct colours in the logo region)
  where the placeholder previously produced a handful of text pixels.
- **Dark mode reachable** — `01b-home-dark` and `66-appshell-dark` render the
  intended dark palette.
- **Large text** — `50-home-large-text` (Extra Large, 1.28×) renders without
  clipping or overflow.
- **Target device** — `80-home-target` / `81-notes-target` at 360dp confirm
  no horizontal overflow.

Evidence: `UI-UX/Phase-1/evidence/`.

## 10. Testing Performed

- `./gradlew :app:compileDebugKotlin` — clean.
- `./gradlew :app:testDebugUnitTest` — 3/3 passing.
- `./gradlew :app:assembleDebug` — `BUILD SUCCESSFUL`, APK produced.

Note: the `*Fixed` M3 colour roles were removed after confirming Material3
**1.3.1** (the version resolved by Compose BOM `2024.12.01`) does not define
them; referencing them was a compile error caught during this phase.

## 11. Known Limitations

1. **The canonical icon set is not yet adopted.** 73 icons are still
   referenced directly as `Icons.Default.*` / `Icons.Filled.*` across ~40
   files. `ThIcons` exists and compiles, but only 4 files use it. Migrating
   call sites is mechanical but wide, and belongs with the per-screen phases
   that already touch those files.
2. **Most primitives are barely adopted.** `ThScreen` (1 call site),
   `ThHeader` (2), `ThSurfaceCard` (3), `ThSwitch` (1), `ThDivider` (1) —
   screens overwhelmingly inline their own `Scaffold` + `Card` + `Text`
   compositions. Until screens adopt the primitives, cross-app consistency
   improvements are only partially realised.
3. **Screens still hardcode design values.** 44 instances of
   `RoundedCornerShape(12.dp)`, 82 of `padding(16.dp)`, and 50 raw
   `Color(0x…)` literals remain in `ui/screens/`. The tokens exist; the
   screens do not use them yet.
4. **Yuki renders essentially empty.** The harness output for `41-yuki` is
   ~393 distinct colours — in practice a blank cream screen with a little
   text. This is a significant product finding, recorded for Phase 13;
   nothing was changed.
5. **`67-home-seeded` renders identically to `01-home`.** The harness seeds
   profiles and a couple relationship, but the rendered output is
   byte-identical to the unseeded Home. Either the seeding does not reach the
   composable or Home does not yet render relationship data. Recorded, not
   investigated — Home is Phase 4.
6. **No real-device validation.** Rendering is Robolectric-based; font
   rasterisation, real insets and OEM behaviour on the Tecno target remain
   unverified. §19 (Real-Device Validation) is the appropriate home.
7. **Tonal/separation cues are still under-used.** The `TintedBand`, warm
   divider and elevation scale exist, but screens rarely differentiate
   chapters or grouped regions yet. That is per-screen work (Phases 4–12).

## 12. Deferred to Later Phases

Recorded per §152; **not** performed here.

| Finding | Belongs to |
|---|---|
| Home does not reflect seeded relationship data; dry visual hierarchy | Phase 4 |
| Bottom-nav / header / brand placement evaluation | Phase 2 |
| Onboarding first-impression quality; brand pacing | Phase 3 |
| Per-screen inline card/header/button composition → primitives | Phases 4–12 (per feature) |
| Hardcoded radii/padding/colour literals in `ui/screens/` | Phases 4–12, consolidated in 14 |
| `ThIcons` call-site migration | Phase 14 (Cross-App Consistency) |
| Yuki blank render, asset pipeline, animation | Phase 13 |
| Full accessibility sweep beyond the two label fixes | Phase 16 |
| Reduced-motion coverage across all animations | Phase 15 / 16 |
| Real-device validation on Tecno Spark 10 Pro | Phase 19 |

## 13. Out of Scope (Deliberately Not Changed)

- No screen was redesigned or restructured.
- No feature, repository, service or navigation route was altered.
- No migration stage was created; `Migration/` was not touched.
- No V2 / online / cloud functionality was introduced.
- `Archive/` was not modified and its React UI was not reused.
- No Yuki asset, character, gameplay or hub change was made.
- `dynamicColor` was disabled rather than deleted, so it remains available
  as a future opt-in.

## 14. Validation Checklist

- [x] `assembleDebug` succeeds; APK produced
- [x] Unit tests pass (3/3)
- [x] App re-rendered after changes; screenshots captured
- [x] No production behaviour changed except the defects deliberately fixed
- [x] No migration architecture altered
- [x] No V2/online functionality introduced
- [x] No Migration Stage 16 created
- [x] No unrelated or generated files added (`tools/node_modules/` is covered
      by the existing `node_modules/` ignore rule)
- [x] Documentation written