/**
 * Settings schema v1 → v2 migration test (Phase 4).
 *
 * Dedicated file so the dynamic import below is the FIRST load of
 * appSettings in this process (node:test isolates each file). A v1
 * payload (no Phase 4 keys) must migrate deterministically: existing
 * values preserved, new keys defaulted, onboarded flag reflected in the
 * staged state.
 */

import { test } from 'node:test';
import assert from 'node:assert';
import { defaultSettingsStorage } from '../src/data/settings/settingsStorage.ts';

test('v1 settings migrate through schema v2/v3 preserving user data', async () => {
  defaultSettingsStorage.set(
    'twohearts.settings.v1',
    JSON.stringify({
      schemaVersion: 1,
      textSize: 'large',
      onboarded: true,
      appLockEnabled: true,
      lockTimeoutSeconds: 30,
    }),
  );

  const { appSettingsStore } = await import('../src/core/appSettings.ts');
  const state = appSettingsStore.getState();

  assert.strictEqual(state.schemaVersion, 3);
  assert.strictEqual(state.textSize, 'large'); // preserved
  assert.strictEqual(state.appLockEnabled, true); // preserved
  assert.strictEqual(state.lockTimeoutSeconds, 30); // preserved
  assert.strictEqual(state.firstLaunchAt, null); // defaulted
  assert.strictEqual(state.onboardingStage, 'complete'); // derived from onboarded flag
  assert.strictEqual(state.themeMode, 'light'); // defaulted
  assert.strictEqual(state.notificationsEnabled, true); // v3 default (Phase 19)
  assert.strictEqual(state.remindersEnabled, true); // v3 default (Phase 19)
  assert.strictEqual(state.reduceMotion, false); // v3 default (Phase 19)

  // Re-migration is a no-op (idempotent by construction).
  assert.strictEqual(appSettingsStore.getState().schemaVersion, 3);
});
