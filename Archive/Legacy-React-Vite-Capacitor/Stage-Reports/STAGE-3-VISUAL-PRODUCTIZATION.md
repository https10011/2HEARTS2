# Stage 3 — Home + Global App Shell (Visual Productization)

Stage 3 of the TwoHearts Visual Productization Master Stage Plan. Scope:
**Home Dashboard (09)**, **More menu (11)**, and the **global app shell**
(bottom navigation, shared headers, navigation states, route transitions).
The Us hub (10) is Stage 4 scope and was intentionally left untouched.

- **Stage objective:** make TwoHearts immediately feel like a finished,
  standalone couples application after onboarding — warm, romantic, branded,
  and alive with the couple's own content.
- **Starting commit:** `def00d8` (Stage 2 checkpoint).
- **Ending commit:** see `git log` on `master` (Stage 3 commit).
- **Baseline:** Stage 2 (docs/STAGE-2-VISUAL-PRODUCTIZATION.md) preserved —
  no onboarding code was modified.

## Method

Preflight inspection of `AGENTS.md`, the visual-productization directive,
`MasterPrompt.txt`, the roadmap, design-system/audit docs, and the Stage 2
report; rendered inspection of the live app via the Vite dev server
(`localhost:5173`) against the approved reference PNGs in
`TwoHeart UI Reference Screens/` (`09 - Home (Main Dashboard)`,
`10 - TwoHearts (Hub)`, `11 - More`). Browser walkthrough covered light/dark
theme, Default/Extra Large text scale, all five bottom-nav destinations, and
deep-links. Findings were fixed, re-rendered, and re-inspected.

## Rendered discrepancies found (before → after)

| # | Area | Before (browser-verified) | After |
|---|------|---------------------------|-------|
| 1 | Home hero | burgundy band with extra double safe-area top padding; avatars floating with no presentation | slim blush band behind the couple centerpiece; avatar chips with labels + heart connector |
| 2 | Home header | none — screen opened straight into the hero | time-aware greeting ("Good morning/afternoon/evening/night" + date) with a notification bell linking to the Notification Center; watercolor rose corner decoration |
| 3 | Couple presentation | plain text under avatars | "Your private couple space" (empty state) / "Together since <date>" (populated) warm pill |
| 4 | Home cards | flat white cards, thin border | elevated warm-white cards with soft burgundy-tinted shadow, larger radius, staggered entrance |
| 5 | Home content | invitation copy ("Life in TwoHearts…") only | "From your story" — live previews of the most recent memory, latest note, and next upcoming reminder, each deep-linking to its detail screen; invitation card kept as the empty state |
| 6 | More | plain row list | burgundy hero band ("More ways to make TwoHearts yours" + floral), owner profile card with blush avatar + pill chip, grouped rows with captions, branded footer |
| 7 | Bottom nav active state | icon tint + tiny dot only | blush capsule pill behind the active destination + burgundy label (existing dot retained) |

## Files changed

- `src/features/app-shell/homeHighlights.ts` — **new**; pure, Node-test-safe
  highlight selection (recent memory / latest note / next upcoming reminder)
  with label/icon/format helpers.
- `src/features/app-shell/useHomeHighlights.ts` — **new**; async hook loading
  highlights from existing services/repositories.
- `src/features/app-shell/screens/HomeScreen.tsx` — rewritten: greeting row +
  bell, hero band + rose, couple centerpiece (avatar chips → profile/relationship
  settings, heart connector), relationship pill, quick-access cards,
  highlights list / invitation card, bottom lily accent.
- `src/features/app-shell/screens/MoreScreen.tsx` — rewritten: hero band,
  profile card, grouped rows, footer.
- `src/features/app-shell/BottomNav.tsx` — active pill capsule span behind the
  active destination icon.
- `src/components/primitives.css` — hero band margin/padding fix (removed
  double safe-area padding); Home greeting/bell/pill/invite styles; enhanced
  `th-home-card`; More profile/row/footer styles; bottom-nav active pill;
  `th-feature-card__desc` two-line clamp.
- `src/theme/tokens.css` — added `--th-nav-center-size: 58px`,
  `--th-avatar-lg: 72px` (bottom-nav center button + avatars now use tokens).
- `src/styles/global.css` — new classes added to the theme-flip transition
  list (greeting bell, pill, invite, More profile).
- `tests/stage3-home-shell.test.ts` — **new**; 13 tests for highlight logic,
  screen wiring, and CSS/token guardrails.
- `docs/STAGE-3-VISUAL-PRODUCTIZATION.md` — this report.

No new dependencies. No changes to storage, repositories, navigation
architecture, settings architecture, security, notifications, or game logic.

## Home changes

- Greeting (time-of-day aware) + local date + notification bell → `/app/notifications`.
- Couple centerpiece: official `BrandLogo` (auto light-recolor in dark theme),
  two avatar chips (initials when names exist, smile icon otherwise) joined by
  a burgundy heart; chips deep-link to Profile / Relationship settings.
- Relationship pill: "Your private couple space" until a start date exists,
  then "Together since <localized date>".
- Quick access (unchanged curated set per Phase 24 contract): Notes,
  Reminders, Us, Games — now on elevated warm cards with staggered entrance.
- "From your story": up to three preview cards (Recent memory, Latest note
  with two-line excerpt, Upcoming reminder with date·time), each linking to
  the entity detail screen. Empty state keeps the "Make TwoHearts yours"
  invitation card with a lily accent.

## Global shell changes

- Bottom navigation: active destination gets a blush capsule pill behind the
  icon (44px+ touch targets preserved); center brand button sized via the new
  `--th-nav-center-size` token; center active state (soft ring) preserved.
- Shared headers: unchanged — Home/More intentionally use the screen hero
  treatment instead of a top header (reference-accurate); other screens keep
  their existing headers.
- Route transitions: existing `th-route-transition` entrance + hero/card
  staggers verified across Home → Us → Notes → More → Notifications → back.
  No new animation system introduced.

## More changes

- Burgundy hero band with subtitle + floral (dark theme keeps a deep rose
  band with readable cream text — verified).
- Owner profile card: blush avatar tile (initial or smile icon), name +
  "Your TwoHearts space" caption, "Profile & Preferences" pill chip →
  profile settings.
- Grouped rows (Settings / Search / About) with captions and chevrons on the
  shared `th-more-item` treatment; footer "TwoHearts • Version x.y.z".

## Asset changes

None — all art comes from the existing centralized brand/decoration system
(`BrandLogo`, `RoseDecoration`, `LilyDecoration`, official `Icon` set).

## Animation changes

Reused the existing motion layer only: `th-hero__stagger-item` delays on Home
cards and highlight rows; bottom-nav pill is a static capsule (no new motion);
theme flips transition the new surfaces via the existing token-driven rule.
Reduced motion collapses all of it (code-verified: stagger short-circuit +
token motion collapse).

## Verification status

| Item | Status |
|------|--------|
| Home empty + populated states | **BROWSER VERIFIED** (light + dark) |
| Highlights with real note/reminder/memory (deep-links) | **BROWSER VERIFIED** |
| More menu | **BROWSER VERIFIED** (light + dark) |
| Bottom nav selected/unselected states, all 5 destinations | **BROWSER VERIFIED** |
| Theme switching (immediate flip) | **BROWSER VERIFIED** (light↔dark) |
| Text scaling (Extra Large) | **BROWSER VERIFIED** — wraps/scrolls, no clipping |
| Reduced motion | **CODE VERIFIED** (existing token collapse covers new classes) |
| Reference comparison (09 Home, 11 More) | **BROWSER VERIFIED** — intent matched: greeting header, burgundy/branded hero, couple centerpiece, quick access, grouped More with profile |
| Tests | **663/663 pass** (95 suites), incl. new `stage3-home-shell` suite |
| TypeScript | **clean** (`npx tsc -b`) |
| Production build | **successful** (`npm run build`) |
| Capacitor sync | **successful** (`npx cap sync android`) |
| Android APK | **BLOCKED** — Android APK verification unavailable due to environment limitation (no JDK/Android SDK installed) |

## Known limitations

- APK-level verification (safe areas, real device rendering) not possible in
  this environment; Vite/browser used as the visual verification bridge.
- Notification Center screen shows its empty state in the walkthrough; the
  notification repository wiring is a pre-existing gap outside Stage 3 scope.
- Dev-browser database is in-memory (sql.js): content seeded for screenshots
  does not persist across full page reloads — expected dev behavior.

## Deferred items

- Us hub (10) productization — Stage 4.
- Relationship counter / Important Dates visual work — Stage 4.
- Any deeper Notification Center data wiring — future stage.

## Next stage

**Stage 4 — Us / Relationship Experience:** Us hub (10), relationship hero
card, Our Story / Our World grouping, relationship counter, Important Dates.
Starts from the Stage 3 commit on `master` with this document as baseline.
