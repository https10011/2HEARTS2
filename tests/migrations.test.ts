import { test } from 'node:test';
import assert from 'node:assert';
import { SqlJsAdapter } from '../src/data/database/sqlJsAdapter.ts';
import { ALL_MIGRATIONS } from '../src/data/database/migrations/index.ts';
import { runMigrations } from '../src/data/database/migrations/runner.ts';
import { PERSISTENCE_CONFIG } from '../src/config/persistence.ts';
import type { AppliedMigrationRow } from '../src/data/database/migrations/types.ts';
import { isValidIsoTimestamp } from '../src/utils/time.ts';

test('migrations apply in order on a fresh database', async () => {
  const db = new SqlJsAdapter();
  await db.open();
  await runMigrations(db, ALL_MIGRATIONS);

  const applied = await db.query<AppliedMigrationRow>('SELECT * FROM schema_migrations ORDER BY id ASC');
  assert.deepStrictEqual(applied.map((row) => row.id), [1]);
  assert.deepStrictEqual(applied.map((row) => row.name), ['initial-schema']);
  assert.ok(isValidIsoTimestamp(applied[0].applied_at));

  const tables = await db.query<{ name: string }>(
    "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'media_assets'",
  );
  assert.strictEqual(tables.length, 1);
  await db.close();
});

test('migrations are idempotent — running again applies nothing twice', async () => {
  const db = new SqlJsAdapter();
  await db.open();
  await runMigrations(db, ALL_MIGRATIONS);
  await runMigrations(db, ALL_MIGRATIONS);
  await runMigrations(db, ALL_MIGRATIONS);

  const applied = await db.query<AppliedMigrationRow>('SELECT * FROM schema_migrations');
  assert.strictEqual(applied.length, 1);
  assert.strictEqual(applied[0].id, 1);
  await db.close();
});

test('schema version matches the highest migration id', () => {
  const highest = Math.max(...ALL_MIGRATIONS.map((m) => m.id));
  assert.strictEqual(PERSISTENCE_CONFIG.schemaVersion, highest);
});

test('migration registry is ordered and gap-less', () => {
  const ids = ALL_MIGRATIONS.map((m) => m.id);
  assert.deepStrictEqual(ids, [...ids].sort((a, b) => a - b));
  ids.forEach((id, index) => assert.strictEqual(id, index + 1));
});

test('a failing migration aborts cleanly and marks nothing applied', async () => {
  const db = new SqlJsAdapter();
  await db.open();
  const broken = {
    id: 99,
    name: 'broken',
    up: async () => {
      throw new Error('boom');
    },
  };
  await assert.rejects(runMigrations(db, [broken]), (error: unknown) => {
    return error instanceof Error && (error as { code?: string }).code === 'migration-failed';
  });
  const applied = await db.query<AppliedMigrationRow>('SELECT * FROM schema_migrations');
  assert.strictEqual(applied.length, 0);
  await db.close();
});
