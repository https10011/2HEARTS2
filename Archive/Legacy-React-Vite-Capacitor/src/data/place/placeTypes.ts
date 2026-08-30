/**
 * Place domain model + serializer (Phase 14).
 *
 * Local place storage for the couple's meaningful locations.
 * Follows Phase 2 Entity conventions (UUID v4, UTC ISO timestamps, tombstone).
 *
 * Conventions:
 * - `name`        — place name (required).
 * - `address`     — user-provided address text (optional, local only).
 * - `city`        — city name (optional).
 * - `state`       — state/province (optional).
 * - `country`     — country name (optional).
 * - `latitude`    — optional numeric latitude for local reference.
 * - `longitude`   — optional numeric longitude for local reference.
 * - `notes`       — optional notes about the place.
 * - `category`    — optional user category tag.
 * - `photoRef`    — optional media_asset id for a place photo.
 * - `memoryId`    — optional link to a related Memory.
 * - `createdAt/updatedAt` — UTC ISO 8601 (Entity standard).
 * - `deletedAt`   — tombstone for soft deletes.
 */

import { assertEntityConventions, type TombstonedEntity } from '../model/entity.ts';
import {
  optionalNumber,
  optionalString,
  requireString,
  type EntitySerializer,
  type Params,
  type Row,
} from '../serialization/entitySerializer.ts';

// ---------------------------------------------------------------------------
// Place
// ---------------------------------------------------------------------------

export interface Place extends TombstonedEntity {
  /** Place name. */
  name: string;
  /** User-provided address text. */
  address: string | null;
  /** City name. */
  city: string | null;
  /** State / province. */
  state: string | null;
  /** Country name. */
  country: string | null;
  /** Latitude for local reference. */
  latitude: number | null;
  /** Longitude for local reference. */
  longitude: number | null;
  /** Optional notes about the place. */
  notes: string | null;
  /** User category tag (e.g. 'restaurant', 'vacation', 'home'). */
  category: string | null;
  /** Reference to a media_asset id for the place photo. */
  photoRef: string | null;
  /** Reference to a related Memory id. */
  memoryId: string | null;
}

export type NewPlace = Omit<Place, 'id' | 'createdAt' | 'updatedAt' | 'deletedAt'>;

export function assertPlace(place: Place): void {
  assertEntityConventions(place);
  if (place.name.trim().length === 0) {
    throw new Error('place.name must not be empty');
  }
}

// ---------------------------------------------------------------------------
// Serializer
// ---------------------------------------------------------------------------

export const PLACE_COLUMNS = [
  'id',
  'name',
  'address',
  'city',
  'state',
  'country',
  'latitude',
  'longitude',
  'notes',
  'category',
  'photo_ref',
  'memory_id',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const placeSerializer: EntitySerializer<Place> = {
  columns: PLACE_COLUMNS,
  toParams(place: Place): Params {
    return [
      place.id,
      place.name,
      place.address,
      place.city,
      place.state,
      place.country,
      place.latitude,
      place.longitude,
      place.notes,
      place.category,
      place.photoRef,
      place.memoryId,
      place.createdAt,
      place.updatedAt,
      place.deletedAt,
    ];
  },
  fromRow(row: Row): Place {
    return {
      id: requireString(row, 'id'),
      name: requireString(row, 'name'),
      address: optionalString(row, 'address'),
      city: optionalString(row, 'city'),
      state: optionalString(row, 'state'),
      country: optionalString(row, 'country'),
      latitude: optionalNumber(row, 'latitude'),
      longitude: optionalNumber(row, 'longitude'),
      notes: optionalString(row, 'notes'),
      category: optionalString(row, 'category'),
      photoRef: optionalString(row, 'photo_ref'),
      memoryId: optionalString(row, 'memory_id'),
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};
