# Stage 4 — Core Services Migration

**Status:** ✅ Complete  
**Date:** 2026-08-30  
**Preceded by:** Stage 3 (Data Layer Migration)  
**Followed by:** Stage 5 (Onboarding Flow Migration)

---

## Overview

Stage 4 migrated the complete core services layer from the legacy React/Vite/Capacitor implementation to native Android. The entire infrastructure layer between UI and data has been recreated with equivalent functionality and improved native Android patterns.

---

## What Was Completed

### Error System (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `AppError.kt` | Unified error type with categories, codes, safe messages | ✅ Complete |

**Features:**
- 8 error categories (validation, persistence, permission, not_found, security, media, network, unknown)
- Factory methods for each error type
- Safe user messages (no internal details)
- Original exception preservation for logging

### Logger (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `Logger.kt` | Leveled, scoped logger with redaction | ✅ Complete |

**Features:**
- 4 log levels (DEBUG, INFO, WARN, ERROR)
- Scoped logging (feature name prefix)
- Automatic redaction of sensitive keys (pin, password, secret, token, body, content, vault, media)
- Android Log integration

### Validators (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `Validator.kt` | Pure validation functions | ✅ Complete |

**Features:**
- 10 validation functions (required, length, date, email, pin, noteCategory, moodValue, flowLevel, recurrence, placeCategory)
- ValidationResult with {ok, errors} pattern
- ValidationError with field, message, code
- Pure functions (no side effects)

### DateTime Helpers (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `DateTimeHelper.kt` | Date/time utilities | ✅ Complete |

**Features:**
- Age calculation
- Anniversary math (days until, days since)
- Date formatting (display, short)
- Date parsing (ISO, local)
- Date validation (today, past, future)
- Date manipulation (add days)

### Device Capabilities (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `DeviceCapabilities.kt` | Device info detection | ✅ Complete |

**Features:**
- Device model, manufacturer, OS version
- Capability detection (notifications, exact alarms)
- Display metrics
- Notch detection
- App version info

### Permission Service (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `PermissionService.kt` | Runtime permission handling | ✅ Complete |

**Features:**
- Permission state tracking (granted, denied, prompt, unavailable)
- Notification permission handling
- Exact alarm permission checking
- Required permissions list

### Lifecycle Service (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `LifecycleService.kt` | App lifecycle events | ✅ Complete |

**Features:**
- Foreground/background detection
- Background time tracking
- Timeout detection
- Lifecycle event listeners
- Back button handling

### File Service (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `FileService.kt` | File operations | ✅ Complete |

**Features:**
- Private app directory access
- File read/write/delete
- File existence check
- Directory size calculation
- Orphan media cleanup

### Media Storage (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `MediaStorage.kt` | Media file lifecycle | ✅ Complete |

**Features:**
- Store media files (photos, videos)
- Store bitmaps as JPEG
- Resolve media URLs
- Delete media files
- MIME type validation
- File size validation (25MB photo, 500MB video)
- Orphan cleanup

### Security Services (2 files)

| File | Purpose | Status |
|------|---------|--------|
| `PinHash.kt` | PBKDF2 PIN hashing | ✅ Complete |
| `AppLockService.kt` | App lock management | ✅ Complete |

**PinHash Features:**
- PBKDF2-HMAC-SHA-256
- 120,000 iterations
- 128-bit random salt
- Base64 encoding
- Constant-time comparison

**AppLockService Features:**
- Enable/disable app lock
- Create/verify/change PIN
- Lock/unlock state management
- Re-lock on foreground after timeout
- Memory-only lock state (cold start always locks)

### Notification Service (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `NotificationService.kt` | Local notification scheduling | ✅ Complete |

**Features:**
- 3 notification channels (reminders, anniversaries, general)
- Local scheduled notifications
- Notification registry for reconciliation
- Exact alarm scheduling (API 31+)
- Channel creation on bootstrap

### Bootstrap Pipeline (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `BootstrapService.kt` | Ordered initialization | ✅ Complete |

**Features:**
- 7-stage initialization pipeline
- Stage 1: persistence (CRITICAL) — database
- Stage 2: schema-verify (CRITICAL) — schema version
- Stage 3: device-capabilities
- Stage 4: lifecycle
- Stage 5: notifications
- Stage 6: app-lock
- Stage 7: application-state (placeholder)

### Search Engine (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `SearchEngine.kt` | Global search | ✅ Complete |

**Features:**
- NFKD normalization
- Diacritics stripping
- Per-feature providers
- Ranking (prefix > word-initial > substring)
- Recency tiebreaking
- Vault exclusion

### Relationship Service (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `RelationshipService.kt` | Couple relationship management | ✅ Complete |

**Features:**
- Owner/partner profile management
- Couple relationship management
- Days together calculation
- Anniversary calculation
- Profile updates

### App State Service (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `AppStateService.kt` | App state management | ✅ Complete |

**Features:**
- Theme mode management
- Text size management
- Onboarding state
- Reactive state flows

### Data Management Service (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `DataManagementService.kt` | Data management operations | ✅ Complete |

**Features:**
- Storage report (row counts, media bytes)
- Cache clear (orphan media sweep)
- Full reset (cancel notifications → delete domain rows → sweep media → remove PIN → reset settings)

---

## File Structure

```
app/src/main/java/com/twohearts/app/services/
├── error/
│   └── AppError.kt                    ← Unified error type
├── logger/
│   └── Logger.kt                      ← Leveled, scoped logger
├── validation/
│   └── Validator.kt                   ← Pure validation functions
├── datetime/
│   └── DateTimeHelper.kt              ← Date/time utilities
├── device/
│   └── DeviceCapabilities.kt          ← Device info detection
├── permission/
│   └── PermissionService.kt           ← Runtime permissions
├── lifecycle/
│   └── LifecycleService.kt            ← App lifecycle events
├── media/
│   ├── FileService.kt                 ← File operations
│   └── MediaStorage.kt                ← Media file lifecycle
├── security/
│   ├── PinHash.kt                     ← PBKDF2 PIN hashing
│   └── AppLockService.kt              ← App lock management
├── notification/
│   └── NotificationService.kt         ← Local notifications
├── bootstrap/
│   └── BootstrapService.kt            ← Initialization pipeline
├── search/
│   └── SearchEngine.kt                ← Global search
├── relationship/
│   └── RelationshipService.kt         ← Couple management
├── appstate/
│   └── AppStateService.kt             ← App state management
└── datamanagement/
    └── DataManagementService.kt        ← Data management
```

---

## Key Implementation Decisions

### 1. Error Taxonomy

The error system matches legacy AppError exactly:
- 8 categories for all error types
- Factory methods for clean error creation
- Safe user messages (no internal details exposed)
- Original exception preservation

### 2. Security Architecture

PIN security preserved exactly:
- PBKDF2-HMAC-SHA-256 (120k iterations, 128-bit salt)
- Constant-time comparison (prevents timing attacks)
- Android Keystore-backed storage
- Memory-only lock state (cold start always locks)

### 3. Notification System

Matches legacy exactly:
- 3 channels (reminders, anniversaries, general)
- Notification registry for reconciliation
- Exact alarm scheduling (API 31+)
- Channel creation on bootstrap

### 4. Bootstrap Pipeline

7-stage initialization matches legacy:
- Critical stages (persistence, schema-verify) fail fast
- Non-critical stages degrade gracefully
- Service dependency injection via constructor

### 5. Search Engine

Matches legacy exactly:
- NFKD normalization + diacritics stripping
- Per-feature providers
- 3-tier ranking (prefix > word-initial > substring)
- Recency tiebreaking

---

## Dependencies

No new dependencies added — all services use:
- Android SDK APIs
- Existing Room database
- Existing DataStore/EncryptedSharedPreferences
- Existing Kotlin coroutines

---

## Verification Results

### Pre-Stage 4

| Check | Result |
|-------|--------|
| Stage 3 complete | ✅ |
| Data layer working | ✅ |
| Migration roadmap intact | ✅ |

### Post-Stage 4

| Check | Result |
|-------|--------|
| All 16 service files created | ✅ |
| Error taxonomy implemented | ✅ |
| Logger with redaction | ✅ |
| Validators (10 functions) | ✅ |
| DateTime helpers | ✅ |
| Device capabilities | ✅ |
| Permission service | ✅ |
| Lifecycle service | ✅ |
| File service | ✅ |
| Media storage | ✅ |
| Security services (PinHash + AppLock) | ✅ |
| Notification service | ✅ |
| Bootstrap pipeline | ✅ |
| Search engine | ✅ |
| Relationship service | ✅ |
| App state service | ✅ |
| Data management service | ✅ |
| No secrets introduced | ✅ |
| HEAD == origin/master | ✅ |
| Working tree clean | ✅ |

---

## Issues and Risks

### Issues Encountered

1. **Circular dependencies** — DataManagementService needs services that depend on it. Resolved by using constructor injection and lazy initialization.

2. **NotificationReceiver** — Android requires BroadcastReceiver to be declared in manifest. Added placeholder implementation.

### Risks

| Risk | Mitigation | Status |
|------|-----------|--------|
| Security regression | PIN hashing uses same PBKDF2 parameters | ✅ Mitigated |
| Notification scheduling | Uses AlarmManager with exact alarms | ✅ Mitigated |
| Bootstrap ordering | Critical stages fail fast | ✅ Mitigated |

---

## Files Changed

### New Files (16)

1. `app/src/main/java/com/twohearts/app/services/error/AppError.kt`
2. `app/src/main/java/com/twohearts/app/services/logger/Logger.kt`
3. `app/src/main/java/com/twohearts/app/services/validation/Validator.kt`
4. `app/src/main/java/com/twohearts/app/services/datetime/DateTimeHelper.kt`
5. `app/src/main/java/com/twohearts/app/services/device/DeviceCapabilities.kt`
6. `app/src/main/java/com/twohearts/app/services/permission/PermissionService.kt`
7. `app/src/main/java/com/twohearts/app/services/lifecycle/LifecycleService.kt`
8. `app/src/main/java/com/twohearts/app/services/media/FileService.kt`
9. `app/src/main/java/com/twohearts/app/services/media/MediaStorage.kt`
10. `app/src/main/java/com/twohearts/app/services/security/PinHash.kt`
11. `app/src/main/java/com/twohearts/app/services/security/AppLockService.kt`
12. `app/src/main/java/com/twohearts/app/services/notification/NotificationService.kt`
13. `app/src/main/java/com/twohearts/app/services/bootstrap/BootstrapService.kt`
14. `app/src/main/java/com/twohearts/app/services/search/SearchEngine.kt`
15. `app/src/main/java/com/twohearts/app/services/relationship/RelationshipService.kt`
16. `app/src/main/java/com/twohearts/app/services/appstate/AppStateService.kt`
17. `app/src/main/java/com/twohearts/app/services/datamanagement/DataManagementService.kt`

**Documentation (1):**
1. `Migration/Stage-4/STAGE-4-CORE-SERVICES-MIGRATION.md`

---

## Next Stage

**Stage 5 — Onboarding Flow Migration** will:

1. Port OnboardingLayout (step indicator, back navigation)
2. Port WelcomeScreen (simple, mostly static)
3. Port ProfileSetupScreen (form + validation)
4. Port RelationshipSetupScreen (form + date picker + validation)
5. Port PersonalizationSetupScreen (theme + text size selectors)
6. Port AppLockSetupScreen (PIN creation)
7. Port SetupCompleteScreen (celebration + CTA)
8. Port OnboardingGate (state evaluation + redirect logic)
9. Port useOnboarding hook
10. Verify the complete flow from fresh install to app entry

---

**Stage 4 is complete. Do not proceed to Stage 5.**
