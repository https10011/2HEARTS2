import { test } from 'node:test';
import assert from 'node:assert';
import { compareIso, isValidIsoTimestamp, nowIso } from '../src/utils/time.ts';

test('nowIso emits a UTC ISO 8601 timestamp', () => {
  const ts = nowIso();
  assert.ok(isValidIsoTimestamp(ts));
  assert.ok(ts.endsWith('Z'));
});

test('clock injection is honored (deterministic timestamps)', () => {
  const fixed = () => new Date('2026-01-02T03:04:05.678Z');
  assert.strictEqual(nowIso(fixed), '2026-01-02T03:04:05.678Z');
});

test('validation rejects non-UTC / malformed values', () => {
  assert.ok(!isValidIsoTimestamp('2026-01-02'));
  assert.ok(!isValidIsoTimestamp('2026-01-02T03:04:05+02:00'));
  assert.ok(!isValidIsoTimestamp('not-a-date'));
  assert.ok(!isValidIsoTimestamp(''));
});

test('lexicographic compare matches chronological order', () => {
  assert.ok(compareIso('2026-01-01T00:00:00.000Z', '2026-01-02T00:00:00.000Z') < 0);
  assert.ok(compareIso('2026-01-02T00:00:00.000Z', '2026-01-01T00:00:00.000Z') > 0);
  assert.ok(compareIso('2026-01-01T00:00:00.000Z', '2026-01-01T00:00:00.000Z') === 0);
});
