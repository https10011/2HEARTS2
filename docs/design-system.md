# TwoHearts Design System (Phase 23)

Single source of truth for visual decisions. Change once → update everywhere.

## Branding
- Official assets: `TwoHearts-Logo-BrandName/` and `Rose Lily Vectors/` (owner sources).
- Generated runtime copies: `src/assets/branding/*.svg` and `src/assets/decorations/rose-lily-*.svg`.
- Generator: `node scripts/generate-design-assets.mjs` (re-run whenever the owner
  sources change).
- Only consumers:
  - `src/components/BrandLogo.tsx` — the ONE brand component (`brand` full lockup,
    `mark` hearts-only). Used on splash, onboarding, app lock, about, headers,
    and the bottom-nav center hub button (Phase 24).
  - `BrandLogo` accepts `tone="light"` to recolor the SAME official asset to
    cream for dark brand surfaces; in the dark theme every `th-brand-logo`
    recolors automatically via one rule in `primitives.css`.
  - `src/components/decorations.tsx` — `ROSE_LILY_DECORATIONS` map +
    `RoseLilyDecoration(id, preset)`, `OnboardingArtVariant`. No feature may import
    the SVG assets directly (guarded by `tests/designTokens.test.ts`).

## Tokens (`src/theme/tokens.css`)
- Color: brand (burgundy family), neutrals, semantic (success/warning/error/info),
  game-specific, decorative. Dark theme recolors from `[data-th-theme="dark"]`.
- Typography: `--th-font-size-*` scale wrapped in `--th-text-scale`.
- Spacing: `--th-space-1..12` (1rem = 16px base).
- Radius: `--th-radius-*` (xs → full).
- Shadows: `--th-shadow-*` (soft 2–3 layer elevations: sm / md / lg / xl).
- Motion: `--th-motion-{fast,normal,emphasized}`, `--th-ease-{standard,entrance,exit}`.
- Breakpoint layering & safe-area in global css components.
- Typed mirror: `src/theme/tokens.ts` (import names like `tokens.color.burgundy`).
- Guard: `tests/designTokens.test.ts` asserts every `var(--th-*)` referenced anywhere
  in src/tests resolves to a definition in tokens.css, and no other file defines
  `--th-*` tokens.

## Motion & feedback (Phase 25)
- One interaction layer in `src/components/primitives.css` ("Motion &
  feedback (Phase 25)"); features must not ship screen-local transitions or
  hardcoded durations/easings (guarded by `tests/phase25-motion.test.ts`).
- Semantic motion pairs already existed (`--th-motion-{fast,standard,
  emphasized,entrance,exit,press,modal,drift}`); Phase 25 adds
  `--th-duration-spin` (indeterminate spinner cycle) and these primitives:
  - `.th-pressable` — generic scale-press feedback for clickable content.
  - `.th-toast-viewport` + `.th-toast[--success|--error|--info]` — ONE toast
    host (`ToastProvider`, mounted once in AppShell above the bottom nav);
    screens publish via `useToast()` from `src/components/toast.tsx`
    (auto-dismiss `TOAST_DURATION_MS` = 2.4s + tokenized exit fade, so toasts
    never linger; the host survives the navigation the action triggered).
  - `th-scale-in` — one calm entrance for empty-state visuals
    (`.th-empty-state__visual` AND the legacy `.th-empty-state__icon`).
  - `th-dialog-in` — the modal vocabulary (fade + rise) applied to the
    shared centered `.th-modal` dialogs (bottom sheets already use
    `.th-modal-overlay` fade + `th-slide-up`).
  - `LoadingState` uses the single `.th-spinner` (`th-spin` keyframe exists
    exactly once) + a visible caption so reduced-motion users still see
    status (spinner freezes via OS setting or `data-th-motion='reduced'`).
- Theme flips (light ↔ dark) animate surface `background-color`/`border-color`
  via one scoped rule in `src/styles/global.css` (no per-property
  choreography, no JS timing).
- `IconInfo` added to the centralized Icon set (toast info variant).

## Text scaling
- `core/appSettings.ts` (schema v3) → `data-th-text-scale` attribute on root; `TEXT_SIZE_SCALE`
  mapping in `src/theme/tokens.ts`. All `--th-font-size-*` multiply by `var(--th-text-scale)`.
- Immediate propagation: `appSettingsStore` subscribers apply the scale synchronously.

## Theme
- Light/dark/system in `core/appSettings.ts` → `applyThemeMode` sets `data-th-theme`.
- Immediate propagation via `themeStore` subscriber; dark colors defined under
  `[data-th-theme="dark"]` in tokens.css.

## Icons
- One set: `src/components/Icon.tsx` (stroke-based icons, `size` in px) re-exported
  via `src/components/index.ts` (+ `IconProps` type export).
- No emoji glyph icons in feature screens (MasterPrompt §22). Feature vocab emojis
  (mood picker, memory-match tiles) are content and remain in `src/data/mood` /
  `src/services/game/gameEngine.ts`.

## Shared primitives
- `src/components/`: Button, Card, Screen, Header, IconButton, Input, Divider,
  EmptyState, LoadingState, Modal, Icon set + BrandLogo + decorations.
  One CSS source: `src/components/primitives.css`.

## Verification
- `tests/designTokens.test.ts` — token integrity + brand/decorations single-consumer
  guards + no-emoji-Icons guard.
- Propagation was verified by temporarily mutating tokens.css / brand assets,
  rebuilding, and confirming the value appears in `dist/` (value restored after).
