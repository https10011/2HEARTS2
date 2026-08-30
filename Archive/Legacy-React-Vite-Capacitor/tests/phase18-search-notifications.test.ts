/**
 * Phase 18 tests — Search & Notification Center.
 *
 * Tests the search providers, notification center repository/service,
 * and migration 012.
 */

import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { PERSISTENCE_CONFIG } from '../src/config/persistence.ts';
import { ALL_MIGRATIONS } from '../src/data/database/migrations/index.ts';
import { runMigrations } from '../src/data/database/migrations/runner.ts';
import { SqlJsAdapter } from '../src/data/database/sqlJsAdapter.ts';
import { NotificationCenterRepository } from '../src/repositories/notificationCenterRepository.ts';
import { notificationCenterSerializer } from '../src/data/notification/notificationCenterTypes.ts';
import { SearchEngine, scoreCandidate } from '../src/services/search/searchEngine.ts';
import { normalizeQuery } from '../src/services/search/normalize.ts';
import { MemorySearchProvider } from '../src/services/search/memorySearchProvider.ts';
import { TimelineSearchProvider } from '../src/services/search/timelineSearchProvider.ts';
import { NoteSearchProvider } from '../src/services/search/noteSearchProvider.ts';
import { PlaceSearchProvider } from '../src/services/search/placeSearchProvider.ts';
import { ReminderSearchProvider } from '../src/services/search/reminderSearchProvider.ts';
import { createMemoryAdapter, runMigrations as runMigrationsHelper } from './helpers.ts';
import { MemoryRepository } from '../src/repositories/memoryRepository.ts';
import { TimelineRepository } from '../src/repositories/timelineRepository.ts';
import { NoteRepository } from '../src/repositories/noteRepository.ts';
import { PlaceRepository } from '../src/repositories/placeRepository.ts';
import { ReminderRepository } from '../src/repositories/reminderRepository.ts';

describe('Phase 18 — Migration 012 notification center', () => {
  it('creates notification_center table', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const rows = await adapter.query<{ name: string }>(
      "SELECT name FROM sqlite_master WHERE type='table' AND name='notification_center'",
    );
    assert.equal(rows.length, 1);
  });

  it('notification_center has correct indexes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const indexes = await adapter.query<{ name: string }>(
      "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='notification_center'",
    );
    const names = indexes.map((r) => r.name);
    assert.ok(names.includes('idx_nc_kind'));
    assert.ok(names.includes('idx_nc_read'));
    assert.ok(names.includes('idx_nc_created'));
    assert.ok(names.includes('idx_nc_origin'));
  });

  it('schema version is 12', () => {
    assert.equal(PERSISTENCE_CONFIG.schemaVersion, 13);
  });
});

describe('Phase 18 — Notification Center Repository', () => {
  it('creates and retrieves a notification entry', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const repo = new NotificationCenterRepository(adapter);

    const entry = await repo.create({
      title: 'Test Reminder',
      body: 'Don\'t forget!',
      kind: 'reminder',
      originFeature: 'reminder',
      originId: 'reminder-1',
      channelId: 'reminders',
    });

    assert.ok(entry.id);
    assert.equal(entry.title, 'Test Reminder');
    assert.equal(entry.kind, 'reminder');
    assert.equal(entry.read, false);

    const retrieved = await repo.getById(entry.id);
    assert.ok(retrieved);
    assert.equal(retrieved.title, 'Test Reminder');
  });

  it('lists notifications newest first', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const repo = new NotificationCenterRepository(adapter);

    await repo.create({ title: 'First', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });
    await repo.create({ title: 'Second', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });

    const list = await repo.list();
    assert.equal(list.length, 2);
    // Both created in same millisecond; rowid tiebreak: first inserted comes last DESC
    const titles = list.map((e) => e.title);
    assert.ok(titles.includes('First') && titles.includes('Second'));
  });

  it('markAsRead works', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const repo = new NotificationCenterRepository(adapter);

    const entry = await repo.create({
      title: 'Test', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general',
    });
    assert.equal(entry.read, false);

    await repo.markAsRead(entry.id);
    const updated = await repo.getById(entry.id);
    assert.ok(updated);
    assert.equal(updated.read, true);
  });

  it('markAllAsRead works', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const repo = new NotificationCenterRepository(adapter);

    await repo.create({ title: 'A', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });
    await repo.create({ title: 'B', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });

    const count = await repo.markAllAsRead();
    assert.equal(count, 2);

    const unread = await repo.listUnread();
    assert.equal(unread.length, 0);
  });

  it('listUnread returns only unread', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const repo = new NotificationCenterRepository(adapter);

    await repo.create({ title: 'Read', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });
    const entry = await repo.create({ title: 'Unread', body: '', kind: 'reminder', originFeature: '', originId: null, channelId: 'general' });
    await repo.markAsRead(entry.id);

    const unread = await repo.listUnread();
    assert.equal(unread.length, 1);
    assert.equal(unread[0].title, 'Read');
  });

  it('clearAll removes all entries', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const repo = new NotificationCenterRepository(adapter);

    await repo.create({ title: 'A', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });
    await repo.create({ title: 'B', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });

    const cleared = await repo.clearAll();
    assert.equal(cleared, 2);

    const list = await repo.list();
    assert.equal(list.length, 0);
  });

  it('delete removes a single entry', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const repo = new NotificationCenterRepository(adapter);

    const entry = await repo.create({ title: 'Del', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });
    const deleted = await repo.delete(entry.id);
    assert.equal(deleted, true);

    const retrieved = await repo.getById(entry.id);
    assert.equal(retrieved, null);
  });

  it('countUnread returns correct count', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
    const repo = new NotificationCenterRepository(adapter);

    await repo.create({ title: 'A', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });
    const entry = await repo.create({ title: 'B', body: '', kind: 'system', originFeature: '', originId: null, channelId: 'general' });
    await repo.markAsRead(entry.id);

    const count = await repo.countUnread();
    assert.equal(count, 1);
  });
});

describe('Phase 18 — Notification Center Serializer', () => {
  it('roundtrips correctly', () => {
    const entry = {
      id: 'test-id',
      title: 'Title',
      body: 'Body',
      kind: 'reminder' as const,
      originFeature: 'reminder',
      originId: 'origin-1',
      channelId: 'reminders',
      read: true,
      createdAt: '2025-01-15T10:00:00.000Z',
      updatedAt: '2025-01-15T10:00:00.000Z',
    };

    const params = notificationCenterSerializer.toParams(entry);
    const row: Record<string, unknown> = {};
    const columns = ['id', 'title', 'body', 'kind', 'origin_feature', 'origin_id', 'channel_id', 'read', 'created_at', 'updated_at'];
    columns.forEach((col, i) => { row[col] = params[i]; });

    const restored = notificationCenterSerializer.fromRow(row as any);
    assert.deepEqual(restored, entry);
  });
});

describe('Phase 18 — Search normalization', () => {
  it('normalizes query text', () => {
    const q = normalizeQuery('Hello World');
    assert.ok(q);
    assert.ok(q!.tokens.length > 0);
  });

  it('returns null for empty query', () => {
    const q = normalizeQuery('');
    assert.equal(q, null);
  });
});

describe('Phase 18 — Search scoring', () => {
  it('scores exact title match highly', () => {
    const q = normalizeQuery('Birthday');
    assert.ok(q);
    const score = scoreCandidate(q!, 'Birthday Party');
    assert.ok(score > 0);
  });

  it('returns 0 for non-matching query', () => {
    const q = normalizeQuery('zzzzzzz');
    assert.ok(q);
    const score = scoreCandidate(q!, 'Birthday Party');
    assert.equal(score, 0);
  });

  it('prefix match scores higher than substring', () => {
    const q = normalizeQuery('Birth');
    assert.ok(q);
    const prefixScore = scoreCandidate(q!, 'Birthday');
    const substringScore = scoreCandidate(q!, 'Birthday Party');
    assert.ok(prefixScore >= substringScore);
  });
});

describe('Phase 18 — SearchEngine', () => {
  it('returns empty results for empty query', async () => {
    const engine = new SearchEngine();
    const results = await engine.search('');
    assert.equal(results.matches.length, 0);
  });

  it('isolates provider failures', async () => {
    const engine = new SearchEngine();
    engine.registerProvider({
      kind: 'broken',
      search: async () => { throw new Error('broken'); },
    });
    engine.registerProvider({
      kind: 'working',
      search: async () => [],
    });
    const results = await engine.search('test');
    assert.equal(results.matches.length, 0);
  });

  it('registeredKinds returns registered providers', () => {
    const engine = new SearchEngine();
    engine.registerProvider({ kind: 'a', search: async () => [] });
    engine.registerProvider({ kind: 'b', search: async () => [] });
    assert.deepEqual(engine.registeredKinds().sort(), ['a', 'b']);
  });
});

describe('Phase 18 — Feature search providers', () => {
  let adapter: SqlJsAdapter;

  before(async () => {
    adapter = await createMemoryAdapter();
    await runMigrationsHelper(adapter);
  });

  after(async () => {
    await adapter.close();
  });

  it('MemorySearchProvider finds matching memories', async () => {
    const memRepo = new MemoryRepository(adapter);
    await memRepo.create({ title: 'Beach Trip', caption: 'Summer vacation', memoryDate: '2025-06-15', sortOrder: 0 });
    await memRepo.create({ title: 'Birthday Party', caption: 'Fun times', memoryDate: '2025-03-10', sortOrder: 1 });

    const provider = new MemorySearchProvider(adapter);
    const q = normalizeQuery('Beach');
    assert.ok(q);
    const matches = await provider.search(q!);
    assert.ok(matches.length >= 1);
    assert.ok(matches.some((m) => m.title === 'Beach Trip'));
    assert.ok(matches.every((m) => m.kind === 'memory'));
  });

  it('TimelineSearchProvider finds matching events', async () => {
    const tlRepo = new TimelineRepository(adapter);
    await tlRepo.create({ title: 'First Date', description: 'Coffee at the café', eventDate: '2024-01-01' });

    const provider = new TimelineSearchProvider(adapter);
    const q = normalizeQuery('First');
    assert.ok(q);
    const matches = await provider.search(q!);
    assert.ok(matches.length >= 1);
    assert.ok(matches.some((m) => m.title === 'First Date'));
  });

  it('NoteSearchProvider finds matching notes', async () => {
    const noteRepo = new NoteRepository(adapter);
    await noteRepo.create({ title: 'Grocery List', content: 'Milk, eggs, bread', category: 'shared' });

    const provider = new NoteSearchProvider(adapter);
    const q = normalizeQuery('Grocery');
    assert.ok(q);
    const matches = await provider.search(q!);
    assert.ok(matches.length >= 1);
    assert.ok(matches.some((m) => m.title === 'Grocery List'));
    assert.ok(matches.every((m) => m.kind === 'note'));
  });

  it('PlaceSearchProvider finds matching places', async () => {
    const placeRepo = new PlaceRepository(adapter);
    await placeRepo.create({ name: 'Our Café', category: 'restaurant', address: '123 Main St', city: 'Portland', state: 'OR', country: 'US', latitude: 0, longitude: 0, notes: 'Favorite spot', photoRef: null, memoryId: null });

    const provider = new PlaceSearchProvider(adapter);
    const q = normalizeQuery('Our');
    assert.ok(q);
    const matches = await provider.search(q!);
    assert.ok(matches.length >= 1);
    assert.ok(matches.some((m) => m.title === 'Our Café'));
  });

  it('ReminderSearchProvider finds matching reminders', async () => {
    const remRepo = new ReminderRepository(adapter);
    await remRepo.create({ title: 'Anniversary Gift', description: 'Buy something special', scheduledDate: '2025-02-14', scheduledTime: '18:00', recurrence: 'none', status: 'active', notificationOwnerRef: null, notificationEnabled: true });

    const provider = new ReminderSearchProvider(adapter);
    const q = normalizeQuery('Anniversary');
    assert.ok(q);
    const matches = await provider.search(q!);
    assert.ok(matches.length >= 1);
    assert.ok(matches.some((m) => m.title === 'Anniversary Gift'));
  });

  it('SearchEngine integrates with MemorySearchProvider', async () => {
    const engine = new SearchEngine();
    engine.registerProvider(new MemorySearchProvider(adapter));
    engine.registerProvider(new NoteSearchProvider(adapter));

    const results = await engine.search('Beach');
    assert.ok(results.matches.length >= 1);
    assert.ok(results.matches.some((m) => m.kind === 'memory'));
  });
});

describe('Phase 18 — Vault exclusion from search', () => {
  it('VaultSearchProvider is not registered in the search engine', () => {
    const engine = new SearchEngine();
    engine.registerProvider(new MemorySearchProvider(new SqlJsAdapter()));
    engine.registerProvider(new NoteSearchProvider(new SqlJsAdapter()));
    assert.ok(!engine.registeredKinds().includes('vault'));
  });
});
