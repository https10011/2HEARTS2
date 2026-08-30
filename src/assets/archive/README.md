# Asset Archive

This directory preserves TwoHearts assets that are no longer active in the current
product but retain historical or reference value.

## Structure

```
archive/
├── legacy-branding/    — Previous brand assets (old logos, wordmarks)
├── legacy-floral/      — Previous floral assets not in active use
├── legacy-decorations/ — Previous decorative elements
└── README.md           — This file
```

## Rules

1. **Never delete** archived assets without explicit product direction
2. **Never import** from archive in active application code
3. **Archive** includes: original 77 screens, legacy brand assets, unused reference assets
4. **Active assets** live in `src/assets/branding/`, `src/assets/decorations/`, etc.

## Active Asset Structure

```
src/assets/
├── branding/          — twohearts-logo.svg, twohearts-logo-mark.svg, twohearts-app-icon.svg
├── decorations/       — rose-lily-01 through rose-lily-20 (20 floral SVGs)
├── images/            — onboarding-welcome-photo.svg
├── yuki/              — yuki-cat.svg (Stage 8 companion character)
└── archive/           — This directory (preserved reference assets)
```

All active assets are locally bundled, offline-ready, and serve the current product identity.
