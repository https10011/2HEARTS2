# Stage 7 — Home, Us, & Core Hub Screens Migration

**Status:** ✅ Complete  
**Date:** 2026-08-30  
**Preceded by:** Stage 6 (App Shell & Navigation Migration)  
**Followed by:** Stage 8 (Content Features Migration)

---

## Overview

Stage 7 migrated the main dashboard and relationship hub screens from the legacy React/Vite/Capacitor implementation to native Android using Jetpack Compose. The HomeScreen, UsScreen, and MoreScreen have been recreated with equivalent functionality.

---

## What Was Completed

### Screens (3 files)

| File | Purpose | Status |
|------|---------|--------|
| `HomeScreen.kt` | Main dashboard with couple header, greeting, counter, action cards | ✅ Complete |
| `UsScreen.kt` | Relationship hub with grouped navigation cards | ✅ Complete |
| `MoreScreen.kt` | Utility links screen with settings, search, about | ✅ Complete |

### Shared Components (2 files)

| File | Purpose | Status |
|------|---------|--------|
| `CouplePair.kt` | Displays owner and partner avatars | ✅ Complete |
| `RelationshipCounter.kt` | Shows days together | ✅ Complete |

### Modified Files (1)

| File | Purpose | Status |
|------|---------|--------|
| `AppRouter.kt` | Updated to use HomeScreen, UsScreen, MoreScreen | ✅ Complete |

---

## File Structure

```
app/src/main/java/com/twohearts/app/ui/screens/
├── shared/
│   ├── CouplePair.kt              ← Owner/partner avatar display
│   └── RelationshipCounter.kt     ← Days together counter
├── home/
│   └── HomeScreen.kt              ← Main dashboard
├── us/
│   └── UsScreen.kt                ← Relationship hub
└── more/
    └── MoreScreen.kt              ← Utility links
```

---

## Key Implementation Decisions

### 1. HomeScreen Layout

The HomeScreen matches legacy exactly:
- Couple header with owner/partner avatars (CouplePair)
- TwoHearts brand logo
- Personalized greeting (time-based)
- "Our story together" counter (RelationshipCounter)
- 4 primary action cards: Notes, Reminders, Us, Yuki
- Vertical scroll layout

### 2. UsScreen Layout

The UsScreen matches legacy exactly:
- Couple pair display
- "Our Story" group: Memories, Timeline, Important Dates
- "Our World" group: Places, Mood, Period Tracker, Vault
- Vertical scroll layout

### 3. MoreScreen Layout

The MoreScreen matches legacy exactly:
- Settings section (App, Profile, Relationship)
- Tools section (Search, Import, Storage)
- About section (About TwoHearts)
- Menu items with icons and subtitles

### 4. CouplePair Component

The CouplePair component:
- Displays two ProfileAvatar components
- Shows names below avatars
- Heart icon between avatars
- Centered layout

### 5. RelationshipCounter Component

The RelationshipCounter component:
- Calculates days since start date
- Displays "Our story together" label
- Shows day count

### 6. Navigation Integration

All screens receive `onNavigate` callback for:
- Navigation to feature screens
- Back navigation
- Deep link handling

---

## Screen Details

### HomeScreen

**Sections:**
1. Brand logo (top)
2. Couple header (owner + partner avatars)
3. Personalized greeting ("Good morning, [Name]!")
4. Relationship counter ("Our story together: X days")
5. Action cards (Notes, Reminders, Us, Yuki)

**Data Sources:**
- Owner profile (RelationshipService)
- Partner profile (RelationshipService)
- Relationship start date (RelationshipService)

### UsScreen

**Sections:**
1. Couple pair display
2. "Our Story" group (3 cards)
3. "Our World" group (4 cards)

**Navigation:**
- Memories → /app/memories
- Timeline → /app/timeline
- Important Dates → /app/us/important-dates
- Places → /app/places
- Mood → /app/mood
- Period → /app/period
- Vault → /app/vault

### MoreScreen

**Sections:**
1. Settings (3 items)
2. Tools (3 items)
3. About (1 item)

**Navigation:**
- App Settings → /app/settings
- Profile Settings → /app/settings/profile
- Relationship Settings → /app/settings/relationship
- Search → /app/search
- Import Data → /app/settings/import
- Storage → /app/settings/storage
- About → /app/about

---

## Dependencies

### Services Used

- **RelationshipService** — Owner/partner profiles, relationship data
- **DateTimeHelper** — Days since start date calculation

### Components Used

- **BrandLogo** — Brand logo display
- **ProfileAvatar** — User avatar display
- **Card** — Action/feature cards

---

## Verification Results

### Pre-Stage 7

| Check | Result |
|-------|--------|
| Stage 6 complete | ✅ |
| Navigation working | ✅ |
| Components available | ✅ |
| Services available | ✅ |
| Migration roadmap intact | ✅ |

### Post-Stage 7

| Check | Result |
|-------|--------|
| All 6 files created | ✅ |
| HomeScreen implemented | ✅ |
| UsScreen implemented | ✅ |
| MoreScreen implemented | ✅ |
| CouplePair implemented | ✅ |
| RelationshipCounter implemented | ✅ |
| AppRouter updated | ✅ |
| Navigation working | ✅ |
| No secrets introduced | ✅ |
| HEAD == origin/master | ✅ |
| Working tree clean | ✅ |

---

## Issues and Risks

### Issues Encountered

1. **Profile photos** — ProfileAvatar needs photo resolution via MediaStorage. Resolved by using photoRef parameter (will be fully integrated in later stages).

2. **Navigation callbacks** — Screens need navigation callbacks. Resolved by passing `onNavigate` lambda to each screen.

### Risks

| Risk | Mitigation | Status |
|------|-----------|--------|
| Home screen layout mismatch | Layout matches legacy exactly | ✅ Mitigated |
| Navigation broken | All routes verified | ✅ Mitigated |
| Data not loading | Services properly injected | ✅ Mitigated |

---

## Files Changed

### New Files (5)

1. `app/src/main/java/com/twohearts/app/ui/screens/shared/CouplePair.kt`
2. `app/src/main/java/com/twohearts/app/ui/screens/shared/RelationshipCounter.kt`
3. `app/src/main/java/com/twohearts/app/ui/screens/home/HomeScreen.kt`
4. `app/src/main/java/com/twohearts/app/ui/screens/us/UsScreen.kt`
5. `app/src/main/java/com/twohearts/app/ui/screens/more/MoreScreen.kt`

### Modified Files (1)

1. `app/src/main/java/com/twohearts/app/ui/navigation/AppRouter.kt` — Updated to use new screens

**Documentation (1):**
1. `Migration/Stage-7/STAGE-7-HOME-US-AND-CORE-HUB-SCREENS-MIGRATION.md`

---

## Next Stage

**Stage 8 — Content Features Migration (Notes, Memories, Timeline)** will:

1. Port Notes types + repository + service + hooks
2. Port Notes screens (Home, Editor, Detail)
3. Port Notes category system (meta + icons)
4. Port Memories types + repository + service + hooks
5. Port Memories screens (Home, Add, Detail)
6. Port Timeline types + repository + service + hooks
7. Port Timeline screens (Home, Add, Detail)
8. Verify all CRUD operations + toasts

---

**Stage 7 is complete. Do not proceed to Stage 8.**
