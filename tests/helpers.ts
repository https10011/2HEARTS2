import { SqlJsAdapter } from '../src/data/database/sqlJsAdapter.ts';
import { ALL_MIGRATIONS } from '../src/data/database/migrations/index.ts';
import { runMigrations as runAllMigrations } from '../src/data/database/migrations/runner.ts';
import type { DatabaseAdapter } from '../src/data/database/databaseAdapter.ts';

/** Create and open a fresh in-memory database adapter. */
export async function createMemoryAdapter(): Promise<SqlJsAdapter> {
  const adapter = new SqlJsAdapter();
  await adapter.open();
  return adapter;
}

/** Run all migrations on an existing adapter. */
export async function runMigrations(adapter: DatabaseAdapter): Promise<void> {
  await runAllMigrations(adapter, ALL_MIGRATIONS);
}

/** Fresh, migrated in-memory database for one test. */
export async function openMigratedDb(): Promise<SqlJsAdapter> {
  const adapter = new SqlJsAdapter();
  await adapter.open();
  await runAllMigrations(adapter, ALL_MIGRATIONS);
  return adapter;
}
