# Stage 5 — Onboarding Flow Migration

**Status:** ✅ Complete  
**Date:** 2026-08-30  
**Preceded by:** Stage 4 (Core Services Migration)  
**Followed by:** Stage 6 (App Shell & Navigation Migration)

---

## Overview

Stage 5 migrated the complete onboarding flow from the legacy React/Vite/Capacitor implementation to native Android using Jetpack Compose. The entire first-launch experience has been recreated with equivalent functionality and improved native Android patterns.

---

## What Was Completed

### Onboarding Components (8 files)

| File | Purpose | Status |
|------|---------|--------|
| `OnboardingState.kt` | State management and data classes | ✅ Complete |
| `OnboardingLayout.kt` | Shared shell with step indicator | ✅ Complete |
| `WelcomeScreen.kt` | First-launch welcome | ✅ Complete |
| `ProfileSetupScreen.kt` | Owner profile setup | ✅ Complete |
| `RelationshipSetupScreen.kt` | Relationship setup | ✅ Complete |
| `PersonalizationSetupScreen.kt` | Theme + text size selection | ✅ Complete |
| `AppLockSetupScreen.kt` | Optional PIN setup | ✅ Complete |
| `SetupCompleteScreen.kt` | Celebration + navigation | ✅ Complete |
| `OnboardingFlow.kt` | Flow management + OnboardingGate | ✅ Complete |

---

## File Structure

```
app/src/main/java/com/twohearts/app/ui/onboarding/
├── OnboardingState.kt              ← State management + data classes
├── OnboardingLayout.kt             ← Shared shell (step indicator, back nav)
├── WelcomeScreen.kt                ← First-launch welcome
├── ProfileSetupScreen.kt           ← Owner profile setup (name + birthday)
├── RelationshipSetupScreen.kt      ← Relationship setup (partner + start date)
├── PersonalizationSetupScreen.kt   ← Theme + text size selection
├── AppLockSetupScreen.kt           ← Optional PIN setup
├── SetupCompleteScreen.kt          ← Celebration + CTA
└── OnboardingFlow.kt               ← Flow management + OnboardingGate
```

---

## Key Implementation Decisions

### 1. State Management

The onboarding flow uses:
- **OnboardingStage enum** — 6 stages (FRESH, OWNER, RELATIONSHIP, PERSONALIZATION, APP_LOCK, COMPLETE)
- **OnboardingData data class** — Collects all onboarding data
- **AppStateService** — Persists onboarding stage and settings

### 2. Navigation Pattern

The flow uses a state-machine pattern:
- Each stage has a clear entry/exit condition
- Back navigation goes to previous stage
- Forward navigation validates input before proceeding
- Completion triggers profile/relationship creation and app lock setup

### 3. Validation

All input validation uses the Validator service from Stage 4:
- Name: required, 1-50 characters
- Date: yyyy-mm-dd format
- PIN: 4-8 digits, must match confirmation

### 4. Data Persistence

On completion, the flow:
1. Creates owner profile via RelationshipService
2. Creates partner profile via RelationshipService
3. Creates couple relationship via RelationshipService
4. Sets theme mode via AppStateService
5. Sets text size via AppStateService
6. Creates app lock PIN via AppLockService (if provided)
7. Marks onboarding as complete via AppStateService

### 5. App Lock Setup

The app lock setup is optional:
- Users can skip and set up later in Settings
- PIN is validated (4-8 digits) and confirmed
- PIN is stored securely via AppLockService

---

## Onboarding Stages

| Stage | Screen | Purpose | Validation |
|-------|--------|---------|------------|
| FRESH | WelcomeScreen | First-launch welcome | None |
| OWNER | ProfileSetupScreen | Owner profile setup | Name required (1-50 chars) |
| RELATIONSHIP | RelationshipSetupScreen | Relationship setup | Partner name + start date required |
| PERSONALIZATION | PersonalizationSetupScreen | Theme + text size | None (defaults provided) |
| APP_LOCK | AppLockSetupScreen | Optional PIN setup | PIN 4-8 digits + confirmation |
| COMPLETE | SetupCompleteScreen | Celebration + navigation | None |

---

## Data Flow

```
WelcomeScreen
    ↓ (Get Started)
ProfileSetupScreen
    ↓ (name, birthday)
RelationshipSetupScreen
    ↓ (partnerName, startDate)
PersonalizationSetupScreen
    ↓ (themeMode, textSize)
AppLockSetupScreen
    ↓ (pin or skip)
SetupCompleteScreen
    ↓ (Start Using TwoHearts)
→ Create profiles
→ Create relationship
→ Set theme/text size
→ Set up app lock (if PIN)
→ Mark onboarding complete
→ Navigate to app
```

---

## Dependencies

### Services Used

- **AppStateService** — Theme, text size, onboarding state
- **RelationshipService** — Profile and relationship creation
- **AppLockService** — PIN creation
- **Validator** — Input validation
- **DateTimeHelper** — Date formatting

### Components Used

- **BrandLogo** — Brand logo display
- **Button** — CTA buttons
- **Input** — Text input fields

---

## Verification Results

### Pre-Stage 5

| Check | Result |
|-------|--------|
| Stage 4 complete | ✅ |
| Core services working | ✅ |
| UI components available | ✅ |
| Migration roadmap intact | ✅ |

### Post-Stage 5

| Check | Result |
|-------|--------|
| All 9 onboarding files created | ✅ |
| WelcomeScreen implemented | ✅ |
| ProfileSetupScreen implemented | ✅ |
| RelationshipSetupScreen implemented | ✅ |
| PersonalizationSetupScreen implemented | ✅ |
| AppLockSetupScreen implemented | ✅ |
| SetupCompleteScreen implemented | ✅ |
| OnboardingFlow implemented | ✅ |
| OnboardingGate implemented | ✅ |
| Validation working | ✅ |
| Data persistence working | ✅ |
| App lock setup working | ✅ |
| No secrets introduced | ✅ |
| HEAD == origin/master | ✅ |
| Working tree clean | ✅ |

---

## Issues and Risks

### Issues Encountered

1. **Service dependencies** — OnboardingFlow needs multiple services. Resolved by passing services via constructor parameters.

2. **State persistence** — Onboarding data needs to survive configuration changes. Resolved by using remember + mutableStateOf for UI state, and AppStateService for persistence.

### Risks

| Risk | Mitigation | Status |
|------|-----------|--------|
| Incomplete onboarding | OnboardingGate checks state on app start | ✅ Mitigated |
| PIN security | Uses AppLockService with PBKDF2 | ✅ Mitigated |
| Data loss | All data persisted via services | ✅ Mitigated |

---

## Files Changed

### New Files (9)

1. `app/src/main/java/com/twohearts/app/ui/onboarding/OnboardingState.kt`
2. `app/src/main/java/com/twohearts/app/ui/onboarding/OnboardingLayout.kt`
3. `app/src/main/java/com/twohearts/app/ui/onboarding/WelcomeScreen.kt`
4. `app/src/main/java/com/twohearts/app/ui/onboarding/ProfileSetupScreen.kt`
5. `app/src/main/java/com/twohearts/app/ui/onboarding/RelationshipSetupScreen.kt`
6. `app/src/main/java/com/twohearts/app/ui/onboarding/PersonalizationSetupScreen.kt`
7. `app/src/main/java/com/twohearts/app/ui/onboarding/AppLockSetupScreen.kt`
8. `app/src/main/java/com/twohearts/app/ui/onboarding/SetupCompleteScreen.kt`
9. `app/src/main/java/com/twohearts/app/ui/onboarding/OnboardingFlow.kt`

**Documentation (1):**
1. `Migration/Stage-5/STAGE-5-ONBOARDING-FLOW-MIGRATION.md`

---

## Next Stage

**Stage 6 — App Shell & Navigation Migration** will:

1. Define all routes (copy RoutePath constants exactly)
2. Port the route map structure
3. Port AppShell layout (scrollable content + bottom nav)
4. Port BottomNav (5-position with center brand button)
5. Port navConfig and navIcons
6. Port Android back button behavior
7. Port route transitions
8. Verify all routes navigate correctly

---

**Stage 5 is complete. Do not proceed to Stage 6.**
