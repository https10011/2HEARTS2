/**
 * Couple relationship repository (Phase 4).
 *
 * NOT a BaseRepository: the couple is a SQL-enforced singleton, so CRUD
 * collapses to get()/save(). Identity still follows Phase 2 conventions
 * (UUID id, UTC ISO timestamps) for future sync-readiness.
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import {
  COUPLE_COLUMNS,
  coupleSerializer,
  type CoupleRelationship,
} from '../data/relationship/relationshipTypes.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { newId } from '../utils/ids.ts';
import { nowIso, systemClock, type Clock } from '../utils/time.ts';

export interface CoupleConfiguration {
  ownerProfileId: string | null;
  partnerProfileId: string | null;
  startDate: string | null;
}

export class CoupleRepository {
  constructor(
    private readonly db: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {}

  /** The couple row, or null before any relationship data exists. */
  async get(): Promise<CoupleRelationship | null> {
    const rows = await this.db.query<Row>(
      `SELECT ${COUPLE_COLUMNS.join(', ')} FROM couple_relationship WHERE singleton = 1`,
    );
    return rows[0] ? coupleSerializer.fromRow(rows[0]) : null;
  }

  /**
   * Creates the singleton row when missing, otherwise applies the changes.
   * Identity (id, createdAt) is preserved across saves; updatedAt refreshes.
   */
  async save(configuration: CoupleConfiguration): Promise<CoupleRelationship> {
    const existing = await this.get();
    const now = nowIso(this.clock);
    if (!existing) {
      const relationship: CoupleRelationship = {
        id: newId(),
        ownerProfileId: configuration.ownerProfileId,
        partnerProfileId: configuration.partnerProfileId,
        startDate: configuration.startDate,
        createdAt: now,
        updatedAt: now,
      };
      await this.db.run(
        `INSERT INTO couple_relationship (${COUPLE_COLUMNS.join(', ')}) VALUES (${COUPLE_COLUMNS.map(() => '?').join(', ')})`,
        coupleSerializer.toParams(relationship),
      );
      return relationship;
    }
    const updated: CoupleRelationship = {
      ...existing,
      ownerProfileId: configuration.ownerProfileId,
      partnerProfileId: configuration.partnerProfileId,
      startDate: configuration.startDate,
      updatedAt: now,
    };
    await this.db.run(
      `UPDATE couple_relationship
         SET owner_profile_id = ?, partner_profile_id = ?, start_date = ?, updated_at = ?
       WHERE singleton = 1`,
      [updated.ownerProfileId, updated.partnerProfileId, updated.startDate, updated.updatedAt],
    );
    return updated;
  }
}
