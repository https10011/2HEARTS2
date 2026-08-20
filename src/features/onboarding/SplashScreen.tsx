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
    <div className="th-splash">
      <div className="th-splash-content">
        {/* TwoHearts logo — authoritative BrandLogo component (Phase 20) */}
        <div className="th-splash-logo">
          <BrandLogo variant="badge" size={80} />
        </div>
        <h1 className="th-splash-title">TwoHearts</h1>
        <p className="th-splash-subtitle">Your private couple space</p>
      </div>
    </div>
  );
}
