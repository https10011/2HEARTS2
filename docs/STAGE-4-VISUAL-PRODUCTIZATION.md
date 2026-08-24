# STAGE 4 — VISUAL PRODUCTIZATION — Us / Relationship Experience

> Stage 4 of the TWOHEARTS-VISUAL-PRODUCTIZATION-MASTER-STAGE-PLAN.
> Goal: make the relationship between the two people the emotional
> centerpiece of the app. References: `10-Us-SharedSpace.png`,
> `15-Relationship-Counter.png`, `66-Relationship-Settings.png`.

## 1. Stage objective

Productize the **Us** hub, the **Relationship Counter**,
**Important Dates**, and the **Relationship/Profile Settings** screens
so the couple — not the feature grid — is the visual hero. Preserve
all Stage 2/3 baseline work (onboarding, Home dashboard, global shell,
bottom nav, More).

## 2. Starting commit

- `8fd067f` — *Stage 3 visual productization: Home dashboard + global
  app shell* (master HEAD before this stage).

## 3. Ending commit

- `f0d2a83` — *Stage 4 visual productization: Us / Relationship
  Experience* (pushed to origin/master and verified via `git
  ls-remote`).

## 4. Files/components changed

| File | Change |
| --- | --- |
| `src/features/app-shell/screens/UsScreen.tsx` | Reworked (186 lines): shared `<CouplePair>` header, branded "Together for" hero → New `/app/us/counter`, Coming Up section (empty + populated states), grouped feature lists (Our Story / Our World), toast feedback |
| `src/features/app-shell/screens/RelationshipCounterScreen.tsx` | **NEW** — hero counter (years/months/days, hours, minutes), "next milestone" progress card, link to Important Dates, "Keep building your story" CTA → Add Memory |
| `src/features/app-shell/couplePair.tsx` | **NEW** — shared couple-header component (owner + partner initials avatars joined by a heart) reused by Us + Counter |
| `src/features/app-shell/relationshipCounter.ts` | **NEW** — pure helpers: elapsed time from the couple start date, next milestone + progress, safe against leap/DST quirks |
| `src/features/app-shell/screens/ImportantDatesScreen.tsx` | Reworked (134 lines): branded `DatePicker` replaces native `<input type="date">`, "In N days / Today! / Yearly / Passed" badges, fairly-ordered rows, warm empty state, toast feedback |
| `src/features/settings/ProfileSettingsScreen.tsx` | Native birthday input → branded `DatePicker` (10 lines) |
| `src/features/settings/RelationshipSettingsScreen.tsx` | Native partner-birthday + start-date inputs → branded `DatePicker` (18 lines) |
| `src/components/primitives.css` | **+442 lines** of centralized tokens: `th-us-*` header, `th-couple-pair`, `th-couple-strip`, `th-together-hero`, `th-counter-*`, `th-date-row-*`, `th-date-form`, `th-us-dates-*` — all token-driven, dark-mode + reduced-motion aware |
| `src/styles/global.css` | Warm screen-level gradient + brand-art corner offset used by Us/Counter (+7 lines) |
| `src/navigation/routes.ts` | `+ RoutePath.appUsCounter` (`/app/us/counter`) typed |
| `src/navigation/AppRouter.tsx` | Route registered (lazy import, +5 lines) |
| `tests/stage4-relationship-experience.test.ts` | **NEW** — 24 tests for `relationshipCounter.ts` + Important Dates decorate/sort logic |

## 5. Us hub changes

- Couple header is now the shared `<CouplePair>` — pair of initial
  avatars joined by an outlined heart; names render as links (owner →
  profile settings, partner → relationship settings).
- "Together for" hero = burgundy gradient pill showing `N years, M months`,
  `Since <formatted start date>`, `Next anniversary in M days` chip.
  Navigates to the new counter screen. Falls "fallback" gracefully when
  relationship data missing.
- **Coming Up** section:
  - Populated: up to next three `summary.upcomingDates` with
    `In N days / Today!` chips and a "See all" link → Important Dates.
  - Empty: warm invite (`No dates yet — add the moments that
    matter · Anniversaries, birthdays, first trips…`) acting as a CTA.
- Feature rows grouped **Our Story** (Memories / Timeline / Important
  Dates) and **Our World** (Places / Mood / Period / Vault) per the
  reference composition; each row reuses the centralized
  `th-feature-card` tokens from Stage 3.

## 6. Relationship Counter (new)

- Big-number hero: days / hours / minutes parsed from the domain start
  date through Phase 3 datetime helpers (`relativeDays`).
- "Next milestone" progress card computes year-quantitative
  milestones (or `90 days` unit for under-a-year couples) and renders a
  fill percentage chip + "N days to go".
- Non-empty fallback CTA → `/app/memory/add` ("Add a memory to
  celebrate today"), honoring empty-and-relevant actions.

## 7. Important Dates changes

- **Native date input removed** in favor of the centralized branded
  `DatePicker` (same component the Stage 2 onboarding introduced).
- Badges: `In N days` or `Today!` for upcoming; `Passed` for one-off
  dates already past; ` · Yearly` suffix for recurrent dates (chip
  rendering bug discovered + fixed during rendered inspection — yearly
  entries store the *original* date while the summary holds the *next
  occurrence*, so matching now keys on title+recurrence for yearly).
- Past one-off dates are dimmed (`th-date-row--past`), upcoming sorted
  by days-until.
- Warm empty state; save/delete feedback through the shared toast
  layer; add/edit/delete preserve safety of `RelationshipService`.

## 8. Settings changes (Stage-4-adjacent shell fixes)

- Profile Settings: birthday input → `DatePicker`.
- Relationship Settings: partner birthday + start-date → `DatePicker`.
- All date presentation is now the branded modal; no native `<input
  type=date>` remains in the relationship/settings context.

## 9. Asset changes

None. All decoration is token-driven CSS + the pre-existing
centralized `BrandLogo`/corner-brand art; no new assets introduced.

## 10. Animation changes

- Reused existing stage-3 motion tokens only: `th-stagger-item`
  entrance (row-by-row fade+rise), `th-pressable` scale on press, and
  existing `th-us-hero` chip count-up effect — all aware of
  `prefers-reduced-motion`.
- No new animation system introduced.

## 11. Theme verification

- Light mode: **BROWSER VERIFIED** across Us counter, dates, settings,
  and Home.
- Dark/system: **CODE VERIFIED** — all new classes consume shared
  `--th-*` tokens only; no new color literals outside existing
  burg/cream palettes. Theme flip is token-driven (same mechanism
  since Stage 3).
- Instant theme update: preserved (`useSyncExternalStore` +
  AppRootProvider + CSS tokens + SettingsStorage untouched).

## 12. Accessibility / text-scaling findings

- Touch targets: interactive rows/buttons ≥ 44 px; DatePicker trigger
  is 44 px high by the Stage 2 component.
- Text scaling: **BROWSER VERIFIED** at larger text on Home +
  Us hub; chips wrap, rows keep min-height, no clipping found.
- Reduced motion: **CODE VERIFIED** — milestone progress transition
  frozen under `@media (prefers-reduced-motion: reduce)`, stagger
  animation gated by existing `th-stagger-item` reduce rules,
  chip count-up skipped when user prefers reduced motion.
- Toast/aria roles preserved on important forms.

## 13. Browser/Vite verification

Environment limitation: SqlJsAdapter is **in-memory** (`new
SQL.Database()`), so full page reloads wipe the dev DB. On Android the
native adapter persists; sql.js is used only for browser dev/tests.
Therefore rendered verification avoids full navigations once data is
seeded:

Launch → onboarding (Alex · Sam) **completed in the same tab** → SPA
navigate:
- `/app/home` — **BROWSER VERIFIED** populated.
- `/app/us` — **BROWSER VERIFIED** couple header, Together hero
  ("Together for 2 years, 11 months · Since Sep 24, 2023 · Next
  anniversary in 31 days"), Coming Up after adding "Sam's birthday"
  ("In 31 days · Yearly"), grouped feature sections, bottom nav
  selected-state.
- `/app/us/counter` — **BROWSER VERIFIED** big-number hero
  ("Together for 1,065 days"), Days/Hours/Minutes tiles, "Next
  milestone" (3 years · 30 days to go · progress fill), Important
  Dates link, "Add a memory" CTA, back navigation.
- `/app/us/reminders` — **BROWSER VERIFIED** branded DatePicker modal,
  chip toggles (Once / Every year), save + toast, badge bug root-caused
  & fixed live (chip missed on yearly entries — matcher now keys by
  title+recurrence).
- `/app/more/settings/profile` — **BROWSER VERIFIED** branded
  DatePicker on birthday.
- `/app/more/settings/relationship` — **BROWSER VERIFIED** branded
  DatePicker ×2 (partner birthday, relationship start date) and
  Important Dates link.
- **APK VERIFIED**: NOT VERIFIED (environment limitation — no
  JDK/Android SDK in this sandbox).

## 14. Reference comparison findings

| Reference | Comparison |
| --- | --- |
| `10-Us-SharedSpace.png` | Composition, hierarchy, couple strip, grouped story/world sections matched; relationship hero now burgundy-primary and correctly routes to the new counter. No emoji icons; centralized Icon set retained. |
| `15-Relationship-Counter.png` | Big-number units, milestone progress, story share, primary CTA match; visual depth added via existing tokens. Reuses `th-together-hero` for the shared top strip. |
| `66-Relationship-Settings.png` | Header, sections (Special Someone / Relationship), branded pickers ×2, Important Dates link row, local-privacy info card — all match; native pickers eliminated. |

## 15. Tests

- **674 / 674 pass, 0 fail** (`npm test`).
- New: 24 tests in `tests/stage4-relationship-experience.test.ts`
  (counter math, milestone progress handling, Important Dates decorate +
  sort ordering).

## 16. TypeScript

- `npx tsc -b` → **CLEAN** (0 errors).

## 17. Production build

- `npm run build` → **SUCCESSFUL** (`dist/` chunk size warning
  unchanged from Stage 3 baseline — pre-existing 643 kB entry chunk).

## 18. Capacitor sync

- `npx cap sync android` → **SUCCESSFUL** (copy + update android in
  <200 ms).

## 19. Android build status

- **BLOCKED — `Android APK verification unavailable due to environment
  limitation`**: no JDK (`/usr/lib/jvm`) and no Android SDK
  (`/opt/android-sdk`) present in this sandbox. Same limitation as Stage
  2 and Stage 3.

## 20. Known limitations

- Dev-db persistence: SqlJsAdapter is in-memory, so full reloads in
  browser dev lose content that's not onboarding-stage metadata — same
  behavior as Stages 2/3; production Android uses the persistent native
  adapter.
- APK visual verification unavailable (no JDK/SDK).
- Pre-existing Vite chunk-size warning unchanged.
- One transient test failure observed during a mid-stage run, then
  re-run green (274/274 remainder + tail 674/674 — environment-timing
  flake, not reproducible across subsequent runs).

## 21. Deferred items

- None in scope. Remaining native `type="date"` inputs elsewhere
  (`AddMemory`, `AddEvent`, `LogPeriod`, `CreateReminder`) belong to
  Stage 5+ feature screens and are **out of Stage 4 scope** (Stage 4
  touched date entry only for the relationship/shell surfaces).

## 22. Exact next stage

- **Stage 5 — Remaining-app feature screens** (Memories, Notes,
  Reminders, Games visuals, Search, Timeline detail) per the MASTER
  STAGE PLAN; each retains the relationship-aware shell defined in
  Stages 1–4.
