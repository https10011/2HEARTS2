/* ------------------------------------------------------------
 * TWOHEARTS — OWNER DEFAULTS
 * ------------------------------------------------------------
 * This file controls application-wide defaults the owner may change.
 *
 * Each field has a comment explaining what it controls and the
 * expected format. Changing these does NOT require editing feature
 * logic (MasterPrompt §17, §70).
 * ------------------------------------------------------------
 */

export const ownerDefaults = {
  /**
   * App display name shown in headers / about screens.
   * Type: string.
   */
  appName: 'TwoHearts',

  /**
   * First-launch default text size. One of:
   *   'small' | 'default' | 'large' | 'extra-large'
   */
  defaultTextSize: 'default' as 'small' | 'default' | 'large' | 'extra-large',

  /**
   * Default onboarding partner language stays relationship-neutral:
   * "you" and "your special someone" (MasterPrompt §25).
   */
  partnerTerm: 'your special someone',
  partnerTermShort: 'you',
} as const;
