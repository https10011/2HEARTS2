/**
 * Place search provider (Phase 18).
 *
 * Integrates places with the existing Phase 3 search infrastructure.
 * Places are searchable by name, address, city, and notes.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import type { SearchProvider, SearchMatch } from './searchEngine.ts';
import { scoreCandidate } from './searchEngine.ts';
import type { NormalizedQuery } from './normalize.ts';
import { PlaceRepository } from '../../repositories/placeRepository.ts';

export class PlaceSearchProvider implements SearchProvider {
  readonly kind = 'place';
  private readonly repository: PlaceRepository;

  constructor(db: DatabaseAdapter) {
    this.repository = new PlaceRepository(db);
  }

  async search(query: NormalizedQuery): Promise<SearchMatch[]> {
    const places = await this.repository.search(query.text);
    const matches: SearchMatch[] = [];

    for (const place of places) {
      const body = [
        place.address,
        place.city,
        place.state,
        place.country,
        place.notes,
      ]
        .filter(Boolean)
        .join(' ');
      const score = scoreCandidate(query, place.name, body || undefined);
      if (score > 0) {
        matches.push({
          id: place.id,
          kind: 'place',
          title: place.name,
          snippet: body ? body.slice(0, 120) : undefined,
          updatedAt: place.updatedAt,
          score,
        });
      }
    }

    return matches;
  }
}
