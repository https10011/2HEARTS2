/**
 * Typed persistence error boundary (Phase 2).
 *
 * All failures raised by the persistence layer are normalized to
 * `PersistenceError` with a stable `code`. Repository/service code catches
 * raw engine exceptions at the adapter boundary and rethrows normalized
 * errors; UI/logger code must use `safeMessageFor` so raw SQL, schema
 * fragments, filesystem paths, or any domain data never leak into user
 * output or logs (MasterPrompt §53; privacy requires sanitized diagnostics).
 */

export type PersistenceErrorCode =
  | 'not-initialized'
  | 'init-failed'
  | 'query-failed'
  | 'transaction-failed'
  | 'migration-failed'
  | 'serialization-failed'
  | 'media-fs-failed'
  | 'media-missing'
  | 'corrupt-data'
  | 'unknown';

const USER_MESSAGES: Record<PersistenceErrorCode, string> = {
  'not-initialized': 'Local data is not ready yet.',
  'init-failed': 'Local data setup failed.',
  'query-failed': 'Reading local data failed.',
  'transaction-failed': 'Saving local data failed.',
  'migration-failed': 'Local data upgrade failed.',
  'serialization-failed': 'Local data format was invalid.',
  'media-fs-failed': 'Storing a photo or video failed.',
  'media-missing': 'A photo or video file is missing.',
  'corrupt-data': 'Local data is corrupted.',
  unknown: 'An unexpected data error occurred.',
};

export class PersistenceError extends Error {
  readonly code: PersistenceErrorCode;

  constructor(code: PersistenceErrorCode, message?: string, options?: { cause?: unknown }) {
    super(message ?? USER_MESSAGES[code], options);
    this.name = 'PersistenceError';
    this.code = code;
  }

  /** User/log-safe message. Never contains SQL, paths, or domain data. */
  get safeMessage(): string {
    return USER_MESSAGES[this.code];
  }
}

/** Normalizes an unknown thrown value into a PersistenceError. */
export function normalizeError(cause: unknown, code: PersistenceErrorCode = 'unknown'): PersistenceError {
  if (cause instanceof PersistenceError) return cause;
  const detail = cause instanceof Error ? cause.message : String(cause);
  return new PersistenceError(code, detail, { cause });
}

/** Safe readable message suitable for UI and logs. */
export function safeMessageFor(error: unknown): string {
  if (error instanceof PersistenceError) return error.safeMessage;
  return USER_MESSAGES.unknown;
}
