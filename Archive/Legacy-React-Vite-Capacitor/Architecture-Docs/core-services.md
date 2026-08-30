# TwoHearts — Core Services & Device Foundation (Phase 3)

Phase 3 adds the cross-feature core services that sit between the Phase 1
core primitives and Phase 2 persistence. Everything here is infrastructure;
no feature logic and no screens exist in this phase.

Layering (unchanged, `§11`/`§45`):

```
UI (components/features — Phase 6+)
 ↓
State (core/appSettings, core/uiState)
 ↓
Repositories (data/repositories)
 ↓
Data access (data/database, data/media, data/settings)
 ↓
Core services (src/services/** — THIS PHASE)
 ↓
Capacitor plugins / OS
```

Rule: React components never import plugins or raw storage — they talk to
stores/hooks; services own every plugin call site. Each plugin package is
imported in exactly one driver file.

## src/services layout

| Directory | Responsibility |
| --- | --- |
| `bootstrap/` | ordered app initialization pipeline + `coreServices` registry |
| `errors/` | `AppError` taxonomy, safe user messages, layer-agnostic normalization |
| `logging/` | leveled logger with privacy redaction |
| `validation/` | reusable pure validators (no UI/DB awareness) |
| `datetime/` | couples-domain date helpers (age, anniversaries, local-day math) |
| `device/` | device info + capability matrix with web fallbacks |
| `permissions/` | normalized runtime-permission state machine |
| `lifecycle/` | foreground/background/back-button event bus (single native owner) |
| `files/` | generic local file utilities (`files/` root, non-media) |
| `media/` | shared media knowledge: MIME policy, size limits, content sniffing |
| `search/` | query normalization + provider-based search engine + ranking |
| `security/` | secure-storage boundary + PIN hash + app-lock service |
| `notifications/` | local notification channels, scheduling, registry reconciliation |

## Bootstrap pipeline (`bootstrap/appBootstrap.ts`)

Ordered stages: `persistence → schema-verify → device-capabilities →
lifecycle → notifications → app-lock`. Critical stages (persistence,
schema-verify) abort startup into the user-safe retry screen rendered by
`AppGate` in `main.tsx`; non-critical stages log-and-continue (the app
works with notifications degraded, never bricks). The `coreServices`
registry exposes `device`, `notifications`, `appLock` after bootstrap.
Initialization is idempotent — re-running the pipeline is safe.

Schema-verify compares the `schema_migrations` ledger maximum against
`PERSISTENCE_CONFIG.schemaVersion` so the app can never run against a
half-migrated database.

## Errors (`errors/appError.ts`)

`AppError` = `{ category, code, recoverable, userMessage?, cause? }`.
Categories: `persistence`, `media`, `filesystem`, `security`,
`notification`, `permission`, `device`, `validation`, `network`,
`serialization`. `safeUserMessage` returns a calm fixed message per
category — raw stack/SQL never leaves the service layer.
`normalizeAppError` translates Phase 2 `PersistenceError` and unknown
throws into `AppError` with layer-specific codes. `reportError` goes to the
redacted logger.

## Logging (`logging/logger.ts`)

Levels `debug/info/warn/error`, scoped, `SetLogTransport` injectable.
Payload keys matching pin/password/secret/token/body/content/vault/media
(see `redact`) are replaced with `[redacted]` before transport — nested
objects and `Uint8Array` included. The logger contract forbids logging
domain data (notes text, media bytes, lock material); tests pin the
redaction list.

## Validation (`validation/validators.ts`)

Pure functions returning `{ ok, errors }` — no exceptions for expected
invalid input, no UI strings. `validate(...)` composes rules. Validators:
required, string/number range, ISO date (real calendar date check), UUID,
regex, enum, non-empty array, boolean, MIME allow-list, file size,
`normalizeInput` (trim + collapse whitespace). Feature phases compose
these; domain errors are thrown at service boundaries as `AppError`, not
returned mid-form.

## Date & time (`datetime/datetime.ts`)

`Date` objects only, local-device wall clock for user-facing moments
(anniversaries, ages); ISO UTC stays the persistence convention from
Phase 2. No date library is added. Conventions:

- leap-anniversary rule: Feb 29 → Feb 28 in non-leap years
  (`anniversaryInYear`, `addYearsPreservingDay` clamp),
- `addMonthsPreservingDay` clamps overflow (Jan 31 + 1mo → Feb 28/29),
- `diffLocalDays` computes calendar days between local midnights —
  DST/offset-safe, never `millis/86400000`,
- `relationshipAgeDays`, `decomposeCalendarAge`, `daysUntilAnniversary`,
  `toLocalDateKey` (yyyy-mm-dd), `startOfLocalDay`, `durationHuman`,
- `parseDate(value, strict)` never returns an invalid Date (no NaN leak).

## Device capabilities (`device/deviceCapabilities.ts`)

`DeviceCapabilities.initialize()` probes `@capacitor/device` once (cached)
and exposes `has(capability)` — the ONLY capability-check call site. The
matrix: localStorage/settings → always; SQLite → always; filesystem/media,
secureStorage, notifications, haptics, camera, photoGallery → native
Android only (web fallbacks document exactly which degrade). Probe failure
degrades to conservative defaults instead of blocking startup.

## Permissions (`permissions/permissionService.ts`)

Normalized `PermissionState = granted | denied | prompt | unavailable`.
Each plugin capability is a registered `PermissionProvider`
(check/request). `check` never prompts; `ensure` requests only from
`prompt`. Unregistered/broken providers answer `unavailable` — permission
failures never throw into feature code. Notifications are the only V1
capability actually requested by Phase 3; camera/photos/gallery providers
exist for future feature phases. Android private app storage requires no
runtime permission (documented).

## Lifecycle (`lifecycle/appLifecycleService.ts`)

Single native owner of `appStateChange`: one `AppLifecycleService`
singleton (`appLifecycle`) emits `foreground | background | backButton`
to any number of subscribers; subscriber exceptions are isolated. On web
it falls back to `visibilitychange` + `keyup(Escape)` so behavior matches
without a device. `core/useAppLifecycle` now delegates to this bus and
adds the UI concerns (dismiss global error, reconcile notifications on
foreground). App-lock auto-relock is a bus subscriber — re-lock logic is
not duplicated per subscriber.

## Files (`files/fileService.ts` + `fileAdapters.ts`)

Generic local file utilities for NON-media app files under `files/` in the
private app data directory. `FileService` (write/read/exists/delete-if-
exists/move/copy/list/sweep, `safeFileName` name scrubbing) over the same
`FileAdapter` discipline as media: `capacitorFileAdapter` (production) and
`MemoryFileAdapter` (tests/web). Path safety (`assertSafeRelativePath`) is
shared with Phase 2's media layer — no raw paths, no traversal, private
scope only.

## Media utilities (`media/mediaUtils.ts`)

Builds on Phase 2 (metadata model + `MediaStorage` own storage). Adds
shared policy: V1 size limits (photo 25 MB, video 500 MB), kind↔MIME
mapping helpers, `validateMediaCandidate` intake check, and magic-byte
content sniffing (`sniffMimeType`, `verifyMediaBytes`) as a spoofing guard
before user-picked files are accepted. No Memories/Vault feature code.

## Search (`search/normalize.ts` + `searchEngine.ts`)

One engine, one normalization, per-feature providers — NO duplicate index
or copied dataset: providers query the Phase 2 repositories directly
(SQLite LIKE scans are trivially fast at couples-app scale). Normalization
is NFKD → strip combining diacritics → lowercase → collapse whitespace →
tokenize (max 200 chars). Every token must match somewhere (AND
semantics); scoring ranks exact > prefix > word-initial > substring >
body, ties by recency desc then id asc — deterministic. Providers are
registered by feature phases; provider failures are isolated. Empty
queries short-circuit to empty results.

## Security & app lock (`security/*`)

- `secureStore.ts` — `SecureStore` boundary (get/set/remove/keys) for
  SMALL SECURITY-SCOPED STRINGS ONLY: the app-lock salt + verifier.
  Ordinary domain data never goes here (documented boundary: settings →
  localStorage, domain data → SQLite, media → filesystem, secrets →
  SecureStore).
- `capacitorSecureStore.ts` — the only `@aparajita/capacitor-secure-storage`
  import site; on Android this is EncryptedSharedPreferences backed by the
  Android Keystore. Version 6.0.1 chosen for Capacitor 6 compatibility.
- `pinHash.ts` — PBKDF2-HMAC-SHA-256, 120k iterations, 128-bit random salt,
  base64 storage, constant-time comparison. WebCrypto is available in the
  Android WebView and in Node 19+, so production and tests share one path.
- `appLockService.ts` — foundation only (no lock UI): enable/unlock/lock/
  disable, state `disabled | unlocked | locked`, listeners for a future
  lock gate. Lock state is MEMORY-ONLY: cold start with lock enabled always
  opens locked (no persisted "unlocked" flag). Re-locks on foreground when
  background time exceeds `lockTimeoutSeconds` (default 60) via the
  lifecycle bus. Settings hold ONLY non-sensitive config
  (`appLockEnabled`, `lockTimeoutSeconds`); PIN material never enters
  settings, UI state, or logs.

## Notifications (`notifications/*`)

Local-only (`@capacitor/local-notifications` — no push/FCM anywhere in
V1). Architecture: `NotificationService` → `NotificationDriver`
(`capacitorNotificationDriver` native/web, `MemoryNotificationDriver`
tests). The plugin keys notifications by INTEGER id while domain identity
is UUID + owner-ref; the `notification_registry` table (migration 002,
schema v2) bridges them:

- `owner_ref` UNIQUE — one pending notification per logical event,
  re-scheduling updates instead of duplicating,
- scheduling metadata (`meta_json`) travels with the row,
- `reconcile()` prunes registry rows the OS no longer has pending (called
  on foreground from `useAppLifecycle`),
- channels (`reminders`, `anniversaries`, `general`) are created once at
  bootstrap; Android channel properties are immutable after creation,
- `exact` scheduling is opt-in (API 31+ exact-alarm permission); default is
  inexact. Manifest adds `POST_NOTIFICATIONS` and `SCHEDULE_EXACT_ALARM`.

Cancel APIs: by id, by owner, all. Scheduling NEVER blocks startup —
notification failure is logged-and-degraded.

## Boundary checklists honored

- React components import no plugins, no DB, no localStorage.
- Services import no React.
- Each Capacitor plugin is imported in exactly one driver file.
- No feature logic, no screens, no fake flows, no network calls.
- Ordinary local data is not encrypted-by-theft-of-abstraction; only the
  lock verifier uses Keystore-backed storage.

## Not verified in this environment

Native plugin behavior (secure storage on-device, notification delivery,
device info, lifecycle transitions) is exercised through test doubles in
Node; on-device verification of scheduling, re-lock-on-foreground, and
permission prompts requires a physical device — NOT VERIFIED here. The
APK builds with all plugins synced (see Phase 3 report).
