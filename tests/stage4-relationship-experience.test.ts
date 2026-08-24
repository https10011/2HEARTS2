/**
 * Stage 4 — Us / Relationship experience productization.
 *
 * Locks the Stage 4 contracts on the real source (same pattern as the
 * Phase 21/Phase 24/Stage 3 suites):
 *   - relationshipCounter helpers (pure: next milestone, days→hours/minutes,
 *     human sentence, date formatting);
 *   - Us hub consumes the Phase 24 couple-hub config AND the shared
 *     CouplePair (no duplicate avatar system);
 *   - the dedicated Relationship Counter screen + route exist;
 *   - Important Dates uses the branded DatePicker (no native date input);
 *   - the design system owns the Stage 4 classes.
 */

import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

import {
  decomposedSentence,
  formatDateKeyLong,
  hoursMinutesFromDays,
  nextMilestone,
} from '../src/features/app-shell/relationshipCounter.ts';
import { RoutePath } from '../src/navigation/routes.ts';

function read(path: string): string {
  return readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');
}

test('formatDateKeyLong renders a human date', () => {
  const out = formatDateKeyLong('2023-09-24');
  assert.match(out, /September/);
  assert.match(out, /24/);
  assert.match(out, /2023/);
});

test('hoursMinutesFromDays uses whole-day semantics', () => {
  assert.deepEqual(hoursMinutesFromDays(2), { hours: 48, minutes: 2880 });
  assert.deepEqual(hoursMinutesFromDays(0), { hours: 0, minutes: 0 });
});

test('decomposedSentence builds the human phrase', () => {
  assert.equal(decomposedSentence({ years: 2, months: 10, days: 1 }), '2 years, 10 months, 1 day');
  assert.equal(decomposedSentence({ years: 0, months: 0, days: 1 }), '1 day');
  assert.equal(decomposedSentence({ years: 1, months: 0, days: 0 }), '1 year');
  assert.equal(decomposedSentence(null), '');
});

test('nextMilestone picks the nearest hundred-day mark', () => {
  const m = nextMilestone(128);
  assert.equal(m?.days, 200);
  assert.equal(m?.kind, 'day');
  assert.equal(m?.daysToGo, 72);
  assert.ok(m !== null && m.progress > 0.6 && m.progress < 1);
});

test('nextMilestone prefers a nearer round-year anniversary', () => {
  const m = nextMilestone(340);
  assert.equal(m?.days, 365);
  assert.equal(m?.kind, 'anniversary');
  assert.equal(m?.daysToGo, 25);
});

test('nextMilestone flags an exact milestone today', () => {
  const m = nextMilestone(100);
  assert.equal(m?.daysToGo, 0);
  assert.equal(m?.days, 100);
});

test('nextMilestone rejects invalid ages', () => {
  assert.equal(nextMilestone(-5), null);
  assert.equal(nextMilestone(Number.NaN), null);
});

test('Us hub keeps the Phase 24 hub config while adding the couple card', () => {
  const us = read('src/features/app-shell/screens/UsScreen.tsx');
  assert.ok(us.includes('COUPLE_HUB_GROUPS'), 'UsScreen must render hub groups');
  assert.ok(us.includes('CouplePair'), 'UsScreen must use the shared CouplePair');
  assert.ok(us.includes('th-together-hero'), 'UsScreen must present the together hero');
  assert.ok(us.includes('th-hub-see-all'), 'Coming-up must offer a see-all link');
});

test('Relationship Counter is a dedicated route', () => {
  assert.equal(RoutePath.appUsCounter, '/app/us/counter');
  const router = read('src/navigation/AppRouter.tsx');
  assert.ok(router.includes('RelationshipCounterScreen'));
  const screen = read('src/features/app-shell/screens/RelationshipCounterScreen.tsx');
  for (const cls of ['th-counter-hero', 'th-counter-stats', 'th-counter-milestone', 'th-counter-cta', 'CouplePair']) {
    assert.ok(screen.includes(cls), `counter screen must use ${cls}`);
  }
});

test('Important Dates uses the branded DatePicker, never a native date input', () => {
  const screen = read('src/features/app-shell/screens/ImportantDatesScreen.tsx');
  assert.ok(screen.includes('DatePicker'));
  assert.ok(!screen.includes("type=\"date\""), 'native date input must be gone');
  assert.ok(screen.includes('useToast'));
  assert.ok(screen.includes('th-date-row--past'));
});

test('the design system owns the Stage 4 classes', () => {
  const css = read('src/components/primitives.css');
  for (const cls of [
    '.th-couple-pair',
    '.th-couple-card',
    '.th-together-hero',
    '.th-hub-see-all',
    '.th-us-dates-invite',
    '.th-counter-hero',
    '.th-counter-stats',
    '.th-counter-milestone',
    '.th-counter-linkrow',
    '.th-counter-cta',
    '.th-us-dates-header',
    '.th-date-row__badge',
    '.th-date-row--past',
  ]) {
    assert.ok(css.includes(cls), `primitives.css must define ${cls}`);
  }
});
