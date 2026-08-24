import { useEffect } from 'react';
import {
  createBrowserRouter,
  RouterProvider,
  Navigate,
} from 'react-router-dom';
import { RoutePath } from './routes';
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
import { NotesHome, NoteEditor, NoteDetail } from '../features/notes/index.ts';
import { MoreScreen } from '../features/app-shell/screens/MoreScreen.tsx';
import { ImportantDatesScreen } from '../features/app-shell/screens/ImportantDatesScreen.tsx';
import { RelationshipCounterScreen } from '../features/app-shell/screens/RelationshipCounterScreen.tsx';
import { MemoriesHome, AddMemory, MemoryDetail } from '../features/memories/index.ts';
import { TimelineHome, AddEvent, EventDetail } from '../features/timeline/index.ts';
import { GamePlayScreen, GameResultsScreen, MemoryMatchScreen, WordScrambleScreen, CasualGamePlayScreen } from '../features/games/index.ts';
import { RemindersHome, CreateReminder, ReminderDetail } from '../features/reminders/index.ts';
import { PlacesHome, CreatePlace, PlaceDetail } from '../features/places/index.ts';
import { MoodHome, MoodEntryScreen, MoodHistory } from '../features/mood/index.ts';
import { PeriodHome, LogPeriod, CycleHistory, PeriodCalendarScreen, PeriodSettingsScreen } from '../features/period/index.ts';
import { VaultEntryRoute, AddVaultContentRoute, VaultContentViewerRoute } from '../features/vault/index.ts';
import { SearchScreen } from '../features/app-shell/screens/SearchScreen.tsx';
import { NotificationCenter } from '../features/notifications/NotificationCenter.tsx';
import {
  SettingsHomeScreen, ProfileSettingsScreen, RelationshipSettingsScreen,
  AppearanceSettingsScreen, NotificationSettingsScreen, SecuritySettingsScreen,
  StorageSettingsScreen, AboutScreen,
} from '../features/settings/index.ts';

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
   * Root path — OnboardingGate evaluates app state and always redirects
   * (onboarding stage for incomplete setup, /app/home once complete).
   */
  { path: '/', element: <OnboardingGate /> },

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
        element: <Navigate to={RoutePath.appMemories} replace />,
      },
      {
        path: 'us/timeline',
        element: <Navigate to={RoutePath.appTimelineRoot} replace />,
      },

      // Timeline (Phase 9)
      { path: 'timeline', element: <TimelineHome /> },
      { path: 'timeline/add', element: <AddEvent /> },
      { path: 'timeline/:eventId', element: <EventDetail /> },
      { path: 'timeline/:eventId/edit', element: <AddEvent /> },
      {
        path: 'us/reminders',
        element: <ImportantDatesScreen />,
      },
      {
        path: 'us/counter',
        element: <RelationshipCounterScreen />,
      },

      // Games (Phase 11 + Phase 12)
      { path: 'games', element: <GamesHubScreen /> },
      { path: 'games/memory-match', element: <MemoryMatchScreen /> },
      { path: 'games/memory-match/results', element: <GameResultsScreen /> },
      { path: 'games/word-scramble', element: <WordScrambleScreen /> },
      { path: 'games/word-scramble/results', element: <GameResultsScreen /> },
      { path: 'games/casual-trivia', element: <CasualGamePlayScreen /> },
      { path: 'games/casual-trivia/results', element: <GameResultsScreen /> },
      { path: 'games/riddle-room', element: <CasualGamePlayScreen /> },
      { path: 'games/riddle-room/results', element: <GameResultsScreen /> },
      { path: 'games/:gameType', element: <GamePlayScreen /> },
      { path: 'games/:gameType/results', element: <GameResultsScreen /> },
      // Legacy route aliases for direct links
      { path: 'games/who-knows', element: <Navigate to="/app/games/who-knows-who-better" replace /> },
      { path: 'games/would-you-rather', element: <Navigate to="/app/games/would-you-rather" replace /> },
      { path: 'games/twenty-questions', element: <Navigate to="/app/games/couple-trivia" replace /> },
      { path: 'games/how-well', element: <Navigate to="/app/games/guess-my-answer" replace /> },

      // Reminders (Phase 13)
      { path: 'reminders', element: <RemindersHome /> },
      { path: 'reminders/add', element: <CreateReminder /> },
      { path: 'reminders/:reminderId', element: <ReminderDetail /> },
      { path: 'reminders/:reminderId/edit', element: <CreateReminder /> },

      // Notes (Phase 8)
      { path: 'notes', element: <NotesHome /> },
      { path: 'notes/add', element: <NoteEditor /> },
      { path: 'notes/:noteId', element: <NoteDetail /> },
      { path: 'notes/:noteId/edit', element: <NoteEditor /> },

      // Memories (Phase 7)
      { path: 'memories', element: <MemoriesHome /> },
      { path: 'memories/add', element: <AddMemory /> },
      { path: 'memories/:memoryId', element: <MemoryDetail /> },
      { path: 'memories/:memoryId/edit', element: <AddMemory /> },

      // Period Tracker (Phase 16)
      { path: 'period', element: <PeriodHome /> },
      { path: 'period/log', element: <LogPeriod /> },
      { path: 'period/calendar', element: <PeriodCalendarScreen /> },
      { path: 'period/history', element: <CycleHistory /> },
      { path: 'period/settings', element: <PeriodSettingsScreen /> },
      { path: 'period/:entryId', element: <LogPeriod /> },
      { path: 'period/:entryId/edit', element: <LogPeriod /> },

      // Mood (Phase 15)
      { path: 'mood', element: <MoodHome /> },
      { path: 'mood/add', element: <MoodEntryScreen /> },
      { path: 'mood/history', element: <MoodHistory /> },
      { path: 'mood/:entryId', element: <MoodEntryScreen /> },
      { path: 'mood/:entryId/edit', element: <MoodEntryScreen /> },

      // Places (Phase 14)
      { path: 'places', element: <PlacesHome /> },
      { path: 'places/add', element: <CreatePlace /> },
      { path: 'places/:placeId', element: <PlaceDetail /> },
      { path: 'places/:placeId/edit', element: <CreatePlace /> },

      // More
      { path: 'more', element: <MoreScreen /> },
      { path: 'more/settings', element: <SettingsHomeScreen /> },
      { path: 'more/settings/profile', element: <ProfileSettingsScreen /> },
      { path: 'more/settings/relationship', element: <RelationshipSettingsScreen /> },
      { path: 'more/settings/appearance', element: <AppearanceSettingsScreen /> },
      { path: 'more/settings/notifications', element: <NotificationSettingsScreen /> },
      { path: 'more/settings/security', element: <SecuritySettingsScreen /> },
      { path: 'more/settings/storage', element: <StorageSettingsScreen /> },
      {
        path: 'more/search',
        element: <SearchScreen />,
      },
      {
        path: 'notifications',
        element: <NotificationCenter />,
      },
      {
        path: 'more/vault',
        element: <Navigate to="/app/vault" replace />,
      },
      {
        path: 'more/about',
        element: <AboutScreen />,
      },

      // Vault (Phase 17; route wrappers resolve services at render time — Phase 21)
      { path: 'vault', element: <VaultEntryRoute /> },
      { path: 'vault/add', element: <AddVaultContentRoute /> },
      { path: 'vault/:itemId', element: <VaultContentViewerRoute /> },
      { path: 'vault/:itemId/edit', element: <AddVaultContentRoute /> },

      // Legacy foundation route
      { path: 'foundation', element: <HomeScreen /> },
    ],
  },

  // Catch-all — hand unknown paths to the root gate so it can route by
  // actual onboarding state (completed users must not land in onboarding).
  { path: '*', element: <Navigate to="/" replace /> },
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
