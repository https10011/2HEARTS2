/**
 * Stage 14 — Search + Notification Center presentation helpers tests.
 *
 * Pure-function tests for searchNotificationPresentation.ts:
 * result counts, kind labels, time formatting, empty states, toasts.
 * No DOM, no sql.js, no mocks.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  resultCountText,
  noResultMessage,
  searchHint,
  kindLabel,
  snippetPrefix,
  unreadCountText,
  notificationKindLabel,
  notificationEmptyTitle,
  notificationEmptyDescription,
  formatRelativeTime,
  clearAllConfirmMessage,
  markAllReadToast,
  clearAllToast,
} from '../src/features/notifications/searchNotificationPresentation.ts';

describe('resultCountText', () => {
  it('returns "No results" for zero', () => {
    assert.equal(resultCountText(0), 'No results');
  });

  it('returns singular for one', () => {
    assert.equal(resultCountText(1), '1 result');
  });

  it('returns plural for multiple', () => {
    assert.equal(resultCountText(5), '5 results');
  });
});

describe('noResultMessage', () => {
  it('returns generic message for empty query', () => {
    const msg = noResultMessage('');
    assert.ok(msg.includes('Enter a search term'));
  });

  it('returns query-specific message', () => {
    const msg = noResultMessage('  hello  ');
    assert.ok(msg.includes('"hello"'));
  });
});

describe('searchHint', () => {
  it('returns a non-empty hint', () => {
    const hint = searchHint();
    assert.ok(hint.length > 0);
  });
});

describe('kindLabel', () => {
  it('returns labels for known kinds', () => {
    assert.equal(kindLabel('memory'), 'Memory');
    assert.equal(kindLabel('timeline'), 'Event');
    assert.equal(kindLabel('place'), 'Place');
    assert.equal(kindLabel('reminder'), 'Reminder');
    assert.equal(kindLabel('note'), 'Note');
  });

  it('returns raw kind for unknown', () => {
    assert.equal(kindLabel('unknown'), 'unknown');
  });
});

describe('snippetPrefix', () => {
  it('returns appropriate prefixes', () => {
    assert.equal(snippetPrefix('memory'), 'Memory');
    assert.equal(snippetPrefix('timeline'), 'Timeline event');
    assert.equal(snippetPrefix('note'), 'Note');
  });

  it('returns fallback for unknown', () => {
    assert.equal(snippetPrefix('other'), 'Content');
  });
});

describe('unreadCountText', () => {
  it('returns "All caught up" for zero', () => {
    assert.equal(unreadCountText(0), 'All caught up');
  });

  it('returns singular for one', () => {
    assert.equal(unreadCountText(1), '1 unread');
  });

  it('returns plural for multiple', () => {
    assert.equal(unreadCountText(5), '5 unread');
  });
});

describe('notificationKindLabel', () => {
  it('returns labels for known kinds', () => {
    assert.equal(notificationKindLabel('reminder'), 'Reminder');
    assert.equal(notificationKindLabel('anniversary'), 'Anniversary');
    assert.equal(notificationKindLabel('system'), 'Notification');
  });

  it('returns fallback for unknown', () => {
    assert.equal(notificationKindLabel('unknown'), 'Notification');
  });
});

describe('notificationEmptyTitle', () => {
  it('returns a non-empty title', () => {
    const title = notificationEmptyTitle();
    assert.ok(title.length > 0);
  });
});

describe('notificationEmptyDescription', () => {
  it('returns a non-empty description', () => {
    const desc = notificationEmptyDescription();
    assert.ok(desc.length > 0);
  });
});

describe('formatRelativeTime', () => {
  it('returns "Just now" for very recent dates', () => {
    const now = new Date().toISOString();
    assert.equal(formatRelativeTime(now), 'Just now');
  });

  it('returns minutes ago', () => {
    const d = new Date(Date.now() - 5 * 60_000);
    assert.equal(formatRelativeTime(d.toISOString()), '5m ago');
  });

  it('returns hours ago', () => {
    const d = new Date(Date.now() - 3 * 3600_000);
    assert.equal(formatRelativeTime(d.toISOString()), '3h ago');
  });

  it('returns days ago', () => {
    const d = new Date(Date.now() - 2 * 86400_000);
    assert.equal(formatRelativeTime(d.toISOString()), '2d ago');
  });

  it('returns date string for old dates', () => {
    const result = formatRelativeTime('2024-01-01T12:00:00.000Z');
    assert.ok(result.includes('2024'));
  });

  it('returns empty for invalid input', () => {
    assert.equal(formatRelativeTime('not-a-date'), '');
  });
});

describe('clearAllConfirmMessage', () => {
  it('returns a non-empty message', () => {
    assert.ok(clearAllConfirmMessage().length > 0);
  });
});

describe('markAllReadToast', () => {
  it('returns a non-empty message', () => {
    assert.ok(markAllReadToast().length > 0);
  });
});

describe('clearAllToast', () => {
  it('returns a non-empty message', () => {
    assert.ok(clearAllToast().length > 0);
  });
});
