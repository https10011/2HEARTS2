/**
 * Keystore-backed SecureStore driver (Android prod; Phase 3).
 *
 * @aparajita/capacitor-secure-storage is the only bridge site; its async
 * API (get/set/remove/keys, key-prefix semantics) maps 1:1 onto the
 * SecureStore contract. On Android the plugin writes to
 * EncryptedSharedPreferences (Android Keystore) — hardware-backed on
 * supported devices.
 */

import { SecureStorage } from '@aparajita/capacitor-secure-storage';
import type { SecureStore } from './secureStore.ts';

export const capacitorSecureStore: SecureStore = {
  async get(key: string): Promise<string | null> {
    const value = await SecureStorage.get(key);
    return value === null || value === undefined ? null : String(value);
  },
  async set(key: string, value: string): Promise<void> {
    await SecureStorage.set(key, value);
  },
  async remove(key: string): Promise<void> {
    await SecureStorage.remove(key);
  },
  async keys(): Promise<string[]> {
    return SecureStorage.keys();
  },
};
