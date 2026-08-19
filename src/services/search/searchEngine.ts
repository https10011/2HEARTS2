/**
 * Shared search infrastructure (Phase 3).
 *
 * One engine, one normalization, per-feature PROVIDERS. Features register a
 * provider that turns a normalized query into SearchMatch[] by querying the
 * Phase 2 repositories — NO duplicate index or copied dataset (documented
 * decision: SQLite LIKE scans over local data at couples-app scale are
 * trivially fast; a redundant in-memory index adds failure modes).
 *
 * Ranking is deterministic: prefix match on title > word-initial > substring;
 * tie-break by recency (updatedAt ISO desc), then id asc. Providers may
 * pre-rank; the engine merges and re-ranks globally.
 */

import { AppError } from '../errors/appError.ts';
import { normalizeQuery, normalizeSearchText, type NormalizedQuery } from './normalize.ts';

export interface SearchMatch {
  /** Domain entity id (UUID). */
  id: string;
  /** Owning feature/search kind, e.g. "note", "memory", "place". */
  kind: string;
  title: string;
  /** Optional secondary text (preview); already safe for display. */
  snippet?: string;
  /** ISO 8601 UTC for deterministic ranking tie-breaks. */
  updatedAt: string;
  /** Provider-computed base score (engine may refine). */
  score: number;
}

/** A feature's searchable slice — provided by the feature phase. */
export interface SearchProvider {
  readonly kind: string;
  search(query: NormalizedQuery): Promise<SearchMatch[]>;
}

export interface SearchResults {
  query: NormalizedQuery;
  matches: SearchMatch[];
}

/** Scores one candidate text against the query; 0 when no match. */
export function scoreCandidate(query: NormalizedQuery, title: string, body?: string): number {
  const nTitle = normalizeSearchText(title);
  const nBody = normalizeSearchText(body);
  let score = 0;
  for (const token of query.tokens) {
    let tokenScore = 0;
    if (nTitle === token) tokenScore = Math.max(tokenScore, 4);
    else if (nTitle.startsWith(token)) tokenScore = Math.max(tokenScore, 3);
    else if (nTitle.includes(` ${token}`)) tokenScore = Math.max(tokenScore, 2.5);
    else if (nTitle.includes(token)) tokenScore = Math.max(tokenScore, 2);
    if (nBody.includes(token)) tokenScore = Math.max(tokenScore, 1);
    if (tokenScore === 0) return 0; // every token must match somewhere
    score += tokenScore;
  }
  return score;
}

export class SearchEngine {
  private providers = new Map<string, SearchProvider>();

  /** Feature phases register their provider here (dedup by kind). */
  registerProvider(provider: SearchProvider): void {
    this.providers.set(provider.kind, provider);
  }

  unregisterProvider(kind: string): void {
    this.providers.delete(kind);
  }

  registeredKinds(): string[] {
    return [...this.providers.keys()];
  }

  /**
   * Runs all providers with the normalized query. Empty/invalid queries
   * return empty results (never an error, never a scan). Provider failures
   * are isolated: a broken feature search cannot break the whole search.
   */
  async search(rawQuery: string): Promise<SearchResults> {
    const query = normalizeQuery(rawQuery);
    if (!query) return { query: { text: '', tokens: [] }, matches: [] };

    const matches: SearchMatch[] = [];
    for (const provider of this.providers.values()) {
      try {
        const providerMatches = await provider.search(query);
        matches.push(...providerMatches);
      } catch (cause) {
        // Isolate provider failure; the error is diagnosable via logging by
        // the provider itself. The engine stays silent by contract.
        void cause;
      }
    }
    matches.sort((a, b) => {
      if (b.score !== a.score) return b.score - a.score;
      const byDate = b.updatedAt.localeCompare(a.updatedAt);
      return byDate !== 0 ? byDate : a.id.localeCompare(b.id);
    });
    return { query, matches };
  }

  /** Defensive helper: providers must receive a valid query object. */
  requireQuery(rawQuery: string): NormalizedQuery {
    const query = normalizeQuery(rawQuery);
    if (!query) {
      throw new AppError('validation', 'empty-search-query', {
        recoverable: false,
        userMessage: 'Enter a search term.',
      });
    }
    return query;
  }
}

/** Shared singleton; feature providers attach during their own phase. */
export const searchEngine = new SearchEngine();
