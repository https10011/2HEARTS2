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
// Relative import (not the @theme alias): this module is imported by the
// Phase 3 bootstrap pipeline, which must also load under Node's test runner
// where Vite path aliases do not resolve.
import { TEXT_SIZE_SCALE, type TextSizeKey } from '../theme/tokens.ts';
import { defaultSettingsStorage } from '../data/settings/settingsStorage.ts';

const STORAGE_KEY = 'twohearts.settings.v1';
const SCHEMA_VERSION = 2;

/**
 * Onboarding progression (Phase 4). Persisted so a killed app resumes setup
 * where it left off. 'complete' is the same truth as `onboarded: true`;
 * both exist because `onboarded` predates the staged model.
 */
export const ONBOARDING_STAGES = ['fresh', 'owner', 'relationship', 'personalization', 'complete'] as const;
export type OnboardingStage = (typeof ONBOARDING_STAGES)[number];

/**
 * Theme mode preference (Phase 4). 'light'/'dark' force a scheme; 'system'
 * follows the OS. Only 'light' styles exist today — the preference is
 * persisted now so the settings UI ships with working persistence, and
 * applying dark mode becomes a CSS token flip later.
 */
export const THEME_MODES = ['light', 'dark', 'system'] as const;
export type ThemeMode = (typeof THEME_MODES)[number];

export interface AppSettings {
  schemaVersion: number;
  textSize: TextSizeKey;
  /** Whether first-launch onboarding has been completed. */
  onboarded: boolean;
  /** Whether app lock is enabled (Vault/AppLock feature, Phase 17). */
  appLockEnabled: boolean;
  /**
   * Seconds of background inactivity before the app re-locks (0 = lock on
   * any background). Non-sensitive CONFIG ONLY — the PIN verifier lives in
   * the SecureStore (src/services/security), never here.
   */
  lockTimeoutSeconds: number;
  /** UTC ISO 8601 of the very first launch, recorded once, never reset. */
  firstLaunchAt: string | null;
  onboardingStage: OnboardingStage;
  themeMode: ThemeMode;
}

const DEFAULT_SETTINGS: AppSettings = {
  schemaVersion: SCHEMA_VERSION,
  textSize: 'default',
  onboarded: false,
  appLockEnabled: false,
  lockTimeoutSeconds: 60,
  firstLaunchAt: null,
  onboardingStage: 'fresh',
  themeMode: 'light',
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
  // v1 → v2 (Phase 4): adds firstLaunchAt / onboardingStage / themeMode.
  // DEFAULT_SETTINGS already supplies the new keys, so the merge in load()
  // covers existing installs; this step only stamps the version and keeps
  // the onboarded flag and staged state coherent for v1 data.
  let out = data;
  if (out.schemaVersion < 1) {
    out = { ...out, schemaVersion: 1 };
  }
  if (out.schemaVersion < 2) {
    out = {
      ...out,
      onboardingStage: out.onboarded ? 'complete' : out.onboardingStage,
      schemaVersion: 2,
    };
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
  setThemeMode(mode: ThemeMode) {
    persist({ ...current, themeMode: mode });
  },
  setOnboardingStage(stage: OnboardingStage) {
    persist({ ...current, onboardingStage: stage, onboarded: stage === 'complete' });
  },
  /**
   * Records the very first launch timestamp exactly once. Called by the
   * bootstrap pipeline; later calls are no-ops. `reset()` does NOT clear it
   * — first launch is historical fact, not a preference.
   */
  markFirstLaunch(timestampIso: string) {
    if (current.firstLaunchAt === null) {
      persist({ ...current, firstLaunchAt: timestampIso });
    }
  },
  completeOnboarding() {
    persist({ ...current, onboarded: true, onboardingStage: 'complete' });
  },
  reset() {
    // firstLaunchAt survives reset: it is history, not a preference.
    persist({ ...DEFAULT_SETTINGS, firstLaunchAt: current.firstLaunchAt });
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

/**
 * Applies the theme-mode preference to the document root
 * (`data-th-theme`). Only light tokens exist today; dark tokens become a
 * CSS-level switch later without touching this persistence contract.
 */
export function applyThemeMode(mode: ThemeMode) {
  if (typeof document === 'undefined') return;
  const resolved =
    mode === 'system'
      ? window.matchMedia?.('(prefers-color-scheme: dark)').matches
        ? 'dark'
        : 'light'
      : mode;
  document.documentElement.dataset.thTheme = resolved;
}
