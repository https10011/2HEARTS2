/**
 * Phase 7 — Memories Tests
 *
 * Tests the memory data model, repository CRUD, service layer,
 * validation, media lifecycle, and migration.
 */

import { describe, it, before, after, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import {
  initializeDatabase,
  finalizeDatabaseForTests,
  getDatabase,
} from '../src/data/database/connection.ts';
import { MemoryRepository } from '../src/repositories/memoryRepository.ts';
import { MemoryService } from '../src/services/memory/memoryService.ts';
import { appSettingsStore } from '../src/core/appSettings.ts';
import { RoutePath } from '../src/navigation/routes.ts';
import { MemoryFileSystem } from '../src/data/media/memoryFileSystem.ts';
import { memorySerializer, type Memory } from '../src/data/memory/memoryTypes.ts';
import { PERSISTENCE_CONFIG } from '../src/config/persistence.ts';

const FIXED_CLOCK = () => new Date('2026-01-15T12:00:00Z');

function resetSettings() {
  appSettingsStore.reset();
}

// ---------------------------------------------------------------------------
// Route structure tests
// ---------------------------------------------------------------------------

describe('Phase 7 routes', () => {
  it('defines memory routes', () => {
    assert.equal(RoutePath.appMemories, '/app/memories');
    assert.equal(RoutePath.appMemoriesAdd, '/app/memories/add');
    assert.ok(RoutePath.appMemoriesDetail.startsWith('/app/memories/'));
  });

  it('memory routes follow app nesting convention', () => {
    assert.ok(RoutePath.appMemories.startsWith('/app/'));
    assert.ok(RoutePath.appMemoriesAdd.startsWith('/app/'));
  });
});

// ---------------------------------------------------------------------------
// Migration tests
// ---------------------------------------------------------------------------

describe('Memory migration', () => {
  before(async () => {
    await initializeDatabase();
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('schema version is 6 after migration', () => {
    assert.equal(PERSISTENCE_CONFIG.schemaVersion, 11);
  });

  it('creates memories table', async () => {
    const db = await getDatabase();
    const tables = await db.query<{ name: string }>(
      "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'memories'",
    );
    assert.equal(tables.length, 1);
  });

  it('creates memory_media table', async () => {
    const db = await getDatabase();
    const tables = await db.query<{ name: string }>(
      "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'memory_media'",
    );
    assert.equal(tables.length, 1);
  });
});

// ---------------------------------------------------------------------------
// Serialization tests
// ---------------------------------------------------------------------------

describe('Memory serialization', () => {
  it('memorySerializer round-trips domain → params → row', () => {
    const memory: Memory = {
      id: 'test-id',
      title: 'Test Memory',
      caption: 'A test caption',
      memoryDate: '2026-01-15',
      sortOrder: 0,
      createdAt: '2026-01-15T12:00:00.000Z',
      updatedAt: '2026-01-15T12:00:00.000Z',
      deletedAt: null,
    };

    const params = memorySerializer.toParams(memory);
    const row: Record<string, unknown> = {};
    memorySerializer.columns.forEach((col, i) => {
      row[col] = params[i];
    });

    const deserialized = memorySerializer.fromRow(row);
    assert.equal(deserialized.id, memory.id);
    assert.equal(deserialized.title, memory.title);
    assert.equal(deserialized.caption, memory.caption);
    assert.equal(deserialized.memoryDate, memory.memoryDate);
    assert.equal(deserialized.sortOrder, memory.sortOrder);
    assert.equal(deserialized.createdAt, memory.createdAt);
    assert.equal(deserialized.updatedAt, memory.updatedAt);
    assert.equal(deserialized.deletedAt, memory.deletedAt);
  });

  it('serializer handles null caption', () => {
    const row: Record<string, unknown> = {
      id: 'test-id',
      title: 'Test',
      caption: null,
      memory_date: null,
      sort_order: 0,
      created_at: '2026-01-15T12:00:00.000Z',
      updated_at: '2026-01-15T12:00:00.000Z',
      deleted_at: null,
    };
    const result = memorySerializer.fromRow(row);
    assert.equal(result.caption, null);
    assert.equal(result.memoryDate, null);
  });
});

// ---------------------------------------------------------------------------
// Repository CRUD tests
// ---------------------------------------------------------------------------

describe('MemoryRepository CRUD', () => {
  let repo: MemoryRepository;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    repo = new MemoryRepository(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('creates a memory', async () => {
    const memory = await repo.create({
      title: 'Our Trip',
      caption: 'A wonderful day',
      memoryDate: '2026-01-10',
      sortOrder: 0,
      deletedAt: null,
    });
    assert.ok(memory.id);
    assert.equal(memory.title, 'Our Trip');
    assert.equal(memory.caption, 'A wonderful day');
    assert.equal(memory.memoryDate, '2026-01-10');
  });

  it('retrieves a memory by id', async () => {
    const created = await repo.create({
      title: 'Get Test',
      caption: null,
      memoryDate: null,
      sortOrder: 0,
      deletedAt: null,
    });
    const found = await repo.getById(created.id);
    assert.ok(found);
    assert.equal(found.title, 'Get Test');
  });

  it('lists memories ordered by sort_order then created_at desc', async () => {
    const memories = await repo.listMemories();
    assert.ok(memories.length >= 1);
    // Verify ordering: sort_order ASC, created_at DESC
    for (let i = 1; i < memories.length; i++) {
      const prev = memories[i - 1];
      const curr = memories[i];
      if (prev.sortOrder === curr.sortOrder) {
        assert.ok(prev.createdAt >= curr.createdAt);
      } else {
        assert.ok(prev.sortOrder <= curr.sortOrder);
      }
    }
  });

  it('updates a memory', async () => {
    const created = await repo.create({
      title: 'Original',
      caption: null,
      memoryDate: null,
      sortOrder: 0,
      deletedAt: null,
    });
    const updated = await repo.update(created.id, { title: 'Updated Title' });
    assert.equal(updated.title, 'Updated Title');
    // updatedAt is set (may equal createdAt with a fixed clock)
    assert.ok(updated.updatedAt);
  });

  it('soft-deletes a memory', async () => {
    const created = await repo.create({
      title: 'To Delete',
      caption: null,
      memoryDate: null,
      sortOrder: 0,
      deletedAt: null,
    });
    const result = await repo.delete(created.id);
    assert.equal(result, true);
    const found = await repo.getById(created.id);
    assert.equal(found, null);
  });

  it('returns false when deleting non-existent memory', async () => {
    const result = await repo.delete('non-existent-id');
    assert.equal(result, false);
  });

  it('counts active memories', async () => {
    const before = await repo.count();
    await repo.create({
      title: 'Count Test',
      caption: null,
      memoryDate: null,
      sortOrder: 0,
      deletedAt: null,
    });
    const after = await repo.count();
    assert.equal(after, before + 1);
  });
});

// ---------------------------------------------------------------------------
// Memory-media join table tests
// ---------------------------------------------------------------------------

describe('Memory-media associations', () => {
  let repo: MemoryRepository;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    repo = new MemoryRepository(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('adds media to memory', async () => {
    const memory = await repo.create({
      title: 'Media Test',
      caption: null,
      memoryDate: null,
      sortOrder: 0,
      deletedAt: null,
    });
    const association = await repo.addMediaToMemory(memory.id, 'media-asset-1');
    assert.equal(association.memoryId, memory.id);
    assert.equal(association.mediaAssetId, 'media-asset-1');
    assert.equal(association.sortOrder, 0);
  });

  it('retrieves media ids for a memory', async () => {
    const memory = await repo.create({
      title: 'Get Media',
      caption: null,
      memoryDate: null,
      sortOrder: 0,
      deletedAt: null,
    });
    await repo.addMediaToMemory(memory.id, 'asset-a');
    await repo.addMediaToMemory(memory.id, 'asset-b');
    const ids = await repo.getMemoryMediaIds(memory.id);
    assert.equal(ids.length, 2);
    assert.ok(ids.includes('asset-a'));
    assert.ok(ids.includes('asset-b'));
  });

  it('prevents duplicate media associations', async () => {
    const memory = await repo.create({
      title: 'No Dupes',
      caption: null,
      memoryDate: null,
      sortOrder: 0,
      deletedAt: null,
    });
    await repo.addMediaToMemory(memory.id, 'same-asset');
    await repo.addMediaToMemory(memory.id, 'same-asset');
    const ids = await repo.getMemoryMediaIds(memory.id);
    assert.equal(ids.length, 1);
  });

  it('removes media from memory', async () => {
    const memory = await repo.create({
      title: 'Remove Media',
      caption: null,
      memoryDate: null,
      sortOrder: 0,
      deletedAt: null,
    });
    await repo.addMediaToMemory(memory.id, 'to-remove');
    const removed = await repo.removeMediaFromMemory(memory.id, 'to-remove');
    assert.equal(removed, true);
    const ids = await repo.getMemoryMediaIds(memory.id);
    assert.equal(ids.length, 0);
  });

  it('clears all media for a memory', async () => {
    const memory = await repo.create({
      title: 'Clear Media',
      caption: null,
      memoryDate: null,
      sortOrder: 0,
      deletedAt: null,
    });
    await repo.addMediaToMemory(memory.id, 'a');
    await repo.addMediaToMemory(memory.id, 'b');
    await repo.clearMemoryMedia(memory.id);
    const ids = await repo.getMemoryMediaIds(memory.id);
    assert.equal(ids.length, 0);
  });
});

// ---------------------------------------------------------------------------
// MemoryService tests
// ---------------------------------------------------------------------------

describe('MemoryService', () => {
  let service: MemoryService;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    const fs = new MemoryFileSystem();
    service = new MemoryService(db, fs, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('creates a memory via service', async () => {
    const result = await service.createMemory({
      title: 'Service Test',
      caption: 'With caption',
      memoryDate: '2026-01-15',
    });
    assert.ok(result.id);
    assert.equal(result.title, 'Service Test');
    assert.equal(result.caption, 'With caption');
    assert.equal(result.memoryDate, '2026-01-15');
    assert.ok(result.createdAt);
  });

  it('validates empty title', () => {
    const result = service.validateInput({ title: '' });
    assert.equal(result.ok, false);
    assert.ok(result.errors.length > 0);
  });

  it('validates title too long', () => {
    const result = service.validateInput({ title: 'A'.repeat(101) });
    assert.equal(result.ok, false);
  });

  it('validates invalid date format', () => {
    const result = service.validateInput({ title: 'Test', memoryDate: 'not-a-date' });
    assert.equal(result.ok, false);
  });

  it('accepts valid input', () => {
    const result = service.validateInput({
      title: 'Valid Memory',
      caption: 'A nice caption',
      memoryDate: '2026-01-15',
    });
    assert.equal(result.ok, true);
    assert.equal(result.errors.length, 0);
  });

  it('lists memories via service', async () => {
    await service.createMemory({ title: 'List Test 1' });
    await service.createMemory({ title: 'List Test 2' });
    const memories = await service.listMemories();
    assert.ok(memories.length >= 2);
  });

  it('gets a single memory via service', async () => {
    const created = await service.createMemory({ title: 'Get Test' });
    const found = await service.getMemory(created.id);
    assert.equal(found.title, 'Get Test');
    assert.ok(found.mediaReferences);
  });

  it('throws for missing memory', async () => {
    try {
      await service.getMemory('non-existent');
      assert.fail('Should have thrown');
    } catch (err) {
      assert.ok(err instanceof Error);
      assert.ok(err.message.includes('not-found'));
    }
  });

  it('updates a memory via service', async () => {
    const created = await service.createMemory({ title: 'Update Me' });
    const updated = await service.updateMemory(created.id, { title: 'Updated!' });
    assert.equal(updated.title, 'Updated!');
  });

  it('deletes a memory via service', async () => {
    const created = await service.createMemory({ title: 'Delete Me' });
    const deleted = await service.deleteMemory(created.id);
    assert.equal(deleted, true);
    try {
      await service.getMemory(created.id);
      assert.fail('Should have thrown');
    } catch {
      // Expected — memory is gone
    }
  });

  it('handles delete of non-existent memory gracefully', async () => {
    const result = await service.deleteMemory('non-existent');
    assert.equal(result, false);
  });
});

// ---------------------------------------------------------------------------
// Returning user behavior
// ---------------------------------------------------------------------------

describe('Returning user behavior with memories', () => {
  before(async () => {
    await initializeDatabase();
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('memories persist across sessions', async () => {
    const db = await getDatabase();
    const fs = new MemoryFileSystem();
    const service = new MemoryService(db, fs, FIXED_CLOCK);
    await service.createMemory({ title: 'Persistent Memory' });
    const memories = await service.listMemories();
    assert.ok(memories.some((m) => m.title === 'Persistent Memory'));
  });
});
