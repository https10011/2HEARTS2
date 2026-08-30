/**
 * sql.js database adapter.
 *
 * `sql.js` is SQLite compiled to WebAssembly. It runs the exact same SQL
 * schema as the native Android adapter, which makes it the right tool for:
 *   - browser development (`npm run dev`) where no native bridge exists,
 *   - Node-based unit tests of the data layer.
 *
 * On Android the production adapter is used; the adapter interface prevents
 * any "second database system" from leaking into application code.
 */

import initSqlJs, { type Database as SqlJsDatabase } from 'sql.js';
import type { DatabaseAdapter, RunResult, SqlValue, TransactionContext } from './adapter.ts';
import { PersistenceError } from './errors.ts';

export interface SqlJsAdapterOptions {
  /** Custom WASM locator (browser dev passes the bundled ?url asset). */
  locateFile?: (file: string) => string;
}

export class SqlJsAdapter implements DatabaseAdapter {
  private db: SqlJsDatabase | null = null;
  private inTransaction = false;

  constructor(private readonly options: SqlJsAdapterOptions = {}) {}

  async open(): Promise<void> {
    if (this.db) return;
    try {
      const SQL = await initSqlJs(
        this.options.locateFile ? { locateFile: this.options.locateFile } : undefined,
      );
      this.db = new SQL.Database();
    } catch (cause) {
      throw new PersistenceError('init-failed', 'sql.js failed to initialize.', { cause });
    }
  }

  async exec(sql: string): Promise<void> {
    const db = this.requireDb();
    try {
      db.exec(sql);
    } catch (cause) {
      throw new PersistenceError('query-failed', 'Statement execution failed.', { cause });
    }
  }

  async run(sql: string, params: SqlValue[] = []): Promise<RunResult> {
    const db = this.requireDb();
    try {
      db.run(sql, params as never);
      return { changes: db.getRowsModified() };
    } catch (cause) {
      throw new PersistenceError('query-failed', 'Statement execution failed.', { cause });
    }
  }

  async query<T = unknown>(sql: string, params: SqlValue[] = []): Promise<T[]> {
    const db = this.requireDb();
    try {
      const stmt = db.prepare(sql);
      stmt.bind(params as never);
      const rows: T[] = [];
      while (stmt.step()) {
        rows.push(stmt.getAsObject() as T);
      }
      stmt.free();
      return rows;
    } catch (cause) {
      throw new PersistenceError('query-failed', 'Query failed.', { cause });
    }
  }

  async transaction<T>(fn: (tx: TransactionContext) => Promise<T>): Promise<T> {
    const db = this.requireDb();
    if (this.inTransaction) {
      throw new PersistenceError('transaction-failed', 'Nested transactions are not supported.');
    }
    this.inTransaction = true;
    try {
      db.exec('BEGIN IMMEDIATE');
      const tx: TransactionContext = { exec: (s) => this.exec(s), run: (s, p) => this.run(s, p), query: (s, p) => this.query(s, p) };
      let result: T;
      try {
        result = await fn(tx);
        db.exec('COMMIT');
      } catch (cause) {
        db.exec('ROLLBACK');
        if (cause instanceof PersistenceError) throw cause;
        throw new PersistenceError('transaction-failed', 'Transaction rolled back.', { cause });
      }
      return result;
    } finally {
      this.inTransaction = false;
    }
  }

  async close(): Promise<void> {
    this.db?.close();
    this.db = null;
  }

  private requireDb(): SqlJsDatabase {
    if (!this.db) {
      throw new PersistenceError('not-initialized', 'Adapter was not opened.');
    }
    return this.db;
  }
}
