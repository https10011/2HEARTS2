/**
 * Media storage service (Phase 2 foundation).
 *
 * Coordinates metadata (media_assets table) and bytes (MediaFileSystem) so
 * feature code never touches either raw layer. SQL and the filesystem can't
 * share one transaction, so each operation uses a compensation pattern: the
 * inner work runs inside a SQL transaction and the filesystem side-effect is
 * rolled back when the write fails (or vice versa).
 *
 * Safe references: UI receives `MediaReference` (id + mime) and resolves a
 * `data:` URL through `resolveUrl`. Raw paths stay inside this module.
 */

import { PERSISTENCE_CONFIG } from '../../config/persistence.ts';
import type { DatabaseAdapter } from '../database/adapter.ts';
import { PersistenceError } from '../database/errors.ts';
import type { Row } from '../serialization/entitySerializer.ts';
import { bytesToBase64 } from '../../utils/base64.ts';
import { newId } from '../../utils/ids.ts';
import { nowIso, type Clock, systemClock } from '../../utils/time.ts';
import {
  MEDIA_EXTENSION_BY_MIME,
  MEDIA_MIME_BY_KIND,
  assertMediaAsset,
  mediaAssetSerializer,
  type MediaAsset,
  type MediaKind,
} from './mediaTypes.ts';
import type { MediaFileSystem } from './fileSystem.ts';

export interface MediaReference {
  id: string;
  kind: MediaKind;
  mimeType: string;
}

export class MediaStorage {
  constructor(
    private readonly db: DatabaseAdapter,
    private readonly fs: MediaFileSystem,
    private readonly clock: Clock = systemClock,
  ) {}

  /**
   * Stores media bytes + metadata atomically (SQL transaction with a
   * filesystem compensation action).
   */
  async store(kind: MediaKind, mimeType: string, bytes: Uint8Array): Promise<MediaReference> {
    if (!MEDIA_MIME_BY_KIND[kind].includes(mimeType)) {
      throw new PersistenceError('serialization-failed', `Unsupported ${kind} mime type.`);
    }
    const id = newId();
    const extension = MEDIA_EXTENSION_BY_MIME[mimeType] ?? 'bin';
    const relativePath = `${PERSISTENCE_CONFIG.mediaRoot}/${kind}s/${id}.${extension}`;
    const timestamp = nowIso(this.clock);
    const asset: MediaAsset = {
      id,
      kind,
      mimeType,
      relativePath,
      sizeBytes: bytes.length,
      createdAt: timestamp,
      updatedAt: timestamp,
      deletedAt: null,
    };
    assertMediaAsset(asset);

    let fileWritten = false;
    try {
      await this.db.transaction(async (tx) => {
        await tx.run(
          `INSERT INTO media_assets (${mediaAssetSerializer.columns.join(', ')}) VALUES (${mediaAssetSerializer.columns.map(() => '?').join(', ')})`,
          mediaAssetSerializer.toParams(asset),
        );
        await this.fs.write(relativePath, bytes);
        fileWritten = true;
      });
    } catch (cause) {
      if (fileWritten) {
        await this.fs.delete(relativePath).catch(() => undefined);
      }
      if (cause instanceof PersistenceError) throw cause;
      throw new PersistenceError('media-fs-failed', 'Media store failed.', { cause });
    }
    return asset;
  }

  /** Metadata by id (active only). Throws when absent or deleted. */
  async getMetadata(id: string): Promise<MediaAsset> {
    const assets = await this.loadAsset(id);
    if (!assets) {
      throw new PersistenceError('media-missing', `Media ${id} not found.`);
    }
    return assets;
  }

  /** Resolves a `data:` URL for UI rendering; never exposes the raw path. */
  async resolveUrl(id: string): Promise<string> {
    const asset = await this.getMetadata(id);
    const bytes = await this.fs.read(asset.relativePath);
    return `data:${asset.mimeType};base64,${bytesToBase64(bytes)}`;
  }

  /** Tombstones metadata, then removes the file (best effort post-commit). */
  async delete(id: string): Promise<boolean> {
    const asset = await this.loadAsset(id);
    if (!asset) return false;
    const deletedAt = nowIso(this.clock);
    await this.db.transaction(async (tx) => {
      const result = await tx.run(
        'UPDATE media_assets SET deleted_at = ?, updated_at = ? WHERE id = ?',
        [deletedAt, deletedAt, id],
      );
      if (result.changes === 0) {
        throw new PersistenceError('media-missing', `Media ${id} not found.`);
      }
      await this.fs.delete(asset.relativePath);
    });
    return true;
  }

  async exists(id: string): Promise<boolean> {
    return (await this.loadAsset(id)) !== null;
  }

  /** Files present on disk with no active metadata (crash leftovers). */
  async findOrphans(): Promise<string[]> {
    const active = await this.db.query<{ relative_path: string }>(
      'SELECT relative_path FROM media_assets WHERE deleted_at IS NULL',
    );
    const known = new Set(active.map((row) => row.relative_path));
    const onDisk = await this.listAllMediaFiles();
    return onDisk.filter((path) => !known.has(path));
  }

  /** Deletes orphaned files. Returns how many were removed. */
  async sweepOrphans(): Promise<number> {
    const orphans = await this.findOrphans();
    let removed = 0;
    for (const path of orphans) {
      await this.fs.delete(path);
      removed += 1;
    }
    return removed;
  }

  private async loadAsset(id: string): Promise<MediaAsset | null> {
    const rows = await this.db.query(
      'SELECT * FROM media_assets WHERE id = ? AND deleted_at IS NULL',
      [id],
    );
    if (rows.length === 0) return null;
    return mediaAssetSerializer.fromRow(rows[0] as Row);
  }

  private async listAllMediaFiles(): Promise<string[]> {
    const paths: string[] = [];
    for (const kind of ['photo', 'video'] as MediaKind[]) {
      const dir = `${PERSISTENCE_CONFIG.mediaRoot}/${kind}s`;
      const names = await this.fs.list(dir).catch(() => [] as string[]);
      for (const name of names) {
        paths.push(`${dir}/${name}`);
      }
    }
    return paths;
  }
}
