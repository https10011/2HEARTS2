/**
 * Migration runner (Phase 2).
 *
 * State is tracked in an explicit `schema_migrations` ledger (not the
 * SQLite `user_version` pragma) so application-owned schema evolution is
 * deterministic and auditable:
 *
 * - each migration runs exactly once, inside its own transaction (all or
 *   nothing),
 * - already-applied ids are detected and skipped (idempotent re-entry),
 * - the ledger entry is written in the same transaction as the migration,
 * - any failure aborts initialization with `migration-failed`.
 */

import type { DatabaseAdapter } from '../adapter.ts';
import { PersistenceError } from '../errors.ts';
import type { AppliedMigrationRow, Migration } from './types.ts';

export const MIGRATIONS_TABLE_SQL = `
CREATE TABLE IF NOT EXISTS schema_migrations (
  id INTEGER PRIMARY KEY NOT NULL,
  name TEXT NOT NULL,
  applied_at TEXT NOT NULL
);`;

/** Applies pending migrations in order. Safe to call on every launch. */
export async function runMigrations(adapter: DatabaseAdapter, migrations: Migration[]): Promise<void> {
  try {
    await adapter.exec(MIGRATIONS_TABLE_SQL);
    const applied = await adapter.query<AppliedMigrationRow>(
      'SELECT id, name, applied_at FROM schema_migrations',
    );
    const appliedIds = new Set(applied.map((row) => row.id));

    const ordered = [...migrations].sort((a, b) => a.id - b.id);
    for (const migration of ordered) {
      if (appliedIds.has(migration.id)) continue;
      await adapter.transaction(async (tx) => {
        await migration.up(tx);
        await tx.run(
          'INSERT INTO schema_migrations (id, name, applied_at) VALUES (?, ?, ?)',
          [migration.id, migration.name, new Date().toISOString()],
        );
      });
      appliedIds.add(migration.id);
    }
  } catch (cause) {
    if (cause instanceof PersistenceError && cause.code === 'migration-failed') throw cause;
    throw new PersistenceError('migration-failed', 'Schema migration failed.', { cause });
  }
}
