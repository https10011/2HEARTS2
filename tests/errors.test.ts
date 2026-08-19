import { test } from 'node:test';
import assert from 'node:assert';
import { normalizeError, PersistenceError, safeMessageFor } from '../src/data/database/errors.ts';

test('errors normalize into typed PersistenceError codes', () => {
  const wrapped = normalizeError(new Error('sql syntax near FROM at /secret/path'), 'query-failed');
  assert.ok(wrapped instanceof PersistenceError);
  assert.strictEqual(wrapped.code, 'query-failed');
});

test('safeMessageFor never leaks SQL, paths, or domain data', () => {
  const raw = new PersistenceError('query-failed', 'INSERT INTO media_assets VALUES ("secret-note") at /data/secret');
  const message = safeMessageFor(raw);
  assert.ok(!message.includes('INSERT'));
  assert.ok(!message.includes('/data/'));
  assert.ok(!message.includes('secret'));
  assert.strictEqual(message, 'Reading local data failed.');
  assert.strictEqual(safeMessageFor(new Error('whatever')), 'An unexpected data error occurred.');
});

test('transactions roll back partial writes on failure', async () => {
  const { SqlJsAdapter } = await import('../src/data/database/sqlJsAdapter.ts');
  const { runMigrations } = await import('../src/data/database/migrations/runner.ts');
  const { ALL_MIGRATIONS } = await import('../src/data/database/migrations/index.ts');
  const db = new SqlJsAdapter();
  await db.open();
  await runMigrations(db, ALL_MIGRATIONS);

  await assert.rejects(
    db.transaction(async (tx) => {
      await tx.run(
        "INSERT INTO media_assets (id, kind, mime_type, relative_path, created_at, updated_at) VALUES ('9e7fd8b4-85b1-4be6-91b1-2b9c0f9f0e11','photo','image/jpeg','media/photos/x.jpg','2026-01-01T00:00:00.000Z','2026-01-01T00:00:00.000Z')",
      );
      throw new Error('abort');
    }),
    (error: unknown) => error instanceof PersistenceError && error.code === 'transaction-failed',
  );
  const rows = await db.query('SELECT id FROM media_assets');
  assert.strictEqual(rows.length, 0);
  await db.close();
});

test('nested transactions are rejected explicitly', async () => {
  const { SqlJsAdapter } = await import('../src/data/database/sqlJsAdapter.ts');
  const db = new SqlJsAdapter();
  await db.open();
  await assert.rejects(
    db.transaction(async (tx) => db.transaction(async () => undefined)),
    (error: unknown) => error instanceof PersistenceError && error.code === 'transaction-failed',
  );
  await db.close();
});
