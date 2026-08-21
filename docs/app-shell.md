# TwoHearts — Main App Shell & Navigation (Phase 6, redesigned Phase 24)

Phase 6 implements the main application structure: the shell layout, bottom
navigation, home dashboard, hub screens, and navigation architecture that
future feature phases plug into.

**Phase 24 (Home & Global Navigation Experience) redesigned the presentation
of this shell** — new five-position bottom navigation with an elevated
central TwoHearts hub button, a relationship-first Home, and token-driven
route transitions — while keeping the Phase 6 route map and all feature
screens intact. The Phase 24 structure:

### Bottom navigation (Phase 24)

Five positions, from `navConfig.ts` (`BOTTOM_NAV_ITEMS` — the single source
of navigation vocabulary, bridged to the Icon set via `navIcons.tsx`):

| Position | Destination | Route | Notes |
|---|---|---|---|
| 1 | Home | `/app/home` | Everyday actions |
| 2 | Notifications | `/app/notifications` | Notification center |
| 3 | **Us (center)** | `/app/us` | Elevated circular brand button — the relationship itself |
| 4 | Notes | `/app/notes` | Notes & lists |
| 5 | More | `/app/more` | Settings, search, about |

The center button carries the official brand mark through `BrandLogo`
(`tone="light"` cream recolor on the burgundy ring — the same official
asset, never a substitute). Active states: burgundy icon/label + indicator
dot for side items; blush halo + scale for the center hub. Press feedback:
subtle scale on the icon wrap / ring. All values come from Phase 23 tokens.

### Home (Phase 24)

Home is the couple's personal space, not a dashboard of every feature:
- **Relationship header**: owner avatar (left) — hairline — official brand
  lockup (center) — partner avatar (right), with a subtle Rose/Lily corner
  decoration from the centralized decoration system. Avatars are tappable
  (owner → profile settings, partner → relationship settings), photos load
  via `MediaStorage`, missing photos fall back to the `IconSmile` icon.
- **Primary actions** (from `navConfig.HOME_PRIMARY_ITEMS`): Notes,
  Reminders, Us, Games — a calm 2×2 grid with press feedback.
- Relationship-specific features are intentionally NOT duplicated here;
  they live in the central hub.

### Us — the couple hub (Phase 24)

`UsScreen` is the conceptual center of the app ("This is us"):
- Relationship counter card (days together + next anniversary, from
  `RelationshipService` domain truth; burgundy gradient with cream text).
- `COUPLE_HUB_GROUPS`: **Our Story** (Memories, Timeline, Important Dates)
  and **Our World** (Places, Mood, Period Tracker, Private Vault).
- Footer links to profile/relationship settings.

### Back behavior (Phase 24)

`AppShell` owns the Android back button via the lifecycle bus:
- Deep/nested screen → `navigate(-1)` (returns to the previous context:
  Home → Us → Memories → back → Us).
- Home root → no-op (never accidentally exits).
- Degenerate cold start at a non-home route (no in-app history) → falls
  forward to Home instead of exiting.

### Motion (Phase 24)

Route changes get a subtle token-driven entrance (fade + 8px rise, 200ms)
keyed on the pathname in `AppShell` (`.th-route-transition`), disabled under
`reduceMotion`. Scroll resets to top on each navigation. Dark theme recolors
the official brand art to cream everywhere via one centralized rule
(`:root[data-th-theme='dark'] .th-brand-logo`).

---

## Architecture (Phase 6 original)


### Navigation hierarchy

```
/                          → OnboardingGate → redirect
/onboarding/*              → Onboarding screens (Phase 5)
/app                       → AppShell (bottom nav + Outlet)
  /app/home                → HomeScreen (dashboard)
  /app/us                  → UsScreen (relationship hub)
  /app/us/memories         → PlaceholderScreen
  /app/us/timeline         → PlaceholderScreen
  /app/us/reminders        → PlaceholderScreen
  /app/games               → GamesHubScreen
  /app/games/who-knows     → PlaceholderScreen
  /app/games/would-you-rather → PlaceholderScreen
  /app/games/twenty-questions  → PlaceholderScreen
  /app/games/how-well      → PlaceholderScreen
  /app/notes               → NotesHubScreen
  /app/notes/shared        → PlaceholderScreen
  /app/notes/private       → PlaceholderScreen
  /app/more                → MoreScreen (menu)
  /app/more/settings       → PlaceholderScreen
  /app/more/search         → PlaceholderScreen
  /app/more/vault          → PlaceholderScreen
  /app/more/about          → PlaceholderScreen
```

### App shell

The `AppShell` component is the layout wrapper for all `/app/*` routes:
- **Top**: Content area (scrollable, rendered via `<Outlet>`)
- **Bottom**: Persistent 5-tab bottom navigation bar

### Bottom navigation

Five tabs with icon + label, using `NavLink` for automatic active-state
highlighting:

| Tab | Route | Icon | Purpose |
|---|---|---|---|
| Home | `/app/home` | Home | Dashboard with greeting + shortcuts |
| Us | `/app/us` | Heart | Relationship hub |
| Games | `/app/games` | Gamepad | Couple games hub |
| Notes | `/app/notes` | FileText | Notes hub |
| More | `/app/more` | Menu | Settings, search, vault, about |

### Back button behavior

Android/system back button handled via the lifecycle event bus:
- **At tab root**: Does nothing (prevents accidental exit)
- **At child screen**: Navigates back within the tab (`router.navigate(-1)`)
- **Modal open**: Closes modal first (future integration)
- **No valid back target**: Defers to system (exits app)

### Feature screens (Phase 6 scope)

- **HomeScreen**: Greeting (from owner profile), relationship days counter,
  quick-access feature cards
- **UsScreen**: Relationship summary card (names, days together, next
  anniversary), future feature entry cards (memories, timeline, reminders)
- **GamesHubScreen**: Game entry cards (Who Knows, Would You Rather,
  20 Questions, How Well)
- **NotesHubScreen**: Notes entry cards (shared, private)
- **MoreScreen**: Menu items (settings, search, vault, about)
- **PlaceholderScreen**: Reusable "coming soon" screen for future features

## Design system

### Bottom navigation
- **Touch targets**: ≥44px height per item
- **Active state**: Burgundy color (#6A1B2B) for icon + label
- **Inactive state**: Text-secondary color
- **Border**: Top divider line, subtle shadow
- **Safe areas**: Bottom safe-area inset applied

### Feature cards
- Elevated surface with border and shadow
- Icon (blush background, burgundy icon) + title + description + chevron
- Touch feedback (scale + background change on press)

### Relationship card
- Burgundy gradient background
- White/on-accent text
- Title (names or "days together") + subtitle (anniversary countdown)

### More menu
- Full-width items with icon + label + chevron
- Bottom border between items (no border on last)

## Testing

Phase 6 adds 19 tests in `tests/app-shell.test.ts` covering:

- All main app route definitions (home, us, games, notes, more)
- All sub-routes (us/*, games/*, notes/*, more/*)
- Onboarding route preservation from Phase 5
- ONBOARDING_STEPS order preservation
- ROUTE_DEFAULTS correctness
- App routes follow `/app/*` pattern
- Sub-routes follow parent path convention
- Total route counts
- Navigation architecture (5 tabs, unique paths, no collisions)
- Feature placeholder sub-route availability

All 181 tests pass (162 existing + 19 new Phase 6).

## New files

```
src/features/app-shell/
  index.ts                         — barrel exports
  AppShell.tsx                     — main layout with Outlet + BottomNav
  BottomNav.tsx                    — 5-tab bottom navigation
  screens/
    HomeScreen.tsx                 — home dashboard
    UsScreen.tsx                   — relationship hub
    GamesHubScreen.tsx             — games hub
    NotesHubScreen.tsx             — notes hub
    MoreScreen.tsx                 — more menu
    PlaceholderScreen.tsx          — reusable future-feature placeholder

tests/
  app-shell.test.ts                — 19 Phase 6 tests

docs/
  app-shell.md                     — this document
```

## Modified files

```
src/navigation/routes.ts           — added 20 app routes
src/navigation/AppRouter.tsx       — full routing tree with AppShell
src/components/Icon.tsx            — added Heart, Gamepad, FileText, Settings
src/components/index.ts            — barrel export updates
src/components/primitives.css      — app shell, bottom nav, cards, more, placeholder CSS
src/theme/components.ts            — app shell class names
```

## Limitations

- **Android native behavior not physically verified**: Java/Android SDK not
  available in the sandbox. Web build and cap sync pass.
- **Feature screens are placeholders**: Memories, timeline, reminders,
  games, notes, settings, search, vault, and about show placeholder
  content. Their full implementations belong to later phases.
- **No deep linking**: The router uses `createBrowserRouter` which does
  not persist navigation state across cold starts; the OnboardingGate
  handles routing on each launch.
- **Bottom nav height**: Uses the CSS token `--th-bottom-nav-height` (64px).
  On devices with larger navigation bars, the safe-area inset handles
  the extra space.
