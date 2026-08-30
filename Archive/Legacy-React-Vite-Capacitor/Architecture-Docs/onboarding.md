# TwoHearts — Onboarding & App Entry Experience (Phase 5)

Phase 5 implements the initial user journey: the first visual experience a new
user sees, through profile setup, relationship configuration, personalization,
optional app-lock, and into the main app. All data persists locally via the
existing Phase 2-4 infrastructure.

## Onboarding flow

```
Splash (during bootstrap) → application-state evaluation →
  ├── Brand new user → Welcome → Profile → Relationship → Personalization → App Lock → Complete → Home
  ├── Incomplete setup → resume correct persisted stage
  └── Completed setup → Home (app entry)
```

### Seven onboarding areas

1. **Splash** (`SplashScreen.tsx`) — Branding displayed during bootstrap
   initialization. Not a route; rendered in `main.tsx` AppGate while
   persistence, migrations, and core services start.

2. **Welcome** (`WelcomeScreen.tsx`) — Introduction to TwoHearts.
   Privacy-first messaging (everything stays on device). "Get started" button
   advances to profile setup and sets `onboardingStage` to `'owner'`.

3. **Profile Setup** (`ProfileSetupScreen.tsx`) — Owner's display name
   (required, 1-40 chars) and optional birthday. Saves via
   `RelationshipService.saveOwner()` in one transaction (profile + couple
   singleton link). Advances stage to `'relationship'`.

4. **Relationship Setup** (`RelationshipSetupScreen.tsx`) — Partner's name
   (required, 1-40 chars), optional birthday, and relationship start date.
   Saves partner via `RelationshipService.savePartner()` and start date via
   `setStartDate()`. Advances stage to `'personalization'`.

5. **Personalization Setup** (`PersonalizationSetupScreen.tsx`) — Text size
   (Small/Default/Large/Extra Large) and theme mode (Light/Dark/System).
   Persists to `appSettingsStore.setTextSize()` / `setThemeMode()`. Applies
   immediately via `applyTextSize()` and `applyThemeMode()`.

6. **App Lock Setup** (`AppLockSetupScreen.tsx`) — Optional PIN-based app
   lock. Users can skip or create a 4-8 digit PIN. Delegates to the existing
   `AppLockService.enable()` (Phase 3). Persists `appLockEnabled` in
   settings. Advances to complete.

7. **Setup Complete** (`SetupCompleteScreen.tsx`) — Celebration screen with
   "Enter TwoHearts" button that navigates to `/app/home`. On reaching this
   screen, `completeSetup()` has already been called to mark
   `onboardingStage: 'complete'`.

## Architecture

### Data flow

```
UI (onboarding screens)
  → useOnboarding hook (state + actions)
    → AppStateService / RelationshipService / AppLockService
      → Repositories (ProfileRepository, CoupleRepository)
        → DatabaseAdapter (SQLite/sql.js)
          → Local persistence (Phase 2)
```

UI never imports Capacitor plugins or storage APIs directly (Phase 3 rule).
Services never leak to repositories. Repositories never leak to UI.

### State management

- **Onboarding stage** persisted in `appSettingsStore` as `onboardingStage`:
  `'fresh' | 'owner' | 'relationship' | 'personalization' | 'complete'`
- **Domain truth** drives reconciliation: `AppStateService.reconcileOnboardingStage()`
  checks actual profile/couple data, never trusts the persisted stage alone
- **First-launch stamp** (`firstLaunchAt`) recorded once at bootstrap, survives reset
- **Preferences** (textSize, themeMode) stored in `appSettingsStore` (localStorage)
- **App-lock config** (`appLockEnabled`, `lockTimeoutSeconds`) in settings;
  PIN material in SecureStore only

### Routes

```
/                          → OnboardingGate → redirect to correct stage
/onboarding/welcome        → WelcomeScreen
/onboarding/profile        → ProfileSetupScreen
/onboarding/relationship   → RelationshipSetupScreen
/onboarding/personalization → PersonalizationSetupScreen
/onboarding/app-lock       → AppLockSetupScreen
/onboarding/complete       → SetupCompleteScreen
/app                       → AppShell → /app/home
/app/home                  → HomeScreen
```

`ONBOARDING_STEPS` array in `routes.ts` defines the ordered step sequence
for the step-indicator dots and back-navigation.

### OnboardingGate

The `OnboardingGate` component sits at the root route (`/`). It:
1. Reads `appSettingsStore.getState().onboardingStage`
2. If `'complete'` → renders children (Navigate to `/app/home`)
3. If incomplete → reconciles from domain truth via `AppStateService`
4. Redirects to the correct onboarding stage route
5. Shows `SplashScreen` during evaluation

Onboarding sub-routes (`/onboarding/*`) are NOT wrapped in the gate — this
prevents redirect loops when screens navigate between each other.

### useOnboarding hook

Central hook consumed by all onboarding screens:
- `saveOwnerProfile(input)` → RelationshipService → advance to 'relationship'
- `saveRelationship(input)` → RelationshipService → advance to 'personalization'
- `savePersonalization(input)` → appSettingsStore → advance to 'personalization'
- `enableAppLock(pin)` → AppLockService → advance to 'complete'
- `skipAppLock()` → completeSetup → advance to 'complete'
- `advanceStage(nextStage)` → appSettingsStore.setOnboardingStage()

### Component hierarchy

```
AppGate (main.tsx)
  └── AppRootProvider (lifecycle + text-size + theme)
        └── AppRouter
              ├── OnboardingGate (root only)
              │     └── Navigate → /onboarding/* or /app/*
              ├── /onboarding/* routes (no gate wrapper)
              │     └── OnboardingLayout
              │           ├── Header (TwoHearts title + back + optional skip)
              │           ├── Step indicator dots
              │           └── Screen content (children)
              │                 ├── WelcomeScreen
              │                 ├── ProfileSetupScreen
              │                 ├── RelationshipSetupScreen
              │                 ├── PersonalizationSetupScreen
              │                 ├── AppLockSetupScreen
              │                 └── SetupCompleteScreen
              └── /app/* routes
                    └── AppShell
                          └── HomeScreen
```

## Returning users

| Scenario | Behavior |
|---|---|
| Brand-new user | Redirected to `/onboarding/welcome` |
| Incomplete setup (owner missing) | Resumes at `/onboarding/profile` |
| Incomplete setup (relationship) | Resumes at `/onboarding/relationship` |
| Incomplete setup (personalization) | Resumes at `/onboarding/personalization` |
| Completed setup | Goes to `/app/home` |
| App-lock enabled | Respects AppLockService locked/unlocked state |

Stage reconciliation uses domain truth (actual profile/couple data), never
the persisted stage alone. This handles partial installs, killed apps, and
data corruption gracefully.

## Testing

Phase 5 adds 48 tests in `tests/onboarding.test.ts` covering:

- Route structure validation (7 route paths, step order, defaults)
- Settings persistence (stage transitions, first launch, text size, theme, app lock)
- AppStateService integration (first launch stamp, stage reconciliation, completion gate)
- Profile persistence through RelationshipService
- Relationship persistence (start date, summary computation, validation)
- Stage progression after data (owner → relationship → personalization → complete)
- Validation (empty names, length limits, PIN shape, date formats)
- App lock service (enable, disable, unlock, state notifications)
- Returning user behavior (completed stays complete, resume from stage, firstLaunchAt survives reset)
- Reactive subscriptions (useSyncExternalStore notifications)

All 162 tests pass (114 existing + 48 new Phase 5).

## Visual identity

Onboarding screens use the established TwoHearts design system:
- **Burgundy primary** (#6A1B2B) for CTAs, active indicators, accents
- **Warm cream** (#FDF6F0) backgrounds, **blush** (#F6E1DE) for secondary elements
- **Georgia serif** display font for headings, system-ui for body
- **Step indicator dots** — burgundy for active, rose-muted for completed, beige for pending
- **Touch targets** ≥ 44px, **safe area** insets, **responsive** 480px max-width
- **Text-size scaling** via `--th-text-scale` CSS custom property
- **Theme mode** (light/dark/system) applied via `data-th-theme` attribute

All screens are keyboard-aware, have proper ARIA labels, and handle loading,
error, and disabled states.

## New files

```
src/features/onboarding/
  index.ts                    — barrel exports
  SplashScreen.tsx            — splash during bootstrap
  WelcomeScreen.tsx           — welcome / get started
  ProfileSetupScreen.tsx      — owner name + birthday
  RelationshipSetupScreen.tsx — partner + start date
  PersonalizationSetupScreen.tsx — text size + theme
  AppLockSetupScreen.tsx      — optional PIN setup
  SetupCompleteScreen.tsx     — celebration + enter app
  OnboardingLayout.tsx        — shared layout with step dots
  OnboardingGate.tsx          — state evaluation at root
  useOnboarding.ts            — hook for screen state/actions
  HomeScreen.tsx              — post-onboarding destination

tests/
  onboarding.test.ts          — 48 Phase 5 tests

docs/
  onboarding.md               — this document
```

## Modified files

```
src/navigation/routes.ts      — added 5 onboarding route paths + ONBOARDING_STEPS
src/navigation/AppRouter.tsx  — wired all onboarding routes + OnboardingGate
src/main.tsx                  — branded splash during bootstrap
src/components/primitives.css — onboarding CSS (splash, layout, dots, form, chips)
```

## Limitations

- **Android build not verified**: Java/Android SDK not available in the test
  environment. The web build and cap sync both pass; native Android behavior
  was not physically tested. The onboarding screens are web-compatible and
  use the same Capacitor patterns as Phase 1-4.
- **Dark mode tokens**: Theme mode preference is persisted and applied via
  `data-th-theme`, but only light-mode CSS tokens exist today. Dark mode
  styling is a future CSS-only change.
- **Home screen is a placeholder**: The HomeScreen shows a greeting and
  "ready" message. The real home/greeting experience is Phase 6+.
- **No biometric lock**: App lock uses PIN only (4-8 digits). Biometric
  authentication is a future enhancement.
