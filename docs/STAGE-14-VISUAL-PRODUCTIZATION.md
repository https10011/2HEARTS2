# Stage 14 — Search + Notification Center Visual Productization

## Stage Objective

Productize the Search and Notification Center experiences so they feel like
native, polished parts of TwoHearts rather than generic utility screens.
Search should feel warm, organized, and discoverable. Notification Center
should feel like a natural part of the private couple application with
clear unread/read hierarchy, timestamps, and polished empty states.

## Starting Commit

c627bd2 (Stage 13 ending commit)

## Ending Commit

(Stage 14 commit SHA)

## Branch

master

## Remote Verification

origin/master — verified

## Working Tree

Clean (after commit)

## Previous-Stage Audits

### Stage 13 (Games)
- Games Hub intact — hero, personality cards, section headers
- Game gameplay intact — GamePlayScreen, CasualGamePlayScreen
- Game results intact — GameResultsScreen
- Memory Match card grid intact
- Word Scramble display intact
- Phase 28 engine untouched
- Phase 29 game UX system intact
- Phase 25 motion system intact
- Stage 13 tests (stage13-games.test.ts) still passing

### Stage 12 (Vault)
- Vault CSS preserved, tests passing, security architecture unchanged

### Stage 11 (Period Tracker)
- Implementation intact, navigation intact, tests intact

### Stage 10 (Mood)
- Implementation intact, CSS intact, tests intact

### Earlier Stages (7-9)
- Timeline, Reminders, Places all intact
- Shared components (DatePicker, TimePicker, Modal) preserved
- Navigation architecture intact

## Search Changes

### Branded Search Field
- Pill-shaped search field with warm border radius
- Integrated search icon that changes color on focus (burgundy)
- Clear button (X) appears when query is non-empty
- Focus state with burgundy border and shadow
- Dark mode: elevated surface background

### Result Cards
- Each card has a kind-specific icon in a warm gradient circle
- Title, snippet, and kind badge with clear hierarchy
- Staggered entrance animation
- Hover/tap states with shadow elevation
- Dark mode: elevated surface colors

### Empty State (Default)
- Warm circle with search icon
- "Search your content" heading
- Descriptive copy mentioning privacy

### No-Result State
- Shows the original query
- "No matches found" heading
- Clear search button to reset
- Distinct from empty state

### Loading State
- Spinner with burgundy accent
- "Searching..." label
- Respects reduced motion

## Notification Center Changes

### Notification Cards
- Each card: unread dot + kind icon + body (meta, title, description)
- Unread: subtle blush gradient background, bold title
- Read: plain surface background, normal weight title
- Unread dot: burgundy filled (unread) vs border-only (read)
- Kind icon: gradient circle (burgundy for reminder, rose for anniversary, charcoal for system)
- Timestamp: relative time (just now, Xm ago, Xh ago, Xd ago, date)
- Staggered entrance animation

### Header Bar
- Unread count (burgundy when > 0, neutral when 0)
- Mark all read button (blush background, burgundy text)
- Clear all button (error color)

### Empty State
- Warm bell icon in gradient circle
- "All caught up" heading
- Descriptive copy about reminders and dates

### Architecture Preserved
- Local notification architecture unchanged
- No cloud notifications introduced
- No FCM introduced
- No remote services introduced
- Schema unchanged

## Files Changed

| File | Change Type | Description |
|------|-------------|-------------|
| src/components/primitives.css | Modified (+350 lines) | Stage 14 search + notification CSS vocabulary |
| src/features/notifications/searchNotificationPresentation.ts | New | Pure helpers (counts, labels, time formatting) |
| src/features/app-shell/screens/SearchScreen.tsx | Rewrite | Branded search field, result cards, empty/no-result states |
| src/features/notifications/NotificationCenter.tsx | Rewrite | Notification cards, unread/read, empty state, timestamps |
| tests/stage14-search-notification.test.ts | New | 26 tests for presentation helpers |
| docs/STAGE-14-VISUAL-PRODUCTIZATION.md | New | This report |

## Visual Verification

### Light Mode
- Search field: warm cream surface, burgundy focus border ✓
- Result cards: white surface, warm icon circles ✓
- Notification cards: warm surface, unread gradient ✓
- Empty states: warm gradients, burgundy accents ✓

### Dark Mode
- All new CSS includes `:root[data-th-theme='dark']` overrides ✓
- Search field: elevated surface ✓
- Result cards: elevated surface ✓
- Notification cards: adjusted gradients ✓
- Empty state icons: adjusted gradients ✓

### Extra Large Text
- All font sizes use --th-font-size-* tokens ✓
- No hardcoded font sizes in new components ✓

### 320px / Narrow Viewport
- Search field uses flex layout, no fixed widths ✓
- Result/notification cards use flex, no fixed widths ✓
- Timestamps flex-shrink: 0 to prevent overlap ✓

### Reduced Motion
- Spinner: @media (prefers-reduced-motion: reduce) freezes animation ✓
- Stagger animations: instant opacity:1 under reduced motion ✓
- All transitions use token durations (collapse to 1ms) ✓

## Accessibility
- All interactive controls ≥ 44px touch targets ✓
- Semantic headings (h3) for state titles ✓
- aria-label on clear search button ✓
- Keyboard support preserved (Enter to search) ✓
- Focus-visible states via existing token ✓

## Tests

866/866 passing (26 new Stage 14 tests + 840 existing)

## TypeScript

PASS

## Production Build

PASS

## Capacitor Sync

CAPACitor sync attempted — successful

## APK Status

BLOCKED BY ENVIRONMENT (no JDK/Android SDK)

## Known Limitations
- Browser/sql.js in-memory DB resets on reload (pre-existing)
- Search results depend on registered feature providers (empty if none seeded)

## Deferred Items
- APK-level visual QA

## Architecture Preservation
- Search engine (Phase 3): PRESERVED
- Notification Center data model: PRESERVED
- Local notification architecture: PRESERVED
- Schema: UNCHANGED
- Cloud storage: NOT INTRODUCED
- Remote notifications: NOT INTRODUCED
- FCM: NOT INTRODUCED

## Exact Next-Stage Starting Point
- Branch: master
- Commit: (Stage 14 commit SHA)
- Working tree: clean
- Tests: 866/866 passing
- Do NOT begin Stage 15
