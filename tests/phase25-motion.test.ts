/**
 * Phase 25 — System-wide motion & micro-interactions tests.
 *
 * Guards the centralized interaction layer (same source-reading style as
 * designTokens.test.ts — no DOM, no mocks):
 *
 *   1. Motion tokens stay centralized: the Motion & Feedback CSS section
 *      and every inline transition in feature screens consume tokens, never
 *      hardcoded durations/easings.
 *   2. One spinner primitive: LoadingState uses `.th-spinner`; the single
 *      `th-spin` keyframe lives in primitives.css and nowhere else.
 *   3. Toast system: centralized host (ToastProvider) mounted once in
 *      AppShell, exported through the components barrel; toasts enter/exit
 *      via tokenized motion and never linger (auto-dismiss constants).
 *   4. Empty states get one calm entrance (th-scale-in), not loops.
 *   5. Reduced motion freezes decorative loops (spinner) in addition to the
 *      token collapse.
 *   6. Theme flips transition smoothly via one scoped global rule.
 *   7. New feedback vocabulary (save/delete toasts) is wired into the
 *      primary feature flows.
 */

import test from 'node:test';
import assert from 'node:assert/strict';
import { readdirSync, readFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';

const ROOT = process.cwd();

function read(path: string): string {
  return readFileSync(join(ROOT, path), 'utf8');
}

function listFiles(dir: string, exts: string[]): string[] {
  const out: string[] = [];
  if (!existsSync(dir)) return out;
  for (const entry of readdirSync(join(ROOT, dir), { withFileTypes: true })) {
    const full = join(dir, entry.name);
    if (entry.isDirectory()) out.push(...listFiles(full, exts));
    else if (exts.some((ext) => full.endsWith(ext))) out.push(full);
  }
  return out;
}

const PRIMITIVES_CSS = read('src/components/primitives.css');
const TOKENS_CSS = read('src/theme/tokens.css');
const GLOBAL_CSS = read('src/styles/global.css');

/** Extract the Phase 25 "Motion & Feedback" section of primitives.css. */
function motionSection(): string {
  const start = PRIMITIVES_CSS.indexOf('Motion & feedback (Phase 25)');
  assert.ok(start >= 0, 'Motion & feedback (Phase 25) section must exist in primitives.css');
  return PRIMITIVES_CSS.slice(start);
}

// ----- 1. Centralized motion vocabulary -----

test('Phase 25 section introduces toast, pressable, empty-entrance primitives', () => {
  const section = motionSection();
  for (const selector of [
    '.th-toast-viewport',
    '.th-toast',
    '.th-toast--exiting',
    '.th-pressable',
    '.th-empty-state__visual',
    '@keyframes th-toast-in',
    '@keyframes th-fade-out',
    '@keyframes th-scale-in',
  ]) {
    assert.ok(section.includes(selector), `missing ${selector}`);
  }
});

test('Phase 25 section uses motion tokens, never hardcoded durations/easings', () => {
  const section = motionSection();
  // No literal ms/s durations or easing curves in the new section.
  assert.ok(!/\d+ms/.test(section), 'hardcoded ms duration in Phase 25 section');
  assert.ok(!/\d\.\d+s/.test(section), 'hardcoded s duration in Phase 25 section');
  assert.ok(!/cubic-bezier/.test(section), 'hardcoded easing in Phase 25 section');
  // And it DOES consume the semantic tokens.
  for (const token of [
    'var(--th-motion-entrance)',
    'var(--th-motion-exit)',
    'var(--th-motion-press)',
    'var(--th-motion-modal)',
    'var(--th-motion-fast)',
  ]) {
    assert.ok(section.includes(token), `missing token usage ${token}`);
  }
});

test('spinner duration is tokenized (--th-duration-spin) and defined once', () => {
  // NB: no colon in this string — designTokens.test.ts flags literal
  // "--th-*:" definitions found outside tokens.css.
  assert.ok(TOKENS_CSS.includes('--th-duration-spin'), 'tokens.css must define --th-duration-spin');
  assert.ok(
    PRIMITIVES_CSS.includes('animation: th-spin var(--th-duration-spin)'),
    '.th-spinner must consume the spin token',
  );
});

// ----- 2. One spinner primitive -----

test('LoadingState uses the shared .th-spinner (no inline keyframes duplication)', () => {
  const loadingState = read('src/components/LoadingState.tsx');
  assert.ok(loadingState.includes('th-spinner'), 'LoadingState must use .th-spinner');
  assert.ok(
    !loadingState.includes('<style>'),
    'LoadingState must not inject a duplicate th-spin keyframe block',
  );
  assert.ok(
    !loadingState.includes('animation:'),
    'LoadingState must not style animation inline',
  );
});

test('th-spin keyframe exists exactly once app-wide', () => {
  const all = [...listFiles('src', ['.css']), ...listFiles('src', ['.tsx'])];
  let count = 0;
  for (const f of all) count += (read(f).match(/@keyframes th-spin/g) ?? []).length;
  assert.equal(count, 1, 'th-spin must be defined in exactly one file');
});

// ----- 3. Centralized toast system -----

test('toast module exports provider + hook and is surfaced via the barrel', () => {
  const toast = read('src/components/toast.tsx');
  for (const symbol of ['ToastProvider', 'useToast', 'TOAST_DURATION_MS']) {
    assert.ok(toast.includes(symbol), `toast.tsx missing ${symbol}`);
  }
  const barrel = read('src/components/index.ts');
  assert.ok(barrel.includes('./toast'), 'components barrel must export the toast module');
});

test('AppShell mounts exactly one toast host', () => {
  const shell = read('src/features/app-shell/AppShell.tsx');
  const mounts = (shell.match(/<ToastProvider>/g) ?? []).length;
  assert.equal(mounts, 1, 'AppShell must mount the single ToastProvider');
  // No screen-local toast hosts anywhere else in features.
  for (const f of listFiles('src/features', ['.tsx'])) {
    if (f.endsWith('app-shell/AppShell.tsx')) continue;
    assert.ok(!read(f).includes('<ToastProvider>'), `${f} mounts a duplicate toast host`);
  }
});

test('toast CSS sits above the bottom nav with tokenized motion', () => {
  const section = motionSection();
  assert.ok(section.includes('bottom: calc(var(--th-bottom-nav-height)'), 'toast viewport must clear the nav');
  assert.ok(section.includes('animation: th-toast-in var(--th-motion-entrance)'), 'entrance must be tokenized');
  assert.ok(section.includes('animation: th-fade-out var(--th-motion-exit)'), 'exit must be tokenized');
});

// ----- 4. Inline transitions in feature screens are tokenized -----

test('no hardcoded inline transitions remain in feature screens', () => {
  for (const f of listFiles('src/features', ['.tsx'])) {
    const src = read(f);
    for (const m of src.matchAll(/transition:\s*'([^']+)'/g)) {
      const value = m[1];
      assert.ok(
        value.includes('var(--th-'),
        `${f}: inline transition "${value}" must consume motion tokens`,
      );
    }
  }
});

// ----- 5. Reduced motion -----

test('reduced motion freezes the interactive spinner loop', () => {
  const section = motionSection();
  assert.ok(
    section.includes('@media (prefers-reduced-motion: reduce)'),
    'Phase 25 section must honor OS reduced motion',
  );
  assert.ok(
    section.includes("[data-th-motion='reduced'] .th-spinner"),
    'in-app reduced motion must freeze the spinner',
  );
});

// ----- 6. Theme transition -----

test('theme flips animate surface colors via one scoped global rule', () => {
  assert.ok(
    GLOBAL_CSS.includes('Theme transition (Phase 25)'),
    'global.css must carry the Phase 25 theme transition rule',
  );
  assert.ok(
    GLOBAL_CSS.includes('transition:\n    background-color var(--th-duration-normal)'),
    'theme transition must use the standard duration token',
  );
});

// ----- 7. Feedback wiring in primary flows -----

test('save/delete flows publish toast feedback', () => {
  const expectations: Array<[string, string[]]> = [
    ['src/features/notes/NoteEditor.tsx', ["toast.success('Note saved')"]],
    ['src/features/notes/NoteDetail.tsx', ["toast.success('Note deleted')"]],
    ['src/features/memories/AddMemory.tsx', ["toast.success('Memory saved')"]],
    ['src/features/memories/MemoryDetail.tsx', ["toast.success('Memory deleted')"]],
    ['src/features/reminders/CreateReminder.tsx', ["toast.success('Reminder saved')"]],
    ['src/features/reminders/ReminderDetail.tsx', ["toast.success('Reminder deleted')"]],
    ['src/features/places/CreatePlace.tsx', ["toast.success('Place saved')"]],
    ['src/features/places/PlaceDetail.tsx', ["toast.success('Place deleted')"]],
    ['src/features/mood/MoodEntry.tsx', ["toast.success('Mood saved')"]],
  ];
  for (const [file, snippets] of expectations) {
    const src = read(file);
    assert.ok(src.includes('useToast'), `${file} must consume the toast hook`);
    for (const snippet of snippets) {
      assert.ok(src.includes(snippet), `${file} missing "${snippet}"`);
    }
    // Failures must surface as error feedback — never silently swallowed.
    if (file.includes('Editor') || file.includes('Add') || file.includes('Create') || file.includes('Entry')) {
      assert.ok(src.includes('toast.error('), `${file} must surface save failures via toast.error`);
    }
  }
});
