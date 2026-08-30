/**
 * Stage 5 — Memories presentation helpers.
 *
 * Pure-function tests for the photo-first Memories vocabulary: year-chip
 * collection/filtering and date formatting. No DOM, no sql.js, no mocks.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  collectYears,
  extractYear,
  filterByYear,
  formatDateKey,
  byNewestFirst,
} from '../src/features/memories/memoryFilters.ts';

interface MemoryLike {
  memoryDate: string | null;
}

const memoryOf = (memoryDate: string | null): MemoryLike => ({ memoryDate });

describe('collectYears', () => {
  it('returns distinct years newest-first', () => {
    const memories = [
      memoryOf('2024-05-12'),
      memoryOf('2023-10-03'),
      memoryOf('2024-02-14'),
      memoryOf('2025-01-26'),
    ];
    assert.deepEqual(collectYears(memories), [2025, 2024, 2023]);
  });

  it('handles an empty list', () => {
    assert.deepEqual(collectYears([]), []);
  });

  it('ignores missing or invalid dates', () => {
    const memories = [memoryOf(null), memoryOf('not-a-date'), memoryOf('2024-01-01')];
    assert.deepEqual(collectYears(memories), [2024]);
  });

  it('returns one year when all memories share it', () => {
    const memories = [memoryOf('2024-05-12'), memoryOf('2024-12-31')];
    assert.deepEqual(collectYears(memories), [2024]);
  });
});

describe('extractYear', () => {
  it('extracts the year from a date key', () => {
    assert.equal(extractYear('2026-08-24'), 2026);
  });

  it('rejects null and malformed values', () => {
    assert.equal(extractYear(null), null);
    assert.equal(extractYear('24-08-2026'), null);
    assert.equal(extractYear(''), null);
  });
});

describe('filterByYear', () => {
  const memories = [
    memoryOf('2024-05-12'),
    memoryOf('2023-10-03'),
    memoryOf('2024-02-14'),
    memoryOf(null),
  ];

  it('returns everything for the null filter', () => {
    assert.equal(filterByYear(memories, null).length, 4);
  });

  it('selects only entries from the chosen year', () => {
    const result = filterByYear(memories, 2024);
    assert.equal(result.length, 2);
    for (const m of result) assert.equal(m.memoryDate?.startsWith('2024'), true);
  });

  it('returns an empty list when nothing matches', () => {
    assert.deepEqual(filterByYear(memories, 2027), []);
  });
});

describe('byNewestFirst', () => {
  it('orders newest first without mutating the input', () => {
    const input = [
      { createdAt: '2024-01-01T00:00:00.000Z' },
      { createdAt: '2026-08-24T00:00:00.000Z' },
      { createdAt: '2025-06-15T00:00:00.000Z' },
    ];
    const result = byNewestFirst(input);
    assert.deepEqual(
      result.map((m) => m.createdAt),
      ['2026-08-24T00:00:00.000Z', '2025-06-15T00:00:00.000Z', '2024-01-01T00:00:00.000Z'],
    );
    // Input untouched
    assert.equal(input[0].createdAt, '2024-01-01T00:00:00.000Z');
  });

  it('handles empty and single-item lists', () => {
    assert.deepEqual(byNewestFirst([]), []);
    const one = [{ createdAt: '2024-01-01T00:00:00.000Z' }];
    assert.equal(byNewestFirst(one).length, 1);
  });
});

describe('formatDateKey', () => {
  it('formats a friendly date', () => {
    assert.equal(formatDateKey('2024-05-12'), 'May 12, 2024');
  });

  it('uses numeric-safe output (no locale day awareness issues)', () => {
    assert.equal(formatDateKey('2026-01-05'), 'January 5, 2026');
  });

  it('returns empty for null input', () => {
    assert.equal(formatDateKey(null), '');
  });

  it('returns empty for malformed input', () => {
    assert.equal(formatDateKey('12-05-2024'), '');
    assert.equal(formatDateKey('not-a-date'), '');
  });

  it('returns empty for out-of-range months', () => {
    assert.equal(formatDateKey('2024-13-01'), '');
    assert.equal(formatDateKey('2024-00-12'), '');
  });
});
