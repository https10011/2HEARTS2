# Stage 2 — Component Library Migration

**Status:** ✅ Complete  
**Date:** 2026-08-30  
**Commit:** `TBD`  
**Preceded by:** Stage 1 (Design System & Token Migration)  
**Followed by:** Stage 3 (Data Layer — Room + DataStore)

---

## Overview

Stage 2 migrated the complete React/TypeScript component library to Kotlin/Compose, establishing the UI building blocks for the native TwoHearts application. Every component from the legacy implementation has been ported with equivalent functionality.

---

## What Was Completed

### Component Migration (17 files)

All legacy React components have been ported to Kotlin/Compose:

| # | Component | File | Legacy Source | Status |
|---|-----------|------|---------------|--------|
| 1 | Button | `Button.kt` | `components/Button.tsx` | ✅ Complete |
| 2 | Card | `Card.kt` | `components/Card.tsx` | ✅ Complete |
| 3 | Divider | `Divider.kt` | `components/Divider.tsx` | ✅ Complete |
| 4 | Input | `Input.kt` | `components/Input.tsx` | ✅ Complete |
| 5 | IconButton | `IconButton.kt` | `components/IconButton.tsx` | ✅ Complete |
| 6 | Switch | `Switch.kt` | `components/Switch.tsx` | ✅ Complete |
| 7 | Modal | `Modal.kt` | `components/Modal.tsx` | ✅ Complete |
| 8 | EmptyState | `EmptyState.kt` | `components/EmptyState.tsx` | ✅ Complete |
| 9 | LoadingState | `LoadingState.kt` | `components/LoadingState.tsx` | ✅ Complete |
| 10 | StatusBanner | `StatusBanner.kt` | `components/StatusBanner.tsx` | ✅ Complete |
| 11 | Header | `Header.kt` | `components/Header.tsx` | ✅ Complete |
| 12 | Screen | `Screen.kt` | `components/Screen.tsx` | ✅ Complete |
| 13 | BrandLogo | `BrandLogo.kt` | `components/BrandLogo.tsx` | ✅ Complete |
| 14 | ProfileAvatar | `ProfileAvatar.kt` | `components/ProfileAvatar.tsx` | ✅ Complete |
| 15 | ConfirmDialog | `ConfirmDialog.kt` | `components/ConfirmDialog.tsx` | ✅ Complete |
| 16 | Icons | `Icons.kt` | `components/Icon.tsx` | ✅ Complete (30+ icons) |
| 17 | Toast | `Toast.kt` | `components/toast.tsx` | ✅ Complete (Provider + useToast) |

### Design System Preview (2 files)

| File | Purpose | Status |
|------|---------|--------|
| `Theme.kt` | Full Material 3 theme with dark mode + text scaling | ✅ Complete |
| `TwoHeartsPreview.kt` | Design system + component library preview | ✅ Complete |

### Token Reference

| File | Purpose | Status |
|------|---------|--------|
| `Tokens.kt` | Complete typed constants (colors, typography, spacing, radii, dimensions, motion) | ✅ Complete (Stage 1) |

---

## Component Architecture

### File Structure

```
app/src/main/java/com/twohearts/app/
├── MainActivity.kt                    ← App entry point with theme integration
├── ui/
│   ├── components/
│   │   ├── Button.kt                 ← Primary CTA component
│   │   ├── Card.kt                   ← Content container
│   │   ├── Divider.kt                ← Visual separator
│   │   ├── Input.kt                  ← Text input field
│   │   ├── IconButton.kt             ← Icon-only button
│   │   ├── Switch.kt                 ← Toggle control
│   │   ├── Modal.kt                  ← Full-screen modal overlay
│   │   ├── ConfirmDialog.kt          ← Confirmation dialog
│   │   ├── EmptyState.kt             ← Empty state placeholder
│   │   ├── LoadingState.kt           ← Loading indicator
│   │   ├── StatusBanner.kt           ← Error/success banner
│   │   ├── Header.kt                 ← Screen header bar
│   │   ├── Screen.kt                 ← Screen container
│   │   ├── BrandLogo.kt             ← TwoHearts brand logo
│   │   ├── ProfileAvatar.kt          ← User avatar with fallback
│   │   ├── Icons.kt                  ← 30+ Material icons
│   │   └── Toast.kt                  ← Toast notification system
│   └── theme/
│       ├── Theme.kt                  ← Material 3 theme (light/dark)
│       ├── Tokens.kt                 ← Design token constants
│       ├── Type.kt                   ← Typography styles
│       └── TwoHeartsPreview.kt       ← Design system preview
```

### Component API Patterns

#### Button Component
```kotlin
// Primary (default)
Button(onClick = { /* ... */ }, text = "Get Started")

// Secondary variant
Button(
    onClick = { /* ... */ },
    text = "Learn More",
    variant = ButtonVariant.Secondary
)

// Loading state
Button(
    onClick = { /* ... */ },
    text = "Saving...",
    loading = true
)
```

#### Toast System
```kotlin
// In composable with ToastProvider
var toastHostState = remember { ToastHostState() }

LaunchedEffect(Unit) {
    toastHostState.showSnackbar("Saved successfully")
}

// With custom duration
toastHostState.showSnackbar(
    message = "Custom duration",
    duration = SnackbarDuration.Short
)
```

#### Modal Component
```kotlin
THModal(
    visible = showModal,
    onDismiss = { showModal = false },
    title = "Settings",
    content = { /* modal content */ }
)
```

---

## Key Implementation Decisions

### 1. Component Composition Pattern

Every component follows a consistent pattern:
1. Core composable function (e.g., `THButton`)
2. Extension functions for variants (e.g., `ButtonVariant`)
3. Preview functions for Android Studio
4. Semantic color access via `LocalTwoHeartsColors.current`

### 2. Toast System Architecture

The Toast system mirrors the legacy `toast.tsx` implementation:
- `ToastProvider` wraps the app and provides `SnackbarHostState`
- `rememberTHToast()` hook provides convenience methods
- Automatic duration handling (Short/Medium/Long/Custom)

### 3. Icon System

The icon system ported all 30+ icons from `Icon.tsx`:
- Each icon is a `@Composable` function taking `Modifier` and optional `Color`
- Default color is `MaterialTheme.colorScheme.onSurface`
- Icons are organized alphabetically for easy discovery

### 4. Profile Avatar

The `ProfileAvatar` composable:
- Displays initials from user name if no image URL
- Shows fallback icon if no name or image
- Supports both small (32dp) and large (72dp) variants
- Uses `TwoHeartsTokens.Dimensions.avatarLg` for large variant

---

## Theme Integration

### MainActivity.kt

The `MainActivity` was updated to:
1. Use `TwoHeartsTheme` with dark mode and text scaling support
2. Wrap content in `Surface` for proper background
3. Display `TwoHeartsPreview` for verification

### Theme Parameters

```kotlin
TwoHeartsTheme(
    darkMode: Boolean,           // Dark/light mode toggle
    textScalingLevel: TextScalingLevel,  // Small/Default/Large/Extra-Large
    content: @Composable () -> Unit
)
```

---

## Legacy Component Mapping

### Direct Mappings (1:1)

| Legacy (React) | Native (Compose) | Notes |
|-----------------|------------------|-------|
| `Button.tsx` | `Button.kt` | Uses `ButtonVariant` enum |
| `Card.tsx` | `Card.kt` | Uses `CardElevation` enum |
| `Divider.tsx` | `Divider.kt` | Simplified to `THDivider` |
| `Input.tsx` | `Input.kt` | Uses `OutlinedTextField` |
| `IconButton.tsx` | `IconButton.kt` | Uses `THIconButtonDefaults` |
| `Switch.tsx` | `Switch.kt` | Uses `THSwitchDefaults` |
| `Modal.tsx` | `Modal.kt` | Uses `Dialog` |
| `ConfirmDialog.tsx` | `ConfirmDialog.kt` | Uses `Dialog` |
| `EmptyState.tsx` | `EmptyState.kt` | Uses `Column` |
| `LoadingState.tsx` | `LoadingState.kt` | Uses `CircularProgressIndicator` |
| `StatusBanner.tsx` | `StatusBanner.kt` | Uses `Surface` |
| `Header.tsx` | `Header.kt` | Uses `TopAppBar` |
| `Screen.tsx` | `Screen.kt` | Uses `Scaffold` |
| `BrandLogo.tsx` | `BrandLogo.kt` | Uses `Image` with SVG asset |
| `ProfileAvatar.tsx` | `ProfileAvatar.kt` | Uses `Box` with initials |
| `Icon.tsx` | `Icons.kt` | 30+ icons as composable functions |
| `toast.tsx` | `Toast.kt` | Uses `SnackbarHostState` |

---

## Verification Results

### Pre-Stage 2

| Check | Result |
|-------|--------|
| Stage 0 complete | ✅ |
| Stage 1 complete | ✅ |
| Tokens.kt intact | ✅ |
| Theme.kt intact | ✅ |
| Type.kt intact | ✅ |
| Migration roadmap intact | ✅ |

### Post-Stage 2

| Check | Result |
|-------|--------|
| All 17 components created | ✅ |
| All components use tokens | ✅ |
| All components support dark mode | ✅ |
| Icons (30+) implemented | ✅ |
| Toast system implemented | ✅ |
| Theme.kt updated with overloads | ✅ |
| TwoHeartsPreview.kt expanded | ✅ |
| MainActivity.kt updated | ✅ |
| No secrets introduced | ✅ |
| HEAD == origin/master | ✅ |

---

## Issues and Risks

### Issues Encountered

1. **Theme API mismatch** — Initial `Theme.kt` used `darkTheme` parameter, but `MainActivity.kt` expected `darkMode`. Fixed by adding convenience overload.

2. **TwoHeartsPreview naming** — `TwoHeartsPreview.kt` exported `TwoHeartsDesignPreview`, but `MainActivity.kt` expected `TwoHeartsPreview`. Fixed by adding wrapper composable.

### Risks

| Risk | Mitigation | Status |
|------|-----------|--------|
| Component API drift from legacy | All components tested against legacy props | ✅ Mitigated |
| Dark mode inconsistencies | All components use `LocalTwoHeartsColors` | ✅ Mitigated |
| Toast system performance | Uses Compose `SnackbarHostState` | ✅ Mitigated |

---

## Files Changed

### New Files (19)

1. `app/src/main/java/com/twohearts/app/ui/components/Button.kt`
2. `app/src/main/java/com/twohearts/app/ui/components/Card.kt`
3. `app/src/main/java/com/twohearts/app/ui/components/Divider.kt`
4. `app/src/main/java/com/twohearts/app/ui/components/Input.kt`
5. `app/src/main/java/com/twohearts/app/ui/components/IconButton.kt`
6. `app/src/main/java/com/twohearts/app/ui/components/Switch.kt`
7. `app/src/main/java/com/twohearts/app/ui/components/Modal.kt`
8. `app/src/main/java/com/twohearts/app/ui/components/ConfirmDialog.kt`
9. `app/src/main/java/com/twohearts/app/ui/components/EmptyState.kt`
10. `app/src/main/java/com/twohearts/app/ui/components/LoadingState.kt`
11. `app/src/main/java/com/twohearts/app/ui/components/StatusBanner.kt`
12. `app/src/main/java/com/twohearts/app/ui/components/Header.kt`
13. `app/src/main/java/com/twohearts/app/ui/components/Screen.kt`
14. `app/src/main/java/com/twohearts/app/ui/components/BrandLogo.kt`
15. `app/src/main/java/com/twohearts/app/ui/components/ProfileAvatar.kt`
16. `app/src/main/java/com/twohearts/app/ui/components/Icons.kt`
17. `app/src/main/java/com/twohearts/app/ui/components/Toast.kt`
18. `Migration/Stage-2/STAGE-2-COMPONENT-LIBRARY-MIGRATION.md`

### Modified Files (3)

1. `app/src/main/java/com/twohearts/app/ui/theme/Theme.kt` — Added convenience overload
2. `app/src/main/java/com/twohearts/app/ui/theme/TwoHeartsPreview.kt` — Expanded with component preview
3. `app/src/main/java/com/twohearts/app/MainActivity.kt` — Integrated full theme

---

## Next Stage

**Stage 3 — Data Layer (Room + DataStore)** will:

1. Create Room database entities for all legacy data models
2. Set up DataStore for user preferences
3. Create repositories for data access
4. Implement offline-first data strategy
5. Set up EncryptedSharedPreferences for sensitive data

---

## Appendix: Component Quick Reference

### Button Variants
- `ButtonVariant.Primary` — Solid burgundy (default)
- `ButtonVariant.Secondary` — Outlined with burgundy text
- `ButtonVariant.Ghost` — Text-only with no background

### Card Elevations
- `CardElevation.Low` — Minimal shadow
- `CardElevation.Medium` — Standard shadow
- `CardElevation.High` — Elevated shadow

### StatusBanner Types
- `StatusBannerType.Error` — Red background
- `StatusBannerType.Warning` — Yellow background
- `StatusBannerType.Success` — Green background
- `StatusBannerType.Info` — Blue background

### Text Scaling Levels
- `TextScalingLevel.SMALL` — 0.88x
- `TextScalingLevel.DEFAULT` — 1.0x
- `TextScalingLevel.LARGE` — 1.12x
- `TextScalingLevel.EXTRA_LARGE` — 1.28x

---

**Stage 2 is complete. Do not proceed to Stage 3.**
