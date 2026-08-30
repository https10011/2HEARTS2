/**
 * Stage 12 — Private Vault presentation helpers tests.
 *
 * Pure-function tests for vaultPresentation.ts: content-type labels,
 * item count text, date formatting, relative date, filter options.
 * No DOM, no sql.js, no mocks.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  securityLabel,
  itemCountText,
  formatVaultDate,
  relativeVaultDate,
  VAULT_FILTER_OPTIONS,
} from '../src/features/vault/vaultPresentation.ts';

describe('securityLabel', () => {
  it('returns appropriate labels for each content type', () => {
    assert.equal(securityLabel('note'), 'Private note');
    assert.equal(securityLabel('photo'), 'Protected photo');
    assert.equal(securityLabel('video'), 'Protected video');
    assert.equal(securityLabel('file'), 'Secured file');
  });

  it('returns a fallback for unknown types', () => {
    // @ts-expect-error testing unknown input
    assert.equal(securityLabel('unknown'), 'Vault item');
  });
});

describe('itemCountText', () => {
  it('returns "No items" for zero', () => {
    assert.equal(itemCountText(0), 'No items');
  });

  it('returns singular for one', () => {
    assert.equal(itemCountText(1), '1 item');
  });

  it('returns plural for multiple', () => {
    assert.equal(itemCountText(5), '5 items');
    assert.equal(itemCountText(42), '42 items');
  });
});

describe('formatVaultDate', () => {
  it('formats a valid ISO date', () => {
    const result = formatVaultDate('2026-08-15T12:00:00.000Z');
    // Should contain month, day, year
    assert.ok(result.includes('2026'), `Expected year 2026 in "${result}"`);
    assert.ok(result.includes('15'), `Expected day 15 in "${result}"`);
  });

  it('returns empty for invalid date', () => {
    assert.equal(formatVaultDate('not-a-date'), '');
    assert.equal(formatVaultDate(''), '');
  });
});

describe('relativeVaultDate', () => {
  it('returns "Today" for today\'s date', () => {
    const now = new Date();
    const today = now.toISOString();
    assert.equal(relativeVaultDate(today), 'Today');
  });

  it('returns "Yesterday" for yesterday\'s date', () => {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    assert.equal(relativeVaultDate(yesterday.toISOString()), 'Yesterday');
  });

  it('returns formatted date for older dates', () => {
    const result = relativeVaultDate('2024-01-01T12:00:00.000Z');
    assert.ok(result.includes('2024'), `Expected year 2024 in "${result}"`);
  });

  it('returns empty for invalid input', () => {
    assert.equal(relativeVaultDate('not-a-date'), '');
    assert.equal(relativeVaultDate(''), '');
  });
});

describe('VAULT_FILTER_OPTIONS', () => {
  it('starts with "all" and includes all content types', () => {
    assert.equal(VAULT_FILTER_OPTIONS[0].value, 'all');
    assert.equal(VAULT_FILTER_OPTIONS[0].label, 'All');
    const values = VAULT_FILTER_OPTIONS.map((o) => o.value);
    assert.ok(values.includes('note'));
    assert.ok(values.includes('photo'));
    assert.ok(values.includes('video'));
    assert.ok(values.includes('file'));
  });

  it('has exactly 5 options (all + 4 types)', () => {
    assert.equal(VAULT_FILTER_OPTIONS.length, 5);
  });

  it('every option has a non-empty label', () => {
    for (const opt of VAULT_FILTER_OPTIONS) {
      assert.ok(opt.label.length > 0, `Option ${opt.value} has empty label`);
    }
  });
});
