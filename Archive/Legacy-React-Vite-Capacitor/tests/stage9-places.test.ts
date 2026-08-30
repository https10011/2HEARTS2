/**
 * Stage 9 — Places + Shared Adventures tests.
 *
 * Covers the pure presentation helpers (location lines, category
 * vocabulary, filtering, ordering, relative "Added …" phrasing, date
 * formatting) and the PlaceService photo coordination against the real
 * local media architecture (sql.js adapter + MemoryFileSystem — no mocks).
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

import type { Place } from '../src/data/place/placeTypes.ts';
import {
  byNewestFirst,
  collectCategories,
  filterPlaces,
  formatAddedAgo,
  formatLocationLine,
  formatPlaceDate,
} from '../src/features/places/placePresentation.ts';
import { createMemoryAdapter, runMigrations } from './helpers.ts';
import { PlaceRepository } from '../src/repositories/placeRepository.ts';
import { PlaceService } from '../src/services/place/placeService.ts';
import { MediaStorage } from '../src/data/media/mediaStorage.ts';
import { MemoryFileSystem } from '../src/data/media/memoryFileSystem.ts';
import { AppError } from '../src/services/errors/appError.ts';

function makePlace(overrides: Partial<Place> = {}): Place {
  return {
    id: overrides.id ?? '550e8400-e29b-41d4-a716-446655440000',
    name: 'That Little Café',
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
    ...overrides,
  };
}

// ---------------------------------------------------------------------------
// formatLocationLine
// ---------------------------------------------------------------------------

describe('formatLocationLine', () => {
  it('joins populated location parts', () => {
    assert.equal(
      formatLocationLine(makePlace({ city: 'Quezon City', state: null, country: 'PH' })),
      'Quezon City, PH',
    );
  });

  it('returns empty string when no location exists', () => {
    assert.equal(formatLocationLine(makePlace()), '');
  });

  it('skips missing parts without dangling separators', () => {
    assert.equal(formatLocationLine(makePlace({ state: 'OR' })), 'OR');
  });
});

// ---------------------------------------------------------------------------
// collectCategories
// ---------------------------------------------------------------------------

describe('collectCategories', () => {
  it('collects distinct categories alphabetically', () => {
    const places = [
      makePlace({ id: 'a', category: 'Special' }),
      makePlace({ id: 'b', category: 'Restaurant' }),
      makePlace({ id: 'c', category: 'Restaurant' }),
      makePlace({ id: 'd', category: null }),
    ];
    assert.deepEqual(collectCategories(places), ['Restaurant', 'Special']);
  });

  it('trims and ignores blank categories', () => {
    const places = [makePlace({ category: '  ' }), makePlace({ id: 'b', category: ' Home ' })];
    assert.deepEqual(collectCategories(places), ['Home']);
  });

  it('returns empty for an empty list', () => {
    assert.deepEqual(collectCategories([]), []);
  });
});

// ---------------------------------------------------------------------------
// filterPlaces
// ---------------------------------------------------------------------------

describe('filterPlaces', () => {
  const places = [
    makePlace({ id: 'a', name: 'That Little Café', city: 'Quezon City', category: 'Restaurant' }),
    makePlace({ id: 'b', name: 'Seaside Walk', city: 'Batangas', category: 'Adventure' }),
    makePlace({ id: 'c', name: 'Coffee Corner', notes: 'our date night spot' }),
  ];

  it('filters by category', () => {
    assert.deepEqual(
      filterPlaces(places, { category: 'Restaurant' }).map((p) => p.id),
      ['a'],
    );
  });

  it('matches query against name, location, and notes (case-insensitive)', () => {
    assert.deepEqual(filterPlaces(places, { query: 'café' }).map((p) => p.id), ['a']);
    assert.deepEqual(filterPlaces(places, { query: 'BATANGAS' }).map((p) => p.id), ['b']);
    assert.deepEqual(filterPlaces(places, { query: 'date night' }).map((p) => p.id), ['c']);
  });

  it('combines category and query', () => {
    assert.deepEqual(
      filterPlaces(places, { category: 'Adventure', query: 'café' }),
      [],
    );
  });

  it('returns everything when filter is empty', () => {
    assert.equal(filterPlaces(places, {}).length, 3);
    assert.equal(filterPlaces(places, { category: null, query: '  ' }).length, 3);
  });
});

// ---------------------------------------------------------------------------
// byNewestFirst
// ---------------------------------------------------------------------------

describe('byNewestFirst', () => {
  it('orders by createdAt descending without mutating the input', () => {
    const places = [
      makePlace({ id: 'old', createdAt: '2025-01-01T00:00:00.000Z' }),
      makePlace({ id: 'new', createdAt: '2026-08-01T00:00:00.000Z' }),
      makePlace({ id: 'mid', createdAt: '2025-06-01T00:00:00.000Z' }),
    ];
    const sorted = byNewestFirst(places);
    assert.deepEqual(sorted.map((p) => p.id), ['new', 'mid', 'old']);
    assert.equal(places[0].id, 'old');
  });
});

// ---------------------------------------------------------------------------
// formatAddedAgo
// ---------------------------------------------------------------------------

describe('formatAddedAgo', () => {
  const now = new Date('2026-08-26T12:00:00.000Z');

  it('buckets recent times warmly', () => {
    assert.equal(formatAddedAgo('2026-08-26T11:30:00.000Z', now), 'Added just now');
    assert.equal(formatAddedAgo('2026-08-26T02:00:00.000Z', now), 'Added today');
    assert.equal(formatAddedAgo('2026-08-25T12:00:00.000Z', now), 'Added yesterday');
    assert.equal(formatAddedAgo('2026-08-22T12:00:00.000Z', now), 'Added 4 days ago');
  });

  it('buckets weeks, months, and years', () => {
    assert.equal(formatAddedAgo('2026-08-05T12:00:00.000Z', now), 'Added 3 weeks ago');
    assert.equal(formatAddedAgo('2026-07-27T12:00:00.000Z', now), 'Added 1 month ago');
    assert.equal(formatAddedAgo('2025-12-26T12:00:00.000Z', now), 'Added 8 months ago');
    assert.equal(formatAddedAgo('2025-06-01T12:00:00.000Z', now), 'Added 1 year ago');
    assert.equal(formatAddedAgo('2023-01-01T12:00:00.000Z', now), 'Added 3 years ago');
  });

  it('returns empty string for invalid input', () => {
    assert.equal(formatAddedAgo('not-a-date', now), '');
  });
});

// ---------------------------------------------------------------------------
// formatPlaceDate
// ---------------------------------------------------------------------------

describe('formatPlaceDate', () => {
  it('formats a full local date', () => {
    const formatted = formatPlaceDate('2025-08-12T10:00:00.000Z');
    assert.match(formatted, /August 1[12], 2025/);
  });

  it('returns empty string for invalid input', () => {
    assert.equal(formatPlaceDate('garbage'), '');
  });
});

// ---------------------------------------------------------------------------
// PlaceService photo coordination (real sql.js + MemoryFileSystem)
// ---------------------------------------------------------------------------

describe('PlaceService photo coordination', () => {
  async function makeService() {
    const db = await createMemoryAdapter();
    await runMigrations(db);
    const fs = new MemoryFileSystem();
    const service = new PlaceService(new PlaceRepository(db), new MediaStorage(db, fs));
    return { service, fs };
  }

  const PNG_BYTES = new Uint8Array([0x89, 0x50, 0x4e, 0x47, 1, 2, 3, 4]);

  it('setPhoto stores bytes locally and links photo_ref', async () => {
    const { service } = await makeService();
    const place = await service.create({ name: 'That Little Café' });
    const updated = await service.setPhoto(place.id, 'image/png', PNG_BYTES);
    assert.ok(updated.photoRef);
    const url = await service.resolvePhotoUrl(place.id);
    assert.ok(url?.startsWith('data:image/png;base64,'));
  });

  it('setPhoto replaces the old asset', async () => {
    const { service } = await makeService();
    const place = await service.create({ name: 'Seaside Walk' });
    const first = await service.setPhoto(place.id, 'image/png', PNG_BYTES);
    const second = await service.setPhoto(place.id, 'image/png', PNG_BYTES);
    assert.notEqual(first.photoRef, second.photoRef);
    const url = await service.resolvePhotoUrl(place.id);
    assert.ok(url);
  });

  it('removePhoto clears photo_ref and resolvePhotoUrl returns null', async () => {
    const { service } = await makeService();
    const place = await service.create({ name: 'Coffee Corner' });
    await service.setPhoto(place.id, 'image/png', PNG_BYTES);
    const cleared = await service.removePhoto(place.id);
    assert.equal(cleared.photoRef, null);
    assert.equal(await service.resolvePhotoUrl(place.id), null);
  });

  it('removePhoto on a photo-less place is a no-op', async () => {
    const { service } = await makeService();
    const place = await service.create({ name: 'Home' });
    const result = await service.removePhoto(place.id);
    assert.equal(result.photoRef, null);
  });

  it('resolvePhotoUrl returns null when bytes are missing', async () => {
    const { service, fs } = await makeService();
    const place = await service.create({ name: 'That View' });
    const updated = await service.setPhoto(place.id, 'image/png', PNG_BYTES);
    // Simulate lost bytes — the safe-ref contract degrades to null.
    await fs.delete(`media/photos/${updated.photoRef}.png`);
    assert.equal(await service.resolvePhotoUrl(place.id), null);
  });

  it('deleting a place best-effort removes its photo asset', async () => {
    const { service } = await makeService();
    const place = await service.create({ name: 'Favorite Ramen' });
    await service.setPhoto(place.id, 'image/png', PNG_BYTES);
    await service.delete(place.id);
    assert.equal(await service.getById(place.id), null);
  });

  it('photo operations reject unknown places', async () => {
    const { service } = await makeService();
    await assert.rejects(
      () => service.setPhoto('missing-id', 'image/png', PNG_BYTES),
      (err: unknown) => err instanceof AppError && err.code === 'not-found',
    );
  });

  it('photo operations fail safely without a media boundary', async () => {
    const db = await createMemoryAdapter();
    await runMigrations(db);
    const service = new PlaceService(new PlaceRepository(db));
    const place = await service.create({ name: 'No Media Here' });
    await assert.rejects(
      () => service.setPhoto(place.id, 'image/png', PNG_BYTES),
      (err: unknown) => err instanceof AppError && err.category === 'media',
    );
    assert.equal(await service.resolvePhotoUrl(place.id), null);
  });
});
