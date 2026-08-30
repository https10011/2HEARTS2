/**
 * Database adapter boundary (Phase 2).
 *
 * The entire application depends on this narrow interface, never on a
 * specific engine. Today there are two implementations:
 *   - `CapacitorSqliteAdapter` — native SQLite on Android (production).
 *   - `SqlJsAdapter`           — the same SQL schema on the sql.js WASM
 *                                build of SQLite, used for browser dev and
 *                                Node unit tests.
 *
 * SQL primitives allowed: TEXT (ids, timestamps, enums), INTEGER (numbers,
 * booleans), REAL, NULL. Binary data never enters domain tables — media is
 * referenced by safe relative path records (see src/data/media).
 */

export type SqlValue = string | number | boolean | null;

export interface RunResult {
  /** Rows affected by the last DML statement. */
  changes: number;
}

export interface DatabaseAdapter {
  /** Opens the underlying connection (called by the connection manager). */
  open(): Promise<void>;
  /** Executes DDL / multi-statement batches (migrations). */
  exec(sql: string): Promise<void>;
  /** Executes a parameterized DML statement and reports affected rows. */
  run(sql: string, params?: SqlValue[]): Promise<RunResult>;
  /** Executes a parameterized query. */
  query<T = unknown>(sql: string, params?: SqlValue[]): Promise<T[]>;
  /**
   * Runs `fn` inside a single real transaction (BEGIN IMMEDIATE / COMMIT /
   * ROLLBACK). Nested transactions are rejected — compose them inside one
   * outer transaction instead.
   */
  transaction<T>(fn: (tx: TransactionContext) => Promise<T>): Promise<T>;
  /** Closes the connection. Used by tests and re-initialization. */
  close(): Promise<void>;
}

/** A level-bound selection of `DatabaseAdapter` usable inside transactions. */
export type TransactionContext = Pick<DatabaseAdapter, 'exec' | 'run' | 'query'>;
