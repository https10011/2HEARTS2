/**
 * Media asset metadata repository (Phase 2).
 *
 * The one authoritative table for safe media references. Feature code and
 * the media storage service read/write metadata here; the filesystem bytes
 * are handled by src/data/media.
 */

import type { DatabaseAdapter } from '../data/database/adapter.ts';
import { mediaAssetSerializer, type MediaAsset } from '../data/media/mediaTypes.ts';
import { systemClock, type Clock } from '../utils/time.ts';
import { BaseRepository } from './repository.ts';

export class MediaAssetRepository extends BaseRepository<MediaAsset> {
  protected readonly table = 'media_assets';
  protected readonly serializer = mediaAssetSerializer;

  constructor(db: DatabaseAdapter, clock: Clock = systemClock) {
    super(db, clock);
  }
}
