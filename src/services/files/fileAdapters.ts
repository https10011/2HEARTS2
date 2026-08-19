/**
 * FileAdapter implementations (Phase 3).
 *
 * - CapacitorFileAdapter: production (Android). Uses @capacitor/filesystem
 *   under the private app data directory; the ONLY plugin import site for
 *   the files service. Scope: `files/` root.
 * - MemoryFileAdapter: tests/web dev. Byte-level moves/copies so the
 *   service contract can be verified end to end without a device.
 */

import { Filesystem, Directory } from '@capacitor/filesystem';
import { assertSafeRelativePath } from '../../data/media/fileSystem.ts';
import { bytesToBase64, base64ToBytes } from '../../utils/base64.ts';
import type { FileAdapter } from './fileService.ts';

const ROOT = 'files';

function scoped(path: string): string {
  return path.length === 0 ? ROOT : `${ROOT}/${path}`;
}

export const capacitorFileAdapter: FileAdapter = {
  async ensureDir(relativeDir: string): Promise<void> {
    await Filesystem.mkdir({
      path: scoped(relativeDir),
      directory: Directory.Data,
      recursive: true,
    }).catch((error: unknown) => {
      const message = error instanceof Error ? error.message : String(error);
      // mkdir is idempotent for the service contract; "exists" is success.
      if (!/exist/i.test(message)) throw error;
    });
  },
  async write(path: string, data: Uint8Array): Promise<void> {
    const full = scoped(path);
    const parent = full.slice(0, full.lastIndexOf('/'));
    await capacitorFileAdapter.ensureDir(parent.replace(`${ROOT}/`, ''));
    await Filesystem.writeFile({
      path: full,
      data: bytesToBase64(data),
      directory: Directory.Data,
      recursive: true,
    });
  },
  async read(path: string): Promise<Uint8Array> {
    const result = await Filesystem.readFile({ path: scoped(path), directory: Directory.Data });
    return base64ToBytes(result.data as string);
  },
  async exists(path: string): Promise<boolean> {
    try {
      await Filesystem.stat({ path: scoped(path), directory: Directory.Data });
      return true;
    } catch {
      return false;
    }
  },
  async delete(path: string): Promise<void> {
    await Filesystem.deleteFile({ path: scoped(path), directory: Directory.Data });
  },
  async move(from: string, to: string): Promise<void> {
    const fullTo = scoped(to);
    const parent = fullTo.slice(0, fullTo.lastIndexOf('/'));
    await capacitorFileAdapter.ensureDir(parent.replace(`${ROOT}/`, ''));
    await Filesystem.rename({ from: scoped(from), to: fullTo, directory: Directory.Data });
  },
  async copy(from: string, to: string): Promise<void> {
    const data = await capacitorFileAdapter.read(from);
    await capacitorFileAdapter.write(to, data);
  },
  async list(relativeDir: string): Promise<string[]> {
    try {
      const result = await Filesystem.readdir({ path: scoped(relativeDir), directory: Directory.Data });
      return result.files.filter((f) => f.type === 'file').map((f) => f.name);
    } catch {
      return [];
    }
  },
};

export class MemoryFileAdapter implements FileAdapter {
  readonly files = new Map<string, Uint8Array>();

  async ensureDir(): Promise<void> {
    // Directories are implicit in the flat memory map.
  }

  async write(path: string, data: Uint8Array): Promise<void> {
    assertSafeRelativePath(path);
    this.files.set(path, new Uint8Array(data));
  }

  async read(path: string): Promise<Uint8Array> {
    const data = this.files.get(path);
    if (!data) throw new Error(`No such file: ${path}`);
    return new Uint8Array(data);
  }

  async exists(path: string): Promise<boolean> {
    return this.files.has(path);
  }

  async delete(path: string): Promise<void> {
    if (!this.files.delete(path)) throw new Error(`No such file: ${path}`);
  }

  async move(from: string, to: string): Promise<void> {
    const data = this.files.get(from);
    if (!data) throw new Error(`No such file: ${from}`);
    this.files.delete(from);
    this.files.set(to, data);
  }

  async copy(from: string, to: string): Promise<void> {
    const data = this.files.get(from);
    if (!data) throw new Error(`No such file: ${from}`);
    this.files.set(to, new Uint8Array(data));
  }

  async list(relativeDir: string): Promise<string[]> {
    const prefix = relativeDir.length === 0 ? '' : `${relativeDir}/`;
    const names: string[] = [];
    for (const key of this.files.keys()) {
      if (!key.startsWith(prefix)) continue;
      const rest = key.slice(prefix.length);
      if (!rest.includes('/')) names.push(rest);
    }
    return names.sort();
  }
}
