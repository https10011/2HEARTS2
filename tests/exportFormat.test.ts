import { test } from 'node:test';
import assert from 'node:assert';
import { validateExport, type TwoHeartsExport } from '../src/services/backup/exportFormat.ts';
import { newId } from '../src/utils/ids.ts';

function validPayload(): TwoHeartsExport {
  return {
    format: 'twohearts-export',
    formatVersion: 1,
    exportedAt: '2026-01-01T00:00:00.000Z',
    schemaVersion: 1,
    entities: {
      mediaAssets: [
        {
          id: newId(),
          kind: 'photo',
          mimeType: 'image/jpeg',
          relativePath: 'media/photos/a.jpg',
          sizeBytes: 10,
          createdAt: '2026-01-01T00:00:00.000Z',
          updatedAt: '2026-01-01T00:00:00.000Z',
          deletedAt: null,
        },
      ],
    },
  };
}

test('a well-formed export validates', () => {
  const result = validateExport(validPayload());
  assert.ok(result.ok, result.errors.join(', '));
  assert.deepStrictEqual(result.errors, []);
});

test('unknown format / wrong format version are rejected', () => {
  assert.ok(!validateExport({ ...validPayload(), format: 'other' }).ok);
  assert.ok(!validateExport({ ...validPayload(), formatVersion: 99 }).ok);
  assert.ok(!validateExport(null).ok);
  assert.ok(!validateExport('string').ok);
});

test('newer schema versions are rejected before any migration work', () => {
  const result = validateExport({ ...validPayload(), schemaVersion: 999 });
  assert.ok(!result.ok);
  assert.ok(result.errors.some((message) => message.includes('newer')));
});

test('unsafe media references and bad ids are rejected', () => {
  const bad = validPayload();
  bad.entities.mediaAssets[0].relativePath = '../secrets.png';
  bad.entities.mediaAssets[0].id = 'not-a-uuid';
  const result = validateExport(bad);
  assert.ok(!result.ok);
  assert.ok(result.errors.length >= 2);
});

test('missing entities block is rejected', () => {
  const result = validateExport({ ...validPayload(), entities: undefined });
  assert.ok(!result.ok);
});
