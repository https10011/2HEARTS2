/**
 * Decorations — the centralized decorative-art system (Phase 23).
 *
 * Rose/Lily florals: the OWNER-APPROVED assets ("Rose Lily Vectors/") rendered
 * via the generated runtime files in src/assets/decorations/. Screens declare
 * what they want (variant, size, position, rotation, opacity, optional drift
 * animation) and never touch SVG handling. Replacing a source floral re-runs
 * `node scripts/generate-design-assets.mjs` and every consumer updates.
 *
 * Onboarding illustrations: vector art moved here from feature screens so all
 * decorative composition stays in ONE presentation-layer place. Decorations
 * are pure visuals — no repositories/services, nothing interactive inside.
 */

import rl01 from '../assets/decorations/rose-lily-01.svg';
import rl02 from '../assets/decorations/rose-lily-02.svg';
import rl03 from '../assets/decorations/rose-lily-03.svg';
import rl04 from '../assets/decorations/rose-lily-04.svg';
import rl05 from '../assets/decorations/rose-lily-05.svg';
import rl06 from '../assets/decorations/rose-lily-06.svg';
import rl07 from '../assets/decorations/rose-lily-07.svg';
import rl08 from '../assets/decorations/rose-lily-08.svg';
import rl09 from '../assets/decorations/rose-lily-09.svg';
import rl10 from '../assets/decorations/rose-lily-10.svg';
import rl11 from '../assets/decorations/rose-lily-11.svg';
import rl12 from '../assets/decorations/rose-lily-12.svg';
import rl13 from '../assets/decorations/rose-lily-13.svg';
import rl14 from '../assets/decorations/rose-lily-14.svg';
import rl15 from '../assets/decorations/rose-lily-15.svg';
import rl16 from '../assets/decorations/rose-lily-16.svg';
import rl17 from '../assets/decorations/rose-lily-17.svg';
import rl18 from '../assets/decorations/rose-lily-18.svg';
import rl19 from '../assets/decorations/rose-lily-19.svg';
import rl20 from '../assets/decorations/rose-lily-20.svg';

/** Approved Rose/Lily variants, numbered like the source files (01–20). */
const ROSE_LILY_SRC = {
  1: rl01, 2: rl02, 3: rl03, 4: rl04, 5: rl05, 6: rl06, 7: rl07, 8: rl08,
  9: rl09, 10: rl10, 11: rl11, 12: rl12, 13: rl13, 14: rl14, 15: rl15,
  16: rl16, 17: rl17, 18: rl18, 19: rl19, 20: rl20,
} as const;

export type RoseLilyVariant = keyof typeof ROSE_LILY_SRC;

export type DecorationPosition =
  | 'top-left'
  | 'top-right'
  | 'bottom-left'
  | 'bottom-right'
  | 'center';

export interface RoseLilyDecorationProps {
  /** Which approved floral to render (source file number). */
  variant: RoseLilyVariant;
  /** Rendered width in px (height follows the artwork ratio). Default 160. */
  size?: number;
  /** Corner preset inside the nearest positioned ancestor. Default bottom-right. */
  position?: DecorationPosition;
  /** 0–1. Use subtle values (~0.4–0.8) so decorations never fight content. */
  opacity?: number;
  /** Optional rotation in degrees. */
  rotate?: number;
  /** Gentle sway (disabled when the user enables reduce-motion). */
  animated?: boolean;
  className?: string;
}

/**
 * Renders a floral at an anchored position. The wrapper is absolutely
 * positioned + pointer-events:none so it never interferes with taps,
 * and the image itself is aria-hidden (pure decoration).
 */
export function RoseLilyDecoration({
  variant,
  size = 160,
  position = 'bottom-right',
  opacity = 1,
  rotate = 0,
  animated = false,
  className = '',
}: RoseLilyDecorationProps) {
  const classes = [
    'th-decor',
    `th-decor--${position}`,
    animated ? 'th-decor--animated' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');
  return (
    <span
      className={classes}
      style={{ opacity, transform: rotate ? `rotate(${rotate}deg)` : undefined }}
    >
      <img
        src={ROSE_LILY_SRC[variant]}
        width={size}
        alt=""
        aria-hidden="true"
        style={{ display: 'block', height: 'auto' }}
      />
    </span>
  );
}

/** Onboarding vector art variants (centralized inline illustrations). */
export type OnboardingArtVariant = 'couple-hearts' | 'celebration-heart';
export interface OnboardingArtProps {
  variant: OnboardingArtVariant;
  size?: number;
}

export function OnboardingArt({ variant, size = 160 }: OnboardingArtProps) {
  if (variant === 'celebration-heart') {
    return (
      <svg width={size} height={size} viewBox="0 0 120 120" fill="none" aria-hidden="true">
        <circle
          cx="60"
          cy="60"
          r="56"
          fill="var(--th-color-blush)"
          stroke="var(--th-color-burgundy)"
          strokeWidth="2"
          opacity="0.4"
        />
        <path
          d="M60 80C60 80 38 66 38 52C38 44 44 38 52 38C56 38 59 40 60 42C61 40 64 38 68 38C76 38 82 44 82 52C82 66 60 80 60 80Z"
          fill="var(--th-color-burgundy)"
        />
        <circle cx="35" cy="40" r="2" fill="var(--th-color-rose-muted)" />
        <circle cx="85" cy="38" r="2.5" fill="var(--th-color-rose-muted)" />
        <circle cx="30" cy="65" r="1.5" fill="var(--th-color-burgundy-light)" />
        <circle cx="90" cy="62" r="2" fill="var(--th-color-burgundy-light)" />
      </svg>
    );
  }
  return (
    <svg width={size} height={size} viewBox="0 0 160 160" fill="none" aria-hidden="true">
      <circle
        cx="80"
        cy="80"
        r="76"
        fill="var(--th-color-blush)"
        stroke="var(--th-color-burgundy)"
        strokeWidth="2"
        opacity="0.5"
      />
      <circle cx="60" cy="72" r="28" fill="var(--th-color-surface)" stroke="var(--th-color-burgundy-light)" strokeWidth="1.5" />
      <circle cx="100" cy="72" r="28" fill="var(--th-color-surface)" stroke="var(--th-color-burgundy-light)" strokeWidth="1.5" />
      <path
        d="M80 108C80 108 66 98 66 88C66 82 72 78 76 78C78 78 79 79 80 80C81 79 82 78 84 78C88 78 94 82 94 88C94 98 80 108 80 108Z"
        fill="var(--th-color-burgundy)"
        opacity="0.8"
      />
    </svg>
  );
}
