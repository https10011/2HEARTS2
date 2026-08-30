import { test } from 'node:test';
import assert from 'node:assert';
import {
  finalizeDatabaseForTests,
  getDatabase,
  initializeDatabase,
  isDatabaseInitialized,
} from '../src/data/database/connection.ts';
import { SqlJsAdapter } from '../src/data/database/sqlJsAdapter.ts';
import { PersistenceError } from '../src/data/database/errors.ts';

test('access before initialization is rejected with not-initialized', async () => {
  await finalizeDatabaseForTests();
  await assert.rejects(getDatabase(), (error: unknown) => {
    return error instanceof PersistenceError && error.code === 'not-initialized';
  });
});

test('initialization is memoized — concurrent callers share one database', async () => {
  await finalizeDatabaseForTests();
  const factory = () => new SqlJsAdapter();
  const [a, b] = await Promise.all([initializeDatabase(factory), initializeDatabase(factory)]);
  assert.strictEqual(a, b);
  assert.ok(isDatabaseInitialized());
  const again = await getDatabase();
  assert.strictEqual(again, a);
  await finalizeDatabaseForTests();
});

test('failed initialization clears the memo so a retry can succeed', async () => {
  await finalizeDatabaseForTests();
  await assert.rejects(
    initializeDatabase(() => {
      throw new PersistenceError('init-failed', 'forced failure');
    }),
    (error: unknown) => error instanceof PersistenceError && error.code === 'init-failed',
  );
  assert.ok(!isDatabaseInitialized());
  const adapter = await initializeDatabase(() => new SqlJsAdapter());
  assert.ok(adapter);
  await finalizeDatabaseForTests();
});

test('failed adapter open surfaces init-failed', async () => {
  await finalizeDatabaseForTests();
  await assert.rejects(
    initializeDatabase(() => {
      return {
        open: () => Promise.reject(new Error('no wasm')),
        exec: () => Promise.reject(),
        run: () => Promise.reject(),
        query: () => Promise.reject(),
        transaction: () => Promise.reject(),
        close: () => Promise.resolve(),
      };
    }),
    (error: unknown) => error instanceof PersistenceError && error.code === 'init-failed',
  );
  await finalizeDatabaseForTests();
});
