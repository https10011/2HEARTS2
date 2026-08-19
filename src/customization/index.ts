/**
 * Owner customization entry point.
 *
 * ONE clearly identifiable place for owner customization
 * (MasterPrompt §17–18). Subfolders:
 *   - branding/   logo + app icon assets
 *   - theme/      owner-editable color/typography identity
 *   - games/      game questions/answers/choices (populated Phase 12+)
 *   - defaults/   feature defaults (text size, partner terms, etc.)
 *
 * The customization guide (TWOHEARTS_CUSTOMIZATION_GUIDE.md) documents
 * every editable file path used here.
 */

export { ownerTheme } from './theme/ownerTheme';
export type { OwnerTheme } from './theme/ownerTheme';

export { ownerDefaults } from './defaults/ownerDefaults';
