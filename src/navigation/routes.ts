/**
 * TwoHearts route map.
 *
 * Routing architecture supports the required application states
 * (MasterPrompt roadmap: Foundation / Main Navigation / features):
 *   - onboarding   (first-launch setup, locked behind app state)
 *   - app          (unlocked main application shell)
 *   - feature      (nested feature screens, added in later phases)
 *   - modal        (bottom-sheet / confirmation flows)
 *
 * Only the foundation routes exist now. Feature routes are added in
 * their respective phases (Phase 6+).
 */

export const RoutePath = {
  /** Onboarding group — first-launch setup flow (Phase 5). */
  onboardingRoot: '/onboarding',
  onboardingWelcome: '/onboarding/welcome',
  onboardingProfile: '/onboarding/profile',
  onboardingRelationship: '/onboarding/relationship',
  onboardingPersonalization: '/onboarding/personalization',
  onboardingAppLock: '/onboarding/app-lock',
  onboardingComplete: '/onboarding/complete',

  /** Main application shell (Phase 6). */
  appRoot: '/app',
  appHome: '/app/home',

  /** Us / Relationship hub. */
  appUs: '/app/us',
  appUsMemories: '/app/us/memories',
  appUsTimeline: '/app/us/timeline',
  appUsReminders: '/app/us/reminders',

  /** Memories feature (Phase 7). */
  appMemories: '/app/memories',
  appMemoriesAdd: '/app/memories/add',
  appMemoriesDetail: '/app/memories/:memoryId',

  /** Games hub. */
  appGames: '/app/games',
  appGamesWhoKnows: '/app/games/who-knows',
  appGamesWouldYouRather: '/app/games/would-you-rather',
  appGamesTwentyQuestions: '/app/games/twenty-questions',
  appGamesHowWell: '/app/games/how-well',

  /** Notes hub (Phase 8). */
  appNotes: '/app/notes',
  appNotesAdd: '/app/notes/add',
  appNotesDetail: '/app/notes/:noteId',
  appNotesEdit: '/app/notes/:noteId/edit',
  appNotesShared: '/app/notes/shared',
  appNotesPrivate: '/app/notes/private',

  /** More menu. */
  appMore: '/app/more',
  appMoreSettings: '/app/more/settings',
  appMoreSearch: '/app/more/search',
  appMoreVault: '/app/more/vault',
  appMoreAbout: '/app/more/about',

  /** Foundation placeholder — verifies the engineering foundation runs. */
  foundation: '/app/foundation',
} as const;

export type RoutePathKey = keyof typeof RoutePath;

/**
 * Onboarding step order for progression and step-indicator rendering.
 * Index 0 = Welcome (first screen after splash).
 */
export const ONBOARDING_STEPS = [
  RoutePath.onboardingWelcome,
  RoutePath.onboardingProfile,
  RoutePath.onboardingRelationship,
  RoutePath.onboardingPersonalization,
  RoutePath.onboardingAppLock,
  RoutePath.onboardingComplete,
] as const;

export type OnboardingStepIndex = number;

export const ROUTE_DEFAULTS = {
  /** Where a fresh, un-onboarded user lands. */
  entryForNewUser: RoutePath.onboardingWelcome,
  /** Where an onboarded, unlocked user lands. */
  entryForAppUser: RoutePath.appHome,
} as const;
