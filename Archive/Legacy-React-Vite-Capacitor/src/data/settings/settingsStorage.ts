/**
 * Settings storage abstraction (Phase 2).
 *
 * APPLICATION SETTINGS (text size, onboarding flag, app-lock flag, UI
 * preferences) are intentionally NOT stored in the SQLite domain database:
 * settings must be readable before database initialization (they gate the
 * boot UI), are tiny and flat, and have their own schema evolution. All
 * settings code goes through this interface so feature/domain code never
 * touches `localStorage` directly; a future move of individual settings
 * into the database happens by implementing this interface again — not by
 * sprinkling engine calls through the app.
 *
 * The in-memory fallback keeps the app functional when WebView storage is
 * unavailable (MasterPrompt §53 graceful degradation).
 */

export interface SettingsStorage {
  get(key: string): string | null;
  set(key: string, value: string): void;
  remove(key: string): void;
}

export class LocalStorageSettingsStorage implements SettingsStorage {
  private readonly memoryFallback = new Map<string, string>();

  constructor(private readonly storageKeyPrefix: string = '') {}

  get(key: string): string | null {
    const target = this.storageKeyPrefix + key;
    try {
      if (typeof localStorage !== 'undefined') {
        const value = localStorage.getItem(target);
        if (value !== null) return value;
      }
    } catch {
      // Fall through to memory fallback.
    }
    return this.memoryFallback.get(target) ?? null;
  }

  set(key: string, value: string): void {
    const target = this.storageKeyPrefix + key;
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem(target, value);
        return;
      }
    } catch {
      // Fall through to memory fallback.
    }
    this.memoryFallback.set(target, value);
  }

  remove(key: string): void {
    const target = this.storageKeyPrefix + key;
    this.memoryFallback.delete(target);
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.removeItem(target);
      }
    } catch {
      // Ignore — the in-memory copy is authoritative in that case anyway.
    }
  }
}

/** Shared production instance. Imported instead of touching localStorage. */
export const defaultSettingsStorage: SettingsStorage = new LocalStorageSettingsStorage();
