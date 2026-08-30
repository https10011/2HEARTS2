/**
 * Reminder search provider (Phase 18).
 *
 * Integrates reminders with the existing Phase 3 search infrastructure.
 * Reminders are searchable by title and description.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import type { SearchProvider, SearchMatch } from './searchEngine.ts';
import { scoreCandidate } from './searchEngine.ts';
import type { NormalizedQuery } from './normalize.ts';
import { ReminderRepository } from '../../repositories/reminderRepository.ts';

export class ReminderSearchProvider implements SearchProvider {
  readonly kind = 'reminder';
  private readonly repository: ReminderRepository;

  constructor(db: DatabaseAdapter) {
    this.repository = new ReminderRepository(db);
  }

  async search(query: NormalizedQuery): Promise<SearchMatch[]> {
    const reminders = await this.repository.search(query.text);
    const matches: SearchMatch[] = [];

    for (const reminder of reminders) {
      const score = scoreCandidate(query, reminder.title, reminder.description ?? undefined);
      if (score > 0) {
        matches.push({
          id: reminder.id,
          kind: 'reminder',
          title: reminder.title,
          snippet: reminder.description ? reminder.description.slice(0, 120) : undefined,
          updatedAt: reminder.updatedAt,
          score,
        });
      }
    }

    return matches;
  }
}
