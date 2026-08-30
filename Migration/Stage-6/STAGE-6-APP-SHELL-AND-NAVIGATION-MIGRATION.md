# Stage 6 — App Shell & Navigation Migration

**Status:** ✅ Complete  
**Date:** 2026-08-30  
**Preceded by:** Stage 5 (Onboarding Flow Migration)  
**Followed by:** Stage 7 (Home, Us, & Core Hub Screens Migration)

---

## Overview

Stage 6 migrated the complete app shell and navigation system from the legacy React/Vite/Capacitor implementation to native Android using Navigation Compose. The entire routing, bottom navigation, and layout system has been recreated with equivalent functionality.

---

## What Was Completed

### Navigation Components (5 files)

| File | Purpose | Status |
|------|---------|--------|
| `RoutePath.kt` | All route constants | ✅ Complete |
| `NavConfig.kt` | Destination vocabulary | ✅ Complete |
| `BottomNav.kt` | 5-position floating pill nav | ✅ Complete |
| `AppShell.kt` | Content area + bottom nav | ✅ Complete |
| `AppRouter.kt` | All routes + navigation | ✅ Complete |

### Modified Files (1)

| File | Purpose | Status |
|------|---------|--------|
| `MainActivity.kt` | Updated to use AppRouter | ✅ Complete |

---

## File Structure

```
app/src/main/java/com/twohearts/app/ui/navigation/
├── RoutePath.kt              ← All route constants
├── NavConfig.kt              ← Destination vocabulary
├── BottomNav.kt              ← 5-position floating pill nav
├── AppShell.kt               ← Content area + bottom nav
└── AppRouter.kt              ← All routes + navigation
```

---

## Key Implementation Decisions

### 1. Route Constants

All route paths match legacy exactly:
- Onboarding routes (6 routes)
- Main app routes (5 routes)
- Feature routes (notes, memories, timeline, reminders, places, mood, period, vault)
- Fun routes (yuki, games)
- System routes (notifications, search, more)
- Settings routes (8 routes)

### 2. Navigation Structure

The navigation uses:
- **NavHost** — Compose navigation host
- **NavController** — Navigation state management
- **BottomNav** — 5-position floating pill navigation
- **AppShell** — Scaffold with bottom bar

### 3. Bottom Navigation

The BottomNav matches legacy exactly:
- 5 positions: Home · Notifications · **TWOHEARTS (center)** · Notes · More
- Pill-shaped floating bar with shadow
- Elevated center brand button (BrandLogo)
- Active state indicators (primary color)

### 4. Route Transitions

Route transitions use:
- **Enter:** Fade + slide up (300ms)
- **Exit:** Fade (300ms)
- **Pop Enter:** Fade (300ms)
- **Pop Exit:** Fade + slide down (300ms)

### 5. Back Button Handling

Back button behavior matches legacy:
- Deep navigation → previous screen
- Home root → no-op
- No history → home

### 6. Navigation Patterns

The navigation uses:
- **Single Top** — Avoid multiple copies of same destination
- **Save State** — Save state when switching tabs
- **Restore State** — Restore state when re-selecting tabs
- **Pop Up To Home** — Pop up to home when switching tabs

---

## Routes Implemented

### Onboarding Routes (6)

| Route | Screen | Status |
|-------|--------|--------|
| `/onboarding/welcome` | WelcomeScreen | ✅ |
| `/onboarding/profile` | ProfileSetupScreen | ✅ |
| `/onboarding/relationship` | RelationshipSetupScreen | ✅ |
| `/onboarding/personalization` | PersonalizationSetupScreen | ✅ |
| `/onboarding/app-lock` | AppLockSetupScreen | ✅ |
| `/onboarding/complete` | SetupCompleteScreen | ✅ |

### Main App Routes (5)

| Route | Screen | Status |
|-------|--------|--------|
| `/app/home` | HomeScreen | ✅ (placeholder) |
| `/app/us` | UsScreen | ✅ (placeholder) |
| `/app/more` | MoreScreen | ✅ (placeholder) |
| `/app/notifications` | NotificationsScreen | ✅ (placeholder) |
| `/app/search` | SearchScreen | ✅ (placeholder) |

### Feature Routes (40+)

All feature routes are implemented with placeholder screens:
- Notes (4 routes)
- Memories (4 routes)
- Timeline (4 routes)
- Reminders (4 routes)
- Places (4 routes)
- Mood (4 routes)
- Period (6 routes)
- Vault (3 routes)
- Yuki (1 route)
- Games (11 routes)
- Settings (9 routes)
- Important Dates (1 route)

---

## Dependencies

### Navigation Libraries

```kotlin
// Navigation Compose
implementation("androidx.navigation:navigation-compose:2.8.5")
```

### Components Used

- **BrandLogo** — Center brand button
- **LoadingState** — Loading during bootstrap

---

## Verification Results

### Pre-Stage 6

| Check | Result |
|-------|--------|
| Stage 5 complete | ✅ |
| Onboarding flow working | ✅ |
| UI components available | ✅ |
| Migration roadmap intact | ✅ |

### Post-Stage 6

| Check | Result |
|-------|--------|
| All 5 navigation files created | ✅ |
| RoutePath constants defined | ✅ |
| NavConfig implemented | ✅ |
| BottomNav implemented | ✅ |
| AppShell implemented | ✅ |
| AppRouter implemented | ✅ |
| All routes defined | ✅ |
| Route transitions working | ✅ |
| Back button handling working | ✅ |
| MainActivity updated | ✅ |
| No secrets introduced | ✅ |
| HEAD == origin/master | ✅ |
| Working tree clean | ✅ |

---

## Issues and Risks

### Issues Encountered

1. **Route parameter handling** — Routes with parameters (e.g., `:noteId`) need special handling. Resolved by using Navigation Compose's argument support.

2. **Back button behavior** — Complex back stack management needed. Resolved by using `popUpTo` with `saveState` and `restoreState`.

### Risks

| Risk | Mitigation | Status |
|------|-----------|--------|
| Route structure mismatch | All routes copied exactly from legacy | ✅ Mitigated |
| Back button incorrect | BackHandler + NavController integration | ✅ Mitigated |
| Tab state loss | saveState/restoreState enabled | ✅ Mitigated |

---

## Files Changed

### New Files (5)

1. `app/src/main/java/com/twohearts/app/ui/navigation/RoutePath.kt`
2. `app/src/main/java/com/twohearts/app/ui/navigation/NavConfig.kt`
3. `app/src/main/java/com/twohearts/app/ui/navigation/BottomNav.kt`
4. `app/src/main/java/com/twohearts/app/ui/navigation/AppShell.kt`
5. `app/src/main/java/com/twohearts/app/ui/navigation/AppRouter.kt`

### Modified Files (1)

1. `app/src/main/java/com/twohearts/app/MainActivity.kt` — Updated to use AppRouter

**Documentation (1):**
1. `Migration/Stage-6/STAGE-6-APP-SHELL-AND-NAVIGATION-MIGRATION.md`

---

## Next Stage

**Stage 7 — Home, Us, & Core Hub Screens Migration** will:

1. Port CouplePair component
2. Port relationship counter logic
3. Port HomeScreen with all sections
4. Port UsScreen with grouped navigation cards
5. Port MoreScreen with utility links
6. Port ProfileAvatar with photo resolution
7. Port useProfilePhotos hook
8. Port ProfilePhotoService (file input → canvas resize → MediaStorage)
9. Verify all data flows correctly

---

**Stage 6 is complete. Do not proceed to Stage 7.**
