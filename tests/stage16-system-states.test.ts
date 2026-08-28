/**
 * Stage 16 — System States tests.
 *
 * Pure-function / source-guard tests for the standardized system states:
 * ConfirmDialog, StatusBanner, Button danger variant, and CSS vocabulary.
 * No DOM, no sql.js, no mocks.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';

const ROOT = process.cwd();
const read = (rel: string) => readFileSync(join(ROOT, rel), 'utf8');

// ---------------------------------------------------------------------------
// Button danger variant
// ---------------------------------------------------------------------------

describe('Button danger variant', () => {
  it('Button component accepts danger variant', () => {
    const src = read('src/components/Button.tsx');
    assert.ok(src.includes("'danger'"), 'Button must accept danger variant');
  });

  it('componentClassNames includes buttonDanger', () => {
    const mod = read('src/theme/components.ts');
    assert.ok(mod.includes('buttonDanger'), 'componentClassNames must export buttonDanger');
    assert.ok(mod.includes('th-button--danger'), 'buttonDanger must map to th-button--danger');
  });

  it('primitives.css defines th-button--danger', () => {
    const css = read('src/components/primitives.css');
    assert.ok(css.includes('.th-button--danger'), 'CSS must define th-button--danger');
  });
});

// ---------------------------------------------------------------------------
// ConfirmDialog
// ---------------------------------------------------------------------------

describe('ConfirmDialog', () => {
  it('ConfirmDialog component exists and is exported', () => {
    const src = read('src/components/ConfirmDialog.tsx');
    assert.ok(src.includes('export function ConfirmDialog'), 'ConfirmDialog must be exported');
  });

  it('ConfirmDialog is exported from barrel', () => {
    const barrel = read('src/components/index.ts');
    assert.ok(barrel.includes('ConfirmDialog'), 'barrel must export ConfirmDialog');
  });

  it('ConfirmDialog uses Modal internally', () => {
    const src = read('src/components/ConfirmDialog.tsx');
    assert.ok(src.includes('Modal'), 'ConfirmDialog must use Modal');
  });

  it('ConfirmDialog uses Button internally', () => {
    const src = read('src/components/ConfirmDialog.tsx');
    assert.ok(src.includes('Button'), 'ConfirmDialog must use Button');
  });

  it('ConfirmDialog accepts actionVariant prop with danger/primary', () => {
    const src = read('src/components/ConfirmDialog.tsx');
    assert.ok(src.includes("'danger'"), 'ConfirmDialog must support danger variant');
    assert.ok(src.includes("'primary'"), 'ConfirmDialog must support primary variant');
  });

  it('ConfirmDialog CSS is defined in primitives.css', () => {
    const css = read('src/components/primitives.css');
    assert.ok(css.includes('.th-confirm-dialog'), 'CSS must define th-confirm-dialog');
    assert.ok(css.includes('.th-confirm-dialog__title'), 'CSS must define title');
    assert.ok(css.includes('.th-confirm-dialog__description'), 'CSS must define description');
    assert.ok(css.includes('.th-confirm-dialog__actions'), 'CSS must define actions');
  });

  it('ConfirmDialog has standardized button order: action then cancel', () => {
    const src = read('src/components/ConfirmDialog.tsx');
    const actionIdx = src.indexOf('variant={actionVariant}');
    const cancelIdx = src.indexOf("variant=\"ghost\"");
    assert.ok(actionIdx < cancelIdx, 'action button must come before cancel button');
  });
});

// ---------------------------------------------------------------------------
// StatusBanner
// ---------------------------------------------------------------------------

describe('StatusBanner', () => {
  it('StatusBanner component exists and is exported', () => {
    const src = read('src/components/StatusBanner.tsx');
    assert.ok(src.includes('export function StatusBanner'), 'StatusBanner must be exported');
  });

  it('StatusBanner is exported from barrel', () => {
    const barrel = read('src/components/index.ts');
    assert.ok(barrel.includes('StatusBanner'), 'barrel must export StatusBanner');
  });

  it('StatusBanner supports error, success, info variants', () => {
    const src = read('src/components/StatusBanner.tsx');
    assert.ok(src.includes("'error'"), 'StatusBanner must support error');
    assert.ok(src.includes("'success'"), 'StatusBanner must support success');
    assert.ok(src.includes("'info'"), 'StatusBanner must support info');
  });

  it('StatusBanner error uses role="alert"', () => {
    const src = read('src/components/StatusBanner.tsx');
    assert.ok(src.includes("role=\"alert\"") || src.includes("role === 'error' ? 'alert'"), 'error variant must use role="alert"');
  });

  it('StatusBanner success uses role="status"', () => {
    const src = read('src/components/StatusBanner.tsx');
    assert.ok(src.includes("'status'"), 'success variant must use role="status"');
  });

  it('StatusBanner CSS is defined in primitives.css', () => {
    const css = read('src/components/primitives.css');
    assert.ok(css.includes('.th-status-banner'), 'CSS must define th-status-banner');
    assert.ok(css.includes('.th-status-banner--error'), 'CSS must define error variant');
    assert.ok(css.includes('.th-status-banner--success'), 'CSS must define success variant');
    assert.ok(css.includes('.th-status-banner--info'), 'CSS must define info variant');
  });

  it('StatusBanner has dark mode overrides', () => {
    const css = read('src/components/primitives.css');
    assert.ok(css.includes("data-th-theme='dark'] .th-status-banner--error"), 'must have dark mode error');
    assert.ok(css.includes("data-th-theme='dark'] .th-status-banner--success"), 'must have dark mode success');
    assert.ok(css.includes("data-th-theme='dark'] .th-status-banner--info"), 'must have dark mode info');
  });
});

// ---------------------------------------------------------------------------
// Cross-screen consistency: confirm dialogs use ConfirmDialog
// ---------------------------------------------------------------------------

describe('Delete confirmation consistency', () => {
  it('MemoryDetail uses ConfirmDialog, not inline Modal', () => {
    const src = read('src/features/memories/MemoryDetail.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'MemoryDetail must use ConfirmDialog');
    assert.ok(!src.includes('<Modal'), 'MemoryDetail must not have inline Modal delete');
  });

  it('NoteDetail uses ConfirmDialog, not inline Modal', () => {
    const src = read('src/features/notes/NoteDetail.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'NoteDetail must use ConfirmDialog');
    assert.ok(!src.includes('<Modal'), 'NoteDetail must not have inline Modal delete');
  });

  it('EventDetail uses ConfirmDialog, not inline Modal', () => {
    const src = read('src/features/timeline/EventDetail.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'EventDetail must use ConfirmDialog');
    assert.ok(!src.includes('<Modal'), 'EventDetail must not have inline Modal delete');
  });

  it('ReminderDetail uses ConfirmDialog, not raw th-btn classes', () => {
    const src = read('src/features/reminders/ReminderDetail.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'ReminderDetail must use ConfirmDialog');
    assert.ok(!src.includes('th-btn--danger'), 'ReminderDetail must not use raw th-btn--danger');
  });

  it('PlaceDetail uses ConfirmDialog, not inline Modal', () => {
    const src = read('src/features/places/PlaceDetail.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'PlaceDetail must use ConfirmDialog');
    assert.ok(!src.includes('<Modal'), 'PlaceDetail must not have inline Modal delete');
  });

  it('VaultContentViewer uses ConfirmDialog, not raw th-btn classes', () => {
    const src = read('src/features/vault/VaultContentViewer.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'VaultContentViewer must use ConfirmDialog');
    assert.ok(!src.includes('th-btn--danger'), 'VaultContentViewer must not use raw th-btn--danger');
  });

  it('MoodEntry uses ConfirmDialog, not inline Modal', () => {
    const src = read('src/features/mood/MoodEntry.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'MoodEntry must use ConfirmDialog');
    assert.ok(!src.includes('<Modal'), 'MoodEntry must not have inline Modal delete');
  });

  it('SecuritySettingsScreen uses ConfirmDialog for disable lock', () => {
    const src = read('src/features/settings/SecuritySettingsScreen.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'SecuritySettingsScreen must use ConfirmDialog for disable');
  });

  it('StorageSettingsScreen uses ConfirmDialog, not inline Modal', () => {
    const src = read('src/features/settings/StorageSettingsScreen.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'StorageSettingsScreen must use ConfirmDialog');
    assert.ok(!src.includes('<Modal'), 'StorageSettingsScreen must not have inline Modal');
  });

  it('AppearanceSettingsScreen uses ConfirmDialog, not inline Modal', () => {
    const src = read('src/features/settings/AppearanceSettingsScreen.tsx');
    assert.ok(src.includes('ConfirmDialog'), 'AppearanceSettingsScreen must use ConfirmDialog');
    assert.ok(!src.includes('<Modal'), 'AppearanceSettingsScreen must not have inline Modal');
  });
});

// ---------------------------------------------------------------------------
// Modal CSS: standardization
// ---------------------------------------------------------------------------

describe('Modal CSS standardization', () => {
  it('th-modal-sheet uses border-radius xl for top corners', () => {
    const css = read('src/components/primitives.css');
    assert.ok(css.includes('border-radius: var(--th-radius-xl)'), 'modal sheet must use xl radius');
  });

  it('th-modal-sheet uses safe-area padding', () => {
    const css = read('src/components/primitives.css');
    assert.ok(css.includes('var(--th-safe-area-bottom)'), 'modal must account for safe area');
  });
});
