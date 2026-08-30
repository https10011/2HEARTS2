/**
 * Stage 14 — Search + Notification Center presentation helpers.
 *
 * Pure-function helpers for the Search and Notification Center
 * visual productization: result counts, kind labels, time formatting.
 * No DOM, no sql.js, no mocks.
 */

// ---------------------------------------------------------------------------
// Search presentation
// ---------------------------------------------------------------------------

/** Returns the result count label for search results. */
export function resultCountText(count: number): string {
  if (count === 0) return 'No results';
  if (count === 1) return '1 result';
  return `${count} results`;
}

/** Returns a human-friendly "no results" message for a query. */
export function noResultMessage(query: string): string {
  const trimmed = query.trim();
  if (!trimmed) return 'Enter a search term to find your content';
  return `No matches for "${trimmed}"`;
}

/** Returns the search hint shown when the field is empty. */
export function searchHint(): string {
  return 'Search memories, notes, places, reminders...';
}

/** Feature kind → display label with relationship-aware wording. */
const KIND_DISPLAY: Record<string, string> = {
  memory: 'Memory',
  timeline: 'Event',
  place: 'Place',
  reminder: 'Reminder',
  note: 'Note',
};

export function kindLabel(kind: string): string {
  return KIND_DISPLAY[kind] ?? kind;
}

/** Returns a contextual snippet prefix describing the match. */
export function snippetPrefix(kind: string): string {
  switch (kind) {
    case 'memory': return 'Memory';
    case 'timeline': return 'Timeline event';
    case 'place': return 'Place';
    case 'reminder': return 'Reminder';
    case 'note': return 'Note';
    default: return 'Content';
  }
}

// ---------------------------------------------------------------------------
// Notification Center presentation
// ---------------------------------------------------------------------------

/** Returns the unread count label. */
export function unreadCountText(count: number): string {
  if (count === 0) return 'All caught up';
  if (count === 1) return '1 unread';
  return `${count} unread`;
}

/** Notification kind → display label. */
const NOTIFICATION_KIND_LABELS: Record<string, string> = {
  reminder: 'Reminder',
  anniversary: 'Anniversary',
  system: 'Notification',
};

export function notificationKindLabel(kind: string): string {
  return NOTIFICATION_KIND_LABELS[kind] ?? 'Notification';
}

/** Returns a contextual notification empty state title. */
export function notificationEmptyTitle(): string {
  return 'All caught up';
}

/** Returns a contextual notification empty state description. */
export function notificationEmptyDescription(): string {
  return 'No notifications yet. Reminders and important dates will appear here.';
}

/**
 * Formats a relative time string from an ISO date.
 * Returns "Just now", "Xm ago", "Xh ago", "Xd ago", or a date string.
 */
export function formatRelativeTime(isoDate: string): string {
  const now = Date.now();
  const then = new Date(isoDate).getTime();
  const diffMs = now - then;

  if (!Number.isFinite(diffMs) || diffMs < 0) return '';

  const diffMin = Math.floor(diffMs / 60_000);
  if (diffMin < 1) return 'Just now';
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffHr = Math.floor(diffMin / 60);
  if (diffHr < 24) return `${diffHr}h ago`;
  const diffDay = Math.floor(diffHr / 24);
  if (diffDay < 7) return `${diffDay}d ago`;
  return new Date(isoDate).toLocaleDateString();
}

/** Returns a clear-all confirmation message. */
export function clearAllConfirmMessage(): string {
  return 'Clear all notifications?';
}

/** Returns a mark-all-read success toast message. */
export function markAllReadToast(): string {
  return 'All marked as read';
}

/** Returns a clear-all success toast message. */
export function clearAllToast(): string {
  return 'Notifications cleared';
}
