/**
 * Persistence configuration (Phase 2).
 *
 * One authoritative place for local-first persistence constants. Feature
 * code must read these values through the persistence layer, never by
 * hard-coding strings.
 */

export const PERSISTENCE_CONFIG = {
  /** Logical SQLite database name (device-local, private app storage). */
  databaseName: 'twohearts',
  /**
   * Current application schema version. Bumped exactly once per migration;
   * the migration with this id must exist in `ALL_MIGRATIONS`.
   */
  schemaVersion: 13,
  /**
   * Root directory (relative to the private app data directory) where
   * locally stored photos/videos live. Files inside use generated names;
   * raw paths never leak to UI code (see src/data/media).
   */
  mediaRoot: 'media',
} as const;
