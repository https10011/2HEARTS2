/**
 * Important date repository (Phase 4).
 *
 * Standard entity CRUD via BaseRepository plus the queries future
 * reminder/notification and timeline features need: per-profile listing
 * (null = relationship-level dates) and recurrence-aware reads. Dates are
 * local `yyyy-mm-dd` keys, so lexical ordering is chronological.
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import {
  importantDateSerializer,
  type ImportantDate,
} from '../data/relationship/relationshipTypes.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { systemClock, type Clock } from '../utils/time.ts';
import { BaseRepository } from './repository.ts';

export class ImportantDateRepository extends BaseRepository<ImportantDate> {
  protected readonly table = 'important_dates';
  protected readonly serializer = importantDateSerializer;

  constructor(db: DatabaseAdapter, clock: Clock = systemClock) {
    super(db, clock);
  }

  /** Dates belonging to one profile (profileId) or the relationship (null). */
  async listForProfile(profileId: string | null): Promise<ImportantDate[]> {
    const rows = await this.db.query<Row>(
      `SELECT ${this.serializer.columns.join(', ')} FROM important_dates
        WHERE deleted_at IS NULL AND ${profileId === null ? 'profile_id IS NULL' : 'profile_id = ?'}
        ORDER BY date ASC, created_at ASC, id ASC`,
      profileId === null ? [] : [profileId],
    );
    return rows.map((row) => this.serializer.fromRow(row));
  }

  /** All yearly-recurring live dates — the reminder feature's input set. */
  async listRecurring(): Promise<ImportantDate[]> {
    const rows = await this.db.query<Row>(
      `SELECT ${this.serializer.columns.join(', ')} FROM important_dates
        WHERE deleted_at IS NULL AND recurrence = 'yearly'
        ORDER BY date ASC, created_at ASC, id ASC`,
    );
    return rows.map((row) => this.serializer.fromRow(row));
  }
}
