# Phase 3 — Onboarding & First-Launch Experience

## 1. Phase Information

| | |
|---|---|
| Phase | 3 — Onboarding & First-Launch Experience |
| Date | 2026-09-21 |
| Branch | `master` |
| Starting commit | `20221af` (`docs(ui-ux): correct Phase 2 git record wording`) |
| Ending commit | `56c27db` — `feat(ui-ux): refine onboarding and first-launch experience` |
| Device target | Tecno Spark 10 Pro class — 720×1612, ~360dp wide, Android 13+ |
| Build verified | `:app:assembleDebug` → `BUILD SUCCESSFUL` → `app-debug.apk` (24,776,469 bytes) |
| Tests | `:app:testDebugUnitTest` — 44/44 passing (13 new in `Phase3OnboardingTest`) |
| Visual evidence | `UI-UX/Phase-3/evidence/` (12 curated Robolectric native-graphics renders) |

## 2. Objective

Phase 3 owns the first few minutes: the welcome, the setup sequence, and the
hand-off into the application.

The question it answers is the directive's:

> does the first launch feel intentional, welcoming, intimate, emotionally
> warm, polished, memorable, private, and unmistakably TwoHearts — before the
> person ever reaches Home?

It deliberately does **not** redesign Home, Us, Memories, Notes, Timeline,
Settings, or Yuki. Home begins in Phase 4 (§12).

## 3. Authoritative References

- `UI-UX/MASTER-UI-UX-DIRECTIVE.md` — §22 Iconography, §98 Phase Roadmap,
  §130 Phase Execution Rules, §149 Documentation; the onboarding and
  first-impression direction.
- `UI-UX/Phase-0/PHASE-0-UI-UX-RECONNAISSANCE.md` — the onboarding baseline
  (§10.1 Welcome, §10.2 the setup steps) and the date-entry friction finding
  (§9.3).
- `UI-UX/Phase-1/PHASE-1-GLOBAL-VISUAL-LANGUAGE.md` — the active visual
  foundation: tokens, `ThIcons`, motion, safe areas, and the official brand
  artwork.
- `UI-UX/Phase-2/PHASE-2-APP-SHELL-AND-NAVIGATION.md` — the shell and
  navigation model onboarding hands off to.
- `Migration/` — the Kotlin/Compose onboarding architecture as migrated.
- `Yuki Assets/Yuki Game Engine.md` — read for the Yuki boundary only; no
  Yuki work performed (§12).

### Historical reference material

The directive's historical UI references were searched for a first-launch
reference (`02-Welcome-FirstLaunch.png` or equivalent). No such asset exists in
the repository: the `Yuki Assets/` tree and the migrated `res/` drawables
contain brand and decoration art, but no legacy welcome screenshot. The
reconstruction therefore works from the master directive, the Phase 0
description of the old welcome, and the assets that *do* exist. The native
Compose implementation remains authoritative.

## 4. Existing Onboarding Baseline

Recorded before any change, from source inspection plus the Phase 0 report.

### 4.1 Architecture

- `AppRouter` branches on onboarding state: `OnboardingFlow` while setup is
  incomplete, otherwise `AppShell` + `NavHost`.
- `OnboardingStage` is a six-value enum (`FRESH`, `OWNER`, `RELATIONSHIP`,
  `PERSONALIZATION`, `APP_LOCK`, `COMPLETE`) with an `order` field.
- `SettingsStorage` persisted the *stage* and the draft answers under the
  `app-lock` / `owner` / … legacy strings.
- `OnboardingData` was the in-memory shape the steps consumed.

### 4.2 What Phase 0 found

- The welcome screen was a centred vertical stack with a gradient/placeholder
  hero — the bundled `onboarding-welcome-photo.svg` had **zero** references, so
  the first impression was typographic only, with no brand mark.
- The setup steps shared a consistent scaffold but had no decoration, no brand
  art, and no per-step validation feedback.
- Date entry was a raw `yyyy-mm-dd` text field — the single most visible
  onboarding friction.

### 4.3 Defects inherited from the migration

Three were structural and not visual:

1. **Only the stage was persisted.** A person who quit mid-setup returned to the
   right step with every field empty.
2. **The stage advanced before the answers were written.** A crash between the
   two could persist a stage the data did not justify.
3. **The PIN was held in the same transient state as the answers**, with no
   explicit rule keeping it off disk.

## 5. Onboarding Flow

The sequence, unchanged in shape but rewritten in execution:

| Stage | Step | Collects |
|---|---|---|
| `FRESH` | Welcome — the front door, not a numbered step | nothing |
| `OWNER` | "What should we call you?" | your name, your birthday (optional) |
| `RELATIONSHIP` | "Who are you sharing this with?" | their name, the day you began |
| `PERSONALIZATION` | "How should TwoHearts feel?" | theme, text size |
| `APP_LOCK` | "Keep it just for you" | optional PIN |
| `COMPLETE` | "Your space is ready" — the exit, not a numbered step | nothing |

Four numbered steps between the front door and the exit; the progress indicator
counts exactly those.

### 5.1 Progression, back, and skip

- **Forward** is one primary action per step. No step presents a second
  equally-weighted button.
- **Back** steps to the previous stage and is always available via the header
  affordance and system back; it never leaves the flow. Stepping back out of the
  lock step discards any typed PIN so a half-entered code cannot be committed by
  a later fast tap.
- **Skip** exists on exactly one step, the app lock, because that step is
  genuinely optional and says so ("Optional" eyebrow, "Not now" secondary). No
  other step is skippable, and no skip was invented to shorten the flow.

## 6. Visual Changes

### 6.1 Welcome

The front door now leads with the official artwork before any words: the
`BrandLogo` at full lockup size, a warm photographic art card, an ornament rule
carrying the words "For two", then the title and the product line. The brand
mark is the official rasterized asset from Phase 1 — never text, an emoji, or a
recreated glyph.

The composition is warm cream with the blush surface gradient, a single
restrained botanical decoration in the top corner, and no heart motif, no pink
flood, and no stock imagery.

### 6.2 Typography

Serif display for the emotional line, system sans for the supporting copy and
all UI chrome, both from the Phase 1 scale. Nothing below the Phase 1 readable
floor. Onboarding titles are the largest text in the flow and the only serif
on each step.

### 6.3 Imagery and decoration

- `onboarding_welcome_photo.png` — the previously unused welcome art, now the
  single hero image.
- `decor_rose_lily_01/11/15.png` — the project's rose/lily decoration set, used
  at low alpha on one corner per step. Decoration is present so the steps
  belong to the same world, and restrained so it never competes with the step
  content.

All art is local; `tools/generate-onboarding-assets.mjs` makes the pipeline
reproducible. No remote dependency was introduced.

### 6.4 Forms

The `yyyy-mm-dd` text field is gone. Dates now use the centralized
`ui/components/DatePicker.kt`, whose visual treatment matches the rest of
the system. Fields use the Phase 1 `ThInput` with labels, supporting text,
token spacing, and explicit error slots. The owner step opens with a live
avatar preview that fills in as the name is typed, so the step visibly builds
something rather than only collecting input.

### 6.5 Buttons and actions

One primary action per step, full width, at the bottom of the frame. Back is a
secondary affordance in the header. The lock step's "Not now" is a quiet text
action. Nothing presents five equal buttons.

### 6.6 Progress

A restrained `OnboardingStepIndicator` — one marker per numbered step, current
one emphasized — under the header. Not a Material stepper. It is derived from
the enum (`SETUP_STEP_COUNT`), so adding a stage cannot silently desync the
denominator.

### 6.7 Completion and transitions

`SetupCompleteScreen` names both people over the pair artwork, states that the
space is ready, and offers one action: "Open TwoHearts". Steps animate with a
short direction-aware slide + fade from Phase 2's motion foundation; the
reduced-motion preference collapses the durations. No theatrical animation.

## 7. UX Changes

### 7.1 Persistence — the significant behavioural fix

The flow now persists an `OnboardingDraft` (a single JSON object under one key)
at every step, written **before** the stage advances. Consequences:

- Quitting mid-setup resumes exactly where it was left, answers intact.
- A crash between the two writes leaves the earlier stage holding the *newer*
  answers — the safe direction, so the person sees their input again rather
  than skipping past a step with nothing recorded.
- `COMPLETE` is persisted only after every domain write has returned.

Malformed or absent draft JSON degrades to a fresh draft rather than throwing:
losing a draft costs one field of re-entry, while throwing would crash first
launch.

### 7.2 Credential safety

The PIN is deliberately **absent from the draft**. DataStore Preferences is not
encrypted; persisting a lock code there to save one re-entry after a crash is a
bad trade for a product whose promise is that private things stay private. The
PIN lives only in the composition and is handed to `AppLockService` (secure
storage) at commit. A restart during the lock step asks for it again — correct
behaviour for a credential. `Phase3OnboardingTest` pins this: the draft class
has no field matching `pin`, case-insensitively, and the serialized form
contains no such key.

### 7.3 Validation

Errors are per-step, concise, and emotionally neutral ("Please add a name so
TwoHearts knows what to call you"), rendered through the Phase 1 error system
rather than alarming red everywhere. The primary action stays enabled and
validates on press, so the person gets a reason rather than a dead button. The
commit path catches failure and offers a calm retry that loses nothing.

### 7.4 Keyboard

The frame consumes the IME through `imePadding()` and the safe drawing insets,
so the primary action is never covered and fields stay visible. The scrolling
content region keeps every step reachable with the keyboard open.

### 7.5 Layout

Each step centres its content when it fits and scrolls when it does not, using
one canonical pattern: a scrollable measures its child with an unbounded height,
so the centring arrangement must sit on a column that is explicitly given the
viewport height (`heightIn(min = viewport)` inside a `BoxWithConstraints`). The
arrangement was previously applied to the scrolling node itself, where it had
nothing to distribute into and silently top-aligned. This is fixed in
`OnboardingLayout`, `WelcomeScreen`, and `SetupCompleteScreen`.

### 7.6 Accessibility

- Every icon-only control keeps a label (the Phase 1 defect this inherits from
  is not reintroduced).
- Decorative art is excluded from the accessibility tree; the brand lockup
  carries a content description.
- Selected states on personalization choices use `Modifier.selectable` with the
  selected flag, not colour alone.
- Touch targets stay at or above the token minimum.
- Contrast holds in both themes; error text uses the semantic error role.
- Reduced motion is respected end to end.

## 8. Design Decisions

### 8.1 The welcome leads with the brand, not with a form

The directive's failure mode is "here is an Android form, fill it out". The
welcome therefore shows the artwork and the product's character before it asks
for anything; the first input is one step later.

### 8.2 Relationship-neutral vocabulary throughout

Copy uses "You", "your special someone", "the two of you", and "together".
No hardcoded boyfriend/girlfriend/husband/wife anywhere in the flow, and no
gendered assumption in the pair artwork or the step labels.

### 8.3 The flow is short on purpose

Four setup steps, one of which is optional and two fields at most per step.
No extra question was added for the sake of a screen.

### 8.4 Privacy is stated once, quietly

"No accounts. Nothing leaves this device." appears as the welcome's closing
line and the lock step explains the PIN. No V2/cloud promise is made anywhere.

### 8.5 The draft/credential split is structural

See §7.2. The rule is enforced by the shape of the data class, so a later change
cannot forget it.

## 9. Responsive Validation

Rendered and inspected at:

| Geometry | Text size | States |
|---|---|---|
| 411×891dp | Default | welcome, all four setup steps, empty/validation state, completion, both themes |
| 411×891dp | Extra Large | welcome, owner, completion |
| 360×780dp (Tecno Spark 10 Pro class) | Default | welcome, owner, relationship, personalization, app lock, completion |
| 360×780dp | Extra Large | welcome, owner, completion |
| 360×780dp | Default | welcome, relationship (dark) |

No hardcoded screen dimensions: the layout is weight/inset driven and was
verified not to clip, wrap badly, or push the primary action under system
navigation at either size.

## 10. Visual Validation

### 10.1 Rendering method

The Phase 0/Phase 2 approach applied to onboarding: real Compose trees rendered
through Robolectric native graphics to PNGs in `app/build/phase3-onboarding/`.
`Phase3RenderHarness` (26 states) is the palette-and-geometry sweep;
`Phase3NarrowRenderHarness` covers the 360dp geometry. Inspection used a
content-segmentation analyzer (`tools/analyze-phase3-renders.py`) plus direct
pixel/palette inspection.

### 10.2 What the renders confirmed

- Brand artwork renders as artwork on light and dark, and the dark-theme
  recolor rule applies to the brand art rather than washing it out.
- Dark mode is warm and designed — deep charcoal-brown surfaces around
  `#1A1310` with blush/burgundy accents — not an inversion of the light palette.
- Both themes hold their hierarchy at 360dp and at Extra Large text; the largest
  text never clips or overlaps.
- Content is vertically centred in the available region and the bottom gap to
  the system navigation is preserved in every state.
- The completion screen names both people and clearly reads as an exit, not
  another form.

### 10.3 Defects found and fixed during iteration

1. **Onboarding content appended at the end of the scroll column** instead of
   centring. Diagnosed with temporary `onGloballyPositioned` bounds probes
   (inner column at 250..2403 of a 717dp viewport, i.e. top-aligned), fixed by
   the nested viewport-pinned column described in §7.5, then re-rendered and
   re-probed to confirm the content bounds sit in the centred band. The probes
   were removed after verification.
2. Several unused imports left over from the layout experiment
   (`BoxWithConstraints`, `heightIn`) — removed, then re-added where the final
   pattern actually needs them.
3. Original test failures caused by inspecting the empty/validation state and
   the draft round-trip through Android's stubbed `org.json` on the plain JVM —
   fixed by running the tests on the real implementation via Robolectric.

### 10.4 Evidence

`UI-UX/Phase-3/evidence/` — 12 curated renders: welcome (light, dark, 360dp),
owner, empty/validation, relationship, personalization (light, dark), app lock,
completion (full and 360dp), and owner at 360dp with Extra Large text.

## 11. Performance

Onboarding stays lightweight: one small hero image and three low-alpha
decorations, no blur, no continuous animation, and no image loader. The
per-step transition is a short slide + fade driven by tokens and collapsed by
reduced motion. Startup work is the existing bootstrap plus one settings read
for the draft.

## 12. Yuki / Home Boundary

- **Yuki:** not touched. No Yuki onboarding or gameplay was added; the Yuki
  redesign belongs to Phase 13.
- **Home:** not redesigned. The only integration work is the hand-off —
  `OnboardingFlow.onComplete` → `AppRouter` → `NavHost` to the existing
  `APP_HOME`, clearing the onboarding back stack so completed setup is not
  re-entered. No Home visual changes.

## 13. Deferred Work

- Per-step transition polish beyond slide + fade, if a later phase wants it.
- Any richer avatar selection — the current product has no avatar asset system
  beyond the generated initials/`ProfileAvatar`, and Phase 3 did not invent one.
- Home's visual design (Phase 4).

## 14. Out of Scope

No online accounts, cloud sync, chat, remote authentication, analytics,
tracking, online media, FCM, or any other V2 functionality. No backend or data
architecture change. No migration stage was created and no Migration Stage 16
exists. No Home, Us, Memories, Notes, Timeline, Settings, or Yuki redesign.

## 15. Files Changed

### Modified

| File | Change |
|---|---|
| `ui/onboarding/OnboardingLayout.kt` | Canonical centring-when-it-fits / scroll-when-it-does-not layout; header, step indicator, decoration, error band. |
| `ui/onboarding/WelcomeScreen.kt` | Brand-led first impression: official lockup, hero art, ornament rule, warm copy, privacy line. |
| `ui/onboarding/ProfileSetupScreen.kt` | Live avatar preview, centralized date picker, token form rhythm, per-step validation. |
| `ui/onboarding/RelationshipSetupScreen.kt` | Neutral copy, centralized date picker, wording that removes the "exact day" anxiety. |
| `ui/onboarding/PersonalizationSetupScreen.kt` | Selectable theme/text-size choices with real selected states. |
| `ui/onboarding/AppLockSetupScreen.kt` | Clearly optional framing, credential explanation, PIN handling kept off disk. |
| `ui/onboarding/SetupCompleteScreen.kt` | Both names, pair artwork, single meaningful exit action. |
| `ui/onboarding/OnboardingFlow.kt` | Draft persistence, advance-before-commit ordering, PIN separation, direction-aware transitions, failure handling. |
| `ui/onboarding/OnboardingState.kt` | Stage derivation (`SETUP_STEP_COUNT`, `LAST_SETUP_ORDER`), single storage-key mapping. |
| `ui/navigation/AppRouter.kt` | Onboarding hand-off; route to existing Home; reduced-motion-aware transitions. |
| `ui/components/Input.kt`, `Aliases.kt`, `Icons.kt` | Token adoption and icon vocabulary for onboarding controls. |
| `data/settings/AppSettings.kt`, `SettingsStorage.kt` | Draft persistence keys and accessors. |

### Added

| File | Purpose |
|---|---|
| `data/settings/OnboardingDraft.kt` | The persisted setup answers — deliberately without the PIN. |
| `ui/components/DatePicker.kt` | Centralized date control replacing `yyyy-mm-dd` text entry. |
| `ui/decorations/OnboardingArt.kt` | Onboarding decoration placement. |
| `res/drawable-nodpi/onboarding_welcome_photo.png`, `decor_rose_lily_01/11/15.png` | Official onboarding and decoration art. |
| `app/src/test/java/com/twohearts/app/Phase3RenderHarness.kt` | 26-state render harness. |
| `app/src/test/java/com/twohearts/app/Phase3OnboardingTest.kt` | 13 onboarding invariants (progression, resume, credential safety, validation). |
| `tools/generate-onboarding-assets.mjs` | Reproducible onboarding art pipeline. |
| `tools/analyze-phase3-renders.py` | Render inspection helper. |

## 16. Completion Checklist

- [x] Master UI/UX directive, Phase 0, Phase 1, Phase 2 read
- [x] Repository preflight completed
- [x] Existing onboarding flow understood
- [x] Historical welcome reference searched for (not present; documented)
- [x] First-launch experience redesigned/refined
- [x] Official TwoHearts branding used
- [x] Welcome experience feels distinctly TwoHearts
- [x] Onboarding flow has clear hierarchy
- [x] Onboarding copy reviewed
- [x] Relationship-neutral language preserved
- [x] Personalization experience refined
- [x] Avatar/profile preview refined (no new avatar system invented)
- [x] Forms refined (centralized date picker)
- [x] Progress indication refined
- [x] Primary actions refined
- [x] Back behavior verified
- [x] Skip behavior verified (app lock only)
- [x] Completion transition refined
- [x] First-launch persistence verified (draft resume + legacy stage strings)
- [x] Light and dark mode verified
- [x] Default and Extra Large text verified
- [x] 411×891dp and 360×780dp verified
- [x] Keyboard behavior verified (IME-aware frame)
- [x] Accessibility verified
- [x] Reduced motion respected
- [x] Actual Compose UI rendered and visually inspected
- [x] Visual iteration performed
- [x] No Home, Yuki, or other out-of-scope redesign
- [x] No V2 functionality added
- [x] No migration restarted; no Migration Stage 16
- [x] Documentation created
- [x] Evidence captured
- [x] Build passes; 44/44 tests pass
- [x] Commit `56c27db` created and pushed to `origin/master`
