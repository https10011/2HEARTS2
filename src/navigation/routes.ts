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

  /** Timeline feature (Phase 9). */
  appTimelineRoot: '/app/timeline',
  appTimelineAdd: '/app/timeline/add',
  appTimelineDetail: '/app/timeline/:eventId',
  appTimelineEdit: '/app/timeline/:eventId/edit',

  /** Us / Relationship hub. */
  appUs: '/app/us',
  appUsMemories: '/app/us/memories',
  appUsTimeline: '/app/us/timeline',
  appUsReminders: '/app/us/reminders',

  /** Memories feature (Phase 7). */
  appMemories: '/app/memories',
  appMemoriesAdd: '/app/memories/add',
  appMemoriesDetail: '/app/memories/:memoryId',

  /** Games hub (Phase 11 + Phase 12). */
  appGames: '/app/games',
  appGamesPlay: '/app/games/:gameType',
  appGamesResults: '/app/games/:gameType/results',
  appGamesWhoKnows: '/app/games/who-knows',
  appGamesWouldYouRather: '/app/games/would-you-rather',
  appGamesTwentyQuestions: '/app/games/twenty-questions',
  appGamesHowWell: '/app/games/how-well',
  appGamesGuessMyAnswer: '/app/games/guess-my-answer',
  appGamesThisOrThat: '/app/games/this-or-that',
  appGamesFinishMySentence: '/app/games/finish-my-sentence',
  appGamesCoupleTrivia: '/app/games/couple-trivia',
  appGamesMemoryMatch: '/app/games/memory-match',
  appGamesWordScramble: '/app/games/word-scramble',
  appGamesCasualTrivia: '/app/games/casual-trivia',
  appGamesRiddleRoom: '/app/games/riddle-room',

  /** Reminders feature (Phase 13). */
  appReminders: '/app/reminders',
  appRemindersAdd: '/app/reminders/add',
  appRemindersDetail: '/app/reminders/:reminderId',
  appRemindersEdit: '/app/reminders/:reminderId/edit',

  /** Notes hub (Phase 8). */
  appNotes: '/app/notes',
  appNotesAdd: '/app/notes/add',
  appNotesDetail: '/app/notes/:noteId',
  appNotesEdit: '/app/notes/:noteId/edit',
  appNotesShared: '/app/notes/shared',
  appNotesPrivate: '/app/notes/private',

  /** Places feature (Phase 14). */
  appPlaces: '/app/places',
  appPlacesAdd: '/app/places/add',
  appPlacesDetail: '/app/places/:placeId',
  appPlacesEdit: '/app/places/:placeId/edit',

  /** Mood feature (Phase 15). */
  appMood: '/app/mood',
  appMoodAdd: '/app/mood/add',
  appMoodHistory: '/app/mood/history',
  appMoodDetail: '/app/mood/:entryId',
  appMoodEdit: '/app/mood/:entryId/edit',

  /** Period Tracker feature (Phase 16). */
  appPeriod: '/app/period',
  appPeriodLog: '/app/period/log',
  appPeriodCalendar: '/app/period/calendar',
  appPeriodHistory: '/app/period/history',
  appPeriodSettings: '/app/period/settings',
  appPeriodDetail: '/app/period/:entryId',
  appPeriodEdit: '/app/period/:entryId/edit',

  /** Vault feature (Phase 17). */
  appVault: '/app/vault',
  appVaultAdd: '/app/vault/add',
  appVaultDetail: '/app/vault/:itemId',
  appVaultEdit: '/app/vault/:itemId/edit',

  /** More menu. */
  appMore: '/app/more',
  appMoreSettings: '/app/more/settings',
  appMoreSearch: '/app/more/search',
  appMoreVault: '/app/more/vault',
  appMoreAbout: '/app/more/about',

  /** Notification Center (Phase 18). */
  appNotifications: '/app/notifications',

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
