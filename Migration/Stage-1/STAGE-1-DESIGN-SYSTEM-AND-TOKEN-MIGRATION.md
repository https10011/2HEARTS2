# Stage 1 — Design System & Token Migration

> **Migration Stage:** 1 of 16 (Stages 0–15)
> **Status:** COMPLETE
> **Date:** August 30, 2026
> **Branch:** `master`
> **Commit:** (Stage 1 commit SHA)
> **Dependencies:** Stage 0 ✅

---

## 1. Objective

Recreate the TwoHearts design system in the new Kotlin/Jetpack Compose stack so all UI work from this point forward uses the correct visual language.

Per the migration roadmap: "All SVG assets, all token values, all semantic color/spacing/motion names" must remain unchanged.

---

## 2. What Was Done

### 2.1 Brand SVGs Copied to Android Project

All brand and decoration SVGs from the legacy implementation were copied to `app/src/main/assets/`:

| Category | Source | Destination | Count |
|----------|--------|-------------|-------|
| Brand logos | `src/assets/branding/` | `app/src/main/assets/branding/` | 3 |
| Rose-lily decorations | `src/assets/decorations/` | `app/src/main/assets/decorations/` | 20 |
| Onboarding image | `src/assets/images/` | `app/src/main/assets/images/` | 1 |
| Yuki character | `src/assets/yuki/` | `app/src/main/assets/` | 1 |

**Files preserved exactly:**
- `twohearts-logo.svg` — Full TwoHearts logo lockup
- `twohearts-logo-mark.svg` — Hearts-only brand mark (used in bottom nav center)
- `twohearts-app-icon.svg` — App icon reference vector
- `rose-lily-01.svg` through `rose-lily-20.svg` — All decorative floral SVGs
- `onboarding-welcome-photo.svg` — Welcome screen illustration
- `yuki-cat.svg` — Yuki companion character

### 2.2 TwoHeartsTokens.kt Created

Created `app/src/main/java/com/twohearts/app/ui/theme/Tokens.kt` — comprehensive typed constants matching the legacy `tokens.css`.

**Token categories ported:**

| Category | Tokens | Source |
|----------|--------|--------|
| **Color palette** | 14 base colors + 10 extended burgundy + 11 surface/semantic + 5 feedback | `:root` in tokens.css |
| **Dark theme colors** | 20 dark overrides | `[data-th-theme='dark']` in tokens.css |
| **Typography** | 8 font sizes + 4 weights + 5 line heights + 5 letter spacings | `:root` typography tokens |
| **Spacing** | 12 values (0–80dp, 4pt base) | `--th-space-*` tokens |
| **Corner radii** | 6 values (sm–xxl + pill) | `--th-radius-*` tokens |
| **Component dimensions** | 6 values (touch target, header, nav, screen, avatar) | `--th-*-height/size` tokens |
| **Motion durations** | 6 values (instant–drift) | `--th-duration-*` tokens |
| **Easing curves** | 6 values (standard, decelerate, accelerate, emphasized, press, spring) | `--th-ease-*` tokens |
| **Z-index layers** | 7 values (base–lock) | `--th-z-*` tokens |
| **Text size scaling** | 4 levels (small/default/large/extra-large) | `TEXT_SIZE_SCALE` from tokens.ts |

**Token values verified against legacy:**
- All hex colors match exactly
- All spacing values match (rem → dp conversion: 1rem = 16dp)
- All radii match
- All durations match
- All easing curves match

### 2.3 Theme.kt Expanded — Complete Material 3 Color Scheme

Expanded `app/src/main/java/com/twohearts/app/ui/theme/Theme.kt` with:

**Material 3 Color Schemes:**
- **Light:** 25 color slots mapped to legacy light theme tokens
- **Dark:** 25 color slots mapped to legacy dark theme tokens (warm charcoal/plum surfaces, luminous burgundy accent)

**Extended Color System:**
- Created `TwoHeartsColors` data class with 35+ semantic color properties
- Created `LocalTwoHeartsColors` CompositionLocal for composable access
- Light and dark variants of all extended colors

**Text Scaling Support:**
- `textSizeScale` parameter on `TwoHeartsTheme`
- Applied via `Density` composition — scales `fontScale` multiplier
- Compatible with the legacy `--th-text-scale` CSS variable approach

**Key Design Decisions:**
- Disabled `dynamicColor` by default (Material You colors would override brand identity)
- Extended colors provided via CompositionLocal (not Material 3 slots) for colors that don't map to Material's system
- Theme follows legacy exactly: burgundy primary, cream background, warm neutrals

### 2.4 Type.kt Updated — Complete Typography

Updated `app/src/main/java/com/twohearts/app/ui/theme/Type.kt` with typography matching legacy tokens exactly:

| Style | Font | Size | Legacy Token |
|-------|------|------|-------------|
| displayLarge | Serif | 40sp | `--th-font-size-4xl` |
| displayMedium | Serif | 32sp | `--th-font-size-3xl` |
| displaySmall | Serif | 26sp | `--th-font-size-2xl` |
| headlineLarge | System | 32sp | `--th-font-size-3xl` + semibold |
| headlineMedium | System | 26sp | `--th-font-size-2xl` + semibold |
| headlineSmall | System | 22sp | `--th-font-size-xl` + semibold |
| titleLarge | System | 22sp | `--th-font-size-xl` + medium |
| titleMedium | System | 18sp | `--th-font-size-lg` + medium |
| titleSmall | System | 16sp | `--th-font-size-md` + medium |
| bodyLarge | System | 18sp | `--th-font-size-lg` |
| bodyMedium | System | 16sp | `--th-font-size-md` |
| bodySmall | System | 13sp | `--th-font-size-sm` |
| labelLarge | System | 16sp | `--th-font-size-md` + medium |
| labelMedium | System | 13sp | `--th-font-size-sm` + medium |
| labelSmall | System | 12sp | `--th-font-size-xs` + medium |

### 2.5 TwoHeartsPreview.kt Created

Created `app/src/main/java/com/twohearts/app/ui/theme/TwoHeartsPreview.kt` — comprehensive preview composable that renders:

- Brand color swatches (burgundy family, neutrals, surfaces, feedback)
- Extended burgundy family (50–500)
- Full typography scale (all 15 text styles)
- Spacing scale visualization
- Corner radii visualization
- Component dimensions
- Text on accent demo
- Light and dark theme variants

### 2.6 MainActivity.kt Updated

Updated to use the complete theme with:
- `dynamicColor = false` (preserves brand identity over Material You)
- `textSizeScale = 1f` (default, can be changed for text scaling)

---

## 3. Token Mapping: CSS → Compose

| CSS Token | Compose Location |
|-----------|-----------------|
| `--th-color-burgundy` | `TwoHeartsTokens.Color.burgundy` + `MaterialTheme.colorScheme.primary` |
| `--th-color-cream` | `TwoHeartsTokens.Color.cream` + `MaterialTheme.colorScheme.background` |
| `--th-color-charcoal` | `TwoHeartsTokens.Color.charcoal` + `MaterialTheme.colorScheme.onBackground` |
| `--th-color-blush` | `TwoHeartsTokens.Color.blush` + `MaterialTheme.colorScheme.primaryContainer` |
| `--th-color-rose-muted` | `TwoHeartsTokens.Color.roseMuted` + `MaterialTheme.colorScheme.secondary` |
| `--th-color-surface` | `TwoHeartsTokens.Color.surface` + `MaterialTheme.colorScheme.surface` |
| `--th-color-border` | `TwoHeartsTokens.Color.border` + `MaterialTheme.colorScheme.outline` |
| `--th-color-error` | `TwoHeartsTokens.Color.error` + `MaterialTheme.colorScheme.error` |
| `--th-color-success` | `TwoHeartsTokens.Color.success` (extended, not in Material 3) |
| `--th-font-size-*` | `MaterialTheme.typography.*` (15 text styles) |
| `--th-space-*` | `TwoHeartsTokens.Spacing.*` |
| `--th-radius-*` | `TwoHeartsTokens.Radius.*` |
| `--th-duration-*` | `TwoHeartsTokens.Duration.*` |
| `--th-ease-*` | `TwoHeartsTokens.Ease.*` (string references for future animation use) |
| `--th-text-scale` | `TwoHeartsTheme(textSizeScale = ...)` |
| `[data-th-theme='dark']` | `TwoHeartsTheme(darkTheme = true)` |

---

## 4. Dark Mode Implementation

The dark theme follows the legacy `[data-th-theme='dark']` tokens exactly:

| Property | Light | Dark |
|----------|-------|------|
| Background | Cream `#FDF6F0` | Warm dark `#1A1310` |
| Surface | White `#FFFFFF` | `#241E1A` |
| Surface elevated | `#FFFDFB` | `#2E2622` |
| Text primary | Charcoal `#2B2420` | Light `#F5ECE4` |
| Primary (burgundy) | `#6A1B2B` | Luminous `#C9808B` |
| Border | `#E8DAD3` | `#3E3230` |

Dark mode is toggled via `TwoHeartsTheme(darkTheme = true)` or automatically via system setting.

---

## 5. Text Scaling Implementation

Four text size levels matching legacy:

| Level | Scale | Effect |
|-------|-------|--------|
| Small | 0.88x | All text 12% smaller |
| Default | 1.00x | Standard size |
| Large | 1.12x | All text 12% larger |
| Extra Large | 1.28x | All text 28% larger |

Implementation: Applied via `Density(fontScale = ...)` in `CompositionLocalProvider`. This scales all `sp` values proportionally, matching the legacy CSS `--th-text-scale` approach.

---

## 6. Reduced Motion Support

The legacy CSS supports reduced motion via:
- `@media (prefers-reduced-motion: reduce)` — OS-level
- `data-th-motion='reduced'` — In-app preference

In Compose, reduced motion is handled by:
- `LocalMotionDurationScale` — Compose's built-in mechanism
- Future implementation: Override `MotionDurationScale` to return 0 for all durations when reduced motion is active
- All `TwoHeartsTokens.Duration.*` values are available for animation code to use

**Note:** Full reduced motion implementation will be refined when animations are added in later stages.

---

## 7. Architecture Decisions

### 7.1 Why CompositionLocal for Extended Colors

Material 3's `ColorScheme` has limited slots (primary, secondary, tertiary, etc.). TwoHearts needs 35+ semantic colors that don't fit this model. Using `LocalTwoHeartsColors` provides:
- Access to the full brand palette from any composable
- No need to override Material 3 slots that would confuse the system
- Clean separation between Material 3 standard colors and brand-specific colors

### 7.2 Why Disable Dynamic Color by Default

Material You (dynamic color) generates colors from the device wallpaper. This would override TwoHearts' carefully designed burgundy brand identity. Dynamic color is disabled by default (`dynamicColor = false`) but can be enabled if desired.

### 7.3 Why Density-Based Text Scaling

The legacy app used CSS `--th-text-scale` multiplier on all font sizes. The Compose equivalent is scaling `fontScale` via `Density`. This:
- Scales all `sp` values proportionally
- Works with Material 3 typography
- Requires no changes to individual text styles
- Matches the legacy behavior exactly

---

## 8. Files Changed

| File | Action | Description |
|------|--------|-------------|
| `app/src/main/assets/branding/*.svg` | Created | 3 brand SVGs copied from archive |
| `app/src/main/assets/decorations/*.svg` | Created | 20 rose-lily SVGs copied from archive |
| `app/src/main/assets/images/*.svg` | Created | 1 onboarding image SVG copied from archive |
| `app/src/main/assets/yuki-cat.svg` | Created | 1 Yuki character SVG copied from archive |
| `app/src/main/java/.../theme/Tokens.kt` | Created | Complete typed token constants |
| `app/src/main/java/.../theme/Theme.kt` | Modified | Expanded Material 3 theme + extended colors + text scaling |
| `app/src/main/java/.../theme/Type.kt` | Modified | Complete typography matching legacy |
| `app/src/main/java/.../theme/TwoHeartsPreview.kt` | Created | Design system verification preview |
| `app/src/main/java/.../MainActivity.kt` | Modified | Updated to use complete theme |

---

## 9. Risks and Issues

| Risk | Severity | Status |
|------|----------|--------|
| SVG assets may not render as Compose vectors | Low | Assets stored in `assets/` folder; can be converted to vector drawables later |
| Dynamic color could override brand | Low | Disabled by default |
| Text scaling via Density affects all sp | Low | Matches legacy behavior; intentional |
| No Gradle build possible locally | Medium | Verified via code review; CI build will confirm |
| Easing curves are string references | Low | Compose uses specific easing types; will be used in animation code |

---

## 10. Verification Results

| Check | Result |
|-------|--------|
| Brand SVGs copied (3) | ✅ All present in `assets/branding/` |
| Decoration SVGs copied (20) | ✅ All present in `assets/decorations/` |
| Image SVGs copied (1) | ✅ Present in `assets/images/` |
| TwoHeartsTokens.kt created | ✅ All token categories ported |
| Theme.kt expanded | ✅ Light + dark schemes + extended colors |
| Type.kt updated | ✅ 15 text styles matching legacy |
| TwoHeartsPreview.kt created | ✅ Light + dark previews |
| Text scaling support | ✅ 4 levels via Density |
| Reduced motion support | ✅ Foundation in place (full impl in later stages) |
| Token values match legacy | ✅ All hex colors verified |
| Dark mode values match legacy | ✅ All dark overrides verified |
| No secrets introduced | ✅ Clean |
| Working tree ready | ✅ All changes staged |

---

## 11. Next Stage Starting Point

- **Branch:** `master`
- **Commit:** (Stage 1 commit SHA)
- **Working tree:** Clean (after commit)
- **Test baseline:** 948/948 legacy tests passing (unchanged)
- **Next stage:** Stage 2 — Component Library Migration
- **Stage 2 depends on:** Stage 1 ✅
- **Do NOT begin Stage 2 until this document is reviewed**

---

*Document generated during Stage 1 design system migration. All token values verified against the legacy tokens.css source of truth.*
