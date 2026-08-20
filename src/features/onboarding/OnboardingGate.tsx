/**
 * OnboardingGate (Phase 5).
 *
 * Evaluates the current application state at the ROOT path (`/`) and
 * determines where the user should land:
 *   - Brand new / first launch → onboarding
 *   - Incomplete setup → resume correct persisted stage
 *   - Completed setup → home
 *   - App-lock enabled → locked gate (future; respects AppLockService)
 *
 * This component sits ONLY at the root route. Once it redirects to
 * /onboarding/* or /app/*, those routes render directly without
 * re-entering the gate. This prevents redirect loops when onboarding
 * screens navigate between each other.
 *
 * Shows the branded splash view during state evaluation.
 */

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { appSettingsStore, type OnboardingStage } from '../../core/appSettings.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { RoutePath } from '../../navigation/routes.ts';
import { SplashScreen } from './SplashScreen.tsx';

/** Maps onboarding stages to their entry route paths. */
const STAGE_TO_ROUTE: Record<OnboardingStage, string> = {
  fresh: RoutePath.onboardingWelcome,
  owner: RoutePath.onboardingProfile,
  relationship: RoutePath.onboardingRelationship,
  personalization: RoutePath.onboardingPersonalization,
  complete: RoutePath.appHome,
};

interface OnboardingGateProps {
  children: React.ReactNode;
}

export function OnboardingGate({ children }: OnboardingGateProps) {
  const navigate = useNavigate();
  const [resolved, setResolved] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const evaluate = async () => {
      const settings = appSettingsStore.getState();

      // Check if setup is complete — no redirect needed, render children
      if (settings.onboardingStage === 'complete') {
        if (!cancelled) setResolved(true);
        return;
      }

      // Reconcile stage from domain truth
      const appState = coreServices.appState;
      if (appState) {
        try {
          const stage = await appState.reconcileOnboardingStage();
          if (cancelled) return;
          if (stage === 'complete') {
            setResolved(true);
          } else {
            navigate(STAGE_TO_ROUTE[stage], { replace: true });
          }
          return;
        } catch {
          // Stage reconciliation failed; fall through to redirect with persisted value
        }
      }

      // Use persisted stage — 'complete' case was already handled above,
      // so stage here is 'fresh' | 'owner' | 'relationship' | 'personalization'.
      if (!cancelled) {
        const stage = settings.onboardingStage;
        navigate(STAGE_TO_ROUTE[stage] ?? RoutePath.onboardingWelcome, { replace: true });
      }
    };

    evaluate();
    return () => { cancelled = true; };
  }, [navigate]); // navigate is stable from react-router

  if (!resolved) {
    return <SplashScreen />;
  }

  return <>{children}</>;
}
