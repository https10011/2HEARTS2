/**
 * Stage 15 — Settings presentation helpers tests.
 *
 * Pure-function tests for settingsPresentation.ts: theme labels, text size
 * descriptions, motion labels, permission statuses, section metadata, info
 * card messages. No DOM, no sql.js, no mocks.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  THEME_OPTIONS,
  themeLabel,
  themeDescription,
  TEXT_SIZE_OPTIONS,
  textSizeLabel,
  textSizeDescription,
  motionLabel,
  motionDescription,
  permissionStatusLabel,
  permissionStatusDescription,
  lockStatusLabel,
  lockMethodLabel,
  SETTINGS_SECTIONS,
  privacyInfoTitle,
  privacyInfoText,
  securityInfoTitle,
  securityInfoText,
} from '../src/features/settings/settingsPresentation.ts';

describe('THEME_OPTIONS', () => {
  it('has light, dark, and system options', () => {
    assert.equal(THEME_OPTIONS.length, 3);
    const values = THEME_OPTIONS.map((o) => o.value);
    assert.ok(values.includes('light'));
    assert.ok(values.includes('dark'));
    assert.ok(values.includes('system'));
  });

  it('each option has label, description, and preview', () => {
    for (const opt of THEME_OPTIONS) {
      assert.ok(opt.label.length > 0, `Missing label for ${opt.value}`);
      assert.ok(opt.description.length > 0, `Missing description for ${opt.value}`);
      assert.ok(opt.preview.length > 0, `Missing preview for ${opt.value}`);
    }
  });
});

describe('themeLabel', () => {
  it('returns label for known mode', () => {
    assert.equal(themeLabel('light'), 'Light');
    assert.equal(themeLabel('dark'), 'Dark');
    assert.equal(themeLabel('system'), 'System');
  });

  it('returns raw mode for unknown', () => {
    assert.equal(themeLabel('unknown' as never), 'unknown');
  });
});

describe('themeDescription', () => {
  it('returns description for known mode', () => {
    assert.equal(themeDescription('light'), 'Warm and bright');
    assert.equal(themeDescription('dark'), 'Soft and low-light');
    assert.equal(themeDescription('system'), 'Follow your device');
  });

  it('returns empty for unknown', () => {
    assert.equal(themeDescription('unknown' as never), '');
  });
});

describe('TEXT_SIZE_OPTIONS', () => {
  it('has 4 options', () => {
    assert.equal(TEXT_SIZE_OPTIONS.length, 4);
  });

  it('each has label and description', () => {
    for (const opt of TEXT_SIZE_OPTIONS) {
      assert.ok(opt.label.length > 0, `Missing label for ${opt.value}`);
      assert.ok(opt.description.length > 0, `Missing description for ${opt.value}`);
    }
  });
});

describe('textSizeLabel', () => {
  it('returns label for known size', () => {
    assert.equal(textSizeLabel('small'), 'Small');
    assert.equal(textSizeLabel('default'), 'Default');
    assert.equal(textSizeLabel('large'), 'Large');
    assert.equal(textSizeLabel('extra-large'), 'Extra Large');
  });
});

describe('textSizeDescription', () => {
  it('returns description for known size', () => {
    assert.equal(textSizeDescription('small'), 'Compact text');
    assert.equal(textSizeDescription('extra-large'), 'Maximum readability');
  });

  it('returns empty for unknown', () => {
    assert.equal(textSizeDescription('huge' as never), '');
  });
});

describe('motionLabel', () => {
  it('returns Reduced when true', () => {
    assert.equal(motionLabel(true), 'Reduced');
  });

  it('returns Standard when false', () => {
    assert.equal(motionLabel(false), 'Standard');
  });
});

describe('motionDescription', () => {
  it('returns reduced description when true', () => {
    assert.ok(motionDescription(true).length > 0);
  });

  it('returns standard description when false', () => {
    assert.ok(motionDescription(false).length > 0);
  });
});

describe('permissionStatusLabel', () => {
  it('returns human labels for all states', () => {
    assert.equal(permissionStatusLabel('granted'), 'Allowed');
    assert.equal(permissionStatusLabel('denied'), 'Blocked');
    assert.equal(permissionStatusLabel('prompt'), 'Not yet requested');
    assert.equal(permissionStatusLabel('unavailable'), 'Not available');
  });
});

describe('permissionStatusDescription', () => {
  it('returns descriptions for all states', () => {
    assert.ok(permissionStatusDescription('granted').length > 0);
    assert.ok(permissionStatusDescription('denied').length > 0);
    assert.ok(permissionStatusDescription('prompt').length > 0);
    assert.ok(permissionStatusDescription('unavailable').length > 0);
  });
});

describe('lockStatusLabel', () => {
  it('returns Protected when enabled', () => {
    assert.equal(lockStatusLabel(true), 'Protected');
  });

  it('returns Unprotected when disabled', () => {
    assert.equal(lockStatusLabel(false), 'Unprotected');
  });
});

describe('lockMethodLabel', () => {
  it('returns PIN', () => {
    assert.equal(lockMethodLabel(), 'PIN');
  });
});

describe('SETTINGS_SECTIONS', () => {
  it('has 6 sections', () => {
    assert.equal(SETTINGS_SECTIONS.length, 6);
  });

  it('each has id, title, description', () => {
    for (const section of SETTINGS_SECTIONS) {
      assert.ok(section.id.length > 0, `Missing id`);
      assert.ok(section.title.length > 0, `Missing title for ${section.id}`);
      assert.ok(section.description.length > 0, `Missing description for ${section.id}`);
    }
  });

  it('includes personal, experience, security, storage, about', () => {
    const ids = SETTINGS_SECTIONS.map((s) => s.id);
    assert.ok(ids.includes('personal'));
    assert.ok(ids.includes('experience'));
    assert.ok(ids.includes('security'));
    assert.ok(ids.includes('storage'));
    assert.ok(ids.includes('about'));
  });
});

describe('privacyInfoTitle', () => {
  it('returns a non-empty title', () => {
    assert.ok(privacyInfoTitle().length > 0);
  });
});

describe('privacyInfoText', () => {
  it('returns a non-empty text', () => {
    assert.ok(privacyInfoText().length > 0);
  });
});

describe('securityInfoTitle', () => {
  it('returns a non-empty title', () => {
    assert.ok(securityInfoTitle().length > 0);
  });
});

describe('securityInfoText', () => {
  it('returns a non-empty text', () => {
    assert.ok(securityInfoText().length > 0);
  });
});
