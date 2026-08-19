/**
 * Persistent application settings store.
 *
 * APPLICATION SETTINGS only (text size, onboarding flag, app-lock flag) —
 * domain data (memories, notes, …) lives in the SQLite layer introduced in
 * Phase 2. Settings stay on `localStorage` ON PURPOSE: they must be readable
 * before database initialization and are tiny/flat; the hard boundary is the
 * `SettingsStorage` abstraction so nothing here touches `localStorage`
 * directly (see src/data/settings).
 *
 * Schema versioning: bump SCHEMA_VERSION and add a migration in
 * `migrate` to preserve user data across releases (MasterPrompt §13).
 */

import { useSyncExternalStore } from 'react';
import { TEXT_SIZE_SCALE, type TextSizeKey } from '@theme/tokens';
import { defaultSettingsStorage } from '../data/settings/settingsStorage.ts';

const STORAGE_KEY = 'twohearts.settings.v1';
const SCHEMA_VERSION = 1;

export interface AppSettings {
  schemaVersion: number;
  textSize: TextSizeKey;
  /** Whether first-launch onboarding has been completed. */
  onboarded: boolean;
  /** Whether app lock is enabled (Vault/AppLock feature, Phase 17). */
  appLockEnabled: boolean;
}

const DEFAULT_SETTINGS: AppSettings = {
  schemaVersion: SCHEMA_VERSION,
  textSize: 'default',
  onboarded: false,
  appLockEnabled: false,
};

type Listener = () => void;
const listeners = new Set<Listener>();
let current: AppSettings = load();

function load(): AppSettings {
  try {
    const raw = defaultSettingsStorage.get(STORAGE_KEY);
    if (!raw) return { ...DEFAULT_SETTINGS };
    const parsed = JSON.parse(raw) as Partial<AppSettings>;
    return migrate({ ...DEFAULT_SETTINGS, ...parsed });
  } catch {
    // Corrupt or missing storage: never crash (MasterPrompt §53 graceful failure).
    return { ...DEFAULT_SETTINGS };
  }
}

function migrate(data: AppSettings): AppSettings {
  // Future migrations go here, preserving existing user data.
  let out = data;
  if (out.schemaVersion < 1) {
    out = { ...out, schemaVersion: 1 };
  }
  return out;
}

function persist(next: AppSettings) {
  current = next;
  try {
    defaultSettingsStorage.set(STORAGE_KEY, JSON.stringify(next));
  } catch {
    // Storage unavailable / quota — state still lives in memory for the session.
  }
  listeners.forEach((l) => l());
}

export const appSettingsStore = {
  getState: () => current,
  subscribe(listener: Listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
  },
  set(partial: Partial<AppSettings>) {
    persist({ ...current, ...partial });
  },
  setTextSize(size: TextSizeKey) {
    persist({ ...current, textSize: size });
  },
  completeOnboarding() {
    persist({ ...current, onboarded: true });
  },
  reset() {
    persist({ ...DEFAULT_SETTINGS });
  },
};

/** React hook for reading application settings reactively. */
export function useAppSettings(): AppSettings {
  return useSyncExternalStore(appSettingsStore.subscribe, appSettingsStore.getState);
}

/**
 * Applies the current text-size scale to the document root, so all
 * typography tokens scale consistently (MasterPrompt §28).
 * Call once at startup and after changes.
 */
export function applyTextSize(size: TextSizeKey) {
  if (typeof document === 'undefined') return;
  document.documentElement.style.setProperty(
    '--th-text-scale',
    String(TEXT_SIZE_SCALE[size]),
  );
}
