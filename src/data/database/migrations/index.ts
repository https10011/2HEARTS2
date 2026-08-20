/**
 * Ordered migration registry (Phase 2).
 *
 * `ALL_MIGRATIONS` is the single source of truth for schema evolution.
 * `PERSISTENCE_CONFIG.schemaVersion` must equal the highest id here.
 * New schema changes APPEND a migration; existing migrations are never
 * edited once shipped.
 */

import { PERSISTENCE_CONFIG } from '../../../config/persistence.ts';
import { initialSchemaMigration } from './001_initial.ts';
import { notificationRegistryMigration } from './002_notification_registry.ts';
import { relationshipFoundationMigration } from './003_relationship_foundation.ts';
import { memoriesMigration } from './004_memories.ts';
import { notesMigration } from './005_notes.ts';
import type { Migration } from './types.ts';

export const ALL_MIGRATIONS: Migration[] = [
  initialSchemaMigration,
  notificationRegistryMigration,
  relationshipFoundationMigration,
  memoriesMigration,
  notesMigration,
];

/** Guards the config/registry agreement at module load. */
function assertRegistryConsistent(): void {
  const highest = Math.max(...ALL_MIGRATIONS.map((m) => m.id));
  if (highest !== PERSISTENCE_CONFIG.schemaVersion) {
    throw new Error(
      `schemaVersion ${PERSISTENCE_CONFIG.schemaVersion} does not match migration registry (highest id ${highest}).`,
    );
  }
  const ids = ALL_MIGRATIONS.map((m) => m.id).sort((a, b) => a - b);
  ids.forEach((id, index) => {
    if (id !== index + 1) {
      throw new Error('Migration ids must be gap-less starting at 1.');
    }
  });
}

assertRegistryConsistent();
