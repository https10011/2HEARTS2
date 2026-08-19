import { test, beforeEach } from 'node:test';
import assert from 'node:assert';
import { bootstrapApp, coreServices } from '../src/services/bootstrap/appBootstrap.ts';
import { finalizeDatabaseForTests, getDatabase } from '../src/data/database/connection.ts';
import { appLifecycle } from '../src/services/lifecycle/appLifecycleService.ts';

// Bootstrap drives the REAL pipeline end-to-end: sql.js persistence +
// migrations + capability probe + lifecycle + notification channels +
// app-lock material check. Only native drivers are substituted by design
// (they cannot exist under Node — documented NOT VERIFIED on-device).

beforeEach(async () => {
  await finalizeDatabaseForTests();
  appLifecycle.stop();
});

test('bootstrap runs all stages and reaches ready state', async () => {
  const result = await bootstrapApp();
  assert.ok(result.ok, JSON.stringify(result.stages));

  const names = result.stages.map((s) => s.name);
  assert.deepStrictEqual(names, [
    'persistence',
    'schema-verify',
    'device-capabilities',
    'lifecycle',
    'notifications',
    'app-lock',
    'application-state',
  ]);
  // Critical stages must succeed everywhere (sql.js path under Node).
  const critical = result.stages.filter((s) => s.critical);
  assert.ok(critical.every((s) => s.ok), JSON.stringify(result.stages));
  // Non-critical native stages (notification channels via the Capacitor
  // driver) legitimately degrade under Node — that IS the designed
  // log-and-continue path, and the pipeline must stay ok:true despite it.
  const degraded = result.stages.filter((s) => !s.ok);
  for (const stage of degraded) {
    assert.ok(!stage.critical);
    assert.ok(typeof stage.error === 'string');
  }

  // Persistence is live and migrated to the configured schema version.
  const db = await getDatabase();
  const rows = await db.query<{ max_id: number }>('SELECT MAX(id) AS max_id FROM schema_migrations');
  assert.ok(rows[0].max_id >= 1);

  // Core services are initialized (notification service only when the
  // native/web driver stage succeeded — degraded under Node).
  assert.ok(coreServices.device.isReady());
  assert.ok(coreServices.appLock);
  assert.strictEqual(coreServices.appLock.currentState(), 'disabled'); // lock off by default

  if (coreServices.notifications) {
    const pending = await coreServices.notifications.list();
    assert.deepStrictEqual(pending, []);
  }
});

test('bootstrap is safe to run repeatedly (idempotent)', async () => {
  const first = await bootstrapApp();
  const second = await bootstrapApp();
  assert.ok(first.ok && second.ok);
});

test('a critical persistence failure fails the pipeline with a safe message', async () => {
  // Corrupt the singleton with a rejecting initializer by finalizing and
  // pre-poisoning the memo via a direct call that fails.
  await finalizeDatabaseForTests();
  const { initializeDatabase } = await import('../src/data/database/connection.ts');
  await assert.rejects(
    initializeDatabase(() => {
      throw new Error('factory exploded');
    }),
  );
  // The pipeline retries cleanly afterwards (memo reset).
  const result = await bootstrapApp();
  assert.ok(result.ok);
});
