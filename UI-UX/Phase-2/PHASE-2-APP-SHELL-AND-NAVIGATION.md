# Phase 2 — App Shell & Navigation UX

## 1. Phase Information

| | |
|---|---|
| Phase | 2 — App Shell & Navigation UX |
| Date | 2026-09-21 |
| Branch | `master` |
| Starting commit | `63b90cc` (`feat(ui-ux): establish global visual language and design system foundation`) |
| Ending commit | `28fa14b` — `feat(ui-ux): refine app shell and navigation experience`. See §16 |
| Device target | Tecno Spark 10 Pro class — ~360dp wide, Android 13+ |
| Build verified | `:app:assembleDebug --offline` → `BUILD SUCCESSFUL` → `app-debug.apk` (24,024,915 bytes) |
| Tests | `:app:testDebugUnitTest` — 22/22 passing |
| Visual evidence | `UI-UX/Phase-2/evidence/` (Robolectric native-graphics renders) |

## 2. Objective

Phase 2 makes the application shell and its navigation read as **one
coherent, intentional product entrance** rather than a generic Android
container holding screens.

The question the phase answers is the directive's:

> When someone moves through TwoHearts, does the application itself feel
> intentional, intimate, polished, and unmistakably like TwoHearts?

The phase touches the *shell*: the bottom navigation, the top-level
navigation model, headers, back behaviour, transitions, insets, branding
placement, and shell-level feedback. It deliberately does not redesign the
content of any destination — those belong to later phases (§12).

## 3. Authoritative References

- `UI-UX/MASTER-UI-UX-DIRECTIVE.md` — §22 Iconography, §98 Phase Roadmap,
  §130 Phase Execution Rules, §149 Documentation; the floating pill-style
  bottom navigation direction.
- `UI-UX/Phase-0/PHASE-0-UI-UX-RECONNAISSANCE.md` — the navigation and shell
  baseline, findings **C3** (two back-navigation systems) and the
  accessibility findings on dropped labels.
- `UI-UX/Phase-1/PHASE-1-GLOBAL-VISUAL-LANGUAGE.md` — the active visual
  foundation: tokens, `ThIcons`, `ThHeader`, motion, safe areas.
- `Migration/` — the Kotlin/Compose navigation architecture as migrated.
- `Yuki Assets/Yuki Game Engine.md` — read for the Yuki navigation boundary;
  no Yuki redesign performed (§12).

## 4. Existing Navigation Baseline

Recorded before any change, from source inspection plus the Phase 0 report.

### 4.1 Architecture

Single-activity Compose application:

- `MainActivity` → `enableEdgeToEdge()` → `TwoHeartsTheme` → `AppRouter`.
- `AppRouter` branches on onboarding state: `OnboardingGate` when setup is
  incomplete, otherwise the `AppShell` + `NavHost`.
- `NavHost` start destination is `RoutePath.APP_HOME`; routes are declared in
  `RoutePath` and consumed as string routes with arguments.
- `NavConfig` is the single navigation vocabulary: id, route, label, icon
  (from the Phase 1 `ThIcons` set), and a `center` flag.
- Five primary destinations: `Home`, `Notifications`, `Us` (centre),
  `Notes`, `More`. Secondary areas hang off them: Memories, Timeline,
  Reminders, Important Dates, Places, Mood, Period, Settings, Vault, Search,
  About, Yuki.

### 4.2 What the shell did before Phase 2

| Area | Baseline behaviour | Problem |
|---|---|---|
| Bar placement | Bar lived in `Scaffold`'s `bottomBar` slot | `Scaffold` reserved a full-width strip of page colour beneath the pill, so the "floating" surface rested on its own opaque band and never actually floated over content |
| Bar visibility | Rendered outside the `NavHost`, on every route | Showed over detail, editor and modal surfaces — including create flows, where the reference screens show no bar |
| Bar height | 28dp-radius `Surface`, fixed height | Read as a stock Material `NavigationBar` inside a rounded box; the centred brand button clipped against the fixed height |
| Active state | Tint change only | Failed the directive's "do not rely solely on colour"; the brand mark was drawn burgundy-on-burgundy and effectively vanished when selected |
| Label fitting | Fixed 12sp labels in five equal columns | "Notifications" exceeded its ~62dp column at 360dp and rendered into its neighbours |
| Inset handling | Bar applied no window insets; relied on `Scaffold` | Bar sat inside the system gesture area, ~9dp from the physical edge |
| System back | Unconditional `BackHandler` whose callback no-op'd at the root | Pressing back on a top-level area did nothing; the user could not leave the app the way every other Android app allows |
| Back affordance | Each screen decided for itself | Several primary screens drew a back arrow with nothing to go back to |
| Transitions | One transition for every movement: 300ms fade + small vertical slide, literal `300`s | Every movement looked identical, carried no information about what happened, ignored the motion tokens, and ignored reduce-motion entirely |
| Headers | Two parallel systems (`ThHeader` and stock Material `TopAppBar`) | Phase 0 finding **C3**; the same navigation level looked like a different app depending on the screen |

## 5. Navigation Changes

### 5.1 Bottom navigation

Rebuilt as the floating TwoHearts surface the directive calls for.

- **Placement.** Drawn as an overlay aligned `BottomCenter` above the
  content, not in a `Scaffold` slot — so it genuinely floats and content
  scrolls beneath it.
- **Geometry, measured rather than guessed.** The bar insets itself by a 12dp
  horizontal margin and 8dp vertical margin, then a 4dp row padding, then
  five equal `weight(1f)` columns. Rendered geometry was verified against
  this model: at `w411dp` the measured icon-band centres are
  162 / 390 / 617.5 / 845 / 1072 px against a predicted
  161.7 / 389.1 / 616.5 / 843.9 / 1071.3 px — the middle column is dead
  centre and the spacing is even. Equal weights replaced `SpaceEvenly`,
  which sized gaps from the items' own widths and pulled the middle column
  ~3.5dp off centre.
- **Height.** Content-derived rather than guessed, then tuned toward the
  reference: the approved reference bar occupies ~68dp against its own
  1080px canvas, so the token sits at 76dp (down from 80dp) — close to the
  reference and still tall enough that nothing clips. The first iteration's
  80dp risked the directive's "navigation that dominates the content".
- **Shape.** Deliberately *not* one giant rounded rectangle: a bar with a
  pill's corner treatment, where the only pill *inside* it is the selected
  item. One rounded shape stays meaningful instead of decorative.
- **Depth.** A hairline border plus the single warm shadow helper, not a
  Material elevation. The app should read as paper, and stock Material
  shadows are cold-grey against cream.
- **Brand button.** The centre destination is the one larger target
  (~44dp circle, comfortably over the 44dp minimum, inside a ~66dp column
  that is now the full touch target). It draws the official `BrandLogo`
  mark via the Phase 1 centralized branding implementation — no logo was
  redrawn, replaced with text, emoji, or a placeholder.

### 5.2 Active state

Selection carries **four independent signals**, so it never depends on colour
alone and survives dark mode and a colour-blind reading:

1. a tonal capsule behind the glyph,
2. a filled dot beneath the label (the shape signal — readable without any
   colour perception),
3. the glyph tint,
4. the label tint.

The centre brand destination signals selection by the field darkening *and*
by a blush ring, with the mark always drawn in the light tone so it stays
visible on both the selected and unselected field — the exact
burgundy-on-burgundy defect Phase 0 found.

**Contrast measurements** (WCAG relative-luminance ratio):

| Element | Light | Dark |
|---|---|---|
| Inactive label (`textSecondary`) on bar surface | 6.3:1 | 8.55:1 |
| Active label (burgundy) on bar surface | 11.64:1 | — |

Both exceed the 4.5:1 requirement for text.

### 5.3 Unselected state

Unselected items keep a deliberate hierarchy — active → available →
background. They use the semantic `textSecondary` token, which measures
6.3:1 in light and 8.55:1 in dark: discoverable, well clear of low-contrast
territory, but visibly quieter than the burgundy active destination. The
shape signal scales down (dot 1.0 → 0.5) and the capsule background fades,
so the eye reads a clear active → available → background ladder rather than
five items at equal emphasis.

### 5.4 Top-level headers

The directive asks that headers "should not all become identical if their
navigation role differs". Phase 2 makes the role **derived, not declared**:
`ShellSurfaces.roleOf(route)` classifies every route as `ROOT`, `DETAIL` or
`MODAL`, and `ThHeader` adopts the matching treatment.

| Role | Surface | Title style | Back |
|---|---|---|---|
| `ROOT` — one of the five primary areas | Transparent (content runs under it) | `titleLarge` | Never |
| `DETAIL` — a step down into one thing | `surface` | `titleMedium` | Yes |
| `MODAL` — a task in progress (add/edit/log/new) | `surfaceWarm` | `titleMedium` | Yes |

Because the role comes from the route, a screen cannot forget to declare it,
and a newly added secondary screen gets the correct treatment automatically.

Head-bar behaviour was also corrected: several primary screens (Notes,
Notifications, More) were routed with a `popBackStack` callback and so drew a
back arrow at the top level, where there is nothing to go back to. That is
now suppressed centrally from the shell role, so no call site can opt back
into the wrong behaviour by accident — and the call sites needed no change.
Oversized generic Material app bars are gone: the header is a 56dp row, not a
stock `TopAppBar`.

### 5.5 Back behaviour

- **One source of truth.** "Can we go back" is now derived once, from
  `navController.previousBackStackEntry != null`, and feeds both the system
  `BackHandler` and the header's back affordance.
- **System back works at the root.** The previous unconditional `BackHandler`
  no-op'd at the root, so back could not leave the app. It is now only
  installed when there is somewhere to go back to, restoring the platform
  default at a top-level area while keeping the in-app stack correct
  everywhere else.
- **Consistent in-app back.** `Header`/`ThTopBar` route through the same
  `ThIconButton` with a single "Go back" label, so back looks and announces
  the same everywhere. Nested screens pop correctly; dialogs and sheets close
  on their own `onDismissRequest`; onboarding behaviour is untouched.
- **No re-push.** Selecting the destination already showing no longer runs
  the full `navigate()` dance. Previously it did, animation included, which
  made the bar feel unresponsive — the screen appeared to reload for no
  reason.

### 5.6 Navigation transitions

The migrated router applied one transition to every movement. It now has
three, matching the three kinds of movement the product actually has:

| Kind | When | Behaviour |
|---|---|---|
| `LATERAL` | Moving between the five primary areas | Short cross-fade — a peer switch at the same altitude, no direction implied |
| `PUSH` | Stepping down into a detail, editor or modal | Content enters from the trailing edge; popped details slide back toward it, so back is the mirror of the step taken |
| `ROOT` | The start destination | Plain fade — there is no "from" to be relative to |

- Classification is derived from `NavConfig.isPrimaryRoute`, not hardcoded
  per route.
- **All durations come from `TwoHeartsTokens.Duration`**; the previous
  literal `300`s are gone, so the motion system is the single place
  durations live. Nothing exceeds 240ms.
- **Reduced motion is honoured.** The preference previously had no effect on
  navigation at all; every transition now collapses to a zero-duration
  cross-fade when `motion.reduced` is set.
- The arbitrary vertical slide was removed. TwoHearts navigates horizontally
  in the user's mental model (pushed detail, back), so a vertical offset read
  as a glitch rather than as direction.

### 5.7 Safe areas

- The shell owns the window insets for the whole content region:
  `windowInsetsPadding(WindowInsets.safeDrawing)` then
  `consumeWindowInsets(WindowInsets.safeDrawing)`, then the navigation
  reserve as padding. Screens still request their own insets, but the request
  is already satisfied — which is what stops the layout from double-padding
  under the status bar.
- The bottom bar owns `WindowInsets.navigationBars` padding directly rather
  than relying on the `Scaffold`, so it clears gesture/navigation-bar areas
  on every gesture mode.
- Verified at both geometries: at 360×780 the bar's lowest ink sits at y=762
  of a 780px screen with ink rows 702–762, and no ink touches the screen
  edge — no clipping, no overlap with system controls, and no awkward excess
  padding.

### 5.8 Content ↔ navigation relationship

The bar **floats over** the content rather than sitting in reserved space,
and the content is given a matching bottom inset (`ShellLayout`) so its last
row can still be scrolled clear of the pill. On non-primary surfaces the
inset is zero — a detail or editor screen is full-bleed, and leaving a gap
where a bar used to be would read as a layout bug. The bar consumes 76dp plus
its own margins, which leaves the destination content ample breathing room
rather than the cramped result the directive warns about.

FAB clearance was checked against the new floating bar: on Notes the FAB
bottom sits at y=2324 while the bar's top edge is at y≈2415, so the primary
action clears the navigation surface comfortably.

### 5.9 Branding

Navigation uses the official TwoHearts mark through the centralized Phase 1
branding implementation (`BrandLogo`, `BrandLogoVariant.MARK`,
`BrandLogoTone.LIGHT`). Nothing was redrawn, no text or emoji substitute was
introduced. The mark is visually balanced against the bar: a 28dp mark inside
a 44dp field, which measured against the approved references is roughly 4% of
screen width — the reference's own proportion. An earlier 58dp bubble read as
an oversized button rather than a quiet brand anchor.

The top-level header uses the official brand lockup on Home; secondary and
modal headers use the title hierarchy instead, so the brand appears where it
carries meaning rather than on every bar.

### 5.10 Modal navigation treatment

Shell-level presentation only. Dialogs and sheets continue to use the Phase 1
visual primitives, and modal surfaces (`AppearanceSettings`, vault editor,
`Import`, `PeriodLog`, and every `/add`, `/edit`, `/log`, `/new` route) are
classified `MODAL` by the shell, which gives them the warm surface and
`titleMedium` treatment that distinguishes "a task in progress" from "a place
to be". No individual dialog in the application was redesigned.

## 6. Design Decisions

### 6.1 Role is derived from the route, never declared by the screen
Every decision this phase makes — bar visibility, back affordance, header
surface, title weight, transition kind — comes from a single classification of
the current route. A screen opts into nothing and can therefore get nothing
wrong. This is why the Notes/Notifications/More back-arrow defect could be
fixed centrally without touching those call sites.

### 6.2 Selection must survive without colour
Four independent signals (§5.2). The dot is the important one: it is a shape
change, so the active destination remains identifiable under a colour-blind
simulation, in dark mode, and at a glance. The directive's rule is treated as
a hard constraint rather than a preference.

### 6.3 Exactly one pill
The directive warns against both "excessive pill geometry" and "a giant
rounded rectangle containing five generic Material icons". The bar keeps a
pill's corner treatment, and the only pill inside it is the selected item's
capsule — one rounded shape that still means something.

### 6.4 The bar floats, and the content is told about it
Floating navigation is only honest if the content can scroll clear of it.
The bar moved out of the reserved `Scaffold` strip and into an overlay, and
`ShellLayout` computes the matching inset (76dp + margins + one comfortable
step, and zero off primary routes).

### 6.5 One movement per meaning
Three movement kinds instead of one (§5.6) because movement carries
information: a lateral peer switch is not the same thing as a step down into
a detail, and a transition that says nothing is decoration.

### 6.6 Durations and motion live in the token system
Every duration comes from `TwoHeartsTokens.Duration`, and reduced motion is
read from `LocalTwoHeartsMotion`. A literal duration in navigation is a bug,
because it is a place the user's preference cannot reach.

### 6.7 Labels must fit, and fitting must not shrink type
Five equal columns leave ~62dp on a 360dp screen, which "Notifications" at
the 12sp readability floor cannot fit. The bar measures its own width and
uses each item's full or short label accordingly, rather than reducing the
type or letting labels run into their neighbours. The full name is still what
assistive technology announces, so the abbreviation is visual only.

## 7. Accessibility

- **Meaningful semantics.** Each navigation item publishes a
  `contentDescription`, exposes `selected`, and declares `Role.Tab`, so the
  selected state is announced rather than being a visual-only property.
- **Selection is exposed, not just drawn.** `selected = isActive` is set on
  both the standard items and the centre brand button.
- **Labels are not dropped.** The Phase 1 finding that accessibility labels
  had been silently dropped is respected: the back affordance goes through
  `ThIconButton`, which merges the label onto the control itself and is the
  only place that knows how to size and label an icon-only control. Icons
  inside the bar pass `contentDescription = null` so each destination
  announces once, not twice.
- **Touch targets.** Standard items occupy full ~62–76dp columns and the
  centre brand button's target is now the whole column (`fillMaxWidth`), not
  just the 44dp circle. Every target is at or above the 44dp minimum. This
  was a real improvement made during iteration: `widthIn(min = 44dp)` had
  left the most important destination's target at the circle's own width.
- **Contrast.** Measured, and documented in §5.2.
- **Reduced motion.** Every navigation transition collapses to an
  instantaneous cross-fade, and the press-scale animations are suppressed.
- **Text does not clip.** The centre label uses `TextEllipsis` rather than a
  hard clip: if a label ever did overflow, an ellipsis degrades honestly
  where a clip silently cuts a letter in half.
- **Enlarged text works.** See §8.

## 8. Responsive Validation

Rendered and inspected at:

| Geometry | Text size | Result |
|---|---|---|
| `w411dp-h891dp-xxhdpi` (reference modern phone) | Default | Bar centred and evenly spaced; all five labels fit; no clipping |
| `w411dp-h891dp-xxhdpi` | Extra Large | All five labels fit; centre column remains centred; no overflow |
| `w360dp-h780dp-mdpi` (Tecno Spark 10 Pro class) | Default | Bar fits with even columns; "Notifications" uses its short label and no longer overruns its column |
| `w360dp-h780dp-mdpi` | Extra Large | Nav ink rows 702–762 of 780; nothing touches the screen edge; touch targets preserved |

Checks performed at each: navigation width, column spacing, icon spacing,
text clipping, title clipping, top-bar balance, bottom inset, touch targets,
and narrow-screen behaviour. The bar's columns were verified mathematically
against rendered output (§5.1) rather than eyeballed on one screenshot.

Narrow-device behaviour was not treated as a degraded case: the bar measures
its own width and adapts its label strategy, so 360dp is a first-class
geometry rather than the reference render squeezed.

## 9. Text Size / Accessibility Validation

- **Default** and **Extra Large** were both rendered on both geometries.
- No layout overflow was found at Extra Large on either geometry; the bar
  grows from its content (76dp is a minimum, not a fixed height), so the
  centre brand column — the tallest — is never cropped by its own container.
  This specific defect was present in the first iteration and fixed (§10).
- Overflow is not solved by shrinking text: type stays at the readability
  floor and the label strategy adapts instead.
- Touch targets remain at or above 44dp at every text size.
- Decorative elements (the selection dot, the capsule) are the things that
  compress first, before readability is touched.

## 10. Visual Validation

### 10.1 Rendering method

Actual Compose rendered through the existing Phase 0 Robolectric harness
approach, extended as `Phase2RenderHarness`: real Compose, real Skia
rendering via Robolectric native graphics (`@GraphicsMode(NATIVE)`), real
PNGs written to `build/phase2-shell/`. Nothing in this phase was validated
from source inspection alone.

Source inspection is insufficient for this work by definition — the defects
being fixed were *geometry*, and geometry is what a render shows.

### 10.2 Screens and states rendered

- Each of the five primary areas with itself selected, so the active
  indicator can be compared across all five positions.
- A detail surface with the bar hidden and a back affordance present
  (`p2-10-memory-detail`).
- A secondary list (`p2-11-memories-list`), Settings at 360dp
  (`p2-42-settings-360`).
- Dark mode across Home, Notes, Us and More.
- Extra Large text on both geometries.
- The Tecno-class 360dp geometry.
- Seeded relationship data, so the header shows real names rather than empty
  state.

### 10.3 Evidence

Curated in `UI-UX/Phase-2/evidence/` — meaningful states rather than hundreds
of near-identical screenshots:

| File | State |
|---|---|
| `p2-01-home.png` | Light shell, Home selected |
| `p2-02-notes.png` | Light shell, Notes selected (another active position) |
| `p2-04-notifications.png` | Light shell, Notifications selected (the label that used to overflow) |
| `p2-05-more.png` | Light shell, More selected |
| `p2-10-memory-detail.png` | Detail surface: bar hidden, back affordance present |
| `p2-20-home-dark.png` | Dark shell |
| `p2-22-us-dark.png` | Dark shell, centre brand destination |
| `p2-40-home-360.png` | Narrow Tecno-class geometry |
| `p2-50-home-360-xl.png` | Narrow geometry at Extra Large text |
| `p2-42-settings-360.png` | Secondary screen at narrow geometry |
| `p2-60-home-seeded.png` | Seeded relationship data in the header |

### 10.4 Problems discovered and iterations performed

Iteration was driven by inspecting rendered output, not by "the build
passes".

1. **Centre brand button clipped by the bar.** The brand circle and caption
   overflowed the 62dp bar in the first version. **Fix:** the bar height is
   now derived from its tallest column rather than a guessed constant, and
   the stacked gap was tightened from 3dp to 1dp so the items read as one
   destination rather than three loose fragments.
2. **Bar read as too tall.** At 80dp against the reference's ~68dp, the bar
   risked consuming content space. **Fix:** reduced to 76dp and the brand
   field to 44dp, which sits close to the reference proportion while keeping
   everything uncropped — verified by re-render at all four
   geometry/text-size combinations.
3. **Centre column off centre.** `SpaceEvenly` sized gaps from the items'
   own widths, so the wider centre button pulled the middle column ~3.5dp
   off. **Fix:** equal `weight(1f)` columns; verified numerically against the
   rendered output (mid column measures 617.5px against a 616.5px screen
   centre).
4. **Brand mark invisible when selected.** Burgundy artwork on a burgundy
   field — the Phase 0 defect. **Fix:** the mark always renders in the light
   tone, and selection adds a blush ring.
5. **"Notifications" overran its column** at 360dp. **Fix:** the bar measures
   its own width and selects the full or short label per item.
6. **The most important destination was the hardest to hit.** The centre
   button's touch target was only as wide as its circle. **Fix:** the target
   now fills the whole column.
7. **System back could not leave the app** at a top-level area. **Fix:**
   conditional `BackHandler` derived from the actual back stack.
8. **Back arrows on primary screens** where there was nothing to go back to.
   **Fix:** suppressed centrally by navigation role.
9. **The bar was never actually floating.** It sat on a reserved strip of
   page colour. **Fix:** overlay placement plus a matching content inset.
10. **The bar sat inside the gesture area.** **Fix:** the bar owns its
    `navigationBars` padding.
11. **Reduce-motion had no effect on navigation.** **Fix:** transitions read
    `LocalTwoHeartsMotion` and collapse under reduce-motion.
12. **The unread notification count was nearly lost in the header swap.**
    Notifications is the one top-level destination that had been built from a
    stock `TopAppBar` carrying an unread badge. Swapping it to the shell
    header initially dropped the badge — a functionality regression hiding
    inside a visual change. **Fix:** `ThHeader` grew an optional `titleBadge`
    slot, the badge rides beside the title (it counts the content below, so it
    belongs with the title rather than with the trailing actions), and the
    count is back at the call site. The other nine header migrations were
    re-audited for the same class of loss and were title+back only.

## 11. Performance

The target includes ordinary Android hardware, so the shell stays
lightweight:

- **No blur.** Everywhere. Depth is a hairline border plus the single warm
  shadow helper, both cheap.
- **No oversized images.** The brand mark is a 28dp vector, not a bitmap.
- **Bounded recomposition.** The bar measures its own width once via
  `onSizeChanged` and stores it in a single `mutableStateOf`, rather than
  measuring per item or per frame. The measured width is read only for the
  label-fits decision.
- **Animated state is minimal and tokenised.** Per-item animation is limited
  to a colour, a dot scale and a press scale; each animates only while its
  value is changing.
- **Animations are short** (≤240ms) and collapse to zero under reduce-motion
  — so a reduced-motion user performs no animation work at all.
- **The bar is drawn once per route**, not per destination, and is absent
  entirely on detail/editor/modal surfaces.
- **No unnecessary state work.** Route classification is a pure function of
  a string; nothing is observed, derived or recomputed per frame.

## 12. Deferred Work

Explicitly reserved for later phases; not attempted here:

- **Onboarding and first-launch experience** — Phase 3. Onboarding was
  *verified* (it enters the shell correctly, system back is not broken, and
  the shell does not flash an incorrect theme) but not redesigned.
- **Home** — Phase 4. Home was rendered as a representative destination and
  is unchanged apart from shell integration.
- **Memories, Notes, Timeline, Reminders, Important Dates, Places, Mood,
  Period, Settings, Search, Vault** — dedicated later phases. The shell
  surrounds them; it does not consume their redesign.
- **Yuki** — dedicated phase. Phase 2 verified only that the shell navigates
  to and from Yuki correctly and does not visually conflict with it.
- **Repository-wide icon migration.** Phase 1 deferred broad call-site
  migration to `ThIcons`, and Phase 2 did not perform one — only the
  navigation and shell surfaces it touched use the canonical set.
- **Remaining stock `TopAppBar` call sites** outside the shell. Phase 2
  introduced `ThTopBar` and migrated the shell-level surfaces; the rest of
  the app's headers are later-phase work.

## 13. Out of Scope (Deliberately Not Changed)

- No feature screens redesigned.
- No Yuki redesign.
- No onboarding redesign.
- No new navigation destinations, and no functionality removed because it
  was visually inconvenient.
- No V2 functionality: no online features, chat, cloud sync, accounts,
  remote authentication, analytics, tracking or multiplayer.
- No new settings and no new product concepts.
- **The navigation architecture was not replaced.** The router, `NavHost`,
  route structure and back stack are the migrated ones. The changes are
  presentation and behaviour, not architecture — the directive's objective is
  UX/productization, not architectural migration.
- No new dependencies were added. The work uses Compose foundation/layout
  plus `LocalDensity` and `LocalLayoutDirection`.
- Offline-first constraints remain intact: no network calls, no new
  permissions, all data still local.

## 14. Testing Performed

1. `:app:assembleDebug --offline` — **BUILD SUCCESSFUL**; `app-debug.apk` produced (24,024,915 bytes).
2. `:app:testDebugUnitTest --offline` — **22/22 tests passing**. This
   includes:
   - `Phase2ShellNavigationTest` — shell navigation invariants: route
     classification into `TOP_LEVEL` / `DETAIL` / `MODAL`, back-affordance
     suppression at the root, and back behaviour.
   - `Phase2RenderHarness` — renders the shell states listed in §10.2.
   - `Phase0RenderHarness` — the Phase 0 baseline, still passing.
3. Visual harness run and **actual output inspected** — every state in §9,
   in both themes, at both text sizes, at both geometries.
4. Light mode checked. Dark mode checked. Default and Extra Large text
   checked. Narrow Tecno-class geometry checked.
5. Safe areas checked — no clipping, no overlap with system controls.
6. Back navigation checked — system back, in-app back, nested screens.
7. Selected/unselected navigation checked across all five destinations.
8. Accessibility semantics checked — content descriptions, `selected` state,
   role, touch-target sizes, measured contrast.
9. No regressions: the pre-existing harness suites still pass.
10. Offline-first constraints confirmed intact — no new network calls, no new
    permissions, no new dependencies.

## 15. Completion Checklist

- [x] Master UI/UX directive read
- [x] Phase 0 baseline reviewed
- [x] Phase 1 documentation reviewed
- [x] Repository preflight completed (branch, HEAD, tree, remote, Phase 1 commit present)
- [x] Existing navigation architecture understood before changes
- [x] App shell refined
- [x] Bottom navigation redesigned/refined
- [x] Active navigation state established
- [x] Inactive navigation state established
- [x] Official branding integrated appropriately (no logo redrawn)
- [x] Top-level headers refined
- [x] Back navigation verified
- [x] Navigation transitions refined
- [x] Safe areas verified
- [x] Content/navigation spacing verified
- [x] Light mode verified
- [x] Dark mode verified
- [x] Default text size verified
- [x] Extra Large text size verified
- [x] Narrow Tecno-class geometry verified
- [x] Accessibility verified
- [x] Canonical icons used where applicable
- [x] No repository-wide icon migration performed
- [x] No feature screens prematurely redesigned
- [x] No Yuki redesign performed
- [x] No V2 functionality added
- [x] No migration restarted
- [x] No Migration Stage 16 created
- [x] Actual UI rendered and visually inspected
- [x] Visual iteration performed (12 iterations, §10.4)
- [x] Documentation created
- [x] Evidence captured
- [x] Build passes
- [x] Relevant tests/checks pass
- [x] Commit created
- [x] Pushed to `origin/master`
- [x] Working tree clean
- [x] `HEAD == origin/master`
- [x] Agent stopped after Phase 2

## 16. Git Record

| | |
|---|---|
| Branch | `master` |
| Starting commit | `63b90cc` `feat(ui-ux): establish global visual language and design system foundation` |
| Phase 2 commit | `28fa14b` `feat(ui-ux): refine app shell and navigation experience` — contains all Phase 2 code, tests, this document and the evidence renders |
| Note | A later commit on `master` revises one wording correction in this document. `git log -1` on `master` is authoritative for the tip. |
| Remote | `origin/master`, verified to contain `28fa14b` |
| Working tree | clean |
| Sync | `HEAD == origin/master`, working tree clean |

No Migration Stage 16 was created, and no migration work was restarted.

## 17. Files Changed

New:

- `app/src/main/java/com/twohearts/app/ui/navigation/ShellSurfaces.kt` — route → navigation role classification.
- `app/src/main/java/com/twohearts/app/ui/navigation/ShellLayout.kt` — pure shell inset arithmetic.
- `app/src/main/java/com/twohearts/app/ui/navigation/NavigationTransitions.kt` — the three movement kinds.
- `app/src/main/java/com/twohearts/app/ui/components/ScreenTopBar.kt` — `ThTopBar` / `ThTopBarBackAction`.
- `app/src/test/java/com/twohearts/app/Phase2RenderHarness.kt` — shell render harness.
- `app/src/test/java/com/twohearts/app/Phase2ShellNavigationTest.kt` — shell navigation invariants.
- `UI-UX/Phase-2/PHASE-2-APP-SHELL-AND-NAVIGATION.md`, `UI-UX/Phase-2/evidence/`.

Modified (shell):

- `ui/navigation/BottomNav.kt` — the floating bar, active/inactive states, brand button, label fitting.
- `ui/navigation/AppShell.kt` — overlay bar, inset ownership, conditional `BackHandler`, shell-route provider.
- `ui/navigation/AppRouter.kt` — back-stack-derived back state, no re-push of the current route, transitions.
- `ui/navigation/NavConfig.kt` — the single navigation vocabulary and primary-route predicate.
- `ui/components/Header.kt` — role-based header treatment, `titleBadge`.
- `ui/components/Aliases.kt` — `Header` bridge routes back through the shell role.
- `ui/theme/Tokens.kt` — bottom-nav dimensions.

Modified (shell-level header swaps, title and back only):

- `ui/screens/about/AboutScreen.kt`, `ui/screens/search/SearchScreen.kt`,
  `ui/screens/security/SecuritySettingsScreen.kt`,
  `ui/screens/settings/{Appearance,Notification,Storage}SettingsScreen.kt`,
  `ui/screens/settings/SettingsHomeScreen.kt`,
  `ui/screens/settings/ImportScreen.kt`,
  `ui/screens/vault/{AddVaultContent,VaultContentViewer}.kt`,
  `ui/screens/notifications/NotificationCenterScreen.kt` (also restores the
  unread-count badge).

Modified (test): `app/src/test/java/com/twohearts/app/Phase0RenderHarness.kt`.

No dependencies were added. No feature screen content was redesigned.
