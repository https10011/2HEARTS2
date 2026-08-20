/**
 * Phase 19 — Settings & App Management tests.
 *
 * Covers: settings schema v3 persistence (appearance/notification/app-lock
 * prefs), appearance application helpers, reminder-scheduling gating by
 * notification preferences, DataManagementService (storage report, cache
 * clear, destructive full reset incl. secure-store PIN removal, settings
 * reset, schema-ledger preservation), and error normalization.
 */

import { test, beforeEach } from 'node:test';
import assert from 'node:assert';
import { openMigratedDb } from './helpers.ts';
import { appSettingsStore, applyReduceMotion } from '../src/core/appSettings.ts';
import { defaultSettingsStorage } from '../src/data/settings/settingsStorage.ts';
import { DataManagementService } from '../src/services/maintenance/dataManagementService.ts';
import { MediaStorage } from '../src/data/media/mediaStorage.ts';
import { MemoryFileSystem } from '../src/data/media/memoryFileSystem.ts';
import { RelationshipService } from '../src/services/relationship/relationshipService.ts';
import { MemoryService } from '../src/services/memory/memoryService.ts';
import { ReminderService } from '../src/services/reminder/reminderService.ts';
import { ReminderRepository } from '../src/repositories/reminderRepository.ts';
import { NotificationRegistryRepository } from '../src/services/notifications/notificationRegistryRepository.ts';
import { NotificationService } from '../src/services/notifications/notificationService.ts';
import { MemoryNotificationDriver } from '../src/services/notifications/memoryNotificationDriver.ts';
import { AppLockService } from '../src/services/security/appLockService.ts';
import { MemorySecureStore, SECURE_STORE_KEYS } from '../src/services/security/secureStore.ts';
import { appLifecycle } from '../src/services/lifecycle/appLifecycleService.ts';
import { AppError } from '../src/services/errors/appError.ts';

const CLOCK = () => new Date(1_700_000_000_000);
const PNG = new Uint8Array([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3]);

beforeEach(() => {
  appSettingsStore.reset();
});

// ---------------------------------------------------------------------------
// Settings schema v3: persistence & validation
// ---------------------------------------------------------------------------

test('schema v3 defaults include notification prefs, theme mode, reduce motion', () => {
  const state = appSettingsStore.getState();
  assert.strictEqual(state.schemaVersion, 3);
  assert.strictEqual(state.notificationsEnabled, true);
  assert.strictEqual(state.remindersEnabled, true);
  assert.strictEqual(state.themeMode, 'light');
  assert.strictEqual(state.reduceMotion, false);
});

test('appearance + notification setters persist and validate', () => {
  appSettingsStore.setThemeMode('dark');
  appSettingsStore.setTextSize('extra-large');
  appSettingsStore.setReduceMotion(true);
  appSettingsStore.setNotificationsEnabled(false);
  appSettingsStore.setRemindersEnabled(false);

  const persisted = JSON.parse(defaultSettingsStorage.get('twohearts.settings.v1')!);
  assert.strictEqual(persisted.schemaVersion, 3);
  assert.strictEqual(persisted.themeMode, 'dark');
  assert.strictEqual(persisted.textSize, 'extra-large');
  assert.strictEqual(persisted.reduceMotion, true);
  assert.strictEqual(persisted.notificationsEnabled, false);
  assert.strictEqual(persisted.remindersEnabled, false);

  // Invalid values are rejected, leaving persisted state untouched.
  assert.throws(() => appSettingsStore.setThemeMode('midnight' as never));
  assert.throws(() => appSettingsStore.setTextSize('huge' as never));
  assert.strictEqual(appSettingsStore.getState().themeMode, 'dark');
});

test('applyReduceMotion is a safe no-op without a document (Node/tests)', () => {
  applyReduceMotion(true);
  applyReduceMotion(false);
  assert.ok(true);
});

// ---------------------------------------------------------------------------
// Notification preferences gate reminder scheduling
// ---------------------------------------------------------------------------

function makeReminderStack(db: Awaited<ReturnType<typeof openMigratedDb>>) {
  const driver = new MemoryNotificationDriver();
  const notifications = new NotificationService(driver, new NotificationRegistryRepository(db), CLOCK);
  const service = new ReminderService(new ReminderRepository(db, CLOCK), notifications);
  return { driver, notifications, service };
}

test('reminder scheduling honors the notification master switch', async () => {
  const db = await openMigratedDb();
  const { driver, service } = makeReminderStack(db);
  const input = {
    title: 'Date night',
    scheduledDate: '2099-01-01',
    scheduledTime: '18:00',
    recurrence: 'none' as const,
    notificationEnabled: true,
  };

  appSettingsStore.setNotificationsEnabled(false);
  await service.create(input);
  assert.strictEqual(driver.pending.size, 0); // gated, not scheduled

  appSettingsStore.setNotificationsEnabled(true);
  await service.create(input);
  assert.strictEqual(driver.pending.size, 1); // schedule proceeds when on

  appSettingsStore.setRemindersEnabled(false);
  await service.create(input);
  assert.strictEqual(driver.pending.size, 1); // category switch gates too
  await db.close();
});

// ---------------------------------------------------------------------------
// DataManagementService: storage report
// ---------------------------------------------------------------------------

test('storage report aggregates domain rows, media bytes, pending notifications', async () => {
  const db = await openMigratedDb();
  const media = new MediaStorage(db, new MemoryFileSystem(), CLOCK);
  await media.store('photo', 'image/png', PNG);
  await media.store('photo', 'image/png', PNG);
  await new RelationshipService(db, CLOCK).saveOwner({ displayName: 'Casey' });
  await new MemoryService(db, new MemoryFileSystem(), CLOCK).createMemory({ title: 'Beach day' });
  const { notifications } = makeReminderStack(db);
  await notifications.initialize();
  await notifications.schedule({
    ownerRef: 'reminder:x',
    channelId: 'reminders',
    title: 'Anniversary',
    body: 'Tomorrow',
    fireAt: new Date(1_900_000_000_000),
  });

  const service = new DataManagementService(db, media, notifications, null);
  const report = await service.getStorageReport();

  assert.strictEqual(report.mediaBytes, PNG.length * 2);
  assert.strictEqual(report.pendingNotifications, 1);
  assert.ok(report.domainRows >= 4); // 2 media assets + 1 profile + 1 memory (+registry)
  const tables = new Map(report.tables.map((t) => [t.table, t.rows]));
  assert.strictEqual(tables.get('profiles'), 1);
  assert.strictEqual(tables.get('memories'), 1);
  assert.strictEqual(tables.get('media_assets'), 2);
  assert.ok(!tables.has('schema_migrations')); // ledger never exposed as user data
  await db.close();
});

test('clearCache removes only unreferenced media, keeping saved data', async () => {
  const db = await openMigratedDb();
  const fs = new MemoryFileSystem();
  const media = new MediaStorage(db, fs, CLOCK);
  const referenced = await media.store('photo', 'image/png', PNG);
  const orphan = await media.store('photo', 'image/png', PNG);

  // Simulate an orphan: metadata row removed without its file (crash path).
  await db.run('DELETE FROM media_assets WHERE id = ?', [orphan.id]);

  const service = new DataManagementService(db, media, null, null);
  const result = await service.clearCache();
  assert.strictEqual(result.mediaFilesRemoved, 1);
  assert.ok(await fs.exists(`media/photos/${referenced.id}.png`));
  assert.ok(!(await fs.exists(`media/photos/${orphan.id}.png`)));

  // Saved data untouched: the referenced asset row still exists.
  const rows = await db.query('SELECT * FROM media_assets');
  assert.strictEqual(rows.length, 1);
  await db.close();
});

// ---------------------------------------------------------------------------
// Destructive full reset
// ---------------------------------------------------------------------------

test('resetAllLocalData wipes domain data, media, notifications, PIN, settings — keeping schema', async () => {
  appLifecycle.stop(); // isolate AppLockService lifecycle wiring
  const db = await openMigratedDb();
  const fs = new MemoryFileSystem();
  const media = new MediaStorage(db, fs, CLOCK);
  const secureStore = new MemorySecureStore();
  const appLock = new AppLockService(secureStore, () => ({ enabled: true, timeoutSeconds: 60 }), CLOCK);
  await appLock.initialize();
  await appLock.enable('4321');

  const relationship = new RelationshipService(db, CLOCK);
  await relationship.saveOwner({ displayName: 'Casey' });
  await relationship.setStartDate('2024-02-14');
  await media.store('photo', 'image/png', PNG);
  const { notifications, driver } = makeReminderStack(db);
  await notifications.initialize();
  await notifications.schedule({
    ownerRef: 'reminder:y',
    channelId: 'reminders',
    title: 'Walk',
    body: 'Evening',
    fireAt: new Date(1_900_000_000_000),
  });
  appSettingsStore.set({ themeMode: 'dark', onboardingStage: 'complete', onboarded: true });
  appSettingsStore.markFirstLaunch('2026-02-01T00:00:00.000Z');

  const service = new DataManagementService(db, media, notifications, appLock);
  const result = await service.resetAllLocalData();

  assert.ok(result.tablesCleared > 0);
  assert.strictEqual(result.mediaFilesRemoved, 1);

  // Domain tables emptied.
  for (const table of ['profiles', 'couple_relationship', 'media_assets', 'notification_registry']) {
    const rows = await db.query(`SELECT * FROM "${table}"`);
    assert.strictEqual(rows.length, 0, `${table} must be empty after reset`);
  }
  // Media files physically removed.
  assert.deepStrictEqual(await fs.list('media'), []);

  // Schema ledger + migrations ledger survive — the app must NOT re-run migrations.
  const ledger = await db.query('SELECT name FROM schema_migrations');
  assert.ok(ledger.length > 0);

  // PIN material removed from SecureStore; lock state back to disabled.
  assert.strictEqual(await secureStore.get(SECURE_STORE_KEYS.pinSalt), null);
  assert.strictEqual(await secureStore.get(SECURE_STORE_KEYS.pinVerifier), null);
  assert.strictEqual(appLock.currentState(), 'disabled');

  // Notifications cancelled.
  assert.strictEqual(driver.pending.size, 0);
  assert.strictEqual((await notifications.list()).length, 0);

  // Settings reset to defaults (fresh onboarding), firstLaunchAt preserved.
  const settings = appSettingsStore.getState();
  assert.strictEqual(settings.themeMode, 'light');
  assert.strictEqual(settings.onboardingStage, 'fresh');
  assert.strictEqual(settings.onboarded, false);
  assert.strictEqual(settings.appLockEnabled, false);
  assert.strictEqual(settings.firstLaunchAt, '2026-02-01T00:00:00.000Z');
  await db.close();
});

test('reset failure normalizes to AppError (never raw SQL/paths)', async () => {
  const broken = {
    async query() {
      throw new Error('SQLITE_CANTOPEN: /private/path/db.sqlite');
    },
  } as never;
  const service = new DataManagementService(broken);
  await assert.rejects(
    () => service.getStorageReport(),
    (error: unknown) => {
      assert.ok(error instanceof AppError);
      assert.strictEqual(error.category, 'persistence');
      assert.ok(!error.message.includes('/private/path'));
      return true;
    },
  );
});

// ---------------------------------------------------------------------------
// App Lock integration boundary (settings ↔ AppLockService)
// ---------------------------------------------------------------------------

test('app lock enable/disable flows through AppLockService with only flags in settings', async () => {
  appLifecycle.stop();
  const secureStore = new MemorySecureStore();
  const appLock = new AppLockService(secureStore, () => ({ enabled: true, timeoutSeconds: 0 }), CLOCK);
  await appLock.initialize();

  // Enable via the same calls the Security screen makes.
  await appLock.enable('1234');
  appSettingsStore.set({ appLockEnabled: true, lockTimeoutSeconds: 60 });

  const persisted = JSON.parse(defaultSettingsStorage.get('twohearts.settings.v1')!);
  assert.strictEqual(persisted.appLockEnabled, true);
  assert.strictEqual(persisted.lockTimeoutSeconds, 60);
  // PIN material NEVER in settings/localStorage.
  const raw = defaultSettingsStorage.get('twohearts.settings.v1')!;
  assert.ok(!raw.includes('1234'));
  for (const key of Object.keys(persisted)) {
    assert.ok(!/pin|salt|verifier/i.test(key), `settings must not carry ${key}`);
  }

  // Cold-start semantics: fresh initialize resolves to 'locked'.
  const coldStart = new AppLockService(secureStore, () => ({ enabled: true, timeoutSeconds: 0 }), CLOCK);
  await coldStart.initialize();
  assert.strictEqual(coldStart.currentState(), 'locked');
  assert.ok(await coldStart.unlock('1234'));

  // Disable removes secure material + flips the flag.
  await coldStart.disable();
  appSettingsStore.set({ appLockEnabled: false });
  assert.strictEqual(coldStart.currentState(), 'disabled');
  assert.strictEqual(await secureStore.get(SECURE_STORE_KEYS.pinVerifier), null);
});

test('lock state transitions drive listeners (gate/vault bus) via onLockChange', async () => {
  appLifecycle.stop();
  const secureStore = new MemorySecureStore();
  const appLock = new AppLockService(secureStore, () => ({ enabled: true, timeoutSeconds: 60 }), CLOCK);
  await appLock.initialize();
  await appLock.enable('9999');

  const states: string[] = [];
  const unsub = appLock.onLockChange((state) => states.push(state));
  appLock.lock();
  await appLock.unlock('9999');
  appLock.lock();
  unsub();
  assert.deepStrictEqual(states, ['locked', 'unlocked', 'locked']);
});
