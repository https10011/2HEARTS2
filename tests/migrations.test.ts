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
  assert.deepStrictEqual(applied.map((row) => row.id), [1, 2, 3, 4, 5, 6, 7, 8, 9]);
  assert.deepStrictEqual(applied.map((row) => row.name), [
    'initial-schema',
    'notification-registry',
    'relationship-foundation',
    'memories',
    'notes',
    'timeline-events',
    'reminders',
    'places',
    'mood-entries',
  ]);
  assert.ok(isValidIsoTimestamp(applied[0].applied_at));

  const tables = await db.query<{ name: string }>(
    `SELECT name FROM sqlite_master WHERE type = 'table' AND name IN
      ('media_assets', 'notification_registry', 'profiles', 'couple_relationship', 'important_dates', 'memories', 'memory_media', 'notes', 'timeline_events', 'reminders', 'places')
     ORDER BY name`,
  );
  assert.deepStrictEqual(
    tables.map((t) => t.name),
    ['couple_relationship', 'important_dates', 'media_assets', 'memories', 'memory_media', 'notes', 'notification_registry', 'places', 'profiles', 'reminders', 'timeline_events'],
  );
  await db.close();
});

test('migrations are idempotent — running again applies nothing twice', async () => {
  const db = new SqlJsAdapter();
  await db.open();
  await runMigrations(db, ALL_MIGRATIONS);
  await runMigrations(db, ALL_MIGRATIONS);
  await runMigrations(db, ALL_MIGRATIONS);

  const applied = await db.query<AppliedMigrationRow>('SELECT * FROM schema_migrations');
  assert.strictEqual(applied.length, 9);
  assert.deepStrictEqual(
    applied.map((row) => row.id).sort(),
    [1, 2, 3, 4, 5, 6, 7, 8, 9],
  );
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

test('existing v2 data survives the migration to schema v3', async () => {
  const db = new SqlJsAdapter();
  await db.open();

  // Simulate a Phase 3 install: migrations 1+2 applied, real data present.
  const phase3Migrations = ALL_MIGRATIONS.filter((m) => m.id <= 2);
  await runMigrations(db, phase3Migrations);
  await db.run(
    `INSERT INTO notification_registry
       (notification_id, owner_ref, channel_id, title, body, scheduled_at, meta_json, created_at, updated_at)
     VALUES
       (7, 'reminder:test', 'reminders', 'T', 'B', '2026-08-20T10:00:00Z', '{"a":1}', '2026-08-19T10:00:00Z', '2026-08-19T10:00:00Z')`,
  );

  // The v2→v3 upgrade applies only migration 3 and preserves everything.
  await runMigrations(db, ALL_MIGRATIONS);
  const applied = await db.query<AppliedMigrationRow>('SELECT * FROM schema_migrations ORDER BY id ASC');
  assert.deepStrictEqual(applied.map((row) => row.id), [1, 2, 3, 4, 5, 6, 7, 8, 9]);

  const rows = await db.query<{ owner_ref: string }>('SELECT owner_ref FROM notification_registry');
  assert.deepStrictEqual(rows.map((r) => r.owner_ref), ['reminder:test']);

  const tables = await db.query<{ name: string }>(
    "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'profiles'",
  );
  assert.strictEqual(tables.length, 1);
  await db.close();
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
