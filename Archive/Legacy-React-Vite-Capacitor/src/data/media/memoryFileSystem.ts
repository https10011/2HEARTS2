/**
 * In-memory media filesystem (tests and non-native harnesses).
 *
 * Enforces the exact same safe-reference validation as production so tests
 * exercise the real boundary rules without touching a device filesystem.
 */

import { PersistenceError } from '../database/errors.ts';
import { assertSafeRelativeDir, assertSafeRelativePath, type MediaFileSystem } from './fileSystem.ts';

export class MemoryFileSystem implements MediaFileSystem {
  private readonly files = new Map<string, Uint8Array>();

  async write(relativePath: string, data: Uint8Array): Promise<void> {
    assertSafeRelativePath(relativePath);
    this.files.set(relativePath, new Uint8Array(data));
  }

  async read(relativePath: string): Promise<Uint8Array> {
    assertSafeRelativePath(relativePath);
    const data = this.files.get(relativePath);
    if (!data) {
      throw new PersistenceError('media-missing', 'Media file is missing.');
    }
    return new Uint8Array(data);
  }

  async delete(relativePath: string): Promise<void> {
    assertSafeRelativePath(relativePath);
    this.files.delete(relativePath);
  }

  async exists(relativePath: string): Promise<boolean> {
    assertSafeRelativePath(relativePath);
    return this.files.has(relativePath);
  }

  async list(relativeDir: string): Promise<string[]> {
    assertSafeRelativeDir(relativeDir);
    const prefix = relativeDir.length === 0 ? '' : `${relativeDir.replace(/\/$/, '')}/`;
    const names = new Set<string>();
    for (const path of this.files.keys()) {
      if (!path.startsWith(prefix)) continue;
      const rest = path.slice(prefix.length);
      if (!rest.includes('/') && rest.length > 0) {
        names.add(rest);
      }
    }
    return [...names].sort();
  }
}
