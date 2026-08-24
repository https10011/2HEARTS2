# Stage 1 — Visual Productization Checkpoint (Onboarding & Entry Screens)

**Checkpoint date:** 2026-08-23
**Direction:** `TWOHEARTS-VISUAL-PRODUCTIZATION-DIRECTIVE.txt` (authoritative; not a roadmap phase)
**Strategy note:** Work is now staged in small batches (≈10 screens), not large phases.

---

## 1. Stage objective

Productize the app-entry and onboarding experience against the approved PNG
references (`TwoHearts UI Reference Screens/`):

- 01-Splash — floral brand polish
- 02-Welcome-FirstLaunch — restored/redesigned First Launch screen
- 03–06 onboarding steps — header, numbered progress, form polish
- 07-Setup-Complete — celebration/summary restoration

## 2. Screens successfully completed (code-change complete; see per-screen verification notes)

| Screen | Visual improvements |
|---|---|
| **Splash** | Brand lockup enlarged (180→200), Rose/Lily florals flanking top-right + bottom-left, loading caption replaced with single `.th-spinner`, warm floral theme (`th-splash--floral`). Verified rendered in browser (Splash → Welcome transition observed). |
| **Welcome / First Launch (02)** | New full-screen layout **without** the standard onboarding header/dots: official `BrandLogo` lockup, replaceable hero photo (`onboarding-welcome-photo.svg`), heart-divider, rewritten emotional copy, primary CTA `Get Started` with heart icon, privacy footnote `Private · Offline · On this device`. Verified rendered in browser — browser successfully advanced Welcome → Profile. |
| **Onboarding layout (03–06 header/progress)** | Dot indicator replaced with numbered step circles (done = `IconCheck`, active = burgundy, connecting lines). Header made transparent over a warm gradient background with subtle florals. Verified rendered in browser (numbered circles 1–4 with step 1 completed-check observed). |
| **Setup Complete (07)** | Restored per reference: `paired-hearts-check` illustration, ordered summary card (profile / partner / connection / preferences / app lock — app-lock line reflects the user's actual choice via `appSettingsStore`), sign-off line, staggered entrance. **Code-verified only — browser run never reached this step (blocked, §5).** |
| **Relationship Setup (05) — validation fix** | Start date made required with dedicated error message — fixes a genuine dead-end where the label said "(optional)" but `appStateService.deriveStage()` gates setup completion on `couple.startDate`. **End-to-end form completion = BLOCKED (§5).** |

## 3. Reusable primitives created / modified

- `OnboardingArt` — new variant `paired-hearts-check` (two overlapping hearts + check medallion, token-colored).
- `OnboardingLayout` — numbered-progress component (`.th-onboarding-steps*`), used by all numbered onboarding steps.
- New replaceable owner asset: `src/assets/images/onboarding-welcome-photo.svg` (scrapbook collage in brand palette, 720×560; MasterPrompt §20).
- `TWOHEARTS_CUSTOMIZATION_GUIDE.md` — documents the welcome-photo swap path.
- `useOnboarding` — error text now normalized via `safeUserMessage` (no internal error detail leaks).

## 4. Major discrepancies discovered through actual Vite/browser inspection

- Step indicator was raw dots; references 03–06 show numbered circles — **fixed**.
- Relationship form rendered raw browser-native date inputs with empty placeholders (`mm/dd/yyyy`) and browser chrome icons — visually generic vs reference styling. **Not yet redesigned.**
- "Relationship start date (optional)" was a functional dead-end: the completion gate requires the start date. Fixed by making it required with a proper error message — this also explains why the current browser-driven flow legitimately blocks on the step.
- A temporary debug `console.warn` was present in `appStateService.ts`; **removed before this commit**.

## 5. BLOCKED / DEFERRED — Relationship start-date input

**Status:** BLOCKED — explicitly deferred to a later agent.

- What was attempted: ~15 browser walk-through attempts (click date field → type `06/14/2022` → click Continue).
- Why it never advanced: the field is a native `<input type="date">`; the browser automation tool's typing does not register a value on native date inputs, so the (correct) required-field validation kept rejecting submission.
- Assessment at checkpoint: the form/validation code change is coherent and arguably correct (fixes the optional-label dead-end), but **visual completion of this step is unverified**. The raw browser-native date input also needs a designed, branded replacement to match reference 05.
- Scope of deferral: verify the form end-to-end and redesign the start-date field styling. Do not attempt further fixes in Stage 1.

## 6. Unfinished / next-batch items

- Relationship Setup date-input visual redesign (05) — blocked item above.
- Personalization (04-06), App Lock (06), Profile (03) screen visual verification against references (only layout-level changes reviewed).
- Remaining directive screens (Home, Notes, Memories, Timeline, Games, Reminders, Places, Mood, Period, Vault, Search, Notifications, Settings, dialogs/modals).

## 7. Visual-verification capability (IMPORTANT for future agents)

The app can be **visually inspected live**: run `npm run dev` (Vite, or the
served build on this workspace host, e.g. `http://localhost:12000`) and inspect
with the browser tools. Code inspection alone lied during this stage — the
onboarding route was walkable and the rendered discrepancies above were found
only via screenshots. Use the 77 approved PNG references as visual truth and
verify rendered output side-by-side whenever possible. Note: avoid native
`<input type="date">` fields in browser-automation verification — automate
them via code-level checks or replace with custom pickers.

## 8. Checkpoint validation (run 2026-08-23)

- `git status` — scoped staging (no accidental files)
- TypeScript: `npx tsc -b` — clean (exit 0)
- Tests: `npm test` — **650/650 pass**
- Production build: `npm run build` — success (chunk-size warning only, pre-existing)
- Capacitor sync: `npx cap sync android` — success
- Android APK: **NOT VERIFIED — environment limitation** (no JVM directory present on this host)
- Scope audit: onboarding/entry screens + shared primitives + customization guide only; no V2/cloud; debug line removed; no secrets; no phase-35+ work.
