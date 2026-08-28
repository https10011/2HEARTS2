# Stage 16 — Dialogs / Modals / System States Audit

## Stage Objective

Cross-screen consistency pass to stop TwoHearts from feeling like different
developers built different parts of the application. Standardize system states
(dialogs, confirmations, error/success banners, destructive actions) while
preserving meaningful feature personality.

## Starting Commit

12b6823 (Stage 15 — Settings + App Customization)

## Ending Commit

(Stage 16 commit SHA)

## Branch

master

## Remote Verification

origin/master — verified at 12b6823

## Working Tree

Clean (after commit)

## Previous-Stage Audits

### Stage 15 (Settings)
- All 8 settings screens intact with enhanced styling
- Theme preview cards, text size selector preserved
- Settings presentation helpers intact

### Stage 14 (Search + Notification Center)
- Search and Notification Center intact

### Stage 13 (Games)
- Game engine, progression, scoring untouched
- Game screens intact

### Stage 12 (Vault)
- Vault security architecture preserved
- Vault content viewer updated with ConfirmDialog

### Stage 11 (Period Tracker)
- Implementation intact

### Earlier Stages (2-10)
- All features verified intact

## Inconsistencies Discovered

### 1. Two Button Systems
- `th-button` (React component) — used by most screens
- `th-btn` (raw CSS classes) — used in some features (Reminders, Vault)
- **No danger variant** in the Button React component, but `th-btn--danger` existed in raw CSS

### 2. Delete Confirmation Patterns
Every feature screen implemented its own delete confirmation differently:
- **Memories**: Feature-specific CSS classes (`th-memory-confirm-title/copy`), `variant="primary"` for delete
- **Notes**: Feature-specific CSS classes (`th-note-confirm-title/copy`), `variant="primary"` for delete
- **Timeline**: Reused `th-note-confirm-*` classes, `variant="primary"` for delete
- **Reminders**: Raw `<button>` with `th-btn th-btn--danger`, reused `th-date-picker__title` class (!)
- **Places**: Reused `th-note-confirm-*` classes, `variant="primary"` for delete
- **Vault**: Raw `<button>` with `th-btn th-btn--danger`, inline centered layout
- **Mood**: Feature-specific `th-mood-actions` class, `variant="primary"` for delete
- **Settings**: Inline styles, `variant="primary"` for action

### 3. Button Ordering
- Feature screens: Action on top, Cancel below (vertical stack)
- Settings screens: Cancel on left, Action on right (horizontal)
- No consistent pattern

### 4. Destructive Action Styling
- Some used `variant="primary"` (burgundy) for delete — not visually indicating danger
- Others used raw `th-btn--danger` (red) — inconsistent with Button component API

### 5. Status/Error Messages
- `th-form-error` / `th-form-error--global` (onboarding, forms) — well-defined
- `th-mood-error` / `th-period-error` (feature-specific full-screen) — duplicated CSS
- Inline `style={{ color: 'var(--th-color-error)' }}` (memories, settings, timeline, vault)

## Changes Made

### 1. Button Component — Danger Variant
- Added `'danger'` to the `Variant` type
- Added `buttonDanger: 'th-button--danger'` to componentClassNames
- Added `.th-button--danger` CSS rule (error color, white text, shadow)

### 2. ConfirmDialog — Shared Confirmation Component
- New `src/components/ConfirmDialog.tsx`
- Standardized layout: title → description → action button (danger/primary) → cancel (ghost)
- Consistent button order: action first (most likely intent), cancel below
- Supports `busy`/`busyLabel` for in-progress states
- `actionVariant` prop for destructive (danger) vs non-destructive (primary) confirms
- CSS: `.th-confirm-dialog`, `__title`, `__description`, `__actions`
- Exported from components barrel

### 3. StatusBanner — Shared Inline Status Component
- New `src/components/StatusBanner.tsx`
- Three variants: error (alert), success (status), info (status)
- Each variant uses appropriate semantic role
- Icon + text layout with token-driven colors
- CSS: `.th-status-banner`, `--error`, `--success`, `--info`
- Dark mode overrides included
- Exported from components barrel

### 4. Cross-Screen Confirmation Standardization
All 10 delete/destructive confirmation modals migrated to ConfirmDialog:

| Screen | Before | After |
|--------|--------|-------|
| MemoryDetail | Inline Modal + feature CSS | ConfirmDialog |
| NoteDetail | Inline Modal + feature CSS | ConfirmDialog |
| EventDetail | Inline Modal + note CSS classes | ConfirmDialog |
| ReminderDetail | Raw th-btn--danger + picker CSS | ConfirmDialog |
| PlaceDetail | Inline Modal + note CSS classes | ConfirmDialog |
| VaultContentViewer | Raw th-btn--danger + outline | ConfirmDialog + Button danger |
| MoodEntry | Inline Modal + mood CSS | ConfirmDialog |
| SecuritySettingsScreen | Inline Modal + inline styles | ConfirmDialog |
| StorageSettingsScreen | Inline Modal + inline styles | ConfirmDialog (×2) |
| AppearanceSettingsScreen | Inline Modal + inline styles | ConfirmDialog |

### 5. VaultContentViewer — Button Consistency
- "Delete from Vault" trigger button: raw `<button class="th-btn th-btn--danger">` → `<Button variant="danger">`

## Components Reused

- `Modal` — ConfirmDialog wraps the existing Modal (no new overlay system)
- `Button` — ConfirmDialog uses the existing Button with the new danger variant
- Icon system — StatusBanner uses existing IconCheck/IconClose/IconInfo
- Toast system — untouched (existing toast feedback preserved)
- Design tokens — all new CSS uses `--th-*` tokens

## Components Centralized

- Delete confirmation dialog pattern → `ConfirmDialog`
- Inline error/success status → `StatusBanner`
- Destructive button styling → `Button variant="danger"`

## Visual Verification

### Rendered Inspection
- Dev server running at preview URL
- ConfirmDialog renders as bottom-sheet modal with consistent layout
- StatusBanner renders with appropriate color and icon for each variant
- Dark mode: all new CSS includes `data-th-theme='dark']` overrides

### Representative Screens Verified
- MemoryDetail delete confirmation
- NoteDetail delete confirmation
- Settings disable lock confirmation
- Settings clear cache / clear data confirmations
- Appearance reset confirmation

## Responsive / Accessibility Verification

### 320px Narrow Viewport
- ConfirmDialog uses flex column, no fixed widths ✓
- StatusBanner uses flex, wraps text naturally ✓

### Extra Large Text
- All font sizes use `--th-font-size-*` tokens ✓
- No hardcoded font sizes ✓

### Light Mode
- Error: error-bg background, error color ✓
- Success: success-bg background, success color ✓
- Info: neutral-soft background, text-secondary ✓
- Danger button: error color, text-on-accent ✓

### Dark Mode
- All new CSS includes dark mode overrides ✓
- Error: semi-transparent error color background ✓
- Success: semi-transparent success color background ✓
- Info: semi-transparent white background ✓

### Reduced Motion
- ConfirmDialog inherits Modal's reduced-motion behavior ✓
- No new decorative animations introduced ✓
- Transitions use token durations ✓

## Tests

922/922 passing (29 new Stage 16 tests + 893 existing)

New tests cover:
- Button danger variant (CSS, component, barrel export)
- ConfirmDialog (structure, props, CSS, button order, barrel export)
- StatusBanner (variants, roles, CSS, dark mode, barrel export)
- Cross-screen consistency (10 screens verified to use ConfirmDialog)
- Modal CSS standardization (border-radius, safe-area)

## TypeScript

PASS

## Production Build

PASS

## Capacitor Sync

BLOCKED BY ENVIRONMENT (no JDK/Android SDK)

## APK Status

BLOCKED BY ENVIRONMENT (no JDK/Android SDK)

## Known Limitations
- Browser/Vite used as visual fallback (no native Android rendering)
- `th-mood-error` and `th-period-error` CSS remain feature-specific (they're full-screen error states, not inline banners)
- Some raw `th-btn` usages remain in VaultContentViewer edit-mode buttons (non-destructive, acceptable)

## Architecture Preservation

- Modal infrastructure: PRESERVED (ConfirmDialog wraps existing Modal)
- Button component: EXTENDED (added danger variant, no breaking changes)
- Toast system: PRESERVED (no changes)
- Phase 25 motion system: PRESERVED (no new animation systems)
- Theme architecture: PRESERVED (all CSS uses existing tokens)
- Local-first architecture: PRESERVED
- Schema: UNCHANGED
- Services: UNCHANGED

## Deferred Items
- APK-level visual verification
- Full-screen error state consolidation (th-mood-error, th-period-error) — these are feature-personality elements, not system states

## Exact Next-Stage Starting Point
- Branch: master
- Working tree: clean (after commit)
- Tests: 922/922 passing
- Do NOT begin Stage 17
