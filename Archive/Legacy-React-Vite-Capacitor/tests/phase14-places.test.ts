/**
 * Phase 14 — Our Places Tests
 *
 * Tests the place data model, migration, repository, service,
 * validation, serialization, search, and edge cases.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

import {
  type Place,
  PLACE_COLUMNS,
  placeSerializer,
  assertPlace,
} from '../src/data/place/placeTypes.ts';
import { createMemoryAdapter, runMigrations } from './helpers.ts';
import { PlaceRepository } from '../src/repositories/placeRepository.ts';
import { PlaceService } from '../src/services/place/placeService.ts';

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

describe('Place data model', () => {
  it('assertPlace accepts valid place', () => {
    const place: Place = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      name: 'Our Favorite Café',
      address: '123 Main St',
      city: 'Portland',
      state: 'OR',
      country: 'US',
      latitude: 45.5152,
      longitude: -122.6784,
      notes: 'Where we had our first date',
      category: 'Restaurant',
      photoRef: null,
      memoryId: null,
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.doesNotThrow(() => assertPlace(place));
  });

  it('assertPlace rejects empty name', () => {
    const place: Place = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      name: '',
      address: null,
      city: null,
      state: null,
      country: null,
      latitude: null,
      longitude: null,
      notes: null,
      category: null,
      photoRef: null,
      memoryId: null,
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.throws(() => assertPlace(place), /name must not be empty/);
  });
});

// ---------------------------------------------------------------------------
// Serializer
// ---------------------------------------------------------------------------

describe('Place serializer', () => {
  it('has correct column count', () => {
    assert.equal(PLACE_COLUMNS.length, 15);
  });

  it('round-trips through toParams/fromRow', () => {
    const place: Place = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      name: 'Our Favorite Café',
      address: '123 Main St',
      city: 'Portland',
      state: 'OR',
      country: 'US',
      latitude: 45.5152,
      longitude: -122.6784,
      notes: 'Where we had our first date',
      category: 'Restaurant',
      photoRef: null,
      memoryId: null,
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };

    const params = placeSerializer.toParams(place);
    const row: Record<string, unknown> = {};
    PLACE_COLUMNS.forEach((col, i) => {
      row[col] = params[i];
    });

    const restored = placeSerializer.fromRow(row as any);
    assert.equal(restored.id, place.id);
    assert.equal(restored.name, place.name);
    assert.equal(restored.city, place.city);
    assert.equal(restored.latitude, place.latitude);
    assert.equal(restored.category, place.category);
  });
});

// ---------------------------------------------------------------------------
// Migration
// ---------------------------------------------------------------------------

describe('Places migration', () => {
  it('creates places table on fresh database', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="places"');
    assert.equal(rows.length, 1);
  });

  it('creates proper indexes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const rows = await adapter.query(
      "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='places'"
    );
    const indexNames = rows.map((r: any) => r.name);
    assert.ok(indexNames.includes('idx_places_name'));
    assert.ok(indexNames.includes('idx_places_city'));
    assert.ok(indexNames.includes('idx_places_category'));
    assert.ok(indexNames.includes('idx_places_deleted'));
    assert.ok(indexNames.includes('idx_places_updated'));
    assert.ok(indexNames.includes('idx_places_memory'));
  });
});

// ---------------------------------------------------------------------------
// Repository
// ---------------------------------------------------------------------------

describe('PlaceRepository', () => {
  it('create and retrieve a place', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);

    const created = await repo.create({
      name: 'Our Café',
      address: '123 Main St',
      city: 'Portland',
      state: 'OR',
      country: 'US',
      latitude: 45.5152,
      longitude: -122.6784,
      notes: 'First date spot',
      category: 'Restaurant',
      photoRef: null,
      memoryId: null,
    });

    assert.ok(created.id);
    assert.equal(created.name, 'Our Café');
    assert.equal(created.city, 'Portland');

    const retrieved = await repo.getById(created.id);
    assert.ok(retrieved);
    assert.equal(retrieved.name, 'Our Café');
  });

  it('list returns all non-deleted places sorted by name', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);

    await repo.create({ name: 'Zoo', address: null, city: null, state: null, country: null, latitude: null, longitude: null, notes: null, category: null, photoRef: null, memoryId: null });
    await repo.create({ name: 'Airport', address: null, city: null, state: null, country: null, latitude: null, longitude: null, notes: null, category: null, photoRef: null, memoryId: null });

    const list = await repo.list();
    assert.equal(list.length, 2);
    assert.equal(list[0].name, 'Airport');
    assert.equal(list[1].name, 'Zoo');
  });

  it('update modifies fields', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);

    const created = await repo.create({
      name: 'Original',
      address: null, city: null, state: null, country: null,
      latitude: null, longitude: null, notes: null, category: null,
      photoRef: null, memoryId: null,
    });

    const updated = await repo.update(created.id, { name: 'Updated Name' });
    assert.ok(updated);
    assert.equal(updated.name, 'Updated Name');
  });

  it('delete soft-deletes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);

    const created = await repo.create({
      name: 'To Delete',
      address: null, city: null, state: null, country: null,
      latitude: null, longitude: null, notes: null, category: null,
      photoRef: null, memoryId: null,
    });

    const deleted = await repo.delete(created.id);
    assert.equal(deleted, true);

    const retrieved = await repo.getById(created.id);
    assert.equal(retrieved, null);
  });

  it('search finds places by name and city', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);

    await repo.create({ name: 'Moon Tower', address: null, city: 'Austin', state: 'TX', country: 'US', latitude: null, longitude: null, notes: null, category: null, photoRef: null, memoryId: null });
    await repo.create({ name: 'Starbucks', address: null, city: 'Portland', state: 'OR', country: 'US', latitude: null, longitude: null, notes: null, category: null, photoRef: null, memoryId: null });

    const results = await repo.search('moon');
    assert.equal(results.length, 1);
    assert.equal(results[0].name, 'Moon Tower');

    const byCity = await repo.search('portland');
    assert.equal(byCity.length, 1);
    assert.equal(byCity[0].name, 'Starbucks');
  });

  it('listByCategory filters correctly', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);

    await repo.create({ name: 'Café', address: null, city: null, state: null, country: null, latitude: null, longitude: null, notes: null, category: 'Restaurant', photoRef: null, memoryId: null });
    await repo.create({ name: 'Beach', address: null, city: null, state: null, country: null, latitude: null, longitude: null, notes: null, category: 'Vacation', photoRef: null, memoryId: null });

    const restaurants = await repo.listByCategory('Restaurant');
    assert.equal(restaurants.length, 1);
    assert.equal(restaurants[0].name, 'Café');
  });

  it('count returns correct number', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);

    assert.equal(await repo.count(), 0);
    await repo.create({ name: 'A', address: null, city: null, state: null, country: null, latitude: null, longitude: null, notes: null, category: null, photoRef: null, memoryId: null });
    assert.equal(await repo.count(), 1);
  });
});

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

describe('PlaceService', () => {
  it('create validates and persists', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);
    const service = new PlaceService(repo);

    const created = await service.create({
      name: 'Service Place',
      city: 'Seattle',
      category: 'Vacation',
    });

    assert.ok(created.id);
    assert.equal(created.name, 'Service Place');
    assert.equal(created.city, 'Seattle');
  });

  it('create rejects empty name', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);
    const service = new PlaceService(repo);

    await assert.rejects(
      service.create({ name: '' }),
      (err: unknown) => {
        assert.ok(err instanceof Error);
        return true;
      },
    );
  });

  it('update validates and persists', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);
    const service = new PlaceService(repo);

    const created = await service.create({ name: 'Original' });
    const updated = await service.update(created.id, { name: 'Updated' });
    assert.equal(updated.name, 'Updated');
  });

  it('update rejects non-existent place', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);
    const service = new PlaceService(repo);

    await assert.rejects(
      service.update('non-existent-id', { name: 'Test' }),
      (err: unknown) => {
        assert.ok(err instanceof Error);
        return true;
      },
    );
  });

  it('delete removes place', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);
    const service = new PlaceService(repo);

    const created = await service.create({ name: 'To Delete' });
    await service.delete(created.id);
    const result = await service.getById(created.id);
    assert.equal(result, null);
  });

  it('search delegates to repository', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);
    const service = new PlaceService(repo);

    await service.create({ name: 'Moon Tower', city: 'Austin' });
    await service.create({ name: 'Starbucks', city: 'Portland' });

    const results = await service.search('moon');
    assert.equal(results.length, 1);
    assert.equal(results[0].name, 'Moon Tower');
  });

  it('search returns empty for blank query', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);
    const service = new PlaceService(repo);

    const results = await service.search('  ');
    assert.equal(results.length, 0);
  });
});

// ---------------------------------------------------------------------------
// Edge cases
// ---------------------------------------------------------------------------

describe('Place edge cases', () => {
  it('handles null optional fields', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);
    const service = new PlaceService(repo);

    const place = await service.create({ name: 'Minimal Place' });
    assert.equal(place.address, null);
    assert.equal(place.city, null);
    assert.equal(place.latitude, null);
    assert.equal(place.photoRef, null);
    assert.equal(place.memoryId, null);
  });

  it('handles coordinates correctly', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PlaceRepository(adapter);
    const service = new PlaceService(repo);

    const place = await service.create({
      name: 'Coordinate Test',
      latitude: -33.8688,
      longitude: 151.2093,
    });
    assert.equal(place.latitude, -33.8688);
    assert.equal(place.longitude, 151.2093);
  });

  it('survives schema upgrade from v7 to v8', async () => {
    const adapter = await createMemoryAdapter();
    // Apply only first 7 migrations (simulating pre-Phase-14)
    const { ALL_MIGRATIONS } = await import('../src/data/database/migrations/index.ts');
    const pre14 = ALL_MIGRATIONS.filter((m) => m.id <= 7);
    await runMigrations(adapter, pre14);

    // Now apply all migrations (should add places table without error)
    await runMigrations(adapter, ALL_MIGRATIONS);

    // Verify places table exists
    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="places"');
    assert.equal(rows.length, 1);
  });
});
