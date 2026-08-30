/**
 * Shared validation (Phase 3).
 *
 * Feature-agnostic rules producing predictable results:
 * `ValidationResult { ok, errors[] }`. Validators compose via `validate()`.
 * Reuses Phase 2 primitives (ids, media path safety) instead of duplicating.
 */

import { isValidId } from '../../utils/ids.ts';
import { isValidIsoTimestamp } from '../../utils/time.ts';
import { assertSafeRelativePath } from '../../data/media/fileSystem.ts';

export interface ValidationResult {
  ok: boolean;
  errors: string[];
}

const okResult: ValidationResult = { ok: true, errors: [] };

export function required(value: unknown, label = 'Value'): ValidationResult {
  if (value === null || value === undefined || value === '') {
    return { ok: false, errors: [`${label} is required.`] };
  }
  return okResult;
}

export function textLength(value: string, min: number, max: number, label = 'Text'): ValidationResult {
  const len = value.trim().length;
  if (len < min) return { ok: false, errors: [`${label} must be at least ${min} character(s).`] };
  if (len > max) return { ok: false, errors: [`${label} must be at most ${max} characters.`] };
  return okResult;
}

export function safeFilename(name: string): ValidationResult {
  try {
    assertSafeRelativePath(name);
    return okResult;
  } catch {
    return { ok: false, errors: ['Filename contains unsafe characters.'] };
  }
}

/** Calendar-date validity for ISO YYYY-MM-DD strings (no time portion). */
export function validIsoDate(value: string): ValidationResult {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return { ok: false, errors: ['Date must use YYYY-MM-DD.'] };
  }
  const date = new Date(`${value}T00:00:00Z`);
  if (Number.isNaN(date.getTime()) || date.toISOString().slice(0, 10) !== value) {
    return { ok: false, errors: ['Date is not a valid calendar date.'] };
  }
  return okResult;
}

export function validTimestamp(value: string): ValidationResult {
  return isValidIsoTimestamp(value) ? okResult : { ok: false, errors: ['Timestamp must be ISO 8601 UTC.'] };
}

export function numericRange(value: number, min: number, max: number, label = 'Value'): ValidationResult {
  if (!Number.isFinite(value)) return { ok: false, errors: [`${label} must be a number.`] };
  if (value < min || value > max) {
    return { ok: false, errors: [`${label} must be between ${min} and ${max}.`] };
  }
  return okResult;
}

export function mimeAllowed(mime: string, allowed: readonly string[]): ValidationResult {
  return allowed.includes(mime)
    ? okResult
    : { ok: false, errors: [`File type "${mime}" is not supported.`] };
}

export function fileSizeWithin(sizeBytes: number, maxBytes: number): ValidationResult {
  return sizeBytes <= maxBytes
    ? okResult
    : { ok: false, errors: [`File exceeds the ${Math.floor(maxBytes / (1024 * 1024))}MB limit.`] };
}

export function validId(value: string): ValidationResult {
  return isValidId(value) ? okResult : { ok: false, errors: ['Identifier is invalid.'] };
}

/** Input normalization: trim + collapse whitespace (shared by all features). */
export function normalizeInput(value: string): string {
  return value.replace(/\s+/g, ' ').trim();
}

/** Runs rules in order and merges errors (all reported, no short-circuit). */
export function validate(...results: ValidationResult[]): ValidationResult {
  const errors = results.flatMap((r) => r.errors);
  return { ok: errors.length === 0, errors };
}
