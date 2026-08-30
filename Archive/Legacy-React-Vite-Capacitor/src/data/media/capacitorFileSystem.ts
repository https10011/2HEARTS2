/**
 * Capacitor-backed media filesystem (Android production).
 *
 * Stores media under `Directory.Data` — the app's private sandbox — so
 * photos/videos stay local-only and offline. Raw paths never escape this
 * file; validation runs before every operation.
 */

import { Directory, Filesystem } from '@capacitor/filesystem';
import { base64ToBytes, bytesToBase64 } from '../../utils/base64.ts';
import { PersistenceError } from '../database/errors.ts';
import { assertSafeRelativeDir, assertSafeRelativePath, type MediaFileSystem } from './fileSystem.ts';

export class CapacitorFileSystem implements MediaFileSystem {
  async write(relativePath: string, data: Uint8Array): Promise<void> {
    assertSafeRelativePath(relativePath);
    try {
      await Filesystem.writeFile({
        path: relativePath,
        data: bytesToBase64(data),
        directory: Directory.Data,
        recursive: true,
      });
    } catch (cause) {
      throw new PersistenceError('media-fs-failed', 'Failed to write media file.', { cause });
    }
  }

  async read(relativePath: string): Promise<Uint8Array> {
    assertSafeRelativePath(relativePath);
    try {
      const result = await Filesystem.readFile({ path: relativePath, directory: Directory.Data });
      return base64ToBytes(result.data as string);
    } catch (cause) {
      throw new PersistenceError('media-missing', 'Media file is missing.', { cause });
    }
  }

  async delete(relativePath: string): Promise<void> {
    assertSafeRelativePath(relativePath);
    try {
      await Filesystem.deleteFile({ path: relativePath, directory: Directory.Data });
    } catch (cause) {
      // Deleting an already-absent file is fine; other failures are reported.
      const exists = await this.exists(relativePath).catch(() => false);
      if (exists) {
        throw new PersistenceError('media-fs-failed', 'Failed to delete media file.', { cause });
      }
    }
  }

  async exists(relativePath: string): Promise<boolean> {
    assertSafeRelativePath(relativePath);
    try {
      await Filesystem.stat({ path: relativePath, directory: Directory.Data });
      return true;
    } catch {
      return false;
    }
  }

  async list(relativeDir: string): Promise<string[]> {
    assertSafeRelativeDir(relativeDir);
    try {
      const result = await Filesystem.readdir({ path: relativeDir, directory: Directory.Data });
      return result.files.map((file) => file.name);
    } catch (cause) {
      throw new PersistenceError('media-fs-failed', 'Failed to list media directory.', { cause });
    }
  }
}
