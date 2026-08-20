/**
 * WelcomeScreen (Phase 5).
 *
 * First onboarding screen after splash. Introduces the app and guides
 * the user into profile setup.
 */

import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button } from '../../components/index.ts';
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
        {/* Illustration area */}
        <div className="th-welcome-illustration">
          <svg
            width="160"
            height="160"
            viewBox="0 0 160 160"
            fill="none"
            aria-hidden="true"
          >
            <circle cx="80" cy="80" r="76" fill="var(--th-color-blush)" stroke="var(--th-color-burgundy)" strokeWidth="2" opacity="0.5" />
            <circle cx="60" cy="72" r="28" fill="var(--th-color-surface)" stroke="var(--th-color-burgundy-light)" strokeWidth="1.5" />
            <circle cx="100" cy="72" r="28" fill="var(--th-color-surface)" stroke="var(--th-color-burgundy-light)" strokeWidth="1.5" />
            <path
              d="M80 108C80 108 66 98 66 88C66 82 72 78 76 78C78 78 79 79 80 80C81 79 82 78 84 78C88 78 94 82 94 88C94 98 80 108 80 108Z"
              fill="var(--th-color-burgundy)"
              opacity="0.8"
            />
          </svg>
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
