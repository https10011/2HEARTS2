/**
 * Capacitor SQLite adapter (production, Android).
 *
 * Uses `@capacitor-community/sqlite` — native SQLite inside the app's
 * private sandbox (offline-first, no cloud). The plugin's own versioned
 * upgrade mechanism is intentionally bypassed: the database is always opened
 * at plugin version 1 and schema evolution is owned exclusively by the
 * application migration runner (src/data/database/migrations), which keeps
 * migrations explicit, deterministic, ordered, and testable.
 */

import { CapacitorSQLite, SQLiteConnection, type SQLiteDBConnection } from '@capacitor-community/sqlite';
import type { DatabaseAdapter, RunResult, SqlValue, TransactionContext } from './adapter.ts';
import { PersistenceError } from './errors.ts';

export interface CapacitorSqliteAdapterOptions {
  databaseName: string;
}

export class CapacitorSqliteAdapter implements DatabaseAdapter {
  private db: SQLiteDBConnection | null = null;
  private inTransaction = false;

  constructor(private readonly options: CapacitorSqliteAdapterOptions) {}

  async open(): Promise<void> {
    if (this.db) return;
    const { databaseName } = this.options;
    try {
      const connection = new SQLiteConnection(CapacitorSQLite);
      const existing = await connection.isConnection(databaseName, false);
      this.db = existing.result
        ? await connection.retrieveConnection(databaseName, false)
        : await connection.createConnection(databaseName, false, 'no-encryption', 1, false);
      await this.db.open();
    } catch (cause) {
      throw new PersistenceError('init-failed', 'Failed to open the local database.', { cause });
    }
  }

  async exec(sql: string): Promise<void> {
    const db = this.requireDb();
    try {
      await db.execute(sql, false);
    } catch (cause) {
      throw new PersistenceError('query-failed', 'Statement execution failed.', { cause });
    }
  }

  async run(sql: string, params: SqlValue[] = []): Promise<RunResult> {
    const db = this.requireDb();
    try {
      const result = await db.run(sql, params, false);
      return { changes: result.changes?.changes ?? 0 };
    } catch (cause) {
      throw new PersistenceError('query-failed', 'Statement execution failed.', { cause });
    }
  }

  async query<T = unknown>(sql: string, params: SqlValue[] = []): Promise<T[]> {
    const db = this.requireDb();
    try {
      const result = await db.query(sql, params);
      return (result.values ?? []) as T[];
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
      await db.execute('BEGIN IMMEDIATE', false);
      const tx: TransactionContext = { exec: (s) => this.exec(s), run: (s, p) => this.run(s, p), query: (s, p) => this.query(s, p) };
      let result: T;
      try {
        result = await fn(tx);
        await db.execute('COMMIT', false);
      } catch (cause) {
        await db.execute('ROLLBACK', false).catch(() => undefined);
        if (cause instanceof PersistenceError) throw cause;
        throw new PersistenceError('transaction-failed', 'Transaction rolled back.', { cause });
      }
      return result;
    } finally {
      this.inTransaction = false;
    }
  }

  async close(): Promise<void> {
    const connection = new SQLiteConnection(CapacitorSQLite);
    await this.db?.close().catch(() => undefined);
    await connection.closeConnection(this.options.databaseName, false).catch(() => undefined);
    this.db = null;
  }

  private requireDb(): SQLiteDBConnection {
    if (!this.db) {
      throw new PersistenceError('not-initialized', 'Adapter was not opened.');
    }
    return this.db;
  }
}
