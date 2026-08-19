import { test } from 'node:test';
import assert from 'node:assert';
import { MediaAssetRepository } from '../src/repositories/mediaAssetRepository.ts';
import type { MediaAsset } from '../src/data/media/mediaTypes.ts';
import { assertEntityConventions } from '../src/data/model/entity.ts';
import { openMigratedDb } from './helpers.ts';

async function withRepo<T>(clockIso: string, fn: (repo: MediaAssetRepository) => Promise<T>): Promise<T> {
  const db = await openMigratedDb();
  const repo = new MediaAssetRepository(db, () => new Date(clockIso));
  try {
    return await fn(repo);
  } finally {
    await db.close();
  }
}

const input = {
  kind: 'photo' as const,
  mimeType: 'image/jpeg',
  relativePath: 'media/photos/x.jpg',
  sizeBytes: 10,
  deletedAt: null,
};

test('create assigns UUID + timestamps and validates conventions', async () => {
  await withRepo('2026-01-01T00:00:00.000Z', async (repo) => {
    const created = await repo.create(input);
    assertEntityConventions(created);
    assert.strictEqual(created.createdAt, '2026-01-01T00:00:00.000Z');
    assert.strictEqual(created.updatedAt, '2026-01-01T00:00:00.000Z');
    assert.strictEqual(created.deletedAt, null);
  });
});

test('get/list/exists round-trip, deterministic ordering', async () => {
  const db = await openMigratedDb();
  let tick = 0;
  const clock = () => {
    tick += 1;
    return new Date(`2026-01-01T00:00:0${tick}.000Z`);
  };
  const repo = new MediaAssetRepository(db, clock);
  const a = await repo.create({ ...input, relativePath: 'media/photos/a.jpg' });
  const b = await repo.create({ ...input, relativePath: 'media/photos/b.jpg' });
  assert.ok(await repo.exists(a.id));
  const fetched = await repo.getById(b.id);
  assert.strictEqual(fetched?.relativePath, 'media/photos/b.jpg');
  const all = await repo.list();
  assert.deepStrictEqual(all.map((x) => x.id), [a.id, b.id]);
  await db.close();
});

test('update refreshes updatedAt and cannot touch identity', async () => {
  await withRepo('2026-01-01T00:00:00.000Z', async (repo) => {
    const created = await repo.create(input);
    const updated = await repo.update(created.id, { mimeType: 'image/png' } as Partial<never>);
    assert.strictEqual(updated.id, created.id);
    assert.strictEqual(updated.mimeType, 'image/png');
    assert.ok(updated.updatedAt >= created.updatedAt);
  });
});

test('tombstoned entities soft-delete and disappear from default list', async () => {
  await withRepo('2026-01-01T00:00:00.000Z', async (repo) => {
    const created = await repo.create(input);
    assert.ok(await repo.delete(created.id));
    assert.ok(!(await repo.delete(created.id)));
    assert.strictEqual(await repo.getById(created.id), null);
    assert.deepStrictEqual(await repo.list(), []);
    const withDeleted = await repo.list({ includeDeleted: true });
    assert.strictEqual(withDeleted.length, 1);
    assert.ok(withDeleted[0].deletedAt);
  });
});

test('non-persisting kinds are rejected by underlying SQL constraints', async () => {
  await withRepo('2026-01-01T00:00:00.000Z', async (repo) => {
    const bad = { ...input, kind: 'audio' } as unknown as Omit<MediaAsset, 'id' | 'createdAt' | 'updatedAt'>;
    await assert.rejects(repo.create(bad));
  });
});
