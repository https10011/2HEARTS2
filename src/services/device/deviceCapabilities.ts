/**
 * Device capability abstraction (Phase 3).
 *
 * Future feature code asks capability questions HERE instead of touching
 * `Capacitor`/navigator UA directly. The native probe is isolated behind
 * `DeviceInfoProvider` — tests inject a `FakeDeviceInfoProvider`.
 *
 * Answers: platform identity, OS version, and derived capability flags
 * (notifications / camera / filesystem / secure storage). Capability flags
 * describe RUNTIME SUPPORT only — permission is a separate concern
 * (services/permissions).
 */

import { Capacitor } from '@capacitor/core';
import { Device } from '@capacitor/device';

export type DeviceCapability =
  | 'notifications'
  | 'camera'
  | 'filesystem'
  | 'secureStorage'
  | 'localStorage';

export interface DeviceInfoSnapshot {
  platform: 'android' | 'ios' | 'web';
  osVersion: string | null;
  model: string | null;
  manufacturer: string | null;
}

export interface DeviceInfoProvider {
  isNative(): boolean;
  info(): Promise<DeviceInfoSnapshot>;
}

/** Native/web probe backed by @capacitor/device (single usage site). */
export const capacitorDeviceProvider: DeviceInfoProvider = {
  isNative: () => Capacitor.isNativePlatform(),
  async info(): Promise<DeviceInfoSnapshot> {
    const info = await Device.getInfo();
    return {
      platform: info.platform as DeviceInfoSnapshot['platform'],
      osVersion: info.osVersion ?? null,
      model: info.model ?? null,
      manufacturer: info.manufacturer ?? null,
    };
  },
};

/** Deterministic provider for tests. */
export function fakeDeviceProvider(snapshot: DeviceInfoSnapshot, native = false): DeviceInfoProvider {
  return {
    isNative: () => native,
    info: () => Promise.resolve({ ...snapshot }),
  };
}

export class DeviceCapabilities {
  private snapshot: DeviceInfoSnapshot | null = null;

  constructor(private readonly provider: DeviceInfoProvider = capacitorDeviceProvider) {}

  async initialize(): Promise<DeviceInfoSnapshot> {
    try {
      this.snapshot = await this.provider.info();
    } catch {
      // Capability probe failures never block startup — degrade to web.
      this.snapshot = { platform: 'web', osVersion: null, model: null, manufacturer: null };
    }
    return this.snapshot;
  }

  isReady(): boolean {
    return this.snapshot !== null;
  }

  isAndroid(): boolean {
    return this.snapshot?.platform === 'android';
  }

  isNative(): boolean {
    return this.provider.isNative();
  }

  platform(): DeviceInfoSnapshot['platform'] | 'uninitialized' {
    return this.snapshot?.platform ?? 'uninitialized';
  }

  osVersion(): string | null {
    return this.snapshot?.osVersion ?? null;
  }

  info(): DeviceInfoSnapshot | null {
    return this.snapshot ? { ...this.snapshot } : null;
  }

  /**
   * Runtime capability matrix. Notifications/filesystem/secure storage are
   * native-only in production; the web/dev fallback exists for development
   * drivers (memory/in-browser), never for real device use.
   */
  has(capability: DeviceCapability): boolean {
    const platform = this.snapshot?.platform ?? 'web';
    switch (capability) {
      case 'notifications':
      case 'filesystem':
      case 'secureStorage':
        return platform === 'android' || platform === 'ios';
      case 'camera':
        return platform === 'android' || platform === 'ios';
      case 'localStorage':
        return true;
    }
  }
}
