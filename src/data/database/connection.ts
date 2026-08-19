/**
 * Database connection lifecycle (Phase 2).
 *
 * A single memoized initializer owns the adapter instance. Rules:
 *
 * - first launch / every launch: `initializeDatabase(factory)` opens the
 *   adapter and runs pending migrations; simultaneous callers share one
 *   in-flight promise (no race conditions),
 * - callers that arrive before initialization completes receive a
 *   `not-initialized` PersistenceError instead of touching the database,
 * - on failure the promise is reset so a later attempt can retry cleanly,
 * - tests use `finalizeDatabaseForTests` to release the singleton.
 */

import { Capacitor } from '@capacitor/core';
import { PERSISTENCE_CONFIG } from '../../config/persistence.ts';
import type { DatabaseAdapter } from './adapter.ts';
import { CapacitorSqliteAdapter } from './capacitorSqliteAdapter.ts';
import { PersistenceError } from './errors.ts';
import { ALL_MIGRATIONS } from './migrations/index.ts';
import { runMigrations } from './migrations/runner.ts';
import { SqlJsAdapter } from './sqlJsAdapter.ts';

export type AdapterFactory = () => DatabaseAdapter;

let current: Promise<DatabaseAdapter> | null = null;

/** Adapter resolution: native Android uses SQLite; web/dev/tests use sql.js. */
export function resolveDefaultAdapter(): DatabaseAdapter {
  if (Capacitor.isNativePlatform()) {
    return new CapacitorSqliteAdapter({ databaseName: PERSISTENCE_CONFIG.databaseName });
  }
  // Browser dev / SSR-free contexts use the same SQL schema via sql.js.
  // The WASM file URL is resolved by the app bootstrap (see main.tsx).
  const wasmUrl = (globalThis as { __TWOHEARTS_SQL_WASM_URL__?: string }).__TWOHEARTS_SQL_WASM_URL__;
  return new SqlJsAdapter(wasmUrl ? { locateFile: () => wasmUrl } : {});
}

/**
 * Initializes the persistence layer exactly once. Concurrent callers get the
 * same promise. Failure clears the memo so a retry can proceed.
 */
export function initializeDatabase(factory: AdapterFactory = resolveDefaultAdapter): Promise<DatabaseAdapter> {
  if (current) return current;
  const attempt = (async () => {
    const adapter = factory();
    await adapter.open();
    await runMigrations(adapter, ALL_MIGRATIONS);
    return adapter;
  })().catch((cause: unknown) => {
    current = null;
    if (cause instanceof PersistenceError) throw cause;
    throw new PersistenceError('init-failed', 'Database initialization failed.', { cause });
  });
  current = attempt;
  return attempt;
}

/** Throws when the persistence layer has not been initialized. */
export function getDatabase(): Promise<DatabaseAdapter> {
  if (!current) {
    return Promise.reject(
      new PersistenceError('not-initialized', 'Call initializeDatabase() before using repositories.'),
    );
  }
  return current;
}

export function isDatabaseInitialized(): boolean {
  return current !== null;
}

/** Test-only: closes and forgets the singleton. */
export async function finalizeDatabaseForTests(): Promise<void> {
  if (!current) return;
  const adapter = await current.catch(() => null);
  await adapter?.close().catch(() => undefined);
  current = null;
}
