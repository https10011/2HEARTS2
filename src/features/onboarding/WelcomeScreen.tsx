/**
 * WelcomeScreen (Phase 5).
 *
 * First onboarding screen after splash. Introduces the app and guides
 * the user into profile setup.
 */

import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, OnboardingArt, RoseLilyDecoration } from '../../components/index.ts';
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
      <div className="th-onboarding-welcome th-welcome-glow">
        {/* Subtle floral accents framing the welcome (Phase 27) */}
        <RoseLilyDecoration variant={1} size={80} position="top-left" opacity={0.12} animated />
        <RoseLilyDecoration variant={12} size={70} position="bottom-right" opacity={0.1} animated />
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
