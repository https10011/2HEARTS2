import { useEffect } from 'react';
import {
  createBrowserRouter,
  RouterProvider,
  Navigate,
  Outlet,
} from 'react-router-dom';
import { RoutePath, ROUTE_DEFAULTS } from './routes';
import {
  WelcomeScreen,
  ProfileSetupScreen,
  RelationshipSetupScreen,
  PersonalizationSetupScreen,
  AppLockSetupScreen,
  SetupCompleteScreen,
} from '../features/onboarding/index.ts';
import { HomeScreen } from '../features/onboarding/HomeScreen.tsx';
import { OnboardingGate } from '../features/onboarding/OnboardingGate.tsx';

/**
 * Navigation foundation (Phase 5).
 *
 * Uses React Router with a single router instance. The OnboardingGate
 * evaluates application state at the root (`/`) and routes to the correct
 * destination:
 *   - Brand-new users → onboarding flow
 *   - Incomplete setup → resume correct stage
 *   - Completed setup → app home
 *
 * Onboarding sub-routes are NOT gated — they are direct children of
 * /onboarding/* and the gate only triggers at `/`. This avoids redirect
 * loops when onboarding screens navigate between each other or to /app.
 */

/** App shell layout — provides the screen container + outlet for nested routes. */
function AppShell() {
  return (
    <div className="th-screen">
      <Outlet />
    </div>
  );
}

const router = createBrowserRouter([
  {
    /**
     * Root path — the OnboardingGate evaluates app state and redirects.
     * Once redirected to /onboarding/* or /app/*, sub-routes render
     * directly without re-entering the gate.
     */
    path: '/',
    element: (
      <OnboardingGate>
        <Outlet />
      </OnboardingGate>
    ),
    children: [
      {
        index: true,
        element: <Navigate to={ROUTE_DEFAULTS.entryForNewUser} replace />,
      },
    ],
  },
  {
    path: RoutePath.onboardingRoot,
    children: [
      {
        index: true,
        element: <Navigate to={RoutePath.onboardingWelcome} replace />,
      },
      { path: 'welcome', element: <WelcomeScreen /> },
      { path: 'profile', element: <ProfileSetupScreen /> },
      { path: 'relationship', element: <RelationshipSetupScreen /> },
      { path: 'personalization', element: <PersonalizationSetupScreen /> },
      { path: 'app-lock', element: <AppLockSetupScreen /> },
      { path: 'complete', element: <SetupCompleteScreen /> },
    ],
  },
  {
    path: RoutePath.appRoot,
    element: <AppShell />,
    children: [
      {
        index: true,
        element: <Navigate to={RoutePath.appHome} replace />,
      },
      { path: 'home', element: <HomeScreen /> },
      { path: 'foundation', element: <HomeScreen /> },
    ],
  },
  { path: '*', element: <Navigate to={ROUTE_DEFAULTS.entryForNewUser} replace /> },
]);

export function AppRouter() {
  // Keep the document title consistent with the app.
  useEffect(() => {
    document.title = 'TwoHearts';
  }, []);
  return <RouterProvider router={router} />;
}
