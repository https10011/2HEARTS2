/**
 * Shared MediaFileSystem adapter resolution (Stage 9).
 *
 * Production (Android): CapacitorFileSystem — private app files.
 * Browser dev / Node tests: MemoryFileSystem — in-memory.
 *
 * This is the one place feature hooks resolve the filesystem adapter so
 * the Capacitor import stays behind a single dynamic boundary.
 */

import type { MediaFileSystem } from './fileSystem.ts';

export async function resolveMediaFileSystem(): Promise<MediaFileSystem> {
  const { Capacitor } = await import('@capacitor/core');
  if (Capacitor.isNativePlatform()) {
    const { CapacitorFileSystem } = await import('./capacitorFileSystem.ts');
    return new CapacitorFileSystem();
  }
  const { MemoryFileSystem } = await import('./memoryFileSystem.ts');
  return new MemoryFileSystem();
}
