/**
 * Timeline repository (Phase 9).
 *
 * Standard entity CRUD via BaseRepository, plus timeline-specific queries:
 * - list sorted by event_date ASC (earliest first) for chronological rendering
 * - list by date range
 * - search by title/description
 */

import {
  TIMELINE_COLUMNS,
  timelineSerializer,
  type TimelineEvent,
} from '../data/timeline/timelineTypes.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { BaseRepository } from './repository.ts';

export class TimelineRepository extends BaseRepository<TimelineEvent> {
  protected readonly table = 'timeline_events';
  protected readonly serializer = timelineSerializer;

  /**
   * Lists active events sorted by event_date ASC (chronological order),
   * then title ASC for same-date events.
   */
  async listEvents(): Promise<TimelineEvent[]> {
    const rows = await this.db.query<Row>(
      `SELECT ${TIMELINE_COLUMNS.join(', ')} FROM timeline_events
       WHERE deleted_at IS NULL
       ORDER BY event_date ASC, title ASC`,
    );
    return rows.map((row) => timelineSerializer.fromRow(row));
  }

  /**
   * Lists events within a date range (inclusive), chronological.
   */
  async listByDateRange(startDate: string, endDate: string): Promise<TimelineEvent[]> {
    const rows = await this.db.query<Row>(
      `SELECT ${TIMELINE_COLUMNS.join(', ')} FROM timeline_events
       WHERE deleted_at IS NULL
         AND event_date >= ? AND event_date <= ?
       ORDER BY event_date ASC, title ASC`,
      [startDate, endDate],
    );
    return rows.map((row) => timelineSerializer.fromRow(row));
  }

  /**
   * Search events by title and description (case-insensitive LIKE).
   */
  async search(query: string): Promise<TimelineEvent[]> {
    const pattern = `%${query}%`;
    const rows = await this.db.query<Row>(
      `SELECT ${TIMELINE_COLUMNS.join(', ')} FROM timeline_events
       WHERE deleted_at IS NULL
         AND (title LIKE ? OR description LIKE ?)
       ORDER BY event_date ASC, title ASC`,
      [pattern, pattern],
    );
    return rows.map((row) => timelineSerializer.fromRow(row));
  }

  /**
   * Returns a description excerpt (first N chars) for an event.
   */
  static excerpt(description: string | null, maxLength = 120): string {
    if (!description) return '';
    if (description.length <= maxLength) return description;
    return description.slice(0, maxLength).trimEnd() + '…';
  }

  /**
   * Counts active events.
   */
  async count(): Promise<number> {
    const rows = await this.db.query<{ n: number }>(
      'SELECT COUNT(1) AS n FROM timeline_events WHERE deleted_at IS NULL',
    );
    return rows[0]?.n ?? 0;
  }
}
