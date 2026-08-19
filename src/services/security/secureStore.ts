/**
 * Secure storage boundary (Phase 3).
 *
 * Sensitive material (app-lock hash + salt). ONLY small, security-scoped
 * strings belong here. Ordinary local domain data is NOT secure storage and
 * never pretends to be (docs/core-services.md records the boundary).
 *
 * Drivers:
 * - `capacitorSecureStore` (@aparajita/capacitor-secure-storage — Android
 *   Keystore/EncryptedSharedPreferences; the dependency is justified for
 *   exactly this binding, see Phase 3 report),
 * - `MemorySecureStore` (tests/web dev — explicitly NOT secure; marked so).
 */

export interface SecureStore {
  get(key: string): Promise<string | null>;
  set(key: string, value: string): Promise<void>;
  remove(key: string): Promise<void>;
  keys(): Promise<string[]>;
}

export const SECURE_STORE_KEYS = {
  /** Base64-encoded random salt for PIN derivation. */
  pinSalt: 'twohearts.lock.salt',
  /** Base64-encoded derived PIN verifier. */
  pinVerifier: 'twohearts.lock.verifier',
} as const;

export class MemorySecureStore implements SecureStore {
  /** NOT SECURE — tests/web-dev substitute only. */
  readonly secure = false;
  private map = new Map<string, string>();

  async get(key: string): Promise<string | null> {
    return this.map.get(key) ?? null;
  }
  async set(key: string, value: string): Promise<void> {
    this.map.set(key, value);
  }
  async remove(key: string): Promise<void> {
    this.map.delete(key);
  }
  async keys(): Promise<string[]> {
    return [...this.map.keys()];
  }
}
