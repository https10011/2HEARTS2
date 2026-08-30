import { test } from 'node:test';
import assert from 'node:assert';
import { MediaStorage } from '../src/data/media/mediaStorage.ts';
import { MemoryFileSystem } from '../src/data/media/memoryFileSystem.ts';
import { assertSafeRelativePath, type MediaFileSystem } from '../src/data/media/fileSystem.ts';
import { PersistenceError } from '../src/data/database/errors.ts';
import { openMigratedDb } from './helpers.ts';

const PNG = new Uint8Array([0x89, 0x50, 0x4e, 0x47]);

async function withStorage<T>(fn: (storage: MediaStorage) => Promise<T>): Promise<T> {
  const db = await openMigratedDb();
  const storage = new MediaStorage(db, new MemoryFileSystem(), () => new Date('2026-01-01T00:00:00.000Z'));
  try {
    return await fn(storage);
  } finally {
    await db.close();
  }
}

test('store validates the relative reference convention', async () => {
  await withStorage(async (storage) => {
    const ref = await storage.store('photo', 'image/png', PNG);
    const meta = await storage.getMetadata(ref.id);
    assert.match(meta.relativePath, /^media\/photos\/[0-9a-f-]+\.png$/);
    assert.strictEqual(meta.sizeBytes, PNG.length);
  });
});

test('resolveUrl returns a data URL without exposing a path', async () => {
  await withStorage(async (storage) => {
    const ref = await storage.store('photo', 'image/png', PNG);
    const url = await storage.resolveUrl(ref.id);
    assert.ok(url.startsWith('data:image/png;base64,'));
  });
});

test('reject unsafe references before any filesystem access', () => {
  for (const bad of ['../x.png', '/abs.png', 'a/b/../c.png', 'a b.png', 'a\\b.png', '..', '', 'a/.png']) {
    assert.throws(() => assertSafeRelativePath(bad));
  }
  assertSafeRelativePath('media/photos/ok.png');
});

test('delete tombstones metadata and removes the file', async () => {
  const db = await openMigratedDb();
  const fs = new MemoryFileSystem();
  const storage = new MediaStorage(db, fs, () => new Date('2026-01-01T00:00:00.000Z'));
  const ref = await storage.store('photo', 'image/png', PNG);
  const meta = await storage.getMetadata(ref.id);
  assert.ok(await fs.exists(meta.relativePath));
  assert.ok(await storage.delete(ref.id));
  assert.ok(!(await fs.exists(meta.relativePath)));
  await assert.rejects(storage.getMetadata(ref.id), (e: unknown) => e instanceof PersistenceError && e.code === 'media-missing');
  assert.ok(!(await storage.delete(ref.id)));
  await db.close();
});

test('orphan detection + sweep removes crash leftovers', async () => {
  const db = await openMigratedDb();
  const fs = new MemoryFileSystem();
  const storage = new MediaStorage(db, fs, () => new Date('2026-01-01T00:00:00.000Z'));
  await storage.store('photo', 'image/png', PNG);
  await fs.write('media/photos/stale.png', PNG);
  assert.deepStrictEqual(await storage.findOrphans(), ['media/photos/stale.png']);
  assert.strictEqual(await storage.sweepOrphans(), 1);
  assert.deepStrictEqual(await storage.findOrphans(), []);
  await db.close();
});

test('fs failure during store rolls back the metadata row', async () => {
  const db = await openMigratedDb();
  const failingFs: MediaFileSystem = {
    write: () => Promise.reject(new PersistenceError('media-fs-failed', 'disk full')),
    read: () => Promise.reject(),
    delete: () => Promise.resolve(),
    exists: () => Promise.resolve(false),
    list: () => Promise.resolve([]),
  };
  const storage = new MediaStorage(db, failingFs, () => new Date('2026-01-01T00:00:00.000Z'));
  await assert.rejects(storage.store('photo', 'image/png', PNG));
  const rows = await db.query('SELECT id FROM media_assets');
  assert.strictEqual(rows.length, 0);
  await db.close();
});

test('unsupported mime types are rejected; valid video is stored', async () => {
  await withStorage(async (storage) => {
    await assert.rejects(
      storage.store('photo', 'text/plain', PNG),
      (e: unknown) => e instanceof PersistenceError && e.code === 'serialization-failed',
    );
    const ok = await storage.store('video', 'video/mp4', PNG);
    assert.strictEqual(ok.kind, 'video');
    const meta = await storage.getMetadata(ok.id);
    assert.match(meta.relativePath, /^media\/videos\/[0-9a-f-]+\.mp4$/);
  });
});
