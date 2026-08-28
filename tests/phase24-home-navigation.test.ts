/**
 * Phase 24 — Home & Global Navigation Experience tests.
 *
 * Verifies the redesigned navigation vocabulary (navConfig.ts — the single
 * source of truth) and guards the key structural contracts:
 *
 *   1. Five-position bottom navigation with the TwoHearts hub in the CENTER.
 *   2. Home offers exactly the curated everyday set (Notes · Reminders ·
 *      Us · Games) and never the relationship archives.
 *   3. The central couple hub covers every relationship-specific feature.
 *   4. The More menu holds utilities only — no duplication.
 *   5. Every destination route exists in the authoritative RoutePath map.
 *   6. Icon keys resolve through the centralized Icon bridge (navIcons).
 *   7. Source guards: center branding via BrandLogo, back-button contract,
 *      route-transition + active-state classes wired in the design system.
 *
 * No DOM, no mocks — data assertions run against the real config module and
 * source guards read the real files (same style as designTokens.test.ts).
 */

import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';

import { RoutePath } from '../src/navigation/routes.ts';
import {
  BOTTOM_NAV_ITEMS,
  NAV_ROOT_ROUTES,
  HOME_PRIMARY_ITEMS,
  COUPLE_HUB_GROUPS,
  COUPLE_HUB_ITEMS,
  MORE_ITEMS,
} from '../src/features/app-shell/navConfig.ts';

const ROOT = process.cwd();
const read = (rel: string) => readFileSync(join(ROOT, rel), 'utf8');

const ALL_ROUTES = new Set(Object.values(RoutePath));

// ---------------------------------------------------------------------------
// 1. Five-position bottom navigation
// ---------------------------------------------------------------------------

test('bottom navigation has exactly five positions', () => {
  assert.equal(BOTTOM_NAV_ITEMS.length, 5);
});

test('bottom navigation order: Home · Notifications · TWOHEARTS · Notes · More', () => {
  assert.deepEqual(
    BOTTOM_NAV_ITEMS.map((i) => i.id),
    ['home', 'notifications', 'twohearts', 'notes', 'more'],
  );
  assert.deepEqual(
    BOTTOM_NAV_ITEMS.map((i) => i.route),
    [
      RoutePath.appHome,
      RoutePath.appNotifications,
      RoutePath.appUs,
      RoutePath.appNotes,
      RoutePath.appMore,
    ],
  );
});

test('the central TwoHearts hub occupies the middle position only', () => {
  const centers = BOTTOM_NAV_ITEMS.filter((i) => i.center);
  assert.equal(centers.length, 1, 'exactly one center item');
  assert.equal(BOTTOM_NAV_ITEMS[2]?.id, 'twohearts', 'center is index 2 of 5');
  assert.equal(centers[0].route, RoutePath.appUs);
});

test('bottom navigation routes are unique and registered', () => {
  const routes = BOTTOM_NAV_ITEMS.map((i) => i.route);
  assert.equal(new Set(routes).size, routes.length);
  for (const route of routes) {
    assert.ok(ALL_ROUTES.has(route), `${route} must exist in RoutePath`);
  }
});

test('NAV_ROOT_ROUTES is derived from the five navigation positions', () => {
  assert.deepEqual([...NAV_ROOT_ROUTES], BOTTOM_NAV_ITEMS.map((i) => i.route));
});

// ---------------------------------------------------------------------------
// 2. Home primary content — curated, not a dashboard of everything
// ---------------------------------------------------------------------------

test('Home offers exactly Notes · Reminders · Us · Games', () => {
  assert.deepEqual(
    HOME_PRIMARY_ITEMS.map((i) => i.id),
    ['notes', 'reminders', 'us', 'games'],
  );
  assert.deepEqual(
    HOME_PRIMARY_ITEMS.map((i) => i.route),
    [RoutePath.appNotes, RoutePath.appReminders, RoutePath.appUs, RoutePath.appGames],
  );
});

test('Home does not expose relationship archives directly', () => {
  const homeRoutes = new Set(HOME_PRIMARY_ITEMS.map((i) => i.route));
  const archives = [
    RoutePath.appMemories,
    RoutePath.appTimelineRoot,
    RoutePath.appPlaces,
    RoutePath.appMood,
    RoutePath.appPeriod,
    RoutePath.appVault,
  ];
  for (const archive of archives) {
    assert.ok(!homeRoutes.has(archive), `${archive} must not sit on Home`);
  }
});

// ---------------------------------------------------------------------------
// 3. Central couple hub — everything about the couple, one home
// ---------------------------------------------------------------------------

test('couple hub covers every relationship-specific feature', () => {
  const hubIds = COUPLE_HUB_ITEMS.map((i) => i.id);
  for (const expected of [
    'memories',
    'timeline',
    'important-dates',
    'places',
    'mood',
    'period',
    'vault',
  ]) {
    assert.ok(hubIds.includes(expected), `hub is missing ${expected}`);
  }
});

test('couple hub groups are the approved story/world split', () => {
  assert.deepEqual(
    COUPLE_HUB_GROUPS.map((g) => g.id),
    ['story', 'world'],
  );
  assert.deepEqual(
    COUPLE_HUB_GROUPS[0].items.map((i) => i.id),
    ['memories', 'timeline', 'important-dates'],
  );
  assert.deepEqual(
    COUPLE_HUB_GROUPS[1].items.map((i) => i.id),
    ['places', 'mood', 'period', 'vault'],
  );
});

test('every couple-hub destination is a registered route', () => {
  for (const item of COUPLE_HUB_ITEMS) {
    assert.ok(ALL_ROUTES.has(item.route), `${item.id} → ${item.route} must exist in RoutePath`);
  }
});

test('bottom navigation never duplicates hub feature routes', () => {
  const navRoutes = new Set(BOTTOM_NAV_ITEMS.map((i) => i.route));
  for (const item of COUPLE_HUB_ITEMS) {
    assert.ok(
      !navRoutes.has(item.route),
      `${item.route} must be reached through the hub, not a nav slot`,
    );
  }
});

// ---------------------------------------------------------------------------
// 4. More menu — utilities only
// ---------------------------------------------------------------------------

test('More menu is exactly Settings · Search · About', () => {
  assert.deepEqual(
    MORE_ITEMS.map((i) => i.id),
    ['settings', 'search', 'about'],
  );
});

test('More menu duplicates no primary navigation or hub destination', () => {
  const taken = new Set([
    ...BOTTOM_NAV_ITEMS.map((i) => i.route),
    ...HOME_PRIMARY_ITEMS.map((i) => i.route),
    ...COUPLE_HUB_ITEMS.map((i) => i.route),
  ]);
  for (const item of MORE_ITEMS) {
    assert.ok(!taken.has(item.route), `${item.route} is duplicated in the More menu`);
  }
});

// ---------------------------------------------------------------------------
// 5. Every navigation destination is a real route
// ---------------------------------------------------------------------------

test('every declared destination route exists in RoutePath', () => {
  const all = [
    ...BOTTOM_NAV_ITEMS,
    ...HOME_PRIMARY_ITEMS,
    ...COUPLE_HUB_ITEMS,
    ...MORE_ITEMS,
  ];
  for (const item of all) {
    assert.ok(ALL_ROUTES.has(item.route), `${item.id} → ${item.route} not in RoutePath`);
    assert.ok(item.label.length > 0, `${item.id} needs a label`);
  }
});

// ---------------------------------------------------------------------------
// 6. Icon keys resolve through the centralized bridge
// ---------------------------------------------------------------------------

test('every icon key is resolved by navIcons.tsx', () => {
  const bridge = read('src/features/app-shell/navIcons.tsx');
  const keys = new Set(
    [...BOTTOM_NAV_ITEMS, ...HOME_PRIMARY_ITEMS, ...COUPLE_HUB_ITEMS, ...MORE_ITEMS].map(
      (i) => i.icon,
    ),
  );
  for (const key of keys) {
    assert.ok(
      bridge.includes(`'${key}'`) || bridge.includes(`  ${key}:`),
      `navIcons.tsx must resolve icon key '${key}'`,
    );
  }
});

// ---------------------------------------------------------------------------
// 7. Source guards — presentation wiring
// ---------------------------------------------------------------------------

test('center navigation button uses the official BrandLogo mark', () => {
  const bottomNav = read('src/features/app-shell/BottomNav.tsx');
  assert.ok(bottomNav.includes('BrandLogo'), 'center button must use BrandLogo');
  assert.ok(bottomNav.includes('variant="mark"'), 'center button renders the hearts mark');
  assert.ok(
    bottomNav.includes('aria-label'),
    'center button must carry an accessible label',
  );
});

test('AppShell keeps the improved Android back contract', () => {
  const shell = read('src/features/app-shell/AppShell.tsx');
  assert.ok(shell.includes('backButton'), 'AppShell must listen for the back button');
  assert.ok(
    shell.includes('path === RoutePath.appHome'),
    'back is suppressed only at the Home root',
  );
  assert.ok(shell.includes('router.navigate(-1)'), 'nested back returns to previous context');
});

test('route transitions are wired through the keyed wrapper', () => {
  const shell = read('src/features/app-shell/AppShell.tsx');
  assert.ok(shell.includes('th-route-transition'), 'route transition class must wrap Outlet');
  assert.ok(shell.includes('useLocation'), 'transition keys on the location');
  const css = read('src/components/primitives.css');
  assert.ok(css.includes('@keyframes th-route-in'), 'route entrance keyframes defined');
  assert.ok(
    css.includes("[data-th-motion='reduced'] .th-route-transition"),
    'reduced-motion short-circuits route transitions',
  );
});

test('Home header renders both avatars around the official branding', () => {
  const home = read('src/features/app-shell/screens/HomeScreen.tsx');
  assert.ok(home.includes('BrandLogo'), 'Home header must carry the official logo');
  assert.ok(home.includes('variant="mark"'), 'Home header uses the brand mark as connector');
  assert.ok(home.includes('summary?.owner'), 'owner avatar bound to owner profile');
  assert.ok(home.includes('summary?.partner'), 'partner avatar bound to partner profile');
  assert.ok(home.includes('HOME_PRIMARY_ITEMS'), 'Home consumes the curated config');
  assert.ok(home.includes('RoseLilyDecoration'), 'Home carries a subtle floral');
});

test('Home never links relationship archives directly', () => {
  const home = read('src/features/app-shell/screens/HomeScreen.tsx');
  for (const forbidden of ['appMemories', 'appTimelineRoot', 'appPlaces', 'appMood', 'appPeriod', 'appVault']) {
    assert.ok(!home.includes(forbidden), `Home must not reference ${forbidden}`);
  }
});

test('couple hub screen consumes the centralized hub config', () => {
  const us = read('src/features/app-shell/screens/UsScreen.tsx');
  assert.ok(us.includes('COUPLE_HUB_GROUPS'), 'UsScreen must render the hub groups');
  assert.ok(!us.includes('appUsMemories'), 'hub links route via config, not hardcoded paths');
});

test('design system exposes the Phase 24 interaction classes', () => {
  const css = read('src/components/primitives.css');
  for (const cls of [
    '.th-bottom-nav-item--center',
    '.th-bottom-nav-center__ring',
    '.th-bottom-nav-dot',
    '.th-home-header',
    '.th-home-avatar',
    '.th-home-grid',
    '.th-home-card',
    '.th-hub-section-title',
  ]) {
    assert.ok(css.includes(cls), `primitives.css must define ${cls}`);
  }
});
