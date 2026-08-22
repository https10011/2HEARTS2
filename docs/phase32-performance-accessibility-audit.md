# Phase 32 — Performance & Accessibility Audit

## Summary

Performance and accessibility audit and optimization pass for the TwoHearts
application. Focused on efficient rendering, reduced asset bloat, and verified
accessibility across all feature areas.

**Commit:** TBD
**Date:** August 2026
**Tests:** 650/650 passing
**TypeScript:** Clean
**Production build:** Successful (4.86s)

---

## Baseline Findings

| Metric | Before | After |
|---|---|---|
| Rose/Lily SVGs in dist | 20 | 14 |
| Rose/Lily dist size | ~4.0 MB | ~3.1 MB |
| JS bundle | 616.82 KB | 616.40 KB |
| CSS containment on decorations | None | `contain: content` |

---

## Optimizations Implemented

### 1. Rose/Lily Asset Tree-Shaking (6 unused variants removed)

**Issue:** All 20 Rose/Lily SVG variants were statically imported in
`decorations.tsx`, causing all 20 to be emitted in the production build
even though only 14 were actually used across screens.

**Fix:** Removed imports for unused variants (8, 10, 13, 17, 19, 20).
Only the 14 actively used variants are now imported and bundled.

**Files changed:** `src/components/decorations.tsx`

**Impact:** ~900KB of SVG assets removed from dist. No visual changes.

### 2. CSS Containment on Decorative Elements

**Issue:** Decorative elements (absolute-positioned florals, ambient containers)
had no CSS containment hints, potentially causing unnecessary layout recalculation
when decorations change.

**Fix:** Added `contain: content` to `.th-decor` and `.th-decor-ambient` classes.
This tells the browser the element's contents are independent and won't affect
outside layout, enabling better rendering optimization.

**Files changed:** `src/components/primitives.css`

**Impact:** Reduced layout scope for decorative elements. No visual changes.

---

## Animation Performance Audit

### Keyframe Analysis (20 keyframes audited)

| Keyframe | Properties | Compositor-Friendly |
|---|---|---|
| th-fade-in | opacity | ✅ |
| th-slide-up | transform | ✅ |
| th-route-in | transform, opacity | ✅ |
| th-spin | transform | ✅ |
| th-sway | transform | ✅ |
| th-toast-in | transform, opacity | ✅ |
| th-fade-out | opacity | ✅ |
| th-scale-in | transform, opacity | ✅ |
| th-dialog-in | transform, opacity | ✅ |
| th-fade-rise | transform, opacity | ✅ |
| th-decor-drift | transform | ✅ |
| th-decor-pulse | opacity | ✅ |
| th-decor-sway | transform | ✅ |
| th-game-enter | transform, opacity | ✅ |
| th-game-correct | transform | ✅ |
| th-game-incorrect | transform | ✅ |
| th-card-flip | transform | ✅ |
| th-score-pulse | transform | ✅ |
| th-badge-enter | transform, opacity | ✅ |
| th-result-enter | transform, opacity | ✅ |

**Result:** All 20 keyframe animations use only `transform` and/or `opacity` —
no layout-triggering properties (width, height, margin, padding, top, left).

### Infinite Animations

| Animation | Usage | Reduced-Motion Support |
|---|---|---|
| th-spin (spinner) | Loading states only | ✅ Frozen |
| th-sway | Decorative | ✅ Stops at 1ms |
| th-decor-drift | Ambient decoration | ✅ Stops at 1ms |
| th-decor-sway | Ambient decoration | ✅ Stops at 1ms |

All infinite animations collapse to 1ms when `data-th-motion='reduced'` or
`prefers-reduced-motion: reduce`.

---

## Reduced-Motion Verification

| Mechanism | Status |
|---|---|
| OS prefers-reduced-motion | ✅ Respected via @media query |
| In-app reduceMotion setting | ✅ Respected via data-th-motion attribute |
| Duration tokens collapse | ✅ All durations → 1ms |
| Decorative animations stop | ✅ All infinite animations freeze |
| Route transitions reduce | ✅ Route animations disabled |
| Game animations reduce | ✅ Game animations disabled |
| Spinner freezes | ✅ Spinner stops |
| Toast animations reduce | ✅ Toast entrance reduced |
| Modal animations reduce | ✅ Modal transitions reduced |

**Result:** No information is communicated solely through motion. All feedback
remains understandable without animation.

---

## Touch Target Audit

| Control | Minimum Target | Status |
|---|---|---|
| Bottom navigation items | 44px+ | ✅ |
| Central TwoHearts button | 44px+ | ✅ |
| Back buttons | 44px+ | ✅ |
| Game answer options | 44px+ | ✅ |
| Settings toggle switches | 44px+ | ✅ |
| Card tap targets | 44px+ | ✅ |
| Modal actions | 44px+ | ✅ |
| FAB buttons | 44px+ | ✅ |
| Toast close | 44px+ | ✅ |

---

## Color/Contrast Audit

| Element | Light Mode | Dark Mode |
|---|---|---|
| Primary text | Burgundy on cream ✅ | Light on dark ✅ |
| Secondary text | Muted on cream ✅ | Muted on dark ✅ |
| Buttons | High contrast ✅ | High contrast ✅ |
| Selected states | Burgundy accent ✅ | Burgundy accent ✅ |
| Game correct | Green on light bg ✅ | Green on dark bg ✅ |
| Game incorrect | Red on light bg ✅ | Red on dark bg ✅ |
| Decorations | Low opacity ✅ | Low opacity ✅ |

---

## Offline-First Verification

| Check | Status |
|---|---|
| No cloud dependency | ✅ |
| No network requirement | ✅ |
| Local storage preserved | ✅ |
| Local media preserved | ✅ |
| All SVGs local | ✅ |
| All assets bundled | ✅ |

---

## React/Render Performance

| Check | Status |
|---|---|
| Game service memoization | ✅ useMemo for GameService |
| Game content selection | ✅ useMemo for question selection |
| Settings subscription | ✅ useSyncExternalStore (efficient) |
| No unnecessary rerenders from settings | ✅ |
| Stable component references | ✅ |

---

## Files Changed

| File | Changes |
|---|---|
| `src/components/decorations.tsx` | Removed 6 unused Rose/Lily imports |
| `src/components/primitives.css` | Added `contain: content` to .th-decor and .th-decor-ambient |

---

## Validation

| Check | Result |
|---|---|
| Tests | **650/650 pass** |
| TypeScript | ✅ Clean |
| Production build | ✅ Success (4.86s) |
| Capacitor sync | ✅ Success |
| Android build | NOT VERIFIED (no JDK/SDK in environment) |

---

## Items Intentionally NOT Changed

| Item | Reason |
|---|---|
| Rose/Lily visual appearance | Preserved — optimization was asset loading only |
| Decorative animations | All compositor-friendly, no performance issue |
| Game engine | No rendering issues found |
| Main JS bundle splitting | Would require Vite config changes (out of scope) |
| Memory virtualization | Datasets are small (< 500 items typical) |
| Image lazy loading | Media is loaded on-demand via CapFilesystem |
| will-change properties | Already present on animated elements |

---

## Scope Verification

| Check | Result |
|---|---|
| No Phase 33 work | ✅ |
| No V2/cloud | ✅ |
| No architecture rewrite | ✅ |
| No functionality removed | ✅ |
| Phase 23–31 systems preserved | ✅ |
| Visual identity preserved | ✅ |
| Offline-first preserved | ✅ |
