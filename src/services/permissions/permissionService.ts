/**
 * Centralized permission handling (Phase 3).
 *
 * Rules demanded by the specs:
 * - permissions are requested ONLY when the owning feature needs them;
 * - every capability plugs in via `PermissionProvider` (notifications now;
 *   camera/photos providers register in their feature phases);
 * - UI sees a normalized `PermissionState`, never plugin-specific values.
 */

import { LocalNotifications } from '@capacitor/local-notifications';

export type PermissionState = 'granted' | 'denied' | 'prompt' | 'unavailable';
export type PermissionCapability = 'notifications' | 'camera' | 'photos' | 'mediaStorage';

export interface PermissionProvider {
  check(): Promise<PermissionState>;
  request(): Promise<PermissionState>;
}

function mapPlugin(state: string | undefined): PermissionState {
  switch (state) {
    case 'granted':
      return 'granted';
    case 'denied':
      return 'denied';
    case 'prompt':
    case 'prompt-with-rationale':
      return 'prompt';
    default:
      return 'unavailable';
  }
}

/** Android 13+ POST_NOTIFICATIONS via @capacitor/local-notifications. */
export const notificationPermissionProvider: PermissionProvider = {
  async check(): Promise<PermissionState> {
    const status = await LocalNotifications.checkPermissions();
    return mapPlugin(status.display);
  },
  async request(): Promise<PermissionState> {
    const status = await LocalNotifications.requestPermissions();
    return mapPlugin(status.display);
  },
};

const DEFAULT_PROVIDERS: Partial<Record<PermissionCapability, PermissionProvider>> = {
  notifications: notificationPermissionProvider,
  // 'camera'/'photos'/'mediaStorage' providers are intentionally absent until
  // a feature that needs them registers one (spec: no premature flows).
  // Android photo picker & app-private files need NO runtime permission,
  // so no provider is synthesized here for them by design.
};

export class PermissionService {
  private providers: Partial<Record<PermissionCapability, PermissionProvider>>;

  constructor(providers: Partial<Record<PermissionCapability, PermissionProvider>> = DEFAULT_PROVIDERS) {
    this.providers = { ...providers };
  }

  /** Late registration for capability providers (feature phases). */
  register(capability: PermissionCapability, provider: PermissionProvider): void {
    this.providers[capability] = provider;
  }

  async check(capability: PermissionCapability): Promise<PermissionState> {
    const provider = this.providers[capability];
    if (!provider) return 'unavailable';
    try {
      return await provider.check();
    } catch {
      return 'unavailable';
    }
  }

  /**
   * Requests permission for an owned feature moment only. Returns the
   * resulting state; a denial is data, not an exception — callers decide
   * how to degrade their own feature.
   */
  async request(capability: PermissionCapability): Promise<PermissionState> {
    const provider = this.providers[capability];
    if (!provider) return 'unavailable';
    try {
      return await provider.request();
    } catch {
      return 'unavailable';
    }
  }

  /** Convenience for "check, then request when promptable". */
  async ensure(capability: PermissionCapability): Promise<PermissionState> {
    const status = await this.check(capability);
    if (status === 'granted') return status;
    if (status === 'prompt') return this.request(capability);
    return status;
  }
}
