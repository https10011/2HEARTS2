/**
 * SetupCompleteScreen (Phase 5).
 *
 * Final onboarding screen. Confirms setup completion and navigates
 * to the main app home.
 */

import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button } from '../../components/index.ts';
import { OnboardingLayout } from './OnboardingLayout.tsx';

export function SetupCompleteScreen() {
  const navigate = useNavigate();

  useEffect(() => {
    // Apply settings immediately on reaching the complete screen
    // (text size + theme may have changed during personalization)
  }, []);

  const handleEnter = () => {
    navigate(RoutePath.appHome);
  };

  return (
    <OnboardingLayout currentPath={RoutePath.onboardingComplete} showBack={false}>
      <div className="th-onboarding-form th-onboarding-complete">
        {/* Celebration illustration */}
        <div className="th-welcome-illustration">
          <svg
            width="120"
            height="120"
            viewBox="0 0 120 120"
            fill="none"
            aria-hidden="true"
          >
            <circle cx="60" cy="60" r="56" fill="var(--th-color-blush)" stroke="var(--th-color-burgundy)" strokeWidth="2" opacity="0.4" />
            <path
              d="M60 80C60 80 38 66 38 52C38 44 44 38 52 38C56 38 59 40 60 42C61 40 64 38 68 38C76 38 82 44 82 52C82 66 60 80 60 80Z"
              fill="var(--th-color-burgundy)"
            />
            {/* Sparkle decorations */}
            <circle cx="35" cy="40" r="2" fill="var(--th-color-rose-muted)" />
            <circle cx="85" cy="38" r="2.5" fill="var(--th-color-rose-muted)" />
            <circle cx="30" cy="65" r="1.5" fill="var(--th-color-burgundy-light)" />
            <circle cx="90" cy="62" r="2" fill="var(--th-color-burgundy-light)" />
          </svg>
        </div>

        <h2 className="th-onboarding-heading">You're all set!</h2>
        <p className="th-onboarding-description">
          Everything is ready. Welcome to your private couple space.
          Enjoy celebrating your relationship together.
        </p>

        <div className="th-onboarding-actions">
          <Button variant="primary" full onClick={handleEnter}>
            Enter TwoHearts
          </Button>
        </div>
      </div>
    </OnboardingLayout>
  );
}
