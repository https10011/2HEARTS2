import { test } from 'node:test';
import assert from 'node:assert';
import { FileService } from '../src/services/files/fileService.ts';
import { MemoryFileAdapter } from '../src/services/files/fileAdapters.ts';

function makeService() {
  return { service: new FileService(new MemoryFileAdapter()), name1: 'docs/a.txt' };
}

test('write/read/exists/delete round trip through the boundary', async () => {
  const { service } = makeService();
  const data = new Uint8Array([1, 2, 3]);
  await service.write('docs/a.txt', data);
  assert.ok(await service.exists('docs/a.txt'));
  assert.deepStrictEqual([...await service.read('docs/a.txt')], [1, 2, 3]);
  assert.ok(await service.deleteIfExists('docs/a.txt'));
  assert.ok(!(await service.exists('docs/a.txt')));
  // idempotent delete
  assert.ok(!(await service.deleteIfExists('docs/a.txt')));
});

test('unsafe paths never reach the adapter', async () => {
  const { service } = makeService();
  await assert.rejects(service.write('../etc/passwd', new Uint8Array([1])));
  await assert.rejects(service.write('..', new Uint8Array()));
  await assert.rejects(service.read('/absolute'));
  assert.ok(!(await service.exists('../escape')));
  assert.ok(!(await service.deleteIfExists('a b'))); // whitespace rejected silently
});

test('move and copy enforce distinct targets', async () => {
  const { service } = makeService();
  await service.write('docs/a.txt', new Uint8Array([9]));
  await service.copy('docs/a.txt', 'docs/b.txt');
  assert.ok(await service.exists('docs/a.txt'));
  assert.ok(await service.exists('docs/b.txt'));
  await service.move('docs/b.txt', 'docs/c.txt');
  assert.ok(!(await service.exists('docs/b.txt')));
  assert.ok(await service.exists('docs/c.txt'));
  // target-exists is a hard AppError
  await assert.rejects(service.copy('docs/a.txt', 'docs/c.txt'), /copy-target-exists/);
});

test('safeFileName scrubs stems and extensions', () => {
  const { service } = makeService();
  assert.strictEqual(service.safeFileName('my photo (1)', 'jpg', '01-Me'), 'my-photo-1-01-Me.jpg');
  assert.strictEqual(service.safeFileName('../../x', 'png', 'y'), 'x-y.png'); // traversal scrubbed
  assert.ok(service.safeFileName('', 'webp', '').length > 0);
});

test('sweep removes only unlisted orphans', async () => {
  const { service } = makeService();
  await service.write('tmp/keep.bin', new Uint8Array([1]));
  await service.write('tmp/drop.bin', new Uint8Array([2]));
  const dry = await service.sweep('tmp', new Set(['keep.bin']), true);
  assert.deepStrictEqual(dry, ['drop.bin']);
  assert.ok(await service.exists('tmp/drop.bin')); // dry-run touched nothing
  const gone = await service.sweep('tmp', new Set(['keep.bin']));
  assert.deepStrictEqual(gone, ['drop.bin']);
  assert.ok(!(await service.exists('tmp/drop.bin')));
});

test('list returns only direct children of a directory', async () => {
  const { service } = makeService();
  await service.write('docs/top.txt', new Uint8Array());
  await service.write('docs/nested/inner.txt', new Uint8Array());
  await service.write('other/x.txt', new Uint8Array());
  assert.deepStrictEqual(await service.list('docs'), ['top.txt']);
  assert.deepStrictEqual(await service.list('docs/nested'), ['inner.txt']);
  assert.deepStrictEqual(await service.list('other'), ['x.txt']);
});
