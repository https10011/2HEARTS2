/**
 * Shared media utilities (Phase 3).
 *
 * Builds on Phase 2: the DB metadata model (mediaTypes.ts) and the
 * MediaStorage service own storage; THIS module owns reusable media
 * KNOWLEDGE features will share — MIME policy, magic-byte sniffing,
 * size limits, safe display/URI concerns.
 *
 * No Memories/Vault feature code here; only infrastructure.
 */

import { MEDIA_EXTENSION_BY_MIME, MEDIA_MIME_BY_KIND, type MediaKind } from '../../data/media/mediaTypes.ts';
import { fileSizeWithin, mimeAllowed, type ValidationResult } from '../validation/validators.ts';

/** V1 size policy (documented; not arbitrary plugin limits). */
export const MEDIA_LIMITS = {
  /** 25 MB per photo — plenty for high-res camera output. */
  photoMaxBytes: 25 * 1024 * 1024,
  /** 500 MB per video clip. */
  videoMaxBytes: 500 * 1024 * 1024,
} as const;

export function allowedMimeTypes(kind: MediaKind): readonly string[] {
  return MEDIA_MIME_BY_KIND[kind];
}

export function extensionForMime(mimeType: string): string | null {
  return MEDIA_EXTENSION_BY_MIME[mimeType] ?? null;
}

export function kindForMime(mimeType: string): MediaKind | null {
  if (MEDIA_MIME_BY_KIND.photo.includes(mimeType)) return 'photo';
  if (MEDIA_MIME_BY_KIND.video.includes(mimeType)) return 'video';
  return null;
}

/**
 * Validates an incoming media candidate against kind/mime/size policy.
 * Features call this BEFORE touching MediaStorage.
 */
export function validateMediaCandidate(input: {
  kind: MediaKind;
  mimeType: string;
  sizeBytes: number;
}): ValidationResult {
  const results: ValidationResult[] = [
    mimeAllowed(input.mimeType, MEDIA_MIME_BY_KIND[input.kind]),
    fileSizeWithin(
      input.sizeBytes,
      input.kind === 'photo' ? MEDIA_LIMITS.photoMaxBytes : MEDIA_LIMITS.videoMaxBytes,
    ),
  ];
  const errors = results.flatMap((r) => r.errors);
  return { ok: errors.length === 0, errors };
}

/** Magic-byte signatures for the MIME types V1 accepts. */
const SIGNATURES: Array<{ mime: string; bytes: number[]; offset?: number }> = [
  { mime: 'image/jpeg', bytes: [0xff, 0xd8, 0xff] },
  { mime: 'image/png', bytes: [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a] },
  { mime: 'video/webm', bytes: [0x1a, 0x45, 0xdf, 0xa3] }, // EBML header (also Matroska)
  { mime: 'video/mp4', bytes: [0x66, 0x74, 0x79, 0x70], offset: 4 }, // "ftyp" box
];

/** image/webp: RIFF....WEBP composite signature. */
function isWebp(data: Uint8Array): boolean {
  if (data.length < 12) return false;
  const riff = [0x52, 0x49, 0x46, 0x46].every((b, i) => data[i] === b);
  const webp = [0x57, 0x45, 0x42, 0x50].every((b, i) => data[8 + i] === b);
  return riff && webp;
}

/**
 * Sniffs the real content type from the leading bytes. Returns the sniffed
 * MIME or null when unrecognized. Features compare this against the
 * declared MIME before accepting user-picked media (spoofing guard).
 */
export function sniffMimeType(data: Uint8Array): string | null {
  if (data.length < 4) return null;
  if (isWebp(data)) return 'image/webp';
  for (const { mime, bytes, offset = 0 } of SIGNATURES) {
    if (data.length < offset + bytes.length) continue;
    if (bytes.every((b, i) => data[offset + i] === b)) return mime;
  }
  return null;
}

/**
 * Full intake check: declared metadata policy + content sniffing.
 * `sizeBytes` must come from the caller (actual byte length).
 */
export function verifyMediaBytes(input: {
  kind: MediaKind;
  declaredMimeType: string;
  data: Uint8Array;
}): ValidationResult {
  const policy = validateMediaCandidate({
    kind: input.kind,
    mimeType: input.declaredMimeType,
    sizeBytes: input.data.length,
  });
  if (!policy.ok) return policy;
  const sniffed = sniffMimeType(input.data);
  if (sniffed === null) {
    return { ok: false, errors: ['File content is not a supported photo or video.'] };
  }
  if (sniffed !== input.declaredMimeType) {
    // video/webm shares the EBML header with Matroska; declared webm/mp4
    // mismatches are hard failures, but sniffing is conservative:
    return { ok: false, errors: ['File content does not match its declared type.'] };
  }
  return { ok: true, errors: [] };
}
