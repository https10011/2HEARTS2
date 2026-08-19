/**
 * Backup/export format boundary (Phase 2).
 *
 * This phase establishes the versioned ENVELOPE and its validation only —
 * no packaging, no UI, no cloud (the roadmap's export points, e.g. period
 * data, remain local/user-controlled). When a user-facing flow arrives it
 * will serialize domain entities through their serializers and list media
 * as safe references in `mediaManifest`, never raw paths.
 *
 * Import validation rejects unsupported format/schema versions BEFORE any
 * migration work, so future imports cannot corrupt the local database.
 */

import { PERSISTENCE_CONFIG } from '../../config/persistence.ts';
import { isValidIsoTimestamp } from '../../utils/time.ts';
import { isValidId } from '../../utils/ids.ts';
import type { MediaAsset } from '../../data/media/mediaTypes.ts';

export const EXPORT_FORMAT = 'twohearts-export' as const;
export const EXPORT_FORMAT_VERSION = 1;

/**
 * Versioned export envelope. New entity kinds append as further fields;
 * `formatVersion` is bumped when the envelope shape changes.
 */
export interface TwoHeartsExport {
  format: typeof EXPORT_FORMAT;
  formatVersion: number;
  exportedAt: string;
  /** Schema version of the producing database. */
  schemaVersion: number;
  entities: {
    /** Metadata for media; binaries are packaged alongside by the exporter. */
    mediaAssets: MediaAsset[];
  };
}

export interface ExportValidationResult {
  ok: boolean;
  errors: string[];
}

/** Validates an untrusted parsed JSON value as an export envelope. */
export function validateExport(value: unknown): ExportValidationResult {
  const errors: string[] = [];
  const candidate = value as Partial<TwoHeartsExport>;

  if (!candidate || typeof candidate !== 'object') {
    return { ok: false, errors: ['Export payload is not an object.'] };
  }
  if (candidate.format !== EXPORT_FORMAT) errors.push('Unknown export format.');
  if (candidate.formatVersion !== EXPORT_FORMAT_VERSION) {
    errors.push(`Unsupported format version: ${String(candidate.formatVersion)}.`);
  }
  if (typeof candidate.schemaVersion !== 'number' || candidate.schemaVersion < 1) {
    errors.push('Missing schema version.');
  } else if (candidate.schemaVersion > PERSISTENCE_CONFIG.schemaVersion) {
    errors.push(`Schema version ${candidate.schemaVersion} is newer than this app supports.`);
  }
  if (typeof candidate.exportedAt !== 'string' || !isValidIsoTimestamp(candidate.exportedAt)) {
    errors.push('Invalid exportedAt timestamp.');
  }

  const entities = candidate.entities as { mediaAssets?: unknown } | undefined;
  if (!entities || !Array.isArray(entities.mediaAssets)) {
    errors.push('Missing entities.mediaAssets array.');
  } else {
    for (const [index, asset] of entities.mediaAssets.entries()) {
      const candidateAsset = asset as Partial<MediaAsset>;
      if (!candidateAsset || typeof candidateAsset !== 'object') {
        errors.push(`mediaAssets[${index}] is not an object.`);
        continue;
      }
      if (typeof candidateAsset.id !== 'string' || !isValidId(candidateAsset.id)) {
        errors.push(`mediaAssets[${index}] has an invalid id.`);
      }
      if (candidateAsset.kind !== 'photo' && candidateAsset.kind !== 'video') {
        errors.push(`mediaAssets[${index}] has an invalid kind.`);
      }
      if (typeof candidateAsset.relativePath !== 'string' || candidateAsset.relativePath.includes('..')) {
        errors.push(`mediaAssets[${index}] has an unsafe relative path.`);
      }
      for (const field of ['createdAt', 'updatedAt'] as const) {
        if (typeof candidateAsset[field] !== 'string' || !isValidIsoTimestamp(candidateAsset[field] as string)) {
          errors.push(`mediaAssets[${index}].${field} is invalid.`);
        }
      }
    }
  }

  return { ok: errors.length === 0, errors };
}
