# Settings & App Management (Phase 19)

Phase 19 implements the full Settings experience (roadmap screens 80–86) and
connects every preference to real, persisted application state. Nothing in
Settings is a visual placeholder: every toggle and field maps to an existing
service, and no setting exists without a shipped capability behind it.

## Screens

| Screen | Route | Connects to |
| --- | --- | --- |
| Settings Home | `/app/more/settings` | Navigation hub; first-launch stamp |
| Profile Settings | `/app/more/settings/profile` | `RelationshipService.saveOwner` |
| Relationship Settings | `/app/more/settings/relationship` | `RelationshipService.savePartner` / `setStartDate` |
| Appearance | `/app/more/settings/appearance` | `appSettingsStore` themeMode / textSize / reduceMotion |
| Notifications | `/app/more/settings/notifications` | `appSettingsStore` + `PermissionService` (diagnostics only) |
| Security | `/app/more/settings/security` | `AppLockService` + app-lock flags in settings |
| Storage | `/app/more/settings/storage` | `DataManagementService` |
| About | `/app/more/about` | `config/appInfo.ts` (derived from `capacitor.config.ts`) |

The More tab already links into these routes; About was added there in this
phase.

## Preferences (settings schema v3)

All preferences continue to live in `localStorage` behind
`data/settings/settingsStorage.ts` — readable before DB init, tiny, flat,
never containing domain data. Schema v3 adds:

- `notificationsEnabled` (default true) — master switch for ALL local
  notification scheduling.
- `remindersEnabled` (default true) — reminders-category switch.
- `reduceMotion` (default false) — applies `data-th-reduce-motion="true"` on
  the document root; CSS suppresses transitions/animations.

v1 → v2 and v2 → v3 migrations are pure, idempotent, and tested
(`tests/settingsMigration.test.ts`, `tests/appStateAndPreferences.test.ts`).
`setTextSize` / `setThemeMode` now reject invalid values at the store
boundary instead of persisting them.

## Appearance application

`AppRootProvider` applies `textSize`, `themeMode`, and `reduceMotion` on every
settings change, so navigation, cold start, and live toggles all converge.
Dark theme consumes the Phase 18 dark token block; reduce-motion suppresses
all transitions/animations via one token rule in `tokens.css`.

## Notifications gating

`ReminderService.scheduleNotification` (and therefore the
`ReminderNotificationDriver` used by the Phase 18 anniversaries/dates worker)
checks `notificationsEnabled && remindersEnabled` before scheduling, and drops
any stale schedule when gated. The toggles affect real scheduling behavior —
verified by tests on the real sql.js path with `MemoryNotificationDriver`.

## App Lock

Security Settings drives the existing `AppLockService` — the security
authority is unchanged. Enable/disable flows call `enable(pin)` / `disable()`;
the PIN input accepts exactly 4 digits and never logs or persists the PIN
(PBKDF2 material stays in SecureStore). `AppLockGate` (mounted around
`AppRouter`) overlays the whole app whenever the lock state is `locked`,
covering both cold start and relock-on-foreground, while the Vault entry
continues to use its own `VaultPinGate`.

## DataManagementService (`services/maintenance/dataManagementService.ts`)

One owner for storage information and destructive maintenance, registered as
a non-critical bootstrap stage (`coreServices.dataManagement`).

- `getStorageReport()` — per-table row counts (system tables excluded),
  media asset bytes from the `media_assets` ledger, pending scheduled
  notifications.
- `clearCache()` — sweeps orphan media files (files without metadata rows);
  never touches rows or user data.
- `resetAllLocalData()` — destructive full reset:
  1. deletes all rows from every non-system table in one transaction,
  2. sweeps now-orphaned media files after commit,
  3. cancels all pending local notifications,
  4. removes PIN material from SecureStore (lock state → `disabled`),
  5. resets settings to defaults (schema v3, `firstLaunchAt` preserved).

  The schema ledger (`schema_migrations`, `schema_version`) is never touched,
  so the app cannot fall into a re-migration state. After reset the user sees
  onboarding again on next navigation/restart. Errors normalize to
  `persistence`/`data-reset-failed` via `normalizeAppError` — raw SQL,
  table names, and paths never reach the UI.

The Storage screen requires explicit confirmation (typed `DELETE`) before a
full reset and lists exactly what is erased.

## Hard rules honored

- No fake settings: every control maps to a shipped capability. Calendar
  reminders, birthday notifications, and push remain V2 (not shown).
- PIN material never leaves SecureStore; settings carry only non-sensitive
  flags (`appLockEnabled`, `lockTimeoutSeconds`).
- No cloud, Firebase, sync, or upload code introduced; all V1 notification
  behavior stays local.
- Domain data stays in SQLite; settings stay in localStorage — no second
  source of truth.

## Tests

`tests/phase19-settings-app-management.test.ts` (10 tests): schema v3
defaults/persistence/validation, appearance application helper, reminder
scheduling gated by both notification switches, storage report aggregation,
cache clear preserving saved data, full reset (domain wipe, media sweep,
notification cancel, PIN removal, settings reset, schema-ledger preservation,
`firstLaunchAt` preservation), error normalization, app-lock enable/disable
boundary, and lock-state listener bus.
