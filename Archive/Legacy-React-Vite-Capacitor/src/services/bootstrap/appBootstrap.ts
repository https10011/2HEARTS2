/**
 * Application initialization pipeline (Phase 3).
 *
 * Ordered, staged, observable startup. Rules:
 * - critical stages abort the pipeline and surface a user-safe failure
 *   (the AppGate in main.tsx renders the retry path),
 * - non-critical stages log-and-continue (a notification hiccup must never
 *   brick app usage),
 * - each stage runs exactly once per attempt; a failed attempt can be
 *   retried (the persistence gate itself resets its memo on failure),
 * - migration startup is part of the pipeline: open → runMigrations →
 *   verify the schema ledger reached the configured version.
 *
 * Stage order (spec §15): persistence → schema verify → device capabilities
 * → lifecycle → notifications → app lock → ready. Settings load implicitly
 * with module import (localStorage); feature init is NOT performed here.
 */

import { AppError, safeUserMessage } from '../errors/appError.ts';
import { createLogger } from '../logging/logger.ts';
import { getDatabase, initializeDatabase } from '../../data/database/connection.ts';
import { PERSISTENCE_CONFIG } from '../../config/persistence.ts';
import { appSettingsStore } from '../../core/appSettings.ts';
import { appLifecycle } from '../lifecycle/appLifecycleService.ts';
import { DeviceCapabilities } from '../device/deviceCapabilities.ts';
import { NotificationService } from '../notifications/notificationService.ts';
import { capacitorNotificationDriver } from '../notifications/capacitorNotificationDriver.ts';
import { NotificationRegistryRepository } from '../notifications/notificationRegistryRepository.ts';
import { capacitorSecureStore } from '../security/capacitorSecureStore.ts';
import { MemorySecureStore } from '../security/secureStore.ts';
import { AppLockService, type LockSettings } from '../security/appLockService.ts';
import { RelationshipService } from '../relationship/relationshipService.ts';
import { AppStateService } from '../state/appStateService.ts';
import { DataManagementService } from '../maintenance/dataManagementService.ts';
import { MediaStorage } from '../../data/media/mediaStorage.ts';
import { CapacitorFileSystem } from '../../data/media/capacitorFileSystem.ts';
import { MemoryFileSystem } from '../../data/media/memoryFileSystem.ts';
import { Capacitor } from '@capacitor/core';

const log = createLogger('bootstrap');

export interface StageResult {
  name: string;
  ok: boolean;
  critical: boolean;
  error?: string;
}

export interface BootstrapResult {
  ok: boolean;
  stages: StageResult[];
  /** User-safe message when ok === false. */
  failureMessage?: string;
}

export interface CoreServices {
  device: DeviceCapabilities;
  /** undefined when the (non-critical) notification stage degraded. */
  notifications?: NotificationService;
  /** undefined when the (non-critical) app-lock stage degraded. */
  appLock?: AppLockService;
  /** undefined when the (non-critical) application-state stage degraded. */
  appState?: AppStateService;
  /** undefined when the (non-critical) application-state stage degraded. */
  relationship?: RelationshipService;
  /** MediaStorage instance — shared across features that need local media. */
  mediaStorage?: MediaStorage;
  /**
   * Phase 19 — data/storage management behind the Storage settings screen.
   * undefined when the (non-critical) application-state stage degraded.
   */
  dataManagement?: DataManagementService;
}

export const coreServices: CoreServices = {
  device: new DeviceCapabilities(),
};

interface Stage {
  name: string;
  critical: boolean;
  run: () => Promise<void>;
}

function lockSettings(): LockSettings {
  const s = appSettingsStore.getState();
  return { enabled: s.appLockEnabled, timeoutSeconds: s.lockTimeoutSeconds };
}

async function verifySchemaVersion(): Promise<void> {
  const adapter = await getDatabase();
  const rows = await adapter.query<{ max_id: number | null }>(
    'SELECT MAX(id) AS max_id FROM schema_migrations',
  );
  const reached = rows[0]?.max_id ?? 0;
  if (reached !== PERSISTENCE_CONFIG.schemaVersion) {
    throw new AppError('persistence', 'schema-version-mismatch', {
      recoverable: false,
      userMessage: 'The local data could not be upgraded.',
      cause: { reached, expected: PERSISTENCE_CONFIG.schemaVersion },
    });
  }
}

function buildStages(): Stage[] {
  return [
    {
      name: 'persistence',
      critical: true,
      run: async () => {
        await initializeDatabase(); // opens adapter + runs pending migrations (Phase 2)
      },
    },
    { name: 'schema-verify', critical: true, run: verifySchemaVersion },
    {
      name: 'device-capabilities',
      critical: false,
      run: async () => {
        await coreServices.device.initialize();
      },
    },
    {
      name: 'lifecycle',
      critical: false,
      run: async () => {
        await appLifecycle.start();
      },
    },
    {
      name: 'notifications',
      critical: false,
      run: async () => {
        const adapter = await getDatabase();
        const registry = new NotificationRegistryRepository(adapter);
        const service = new NotificationService(capacitorNotificationDriver, registry);
        await service.initialize();
        coreServices.notifications = service;
      },
    },
    {
      name: 'app-lock',
      critical: false,
      run: async () => {
        // Native Keystore store on device; memory store on web/dev so the
        // lock FLOW is testable (web dev never claims real security).
        const store = Capacitor.isNativePlatform() ? capacitorSecureStore : new MemorySecureStore();
        const lock = new AppLockService(store, lockSettings);
        await lock.initialize();
        coreServices.appLock = lock;
      },
    },
    {
      name: 'application-state',
      critical: false,
      run: async () => {
        const adapter = await getDatabase();
        const appState = new AppStateService(adapter);
        appState.markFirstLaunchIfNeeded();
        await appState.reconcileOnboardingStage();
        coreServices.appState = appState;
        coreServices.relationship = new RelationshipService(adapter);
        // Phase 19: storage/data management for the Settings screens. Media
        // bytes live in private app files on device, memory fs in web/dev.
        const fs = Capacitor.isNativePlatform() ? new CapacitorFileSystem() : new MemoryFileSystem();
        const mediaStorage = new MediaStorage(adapter, fs);
        coreServices.mediaStorage = mediaStorage;
        coreServices.dataManagement = new DataManagementService(
          adapter,
          mediaStorage,
          coreServices.notifications ?? null,
          coreServices.appLock ?? null,
        );
      },
    },
  ];
}

/** Runs the full pipeline; the result is always user-safe (no internals). */
export async function bootstrapApp(): Promise<BootstrapResult> {
  const stages: StageResult[] = [];
  for (const stage of buildStages()) {
    try {
      await stage.run();
      stages.push({ name: stage.name, ok: true, critical: stage.critical });
      log.debug('Stage completed.', { stage: stage.name });
    } catch (cause) {
      const message = safeUserMessage(cause);
      stages.push({ name: stage.name, ok: false, critical: stage.critical, error: message });
      if (stage.critical) {
        log.error('Critical startup stage failed.', { stage: stage.name, cause });
        return { ok: false, stages, failureMessage: message };
      }
      log.warn('Non-critical startup stage failed; continuing.', { stage: stage.name, cause });
    }
  }
  log.info('Bootstrap completed.', {
    stages: stages.filter((s) => s.ok).map((s) => s.name),
    degraded: stages.filter((s) => !s.ok).map((s) => s.name),
  });
  return { ok: true, stages };
}
