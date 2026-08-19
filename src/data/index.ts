/**
 * Data layer barrel (Phase 2). Import from here or the specific module —
 * never re-exported raw engine APIs.
 */

export { initializeDatabase, getDatabase, isDatabaseInitialized } from './database/connection.ts';
export type { DatabaseAdapter, RunResult, SqlValue, TransactionContext } from './database/adapter.ts';
export { PersistenceError, normalizeError, safeMessageFor } from './database/errors.ts';
export type { PersistenceErrorCode } from './database/errors.ts';
export { ALL_MIGRATIONS } from './database/migrations/index.ts';
export { runMigrations } from './database/migrations/runner.ts';
export type { Migration } from './database/migrations/types.ts';
export type { Entity, TombstonedEntity, NewEntity } from './model/entity.ts';
export { assertEntityConventions } from './model/entity.ts';
export type { EntitySerializer } from './serialization/entitySerializer.ts';
export { MediaStorage, type MediaReference } from './media/mediaStorage.ts';
export type { MediaAsset, MediaKind } from './media/mediaTypes.ts';
export type { MediaFileSystem } from './media/fileSystem.ts';
export type { SettingsStorage } from './settings/settingsStorage.ts';
export type {
  Profile,
  ProfileRole,
  CoupleRelationship,
  ImportantDate,
  Recurrence,
} from './relationship/relationshipTypes.ts';
export {
  assertProfile,
  assertCoupleRelationship,
  assertImportantDate,
  PROFILE_ROLES,
  RECURRENCES,
} from './relationship/relationshipTypes.ts';
