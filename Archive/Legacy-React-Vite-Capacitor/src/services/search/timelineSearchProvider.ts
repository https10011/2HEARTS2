/**
 * Timeline search provider (Phase 18).
 *
 * Integrates timeline events with the existing Phase 3 search infrastructure.
 * Events are searchable by title and description.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import type { SearchProvider, SearchMatch } from './searchEngine.ts';
import { scoreCandidate } from './searchEngine.ts';
import type { NormalizedQuery } from './normalize.ts';
import { TimelineRepository } from '../../repositories/timelineRepository.ts';

export class TimelineSearchProvider implements SearchProvider {
  readonly kind = 'timeline';
  private readonly repository: TimelineRepository;

  constructor(db: DatabaseAdapter) {
    this.repository = new TimelineRepository(db);
  }

  async search(query: NormalizedQuery): Promise<SearchMatch[]> {
    const events = await this.repository.search(query.text);
    const matches: SearchMatch[] = [];

    for (const event of events) {
      const score = scoreCandidate(query, event.title, event.description ?? undefined);
      if (score > 0) {
        matches.push({
          id: event.id,
          kind: 'timeline',
          title: event.title,
          snippet: event.description ? event.description.slice(0, 120) : undefined,
          updatedAt: event.updatedAt,
          score,
        });
      }
    }

    return matches;
  }
}
