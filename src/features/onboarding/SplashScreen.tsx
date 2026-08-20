/**
 * SplashScreen (Phase 5).
 *
 * Renders during the bootstrap phase (before React Router is active).
 * Shows the TwoHearts logo/branding while the app initializes.
 */

import { useEffect, useState } from 'react';

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
        {/* TwoHearts logo */}
        <div className="th-splash-logo">
          <svg
            width="80"
            height="80"
            viewBox="0 0 80 80"
            fill="none"
            aria-hidden="true"
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
        </div>
        <h1 className="th-splash-title">TwoHearts</h1>
        <p className="th-splash-subtitle">Your private couple space</p>
      </div>
    </div>
  );
}
