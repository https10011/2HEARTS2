/**
 * SplashScreen (Phase 5).
 *
 * Renders during the bootstrap phase (before React Router is active).
 * Shows the official TwoHearts brand lockup flanked by the signature
 * Rose/Lily florals (reference 01-Splash) while the app initializes.
 */

import { useEffect, useState } from 'react';
import { BrandLogo, RoseLilyDecoration } from '../../components/index.ts';

export function SplashScreen() {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    // Ensure splash is visible for at least a brief moment
    const timer = setTimeout(() => setVisible(false), 300);
    return () => clearTimeout(timer);
  }, []);

  if (!visible) return null;

  return (
    <div className="th-splash th-splash--floral">
      {/* Signature florals flanking the brand lockup */}
      <RoseLilyDecoration variant={1} size={110} position="bottom-left" opacity={0.45} />
      <RoseLilyDecoration variant={11} size={120} position="top-right" opacity={0.5} />
      <div className="th-splash-content">
        {/* Official TwoHearts logo — one BrandLogo component, one asset (Phase 23) */}
        <div className="th-splash-logo">
          <BrandLogo variant="brand" size={200} title="TwoHearts" />
        </div>
        <span className="th-splash__spinner th-spinner" aria-hidden="true" />
      </div>
    </div>
  );
}
