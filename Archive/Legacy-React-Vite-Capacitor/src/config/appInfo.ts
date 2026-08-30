/**
 * Application identity (Phase 19).
 *
 * Values mirror the authoritative project sources so the About screen shows
 * real information instead of marketing text:
 *   - name/appId ← capacitor.config.ts (appId 'com.twohearts.app')
 *   - version    ← package.json "version" (0.1.0)
 * Update alongside either source; the coupling is documented on purpose.
 */

export const APP_INFO = {
  name: 'TwoHearts',
  version: '0.1.0',
  appId: 'com.twohearts.app',
} as const;
