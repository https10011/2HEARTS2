/**
 * Local identifier strategy (Phase 2).
 *
 * All domain entities are identified by RFC 4122 version 4 UUIDs generated
 * on-device. Auto-increment row ids are never used as entity identity, which
 * keeps identifiers stable for a future V2 synchronization layer and
 * independent of database row ordering.
 */

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;

/** Generates a random UUID v4. Falls back if crypto.randomUUID is unavailable. */
export function newId(): string {
  const cryptoApi = globalThis.crypto;
  if (cryptoApi && typeof cryptoApi.randomUUID === 'function') {
    return cryptoApi.randomUUID();
  }
  // Fallback for environments without randomUUID (older WebViews / tests).
  const bytes = new Uint8Array(16);
  cryptoApi.getRandomValues(bytes);
  bytes[6] = (bytes[6] & 0x0f) | 0x40; // version 4
  bytes[8] = (bytes[8] & 0x3f) | 0x80; // variant
  const hex = [...bytes].map((b) => b.toString(16).padStart(2, '0'));
  return `${hex.slice(0, 4).join('')}-${hex.slice(4, 6).join('')}-${hex.slice(6, 8).join('')}-${hex.slice(8, 10).join('')}-${hex.slice(10, 16).join('')}`;
}

/** Returns true when value is a well-formed lowercase UUID v4. */
export function isValidId(value: string): boolean {
  return UUID_PATTERN.test(value);
}
