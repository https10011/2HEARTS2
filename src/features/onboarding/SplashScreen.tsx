/**
 * SplashScreen (Phase 5).
 *
 * Renders during the bootstrap phase (before React Router is active).
 * Shows the TwoHearts logo/branding while the app initializes.
 */

import { useEffect, useState } from 'react';
import { BrandLogo } from '../../components/index.ts';

export function SplashScreen() {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    // Ensure splash is visible for at least a brief moment
    const timer = setTimeout(() => setVisible(false), 300);
    return () => clearTimeout(timer);
  }, []);

  if (!visible) return null;

  return (
    <div className="th-splash th-welcome-glow">
      <div className="th-splash-content">
        {/* Official TwoHearts logo — one BrandLogo component, one asset (Phase 23) */}
        <div className="th-splash-logo">
          <BrandLogo variant="brand" size={180} title="TwoHearts" />
        </div>
        <p className="th-splash-subtitle">Your private couple space</p>
      </div>
    </div>
  );
}
