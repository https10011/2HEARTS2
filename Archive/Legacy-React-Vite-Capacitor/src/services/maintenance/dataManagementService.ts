/**
 * Data & storage management service (Phase 19).
 *
 * The application-facing boundary behind the Storage settings screen:
 *
 * - `getStorageReport()` — honest, locally-derived storage picture:
 *   domain rows per table, stored media bytes, pending local
 *   notifications. Everything stays on-device; nothing is uploaded.
 * - `clearCache()` — removes TEMPORARY data only: unreferenced media
 *   files (the Phase 2 orphan sweep). Saved domain data is untouched.
 * - `resetAllLocalData()` — the destructive full reset (Storage screen
 *   "Clear Local Data", gated by UI confirmation): cancels every pending
 *   local notification, deletes ALL domain rows (schema + migration
 *   ledger survive — the app must not re-run migrations), sweeps the now
 *   orphaned media files (vault media included), removes the App Lock PIN
 *   material from the SecureStore, and resets settings so the next entry
 *   lands on first-launch onboarding. `firstLaunchAt` survives on purpose
 *   (Phase 4: historical fact, not a preference).
 *
 * Layer discipline: reads/aggregates through the DatabaseAdapter only,
 * media deletion goes through MediaStorage, notifications through the
 * NotificationService, PIN removal through AppLockService. No screen
 * touches SQL or SecureStore keys directly.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import type { MediaStorage } from '../../data/media/mediaStorage.ts';
import { appSettingsStore } from '../../core/appSettings.ts';
import { AppError, normalizeAppError } from '../errors/appError.ts';
import { createLogger } from '../logging/logger.ts';
import type { NotificationService } from '../notifications/notificationService.ts';
import type { AppLockService } from '../security/appLockService.ts';

const log = createLogger('data-management');

/** Tables that are persistence infrastructure, not user data. */
const SYSTEM_TABLES = new Set(['schema_migrations', 'sqlite_sequence']);

export interface TableUsage {
  table: string;
  rows: number;
}

export interface StorageReport {
  /** Total bytes of locally stored media (photos/videos metadata sum). */
  mediaBytes: number;
  /** Row counts per domain table (excluding schema ledger). */
  tables: TableUsage[];
  /** Total domain rows across all tables. */
  domainRows: number;
  /** Pending local notifications currently registered. */
  pendingNotifications: number;
}

export interface ClearCacheResult {
  mediaFilesRemoved: number;
}

export interface ResetResult {
  tablesCleared: number;
  mediaFilesRemoved: number;
}

export class DataManagementService {
  constructor(
    private readonly db: DatabaseAdapter,
    private readonly media: MediaStorage | null = null,
    private readonly notifications: NotificationService | null = null,
    private readonly appLock: AppLockService | null = null,
  ) {}

  /** Aggregates an honest local-storage picture. Never throws raw SQL. */
  async getStorageReport(): Promise<StorageReport> {
    try {
      const tables = await this.listDomainTables();
      const usage: TableUsage[] = [];
      let domainRows = 0;
      for (const table of tables) {
        const rows = await this.db.query<{ n: number }>(
          `SELECT COUNT(*) AS n FROM "${table}"`,
        );
        const n = rows[0]?.n ?? 0;
        usage.push({ table, rows: n });
        domainRows += n;
      }
      const mediaRows = await this.db.query<{ total: number | null }>(
        'SELECT COALESCE(SUM(size_bytes), 0) AS total FROM media_assets',
      );
      const pendingNotifications = this.notifications
        ? (await this.notifications.list()).length
        : 0;
      return {
        mediaBytes: mediaRows[0]?.total ?? 0,
        tables: usage,
        domainRows,
        pendingNotifications,
      };
    } catch (cause) {
      throw normalizeAppError(cause, 'persistence', 'storage-report-failed', { recoverable: true });
    }
  }

  /**
   * Removes temporary data only: media files no longer referenced by any
   * asset row. Saved data (rows + referenced media) is untouched.
   */
  async clearCache(): Promise<ClearCacheResult> {
    if (!this.media) return { mediaFilesRemoved: 0 };
    try {
      const mediaFilesRemoved = await this.media.sweepOrphans();
      log.info('Cleared temporary media cache.', { mediaFilesRemoved });
      return { mediaFilesRemoved };
    } catch (cause) {
      throw normalizeAppError(cause, 'persistence', 'cache-clear-failed', { recoverable: true });
    }
  }

  /**
   * Destructive full local reset. Caller (Storage screen) owns the
   * confirmation flow; this method performs the wipe atomically where it
   * matters (domain rows in ONE transaction; orphan sweep follows commit
   * because MediaStorage opens its own transaction).
   */
  async resetAllLocalData(): Promise<ResetResult> {
    try {
      // 1. Nothing scheduled may fire after the data it references is gone.
      if (this.notifications) {
        await this.notifications.cancelAll();
      }

      // 2. Domain rows in one transaction; schema + ledger stay intact.
      const tables = await this.listDomainTables();
      await this.db.transaction(async (tx) => {
        for (const table of tables) {
          await tx.run(`DELETE FROM "${table}"`);
        }
      });

      // 3. Every media file is now unreferenced → the existing orphan
      //    sweep removes them from private app storage.
      let mediaFilesRemoved = 0;
      if (this.media) {
        mediaFilesRemoved = await this.media.sweepOrphans();
      }

      // 4. Security material: PIN salt/verifier leave the SecureStore; the
      //    lock state returns to 'disabled'.
      if (this.appLock) {
        await this.appLock.disable();
      }

      // 5. Preferences return to defaults; firstLaunchAt survives (Phase 4).
      appSettingsStore.reset();

      log.info('Local data reset completed.', {
        tablesCleared: tables.length,
        mediaFilesRemoved,
      });
      return { tablesCleared: tables.length, mediaFilesRemoved };
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'persistence', 'data-reset-failed', { recoverable: true });
    }
  }

  /** User tables = everything except the migration ledger / sqlite internals. */
  private async listDomainTables(): Promise<string[]> {
    const rows = await this.db.query<{ name: string }>(
      "SELECT name FROM sqlite_master WHERE type = 'table' ORDER BY name",
    );
    return rows.map((r) => r.name).filter((name) => !SYSTEM_TABLES.has(name));
  }
}
