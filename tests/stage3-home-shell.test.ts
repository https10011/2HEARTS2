/**
 * Stage 3 — Home + Global App Shell visual productization tests.
 *
 * Verifies the new Home composition contracts:
 *   1. Pure highlight builders produce detail-scoped routes (never archive
 *      roots — the Phase 24 navigation contract stays intact).
 *   2. The time-of-day greeting boundaries.
 *   3. Source guards: Home keeps the official branding + curated config,
 *      Home never links relationship archive roots, More consumes the
 *      utilities config + app info, the bottom nav keeps the official mark.
 *   4. The new Stage 3 CSS classes exist in the design system.
 *
 * No DOM, no mocks — pure builders + real source files (same style as
 * phase24-home-navigation.test.ts and designTokens.test.ts).
 */

import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';

import { RoutePath } from '../src/navigation/routes.ts';
import {
  greetingForHour,
  formatLocalDateKey,
  buildNoteHighlight,
  buildReminderHighlight,
  buildMemoryHighlight,
  selectHomeHighlights,
} from '../src/features/app-shell/homeHighlights.ts';
import type { NoteView } from '../src/services/note/noteService.ts';
import type { Reminder } from '../src/data/reminder/reminderTypes.ts';
import type { MemoryWithMedia } from '../src/services/memory/memoryService.ts';

const ROOT = process.cwd();
const read = (rel: string) => readFileSync(join(ROOT, rel), 'utf8');

// ---------------------------------------------------------------------------
// 1. Highlight builders — detail-scoped deep links
// ---------------------------------------------------------------------------

const note: NoteView = {
  id: 'note-1',
  title: 'Good morning',
  content: 'Today is ours.',
  excerpt: 'Today is ours.',
  category: 'love-note',
  createdAt: '2026-08-24T07:48:00.000Z',
  updatedAt: '2026-08-24T07:48:00.000Z',
};

const reminder: Reminder = {
  id: 'rem-1',
  title: 'Movie night',
  description: null,
  scheduledDate: '2026-08-30',
  scheduledTime: '19:00',
  recurrence: 'none',
  status: 'active',
  notificationOwnerRef: null,
  notificationEnabled: true,
  createdAt: '2026-08-24T07:48:00.000Z',
  updatedAt: '2026-08-24T07:48:00.000Z',
  deletedAt: null,
};

const memory: MemoryWithMedia = {
  id: 'mem-1',
  title: 'Sunset conversations',
  caption: null,
  memoryDate: '2026-05-12',
  sortOrder: 0,
  createdAt: '2026-08-20T07:48:00.000Z',
  updatedAt: '2026-08-20T07:48:00.000Z',
  mediaReferences: [],
};

test('note highlight deep-links to the note detail screen', () => {
  const h = buildNoteHighlight(note);
  assert.equal(h.to, '/app/notes/note-1');
  assert.equal(h.label, 'Latest note');
  assert.equal(h.meta, 'Today is ours.');
});

test('reminder highlight deep-links to the reminder detail screen', () => {
  const h = buildReminderHighlight(reminder);
  assert.equal(h.to, '/app/reminders/rem-1');
  assert.equal(h.label, 'Upcoming');
  assert.ok(h.meta.includes('19:00'));
});

test('memory highlight deep-links to the memory detail screen', () => {
  const h = buildMemoryHighlight(memory);
  assert.equal(h.to, '/app/memories/mem-1');
  assert.equal(h.label, 'Recent memory');
  assert.equal(h.meta, formatLocalDateKey('2026-05-12'));
});

test('highlight routes never equal a relationship archive root', () => {
  const archiveRoots = [
    RoutePath.appMemories,
    RoutePath.appTimelineRoot,
    RoutePath.appPlaces,
    RoutePath.appMood,
    RoutePath.appPeriod,
    RoutePath.appVault,
  ];
  const highlights = selectHomeHighlights({
    notes: [note],
    reminders: [reminder],
    memories: [memory],
  });
  for (const h of highlights) {
    assert.ok(!archiveRoots.includes(h.to as never), `${h.to} must be a detail route`);
  }
});

test('selectHomeHighlights picks the newest memory and skips empty sources', () => {
  const older: MemoryWithMedia = { ...memory, id: 'mem-0', createdAt: '2026-01-01T00:00:00.000Z' };
  const highlights = selectHomeHighlights({ notes: [], reminders: [], memories: [older, memory] });
  assert.equal(highlights.length, 1);
  assert.equal(highlights[0]?.id, 'mem-1');
  assert.deepEqual(selectHomeHighlights({}), []);
});

// ---------------------------------------------------------------------------
// 2. Greeting + date formatting
// ---------------------------------------------------------------------------

test('greetingForHour covers the full day', () => {
  assert.equal(greetingForHour(2), 'Good night');
  assert.equal(greetingForHour(8), 'Good morning');
  assert.equal(greetingForHour(13), 'Good afternoon');
  assert.equal(greetingForHour(19), 'Good evening');
  assert.equal(greetingForHour(23), 'Good night');
});

test('formatLocalDateKey renders a friendly month-day', () => {
  assert.equal(formatLocalDateKey('2026-05-12'), 'May 12');
});

// ---------------------------------------------------------------------------
// 3. Source guards
// ---------------------------------------------------------------------------

test('Home keeps the official branding, couple binding, and curated config', () => {
  const home = read('src/features/app-shell/screens/HomeScreen.tsx');
  assert.ok(home.includes('BrandLogo'), 'Home must carry the official logo');
  assert.ok(home.includes('variant="brand"'), 'Home uses the full brand lockup');
  assert.ok(home.includes('summary?.owner'), 'owner avatar bound to owner profile');
  assert.ok(home.includes('summary?.partner'), 'partner avatar bound to partner profile');
  assert.ok(home.includes('HOME_PRIMARY_ITEMS'), 'Home consumes the curated config');
  assert.ok(home.includes('RoseLilyDecoration'), 'Home carries a subtle floral');
  assert.ok(home.includes('useHomeHighlights'), 'Home renders the story previews');
  assert.ok(home.includes('th-home-greeting__bell'), 'Home restores the notification bell');
  assert.ok(home.includes('th-home-couple-avatars__heart'), 'Home joins the two avatars');
});

test('Home never links relationship archive roots directly', () => {
  const home = read('src/features/app-shell/screens/HomeScreen.tsx');
  for (const forbidden of ['appMemories', 'appTimelineRoot', 'appPlaces', 'appMood', 'appPeriod', 'appVault']) {
    assert.ok(!home.includes(forbidden), `Home must not reference ${forbidden}`);
  }
});

test('highlight builders live in the pure module (Node-test-safe, no JSX)', () => {
  const mod = read('src/features/app-shell/homeHighlights.ts');
  assert.ok(!mod.includes('react'), 'pure module must not import React');
  assert.ok(!mod.includes('getDatabase'), 'pure module must not touch storage');
});

test('More screen consumes the utilities config, app info, and hero', () => {
  const more = read('src/features/app-shell/screens/MoreScreen.tsx');
  assert.ok(more.includes('MORE_ITEMS'), 'More consumes the utilities config');
  assert.ok(more.includes('APP_INFO'), 'More renders the real version');
  assert.ok(more.includes('th-more-hero'), 'More carries the burgundy hero band');
  assert.ok(more.includes('th-more-profile'), 'More carries the profile card');
  assert.ok(more.includes('RoseLilyDecoration'), 'More carries the official lily');
});

test('bottom navigation keeps the official mark and gains the icon capsule', () => {
  const nav = read('src/features/app-shell/BottomNav.tsx');
  assert.ok(nav.includes('BrandLogo'), 'center button must use BrandLogo');
  assert.ok(nav.includes('variant="mark"'), 'center button renders the hearts mark');
  assert.ok(nav.includes('th-bottom-nav-icon'), 'side items render the icon capsule');
});

// ---------------------------------------------------------------------------
// 4. Design-system classes exist
// ---------------------------------------------------------------------------

test('design system exposes the Stage 3 classes', () => {
  const css = read('src/components/primitives.css');
  for (const cls of [
    '.th-bottom-nav-icon',
    '.th-home-greeting',
    '.th-home-greeting__bell',
    '.th-home-greeting__bell-badge',
    '.th-home-couple-avatars',
    '.th-home-couple-avatars__heart',
    '.th-home-pill',
    '.th-home-card__body',
    '.th-home-card__chevron',
    '.th-home-highlights',
    '.th-home-highlight__label',
    '.th-home-invite',
    '.th-more-hero',
    '.th-more-hero__floral',
    '.th-more-profile',
    '.th-more-profile__chip',
    '.th-more-footer',
    '.th-more-item__body',
  ]) {
    assert.ok(css.includes(cls), `primitives.css must define ${cls}`);
  }
});
