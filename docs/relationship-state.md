# TwoHearts — Relationship & Application State Foundation (Phase 4)

Phase 4 adds the local identity and setup foundation the rest of the app
depends on: profiles, the couple relationship, important dates, preferences,
first-launch/onboarding state. No screens, no onboarding UI — this is the
data + service layer that those future phases consume.

## Domain models (`src/data/relationship/relationshipTypes.ts`)

Three entities, all following the Phase 2 conventions (UUID v4 ids,
UTC ISO 8601 `created_at`/`updated_at`, explicit serializers, tombstones
where deletes must be sync-propagatable):

- **Profile** — one row per person, `role TEXT CHECK (role IN
  ('owner','partner'))`, SQL-enforced at most one LIVE profile per role
  (partial unique index `WHERE deleted_at IS NULL` — re-pairing is
  possible; a tombstoned profile doesn't block a new one).
  Birthday is a LOCAL calendar key, see below.
- **CoupleRelationship** — SQL-enforced singleton
  (`singleton INTEGER NOT NULL DEFAULT 1 UNIQUE CHECK (singleton = 1)`).
  Owner/partner references are nullable so onboarding configures each side
  independently; FK `ON DELETE SET NULL` so removing a profile never
  orphans the couple row. Holds the relationship start date.
- **ImportantDate** — relationship/personal dates (anniversaries,
  birthdays, milestones, first trip, …). `recurrence` ('none' | 'yearly')
  exists NOW so Phase-reminder/notification features schedule without
  remodeling; `profile_id` nullable (null = relationship-level date).

### Date conventions (utils/time.ts)

Two formats, never mixed:
- `yyyy-mm-dd` local calendar keys for relationship dates (birthdays,
  start date, anniversaries) — a couples app celebrates THE DAY, not an
  instant; validated as REAL calendar dates by `isValidDateKey`.
- UTC ISO 8601 timestamps for entity metadata (`createdAt`/`updatedAt`) —
  Phase 2 convention kept everywhere.

## Repositories (`src/repositories/`)

- `ProfileRepository extends BaseRepository` — standard CRUD +
  `getByRole/getOwner/getPartner`.
- `CoupleRepository` — DELIBERATELY not a BaseRepository: CRUD collapses
  to `get()/save()` on the singleton; `save()` preserves `id`/`createdAt`
  and refreshes `updatedAt`.
- `ImportantDateRepository extends BaseRepository` — CRUD +
  `listForProfile(id|null)` + `listRecurring()` (the reminder feature's
  input set).

## Services

- `src/services/relationship/relationshipService.ts` — application-facing
  boundary over the three repositories. `saveOwner/savePartner` run the
  profile write + couple-singleton link inside ONE outer transaction via
  the adapter (never a raw SQL string caller). Input validation uses the
  shared Phase 3 validators; failures are typed `AppError('validation',
  'invalid-input')`, never silent writes. `getSummary()` computes age
  days / next anniversary / countdown via the Phase 3 datetime helpers
  (including the Feb 29 → Feb 28 leap rule, verified to the day).
- `src/services/state/appStateService.ts` — the setup/first-launch truth:
  `markFirstLaunchIfNeeded()` (stamps once, survives `reset()`),
  `reconcileOnboardingStage()` (derives the persisted stage from DOMAIN
  TRUTH: missing owner → 'owner', missing partner/start-date →
  'relationship', else 'personalization'; 'complete' never downgrades),
  `completeSetup()` (refuses while the domain is incomplete),
  `getSnapshot()` (read model for future routing/UI).

## Preferences (`src/core/appSettings.ts`, settings schema v2)

Settings stay OUT of the domain database (they must be readable before DB
init; tiny/flat). Schema v2 adds:
- `firstLaunchAt: string | null` — stamped once at bootstrap, never reset,
- `onboardingStage: 'fresh' | 'owner' | 'relationship' | 'personalization'
  | 'complete'` — persisted so a killed app resumes setup,
- `themeMode: 'light' | 'dark' | 'system'` — persisted NOW so the future
  settings UI ships with working persistence; applying dark tokens later
  is purely a CSS switch. `applyThemeMode()` sets `data-th-theme` on the
  root; wired alongside `applyTextSize` in `AppRootProvider`/`App.tsx`.

Migration v1→v2 is explicit and tested in isolation
(`tests/settingsMigration.test.ts` uses a dynamic import so the first
module load sees the v1 payload): existing values preserved, new keys
defaulted, `onboarded: true` → `onboardingStage: 'complete'`.

App-lock settings remain NON-sensitive config only
(`appLockEnabled`/`lockTimeoutSeconds`) — the verifier/salt live in the
SecureStore (Phase 3); the integration test pins that JSON contract.

## Bootstrap (`src/services/bootstrap/appBootstrap.ts`)

New non-critical stage `application-state` runs after `app-lock`: marks
first launch, reconciles the onboarding stage from domain truth, and
registers `coreServices.appState` + `coreServices.relationship`. Failure
of this stage degrades, it never bricks startup — the same rule as every
other non-critical stage.

## Migration (`src/data/database/migrations/003_relationship_foundation.ts`)

Schema version 3 adds `profiles`, `couple_relationship`,
`important_dates` (+ indexes). Migration is deterministic and idempotent;
the existing-data path is tested (`tests/migrations.test.ts`: a v2
install with real `notification_registry` rows upgrades to v3 preserving
all rows). Old migrations were not touched.

## What was intentionally NOT built

Onboarding screens, settings UI, home/greeting header, reminder
scheduling, and the 77-screen reference UI remain out of scope. The
relationship summary math exists for those future screens to consume;
none of it is rendered yet.
