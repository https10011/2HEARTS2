/**
 * Stage 7 — Timeline + Story experience tests.
 *
 * Covers the pure storytelling helpers (timelineStory.ts): date
 * formatting, story-row composition (year anchors, chapters, latest
 * emphasis), and chapter lookup.
 */

import { test } from 'node:test';
import assert from 'node:assert/strict';

import {
  buildStoryRows,
  chapterOf,
  formatEventDate,
} from '../src/features/timeline/timelineStory.ts';

const ev = (id: string, eventDate: string) => ({ id, eventDate });

test('formatEventDate renders a long local date from a yyyy-mm-dd key', () => {
  const formatted = formatEventDate('2025-08-08');
  assert.match(formatted, /August/);
  assert.match(formatted, /8/);
  assert.match(formatted, /2025/);
});

test('formatEventDate is UTC-safe (no day drift across timezones)', () => {
  // Jan 1 must never render as Dec 31 regardless of host timezone.
  const formatted = formatEventDate('2026-01-01');
  assert.match(formatted, /January/);
  assert.match(formatted, /2026/);
  assert.doesNotMatch(formatted, /December/);
});

test('formatEventDate passes through malformed keys unchanged', () => {
  assert.equal(formatEventDate('not-a-date'), 'not-a-date');
});

test('buildStoryRows: chapters follow chronological order', () => {
  const rows = buildStoryRows([
    ev('a', '2025-08-08'),
    ev('b', '2025-09-02'),
    ev('c', '2025-10-08'),
  ]);
  const events = rows.filter((r) => r.type === 'event');
  assert.deepEqual(
    events.map((r) => (r.type === 'event' ? r.chapter : 0)),
    [1, 2, 3],
  );
});

test('buildStoryRows: the final event is flagged as latest', () => {
  const rows = buildStoryRows([ev('a', '2025-08-08'), ev('b', '2025-09-02')]);
  const events = rows.filter((r) => r.type === 'event');
  assert.equal(events[0].type === 'event' && events[0].isLatest, false);
  assert.equal(events[1].type === 'event' && events[1].isLatest, true);
});

test('buildStoryRows: single year story has no year anchors', () => {
  const rows = buildStoryRows([ev('a', '2025-08-08'), ev('b', '2025-09-02')]);
  assert.equal(rows.some((r) => r.type === 'year'), false);
});

test('buildStoryRows: multi-year story inserts anchors per year group', () => {
  const rows = buildStoryRows([
    ev('a', '2025-08-08'),
    ev('b', '2025-12-24'),
    ev('c', '2026-01-14'),
  ]);
  const anchors = rows.filter((r) => r.type === 'year');
  assert.deepEqual(
    anchors.map((r) => (r.type === 'year' ? r.year : '')),
    ['2025', '2026'],
  );
  // The 2026 anchor must come after the 2025 events.
  const idx2026 = rows.findIndex((r) => r.type === 'year' && r.year === '2026');
  const idxB = rows.findIndex((r) => r.type === 'event' && r.event.id === 'b');
  const idxC = rows.findIndex((r) => r.type === 'event' && r.event.id === 'c');
  assert.ok(idxB < idx2026 && idx2026 < idxC);
});

test('buildStoryRows: empty list yields no rows', () => {
  assert.deepEqual(buildStoryRows([]), []);
});

test('chapterOf returns the 1-based position or null', () => {
  const events = [ev('a', '2025-08-08'), ev('b', '2025-09-02')];
  assert.equal(chapterOf(events, 'a'), 1);
  assert.equal(chapterOf(events, 'b'), 2);
  assert.equal(chapterOf(events, 'missing'), null);
});
