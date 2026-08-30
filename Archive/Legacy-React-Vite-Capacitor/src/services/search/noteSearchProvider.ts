/**
 * Note search provider (Phase 18).
 *
 * Integrates notes with the existing Phase 3 search infrastructure.
 * Notes are searchable by title and content.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import type { SearchProvider, SearchMatch } from './searchEngine.ts';
import { scoreCandidate } from './searchEngine.ts';
import type { NormalizedQuery } from './normalize.ts';
import { NoteRepository } from '../../repositories/noteRepository.ts';

export class NoteSearchProvider implements SearchProvider {
  readonly kind = 'note';
  private readonly repository: NoteRepository;

  constructor(db: DatabaseAdapter) {
    this.repository = new NoteRepository(db);
  }

  async search(query: NormalizedQuery): Promise<SearchMatch[]> {
    const notes = await this.repository.search(query.text);
    const matches: SearchMatch[] = [];

    for (const note of notes) {
      const score = scoreCandidate(query, note.title, note.content ?? undefined);
      if (score > 0) {
        matches.push({
          id: note.id,
          kind: 'note',
          title: note.title,
          snippet: note.content ? note.content.slice(0, 120) : undefined,
          updatedAt: note.updatedAt,
          score,
        });
      }
    }

    return matches;
  }
}
