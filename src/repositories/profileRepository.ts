/**
 * Profile repository (Phase 4).
 *
 * Standard entity CRUD (Phase 2 BaseRepository) plus role lookups. The SQL
 * unique index on live roles guarantees at most one owner and one partner;
 * the repository surfaces that invariant as getOwner()/getPartner().
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import { profileSerializer, type Profile, type ProfileRole } from '../data/relationship/relationshipTypes.ts';
import type { Row } from '../data/serialization/entitySerializer.ts';
import { systemClock, type Clock } from '../utils/time.ts';
import { BaseRepository } from './repository.ts';

export class ProfileRepository extends BaseRepository<Profile> {
  protected readonly table = 'profiles';
  protected readonly serializer = profileSerializer;

  constructor(db: DatabaseAdapter, clock: Clock = systemClock) {
    super(db, clock);
  }

  /** The live profile for a role, or null when not yet configured. */
  async getByRole(role: ProfileRole): Promise<Profile | null> {
    const rows = await this.db.query<Row>(
      `SELECT ${this.serializer.columns.join(', ')} FROM profiles WHERE role = ? AND deleted_at IS NULL`,
      [role],
    );
    return rows[0] ? this.serializer.fromRow(rows[0]) : null;
  }

  async getOwner(): Promise<Profile | null> {
    return this.getByRole('owner');
  }

  async getPartner(): Promise<Profile | null> {
    return this.getByRole('partner');
  }
}
