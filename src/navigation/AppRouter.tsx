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
import { OnboardingGate } from '../features/onboarding/OnboardingGate.tsx';
import { AppShell } from '../features/app-shell/AppShell.tsx';
import { HomeScreen } from '../features/app-shell/screens/HomeScreen.tsx';
import { UsScreen } from '../features/app-shell/screens/UsScreen.tsx';
import { GamesHubScreen } from '../features/app-shell/screens/GamesHubScreen.tsx';
import { NotesHubScreen } from '../features/app-shell/screens/NotesHubScreen.tsx';
import { MoreScreen } from '../features/app-shell/screens/MoreScreen.tsx';
import { PlaceholderScreen } from '../features/app-shell/screens/PlaceholderScreen.tsx';

/**
 * Navigation architecture (Phase 6).
 *
 * Uses React Router 6 with a single router instance. The routing tree:
 *
 *   /                    → OnboardingGate → redirect to correct stage
 *   /onboarding/*        → Onboarding screens (not gated)
 *   /app                 → AppShell (bottom nav + content) → /app/home
 *     /app/home          → HomeScreen (dashboard)
 *     /app/us            → UsScreen (relationship hub)
 *     /app/us/*          → Us feature sub-routes (placeholders)
 *     /app/games         → GamesHubScreen
 *     /app/games/*       → Game feature sub-routes (placeholders)
 *     /app/notes         → NotesHubScreen
 *     /app/notes/*       → Notes feature sub-routes (placeholders)
 *     /app/more          → MoreScreen (menu)
 *     /app/more/*        → More sub-routes (settings, search, etc.)
 *
 * The OnboardingGate evaluates state only at `/`. Once redirected,
 * onboarding and app sub-routes render directly without re-entering the gate.
 *
 * AppShell owns the bottom navigation bar and Android back-button handling.
 */

const router = createBrowserRouter([
  /**
   * Root path — OnboardingGate evaluates app state and redirects.
   */
  {
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

  /**
   * Onboarding — not wrapped in the gate; screens navigate between each other.
   */
  {
    path: RoutePath.onboardingRoot,
    children: [
      { index: true, element: <Navigate to={RoutePath.onboardingWelcome} replace /> },
      { path: 'welcome', element: <WelcomeScreen /> },
      { path: 'profile', element: <ProfileSetupScreen /> },
      { path: 'relationship', element: <RelationshipSetupScreen /> },
      { path: 'personalization', element: <PersonalizationSetupScreen /> },
      { path: 'app-lock', element: <AppLockSetupScreen /> },
      { path: 'complete', element: <SetupCompleteScreen /> },
    ],
  },

  /**
   * Main application shell — bottom nav + nested feature routes.
   * AppShell renders the BottomNav and <Outlet> for child routes.
   */
  {
    path: RoutePath.appRoot,
    element: <AppShell />,
    children: [
      { index: true, element: <Navigate to={RoutePath.appHome} replace /> },

      // Home
      { path: 'home', element: <HomeScreen /> },

      // Us / Relationship
      { path: 'us', element: <UsScreen /> },
      {
        path: 'us/memories',
        element: (
          <PlaceholderScreen
            title="Memories"
            description="Photos and moments from your journey together will live here."
          />
        ),
      },
      {
        path: 'us/timeline',
        element: (
          <PlaceholderScreen
            title="Timeline"
            description="Your relationship story told over time."
          />
        ),
      },
      {
        path: 'us/reminders',
        element: (
          <PlaceholderScreen
            title="Reminders"
            description="Never miss an important date or occasion."
          />
        ),
      },

      // Games
      { path: 'games', element: <GamesHubScreen /> },
      {
        path: 'games/who-knows',
        element: (
          <PlaceholderScreen
            title="Who Knows Who Better?"
            description="A fun game to test how well you know each other."
          />
        ),
      },
      {
        path: 'games/would-you-rather',
        element: (
          <PlaceholderScreen
            title="Would You Rather?"
            description="Fun dilemmas to spark conversation."
          />
        ),
      },
      {
        path: 'games/twenty-questions',
        element: (
          <PlaceholderScreen
            title="20 Questions"
            description="Deep conversation starters for couples."
          />
        ),
      },
      {
        path: 'games/how-well',
        element: (
          <PlaceholderScreen
            title="How Well Do You Know Each Other?"
            description="The ultimate couple challenge."
          />
        ),
      },

      // Notes
      { path: 'notes', element: <NotesHubScreen /> },
      {
        path: 'notes/shared',
        element: (
          <PlaceholderScreen
            title="Shared Notes"
            description="Notes you both can see and edit."
          />
        ),
      },
      {
        path: 'notes/private',
        element: (
          <PlaceholderScreen
            title="Private Notes"
            description="Only you can see these notes."
          />
        ),
      },

      // More
      { path: 'more', element: <MoreScreen /> },
      {
        path: 'more/settings',
        element: (
          <PlaceholderScreen
            title="Settings"
            description="Customize your TwoHearts experience."
          />
        ),
      },
      {
        path: 'more/search',
        element: (
          <PlaceholderScreen
            title="Search"
            description="Find memories, notes, and more."
          />
        ),
      },
      {
        path: 'more/vault',
        element: (
          <PlaceholderScreen
            title="Vault"
            description="Your private, secure storage."
          />
        ),
      },
      {
        path: 'more/about',
        element: (
          <PlaceholderScreen
            title="About"
            description="TwoHearts — your private couple space."
          />
        ),
      },

      // Legacy foundation route
      { path: 'foundation', element: <HomeScreen /> },
    ],
  },

  // Catch-all
  { path: '*', element: <Navigate to={ROUTE_DEFAULTS.entryForNewUser} replace /> },
]);

/**
 * Exported router instance for programmatic navigation (e.g. back-button
 * handling in AppShell). NOT used by React components — they use hooks.
 */
export { router };

export function AppRouter() {
  useEffect(() => {
    document.title = 'TwoHearts';
  }, []);
  return <RouterProvider router={router} />;
}
