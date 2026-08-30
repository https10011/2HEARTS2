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
 * This component sits ONLY at the root route. Every evaluation ends in a
 * redirect — to /onboarding/* for incomplete setup or to /app/home for
 * completed setup — so those routes render directly without re-entering
 * the gate. This prevents redirect loops when onboarding screens navigate
 * between each other, and ensures a completed user never lands back in
 * onboarding (Phase 21 fix: the gate previously rendered an index child
 * that always redirected to the new-user entry, even after setup).
 *
 * Shows the branded splash view during state evaluation.
 */

import { useEffect } from 'react';
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

export function OnboardingGate() {
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;
    const go = (path: string) => {
      if (!cancelled) navigate(path, { replace: true });
    };

    const evaluate = async () => {
      const settings = appSettingsStore.getState();

      // Completed setup — straight into the app.
      if (settings.onboardingStage === 'complete') {
        go(RoutePath.appHome);
        return;
      }

      // Reconcile stage from domain truth
      const appState = coreServices.appState;
      if (appState) {
        try {
          const stage = await appState.reconcileOnboardingStage();
          go(STAGE_TO_ROUTE[stage]);
          return;
        } catch {
          // Stage reconciliation failed; fall through to redirect with persisted value
        }
      }

      // Use persisted stage — 'complete' case was already handled above,
      // so stage here is 'fresh' | 'owner' | 'relationship' | 'personalization'.
      const stage = settings.onboardingStage;
      go(STAGE_TO_ROUTE[stage] ?? RoutePath.onboardingWelcome);
    };

    evaluate();
    return () => { cancelled = true; };
  }, [navigate]); // navigate is stable from react-router

  return <SplashScreen />;
}
