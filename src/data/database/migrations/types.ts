/**
 * Migration framework types (Phase 2).
 *
 * Migrations are PURE FUNCTIONS over the adapter interface — deterministic,
 * ordered, and safe to detect as already applied. They never use engine-
 * specific APIs, so the same migration code runs on native SQLite (Android)
 * and on sql.js (browser dev / tests).
 */

import type { TransactionContext } from '../adapter.ts';

export interface Migration {
  /** Monotonically increasing integer id (1, 2, 3, …). Gap-less. */
  readonly id: number;
  /** Human-readable name used in the migration ledger. */
  readonly name: string;
  /** Applies the schema change. Runs inside a transaction. */
  up(tx: TransactionContext): Promise<void>;
}

export interface AppliedMigrationRow {
  id: number;
  name: string;
  applied_at: string;
}
