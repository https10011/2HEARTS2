/**
 * Stage 6 — Notes experience tests.
 *
 * Covers the pure presentation helpers introduced for the Notes
 * productization (relative-time lines) and the integrity of the
 * centralized category metadata (labels/colors/icons aligned with
 * the domain NOTE_CATEGORIES list).
 */

import { test } from 'node:test';
import assert from 'node:assert/strict';

import {
  formatRelativeTime,
  formatLastEdited,
} from '../src/features/notes/noteTime.ts';
import {
  NOTE_CATEGORY_LABELS,
  NOTE_CATEGORY_COLORS,
} from '../src/features/notes/categoryMeta.ts';
import { NOTE_CATEGORIES } from '../src/data/note/noteTypes.ts';

const NOW = new Date('2026-08-24T12:00:00');

test('formatRelativeTime: just now for < 1 minute', () => {
  assert.equal(formatRelativeTime('2026-08-24T11:59:30', NOW), 'Just now');
});

test('formatRelativeTime: minutes, hours, days buckets', () => {
  assert.equal(formatRelativeTime('2026-08-24T11:55:00', NOW), '5m ago');
  assert.equal(formatRelativeTime('2026-08-24T09:00:00', NOW), '3h ago');
  assert.equal(formatRelativeTime('2026-08-22T12:00:00', NOW), '2d ago');
});

test('formatRelativeTime: falls back to a local date after 7 days', () => {
  const iso = '2026-08-10T12:00:00';
  const expected = new Date(iso).toLocaleDateString();
  assert.equal(formatRelativeTime(iso, NOW), expected);
});

test('formatRelativeTime: future timestamps read as Just now', () => {
  assert.equal(formatRelativeTime('2026-08-25T12:00:00', NOW), 'Just now');
});

test('formatRelativeTime: invalid input returns empty string', () => {
  assert.equal(formatRelativeTime('not-a-date', NOW), '');
});

test('formatLastEdited: just now phrasing', () => {
  assert.equal(formatLastEdited('2026-08-24T11:59:30', NOW), 'Last edited just now');
});

test('formatLastEdited: relative phrasing', () => {
  assert.equal(formatLastEdited('2026-08-24T09:00:00', NOW), 'Last edited 3h ago');
});

test('formatLastEdited: date phrasing after 7 days', () => {
  const iso = '2026-08-10T12:00:00';
  const expected = `Last edited ${new Date(iso).toLocaleDateString()}`;
  assert.equal(formatLastEdited(iso, NOW), expected);
});

test('formatLastEdited: invalid input returns empty string', () => {
  assert.equal(formatLastEdited('junk', NOW), '');
});

test('category metadata covers every domain category', () => {
  for (const cat of NOTE_CATEGORIES) {
    assert.ok(NOTE_CATEGORY_LABELS[cat], `missing label for ${cat}`);
    assert.ok(NOTE_CATEGORY_COLORS[cat], `missing color for ${cat}`);
  }
  // Icon completeness is enforced at compile time:
  // NOTE_CATEGORY_ICONS is typed Record<NoteCategory, …> in categoryIcons.tsx.
});
