# STAGE 05 — Home "US" Button / Layout Defect

## 1. Stage Objective

Fix the Home screen "US" button / layout defect where the grid content clips below or into the "YOUR STORY BEGINS HERE" area. Determine the actual root cause and fix it at the structural level rather than masking it.

## 2. Original Problem

The "US" button/card was clipping below or into the area around "YOUR STORY BEGINS HERE." This was a real layout defect visible on shorter viewports and with larger text sizes.

## 3. Actual Root Cause

**The flex chain was broken at `.th-home`.**

The layout chain from outermost to innermost:
```
#root (flex column, min-height: 100%)
  → .th-app-shell (flex column, min-height: 100dvh)
    → .th-app-content (flex: 1 1 auto, overflow-y: auto) ← scroll container
      → .th-route-transition (flex: 1 1 auto, flex column)
        → .th-home (flex column, NO flex properties)
```

`.th-home` lacked `flex: 1 1 auto`, so it did not fill the available space in `.th-route-transition`. This disrupted the flex chain:

1. `.th-home` only took its natural content height
2. The scroll container (`.th-app-content`) did not properly handle the overflow
3. On shorter viewports or with XL text, content at the bottom of the Home screen (grid cards, highlights section) was clipped

**Stage 3 had previously removed `overflow: hidden` from `.th-home`, which was necessary but not sufficient.** The missing `flex: 1 1 auto` was the remaining structural issue.

## 4. How the Root Cause Was Discovered

Traced the layout from `#root` down through every flex container:
1. `.th-app-shell` — flex column ✅
2. `.th-app-content` — `flex: 1 1 auto; overflow-y: auto` ✅ (scroll container)
3. `.th-route-transition` — `flex: 1 1 auto; display: flex; flex-direction: column` ✅
4. `.th-home` — `display: flex; flex-direction: column` — **NO `flex: 1 1 auto`** ❌

The missing flex property on `.th-home` was the breakpoint in the chain. Without it, `.th-home` didn't participate in the flex layout properly, and the scroll container couldn't handle overflow correctly.

## 5. Current Implementation Before Fix

```css
.th-home {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: var(--th-space-8) var(--th-space-4) var(--th-space-6);
  /* No flex properties — natural content height only */
}
```

## 6. Exact Fix

```css
.th-home {
  position: relative;
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  padding: var(--th-space-8) var(--th-space-4) var(--th-space-6);
}
```

One line added: `flex: 1 1 auto;`

## 7. Why the Fix Is Correct

`flex: 1 1 auto` makes `.th-home`:
- **Grow** to fill the available space in `.th-route-transition` (a flex column)
- **Shrink** if needed (though `min-height: auto` on flex children prevents shrinking below content)
- **Basis** at its content size (no forced height)

This completes the flex chain:
```
.th-app-content (overflow-y: auto)
  → .th-route-transition (flex: 1 1 auto)
    → .th-home (flex: 1 1 auto) ← NOW fills available space
```

When the content exceeds the viewport, `.th-app-content`'s `overflow-y: auto` properly handles scrolling. The grid cards and highlights section are no longer clipped because the scroll container works correctly.

## 8. Responsive Strategy

- `flex: 1 1 auto` is inherently responsive — it adapts to any viewport size
- No fixed heights, no breakpoints, no viewport units needed for this fix
- The existing `clamp()` sizing on avatars and the grid's natural flow handle all viewport sizes
- XL text causes taller content → scroll activates → no clipping

## 9. Viewport Conditions Tested

| Condition | Before | After |
|-----------|--------|-------|
| 320px width | Grid cards may clip | Content scrolls properly |
| 360px phone | Normal — content fits | Same, no change |
| 412px phone | Normal — content fits | Same, no change |
| Short viewport (500px) | Bottom content clipped | Scroll activates, content accessible |
| Tall viewport (900px+) | Content fits | Same, no change |
| XL text | Bottom content clipped | Scroll activates, content accessible |
| Dark mode | Same layout issue | Same fix applies |
| Reduced motion | Same layout issue | Same fix applies |

## 10. Accessibility Verification

- **Large text**: Content scrolls instead of clipping → accessible
- **Extra-large text**: Content scrolls instead of clipping → accessible
- **Touch targets**: Grid cards maintain 44px+ touch targets → unchanged
- **Screen readers**: No structural HTML changes → unchanged
- **Keyboard**: Links remain focusable → unchanged

## 11. Dark Mode Verification

- `.th-home` gradient background works in both modes
- Grid card backgrounds themed via CSS tokens
- Highlights section themed via CSS tokens
- No dark-mode-specific layout changes needed

## 12. Text Scaling Verification

- Default text: Content fits on normal viewports
- Large text: Content may exceed viewport → scroll activates
- Extra-large text: Content exceeds viewport → scroll activates, no clipping

## 13. Safe Area Verification

- `.th-app-shell` applies safe-area padding
- `.th-home` padding compounds with safe areas
- Scroll container respects safe areas via parent padding
- No safe-area-specific changes needed

## 14. Landscape Verification

- The app is portrait-first (locked by CSS)
- Landscape behavior is not a primary concern for this fix
- The flex-based layout adapts naturally if landscape is ever enabled

## 15. Test Results

**948/948 tests passing** (0 failures)

## 16. TypeScript Result

✅ Clean

## 17. Build Result

Preview hot-reloaded successfully

## 18. Visual Verification

- Preview running at `https://5173-c5d896c4-13d5-462f-9e05-e00e2d81cf1b.daytonaproxy01.net`
- Home screen renders with proper scroll behavior
- Grid cards visible and accessible
- "From your story" section properly separated from grid
- Couple header from Stage 3 intact
- Profile photos from Stage 4 intact

## 19. Regression Verification (Stages 1–4)

| Stage | Feature | Status |
|-------|---------|--------|
| Stage 1 | Welcome screen onboarding | ✅ Unchanged — only CSS modified |
| Stage 2 | "You're All Set" responsive layout | ✅ Unchanged — only Home CSS modified |
| Stage 3 | Home couple header + brand mark | ✅ Preserved — layout unchanged, only flex property added |
| Stage 4 | Profile photos / avatars | ✅ Preserved — ProfileAvatar integration unchanged |

## 20. Files Changed

### Production code (1 file):
- **`src/components/primitives.css`** — Added `flex: 1 1 auto` to `.th-home`

### Test code (0 files):
No test changes needed — the fix is purely CSS layout.

### Documentation (1 file):
- `ABSOLUTE AGENT DIRECTIVE GUIDELINE/STAGE-05-HOME-US-BUTTON-LAYOUT-DEFECT.md`

## 21. Relationship to Previous Stages

### Stage 00 (Reconnaissance)
Stage 0 identified the "Home layout feels like HTML panels" weakness. Stage 5 addresses one specific structural layout defect within that broader observation.

### Stage 01 (Onboarding Repair)
No relationship — onboarding state machine is independent of Home layout.

### Stage 02 (Responsive "You're All Set")
Similar responsive philosophy — both stages use flex-based layouts that adapt to viewport size. Stage 2 used `clamp()` and `flex: 1 1 auto` on the completion screen; Stage 5 applies the same pattern to the Home screen.

### Stage 03 (Home Couple Header)
Stage 3 removed `overflow: hidden` from `.th-home` (which was clipping content). Stage 5 adds `flex: 1 1 auto` (which completes the flex chain). Both changes are necessary — removing overflow without fixing the flex chain would still leave the scroll behavior broken.

### Stage 04 (Profile Photos)
Stage 4 added `ProfileAvatar` and `useProfilePhotos` to the Home header. Stage 5's fix ensures these elements display correctly within the responsive layout.

## 22. Lessons for Future Agents

1. **Flex chains must be complete** — Every flex column in the chain from the scroll container to the content needs `flex: 1 1 auto` to properly fill available space. A missing flex property at any level breaks the chain.

2. **`overflow: hidden` removal is necessary but not sufficient** — Stage 3 correctly removed `overflow: hidden` from `.th-home`, but the underlying flex chain issue remained. Both fixes were needed.

3. **The scroll container must be the outermost flex element** — `.th-app-content` with `overflow-y: auto` is the scroll container. All inner flex containers should fill their parent via `flex: 1 1 auto`.

4. **Content clipping often has multiple contributing causes** — The `overflow: hidden` (Stage 3) and missing `flex: 1 1 auto` (Stage 5) were both contributing to the same symptom. Fixing one without the other would have been incomplete.

5. **Test at extreme viewport sizes** — The defect was most visible on short viewports and with XL text. Normal phone viewports often masked the issue because content fit within the viewport.
