/**
 * Generic local file utilities (Phase 3).
 *
 * Media (photos/videos) already owns `media/` via src/data/media — this is
 * the companion for NON-media app files: export payloads, cached documents,
 * working files. Same discipline: one bounded adapter, generated/validated
 * relative names only, private app directory, no UI→plugin access.
 *
 * Root: `files/` under the private app data directory (Capacitor
 * Directory.Data via the same permission-free scope as media).
 */

import { AppError, normalizeAppError } from '../errors/appError.ts';
import { assertSafeRelativeDir, assertSafeRelativePath } from '../../data/media/fileSystem.ts';

/** The surface BOTH file adapters must satisfy. */
export interface FileAdapter {
  ensureDir(relativeDir: string): Promise<void>;
  write(path: string, data: Uint8Array): Promise<void>;
  read(path: string): Promise<Uint8Array>;
  exists(path: string): Promise<boolean>;
  delete(path: string): Promise<void>;
  /** Moves a file; fails when the destination already exists. */
  move(from: string, to: string): Promise<void>;
  /** Copies a file; fails when the destination already exists. */
  copy(from: string, to: string): Promise<void>;
  list(relativeDir: string): Promise<string[]>;
}

export class FileService {
  constructor(private readonly adapter: FileAdapter) {}

  /** Creates the full directory chain under the service root. */
  async ensureDirectory(relativeDir: string): Promise<void> {
    assertSafeRelativeDir(relativeDir);
    try {
      await this.adapter.ensureDir(relativeDir);
    } catch (cause) {
      throw normalizeAppError(cause, 'filesystem', 'mkdir-failed');
    }
  }

  /** Generated, collision-safe name: `{stem}-{nominal}.{ext}` pattern. */
  safeFileName(stem: string, ext: string, nominal: string): string {
    const cleanStem = stem.replace(/[^A-Za-z0-9_-]/g, '-').replace(/-+/g, '-').replace(/^-|-$/g, '') || 'file';
    const cleanExt = ext.replace(/[^A-Za-z0-9]/g, '');
    const cleanNominal = nominal.replace(/[^A-Za-z0-9_-]/g, '-') || 'x';
    return `${cleanStem}-${cleanNominal}.${cleanExt}`;
  }

  async write(path: string, data: Uint8Array): Promise<void> {
    assertSafeRelativePath(path);
    try {
      await this.adapter.write(path, data);
    } catch (cause) {
      throw normalizeAppError(cause, 'filesystem', 'write-failed');
    }
  }

  async read(path: string): Promise<Uint8Array> {
    assertSafeRelativePath(path);
    try {
      return await this.adapter.read(path);
    } catch (cause) {
      throw normalizeAppError(cause, 'filesystem', 'read-failed');
    }
  }

  async exists(path: string): Promise<boolean> {
    if (!this.isSafe(path)) return false;
    try {
      return await this.adapter.exists(path);
    } catch {
      return false;
    }
  }

  /** Deletes only when the target exists — idempotent by design. */
  async deleteIfExists(path: string): Promise<boolean> {
    if (!this.isSafe(path)) return false;
    try {
      if (!(await this.adapter.exists(path))) return false;
      await this.adapter.delete(path);
      return true;
    } catch (cause) {
      throw normalizeAppError(cause, 'filesystem', 'delete-failed');
    }
  }

  async move(from: string, to: string): Promise<void> {
    assertSafeRelativePath(from);
    assertSafeRelativePath(to);
    if (from === to) return;
    if (await this.adapter.exists(to)) {
      throw new AppError('filesystem', 'move-target-exists', {
        recoverable: false,
        userMessage: 'A file with that name already exists.',
      });
    }
    try {
      await this.adapter.move(from, to);
    } catch (cause) {
      throw normalizeAppError(cause, 'filesystem', 'move-failed');
    }
  }

  async copy(from: string, to: string): Promise<void> {
    assertSafeRelativePath(from);
    assertSafeRelativePath(to);
    if (await this.adapter.exists(to)) {
      throw new AppError('filesystem', 'copy-target-exists', {
        recoverable: false,
        userMessage: 'A file with that name already exists.',
      });
    }
    try {
      await this.adapter.copy(from, to);
    } catch (cause) {
      throw normalizeAppError(cause, 'filesystem', 'copy-failed');
    }
  }

  async list(relativeDir = ''): Promise<string[]> {
    assertSafeRelativeDir(relativeDir);
    try {
      return await this.adapter.list(relativeDir);
    } catch (cause) {
      throw normalizeAppError(cause, 'filesystem', 'list-failed');
    }
  }

  /**
   * Deletes files in `relativeDir` NOT in `keep` — the generic counterpart
   * of the media orphan sweep (export recovery, temp dirs). Dry-runable.
   */
  async sweep(relativeDir: string, keep: Set<string>, dryRun = false): Promise<string[]> {
    const names = await this.list(relativeDir);
    const orphans = names.filter((n) => !keep.has(n));
    if (!dryRun) {
      for (const name of orphans) {
        await this.deleteIfExists(`${relativeDir}/${name}`);
      }
    }
    return orphans;
  }

  private isSafe(path: string): boolean {
    try {
      assertSafeRelativePath(path);
      return true;
    } catch {
      return false;
    }
  }
}
