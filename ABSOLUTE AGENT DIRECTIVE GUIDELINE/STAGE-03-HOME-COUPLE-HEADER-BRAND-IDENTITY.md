# STAGE 03 — Home Screen / Couple Header / Brand Identity

## 1. Stage Objective

Redesign the Home screen's couple header to immediately communicate "THIS IS OUR SPACE." The header must visually establish two people, their relationship, and the TwoHearts brand identity in a cohesive, emotionally warm composition. Fix the "US" button clipping issue. Ensure the composition is responsive across all viewport sizes and text scales.

## 2. Original Problem

The previous Home header had several weaknesses:
1. **Weak couple identity**: Two 72px avatar circles with a small heart icon between them didn't feel like a meaningful relationship presentation.
2. **Wrong brand usage**: The full BrandLogo (92px wide, with text "TwoHearts" + tagline) was used as the header centerpiece, but it competed with the avatars rather than connecting them. The brand mark (interlocked hearts) should be the visual connector.
3. **Content clipping**: `.th-home { overflow: hidden }` clipped content that extended beyond the container bounds, causing the "US" grid card and highlights section to be cut off on shorter viewports or with large text.
4. **No emotional warmth**: The header felt like a UI layout, not a couple's shared space.

## 3. Root Cause

**Content clipping**: `.th-home { overflow: hidden }` was the direct cause of the "US" button clipping. The `.th-app-content` container already handles scrolling, so `overflow: hidden` on `.th-home` served no purpose and actively clipped content.

**Weak header composition**: The original layout placed the full brand lockup above two small avatars, creating a vertical stack that prioritized the logo over the people. The brand mark (interlocked hearts) — the visual symbol of the relationship — was not used as a connector.

## 4. Previous Behavior

```
Previous header layout:
┌──────────────────────────┐
│   [TwoHearts full logo]  │  92px wide brand lockup
│  [Avatar] ♥ [Avatar]     │  72px circles + small heart
│    "X years together"    │  pill text
└──────────────────────────┘

Problems:
- Brand logo dominates, avatars feel secondary
- Heart icon between avatars is 16px — too small to matter
- overflow: hidden clips content below
```

## 5. New Design Decision

**Composition**: Two prominent avatar circles flanking the official TwoHearts brand mark (interlocked hearts), with names beneath and a relationship counter pill.

```
New header layout:
┌──────────────────────────┐
│  [Avatar]  ⓗ  [Avatar]  │  Large avatars + brand mark connector
│   "Name"    "Name"      │  Names beneath avatars
│  "X years together"     │  pill text
└──────────────────────────┘
```

### Why this composition works:

1. **People first**: The avatars are the largest elements — the two people are the emotional center.
2. **Brand as connector**: The mark (interlocked hearts) sits between them, visually representing the relationship. It's wrapped in a warm burgundy circle with the white mark inside.
3. **Personal**: Each avatar has a name beneath, making it feel like real people rather than generic UI elements.
4. **Balanced**: The three elements (avatar — mark — avatar) create visual symmetry.
5. **Responsive**: Avatar sizes use `clamp()` to scale with viewport width.

### Why NOT the full brand logo?

The full brand lockup (interlocked hearts + "TwoHearts" text + tagline) is too wide and text-heavy to sit between two avatars. The mark-only variant (just the interlocked hearts) is the right symbol — it represents the relationship without competing with the couple's identity.

## 6. Files Changed

### Production code (2 files):

- **`src/features/app-shell/screens/HomeScreen.tsx`**
  - Removed `AvatarChip` component (replaced by inline couple header)
  - Removed `Profile` type import (no longer needed)
  - Rewrote header section: two `<Link>` avatars flanking a `<BrandLogo variant="mark">` connector
  - Each avatar has a circle with initial + name beneath
  - Added `th-home-couple`, `th-home-couple__avatar`, `th-home-couple__circle`, `th-home-couple__initial`, `th-home-couple__name`, `th-home-couple__mark` classes
  - Updated JSDoc to describe the new composition

- **`src/components/primitives.css`**
  - `.th-home`: Removed `overflow: hidden` (the root cause of content clipping)
  - Removed old `.th-home-couple-avatars` and `.th-home-couple-avatars__heart` classes
  - Added new `.th-home-couple` flex container with responsive `clamp()` gap
  - Added `.th-home-couple__avatar` — clickable column with circle + name, responsive sizing via `clamp(72px, 22vw, 96px)`
  - Added `.th-home-couple__circle` — `clamp(64px, 18vw, 84px)` with gradient background, border, shadow
  - Added `.th-home-couple__initial` — display font initial
  - Added `.th-home-couple__name` — truncated name with responsive max-width
  - Added `.th-home-couple__mark` — 48px burgundy circle with white BrandLogo mark inside
  - Updated `@media (max-width: 360px)` to use new class names

### Test code (2 files):

- **`tests/phase24-home-navigation.test.ts`** — Updated assertion from `variant="brand"` to `variant="mark"`
- **`tests/stage3-home-shell.test.ts`** — Updated `variant="brand"` → `variant="mark"`, `th-home-couple-avatars__heart` → `th-home-couple__mark`, `.th-home-couple-avatars` → `.th-home-couple`, `.th-home-couple-avatars__heart` → `.th-home-couple__mark`

### Documentation (1 file):

- `ABSOLUTE AGENT DIRECTIVE GUIDELINE/STAGE-03-HOME-COUPLE-HEADER-BRAND-IDENTITY.md`

## 7. Implementation Decisions

### Why `clamp()` for avatar sizing?

`clamp(64px, 18vw, 84px)` provides:
- **Minimum 64px**: Never becomes too small to read the initial
- **Preferred 18vw**: Scales proportionally with viewport width
- **Maximum 84px**: Caps at a comfortable size on wider screens

On a 360px phone: `max(64, min(64.8, 84)) = 64.8px`
On a 412px phone: `max(64, min(74.16, 84)) = 74.16px`
On a 480px tablet: `max(64, min(86.4, 84)) = 84px` (capped)

### Why the mark is wrapped in a circle?

The brand mark (interlocked hearts) is white artwork designed for light surfaces. Wrapping it in a 48px burgundy circle with `filter: brightness(0) invert(1)` creates:
- A warm, branded connector element
- Visual weight between the two avatars
- The burgundy circle echoes the brand color
- The white hearts "glow" against the dark background

### Why remove `overflow: hidden`?

`.th-app-content` already provides `overflow-y: auto` for scrolling. `.th-home` doesn't need its own overflow constraint. The `overflow: hidden` was clipping content on shorter viewports and with larger text, which was the root cause of the "US" button clipping.

### Why inline the avatars instead of reusing CouplePair?

The `CouplePair` component (used in UsScreen) has its own fixed styling (`th-couple-pair`, `th-couple-pair__face`, etc.) that doesn't match the Home header's larger scale. The Home header needs:
- Larger circles (64-84px vs CouplePair's fixed size)
- Names beneath (CouplePair doesn't show names)
- Brand mark connector (CouplePair uses a heart icon)

Rather than making CouplePair configurable (which would add complexity to a shared component), the Home header has its own inline composition. This keeps the concerns separated — CouplePair serves the Us hub; the Home header serves the couple's first impression.

## 8. Responsive Strategy

| Viewport | Avatar Size | Mark Size | Gap | Behavior |
|----------|------------|-----------|-----|----------|
| 320px narrow | 56px circle | 40px | var(--th-space-2) | Media query override |
| 360px phone | ~65px | 48px | clamp responsive | Natural |
| 412px phone | ~74px | 48px | clamp responsive | Natural |
| 480px tablet | 84px (cap) | 48px | clamp responsive | Natural |
| XL text | Same circles, names truncate | Same | Same | Text scales independently |

## 9. Accessibility

- **Touch targets**: Avatar links have `min-height: var(--th-touch-target-min)` via the column layout
- **Labels**: `aria-label="Your profile"` and `aria-label="Partner profile"` on avatar links
- **Mark is decorative**: `aria-hidden="true"` on the brand mark connector
- **Focus order**: Avatar → Mark (hidden) → Avatar — natural left-to-right
- **Keyboard**: Links are keyboard-focusable via `<Link>` (renders `<a>`)

## 10. Dark Mode

- Avatar circles: `var(--th-color-blush)` gradient works on both light and dark surfaces
- Mark circle: Burgundy background + white hearts — consistent in both modes
- Name text: `var(--th-color-text-secondary)` — themed automatically
- `th-couple-header-backdrop` radial gradient: opacity reduced to 0.2 in dark mode

## 11. Test Results

**948/948 tests passing** (0 failures)

## 12. TypeScript Result

✅ Clean

## 13. Build Result

Preview hot-reloaded successfully

## 14. Limitations

1. **Profile photos**: Avatars still show initials (no image upload). Full photo system belongs to a later stage. The larger circles make the initials more prominent and visually appealing.

2. **Browser-only verification**: The preview environment is a browser, not an Android device. Safe-area inset behavior on actual devices cannot be fully verified here.

3. **Old CSS classes preserved**: `.th-home-avatar`, `.th-home-avatar__circle`, `.th-home-avatar__initial`, `.th-home-avatar__name`, `.th-home-couple-avatars`, `.th-home-couple-avatars__heart` still exist in the CSS but are no longer used by HomeScreen. They could be cleaned up in a future stage.

## 15. Deferred Work

- Full profile photo system (Stage 4)
- Bottom nav pill shape (noted in reconnaissance)
- Complete UI/UX overhaul (Stage 9+)
- CSS cleanup of unused old avatar classes

## 16. Architectural Systems Preserved

- BrandLogo component (unchanged — just using `variant="mark"` instead of `variant="brand"`)
- RoseLilyDecoration system (unchanged)
- Design token system (unchanged)
- Navigation architecture (unchanged)
- AppShell layout (unchanged)
- Home highlights system (unchanged)
- Home grid items (unchanged)
- Greeting row (unchanged)
- Dark mode system (unchanged)
- Reduced motion system (unchanged)
- Toast system (unchanged)
- All feature modules (unchanged)

## 17. Lessons for Future Agents

1. **`overflow: hidden` on content containers is dangerous** — the scroll container (`.th-app-content`) already handles overflow. Adding `overflow: hidden` on child containers clips content unexpectedly on different viewport sizes.

2. **BrandLogo `variant="mark"` is the right connector for couple presentations** — the interlocked hearts represent the relationship. The full `variant="brand"` (with text) is better for splash screens and headers where the brand name matters.

3. **`clamp()` is the responsive sizing primitive** — avatar circles, gaps, and mark size all use `clamp()` for continuous scaling without breakpoints.

4. **The Home header is the most important screen in TwoHearts** — it must immediately communicate "THIS IS OUR SPACE." The composition of two people + their relationship mark achieves this.

5. **Test assertions match the design intent** — tests that check for `variant="brand"` need updating when the design decision changes to `variant="mark"`. This is expected and correct.
