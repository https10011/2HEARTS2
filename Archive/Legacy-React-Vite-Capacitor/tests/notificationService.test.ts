import { test } from 'node:test';
import assert from 'node:assert';
import { openMigratedDb } from './helpers.ts';
import { NotificationRegistryRepository } from '../src/services/notifications/notificationRegistryRepository.ts';
import { NotificationService, NOTIFICATION_CHANNELS } from '../src/services/notifications/notificationService.ts';
import { MemoryNotificationDriver } from '../src/services/notifications/memoryNotificationDriver.ts';

function makeService() {
  return openMigratedDb().then((db) => {
    const driver = new MemoryNotificationDriver();
    const registry = new NotificationRegistryRepository(db);
    let tick = 0;
    const clock = () => new Date(1_700_000_000_000 + tick++ * 1000);
    const service = new NotificationService(driver, registry, clock);
    return { db, driver, registry, service };
  });
}

test('initialize creates the V1 channel set (idempotent)', async () => {
  const { driver, service } = await makeService();
  await service.initialize();
  await service.initialize();
  assert.deepStrictEqual(
    driver.channels.map((c) => c.id).sort(),
    NOTIFICATION_CHANNELS.map((c) => c.id).sort(),
  );
});

test('schedule assigns sequential platform ids and records metadata', async () => {
  const { service, registry, driver } = await makeService();
  await service.initialize();

  const first = await service.schedule({
    ownerRef: 'reminder:1',
    channelId: 'reminders',
    title: 'Anniversary',
    body: 'Tomorrow!',
    fireAt: new Date(1_800_000_000_000),
    metadata: { feature: 'reminders' },
  });
  const second = await service.schedule({
    ownerRef: 'reminder:2',
    channelId: 'reminders',
    title: 'Date night',
    body: 'Friday',
    fireAt: new Date(1_800_000_100_000),
  });

  assert.strictEqual(first.notificationId, 1);
  assert.strictEqual(second.notificationId, 2);
  assert.strictEqual(driver.pending.size, 2);

  const entry = await registry.getByOwner('reminder:1');
  assert.ok(entry);
  assert.strictEqual(entry.notificationId, 1);
  assert.deepStrictEqual(entry.metadata, { feature: 'reminders' });
  assert.strictEqual(entry.scheduledAt, new Date(1_800_000_000_000).toISOString());
});

test('re-scheduling the same owner replaces, not duplicates (upsert)', async () => {
  const { service, registry, driver } = await makeService();
  await service.initialize();

  await service.schedule({
    ownerRef: 'anniv:1',
    channelId: 'anniversaries',
    title: 'Old',
    body: 'Old time',
    fireAt: new Date(1_800_000_000_000),
  });
  const rescheduled = await service.schedule({
    ownerRef: 'anniv:1',
    channelId: 'anniversaries',
    title: 'New',
    body: 'New time',
    fireAt: new Date(1_800_000_200_000),
  });

  assert.strictEqual(rescheduled.notificationId, 2);
  assert.strictEqual(driver.pending.size, 1);
  assert.strictEqual(driver.pending.get(2)?.title, 'New');
  assert.strictEqual(driver.pending.has(1), false);
  assert.strictEqual((await registry.list()).length, 1);
});

test('cancelByOwner removes platform and registry entries', async () => {
  const { service, registry, driver } = await makeService();
  await service.initialize();
  const handle = await service.schedule({
    ownerRef: 'reminder:x',
    channelId: 'reminders',
    title: 'T',
    body: 'B',
    fireAt: new Date(1_800_000_000_000),
  });
  await service.cancelByOwner('reminder:x');
  assert.strictEqual(driver.pending.size, 0);
  assert.strictEqual(await registry.getById(handle.notificationId), null);
  // idempotent second cancel
  await service.cancelByOwner('reminder:x');
});

test('cancelById and cancelAll work', async () => {
  const { service, registry } = await makeService();
  await service.initialize();
  const a = await service.schedule({
    ownerRef: 'a', channelId: 'general', title: 'A', body: 'A', fireAt: new Date(1_800_000_000_000),
  });
  await service.schedule({
    ownerRef: 'b', channelId: 'general', title: 'B', body: 'B', fireAt: new Date(1_800_000_100_000),
  });
  await service.cancelById(a.notificationId);
  assert.strictEqual((await registry.list()).length, 1);
  await service.cancelAll();
  assert.strictEqual((await registry.list()).length, 0);
});

test('scheduling with an unknown channel is rejected', async () => {
  const { service } = await makeService();
  await service.initialize();
  await assert.rejects(
    service.schedule({
      ownerRef: 'x', channelId: 'nope', title: 'T', body: 'B', fireAt: new Date(1_800_000_000_000),
    }),
    (error: unknown) => error instanceof Error && (error as { code?: string }).code === 'unknown-channel',
  );
});

test('reconcile prunes registry rows the OS no longer has pending', async () => {
  const { service, registry, driver } = await makeService();
  await service.initialize();
  await service.schedule({
    ownerRef: 'keep', channelId: 'general', title: 'K', body: 'K', fireAt: new Date(1_800_000_000_000),
  });
  const dropped = await service.schedule({
    ownerRef: 'drop', channelId: 'general', title: 'D', body: 'D', fireAt: new Date(1_800_000_100_000),
  });
  driver.simulateDelivered(dropped.notificationId);

  const pruned = await service.reconcile();
  assert.strictEqual(pruned, 1);
  assert.ok(await registry.getByOwner('keep'));
  assert.strictEqual(await registry.getByOwner('drop'), null);
});
