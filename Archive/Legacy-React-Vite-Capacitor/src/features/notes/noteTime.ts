/**
 * Relative-time presentation helper for the Notes experience (Stage 6).
 *
 * Pure and injectable — accepts an optional `now` so tests are
 * deterministic. Used for note card timestamps and the editor/detail
 * "last edited" line.
 */

/**
 * Formats an ISO timestamp as a short human relative time:
 * "Just now", "5m ago", "3h ago", "2d ago", then a local date.
 */
export function formatRelativeTime(iso: string, now: Date = new Date()): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  const diffMs = now.getTime() - d.getTime();
  if (diffMs < 0) return 'Just now';
  const diffMins = Math.floor(diffMs / 60000);
  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours}h ago`;
  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) return `${diffDays}d ago`;
  return d.toLocaleDateString();
}

/**
 * "Last edited" phrasing for the editor/detail screens.
 * Same bucketing as formatRelativeTime but with explicit wording.
 */
export function formatLastEdited(iso: string, now: Date = new Date()): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  const diffMs = now.getTime() - d.getTime();
  if (diffMs < 60000) return 'Last edited just now';
  const rel = formatRelativeTime(iso, now);
  if (rel.endsWith(' ago')) return `Last edited ${rel}`;
  return `Last edited ${rel}`;
}
