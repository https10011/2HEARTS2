# Stage 3 — Data Layer Migration

**Status:** ✅ Complete  
**Date:** 2026-08-30  
**Preceded by:** Stage 2 (Component Library Migration)  
**Followed by:** Stage 4 (Core Services Migration)

---

## Overview

Stage 3 migrated the complete data layer from the legacy React/Vite/Capacitor implementation to native Android using Room, DataStore, and EncryptedSharedPreferences. The entire persistence architecture has been recreated with full schema compatibility.

---

## What Was Completed

### Database Entities (17 files)

All legacy data models have been ported to Room entities:

| Entity | File | Legacy Table | Status |
|--------|------|--------------|--------|
| BaseEntity | `BaseEntity.kt` | Entity conventions | ✅ Complete |
| Profile | `Profile.kt` | `profiles` | ✅ Complete |
| CoupleRelationship | `CoupleRelationship.kt` | `couple_relationship` | ✅ Complete |
| ImportantDate | `ImportantDate.kt` | `important_dates` | ✅ Complete |
| MediaAsset | `MediaAsset.kt` | `media_assets` | ✅ Complete |
| Memory | `Memory.kt` | `memories` | ✅ Complete |
| MemoryMedia | `MemoryMedia.kt` | `memory_media` | ✅ Complete |
| Note | `Note.kt` | `notes` | ✅ Complete |
| TimelineEvent | `TimelineEvent.kt` | `timeline_events` | ✅ Complete |
| Reminder | `Reminder.kt` | `reminders` | ✅ Complete |
| Place | `Place.kt` | `places` | ✅ Complete |
| MoodEntry | `MoodEntry.kt` | `mood_entries` | ✅ Complete |
| PeriodEntry | `PeriodEntry.kt` | `period_entries` | ✅ Complete |
| PeriodSettings | `PeriodSettings.kt` | `period_settings` | ✅ Complete |
| VaultItem | `VaultItem.kt` | `vault_items` | ✅ Complete |
| NotificationCenterEntry | `NotificationCenterEntry.kt` | `notification_center` | ✅ Complete |
| NotificationRegistry | `NotificationRegistry.kt` | `notification_registry` | ✅ Complete |
| SchemaMigration | `SchemaMigration.kt` | `schema_migrations` | ✅ Complete |

### DAOs (16 files)

| DAO | File | Operations |
|-----|------|------------|
| ProfileDao | `ProfileDao.kt` | CRUD + role query |
| CoupleRelationshipDao | `CoupleRelationshipDao.kt` | CRUD + single query |
| ImportantDateDao | `ImportantDateDao.kt` | CRUD |
| MediaAssetDao | `MediaAssetDao.kt` | CRUD |
| MemoryDao | `MemoryDao.kt` | CRUD + date ordering |
| MemoryMediaDao | `MemoryMediaDao.kt` | Insert/delete + cascade |
| NoteDao | `NoteDao.kt` | CRUD + category filter |
| TimelineEventDao | `TimelineEventDao.kt` | CRUD + date ordering |
| ReminderDao | `ReminderDao.kt` | CRUD + pending filter |
| PlaceDao | `PlaceDao.kt` | CRUD + category filter |
| MoodEntryDao | `MoodEntryDao.kt` | CRUD + date/profile queries |
| PeriodEntryDao | `PeriodEntryDao.kt` | CRUD + profile filter |
| PeriodSettingsDao | `PeriodSettingsDao.kt` | CRUD + single query |
| VaultItemDao | `VaultItemDao.kt` | CRUD + content type filter |
| NotificationCenterDao | `NotificationCenterDao.kt` | CRUD + unread/mark as read |
| NotificationRegistryDao | `NotificationRegistryDao.kt` | CRUD + owner ref query |

### Database (1 file)

| File | Purpose | Status |
|------|---------|--------|
| `TwoHeartsDatabase.kt` | Room database with 13 migrations | ✅ Complete |
| `DatabaseInitializer.kt` | Database setup and verification | ✅ Complete |

### Repositories (14 files)

| Repository | File | Entity |
|------------|------|--------|
| ProfileRepository | `ProfileRepository.kt` | Profile |
| CoupleRelationshipRepository | `CoupleRelationshipRepository.kt` | CoupleRelationship |
| ImportantDateRepository | `ImportantDateRepository.kt` | ImportantDate |
| MediaAssetRepository | `MediaAssetRepository.kt` | MediaAsset |
| MemoryRepository | `MemoryRepository.kt` | Memory + MemoryMedia |
| NoteRepository | `NoteRepository.kt` | Note |
| TimelineEventRepository | `TimelineEventRepository.kt` | TimelineEvent |
| ReminderRepository | `ReminderRepository.kt` | Reminder |
| PlaceRepository | `PlaceRepository.kt` | Place |
| MoodEntryRepository | `MoodEntryRepository.kt` | MoodEntry |
| PeriodEntryRepository | `PeriodEntryRepository.kt` | PeriodEntry |
| PeriodSettingsRepository | `PeriodSettingsRepository.kt` | PeriodSettings |
| VaultItemRepository | `VaultItemRepository.kt` | VaultItem |
| NotificationCenterRepository | `NotificationCenterRepository.kt` | NotificationCenterEntry |
| BaseRepository | `BaseRepository.kt` | Common CRUD interface |

### Settings Storage (3 files)

| File | Purpose | Status |
|------|---------|--------|
| `AppSettings.kt` | Settings schema + keys | ✅ Complete |
| `SettingsStorage.kt` | DataStore implementation | ✅ Complete |
| `SecureStorage.kt` | EncryptedSharedPreferences for PIN | ✅ Complete |

---

## File Structure

```
app/src/main/java/com/twohearts/app/data/
├── entity/
│   ├── BaseEntity.kt                    ← Base types (UUID v4, timestamps, tombstone)
│   ├── Profile.kt                       ← Profile entity
│   ├── CoupleRelationship.kt            ← Couple relationship entity
│   ├── ImportantDate.kt                 ← Important date entity
│   ├── MediaAsset.kt                    ← Media asset entity
│   ├── Memory.kt                        ← Memory entity
│   ├── MemoryMedia.kt                   ← Memory-media join entity
│   ├── Note.kt                          ← Note entity
│   ├── TimelineEvent.kt                 ← Timeline event entity
│   ├── Reminder.kt                      ← Reminder entity
│   ├── Place.kt                         ← Place entity
│   ├── MoodEntry.kt                     ← Mood entry entity
│   ├── PeriodEntry.kt                   ← Period entry entity
│   ├── PeriodSettings.kt                ← Period settings entity
│   ├── VaultItem.kt                     ← Vault item entity
│   ├── NotificationCenterEntry.kt       ← Notification center entity
│   ├── NotificationRegistry.kt          ← Notification registry entity
│   └── SchemaMigration.kt               ← Schema migration entity
├── dao/
│   ├── ProfileDao.kt                    ← Profile DAO
│   ├── CoupleRelationshipDao.kt         ← Couple relationship DAO
│   ├── ImportantDateDao.kt              ← Important date DAO
│   ├── MediaAssetDao.kt                 ← Media asset DAO
│   ├── MemoryDao.kt                     ← Memory DAO
│   ├── MemoryMediaDao.kt                ← Memory-media DAO
│   ├── NoteDao.kt                       ← Note DAO
│   ├── TimelineEventDao.kt              ← Timeline event DAO
│   ├── ReminderDao.kt                   ← Reminder DAO
│   ├── PlaceDao.kt                      ← Place DAO
│   ├── MoodEntryDao.kt                  ← Mood entry DAO
│   ├── PeriodEntryDao.kt                ← Period entry DAO
│   ├── PeriodSettingsDao.kt             ← Period settings DAO
│   ├── VaultItemDao.kt                  ← Vault item DAO
│   ├── NotificationCenterDao.kt         ← Notification center DAO
│   └── NotificationRegistryDao.kt       ← Notification registry DAO
├── database/
│   ├── TwoHeartsDatabase.kt             ← Room database with 13 migrations
│   └── DatabaseInitializer.kt           ← Database initialization and verification
├── repository/
│   ├── BaseRepository.kt                ← Common CRUD interface
│   ├── ProfileRepository.kt             ← Profile repository
│   ├── CoupleRelationshipRepository.kt  ← Couple relationship repository
│   ├── ImportantDateRepository.kt       ← Important date repository
│   ├── MediaAssetRepository.kt          ← Media asset repository
│   ├── MemoryRepository.kt              ← Memory repository
│   ├── NoteRepository.kt                ← Note repository
│   ├── TimelineEventRepository.kt       ← Timeline event repository
│   ├── ReminderRepository.kt            ← Reminder repository
│   ├── PlaceRepository.kt               ← Place repository
│   ├── MoodEntryRepository.kt           ← Mood entry repository
│   ├── PeriodEntryRepository.kt         ← Period entry repository
│   ├── PeriodSettingsRepository.kt      ← Period settings repository
│   ├── VaultItemRepository.kt           ← Vault item repository
│   └── NotificationCenterRepository.kt  ← Notification center repository
└── settings/
    ├── AppSettings.kt                   ← Settings schema and keys
    ├── SettingsStorage.kt               ← DataStore implementation
    └── SecureStorage.kt                 ← EncryptedSharedPreferences for PIN
```

---

## Key Implementation Decisions

### 1. Entity Conventions

All entities follow legacy conventions exactly:
- **Primary Key:** UUID v4 (`id: String`)
- **Timestamps:** ISO 8601 UTC (`createdAt`, `updatedAt`)
- **Soft Deletes:** Tombstone pattern (`deletedAt: String?`)
- **Column Naming:** snake_case in database, camelCase in Kotlin

### 2. Database Schema

The Room database uses schema version 13 with all 13 migrations from the legacy implementation:
- Migration 001: Initial (schema_migrations, media_assets)
- Migration 002: notification_registry
- Migration 003: relationship_foundation (profiles, couple_relationship, important_dates)
- Migration 004: memories + memory_media
- Migration 005: notes
- Migration 006: timeline_events
- Migration 007: reminders
- Migration 008: places
- Migration 009: mood_entries
- Migration 010: period_entries + period_settings
- Migration 011: vault_items
- Migration 012: notification_center
- Migration 013: profile_photo (adds photo_ref to profiles)

### 3. Repository Pattern

Each repository follows the legacy BaseRepository pattern:
- `getAll()` — returns all non-deleted entities
- `getById(id)` — returns single entity
- `create(entity)` — inserts with timestamps
- `update(id, changes)` — updates with updatedAt refresh
- `softDelete(id)` — sets deletedAt (tombstone)
- `count()` — returns count of non-deleted entities

### 4. Settings Storage

- **DataStore:** Replaces legacy localStorage for app settings
- **EncryptedSharedPreferences:** Replaces legacy @aparajita/capacitor-secure-storage for PIN material
- **Schema:** Matches legacy AppSettings (v3) exactly

### 5. Security Architecture

PIN storage uses Android Keystore-backed EncryptedSharedPreferences:
- PBKDF2-HMAC-SHA-256 (120k iterations, 128-bit salt)
- AES256_GCM encryption for stored values
- PIN material never enters settings, UI state, or logs

---

## Migration Compatibility

### Schema Compatibility

All column names, types, and constraints match the legacy database exactly:
- `id TEXT NOT NULL PRIMARY KEY`
- `created_at TEXT NOT NULL`
- `updated_at TEXT NOT NULL`
- `deleted_at TEXT`
- All foreign key relationships preserved

### Data Format Compatibility

All data formats match legacy conventions:
- UUID v4 for IDs
- ISO 8601 UTC for timestamps
- LOCAL calendar key (yyyy-mm-dd) for dates
- snake_case for database columns
- camelCase for Kotlin properties

### Migration Path

Room handles migrations automatically:
- All 13 migrations are defined in `TwoHeartsDatabase.kt`
- Database initializes with `Room.databaseBuilder().addMigrations()`
- Schema verification is automatic

---

## Dependencies Added

```kotlin
// Room (SQLite)
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// DataStore (replaces SharedPreferences)
implementation("androidx.datastore:datastore-preferences:1.1.1")

// Security (encrypted storage)
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

---

## Verification Results

### Pre-Stage 3

| Check | Result |
|-------|--------|
| Stage 2 complete | ✅ |
| Components working | ✅ |
| Theme system working | ✅ |
| Migration roadmap intact | ✅ |

### Post-Stage 3

| Check | Result |
|-------|--------|
| All 17 entities created | ✅ |
| All 16 DAOs created | ✅ |
| Room database with 13 migrations | ✅ |
| All 15 repositories created | ✅ |
| DataStore for settings | ✅ |
| EncryptedSharedPreferences for PIN | ✅ |
| Schema compatibility verified | ✅ |
| No secrets introduced | ✅ |
| HEAD == origin/master | ✅ |
| Working tree clean | ✅ |

---

## Issues and Risks

### Issues Encountered

1. **kapt configuration** — Initially had both `annotationProcessor` and `kapt` for Room. Fixed by removing `annotationProcessor` and adding `kapt` plugin.

2. **Schema export** — Added `room.schemaLocation` argument to kapt for migration testing.

### Risks

| Risk | Mitigation | Status |
|------|-----------|--------|
| Schema drift from legacy | All migrations match legacy exactly | ✅ Mitigated |
| Data format incompatibility | All formats match legacy conventions | ✅ Mitigated |
| Security regression | PIN storage uses same PBKDF2 parameters | ✅ Mitigated |

---

## Files Changed

### New Files (51)

**Entities (17):**
1. `app/src/main/java/com/twohearts/app/data/entity/BaseEntity.kt`
2. `app/src/main/java/com/twohearts/app/data/entity/Profile.kt`
3. `app/src/main/java/com/twohearts/app/data/entity/CoupleRelationship.kt`
4. `app/src/main/java/com/twohearts/app/data/entity/ImportantDate.kt`
5. `app/src/main/java/com/twohearts/app/data/entity/MediaAsset.kt`
6. `app/src/main/java/com/twohearts/app/data/entity/Memory.kt`
7. `app/src/main/java/com/twohearts/app/data/entity/MemoryMedia.kt`
8. `app/src/main/java/com/twohearts/app/data/entity/Note.kt`
9. `app/src/main/java/com/twohearts/app/data/entity/TimelineEvent.kt`
10. `app/src/main/java/com/twohearts/app/data/entity/Reminder.kt`
11. `app/src/main/java/com/twohearts/app/data/entity/Place.kt`
12. `app/src/main/java/com/twohearts/app/data/entity/MoodEntry.kt`
13. `app/src/main/java/com/twohearts/app/data/entity/PeriodEntry.kt`
14. `app/src/main/java/com/twohearts/app/data/entity/PeriodSettings.kt`
15. `app/src/main/java/com/twohearts/app/data/entity/VaultItem.kt`
16. `app/src/main/java/com/twohearts/app/data/entity/NotificationCenterEntry.kt`
17. `app/src/main/java/com/twohearts/app/data/entity/NotificationRegistry.kt`
18. `app/src/main/java/com/twohearts/app/data/entity/SchemaMigration.kt`

**DAOs (16):**
1. `app/src/main/java/com/twohearts/app/data/dao/ProfileDao.kt`
2. `app/src/main/java/com/twohearts/app/data/dao/CoupleRelationshipDao.kt`
3. `app/src/main/java/com/twohearts/app/data/dao/ImportantDateDao.kt`
4. `app/src/main/java/com/twohearts/app/data/dao/MediaAssetDao.kt`
5. `app/src/main/java/com/twohearts/app/data/dao/MemoryDao.kt`
6. `app/src/main/java/com/twohearts/app/data/dao/MemoryMediaDao.kt`
7. `app/src/main/java/com/twohearts/app/data/dao/NoteDao.kt`
8. `app/src/main/java/com/twohearts/app/data/dao/TimelineEventDao.kt`
9. `app/src/main/java/com/twohearts/app/data/dao/ReminderDao.kt`
10. `app/src/main/java/com/twohearts/app/data/dao/PlaceDao.kt`
11. `app/src/main/java/com/twohearts/app/data/dao/MoodEntryDao.kt`
12. `app/src/main/java/com/twohearts/app/data/dao/PeriodEntryDao.kt`
13. `app/src/main/java/com/twohearts/app/data/dao/PeriodSettingsDao.kt`
14. `app/src/main/java/com/twohearts/app/data/dao/VaultItemDao.kt`
15. `app/src/main/java/com/twohearts/app/data/dao/NotificationCenterDao.kt`
16. `app/src/main/java/com/twohearts/app/data/dao/NotificationRegistryDao.kt`

**Database (2):**
1. `app/src/main/java/com/twohearts/app/data/database/TwoHeartsDatabase.kt`
2. `app/src/main/java/com/twohearts/app/data/database/DatabaseInitializer.kt`

**Repositories (15):**
1. `app/src/main/java/com/twohearts/app/data/repository/BaseRepository.kt`
2. `app/src/main/java/com/twohearts/app/data/repository/ProfileRepository.kt`
3. `app/src/main/java/com/twohearts/app/data/repository/CoupleRelationshipRepository.kt`
4. `app/src/main/java/com/twohearts/app/data/repository/ImportantDateRepository.kt`
5. `app/src/main/java/com/twohearts/app/data/repository/MediaAssetRepository.kt`
6. `app/src/main/java/com/twohearts/app/data/repository/MemoryRepository.kt`
7. `app/src/main/java/com/twohearts/app/data/repository/NoteRepository.kt`
8. `app/src/main/java/com/twohearts/app/data/repository/TimelineEventRepository.kt`
9. `app/src/main/java/com/twohearts/app/data/repository/ReminderRepository.kt`
10. `app/src/main/java/com/twohearts/app/data/repository/PlaceRepository.kt`
11. `app/src/main/java/com/twohearts/app/data/repository/MoodEntryRepository.kt`
12. `app/src/main/java/com/twohearts/app/data/repository/PeriodEntryRepository.kt`
13. `app/src/main/java/com/twohearts/app/data/repository/PeriodSettingsRepository.kt`
14. `app/src/main/java/com/twohearts/app/data/repository/VaultItemRepository.kt`
15. `app/src/main/java/com/twohearts/app/data/repository/NotificationCenterRepository.kt`

**Settings (3):**
1. `app/src/main/java/com/twohearts/app/data/settings/AppSettings.kt`
2. `app/src/main/java/com/twohearts/app/data/settings/SettingsStorage.kt`
3. `app/src/main/java/com/twohearts/app/data/settings/SecureStorage.kt`

**Documentation (1):**
1. `Migration/Stage-3/STAGE-3-DATA-LAYER-MIGRATION.md`

### Modified Files (1)

1. `app/build.gradle.kts` — Added kapt plugin, Room schema export

---

## Next Stage

**Stage 4 — Core Services Migration** will:

1. Port error taxonomy (AppError with categories, codes, safe messages)
2. Port logger with redaction rules
3. Port validators (pure functions)
4. Port DateTime helpers
5. Port DeviceCapabilities
6. Port PermissionService
7. Port LifecycleService
8. Port FileService + file adapters
9. Port MediaStorage + media utils
10. Port Security services (AppLockService, pinHash, SecureStore)
11. Port NotificationService + drivers
12. Port Bootstrap pipeline
13. Port SearchEngine + normalization
14. Port RelationshipService + AppStateService
15. Port DataManagementService

---

**Stage 3 is complete. Do not proceed to Stage 4.**
