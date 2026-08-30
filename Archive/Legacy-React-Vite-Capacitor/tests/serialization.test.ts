import { test } from 'node:test';
import assert from 'node:assert';
import {
  optionalNumber,
  optionalString,
  requireEnum,
  requireString,
  stableStringify,
} from '../src/data/serialization/entitySerializer.ts';
import { mediaAssetSerializer, type MediaAsset } from '../src/data/media/mediaTypes.ts';
import { PersistenceError } from '../src/data/database/errors.ts';
import { newId } from '../src/utils/ids.ts';

const asset: MediaAsset = {
  id: newId(),
  kind: 'photo',
  mimeType: 'image/jpeg',
  relativePath: 'media/photos/a.jpg',
  sizeBytes: 42,
  createdAt: '2026-01-01T00:00:00.000Z',
  updatedAt: '2026-01-01T00:00:00.000Z',
  deletedAt: null,
};

test('media asset serializer round-trips entity → params → row', () => {
  const params = mediaAssetSerializer.toParams(asset);
  const row: Record<string, unknown> = {};
  mediaAssetSerializer.columns.forEach((column, index) => {
    row[column] = params[index];
  });
  assert.deepStrictEqual(mediaAssetSerializer.fromRow(row), asset);
});

test('serialization converts null properly', () => {
  const row = {
    ...asset,
    size_bytes: null,
    deleted_at: '2026-02-01T00:00:00.000Z',
    kind: asset.kind,
    mime_type: asset.mimeType,
    relative_path: asset.relativePath,
    created_at: asset.createdAt,
    updated_at: asset.updatedAt,
    id: asset.id,
  };
  const parsed = mediaAssetSerializer.fromRow(row);
  assert.strictEqual(parsed.sizeBytes, null);
  assert.strictEqual(parsed.deletedAt, '2026-02-01T00:00:00.000Z');
});

test('serializer rejects corrupt rows instead of silently guessing', () => {
  assert.throws(
    () => mediaAssetSerializer.fromRow({ id: 123 }),
    (error: unknown) => error instanceof PersistenceError && error.code === 'serialization-failed',
  );
  assert.throws(() => mediaAssetSerializer.fromRow({ ...asset, kind: 'audio' }));
});

test('stableStringify produces deterministic key order', () => {
  const a = stableStringify({ b: 1, a: { d: 4, c: 3 }, e: [2, { y: 1, x: 0 }] });
  const b = stableStringify({ e: [2, { x: 0, y: 1 }], a: { c: 3, d: 4 }, b: 1 });
  assert.strictEqual(a, b);
});

test('field helpers enforce types', () => {
  assert.strictEqual(requireString({ name: 'x' }, 'name'), 'x');
  assert.throws(() => requireString({ name: '' }, 'name'));
  assert.strictEqual(optionalString({}, 'missing'), null);
  assert.strictEqual(optionalNumber({ n: 7 }, 'n'), 7);
  assert.strictEqual(optionalNumber({ n: null }, 'n'), null);
  assert.throws(() => requireEnum({ k: 'x' }, 'k', ['a', 'b'] as const));
  assert.strictEqual(requireEnum({ k: 'a' }, 'k', ['a', 'b'] as const), 'a');
});
