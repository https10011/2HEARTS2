/**
 * Relationship domain models + serializers (Phase 4).
 *
 * The local identity foundation the rest of TwoHearts depends on:
 *
 * - `Profile`            — one row per person. `role` distinguishes the
 *                          device OWNER from the PARTNER; both are plain
 *                          domain entities (UUID identity, tombstoned) so a
 *                          future V2 sync layer can share/merge them.
 * - `CoupleRelationship` — the singleton joining the two profiles plus
 *                          relationship-level data (start date). Exactly one
 *                          row ever exists (SQL-enforced `singleton` guard);
 *                          it still carries UUID identity + timestamps for
 *                          sync-readiness.
 * - `ImportantDate`      — relationship/personal dates (anniversaries,
 *                          birthdays, milestones). `date` is a LOCAL
 *                          calendar day (`yyyy-mm-dd`, see utils/time) —
 *                          never a timestamp. `recurrence` lets future
 *                          reminder/notification features schedule yearly
 *                          repeats without remodeling.
 *
 * Conventions follow Phase 2 (utils/ids, utils/time, Entity): UUID v4 ids,
 * UTC ISO 8601 createdAt/updatedAt, nullable tombstones, explicit
 * serializers — no ad-hoc JSON or casts anywhere else.
 */

import { assertEntityConventions, type Entity, type TombstonedEntity } from '../model/entity.ts';
import {
  optionalString,
  requireEnum,
  requireString,
  type EntitySerializer,
  type Params,
  type Row,
} from '../serialization/entitySerializer.ts';
import { isValidDateKey } from '../../utils/time.ts';

// ---------------------------------------------------------------------------
// Profile
// ---------------------------------------------------------------------------

export const PROFILE_ROLES = ['owner', 'partner'] as const;
export type ProfileRole = (typeof PROFILE_ROLES)[number];

export interface Profile extends TombstonedEntity {
  role: ProfileRole;
  displayName: string;
  /** Local calendar day `yyyy-mm-dd`, or null when not provided. */
  birthDate: string | null;
  /** MediaAsset.id for the profile photo, or null when no photo is set. */
  photoRef: string | null;
}

export function assertProfile(profile: Profile): void {
  assertEntityConventions(profile);
  if (profile.displayName.trim().length === 0) {
    throw new Error('profile.displayName must not be empty');
  }
  if (profile.birthDate !== null && !isValidDateKey(profile.birthDate)) {
    throw new Error('profile.birthDate must be a yyyy-mm-dd calendar date');
  }
}

export const PROFILE_COLUMNS = [
  'id',
  'role',
  'display_name',
  'birth_date',
  'photo_ref',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const profileSerializer: EntitySerializer<Profile> = {
  columns: PROFILE_COLUMNS,
  toParams(profile: Profile): Params {
    return [
      profile.id,
      profile.role,
      profile.displayName,
      profile.birthDate,
      profile.photoRef ?? null,
      profile.createdAt,
      profile.updatedAt,
      profile.deletedAt ?? null,
    ];
  },
  fromRow(row: Row): Profile {
    return {
      id: requireString(row, 'id'),
      role: requireEnum(row, 'role', PROFILE_ROLES),
      displayName: requireString(row, 'display_name'),
      birthDate: optionalString(row, 'birth_date'),
      photoRef: optionalString(row, 'photo_ref'),
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};

// ---------------------------------------------------------------------------
// CoupleRelationship (singleton)
// ---------------------------------------------------------------------------

export interface CoupleRelationship extends Entity {
  /** Profile.id of the owner, or null while unconfigured. */
  ownerProfileId: string | null;
  /** Profile.id of the partner, or null while unconfigured. */
  partnerProfileId: string | null;
  /** Relationship start date (local `yyyy-mm-dd`), or null while unset. */
  startDate: string | null;
}

export function assertCoupleRelationship(relationship: CoupleRelationship): void {
  assertEntityConventions(relationship);
  if (relationship.startDate !== null && !isValidDateKey(relationship.startDate)) {
    throw new Error('couple.startDate must be a yyyy-mm-dd calendar date');
  }
}

export const COUPLE_COLUMNS = [
  'id',
  'owner_profile_id',
  'partner_profile_id',
  'start_date',
  'created_at',
  'updated_at',
] as const;

export const coupleSerializer: EntitySerializer<CoupleRelationship> = {
  columns: COUPLE_COLUMNS,
  toParams(relationship: CoupleRelationship): Params {
    return [
      relationship.id,
      relationship.ownerProfileId,
      relationship.partnerProfileId,
      relationship.startDate,
      relationship.createdAt,
      relationship.updatedAt,
    ];
  },
  fromRow(row: Row): CoupleRelationship {
    return {
      id: requireString(row, 'id'),
      ownerProfileId: optionalString(row, 'owner_profile_id'),
      partnerProfileId: optionalString(row, 'partner_profile_id'),
      startDate: optionalString(row, 'start_date'),
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
    };
  },
};

// ---------------------------------------------------------------------------
// ImportantDate
// ---------------------------------------------------------------------------

export const RECURRENCES = ['none', 'yearly'] as const;
export type Recurrence = (typeof RECURRENCES)[number];

export interface ImportantDate extends TombstonedEntity {
  title: string;
  /** Local calendar day `yyyy-mm-dd`. */
  date: string;
  recurrence: Recurrence;
  /**
   * Profile.id the date belongs to (e.g. a birthday), or null for
   * relationship-level dates (e.g. "first trip together").
   */
  profileId: string | null;
}

export function assertImportantDate(entry: ImportantDate): void {
  assertEntityConventions(entry);
  if (entry.title.trim().length === 0) {
    throw new Error('importantDate.title must not be empty');
  }
  if (!isValidDateKey(entry.date)) {
    throw new Error('importantDate.date must be a yyyy-mm-dd calendar date');
  }
}

export const IMPORTANT_DATE_COLUMNS = [
  'id',
  'title',
  'date',
  'recurrence',
  'profile_id',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const importantDateSerializer: EntitySerializer<ImportantDate> = {
  columns: IMPORTANT_DATE_COLUMNS,
  toParams(entry: ImportantDate): Params {
    return [
      entry.id,
      entry.title,
      entry.date,
      entry.recurrence,
      entry.profileId,
      entry.createdAt,
      entry.updatedAt,
      entry.deletedAt,
    ];
  },
  fromRow(row: Row): ImportantDate {
    return {
      id: requireString(row, 'id'),
      title: requireString(row, 'title'),
      date: requireString(row, 'date'),
      recurrence: requireEnum(row, 'recurrence', RECURRENCES),
      profileId: optionalString(row, 'profile_id'),
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};
