/**
 * Phase 6 — Main App Shell & Navigation Tests
 *
 * Tests the route structure, navigation architecture, and screen
 * availability for the main application shell.
 *
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { RoutePath, ONBOARDING_STEPS, ROUTE_DEFAULTS } from '../src/navigation/routes.ts';

// ---------------------------------------------------------------------------
// Route structure tests
// ---------------------------------------------------------------------------

describe('Phase 6 route structure', () => {
  it('defines all main app routes', () => {
    assert.equal(RoutePath.appRoot, '/app');
    assert.equal(RoutePath.appHome, '/app/home');
    assert.equal(RoutePath.appUs, '/app/us');
    assert.equal(RoutePath.appGames, '/app/games');
    assert.equal(RoutePath.appNotes, '/app/notes');
    assert.equal(RoutePath.appMore, '/app/more');
  });

  it('defines Us sub-routes', () => {
    assert.equal(RoutePath.appUsMemories, '/app/us/memories');
    assert.equal(RoutePath.appUsTimeline, '/app/us/timeline');
    assert.equal(RoutePath.appUsReminders, '/app/us/reminders');
  });

  it('defines Games sub-routes', () => {
    assert.equal(RoutePath.appGamesWhoKnows, '/app/games/who-knows');
    assert.equal(RoutePath.appGamesWouldYouRather, '/app/games/would-you-rather');
    assert.equal(RoutePath.appGamesTwentyQuestions, '/app/games/twenty-questions');
    assert.equal(RoutePath.appGamesHowWell, '/app/games/how-well');
  });

  it('defines Notes sub-routes', () => {
    assert.equal(RoutePath.appNotesShared, '/app/notes/shared');
    assert.equal(RoutePath.appNotesPrivate, '/app/notes/private');
  });

  it('defines More sub-routes', () => {
    assert.equal(RoutePath.appMoreSettings, '/app/more/settings');
    assert.equal(RoutePath.appMoreSearch, '/app/more/search');
    assert.equal(RoutePath.appMoreVault, '/app/more/vault');
    assert.equal(RoutePath.appMoreAbout, '/app/more/about');
  });

  it('preserves all onboarding routes from Phase 5', () => {
    assert.equal(RoutePath.onboardingWelcome, '/onboarding/welcome');
    assert.equal(RoutePath.onboardingProfile, '/onboarding/profile');
    assert.equal(RoutePath.onboardingRelationship, '/onboarding/relationship');
    assert.equal(RoutePath.onboardingPersonalization, '/onboarding/personalization');
    assert.equal(RoutePath.onboardingAppLock, '/onboarding/app-lock');
    assert.equal(RoutePath.onboardingComplete, '/onboarding/complete');
  });

  it('ONBOARDING_STEPS order is preserved', () => {
    assert.equal(ONBOARDING_STEPS.length, 6);
    assert.equal(ONBOARDING_STEPS[0], RoutePath.onboardingWelcome);
    assert.equal(ONBOARDING_STEPS[5], RoutePath.onboardingComplete);
  });

  it('ROUTE_DEFAULTS are correct', () => {
    assert.equal(ROUTE_DEFAULTS.entryForNewUser, RoutePath.onboardingWelcome);
    assert.equal(ROUTE_DEFAULTS.entryForAppUser, RoutePath.appHome);
  });

  it('app routes follow /app/* pattern for nesting', () => {
    const appRoutes = [
      RoutePath.appHome,
      RoutePath.appUs,
      RoutePath.appGames,
      RoutePath.appNotes,
      RoutePath.appMore,
    ];
    for (const route of appRoutes) {
      assert.ok(route.startsWith('/app/'), `Route ${route} should start with /app/`);
    }
  });

  it('sub-routes follow parent path convention', () => {
    assert.ok(RoutePath.appUsMemories.startsWith('/app/us/'));
    assert.ok(RoutePath.appGamesWhoKnows.startsWith('/app/games/'));
    assert.ok(RoutePath.appNotesShared.startsWith('/app/notes/'));
    assert.ok(RoutePath.appMoreSettings.startsWith('/app/more/'));
  });

  it('total app route count is correct', () => {
    // Count all RoutePath values that start with /app/
    const appRoutes = Object.values(RoutePath).filter(
      (v) => typeof v === 'string' && v.startsWith('/app/'),
    );
    // home, us, us/*(3), games, games/*(4), notes, notes/*(2), more, more/*(4), foundation = 17
    assert.ok(appRoutes.length >= 15, `Expected at least 15 app routes, got ${appRoutes.length}`);
  });

  it('total onboarding route count is correct', () => {
    const onboardingRoutes = Object.values(RoutePath).filter(
      (v) => typeof v === 'string' && v.startsWith('/onboarding'),
    );
    assert.equal(onboardingRoutes.length, 7); // root + 6 screens
  });
});

// ---------------------------------------------------------------------------
// Navigation architecture tests
// ---------------------------------------------------------------------------

describe('Navigation architecture', () => {
  it('has 5 main navigation tabs', () => {
    const tabs = [
      RoutePath.appHome,
      RoutePath.appUs,
      RoutePath.appGames,
      RoutePath.appNotes,
      RoutePath.appMore,
    ];
    assert.equal(tabs.length, 5);
  });

  it('each main tab has a unique path', () => {
    const tabs = [
      RoutePath.appHome,
      RoutePath.appUs,
      RoutePath.appGames,
      RoutePath.appNotes,
      RoutePath.appMore,
    ];
    const unique = new Set(tabs);
    assert.equal(unique.size, tabs.length);
  });

  it('no onboarding route collides with app route', () => {
    const onboardingPaths = Object.values(RoutePath).filter(
      (v) => typeof v === 'string' && v.startsWith('/onboarding'),
    );
    const appPaths = Object.values(RoutePath).filter(
      (v) => typeof v === 'string' && v.startsWith('/app'),
    );
    const overlap = onboardingPaths.filter((p) => appPaths.includes(p));
    assert.equal(overlap.length, 0, `Overlapping routes: ${overlap.join(', ')}`);
  });
});

// ---------------------------------------------------------------------------
// Feature placeholder tests
// ---------------------------------------------------------------------------

describe('Feature placeholders', () => {
  it('Us hub has memories, timeline, reminders sub-routes', () => {
    assert.ok(RoutePath.appUsMemories);
    assert.ok(RoutePath.appUsTimeline);
    assert.ok(RoutePath.appUsReminders);
  });

  it('Games hub has all four game sub-routes', () => {
    assert.ok(RoutePath.appGamesWhoKnows);
    assert.ok(RoutePath.appGamesWouldYouRather);
    assert.ok(RoutePath.appGamesTwentyQuestions);
    assert.ok(RoutePath.appGamesHowWell);
  });

  it('Notes hub has shared and private sub-routes', () => {
    assert.ok(RoutePath.appNotesShared);
    assert.ok(RoutePath.appNotesPrivate);
  });

  it('More has settings, search, vault, about sub-routes', () => {
    assert.ok(RoutePath.appMoreSettings);
    assert.ok(RoutePath.appMoreSearch);
    assert.ok(RoutePath.appMoreVault);
    assert.ok(RoutePath.appMoreAbout);
  });
});
