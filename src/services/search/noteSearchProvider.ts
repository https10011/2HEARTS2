/**
 * Note search provider (Phase 8).
 *
 * Integrates notes with the existing Phase 3 search infrastructure.
 * Notes are searchable by title and content. The provider queries
 * the NoteRepository directly — no duplicate index.
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
      const score = scoreCandidate(query, note.title, note.content);
      if (score > 0) {
        matches.push({
          id: note.id,
          kind: 'note',
          title: note.title,
          snippet: NoteRepository.excerpt(note.content, 120),
          updatedAt: note.updatedAt,
          score,
        });
      }
    }

    return matches;
  }
}
