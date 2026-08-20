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

  /** Main application shell (unlocked). */
  appRoot: '/app',
  appHome: '/app/home',

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
