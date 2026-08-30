/**
 * Phase 15 — Mood Tests
 *
 * Tests the mood data model, migration, repository, service,
 * validation, serialization, mood tracking, and edge cases.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

import {
  type MoodEntry,
  type MoodValue,
  MOOD_VALUES,
  MOOD_EMOJI,
  MOOD_LABELS,
  MOOD_COLUMNS,
  moodSerializer,
  assertMoodEntry,
} from '../src/data/mood/moodTypes.ts';
import { createMemoryAdapter, runMigrations } from './helpers.ts';
import { MoodRepository } from '../src/repositories/moodRepository.ts';
import { MoodService } from '../src/services/mood/moodService.ts';

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

describe('Mood data model', () => {
  it('has correct mood values', () => {
    assert.equal(MOOD_VALUES.length, 10);
    assert.ok(MOOD_VALUES.includes('happy'));
    assert.ok(MOOD_VALUES.includes('love'));
    assert.ok(MOOD_VALUES.includes('sad'));
  });

  it('has emoji for every mood value', () => {
    for (const mood of MOOD_VALUES) {
      assert.ok(MOOD_EMOJI[mood], `Missing emoji for ${mood}`);
    }
  });

  it('has labels for every mood value', () => {
    for (const mood of MOOD_VALUES) {
      assert.ok(MOOD_LABELS[mood], `Missing label for ${mood}`);
    }
  });

  it('assertMoodEntry accepts valid entry', () => {
    const entry: MoodEntry = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      moodValue: 'happy',
      moodEmoji: '😊',
      note: 'Great day!',
      profileId: 'owner-1',
      entryDate: '2026-08-20',
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.doesNotThrow(() => assertMoodEntry(entry));
  });

  it('assertMoodEntry rejects invalid mood value', () => {
    const entry: MoodEntry = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      moodValue: 'invalid' as MoodValue,
      moodEmoji: '❓',
      note: null,
      profileId: 'owner-1',
      entryDate: '2026-08-20',
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.throws(() => assertMoodEntry(entry), /moodValue must be one of/);
  });

  it('assertMoodEntry rejects invalid date format', () => {
    const entry: MoodEntry = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      moodValue: 'happy',
      moodEmoji: '😊',
      note: null,
      profileId: 'owner-1',
      entryDate: 'not-a-date',
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.throws(() => assertMoodEntry(entry), /entryDate must be a yyyy-mm-dd/);
  });
});

// ---------------------------------------------------------------------------
// Serializer
// ---------------------------------------------------------------------------

describe('Mood serializer', () => {
  it('has correct column count', () => {
    assert.equal(MOOD_COLUMNS.length, 9);
  });

  it('round-trips through toParams/fromRow', () => {
    const entry: MoodEntry = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      moodValue: 'love',
      moodEmoji: '❤️',
      note: 'Feeling loved',
      profileId: 'owner-1',
      entryDate: '2026-08-20',
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };

    const params = moodSerializer.toParams(entry);
    const row: Record<string, unknown> = {};
    MOOD_COLUMNS.forEach((col, i) => {
      row[col] = params[i];
    });

    const restored = moodSerializer.fromRow(row as any);
    assert.equal(restored.id, entry.id);
    assert.equal(restored.moodValue, entry.moodValue);
    assert.equal(restored.moodEmoji, entry.moodEmoji);
    assert.equal(restored.note, entry.note);
    assert.equal(restored.profileId, entry.profileId);
    assert.equal(restored.entryDate, entry.entryDate);
  });
});

// ---------------------------------------------------------------------------
// Migration
// ---------------------------------------------------------------------------

describe('Mood migration', () => {
  it('creates mood_entries table on fresh database', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="mood_entries"');
    assert.equal(rows.length, 1);
  });

  it('creates proper indexes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const rows = await adapter.query(
      "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='mood_entries'"
    );
    const indexNames = rows.map((r: any) => r.name);
    assert.ok(indexNames.includes('idx_mood_entries_profile'));
    assert.ok(indexNames.includes('idx_mood_entries_date'));
    assert.ok(indexNames.includes('idx_mood_entries_deleted'));
    assert.ok(indexNames.includes('idx_mood_entries_updated'));
    assert.ok(indexNames.includes('idx_mood_entries_unique_per_day'));
  });
});

// ---------------------------------------------------------------------------
// Repository
// ---------------------------------------------------------------------------

describe('MoodRepository', () => {
  it('create and retrieve a mood entry', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);

    const created = await repo.createOrUpdate({
      moodValue: 'happy',
      moodEmoji: '😊',
      note: 'Great day!',
      profileId: 'owner-1',
      entryDate: '2026-08-20',
    });

    assert.ok(created.id);
    assert.equal(created.moodValue, 'happy');
    assert.equal(created.note, 'Great day!');

    const retrieved = await repo.getById(created.id);
    assert.ok(retrieved);
    assert.equal(retrieved.moodValue, 'happy');
  });

  it('upserts on same profile+date', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);

    const first = await repo.createOrUpdate({
      moodValue: 'happy',
      moodEmoji: '😊',
      note: null,
      profileId: 'owner-1',
      entryDate: '2026-08-20',
    });

    const second = await repo.createOrUpdate({
      moodValue: 'sad',
      moodEmoji: '😢',
      note: 'Rainy day',
      profileId: 'owner-1',
      entryDate: '2026-08-20',
    });

    // Same ID — should be an update, not a new record
    assert.equal(first.id, second.id);
    assert.equal(second.moodValue, 'sad');
    assert.equal(second.note, 'Rainy day');

    // Only one record total
    const count = await repo.count();
    assert.equal(count, 1);
  });

  it('allows different profiles on same date', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);

    await repo.createOrUpdate({
      moodValue: 'happy',
      moodEmoji: '😊',
      note: null,
      profileId: 'owner-1',
      entryDate: '2026-08-20',
    });

    await repo.createOrUpdate({
      moodValue: 'love',
      moodEmoji: '❤️',
      note: null,
      profileId: 'partner-1',
      entryDate: '2026-08-20',
    });

    const count = await repo.count();
    assert.equal(count, 2);
  });

  it('list returns entries sorted by date descending', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);

    await repo.createOrUpdate({ moodValue: 'happy', moodEmoji: '😊', note: null, profileId: 'owner-1', entryDate: '2026-08-18' });
    await repo.createOrUpdate({ moodValue: 'sad', moodEmoji: '😢', note: null, profileId: 'owner-1', entryDate: '2026-08-20' });
    await repo.createOrUpdate({ moodValue: 'calm', moodEmoji: '😌', note: null, profileId: 'owner-1', entryDate: '2026-08-19' });

    const list = await repo.list();
    assert.equal(list.length, 3);
    assert.equal(list[0].entryDate, '2026-08-20');
    assert.equal(list[1].entryDate, '2026-08-19');
    assert.equal(list[2].entryDate, '2026-08-18');
  });

  it('listByProfile filters correctly', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);

    await repo.createOrUpdate({ moodValue: 'happy', moodEmoji: '😊', note: null, profileId: 'owner-1', entryDate: '2026-08-20' });
    await repo.createOrUpdate({ moodValue: 'sad', moodEmoji: '😢', note: null, profileId: 'partner-1', entryDate: '2026-08-20' });

    const ownerMoods = await repo.listByProfile('owner-1');
    assert.equal(ownerMoods.length, 1);
    assert.equal(ownerMoods[0].moodValue, 'happy');
  });

  it('delete soft-deletes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);

    const created = await repo.createOrUpdate({
      moodValue: 'happy',
      moodEmoji: '😊',
      note: null,
      profileId: 'owner-1',
      entryDate: '2026-08-20',
    });

    const deleted = await repo.delete(created.id);
    assert.equal(deleted, true);

    const retrieved = await repo.getById(created.id);
    assert.equal(retrieved, null);
  });
});

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

describe('MoodService', () => {
  it('record validates and persists', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);
    const service = new MoodService(repo);

    const entry = await service.record({
      moodValue: 'happy',
      note: 'Beautiful day!',
      profileId: 'owner-1',
      entryDate: '2026-08-20',
    });

    assert.ok(entry.id);
    assert.equal(entry.moodValue, 'happy');
    assert.equal(entry.moodEmoji, '😊');
    assert.equal(entry.note, 'Beautiful day!');
  });

  it('record rejects invalid mood value', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);
    const service = new MoodService(repo);

    await assert.rejects(
      service.record({
        moodValue: 'invalid' as MoodValue,
        profileId: 'owner-1',
        entryDate: '2026-08-20',
      }),
      (err: unknown) => {
        assert.ok(err instanceof Error);
        return true;
      },
    );
  });

  it('record rejects empty profileId', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);
    const service = new MoodService(repo);

    await assert.rejects(
      service.record({
        moodValue: 'happy',
        profileId: '',
        entryDate: '2026-08-20',
      }),
      (err: unknown) => {
        assert.ok(err instanceof Error);
        return true;
      },
    );
  });

  it('getById returns entry', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);
    const service = new MoodService(repo);

    const created = await service.record({
      moodValue: 'love',
      profileId: 'owner-1',
      entryDate: '2026-08-20',
    });

    const found = await service.getById(created.id);
    assert.ok(found);
    assert.equal(found.moodValue, 'love');
  });

  it('delete removes entry', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);
    const service = new MoodService(repo);

    const created = await service.record({
      moodValue: 'happy',
      profileId: 'owner-1',
      entryDate: '2026-08-20',
    });

    await service.delete(created.id);
    const result = await service.getById(created.id);
    assert.equal(result, null);
  });

  it('getMoodStats computes correctly', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);
    const service = new MoodService(repo);

    await service.record({ moodValue: 'happy', profileId: 'owner-1', entryDate: '2026-08-18' });
    await service.record({ moodValue: 'happy', profileId: 'owner-1', entryDate: '2026-08-19' });
    await service.record({ moodValue: 'sad', profileId: 'owner-1', entryDate: '2026-08-20' });

    const stats = await service.getMoodStats('owner-1');
    assert.equal(stats.totalEntries, 3);
    assert.equal(stats.mostFrequentMood, 'happy');
  });

  it('getMoodStats returns empty for no entries', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);
    const service = new MoodService(repo);

    const stats = await service.getMoodStats('nonexistent');
    assert.equal(stats.totalEntries, 0);
    assert.equal(stats.mostFrequentMood, null);
  });
});

// ---------------------------------------------------------------------------
// Edge cases
// ---------------------------------------------------------------------------

describe('Mood edge cases', () => {
  it('handles null note correctly', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new MoodRepository(adapter);
    const service = new MoodService(repo);

    const entry = await service.record({
      moodValue: 'calm',
      profileId: 'owner-1',
      entryDate: '2026-08-20',
    });
    assert.equal(entry.note, null);
  });

  it('survives schema upgrade from v8 to v9', async () => {
    const adapter = await createMemoryAdapter();
    const { ALL_MIGRATIONS } = await import('../src/data/database/migrations/index.ts');
    const pre15 = ALL_MIGRATIONS.filter((m) => m.id <= 8);
    await runMigrations(adapter, pre15);

    // Apply all migrations
    await runMigrations(adapter, ALL_MIGRATIONS);

    // Verify mood_entries table exists
    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="mood_entries"');
    assert.equal(rows.length, 1);
  });
});
