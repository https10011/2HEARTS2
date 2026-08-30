# Stage 15 — Settings + App Customization Visual Productization

## Stage Objective

Productize the Settings and App Customization experiences so they feel like
polished, premium, cohesive TwoHearts application screens rather than generic
utility pages. Settings should communicate privacy, calm, trust, and local
ownership while remaining architecturally unchanged.

## Starting Commit

0c29014 (Stage 14 ending commit)

## Ending Commit

(Stage 15 commit SHA)

## Branch

master

## Remote Verification

origin/master — verified at 0c29014

## Working Tree

Contains Stage 15 changes (pre-existing partial work + completed implementation)

## Previous-Stage Audits

### Stage 14 (Search + Notification Center)
- Search screen intact — branded search field, result cards, empty states
- Notification Center intact — unread/read cards, timestamps, empty state
- Stage 14 tests (stage14-search-notification.test.ts) passing

### Stage 13 (Games)
- Games Hub intact
- Game engine, progression, scoring untouched
- Stage 13 tests passing

### Stage 12 (Vault)
- Vault CSS preserved, security architecture unchanged

### Stage 11 (Period Tracker)
- Implementation intact, navigation intact

### Earlier Stages (2-10)
- All previous visual productization stages verified intact

## Changes

### 1. Settings Home Screen
- Branded burgundy hero band with "TwoHearts" label and description
- Profile card with avatar initial, name, and chevron
- Enhanced section headers with burgundy dot indicators
- Enhanced grouped rows with surface cards and shadows
- Improved privacy info card with icon and gradient background
- Staggered entrance animations for sections

### 2. Appearance Settings Screen
- Theme selector: visual preview cards (Light/Dark/System) with color swatches
- Active theme gets burgundy border + check mark
- Text size selector: radio card rows with descriptions and check marks
- Motion preference: enhanced switch row with icon
- Reset to default: enhanced group card

### 3. Notification Settings Screen
- Enhanced grouped rows with icons
- Permission status badges (Allowed/Blocked/Not yet requested/Not available)
- Enhanced privacy info card with bell icon
- Device notification status with icon row

### 4. Security & App Lock Settings Screen
- Lock icon replacing generic heart icon
- Enhanced groups with proper Lock icon
- Status badge for active PIN state
- Auto-lock section with enhanced headers
- Privacy notes with lock icons

### 5. Profile Settings Screen
- Section header with dot indicator
- Enhanced group card wrapping form fields
- Improved info cards with file icon

### 6. Relationship Settings Screen
- Section headers for Special Someone / Relationship
- Enhanced groups wrapping form fields
- Important Dates link row with calendar icon
- Heart icon on privacy info card

### 7. Storage Settings Screen
- Enhanced storage summary card with icon
- Breakdown list in enhanced group
- Local Data section with file icons
- Danger Zone section
- Enhanced info card

### 8. About Screen
- Brand logo retained (Phase 23)
- Feature list uses enhanced row style with heart icons
- Enhanced privacy info card
- Footer class for version/credit line

### 9. Settings UI Components (settingsUi.tsx)
- SettingRow: enhanced row style (th-settings-row--stage15) with icon circles
- SettingSwitchRow: enhanced row style
- InfoCard: optional icon prop, enhanced gradient background

### 10. Settings Presentation Helpers (settingsPresentation.ts)
- Pure-function helpers for theme labels, text size descriptions
- Motion labels, permission status labels
- Lock status, section metadata, info card messages
- Zero DOM dependencies — fully testable

### 11. Settings CSS Vocabulary (primitives.css +409 lines)
- Hero band with burgundy gradient and subtle light overlay
- Profile card with avatar, name, chevron
- Section headers with dot indicator
- Enhanced groups with surface and shadow
- Theme preview cards grid (3-column)
- Text size selector cards
- Enhanced info cards with icon slot and gradient
- Status badges (granted/denied/prompt/unavailable)
- Settings footer
- Switch rows enhanced
- Dark mode overrides for all
- Reduced motion awareness

## Files Changed

| File | Change Type | Description |
|------|-------------|-------------|
| src/components/primitives.css | Modified (+409 lines) | Stage 15 settings CSS vocabulary |
| src/features/settings/settingsPresentation.ts | New | Pure presentation helpers |
| src/features/settings/settingsUi.tsx | Modified | Enhanced SettingRow, SettingSwitchRow, InfoCard |
| src/features/settings/SettingsHomeScreen.tsx | Modified | Branded hero, profile card, enhanced sections |
| src/features/settings/AppearanceSettingsScreen.tsx | Modified | Theme preview cards, text size selector |
| src/features/settings/NotificationSettingsScreen.tsx | Modified | Enhanced groups, status badges |
| src/features/settings/SecuritySettingsScreen.tsx | Modified | Lock icon, enhanced groups, status badges |
| src/features/settings/ProfileSettingsScreen.tsx | Modified | Enhanced sections and info cards |
| src/features/settings/RelationshipSettingsScreen.tsx | Modified | Enhanced sections and info cards |
| src/features/settings/StorageSettingsScreen.tsx | Modified | Enhanced summary, sections, info cards |
| src/features/settings/AboutScreen.tsx | Modified | Enhanced feature list and footer |
| tests/stage15-settings.test.ts | New | 27 tests for settingsPresentation helpers |
| docs/STAGE-15-VISUAL-PRODUCTIZATION.md | New | This report |

## Visual Verification

### Light Mode
- Hero band: burgundy gradient with warm light overlay ✓
- Profile card: white surface, burgundy avatar gradient ✓
- Section headers: rose text with burgundy dot ✓
- Groups: white surface with shadow ✓
- Theme cards: visual previews with burgundy selection ring ✓
- Status badges: green/red/neutral backgrounds ✓

### Dark Mode
- Hero band: dark burgundy with reduced opacity ✓
- Profile card: elevated surface ✓
- Groups: elevated surface ✓
- Theme cards: adjusted for dark surface ✓
- Status badges: dark-friendly backgrounds ✓

### Extra Large Text
- All font sizes use --th-font-size-* tokens ✓
- No hardcoded font sizes in new CSS ✓

### 320px / Narrow Viewport
- Theme grid uses 3-column with gap, no fixed widths ✓
- All rows use flex, no fixed widths ✓

### Reduced Motion
- Stagger animations collapse to instant under reduced motion ✓
- All transitions use token durations ✓

## Architecture Preservation

- AppSettings store (useSyncExternalStore): PRESERVED
- Settings schema v3: UNCHANGED
- AppLockService + SecureStore: PRESERVED
- Local-first architecture: PRESERVED
- Cloud services: NOT INTRODUCED
- Remote notifications: NOT INTRODUCED
- Schema: UNCHANGED
- Navigation routes: PRESERVED

## Tests

893/893 passing (27 new Stage 15 tests + 866 existing)

## TypeScript

PASS

## Production Build

PASS

## Capacitor Sync

CAPACitor sync — no Android environment available for full verification

## APK Status

BLOCKED BY ENVIRONMENT (no JDK/Android SDK)

## Known Limitations
- Browser/sql.js in-memory DB resets on reload (pre-existing)
- APK-level visual QA unavailable in this environment

## Deferred Items
- APK-level visual verification

## Exact Next-Stage Starting Point
- Branch: master
- Working tree: clean (after commit)
- Tests: 893/893 passing
- Do NOT begin Stage 16
