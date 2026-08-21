/**
 * WelcomeScreen (Phase 5).
 *
 * First onboarding screen after splash. Introduces the app and guides
 * the user into profile setup.
 */

import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, OnboardingArt } from '../../components/index.ts';
import { OnboardingLayout } from './OnboardingLayout.tsx';
import { useOnboarding } from './useOnboarding.ts';

export function WelcomeScreen() {
  const navigate = useNavigate();
  const { advanceStage } = useOnboarding();

  const handleGetStarted = () => {
    advanceStage('owner');
    navigate(RoutePath.onboardingProfile);
  };

  return (
    <OnboardingLayout currentPath={RoutePath.onboardingWelcome} showBack={false}>
      <div className="th-onboarding-welcome">
        {/* Illustration (centralized decorative art — Phase 23) */}
        <div className="th-welcome-illustration">
          <OnboardingArt variant="couple-hearts" size={160} />
        </div>

        <h2 className="th-onboarding-heading">Welcome to TwoHearts</h2>
        <p className="th-onboarding-description">
          Your private, shared space for celebrating your relationship.
          Everything stays on your device — no accounts, no cloud, no data leaves you.
        </p>

        <div className="th-onboarding-actions">
          <Button variant="primary" full onClick={handleGetStarted}>
            Get started
          </Button>
        </div>
      </div>
    </OnboardingLayout>
  );
}
