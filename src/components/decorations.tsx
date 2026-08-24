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
import rl09 from '../assets/decorations/rose-lily-09.svg';
import rl11 from '../assets/decorations/rose-lily-11.svg';
import rl12 from '../assets/decorations/rose-lily-12.svg';
import rl14 from '../assets/decorations/rose-lily-14.svg';
import rl15 from '../assets/decorations/rose-lily-15.svg';
import rl16 from '../assets/decorations/rose-lily-16.svg';
import rl18 from '../assets/decorations/rose-lily-18.svg';

/** Approved Rose/Lily variants, numbered like the source files (01–20). */
const ROSE_LILY_SRC = {
  1: rl01, 2: rl02, 3: rl03, 4: rl04, 5: rl05, 6: rl06, 7: rl07,
  9: rl09, 11: rl11, 12: rl12, 14: rl14, 15: rl15, 16: rl16, 18: rl18,
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
export type OnboardingArtVariant =
  | 'couple-hearts'
  | 'celebration-heart'
  | 'paired-hearts-check'
  | 'relationship-hearts'
  | 'personal-profile'
  | 'personalization-card'
  | 'security-lock';
export interface OnboardingArtProps {
  variant: OnboardingArtVariant;
  size?: number;
}

export function OnboardingArt({ variant, size = 160 }: OnboardingArtProps) {
  if (variant === 'paired-hearts-check') {
    // Reference 07 — two overlapping hearts with a check medallion.
    return (
      <svg width={size} height={size * 0.82} viewBox="0 0 200 164" fill="none" aria-hidden="true">
        <path
          d="M84 128C84 128 28 92 28 56C28 36 44 24 62 24C72 24 80 28 84 34C88 28 96 24 106 24C124 24 140 36 140 56C140 92 84 128 84 128Z"
          fill="var(--th-color-burgundy)"
        />
        <path
          d="M132 136C132 136 88 108 88 80C88 64 100 54 114 54C122 54 128 57 132 62C136 57 142 54 150 54C164 54 176 64 176 80C176 108 132 136 132 136Z"
          fill="var(--th-color-blush)"
          stroke="var(--th-color-rose-muted)"
          strokeWidth="2"
        />
        <circle
          cx="112"
          cy="116"
          r="34"
          fill="var(--th-color-surface)"
          stroke="var(--th-color-beige)"
          strokeWidth="3"
        />
        <path
          d="M96 116l12 12 22-24"
          stroke="var(--th-color-burgundy)"
          strokeWidth="6"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    );
  }
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
  if (variant === 'relationship-hearts') {
    return (
      <svg width={size} height={size * 0.88} viewBox="0 0 160 140" fill="none" aria-hidden="true">
        <path
          d="M80 120C80 120 30 85 30 52C30 34 42 24 56 24C65 24 73 28 80 36C87 28 95 24 104 24C118 24 130 34 130 52C130 85 80 120 80 120Z"
          fill="var(--th-color-blush)"
          stroke="var(--th-color-burgundy)"
          strokeWidth="2"
        />
        <path
          d="M60 68C60 68 48 60 48 52C48 46 52 42 58 42C61 42 63 44 64 46C65 44 67 42 70 42C76 42 80 46 80 52C80 60 68 68 68 68"
          fill="var(--th-color-rose-muted)"
          opacity="0.5"
        />
        <path
          d="M92 68C92 68 80 60 80 52C80 46 84 42 90 42C93 42 95 44 96 46C97 44 99 42 102 42C108 42 112 46 112 52C112 60 100 68 100 68"
          fill="var(--th-color-rose-muted)"
          opacity="0.3"
        />
      </svg>
    );
  }
  if (variant === 'personal-profile') {
    return (
      <svg width={size} height={size * 0.93} viewBox="0 0 120 110" fill="none" aria-hidden="true">
        <circle cx="60" cy="44" r="28" fill="var(--th-color-blush)" stroke="var(--th-color-burgundy)" strokeWidth="2" />
        <circle cx="60" cy="36" r="12" fill="var(--th-color-surface)" stroke="var(--th-color-rose-muted)" strokeWidth="1.5" />
        <path
          d="M42 58C42 58 42 52 48 48C54 44 60 44 60 44C60 44 66 44 72 48C78 52 78 58 78 58C78 66 60 76 60 76C60 76 42 66 42 58Z"
          fill="var(--th-color-burgundy)"
          opacity="0.15"
        />
        <circle cx="60" cy="96" r="6" fill="var(--th-color-rose-muted)" opacity="0.3" />
        <path
          d="M52 96L56 92L60 96L64 92L68 96"
          stroke="var(--th-color-burgundy)"
          strokeWidth="1.5"
          strokeLinecap="round"
          opacity="0.3"
        />
      </svg>
    );
  }
  if (variant === 'personalization-card') {
    return (
      <svg width={size} height={size * 0.92} viewBox="0 0 120 110" fill="none" aria-hidden="true">
        <rect x="20" y="16" width="80" height="70" rx="16" fill="var(--th-color-blush)" stroke="var(--th-color-burgundy)" strokeWidth="2" />
        <rect x="32" y="28" width="56" height="8" rx="4" fill="var(--th-color-rose-muted)" opacity="0.3" />
        <rect x="32" y="42" width="40" height="8" rx="4" fill="var(--th-color-rose-muted)" opacity="0.2" />
        <rect x="32" y="56" width="48" height="8" rx="4" fill="var(--th-color-rose-muted)" opacity="0.15" />
        <circle cx="60" cy="88" r="4" fill="var(--th-color-burgundy)" opacity="0.3" />
      </svg>
    );
  }
  if (variant === 'security-lock') {
    return (
      <svg width={size} height={size * 0.92} viewBox="0 0 120 110" fill="none" aria-hidden="true">
        <rect x="28" y="48" width="64" height="50" rx="12" fill="var(--th-color-blush)" stroke="var(--th-color-burgundy)" strokeWidth="2" />
        <path
          d="M44 48V36C44 27.2 51.2 20 60 20C68.8 20 76 27.2 76 36V48"
          stroke="var(--th-color-burgundy)"
          strokeWidth="3"
          strokeLinecap="round"
        />
        <circle cx="60" cy="72" r="8" fill="var(--th-color-surface)" stroke="var(--th-color-burgundy)" strokeWidth="2" />
        <circle cx="60" cy="72" r="3" fill="var(--th-color-burgundy)" />
        <line x1="60" y1="75" x2="60" y2="86" stroke="var(--th-color-burgundy)" strokeWidth="2" strokeLinecap="round" />
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
