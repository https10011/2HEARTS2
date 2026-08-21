/**
 * BrandLogo — the ONE authoritative TwoHearts brand mark (Phase 23).
 *
 * Renders the OFFICIAL owner-provided artwork (`TwoHearts-Logo-BrandName/`)
 * via the generated runtime assets in src/assets/branding/ — never an inline
 * SVG recreation. Customization contract: replace the official source SVG,
 * re-run `node scripts/generate-design-assets.mjs`, and every approved
 * location (splash, onboarding, app lock, about, headers) updates at once.
 *
 * Variants:
 *   - 'brand' — full logo: interlocked hearts + "TwoHearts" + tagline
 *   - 'mark'  — interlocked hearts only
 *
 * The official raster art includes a white outline, so both variants read
 * on light and dark surfaces.
 */

import brandUrl from '../assets/branding/twohearts-logo.svg';
import markUrl from '../assets/branding/twohearts-logo-mark.svg';

export type BrandLogoVariant = 'brand' | 'mark';

/** Aspect ratios (width/height) of the generated brand assets. */
export const BRAND_LOGO_ASPECT: Record<BrandLogoVariant, number> = {
  brand: 506.3152 / 433.8324,
  mark: 306.7499 / 285,
};

export interface BrandLogoProps {
  /** 'brand' (full logo) or 'mark' (hearts only). */
  variant?: BrandLogoVariant;
  /** Rendered width in px (height follows the official aspect ratio). */
  size?: number;
  /** Accessible label (decorative when omitted). */
  title?: string;
}

export function BrandLogo({ variant = 'brand', size = 120, title }: BrandLogoProps) {
  return (
    <img
      src={variant === 'brand' ? brandUrl : markUrl}
      width={size}
      height={Math.round(size / BRAND_LOGO_ASPECT[variant])}
      alt={title ?? ''}
      role={title ? 'img' : undefined}
      aria-hidden={title ? undefined : true}
      style={{ display: 'block', maxWidth: '100%', height: 'auto' }}
    />
  );
}
