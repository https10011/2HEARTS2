/**
 * SetupCompleteScreen (Phase 5).
 *
 * Final onboarding screen. Confirms setup completion and navigates
 * to the main app home.
 */

import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, OnboardingArt, RoseLilyDecoration } from '../../components/index.ts';
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
      <div className="th-onboarding-form th-onboarding-complete th-welcome-glow">
        <RoseLilyDecoration variant={6} size={80} position="top-left" opacity={0.15} animated />
        <RoseLilyDecoration variant={15} size={75} position="bottom-right" opacity={0.12} animated />
        {/* Celebration illustration (centralized decorative art — Phase 23) */}
        <div className="th-welcome-illustration">
          <OnboardingArt variant="celebration-heart" size={120} />
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
