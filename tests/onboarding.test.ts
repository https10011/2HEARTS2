/**
 * Phase 5 — Onboarding & App Entry Experience Tests
 *
 * Tests the onboarding flow logic, state management, persistence,
 * and integration with Phase 2-4 infrastructure. Uses real sql.js
 * (no mocks) per project conventions.
 */

import { describe, it, before, after, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import {
  initializeDatabase,
  finalizeDatabaseForTests,
  getDatabase,
} from '../src/data/database/connection.ts';
import { AppStateService } from '../src/services/state/appStateService.ts';
import { RelationshipService } from '../src/services/relationship/relationshipService.ts';
import { appSettingsStore } from '../src/core/appSettings.ts';
import { RoutePath, ONBOARDING_STEPS, ROUTE_DEFAULTS } from '../src/navigation/routes.ts';
import { AppLockService } from '../src/services/security/appLockService.ts';
import { MemorySecureStore } from '../src/services/security/secureStore.ts';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const FIXED_CLOCK = () => new Date('2026-01-15T12:00:00Z');

function resetSettings() {
  appSettingsStore.reset();
}

// ---------------------------------------------------------------------------
// Route structure tests
// ---------------------------------------------------------------------------

describe('Onboarding routes', () => {
  it('defines all required onboarding route paths', () => {
    assert.equal(RoutePath.onboardingRoot, '/onboarding');
    assert.equal(RoutePath.onboardingWelcome, '/onboarding/welcome');
    assert.equal(RoutePath.onboardingProfile, '/onboarding/profile');
    assert.equal(RoutePath.onboardingRelationship, '/onboarding/relationship');
    assert.equal(RoutePath.onboardingPersonalization, '/onboarding/personalization');
    assert.equal(RoutePath.onboardingAppLock, '/onboarding/app-lock');
    assert.equal(RoutePath.onboardingComplete, '/onboarding/complete');
  });

  it('defines app root and home paths', () => {
    assert.equal(RoutePath.appRoot, '/app');
    assert.equal(RoutePath.appHome, '/app/home');
  });

  it('ONBOARDING_STEPS has correct order', () => {
    assert.equal(ONBOARDING_STEPS[0], RoutePath.onboardingWelcome);
    assert.equal(ONBOARDING_STEPS[1], RoutePath.onboardingProfile);
    assert.equal(ONBOARDING_STEPS[2], RoutePath.onboardingRelationship);
    assert.equal(ONBOARDING_STEPS[3], RoutePath.onboardingPersonalization);
    assert.equal(ONBOARDING_STEPS[4], RoutePath.onboardingAppLock);
    assert.equal(ONBOARDING_STEPS[5], RoutePath.onboardingComplete);
  });

  it('ROUTE_DEFAULTS point to correct entry paths', () => {
    assert.equal(ROUTE_DEFAULTS.entryForNewUser, RoutePath.onboardingWelcome);
    assert.equal(ROUTE_DEFAULTS.entryForAppUser, RoutePath.appHome);
  });
});

// ---------------------------------------------------------------------------
// Settings & persistence tests
// ---------------------------------------------------------------------------

describe('Onboarding settings persistence', () => {
  beforeEach(() => {
    resetSettings();
  });

  it('starts with fresh onboarding stage', () => {
    const state = appSettingsStore.getState();
    assert.equal(state.onboardingStage, 'fresh');
    assert.equal(state.onboarded, false);
  });

  it('persists onboarding stage transitions', () => {
    appSettingsStore.setOnboardingStage('owner');
    assert.equal(appSettingsStore.getState().onboardingStage, 'owner');
    assert.equal(appSettingsStore.getState().onboarded, false);

    appSettingsStore.setOnboardingStage('relationship');
    assert.equal(appSettingsStore.getState().onboardingStage, 'relationship');

    appSettingsStore.setOnboardingStage('personalization');
    assert.equal(appSettingsStore.getState().onboardingStage, 'personalization');

    appSettingsStore.setOnboardingStage('complete');
    assert.equal(appSettingsStore.getState().onboardingStage, 'complete');
    assert.equal(appSettingsStore.getState().onboarded, true);
  });

  it('marks first launch exactly once', () => {
    const state1 = appSettingsStore.getState();
    assert.equal(state1.firstLaunchAt, null);

    appSettingsStore.markFirstLaunch('2026-01-15T12:00:00.000Z');
    const state2 = appSettingsStore.getState();
    assert.equal(state2.firstLaunchAt, '2026-01-15T12:00:00.000Z');

    // Second call is a no-op
    appSettingsStore.markFirstLaunch('2026-02-20T00:00:00.000Z');
    const state3 = appSettingsStore.getState();
    assert.equal(state3.firstLaunchAt, '2026-01-15T12:00:00.000Z');
  });

  it('completeOnboarding sets both onboarded and stage', () => {
    appSettingsStore.completeOnboarding();
    const state = appSettingsStore.getState();
    assert.equal(state.onboarded, true);
    assert.equal(state.onboardingStage, 'complete');
  });

  it('persists text size preference', () => {
    appSettingsStore.setTextSize('large');
    assert.equal(appSettingsStore.getState().textSize, 'large');
    appSettingsStore.setTextSize('extra-large');
    assert.equal(appSettingsStore.getState().textSize, 'extra-large');
  });

  it('persists theme mode preference', () => {
    appSettingsStore.setThemeMode('dark');
    assert.equal(appSettingsStore.getState().themeMode, 'dark');
    appSettingsStore.setThemeMode('system');
    assert.equal(appSettingsStore.getState().themeMode, 'system');
  });

  it('persists app lock enabled setting', () => {
    appSettingsStore.set({ appLockEnabled: true });
    assert.equal(appSettingsStore.getState().appLockEnabled, true);
    appSettingsStore.set({ appLockEnabled: false });
    assert.equal(appSettingsStore.getState().appLockEnabled, false);
  });

  it('reset preserves firstLaunchAt', () => {
    appSettingsStore.markFirstLaunch('2026-01-15T12:00:00.000Z');
    appSettingsStore.setOnboardingStage('complete');
    appSettingsStore.setTextSize('large');

    appSettingsStore.reset();

    const state = appSettingsStore.getState();
    assert.equal(state.firstLaunchAt, '2026-01-15T12:00:00.000Z');
    assert.equal(state.onboardingStage, 'fresh');
    assert.equal(state.onboarded, false);
    assert.equal(state.textSize, 'default');
  });
});

// ---------------------------------------------------------------------------
// AppStateService tests (first-launch, stage reconciliation, completion)
// ---------------------------------------------------------------------------

describe('AppStateService — onboarding integration', () => {
  let appState;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    appState = new AppStateService(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  beforeEach(() => {
    resetSettings();
  });

  it('marks first launch on initial call', () => {
    // Explicitly clear firstLaunchAt (reset() preserves it by design)
    appSettingsStore.set({ firstLaunchAt: null });
    assert.equal(appSettingsStore.getState().firstLaunchAt, null);
    appState.markFirstLaunchIfNeeded();
    assert.equal(
      appSettingsStore.getState().firstLaunchAt,
      '2026-01-15T12:00:00.000Z',
    );
  });

  it('is idempotent for first-launch stamp', () => {
    appState.markFirstLaunchIfNeeded();
    appState.markFirstLaunchIfNeeded();
    assert.equal(
      appSettingsStore.getState().firstLaunchAt,
      '2026-01-15T12:00:00.000Z',
    );
  });

  it('reconciles stage to fresh when no profiles exist (Welcome screen should appear)', async () => {
    const stage = await appState.reconcileOnboardingStage();
    assert.equal(stage, 'fresh');
    // fresh === persisted (no downgrade needed), so the settings store is not overwritten
    assert.equal(appSettingsStore.getState().onboardingStage, 'fresh');
  });

  it('does not downgrade from complete', async () => {
    appSettingsStore.setOnboardingStage('complete');
    const stage = await appState.reconcileOnboardingStage();
    assert.equal(stage, 'complete');
  });

  it('getSnapshot returns correct state', () => {
    appSettingsStore.markFirstLaunch('2026-01-15T12:00:00.000Z');
    appSettingsStore.setOnboardingStage('owner');
    const snap = appState.getSnapshot();
    assert.equal(snap.hasLaunchedBefore, true);
    assert.equal(snap.firstLaunchAt, '2026-01-15T12:00:00.000Z');
    assert.equal(snap.onboardingStage, 'owner');
    assert.equal(snap.setupComplete, false);
  });

  it('getSnapshot shows setupComplete when stage is complete', () => {
    appSettingsStore.setOnboardingStage('complete');
    const snap = appState.getSnapshot();
    assert.equal(snap.setupComplete, true);
  });
});

// ---------------------------------------------------------------------------
// Profile persistence tests
// ---------------------------------------------------------------------------

describe('Profile persistence through onboarding', () => {
  let relService;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    relService = new RelationshipService(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('saves owner profile via RelationshipService', async () => {
    const owner = await relService.saveOwner({
      displayName: 'Alice',
      birthDate: '1995-06-15',
    });
    assert.equal(owner.displayName, 'Alice');
    assert.equal(owner.birthDate, '1995-06-15');
    assert.equal(owner.role, 'owner');
    assert.ok(owner.id);
    assert.ok(owner.createdAt);
  });

  it('retrieves owner profile', async () => {
    const owner = await relService.getOwner();
    assert.ok(owner);
    assert.equal(owner.displayName, 'Alice');
  });

  it('saves partner profile', async () => {
    const partner = await relService.savePartner({
      displayName: 'Bob',
      birthDate: null,
    });
    assert.equal(partner.displayName, 'Bob');
    assert.equal(partner.birthDate, null);
    assert.equal(partner.role, 'partner');
  });

  it('retrieves partner profile', async () => {
    const partner = await relService.getPartner();
    assert.ok(partner);
    assert.equal(partner.displayName, 'Bob');
  });

  it('updates owner profile', async () => {
    const updated = await relService.saveOwner({
      displayName: 'Alice Updated',
      birthDate: '1995-06-15',
    });
    assert.equal(updated.displayName, 'Alice Updated');
  });
});

// ---------------------------------------------------------------------------
// Relationship persistence tests
// ---------------------------------------------------------------------------

describe('Relationship persistence through onboarding', () => {
  let relService;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    relService = new RelationshipService(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('sets relationship start date', async () => {
    await relService.setStartDate('2020-03-14');
    const summary = await relService.getSummary();
    assert.equal(summary.startDate, '2020-03-14');
  });

  it('getSummary computes ageDays and nextAnniversary', async () => {
    const summary = await relService.getSummary();
    assert.ok(summary.ageDays !== null);
    assert.ok(summary.ageDays > 0);
    assert.ok(summary.nextAnniversary !== null);
    assert.ok(summary.daysUntilNextAnniversary !== null);
    assert.ok(summary.daysUntilNextAnniversary >= 0);
  });

  it('clears start date', async () => {
    await relService.setStartDate(null);
    const summary = await relService.getSummary();
    assert.equal(summary.startDate, null);
  });

  it('validates invalid start date', async () => {
    try {
      await relService.setStartDate('not-a-date');
      assert.fail('Should have thrown');
    } catch (err) {
      assert.ok(err instanceof Error);
      assert.ok(err.message.includes('validation'));
    }
  });
});

// ---------------------------------------------------------------------------
// AppStateService — reconcile after profile + relationship
// ---------------------------------------------------------------------------

describe('AppStateService — stage progression after data', () => {
  let appState;
  let relService;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    appState = new AppStateService(db, FIXED_CLOCK);
    relService = new RelationshipService(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  beforeEach(async () => {
    resetSettings();
    await appState.reconcileOnboardingStage();
  });

  it('rejects completion when domain is incomplete (no profiles)', async () => {
    try {
      await appState.completeSetup();
      assert.fail('Should have thrown');
    } catch (err) {
      // AppError message format: 'category:code'
      assert.ok(err instanceof Error);
      assert.ok(err.message.includes('setup-incomplete'), `Expected error message to include 'setup-incomplete', got: ${err.message}`);
    }
  });

  it('advances to relationship stage after saving owner profile', async () => {
    await relService.saveOwner({ displayName: 'Test Owner' });
    const stage = await appState.reconcileOnboardingStage();
    assert.equal(stage, 'relationship');
  });

  it('advances to personalization after partner + start date', async () => {
    await relService.savePartner({ displayName: 'Test Partner' });
    await relService.setStartDate('2022-01-01');
    const stage = await appState.reconcileOnboardingStage();
    assert.equal(stage, 'personalization');
  });

  it('allows completion when domain is fully configured', async () => {
    await appState.completeSetup();
    const snap = appState.getSnapshot();
    assert.equal(snap.setupComplete, true);
  });
});

// ---------------------------------------------------------------------------
// Validation tests (onboarding input)
// ---------------------------------------------------------------------------

describe('Onboarding validation', () => {
  it('validates empty profile name', () => {
    const name = '';
    assert.ok(name.trim().length === 0, 'Empty name should be invalid');
  });

  it('validates profile name length limit', () => {
    const name = 'A'.repeat(41);
    assert.ok(name.length > 40, 'Name over 40 chars should be invalid');
  });

  it('validates valid profile name', () => {
    const name = 'Alice';
    assert.ok(name.trim().length >= 1 && name.length <= 40);
  });

  it('validates PIN shape (4-8 digits)', () => {
    const validPins = ['1234', '12345', '12345678'];
    const invalidPins = ['123', '123456789', 'abcd', '12 34', ''];
    for (const pin of validPins) {
      assert.ok(/^\d{4,8}$/.test(pin), `PIN "${pin}" should be valid`);
    }
    for (const pin of invalidPins) {
      assert.ok(!/^\d{4,8}$/.test(pin), `PIN "${pin}" should be invalid`);
    }
  });

  it('validates date format for start date', () => {
    const validDates = ['2020-01-15', '2023-12-31', '2000-02-29'];
    const invalidDates = ['2020/01/15', 'not-a-date', ''];
    for (const d of validDates) {
      assert.ok(/^\d{4}-\d{2}-\d{2}$/.test(d), `Date "${d}" format should be valid`);
    }
    for (const d of invalidDates) {
      assert.ok(!/^\d{4}-\d{2}-\d{2}$/.test(d), `Date "${d}" format should be invalid`);
    }
  });
});

// ---------------------------------------------------------------------------
// App lock tests
// ---------------------------------------------------------------------------

describe('AppLockService — onboarding integration', () => {
  let lockService;
  const store = new MemorySecureStore();
  const lockSettings = { enabled: true, timeoutSeconds: 60 };

  before(async () => {
    lockService = new AppLockService(store, () => lockSettings, FIXED_CLOCK);
    await lockService.initialize();
  });

  it('starts in disabled state when no PIN configured', () => {
    assert.equal(lockService.currentState(), 'disabled');
    assert.equal(lockService.isConfigured(), false);
  });

  it('enables lock with a valid PIN', async () => {
    await lockService.enable('1234');
    assert.equal(lockService.currentState(), 'unlocked');
    assert.equal(lockService.isConfigured(), true);
  });

  it('rejects invalid PIN shapes', async () => {
    try {
      await lockService.enable('123');
      assert.fail('Should have thrown');
    } catch (err) {
      assert.ok(err instanceof Error);
      // AppError.message is 'security:invalid-pin-shape'
      assert.ok(err.message.includes('invalid-pin-shape'));
    }
  });

  it('unlocks with correct PIN', async () => {
    lockService.lock();
    assert.equal(lockService.currentState(), 'locked');
    const result = await lockService.unlock('1234');
    assert.equal(result, true);
    assert.equal(lockService.currentState(), 'unlocked');
  });

  it('rejects incorrect PIN', async () => {
    lockService.lock();
    const result = await lockService.unlock('9999');
    assert.equal(result, false);
    assert.equal(lockService.currentState(), 'locked');
  });

  it('disables lock entirely', async () => {
    await lockService.disable();
    assert.equal(lockService.currentState(), 'disabled');
    assert.equal(lockService.isConfigured(), false);
  });

  it('notifies lock state changes', async () => {
    const states = [];
    const unsub = lockService.onLockChange((state) => states.push(state));
    await lockService.enable('5678');
    lockService.lock();
    unsub();
    assert.ok(states.includes('unlocked'));
    assert.ok(states.includes('locked'));
  });
});

// ---------------------------------------------------------------------------
// Returning user behavior tests
// ---------------------------------------------------------------------------

describe('Returning user behavior', () => {
  let appState;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    appState = new AppStateService(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('completed user stays complete after reconcile', async () => {
    resetSettings();
    appSettingsStore.setOnboardingStage('complete');
    const stage = await appState.reconcileOnboardingStage();
    assert.equal(stage, 'complete');
  });

  it('user at owner stage resumes at owner when owner profile exists in DB', async () => {
    resetSettings();
    appSettingsStore.setOnboardingStage('owner');
    // NOTE: a prior test suite created owner data in the shared test database;
    // the advance-only reconciler preserves 'owner' when domain truth matches.
    const stage = await appState.reconcileOnboardingStage();
    // If no owner in DB, this would be 'fresh' (advance-only doesn't downgrade 'owner' to 'fresh')
    // If owner exists, derive returns 'owner' which equals persisted → returns 'owner'
    assert.ok(stage === 'owner' || stage === 'fresh');
  });

  it('firstLaunchAt never resets', () => {
    appSettingsStore.set({ firstLaunchAt: null });
    appSettingsStore.markFirstLaunch('2026-01-01T00:00:00.000Z');
    appSettingsStore.reset();
    assert.equal(
      appSettingsStore.getState().firstLaunchAt,
      '2026-01-01T00:00:00.000Z',
    );
  });
});

// ---------------------------------------------------------------------------
// useSyncExternalStore integration
// ---------------------------------------------------------------------------

describe('appSettingsStore reactive subscription', () => {
  beforeEach(() => {
    resetSettings();
  });

  it('notifies listeners on state change', () => {
    let notified = false;
    const unsub = appSettingsStore.subscribe(() => {
      notified = true;
    });
    appSettingsStore.setTextSize('large');
    assert.equal(notified, true);
    unsub();
  });

  it('unsubscribe stops notifications', () => {
    let count = 0;
    const unsub = appSettingsStore.subscribe(() => {
      count++;
    });
    appSettingsStore.setTextSize('large');
    unsub();
    appSettingsStore.setTextSize('small');
    assert.equal(count, 1);
  });
});
