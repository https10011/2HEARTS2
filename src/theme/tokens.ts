/**
 * TwoHearts design tokens — typed mirror (Stage 10 enhanced).
 *
 * The CSS custom properties in tokens.css are the runtime source of truth.
 * This module exposes typed constants for use in JS/TS.
 *
 * Keep in sync with src/theme/tokens.css.
 */

export const palette = {
  burgundy: '#6A1B2B',
  burgundyLight: '#8E3147',
  burgundyDark: '#4A0F1D',
  cream: '#FDF6F0',
  blush: '#F6E1DE',
  roseMuted: '#C9808B',
  pink: '#E8A0B4',
  beige: '#EDE0D4',
  charcoal: '#2B2420',
  neutralSoft: '#F2E9E4',
  warmIvory: '#FBF4ED',
  dustyRose: '#C9A0A8',
  plum: '#7A3F5E',
  sage: '#8B9E7C',
} as const;

/** Semantic brand aliases (JS mirror of tokens.css). */
export const brand = {
  primary: palette.burgundy,
  secondary: palette.burgundyLight,
  deep: palette.burgundyDark,
  soft: palette.roseMuted,
  wash: palette.blush,
} as const;

export const duration = {
  instant: 1,
  fast: 100,
  normal: 200,
  slow: 320,
  drift: 6400,
} as const;

export const ease = {
  standard: 'cubic-bezier(0.25, 0.46, 0.45, 0.94)',
  decelerate: 'cubic-bezier(0, 0, 0.2, 1)',
  accelerate: 'cubic-bezier(0.4, 0, 1, 1)',
  emphasized: 'cubic-bezier(0.2, 0, 0, 1)',
  press: 'cubic-bezier(0.3, 0, 0.2, 1)',
  spring: 'cubic-bezier(0.34, 1.56, 0.64, 1)',
} as const;

/** Semantic motion pairs: `${durationMs} ${cubicBezier}` ready for JS-driven transitions. */
export const motion = {
  fast: `${duration.fast}ms ${ease.standard}`,
  standard: `${duration.normal}ms ${ease.standard}`,
  slow: `${duration.slow}ms ${ease.emphasized}`,
  entrance: `${duration.normal}ms ${ease.decelerate}`,
  exit: `${duration.fast}ms ${ease.accelerate}`,
  press: `${duration.fast}ms ${ease.press}`,
  modal: `${duration.normal}ms ${ease.decelerate}`,
  drift: `${duration.drift}ms ease-in-out`,
  spring: `${duration.slow}ms ${ease.spring}`,
} as const;

/** Elevation layers (JS mirror of --th-z-*). */
export const zIndex = {
  base: 0,
  content: 1,
  sticky: 5,
  nav: 10,
  fab: 100,
  modal: 1000,
  lock: 1100,
} as const;

/**
 * System-wide text-size setting (MasterPrompt §28).
 * Maps a user-facing option to a CSS scale multiplier applied to
 * --th-text-scale on the root element.
 */
export type TextSizeKey = 'small' | 'default' | 'large' | 'extra-large';

export const TEXT_SIZE_SCALE: Record<TextSizeKey, number> = {
  small: 0.88,
  default: 1,
  large: 1.12,
  'extra-large': 1.28,
};

export const TEXT_SIZE_LABELS: Record<TextSizeKey, string> = {
  small: 'Small',
  default: 'Default',
  large: 'Large',
  'extra-large': 'Extra Large',
};
