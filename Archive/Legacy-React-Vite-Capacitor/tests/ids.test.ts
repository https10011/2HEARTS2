import { test } from 'node:test';
import assert from 'node:assert';
import { isValidId, newId } from '../src/utils/ids.ts';

test('generated ids are UUID v4', () => {
  for (let i = 0; i < 100; i++) {
    assert.ok(isValidId(newId()), 'every generated id must be a valid UUID v4');
  }
});

test('generated ids are effectively unique', () => {
  const ids = new Set(Array.from({ length: 1000 }, () => newId()));
  assert.strictEqual(ids.size, 1000);
});

test('id validation rejects non-UUID input', () => {
  assert.ok(!isValidId(''));
  assert.ok(!isValidId('1'));
  assert.ok(!isValidId('not-a-uuid'));
  assert.ok(!isValidId('9e7fd8b4-85b1-4be6-91b1-2b9c0f9f0e11'.toUpperCase()));
  assert.ok(!isValidId('9e7fd8b4-85b1-4be6-91b1-2b9c0f9f0e1'));
});
