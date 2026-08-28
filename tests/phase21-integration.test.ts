/**
 * Phase 21 — Integration, QA & Hardening tests.
 *
 * Exercises the COMPLETE V1 application as one integrated system at the
 * service/repository layer over a real (sql.js) database — no mocks:
 *
 *   UI hooks → Services → Repositories → Local Persistence
 *
 * Covers: first-launch → onboarding → main-app state machine (incl.
 * interrupted/resumed setup), cross-feature data creation with global
 * search (vault exclusion), reminder ↔ notification coordination and
 * settings gating, app-lock + vault access control (incl. cold-start
 * relock), persistence across "restarts", upgrade/migration with existing
 * user data, graceful handling of missing/corrupt/invalid data, large
 * datasets, and the destructive full-reset flow.
 */

import { test, beforeEach } from 'node:test';
import assert from 'node:assert/strict';

import { createMemoryAdapter, openMigratedDb, runMigrations } from './helpers.ts';
import { ALL_MIGRATIONS } from '../src/data/database/migrations/index.ts';

import { appSettingsStore } from '../src/core/appSettings.ts';
import { AppStateService } from '../src/services/state/appStateService.ts';
import { RelationshipService } from '../src/services/relationship/relationshipService.ts';
import { MemoryService } from '../src/services/memory/memoryService.ts';
import { NoteService } from '../src/services/note/noteService.ts';
import { TimelineService } from '../src/services/timeline/timelineService.ts';
import { PlaceService } from '../src/services/place/placeService.ts';
import { MoodService } from '../src/services/mood/moodService.ts';
import { PeriodService } from '../src/services/period/periodService.ts';
import { ReminderService } from '../src/services/reminder/reminderService.ts';
import { VaultService } from '../src/services/vault/vaultService.ts';
import { DataManagementService } from '../src/services/maintenance/dataManagementService.ts';
import { NotificationService } from '../src/services/notifications/notificationService.ts';
import { NotificationRegistryRepository } from '../src/services/notifications/notificationRegistryRepository.ts';
import { MemoryNotificationDriver } from '../src/services/notifications/memoryNotificationDriver.ts';
import { AppLockService } from '../src/services/security/appLockService.ts';
import { MemorySecureStore, SECURE_STORE_KEYS } from '../src/services/security/secureStore.ts';
import { AppError } from '../src/services/errors/appError.ts';

import { PlaceRepository } from '../src/repositories/placeRepository.ts';
import { MoodRepository } from '../src/repositories/moodRepository.ts';
import { PeriodRepository } from '../src/repositories/periodRepository.ts';
import { ReminderRepository } from '../src/repositories/reminderRepository.ts';
import { VaultRepository } from '../src/repositories/vaultRepository.ts';
import { ProfileRepository } from '../src/repositories/profileRepository.ts';

import { MemoryFileSystem } from '../src/data/media/memoryFileSystem.ts';
import { MediaStorage } from '../src/data/media/mediaStorage.ts';

import { SearchEngine } from '../src/services/search/searchEngine.ts';
import { MemorySearchProvider } from '../src/services/search/memorySearchProvider.ts';
import { NoteSearchProvider } from '../src/services/search/noteSearchProvider.ts';
import { TimelineSearchProvider } from '../src/services/search/timelineSearchProvider.ts';
import { PlaceSearchProvider } from '../src/services/search/placeSearchProvider.ts';
import { ReminderSearchProvider } from '../src/services/search/reminderSearchProvider.ts';

const CLOCK = () => new Date(1_700_000_000_000); // 2023-11-14T22:13:20Z
const PNG = new Uint8Array([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3]);

/**
 * Local 'yyyy-mm-dd' key N days from REAL now. Notification scheduling
 * correctly refuses past fire times (compared against the real clock), so
 * scheduling tests must use genuinely future dates.
 */
function futureDateKey(daysFromNow: number): string {
  const d = new Date(Date.now() + daysFromNow * 86_400_000);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

beforeEach(() => {
  appSettingsStore.reset();
});

/** Wires the full global search engine exactly as the Search screen does. */
function buildSearchEngine(db: unknown): SearchEngine {
  const adapter = db as ConstructorParameters<typeof MemorySearchProvider>[0];
  const engine = new SearchEngine();
  engine.registerProvider(new MemorySearchProvider(adapter));
  engine.registerProvider(new NoteSearchProvider(adapter));
  engine.registerProvider(new TimelineSearchProvider(adapter));
  engine.registerProvider(new PlaceSearchProvider(adapter));
  engine.registerProvider(new ReminderSearchProvider(adapter));
  return engine;
}

// ---------------------------------------------------------------------------
// First launch → onboarding → main app (domain-truth state machine)
// ---------------------------------------------------------------------------

test('integration: full onboarding flow derives stages from domain truth', async () => {
  const db = await openMigratedDb();
  const appState = new AppStateService(db, CLOCK);
  const relationship = new RelationshipService(db, CLOCK);

  // First launch: stamped once, stage reflects missing owner profile.
  appState.markFirstLaunchIfNeeded();
  const firstLaunchAt = appState.getSnapshot().firstLaunchAt;
  assert.ok(firstLaunchAt);
  assert.strictEqual(await appState.reconcileOnboardingStage(), 'fresh'); // no owner → Welcome screen

  // Owner profile done → next unmet step is the relationship.
  await relationship.saveOwner({ displayName: 'Alex', birthDate: '1995-04-12' });
  assert.strictEqual(await appState.reconcileOnboardingStage(), 'relationship');

  // Partner without start date is still incomplete.
  await relationship.savePartner({ displayName: 'Sam' });
  assert.strictEqual(await appState.reconcileOnboardingStage(), 'relationship');

  await relationship.setStartDate('2020-06-01');
  assert.strictEqual(await appState.reconcileOnboardingStage(), 'personalization');

  // Terminal step verifies domain truth, then never downgrades.
  await appState.completeSetup();
  assert.strictEqual(appState.getSnapshot().onboardingStage, 'complete');
  assert.strictEqual(await appState.reconcileOnboardingStage(), 'complete');
  assert.strictEqual(appState.getSnapshot().firstLaunchAt, firstLaunchAt);
});

test('integration: interrupted onboarding resumes at the persisted domain stage', async () => {
  const db = await openMigratedDb();
  const relationship = new RelationshipService(db, CLOCK);
  await relationship.saveOwner({ displayName: 'Alex' });

  // "Restart": brand-new service instances over the same database must
  // resume where setup actually is — not where UI state claims it is.
  const restartedAppState = new AppStateService(db, CLOCK);
  restartedAppState.markFirstLaunchIfNeeded();
  assert.strictEqual(await restartedAppState.reconcileOnboardingStage(), 'relationship');
});

test('integration: completeSetup refuses while domain is incomplete', async () => {
  const db = await openMigratedDb();
  const appState = new AppStateService(db, CLOCK);
  await assert.rejects(() => appState.completeSetup());
  assert.notStrictEqual(appSettingsStore.getState().onboardingStage, 'complete');
});

// ---------------------------------------------------------------------------
// Cross-feature data + global search (vault excluded)
// ---------------------------------------------------------------------------

test('integration: every feature persists and global search finds it — except the vault', async () => {
  const db = await openMigratedDb();
  const fs = new MemoryFileSystem();

  const memories = new MemoryService(db, fs, CLOCK);
  const notes = new NoteService(db, CLOCK);
  const timeline = new TimelineService(db, CLOCK);
  const places = new PlaceService(new PlaceRepository(db));
  const moods = new MoodService(new MoodRepository(db));
  const periods = new PeriodService(new PeriodRepository(db));
  const reminders = new ReminderService(new ReminderRepository(db, CLOCK), null);

  const secureStore = new MemorySecureStore();
  const appLock = new AppLockService(secureStore, () => ({ enabled: true, timeoutSeconds: 60 }), CLOCK);
  await appLock.initialize();
  await appLock.enable('1234');
  const vault = new VaultService(new VaultRepository(db), appLock);

  // Create one record per feature, all sharing the word "beach".
  const memory = await memories.createMemory(
    { title: 'Beach day', caption: 'Sunset walk', memoryDate: '2023-06-15' },
    [{ kind: 'photo', mimeType: 'image/png', data: PNG }],
  );
  assert.strictEqual(memory.mediaReferences.length, 1);

  await notes.createNote({ title: 'Beach packing list', content: 'Towels', category: 'shared' });
  await timeline.createEvent({ title: 'First beach trip', eventDate: '2021-07-04' });
  await places.create({ name: 'Beach Café', city: 'Portland' });
  await reminders.create({
    title: 'Beach anniversary',
    scheduledDate: '2023-12-01',
    scheduledTime: '09:00',
    recurrence: 'yearly',
    notificationEnabled: false,
  });
  const mood = await moods.record({ moodValue: 'happy', profileId: 'owner', entryDate: '2023-11-14' });
  assert.ok(mood.id);
  const period = await periods.logPeriod({ startDate: '2023-11-01', flowLevel: 'medium', profileId: 'owner' });
  assert.ok(period.id);
  await vault.create({ title: 'Beach secret letter', contentType: 'note', content: 'hidden', profileId: 'owner' });

  const engine = buildSearchEngine(db);
  const results = await engine.search('beach');
  const kinds = new Set(results.matches.map((m) => m.kind));
  assert.ok(kinds.has('memory'), 'memory searchable');
  assert.ok(kinds.has('note'), 'note searchable');
  assert.ok(kinds.has('timeline'), 'timeline searchable');
  assert.ok(kinds.has('place'), 'place searchable');
  assert.ok(kinds.has('reminder'), 'reminder searchable');

  // Vault content must never leak into global search.
  assert.ok(
    results.matches.every((m) => m.title !== 'Beach secret letter'),
    'vault content excluded from global search',
  );
  const vaultOnly = await engine.search('secret letter');
  assert.strictEqual(vaultOnly.matches.length, 0);

  // No-match queries return empty, not an error.
  const none = await engine.search('zzzz-no-such-thing');
  assert.strictEqual(none.matches.length, 0);
});

// ---------------------------------------------------------------------------
// Reminders ↔ notification scheduling coordination
// ---------------------------------------------------------------------------

test('integration: reminder lifecycle schedules, reschedules and cancels notifications', async () => {
  const db = await openMigratedDb();
  const driver = new MemoryNotificationDriver();
  const notifications = new NotificationService(driver, new NotificationRegistryRepository(db), CLOCK);
  await notifications.initialize();
  const reminders = new ReminderService(new ReminderRepository(db, CLOCK), notifications);

  const reminder = await reminders.create({
    title: 'Dinner reservation',
    scheduledDate: futureDateKey(2),
    scheduledTime: '19:00',
    recurrence: 'none',
    notificationEnabled: true,
  });
  assert.strictEqual(driver.pending.size, 1);

  // Edit → exactly one pending notification, at the new time.
  await reminders.update(reminder.id, { scheduledTime: '20:30' });
  assert.strictEqual(driver.pending.size, 1);
  const fireAt = [...driver.pending.values()][0].fireAt;
  assert.ok(fireAt.getHours() === 20 && fireAt.getMinutes() === 30);

  // Complete → cancelled.
  await reminders.complete(reminder.id);
  assert.strictEqual(driver.pending.size, 0);

  // Recreate then delete → cancelled.
  const second = await reminders.create({
    title: 'Call the florist',
    scheduledDate: futureDateKey(3),
    scheduledTime: '08:00',
    recurrence: 'none',
    notificationEnabled: true,
  });
  assert.strictEqual(driver.pending.size, 1);
  await reminders.delete(second.id);
  assert.strictEqual(driver.pending.size, 0);
});

test('integration: notification master switch gates reminder scheduling', async () => {
  const db = await openMigratedDb();
  const driver = new MemoryNotificationDriver();
  const notifications = new NotificationService(driver, new NotificationRegistryRepository(db), CLOCK);
  await notifications.initialize();
  const reminders = new ReminderService(new ReminderRepository(db, CLOCK), notifications);

  appSettingsStore.setNotificationsEnabled(false);
  await reminders.create({
    title: 'Muted reminder',
    scheduledDate: futureDateKey(2),
    scheduledTime: '10:00',
    recurrence: 'none',
    notificationEnabled: true,
  });
  assert.strictEqual(driver.pending.size, 0, 'no scheduling while notifications are disabled');

  appSettingsStore.setNotificationsEnabled(true);
  await reminders.create({
    title: 'Audible reminder',
    scheduledDate: futureDateKey(2),
    scheduledTime: '10:00',
    recurrence: 'none',
    notificationEnabled: true,
  });
  assert.strictEqual(driver.pending.size, 1);
});

// ---------------------------------------------------------------------------
// App lock + vault access control
// ---------------------------------------------------------------------------

test('integration: vault enforces lock state across the full PIN lifecycle', async () => {
  const db = await openMigratedDb();
  const secureStore = new MemorySecureStore();
  const appLock = new AppLockService(secureStore, () => ({ enabled: true, timeoutSeconds: 60 }), CLOCK);
  await appLock.initialize();
  const vault = new VaultService(new VaultRepository(db), appLock);

  // Lock disabled → vault closed to writes? No: disabled means accessible.
  assert.strictEqual(appLock.currentState(), 'disabled');

  await appLock.enable('2580');
  assert.strictEqual(appLock.currentState(), 'unlocked');
  const item = await vault.create({ title: 'Private note', contentType: 'note', content: 'for us', profileId: 'owner' });
  assert.ok(item.id);

  appLock.lock();
  assert.strictEqual(appLock.currentState(), 'locked');
  await assert.rejects(
    () => vault.create({ title: 'Nope', contentType: 'note', profileId: 'owner' }),
    (err) => err instanceof AppError && err.code === 'vault-locked',
  );
  // Reads fail closed: a locked vault returns nothing rather than content.
  assert.deepStrictEqual(await vault.list('owner'), [], 'locked vault lists nothing');
  assert.strictEqual(await vault.getById(item.id), null, 'locked vault hides items');

  assert.strictEqual(await appLock.unlock('0000'), false, 'wrong PIN rejected');
  assert.strictEqual(appLock.currentState(), 'locked');
  assert.strictEqual(await appLock.unlock('2580'), true, 'correct PIN unlocks');
  assert.strictEqual((await vault.list('owner')).length, 1);
});

test('integration: cold start re-locks when app lock is enabled', async () => {
  const db = await openMigratedDb();
  const secureStore = new MemorySecureStore();
  const first = new AppLockService(secureStore, () => ({ enabled: true, timeoutSeconds: 60 }), CLOCK);
  await first.initialize();
  await first.enable('2580');
  assert.strictEqual(first.currentState(), 'unlocked');
  const warmVault = new VaultService(new VaultRepository(db), first);
  await warmVault.create({ title: 'Persisted secret', contentType: 'note', content: 'x', profileId: 'owner' });

  // "Cold start": new service over the same secure store — memory-only lock
  // state means the app comes up locked even though the session was unlocked.
  const coldStart = new AppLockService(secureStore, () => ({ enabled: true, timeoutSeconds: 60 }), CLOCK);
  assert.strictEqual(await coldStart.initialize(), 'locked');
  const vault = new VaultService(new VaultRepository(db), coldStart);
  assert.deepStrictEqual(await vault.list('owner'), [], 'cold start hides vault content');
  assert.strictEqual(await coldStart.unlock('2580'), true);
  assert.strictEqual((await vault.list('owner')).length, 1, 'content intact after re-unlock');
});

// ---------------------------------------------------------------------------
// Persistence, restart and upgrade scenarios
// ---------------------------------------------------------------------------

test('integration: data survives an app restart (new services, same database)', async () => {
  const db = await openMigratedDb();
  const notes = new NoteService(db, CLOCK);
  const created = await notes.createNote({ title: 'Vows draft', content: 'Forever', category: 'private' });

  // "Relaunch": fresh service instances reading the same database.
  const restartedNotes = new NoteService(db, CLOCK);
  const loaded = await restartedNotes.getNote(created.id);
  assert.strictEqual(loaded.title, 'Vows draft');
  assert.strictEqual(loaded.content, 'Forever');
});

test('integration: upgrade from schema v3 preserves existing user data', async () => {
  const db = await createMemoryAdapter();
  // Simulate an existing user whose install only has the first 3 migrations.
  await runMigrations(db, ALL_MIGRATIONS.filter((m) => m.id <= 3));
  const profiles = new ProfileRepository(db, CLOCK);
  await profiles.create({ role: 'owner', displayName: 'Alex', birthDate: '1995-04-12', deletedAt: null });

  // Upgrade: remaining migrations apply; old data is untouched.
  await runMigrations(db, ALL_MIGRATIONS);
  const owner = await profiles.getOwner();
  assert.strictEqual(owner?.displayName, 'Alex');

  // New-schema features work on the upgraded database.
  const notes = new NoteService(db, CLOCK);
  const note = await notes.createNote({ title: 'Post-upgrade note' });
  assert.ok(note.id);

  const ledger = await db.query<{ id: number }>('SELECT id FROM schema_migrations ORDER BY id');
  assert.deepStrictEqual(ledger.map((r) => r.id), ALL_MIGRATIONS.map((m) => m.id));
});

test('integration: re-running migrations on a current database is a no-op', async () => {
  const db = await openMigratedDb();
  const before = await db.query<{ id: number; applied_at: string }>(
    'SELECT id, applied_at FROM schema_migrations ORDER BY id',
  );
  await runMigrations(db, ALL_MIGRATIONS);
  const after = await db.query<{ id: number; applied_at: string }>(
    'SELECT id, applied_at FROM schema_migrations ORDER BY id',
  );
  assert.deepStrictEqual(after, before);
});

// ---------------------------------------------------------------------------
// Error & edge cases — fail gracefully, never crash
// ---------------------------------------------------------------------------

test('integration: invalid input is rejected with safe, normalized errors', async () => {
  const db = await openMigratedDb();
  const memories = new MemoryService(db, new MemoryFileSystem(), CLOCK);
  const notes = new NoteService(db, CLOCK);
  const reminders = new ReminderService(new ReminderRepository(db, CLOCK), null);

  await assert.rejects(
    () => memories.createMemory({ title: 'Bad date', memoryDate: '2023-13-40' }),
    (err) => err instanceof AppError && err.category === 'validation',
  );
  await assert.rejects(
    () => memories.createMemory({ title: '', memoryDate: '2023-01-01' }),
    (err) => err instanceof AppError,
  );
  await assert.rejects(() => notes.createNote({ title: '   ' }), (err) => err instanceof AppError);
  await assert.rejects(
    () =>
      reminders.create({
        title: 'Broken',
        scheduledDate: 'not-a-date',
        scheduledTime: '10:00',
        recurrence: 'none',
        notificationEnabled: false,
      }),
    (err) => err instanceof AppError,
  );

  // Safe user messages never leak internals.
  try {
    await memories.createMemory({ title: '', memoryDate: null });
    assert.fail('expected validation failure');
  } catch (err) {
    assert.ok(err instanceof AppError);
    assert.ok(err.userMessage.length > 0);
    assert.ok(!/sqlite|sql|stack|undefined/i.test(err.userMessage));
  }
});

test('integration: memory with a deleted media reference still loads', async () => {
  const db = await openMigratedDb();
  const memories = new MemoryService(db, new MemoryFileSystem(), CLOCK);
  const memory = await memories.createMemory(
    { title: 'Photo memory', memoryDate: '2023-05-01' },
    [{ kind: 'photo', mimeType: 'image/png', data: PNG }],
  );
  assert.strictEqual(memory.mediaReferences.length, 1);

  // Simulate lost media metadata (e.g. partial reset, manual file damage).
  await db.run('DELETE FROM media_assets');

  const loaded = await memories.getMemory(memory.id);
  assert.strictEqual(loaded.title, 'Photo memory');
  assert.strictEqual(loaded.mediaReferences.length, 0, 'missing media skipped gracefully');

  const listed = await memories.listMemories();
  assert.strictEqual(listed.length, 1);
});

test('integration: large datasets stay correct across list and search', async () => {
  const db = await openMigratedDb();
  const notes = new NoteService(db, CLOCK);
  const memories = new MemoryService(db, new MemoryFileSystem(), CLOCK);

  for (let i = 0; i < 150; i += 1) {
    await notes.createNote({ title: `Note ${i}`, content: i % 10 === 0 ? 'anniversary idea' : 'misc' });
  }
  for (let i = 0; i < 50; i += 1) {
    await memories.createMemory({ title: `Memory ${i}`, memoryDate: '2023-01-01' });
  }

  assert.strictEqual((await notes.listNotes()).length, 150);
  assert.strictEqual((await memories.listMemories()).length, 50);

  const engine = buildSearchEngine(db);
  const results = await engine.search('anniversary');
  assert.strictEqual(results.matches.length, 15);
  assert.ok(results.matches.every((m) => m.kind === 'note'));
});

// ---------------------------------------------------------------------------
// Destructive reset — back to a clean first-launch-capable state
// ---------------------------------------------------------------------------

test('integration: full reset wipes domain data but preserves install identity', async () => {
  const db = await openMigratedDb();
  const fs = new MemoryFileSystem();
  const media = new MediaStorage(db, fs);
  const driver = new MemoryNotificationDriver();
  const notifications = new NotificationService(driver, new NotificationRegistryRepository(db), CLOCK);
  await notifications.initialize();
  const secureStore = new MemorySecureStore();
  const appLock = new AppLockService(secureStore, () => ({ enabled: true, timeoutSeconds: 60 }), CLOCK);
  await appLock.initialize();
  await appLock.enable('2580');

  const appState = new AppStateService(db, CLOCK);
  const relationship = new RelationshipService(db, CLOCK);
  appState.markFirstLaunchIfNeeded();
  const firstLaunchAt = appState.getSnapshot().firstLaunchAt;
  await relationship.saveOwner({ displayName: 'Alex' });
  await relationship.savePartner({ displayName: 'Sam' });
  await relationship.setStartDate('2020-06-01');
  await appState.completeSetup();

  const memories = new MemoryService(db, fs, CLOCK);
  await memories.createMemory({ title: 'To be wiped', memoryDate: '2023-01-01' }, [
    { kind: 'photo', mimeType: 'image/png', data: PNG },
  ]);
  const reminders = new ReminderService(new ReminderRepository(db, CLOCK), notifications);
  await reminders.create({
    title: 'Wipe me',
    scheduledDate: futureDateKey(2),
    scheduledTime: '10:00',
    recurrence: 'none',
    notificationEnabled: true,
  });
  assert.strictEqual(driver.pending.size, 1);

  const management = new DataManagementService(db, media, notifications, appLock);
  await management.resetAllLocalData();

  // Domain data gone, notifications cancelled, PIN removed.
  assert.strictEqual((await memories.listMemories()).length, 0);
  assert.strictEqual((await reminders.list()).length, 0);
  assert.strictEqual(driver.pending.size, 0);
  assert.strictEqual(await secureStore.get(SECURE_STORE_KEYS.pinHash), null);
  assert.strictEqual((await relationship.getSummary()).owner, null);

  // Install identity survives: migration ledger + first-launch stamp.
  const ledger = await db.query<{ id: number }>('SELECT id FROM schema_migrations');
  assert.strictEqual(ledger.length, ALL_MIGRATIONS.length);
  assert.strictEqual(appSettingsStore.getState().firstLaunchAt, firstLaunchAt);

  // The app is back on the onboarding path, derived from domain truth.
  assert.notStrictEqual(appSettingsStore.getState().onboardingStage, 'complete');
  assert.strictEqual(await appState.reconcileOnboardingStage(), 'fresh'); // no owner in DB → Welcome screen
});
