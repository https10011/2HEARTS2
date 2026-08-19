import { SqlJsAdapter } from '../src/data/database/sqlJsAdapter.ts';
import { ALL_MIGRATIONS } from '../src/data/database/migrations/index.ts';
import { runMigrations } from '../src/data/database/migrations/runner.ts';

/** Fresh, migrated in-memory database for one test. */
export async function openMigratedDb(): Promise<SqlJsAdapter> {
  const adapter = new SqlJsAdapter();
  await adapter.open();
  await runMigrations(adapter, ALL_MIGRATIONS);
  return adapter;
}
