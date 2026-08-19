/**
 * Safe media-reference boundary (Phase 2).
 *
 * All filesystem access for photos/videos goes through `MediaFileSystem`.
 * Callers work with generated, validated RELATIVE references — never raw
 * device paths — so there is exactly one place that enforces:
 *
 * - no absolute paths, no `..` traversal, no path separators tricks,
 * - root-relative, allow-listed character set, controlled file names,
 * - local-only storage (private app data dir; nothing leaves the device).
 *
 * Implementations: `CapacitorFileSystem` (Android prod) and
 * `MemoryFileSystem` (tests). Both enforce the same validation.
 */

export interface MediaFileSystem {
  /** Creates or overwrites the file at `relativePath`. */
  write(relativePath: string, data: Uint8Array): Promise<void>;
  /** Reads the whole file. Throws when missing. */
  read(relativePath: string): Promise<Uint8Array>;
  /** Deletes the file. Succeeds when already absent (idempotent). */
  delete(relativePath: string): Promise<void>;
  /** Whether the file exists at exactly `relativePath`. */
  exists(relativePath: string): Promise<boolean>;
  /** Lists file names (not full paths) directly under `relativeDir`. */
  list(relativeDir: string): Promise<string[]>;
}

const SAFE_SEGMENT = /^[A-Za-z0-9_-]+(\.[A-Za-z0-9]+)?$/;

/**
 * Validates a relative reference: dot-segment free, no traversal,
 * segment-wise safe characters. Throws on any violation.
 */
export function assertSafeRelativePath(relativePath: string): void {
  if (relativePath.length === 0 || relativePath.startsWith('/') || relativePath.startsWith('\\')) {
    throw new Error(`Unsafe media reference "${relativePath}".`);
  }
  const segments = relativePath.split('/');
  for (const segment of segments) {
    if (!SAFE_SEGMENT.test(segment) || segment === '.' || segment === '..') {
      throw new Error(`Unsafe media reference "${relativePath}".`);
    }
  }
}

/** Validates a directory portion for `list` calls (same rules, no file). */
export function assertSafeRelativeDir(relativeDir: string): void {
  if (relativeDir.length === 0) return;
  assertSafeRelativePath(relativeDir.replace(/\/$/, ''));
}
