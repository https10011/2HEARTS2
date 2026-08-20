/**
 * OnboardingLayout (Phase 5).
 *
 * Shared layout for onboarding screens: step indicator dots, back navigation,
 * and a consistent content area. Screens render their own content via children.
 */

import type { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { IconButton, IconBack, Button } from '../../components/index.ts';
import { ONBOARDING_STEPS, RoutePath } from '../../navigation/routes.ts';

interface OnboardingLayoutProps {
  children: ReactNode;
  /** Current route path — used to compute the step indicator. */
  currentPath: string;
  /** Whether to show the back button. */
  showBack?: boolean;
  /** Whether to show the skip button (app-lock step). */
  showSkip?: boolean;
  /** Callback when skip is pressed. */
  onSkip?: () => void;
  /** Whether skip is loading. */
  skipLoading?: boolean;
}

export function OnboardingLayout({
  children,
  currentPath,
  showBack = true,
  showSkip = false,
  onSkip,
  skipLoading = false,
}: OnboardingLayoutProps) {
  const navigate = useNavigate();

  // Find the current step index (exclude the "complete" step from the progress bar)
  const progressSteps = ONBOARDING_STEPS.filter(
    (s) => s !== RoutePath.onboardingComplete,
  );
  const currentIdx = progressSteps.indexOf(
    currentPath as (typeof progressSteps)[number],
  );

  const handleBack = () => {
    if (currentIdx > 0) {
      navigate(progressSteps[currentIdx - 1]);
    } else {
      // At the first step — go to root which redirects
      navigate('/');
    }
  };

  return (
    <div className="th-screen th-onboarding">
      {/* Header bar */}
      <header className="th-onboarding-header">
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            minWidth: 'var(--th-touch-target-min)',
          }}
        >
          {showBack && currentIdx > 0 && (
            <IconButton label="Go back" onClick={handleBack}>
              <IconBack />
            </IconButton>
          )}
        </div>
        <h1
          className="th-header__title"
          style={{ flex: 1, textAlign: 'center' }}
        >
          TwoHearts
        </h1>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            minWidth: 'var(--th-touch-target-min)',
            justifyContent: 'flex-end',
          }}
        >
          {showSkip && onSkip && (
            <Button
              variant="ghost"
              onClick={onSkip}
              disabled={skipLoading}
              style={{ fontSize: 'var(--th-font-size-sm)' }}
            >
              Skip
            </Button>
          )}
        </div>
      </header>

      {/* Step indicator dots */}
      {currentIdx >= 0 && (
        <div className="th-onboarding-dots" role="progressbar" aria-label={`Step ${currentIdx + 1} of ${progressSteps.length}`}>
          {progressSteps.map((step, i) => (
            <span
              key={step}
              className={`th-onboarding-dot ${i === currentIdx ? 'th-onboarding-dot--active' : ''} ${i < currentIdx ? 'th-onboarding-dot--done' : ''}`}
            />
          ))}
        </div>
      )}

      {/* Screen content */}
      <div className="th-scroll th-onboarding-content">{children}</div>
    </div>
  );
}
