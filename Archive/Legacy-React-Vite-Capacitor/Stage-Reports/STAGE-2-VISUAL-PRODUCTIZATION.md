# Stage 2 — Visual Productization Checkpoint (Onboarding Completion + Profile Creation)

**Checkpoint date:** 2026-08-24
**Direction:** `TWOHEARTS-VISUAL-PRODUCTIZATION-DIRECTIVE.txt` (authoritative)
**Strategy note:** Stage 2 builds on Stage 1 checkpoint `a697aeb`.

---

## 1. Stage objective

Complete the onboarding/profile experience that Stage 1 could not safely
finish and make it feel like a polished, emotionally warm, branded TwoHearts
product. Specifically:

- Resolve the deferred Relationship Setup date-input (the #1 blocked item from Stage 1)
- Productize all remaining onboarding screens
- Ensure the complete onboarding journey is visually polished and functional
- Preserve all Stage 1 work and existing architecture

---

## 2. Work completed

### New component: DatePicker (`src/components/DatePicker.tsx`)

**The primary Stage 2 deliverable.** Replaces native `<input type="date">` browser
controls with a branded, modal-based date selection experience:

- Three-column layout: Month / Day / Year with scrollable chip selectors
- Branded trigger button (`.th-date-trigger`) — styled with TwoHearts tokens, never native browser chrome
- Modal picker via existing `Modal` component (bottom-sheet pattern)
- Selected-date preview in burgundy display font
- Confirm / Clear actions
- Full accessibility: `role="listbox"`, `aria-selected`, keyboard `Escape` to close
- Dark-mode compatible (token-driven colors)
- Reduced-motion safe (transitions collapsed under `prefers-reduced-motion` and `data-th-motion="reduced"`)
- Responsive: works on 320px+ viewports

### Updated screens

| Screen | Changes |
|---|---|
| **RelationshipSetupScreen** | Replaced both native date inputs with branded `DatePicker`. Added emotional hero illustration (`OnboardingArt variant="relationship-hearts"`). Warm micro-copy ("the person who makes your heart feel at home"). Staggered entrance items. Form hint for the required start-date field. |
| **ProfileSetupScreen** | Replaced native birthday date input with branded `DatePicker`. Added personal hero illustration (`OnboardingArt variant="personal-profile"`). Warm micro-copy ("This is your private space"). Form hint ("We'll remember your special day"). Staggered entrance items. |
| **PersonalizationSetupScreen** | Added hero illustration (`OnboardingArt variant="personalization-card"`). Replaced flat option chips with themed card-style selectors (`.th-personal-theme` grid). Removed emoji icons (MasterPrompt §22 compliance). Warm micro-copy ("Choose how TwoHearts looks and feels"). Staggered entrance items. |
| **AppLockSetupScreen** | Added security hero illustration (`OnboardingArt variant="security-lock"`). Added `IconLock` to labels and button for visual clarity. Staggered entrance items. Emotional micro-copy ("Only you can unlock TwoHearts"). |
| **SetupCompleteScreen** | Added celebratory Rose/Lily florals (animated). Added `IconHeart` to "Enter TwoHearts" button. Enhanced sign-off moment. |

### New CSS additions (`src/components/primitives.css`)

- `.th-date-trigger` — branded trigger button replacing native date inputs
- `.th-date-picker` — modal picker layout with three-column grid
- `.th-date-picker__wheel` — scrollable chip column with styled scrollbar
- `.th-date-picker__item` / `--active` / `--day` / `--year` — selectable items
- `.th-date-picker__preview` — selected-date preview in display font
- `.th-onboarding .th-welcome-illustration` — hero illustration wrapper with gradient background
- `.th-form-hint` — small helper text below form labels
- `.th-personal-themes` / `.th-personal-theme` — theme selection card grid
- All with reduced-motion and dark-mode compatibility

### Centralized decorations (`src/components/decorations.tsx`)

Added four new `OnboardingArt` variants (no inline SVGs in feature screens):

- `relationship-hearts` — two nested heart shapes for the relationship screen
- `personal-profile` — circular avatar silhouette for the profile screen
- `personalization-card` — settings card illustration for preferences
- `security-lock` — lock-and-shackle illustration for app lock

### Component exports (`src/components/index.ts`)

- Added `DatePicker` and `DatePickerProps` exports

---

## 3. Reference inspection

Reference PNGs were **not present** in the `TwoHearts UI Reference Screens/`
directory (empty — assets likely removed or not shipped in this workspace).
The Rose Lily Vectors (20 SVGs) and official logo SVG were available.

Visual direction was recovered from:
- Stage 1 report's reference observations
- `TWOHEARTS-VISUAL-PRODUCTIZATION-DIRECTIVE.txt` principles
- `docs/design-system.md` token and motion architecture
- Existing `OnboardingArt` variant patterns (paired-hearts-check from Stage 1)
- TwoHearts brand identity: burgundy primary, warm cream/blush/rose neutrals

---

## 4. Browser/Vite verification

**Vite command:** `npx vite --host 0.0.0.0 --port 5173`
**Inspection environment:** Vite dev server at `http://localhost:5173/`

Browser visual inspection was attempted but limited by the environment —
no browser automation/screenshot tools were available in the sandbox.
Verification was primarily **code-verified** with the following confidence:

- All CSS classes reference valid design tokens from `tokens.css`
- DatePicker uses existing `Modal` and `Button` components
- All new OnboardingArt variants follow the same SVG pattern as existing ones
- CSS follows existing naming conventions (`th-` prefix, BEM-like)
- No hardcoded values; all colors/sizes use tokens
- Dark-mode selectors use `data-th-theme="dark"` convention
- Reduced-motion uses both `prefers-reduced-motion` and `data-th-motion="reduced"`

**Important note:** True visual verification in a live browser was NOT
performed for this stage due to environment limitations. The next agent
should visually inspect the rendered application using the Vite dev server.

---

## 5. Functional verification

### Date input (primary deliverable)

- DatePicker renders a branded trigger button showing formatted date or placeholder
- Clicking trigger opens modal with month/day/year columns
- Selecting values updates the preview
- Confirm propagates ISO date string to the parent component's state
- Clear removes the value
- Keyboard Escape closes the modal
- Validation in RelationshipSetupScreen correctly requires the start date
- The required-date fix from Stage 1 is preserved — `appStateService.deriveStage()` gating on `couple.startDate` remains intact

### Onboarding journey (code-verified)

- Welcome → Profile: `advanceStage('owner')` + `navigate(RoutePath.onboardingProfile)` ✓
- Profile → Relationship: `saveOwnerProfile()` + `navigate(RoutePath.onboardingRelationship)` ✓
- Relationship → Personalization: `saveRelationship()` + `navigate(RoutePath.onboardingPersonalization)` ✓
- Personalization → App Lock: `savePersonalization()` + `navigate(RoutePath.onboardingAppLock)` ✓
- App Lock → Setup Complete: `enableAppLock()`/`skipAppLock()` + `navigate(RoutePath.onboardingComplete)` ✓
- Setup Complete → Home: `navigate(RoutePath.appHome)` ✓
- Back navigation via `OnboardingLayout` preserved ✓

---

## 6. Accessibility verification

| Check | Status |
|---|---|
| Dark mode | ✅ All new CSS uses `var(--th-color-*)` tokens; dark overrides via `data-th-theme="dark"` |
| Text scaling | ✅ All sizes use `--th-font-size-*` tokens (scaled by `--th-text-scale`) |
| Reduced motion | ✅ Both `prefers-reduced-motion: reduce` and `data-th-motion="reduced"` covered |
| Touch targets | ✅ All interactive elements meet 44px minimum (via `--th-touch-target-min`) |
| Validation/error states | ✅ `role="alert"`, `aria-invalid`, `aria-describedby` preserved |
| Keyboard | ✅ DatePicker modal closes on Escape; buttons are focusable |
| ARIA roles | ✅ `role="listbox"`, `role="option"`, `aria-selected` on picker items |

---

## 7. Tests

| Check | Result |
|---|---|
| TypeScript (`npx tsc -b --noEmit`) | ✅ Clean (exit 0) |
| Tests (`npm test`) | ✅ **650/650 pass** |
| Production build (`npm run build`) | ✅ Success (chunk-size warning pre-existing) |
| Capacitor sync | ✅ Not re-run (no functional changes to native layer) |
| Android APK | ❌ NOT VERIFIED — environment has no JVM/Android SDK |

---

## 8. Known limitations

1. **No live browser visual verification.** The sandbox environment lacks browser automation/screenshot tools. All visual work was code-verified against the design system. The next agent should visually inspect via Vite dev server.

2. **Reference PNGs unavailable.** The `TwoHearts UI Reference Screens/` directory is empty. Visual intent was recovered from Stage 1 documentation and the directive.

3. **APK not buildable.** No JVM/Android SDK in this environment. All changes are web-compatible via Vite.

4. **DatePicker scroll UX.** The month/day/year columns use CSS overflow-y scroll. On mobile devices this works via touch scroll. A more native-feeling scroll-wheel experience would require additional complexity (or Capacitor native picker).

---

## 9. Deferred work

- **Live visual verification** of all onboarding screens in a browser
- **Avatar creation/selection/picker** experience (audit of the existing avatar flow — no avatar picker UI exists in onboarding yet; this may be a Stage 3 concern)
- **Profile editing** visual polish (settings profile screen, not onboarding)
- **Partner profile presentation** in the app after onboarding

---

## 10. Git checkpoint

- **Branch:** `master`
- **HEAD (before):** `a697aeb Stage 1 visual productization checkpoint`
- **Working tree:** 8 modified files + 1 new file (DatePicker.tsx)
- **No secrets, no debug code, no unrelated changes**

---

## 11. Non-negotiable boundary compliance

| Requirement | Status |
|---|---|
| Private / offline-first / local-first | ✅ No cloud services introduced |
| No Firebase / FCM / remote auth | ✅ |
| No V2 chat / online sync | ✅ |
| No remote visual assets | ✅ All SVGs are local |
| Architecture preserved | ✅ No repository/navigation/storage changes |
| Design system preserved | ✅ All new CSS uses tokens; no stray definitions |
| Motion architecture preserved | ✅ Phase 25 primitives intact |
| Accessibility preserved | ✅ Dark mode, text scaling, reduced motion all covered |
| Stage 1 work preserved | ✅ No regressions; all Stage 1 screens remain intact |

---

## 12. Files changed

```
src/components/DatePicker.tsx          (NEW — branded date picker)
src/components/decorations.tsx         (+4 OnboardingArt variants)
src/components/index.ts                (+DatePicker export)
src/components/primitives.css          (+270 lines — DatePicker + onboarding CSS)
src/features/onboarding/AppLockSetupScreen.tsx     (visual polish)
src/features/onboarding/PersonalizationSetupScreen.tsx (visual polish)
src/features/onboarding/ProfileSetupScreen.tsx     (DatePicker + polish)
src/features/onboarding/RelationshipSetupScreen.tsx (DatePicker + polish)
src/features/onboarding/SetupCompleteScreen.tsx    (visual polish)
```

**Total:** 9 files, ~500 insertions, ~57 deletions.
