/**
 * Memory search provider (Phase 18).
 *
 * Integrates memories with the existing Phase 3 search infrastructure.
 * Memories are searchable by title and caption.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import type { SearchProvider, SearchMatch } from './searchEngine.ts';
import { scoreCandidate } from './searchEngine.ts';
import type { NormalizedQuery } from './normalize.ts';
import { MemoryRepository } from '../../repositories/memoryRepository.ts';

export class MemorySearchProvider implements SearchProvider {
  readonly kind = 'memory';
  private readonly repository: MemoryRepository;

  constructor(db: DatabaseAdapter) {
    this.repository = new MemoryRepository(db);
  }

  async search(query: NormalizedQuery): Promise<SearchMatch[]> {
    const memories = await this.repository.search(query.text);
    const matches: SearchMatch[] = [];

    for (const memory of memories) {
      const score = scoreCandidate(query, memory.title, memory.caption ?? undefined);
      if (score > 0) {
        matches.push({
          id: memory.id,
          kind: 'memory',
          title: memory.title,
          snippet: memory.caption ? memory.caption.slice(0, 120) : undefined,
          updatedAt: memory.updatedAt,
          score,
        });
      }
    }

    return matches;
  }
}
