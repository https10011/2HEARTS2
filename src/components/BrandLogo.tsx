/**
 * BrandLogo — the ONE authoritative TwoHearts brand mark (Phase 20).
 *
 * System-wide customization contract: changing this component (or the
 * underlying src/assets/branding SVG) propagates to every consumer —
 * splash, onboarding, app lock, about, any future header.
 *
 * Two approved marks:
 *   - 'badge'  — solid burgundy circle with a single heart (in-app badges)
 *   - 'brand'  — two overlapping hearts (onboarding/splash identity)
 *
 * Both are pure SVG influenced by design tokens, so the brand color change
 * centrally propagates; no per-screen hardcoded logos exist.
 */

export type BrandLogoVariant = 'badge' | 'brand';

export interface BrandLogoProps {
  /** Variant: 'badge' (single heart circle) or 'brand' (overlapping hearts). */
  variant?: BrandLogoVariant;
  /** Outer size in px. */
  size?: number;
  /** Accessible label (decorative when omitted). */
  title?: string;
}

export function BrandLogo({ variant = 'badge', size = 72, title }: BrandLogoProps) {
  const a11y = title
    ? { role: 'img' as const, 'aria-label': title }
    : { 'aria-hidden': true as const };
  if (variant === 'brand') {
    return (
      <svg
        width={size}
        height={size}
        viewBox="0 0 120 120"
        fill="none"
        {...a11y}
      >
        <path
          d="M58 38c-6-9-20-9-24 1-3 8 4 16 24 27 20-11 27-19 24-27-4-10-18-10-24-1z"
          fill="var(--th-color-burgundy)"
        />
        <path
          d="M42 52c-4-6-13-6-15 0-2 5 3 10 15 17 12-7 17-12 15-17-2-6-11-6-15 0z"
          fill="var(--th-color-blush)"
        />
      </svg>
    );
  }
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 80 80"
      fill="none"
      {...a11y}
    >
      <circle
        cx="40"
        cy="40"
        r="38"
        fill="var(--th-color-burgundy)"
        stroke="var(--th-color-burgundy-dark)"
        strokeWidth="2"
      />
      <path
        d="M40 58C40 58 18 44 18 30C18 22 24 16 32 16C36 16 39 18 40 20C41 18 44 16 48 16C56 16 62 22 62 30C62 44 40 58 40 58Z"
        fill="var(--th-color-text-on-accent)"
        opacity="0.95"
      />
    </svg>
  );
}
