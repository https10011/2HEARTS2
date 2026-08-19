/**
 * Media domain model + serializer (Phase 2).
 *
 * `MediaAsset` is METADATA ONLY — photo/video binaries live on the device
 * filesystem; the database holds the safe reference (`relativePath`) and
 * bookkeeping fields. The relative path is the only persistence-level
 * locator; UI code never sees it (see mediaStorage.ts, MediaReference).
 */

import { assertEntityConventions, type TombstonedEntity } from '../model/entity.ts';
import {
  optionalNumber,
  optionalString,
  requireEnum,
  requireString,
  type EntitySerializer,
  type Params,
  type Row,
} from '../serialization/entitySerializer.ts';

export const MEDIA_KINDS = ['photo', 'video'] as const;
export type MediaKind = (typeof MEDIA_KINDS)[number];

export const MEDIA_MIME_BY_KIND: Record<MediaKind, readonly string[]> = {
  photo: ['image/jpeg', 'image/png', 'image/webp'],
  video: ['video/mp4', 'video/webm'],
};

export const MEDIA_EXTENSION_BY_MIME: Record<string, string> = {
  'image/jpeg': 'jpg',
  'image/png': 'png',
  'image/webp': 'webp',
  'video/mp4': 'mp4',
  'video/webm': 'webm',
};

export interface MediaAsset extends TombstonedEntity {
  kind: MediaKind;
  mimeType: string;
  /** Safe relative reference, e.g. "media/photos/<id>.jpg". Not a raw path. */
  relativePath: string;
  sizeBytes: number | null;
}

export function assertMediaAsset(asset: MediaAsset): void {
  assertEntityConventions(asset);
  if (!MEDIA_MIME_BY_KIND[asset.kind].includes(asset.mimeType)) {
    throw new Error(`mime type ${asset.mimeType} is not valid for kind ${asset.kind}`);
  }
}

export const MEDIA_ASSET_COLUMNS = [
  'id',
  'kind',
  'mime_type',
  'relative_path',
  'size_bytes',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const mediaAssetSerializer: EntitySerializer<MediaAsset> = {
  columns: MEDIA_ASSET_COLUMNS,
  toParams(asset: MediaAsset): Params {
    return [
      asset.id,
      asset.kind,
      asset.mimeType,
      asset.relativePath,
      asset.sizeBytes,
      asset.createdAt,
      asset.updatedAt,
      asset.deletedAt,
    ];
  },
  fromRow(row: Row): MediaAsset {
    const asset: MediaAsset = {
      id: requireString(row, 'id'),
      kind: requireEnum(row, 'kind', MEDIA_KINDS),
      mimeType: requireString(row, 'mime_type'),
      relativePath: requireString(row, 'relative_path'),
      sizeBytes: optionalNumber(row, 'size_bytes'),
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
    return asset;
  },
};
