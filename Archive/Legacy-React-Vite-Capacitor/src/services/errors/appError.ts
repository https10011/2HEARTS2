/**
 * Application error taxonomy (Phase 3).
 *
 * One error shape for ALL service layers. Rules:
 * - `category` identifies the boundary that failed (never raw subsystem text),
 * - `code` is a stable machine-readable string,
 * - `recoverable` tells the caller whether retry/recovery is appropriate,
 * - `userMessage` is the ONLY string UI/log surfaces may show to users —
 *   it must never contain SQL, paths, credentials, or domain values,
 * - `cause` carries internal diagnostics for logging only (logger redacts).
 *
 * PersistenceError (Phase 2) keeps its own shape for the data layer; services
 * translate it into AppError at the boundary (see `fromPersistence`).
 */

import { PersistenceError } from '../../data/database/errors.ts';

export type AppErrorCategory =
  | 'service'
  | 'persistence'
  | 'filesystem'
  | 'notification'
  | 'permission'
  | 'security'
  | 'validation'
  | 'media'
  | 'unknown';

export interface AppErrorOptions {
  recoverable?: boolean;
  userMessage?: string;
  cause?: unknown;
}

export class AppError extends Error {
  readonly category: AppErrorCategory;
  readonly code: string;
  readonly recoverable: boolean;
  readonly userMessage: string;
  readonly cause?: unknown;

  constructor(category: AppErrorCategory, code: string, options: AppErrorOptions = {}) {
    super(`${category}:${code}`);
    this.name = 'AppError';
    this.category = category;
    this.code = code;
    this.recoverable = options.recoverable ?? true;
    this.userMessage = options.userMessage ?? DEFAULT_USER_MESSAGES[category];
    this.cause = options.cause;
  }
}

const DEFAULT_USER_MESSAGES: Record<AppErrorCategory, string> = {
  service: 'A service failed.',
  persistence: 'Local data could not be accessed.',
  filesystem: 'A file operation failed.',
  notification: 'Notification scheduling failed.',
  permission: 'A required permission is missing.',
  security: 'A security operation failed.',
  validation: 'Input is invalid.',
  media: 'Media processing failed.',
  unknown: 'An unexpected error occurred.',
};

/** Stable user-facing message — never raw internals. */
export function safeUserMessage(error: unknown): string {
  if (error instanceof AppError) return error.userMessage;
  if (error instanceof PersistenceError) return DEFAULT_USER_MESSAGES.persistence;
  return DEFAULT_USER_MESSAGES.unknown;
}

/** Wrap any failure as AppError; pass AppError through untouched. */
export function normalizeAppError(
  cause: unknown,
  category: AppErrorCategory,
  code: string,
  options: AppErrorOptions = {},
): AppError {
  if (cause instanceof AppError) return cause;
  if (cause instanceof PersistenceError) return fromPersistence(cause, code);
  return new AppError(category, code, { ...options, cause });
}

/** Translates a Phase 2 PersistenceError without leaking internals. */
export function fromPersistence(error: PersistenceError, code = 'persistence-failed'): AppError {
  const recoverable = error.code !== 'init-failed' && error.code !== 'migration-failed';
  return new AppError('persistence', code, { recoverable, cause: error });
}
