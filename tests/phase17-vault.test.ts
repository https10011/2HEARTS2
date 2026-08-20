/**
 * Phase 17 — Private Vault Tests
 *
 * Tests the vault data model, migration, repository, service,
 * access control, validation, and edge cases.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

import {
  type VaultItem,
  type VaultContentType,
  VAULT_CONTENT_TYPES,
  VAULT_ITEM_COLUMNS,
  vaultItemSerializer,
  assertVaultItem,
} from '../src/data/vault/vaultTypes.ts';
import { createMemoryAdapter, runMigrations } from './helpers.ts';
import { VaultRepository } from '../src/repositories/vaultRepository.ts';
import { VaultService } from '../src/services/vault/vaultService.ts';

// ---------------------------------------------------------------------------
// Mock AppLockService for testing
// ---------------------------------------------------------------------------

function createMockAppLock(configured = false) {
  let state: 'disabled' | 'unlocked' | 'locked' = configured ? 'locked' : 'disabled';
  const listeners = new Set<(s: string) => void>();

  return {
    currentState: () => state,
    isConfigured: () => configured,
    unlock: async (_pin: string) => {
      if (configured) {
        state = 'unlocked';
        listeners.forEach((l) => l(state));
        return true;
      }
      return false;
    },
    lock: () => {
      state = 'locked';
      listeners.forEach((l) => l(state));
    },
    onLockChange: (cb: (s: string) => void) => {
      listeners.add(cb);
      return () => listeners.delete(cb);
    },
  };
}

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

describe('Vault data model', () => {
  it('has correct content types', () => {
    assert.deepStrictEqual(VAULT_CONTENT_TYPES, ['photo', 'video', 'note', 'file']);
  });

  it('assertVaultItem accepts valid item', () => {
    const item: VaultItem = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      title: 'Secret Note',
      contentType: 'note',
      mediaRef: null,
      filePath: null,
      content: 'Private thoughts',
      description: null,
      profileId: 'owner-1',
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.doesNotThrow(() => assertVaultItem(item));
  });

  it('assertVaultItem rejects empty title', () => {
    const item: VaultItem = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      title: '',
      contentType: 'note',
      mediaRef: null,
      filePath: null,
      content: null,
      description: null,
      profileId: 'owner-1',
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.throws(() => assertVaultItem(item), /title must not be empty/);
  });

  it('assertVaultItem rejects invalid content type', () => {
    const item: VaultItem = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      title: 'Test',
      contentType: 'invalid' as VaultContentType,
      mediaRef: null,
      filePath: null,
      content: null,
      description: null,
      profileId: 'owner-1',
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.throws(() => assertVaultItem(item), /contentType must be one of/);
  });
});

// ---------------------------------------------------------------------------
// Serializer
// ---------------------------------------------------------------------------

describe('Vault serializer', () => {
  it('has correct column count', () => {
    assert.equal(VAULT_ITEM_COLUMNS.length, 11);
  });

  it('round-trips through toParams/fromRow', () => {
    const item: VaultItem = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      title: 'Secret Note',
      contentType: 'note',
      mediaRef: null,
      filePath: null,
      content: 'Private thoughts',
      description: 'A test',
      profileId: 'owner-1',
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };

    const params = vaultItemSerializer.toParams(item);
    const row: Record<string, unknown> = {};
    VAULT_ITEM_COLUMNS.forEach((col, i) => {
      row[col] = params[i];
    });

    const restored = vaultItemSerializer.fromRow(row as any);
    assert.equal(restored.id, item.id);
    assert.equal(restored.title, item.title);
    assert.equal(restored.contentType, item.contentType);
    assert.equal(restored.content, item.content);
    assert.equal(restored.profileId, item.profileId);
  });
});

// ---------------------------------------------------------------------------
// Migration
// ---------------------------------------------------------------------------

describe('Vault migration', () => {
  it('creates vault_items table', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="vault_items"');
    assert.equal(rows.length, 1);
  });

  it('creates proper indexes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const rows = await adapter.query(
      "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='vault_items'"
    );
    const indexNames = rows.map((r: any) => r.name);
    assert.ok(indexNames.includes('idx_vault_items_profile'));
    assert.ok(indexNames.includes('idx_vault_items_type'));
    assert.ok(indexNames.includes('idx_vault_items_deleted'));
    assert.ok(indexNames.includes('idx_vault_items_updated'));
  });
});

// ---------------------------------------------------------------------------
// Repository
// ---------------------------------------------------------------------------

describe('VaultRepository', () => {
  it('create and retrieve a vault item', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);

    const created = await repo.create({
      title: 'Secret Note',
      contentType: 'note',
      mediaRef: null,
      filePath: null,
      content: 'Private thoughts',
      description: null,
      profileId: 'owner-1',
    });

    assert.ok(created.id);
    assert.equal(created.title, 'Secret Note');

    const retrieved = await repo.getById(created.id);
    assert.ok(retrieved);
    assert.equal(retrieved.title, 'Secret Note');
  });

  it('list returns items sorted by creation date descending', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);

    await repo.create({ title: 'First', contentType: 'note', mediaRef: null, filePath: null, content: null, description: null, profileId: 'owner-1' });
    await repo.create({ title: 'Second', contentType: 'photo', mediaRef: 'ref-1', filePath: null, content: null, description: null, profileId: 'owner-1' });

    const list = await repo.list('owner-1');
    assert.equal(list.length, 2);
    assert.equal(list[0].title, 'Second'); // Most recent first
  });

  it('listByType filters correctly', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);

    await repo.create({ title: 'Note', contentType: 'note', mediaRef: null, filePath: null, content: null, description: null, profileId: 'owner-1' });
    await repo.create({ title: 'Photo', contentType: 'photo', mediaRef: 'ref-1', filePath: null, content: null, description: null, profileId: 'owner-1' });

    const notes = await repo.listByType('owner-1', 'note');
    assert.equal(notes.length, 1);
    assert.equal(notes[0].title, 'Note');
  });

  it('delete soft-deletes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);

    const created = await repo.create({
      title: 'To Delete',
      contentType: 'note',
      mediaRef: null,
      filePath: null,
      content: null,
      description: null,
      profileId: 'owner-1',
    });

    const deleted = await repo.delete(created.id);
    assert.equal(deleted, true);

    const retrieved = await repo.getById(created.id);
    assert.equal(retrieved, null);
  });

  it('count returns correct number', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);

    assert.equal(await repo.count('owner-1'), 0);
    await repo.create({ title: 'A', contentType: 'note', mediaRef: null, filePath: null, content: null, description: null, profileId: 'owner-1' });
    assert.equal(await repo.count('owner-1'), 1);
  });
});

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

describe('VaultService', () => {
  it('create validates and persists when unlocked', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);
    const lock = createMockAppLock(false); // disabled = accessible
    const service = new VaultService(repo, lock as any);

    const item = await service.create({
      title: 'Secret Note',
      contentType: 'note',
      content: 'Private thoughts',
      profileId: 'owner-1',
    });

    assert.ok(item.id);
    assert.equal(item.title, 'Secret Note');
  });

  it('create rejects when locked', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);
    const lock = createMockAppLock(true); // configured = locked
    const service = new VaultService(repo, lock as any);

    await assert.rejects(
      service.create({
        title: 'Test',
        contentType: 'note',
        profileId: 'owner-1',
      }),
      (err: unknown) => {
        assert.ok(err instanceof Error);
        return true;
      },
    );
  });

  it('create rejects empty title', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);
    const lock = createMockAppLock(false);
    const service = new VaultService(repo, lock as any);

    await assert.rejects(
      service.create({
        title: '',
        contentType: 'note',
        profileId: 'owner-1',
      }),
      (err: unknown) => {
        assert.ok(err instanceof Error);
        return true;
      },
    );
  });

  it('isAccessible returns true when lock is disabled', () => {
    const lock = createMockAppLock(false);
    const service = new VaultService(null as any, lock as any);
    assert.equal(service.isAccessible(), true);
  });

  it('isAccessible returns true when unlocked', () => {
    const lock = createMockAppLock(true);
    lock.unlock('1234');
    const service = new VaultService(null as any, lock as any);
    assert.equal(service.isAccessible(), true);
  });

  it('isAccessible returns false when locked', () => {
    const lock = createMockAppLock(true); // starts locked
    const service = new VaultService(null as any, lock as any);
    assert.equal(service.isAccessible(), false);
  });

  it('list returns empty when locked', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);
    const lock = createMockAppLock(true); // locked
    const service = new VaultService(repo, lock as any);

    const items = await service.list('owner-1');
    assert.equal(items.length, 0);
  });

  it('delete removes vault item', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);
    const lock = createMockAppLock(false);
    const service = new VaultService(repo, lock as any);

    const created = await service.create({
      title: 'To Delete',
      contentType: 'note',
      profileId: 'owner-1',
    });

    await service.delete(created.id);
    const result = await service.getById(created.id);
    assert.equal(result, null);
  });
});

// ---------------------------------------------------------------------------
// Security
// ---------------------------------------------------------------------------

describe('Vault security', () => {
  it('vault content not accessible when locked', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);
    const lock = createMockAppLock(true);
    const service = new VaultService(repo, lock as any);

    // Create while unlocked (simulate pre-locked state)
    await lock.unlock('1234');
    await service.create({ title: 'Secret', contentType: 'note', content: 'Hidden', profileId: 'owner-1' });

    // Lock
    lock.lock();

    // Verify inaccessible
    assert.equal(service.isAccessible(), false);
    assert.equal((await service.list('owner-1')).length, 0);
    assert.equal(await service.getById('any-id'), null);
  });

  it('vault content accessible after unlock', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new VaultRepository(adapter);
    const lock = createMockAppLock(true);
    const service = new VaultService(repo, lock as any);

    // Create while unlocked
    await lock.unlock('1234');
    const item = await service.create({ title: 'Secret', contentType: 'note', profileId: 'owner-1' });

    // Lock and verify inaccessible
    lock.lock();
    assert.equal(service.isAccessible(), false);

    // Unlock and verify accessible
    await lock.unlock('1234');
    assert.equal(service.isAccessible(), true);
    const found = await service.getById(item.id);
    assert.ok(found);
    assert.equal(found.title, 'Secret');
  });
});

// ---------------------------------------------------------------------------
// Edge cases
// ---------------------------------------------------------------------------

describe('Vault edge cases', () => {
  it('survives schema upgrade from v10 to v11', async () => {
    const adapter = await createMemoryAdapter();
    const { ALL_MIGRATIONS } = await import('../src/data/database/migrations/index.ts');
    const pre17 = ALL_MIGRATIONS.filter((m) => m.id <= 10);
    await runMigrations(adapter, pre17);

    await runMigrations(adapter, ALL_MIGRATIONS);

    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="vault_items"');
    assert.equal(rows.length, 1);
  });

  it('search does NOT include vault content', async () => {
    // This test verifies that the vault does NOT register a search provider
    // Vault content must remain private and not appear in global search
    const { searchEngine } = await import('../src/services/search/searchEngine.ts');
    const kinds = searchEngine.registeredKinds();
    assert.ok(!kinds.includes('vault'), 'Vault should not be registered in global search');
  });
});
