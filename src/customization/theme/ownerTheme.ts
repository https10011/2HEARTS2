/* ------------------------------------------------------------
 * TWOHEARTS — OWNER THEME CUSTOMIZATION
 * ------------------------------------------------------------
 * This file controls the TwoHearts color + base typography identity.
 *
 * You can safely change the HEX color values below to retheme the app
 * without touching feature logic. Tokens here feed the CSS variables
 * used everywhere (see src/theme/tokens.css).
 *
 * Format: standard CSS hex strings.
 * ------------------------------------------------------------
 */

export const ownerTheme = {
  // Primary brand accent. Used strategically — not screen-flooding.
  burgundy: '#6A1B2B',
  burgundyLight: '#8E3147',
  burgundyDark: '#4A0F1D',

  // Supporting warm-neutral palette.
  cream: '#FDF6F0',
  blush: '#F6E1DE',
  roseMuted: '#C9808B',
  beige: '#EDE0D4',
  charcoal: '#2B2420',

  // Base font families. Replace with a bundled @font-face family if you
  // add custom fonts (keep offline — MasterPrompt §65).
  fontFamilyBase: "'Segoe UI', system-ui, -apple-system, sans-serif",
  fontFamilyDisplay: "'Georgia', 'Times New Roman', serif",
} as const;

export type OwnerTheme = typeof ownerTheme;
