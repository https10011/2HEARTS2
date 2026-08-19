import { useEffect } from 'react';
import {
  createBrowserRouter,
  RouterProvider,
  Navigate,
  Outlet,
} from 'react-router-dom';
import { RoutePath, ROUTE_DEFAULTS } from './routes';

/**
 * Navigation foundation.
 *
 * Uses React Router with a single router instance. The AppRootProvider
 * (src/core/AppRootProvider) gates the main app shell behind application
 * state (onboarded + unlocked), which is implemented in the state phase.
 * For Phase 1 the router simply demonstrates the working foundation.
 *
 * Modal-style flows are supported via nested routes + the Modal primitive;
 * feature phases will add them as needed.
 */

function FoundationPlaceholder() {
  return (
    <main style={{ padding: 'var(--th-space-6)', textAlign: 'center' }}>
      <p style={{ marginBottom: 'var(--th-space-4)' }}>
        TwoHearts engineering foundation is running.
      </p>
      <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)' }}>
        Onboarding, main navigation, and feature screens arrive in later phases.
      </p>
    </main>
  );
}

function OnboardingPlaceholder() {
  return (
    <main style={{ padding: 'var(--th-space-6)', textAlign: 'center' }}>
      <p>Onboarding foundation route (content in Phase 5).</p>
    </main>
  );
}

/** App shell layout — provides the screen container + outlet for nested routes. */
function AppShell() {
  // Screen container from primitives gives safe-area + scroll behavior.
  return (
    <div className="th-screen">
      <Outlet />
    </div>
  );
}

const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to={ROUTE_DEFAULTS.entryForNewUser} replace />,
  },
  {
    path: RoutePath.onboardingRoot,
    children: [
      { index: true, element: <Navigate to={RoutePath.onboardingWelcome} replace /> },
      { path: 'welcome', element: <OnboardingPlaceholder /> },
    ],
  },
  {
    path: RoutePath.appRoot,
    element: <AppShell />,
    children: [
      { index: true, element: <Navigate to={RoutePath.appHome} replace /> },
      { path: 'home', element: <FoundationPlaceholder /> },
      { path: 'foundation', element: <FoundationPlaceholder /> },
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
