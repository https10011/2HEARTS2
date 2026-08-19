/**
 * Search query normalization (Phase 3).
 *
 * One normalization function shared by every future searchable feature:
 * NFKC-fold → strip diacritics → lowercase → collapse whitespace →
 * tokenize (split on whitespace) → length cap. Queries that reduce to
 * nothing are reported via `null` so callers can show an empty state
 * instead of running a meaningless scan.
 */

export interface NormalizedQuery {
  /** Single-space, lowercased, diacritic-free query text. */
  text: string;
  /** Individual tokens (at least one). */
  tokens: string[];
}

const MAX_QUERY_LENGTH = 200;

export function normalizeQuery(raw: string | null | undefined): NormalizedQuery | null {
  if (!raw) return null;
  const text = normalizeSearchText(raw).slice(0, MAX_QUERY_LENGTH);
  if (text.length === 0) return null;
  return { text, tokens: text.split(' ') };
}

/** Field normalization for indexed values — identical rules to queries. */
export function normalizeSearchText(value: string | null | undefined): string {
  if (!value) return '';
  return value
    .normalize('NFKD') // é → e + combining mark
    .replace(/[\u0300-\u036f]/g, '') // strip combining diacritics (U+0300-U+036F)
    .toLowerCase()
    .replace(/\s+/g, ' ')
    .trim();
}
