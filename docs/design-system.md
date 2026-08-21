# TwoHearts Design System (Phase 23)

Single source of truth for visual decisions. Change once → update everywhere.

## Branding
- Official assets: `TwoHearts-Logo-BrandName/` and `Rose Lily Vectors/` (owner sources).
- Generated runtime copies: `src/assets/branding/*.svg` and `src/assets/decorations/rose-lily-*.svg`.
- Generator: `node scripts/generate-design-assets.mjs` (re-run whenever the owner
  sources change).
- Only consumers:
  - `src/components/BrandLogo.tsx` — the ONE brand component (`brand` full lockup,
    `mark` hearts-only). Used on splash, onboarding, app lock, about, headers.
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
