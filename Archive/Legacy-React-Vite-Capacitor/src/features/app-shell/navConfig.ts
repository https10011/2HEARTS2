/**
 * Navigation configuration (Phase 24) — the ONE navigation vocabulary map.
 *
 * Every primary destination of the application is declared here exactly once:
 *
 *   BOTTOM_NAV_ITEMS  — the five-position global bottom navigation, whose
 *                       CENTER slot is the TwoHearts relationship hub.
 *   HOME_PRIMARY_ITEMS — the curated everyday surfaces offered on Home
 *                       (Notes · Reminders · Us · Games — nothing else).
 *   COUPLE_HUB_ITEMS  — everything that is primarily about the couple,
 *                       reached through the central TwoHearts hub.
 *   MORE_ITEMS        — application utilities (settings, search, about).
 *
 * Icons are string keys resolved through `navIcons.tsx` so this module stays
 * importable from Node tests (no JSX, no component imports — plain data).
 * Change a destination here and the bottom nav, Home, the couple hub, the
 * More menu, tab-root back behavior, and the tests all follow one source.
 */

import { RoutePath } from '../../navigation/routes.ts';

/** Keys into the centralized Icon vocabulary (resolved by navIcons.tsx). */
export type NavIconKey =
  | 'home'
  | 'bell'
  | 'file-text'
  | 'menu'
  | 'gamepad'
  | 'heart'
  | 'calendar'
  | 'map-pin'
  | 'smile'
  | 'lock'
  | 'settings'
  | 'search'
  | 'camera'
  | 'cat';

export interface NavDestination {
  /** Stable semantic id (used for tests + aria). */
  id: string;
  /** Absolute route the destination navigates to. */
  route: string;
  /** Short display label. */
  label: string;
  /** One-line supporting caption (used on cards; optional in nav). */
  caption?: string;
  /** Icon vocabulary key (center hub renders the official brand mark). */
  icon: NavIconKey;
  /** True only for the central TwoHearts hub position. */
  center?: boolean;
}

// ---------------------------------------------------------------------------
// Global bottom navigation — exactly five positions (RoadMap "New bottom
// navigation concept"): Home · Notifications · TWOHEARTS · Notes · More.
// The center position is the relationship itself: larger, branded, special.
// ---------------------------------------------------------------------------

export const BOTTOM_NAV_ITEMS: readonly NavDestination[] = [
  {
    id: 'home',
    route: RoutePath.appHome,
    label: 'Home',
    icon: 'home',
  },
  {
    id: 'notifications',
    route: RoutePath.appNotifications,
    label: 'Notifications',
    icon: 'bell',
  },
  {
    id: 'twohearts',
    route: RoutePath.appUs,
    label: 'Us',
    caption: 'Your relationship world',
    icon: 'heart',
    center: true,
  },
  {
    id: 'notes',
    route: RoutePath.appNotes,
    label: 'Notes',
    icon: 'file-text',
  },
  {
    id: 'more',
    route: RoutePath.appMore,
    label: 'More',
    icon: 'menu',
  },
] as const;

/** Route paths where the Android back button must not fire history-back. */
export const NAV_ROOT_ROUTES: readonly string[] = BOTTOM_NAV_ITEMS.map(
  (item) => item.route,
);

// ---------------------------------------------------------------------------
// Home primary content — the everyday actions (NOT the relationship archive;
// relationship-specific features live in the couple hub below).
// ---------------------------------------------------------------------------

export const HOME_PRIMARY_ITEMS: readonly NavDestination[] = [
  {
    id: 'notes',
    route: RoutePath.appNotes,
    label: 'Notes',
    caption: 'Letters, lists & thoughts',
    icon: 'file-text',
  },
  {
    id: 'reminders',
    route: RoutePath.appReminders,
    label: 'Reminders',
    caption: 'Never miss a moment',
    icon: 'bell',
  },
  {
    id: 'us',
    route: RoutePath.appUs,
    label: 'Us',
    caption: 'Your relationship world',
    icon: 'heart',
  },
  {
    id: 'yuki',
    route: RoutePath.appYuki,
    label: 'Yuki',
    caption: 'Your companion',
    icon: 'cat',
  },
] as const;

// ---------------------------------------------------------------------------
// Central TwoHearts hub — everything primarily about the couple.
// Grouped for presentation; routes are the existing feature routes.
// ---------------------------------------------------------------------------

export interface CoupleHubGroup {
  id: string;
  title: string;
  items: readonly NavDestination[];
}

export const COUPLE_HUB_GROUPS: readonly CoupleHubGroup[] = [
  {
    id: 'story',
    title: 'Our story',
    items: [
      {
        id: 'memories',
        route: RoutePath.appMemories,
        label: 'Our Memories',
        caption: 'Photos & moments together',
        icon: 'camera',
      },
      {
        id: 'timeline',
        route: RoutePath.appTimelineRoot,
        label: 'Our Timeline',
        caption: 'Your story over time',
        icon: 'calendar',
      },
      {
        id: 'important-dates',
        route: RoutePath.appUsReminders,
        label: 'Important Dates',
        caption: 'Anniversaries & occasions',
        icon: 'heart',
      },
    ],
  },
  {
    id: 'world',
    title: 'Our world',
    items: [
      {
        id: 'places',
        route: RoutePath.appPlaces,
        label: 'Our Places',
        caption: 'Meaningful locations',
        icon: 'map-pin',
      },
      {
        id: 'mood',
        route: RoutePath.appMood,
        label: 'Mood',
        caption: 'How are you feeling?',
        icon: 'smile',
      },
      {
        id: 'period',
        route: RoutePath.appPeriod,
        label: 'Period Tracker',
        caption: 'Track your cycle',
        icon: 'calendar',
      },
      {
        id: 'vault',
        route: RoutePath.appVault,
        label: 'Private Vault',
        caption: 'Locked, just for you two',
        icon: 'lock',
      },
    ],
  },
] as const;

/** Flat lookup of every couple-hub destination (tests + guards). */
export const COUPLE_HUB_ITEMS: readonly NavDestination[] = COUPLE_HUB_GROUPS.flatMap(
  (group) => group.items,
);

// ---------------------------------------------------------------------------
// More menu — application utilities only. Relationship destinations live in
// the couple hub; primary destinations live in the bottom navigation.
// ---------------------------------------------------------------------------

export const MORE_ITEMS: readonly NavDestination[] = [
  {
    id: 'settings',
    route: RoutePath.appMoreSettings,
    label: 'Settings',
    caption: 'Make TwoHearts yours',
    icon: 'settings',
  },
  {
    id: 'search',
    route: RoutePath.appMoreSearch,
    label: 'Search',
    caption: 'Find anything, privately',
    icon: 'search',
  },
  {
    id: 'about',
    route: RoutePath.appMoreAbout,
    label: 'About',
    caption: 'Version & credits',
    icon: 'heart',
  },
] as const;
