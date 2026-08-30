/**
 * PIN derivation & verification (Phase 3).
 *
 * - A random 128-bit salt per PIN (crypto.getRandomValues).
 * - PBKDF2-HMAC-SHA-256, 120_000 iterations → 256-bit verifier
 *   (WebCrypto subtle works in Android WebView and in Node 19+'s
 *   globalThis.crypto, so prod code and unit tests share one path).
 * - The RAW PIN never persists; only salt + verifier (base64) go to
 *   SecureStore. Verification recomputes and compares in constant time.
 * - `derivePinVerifier` is injectable for deterministic tests.
 */

import { bytesToBase64, base64ToBytes } from '../../utils/base64.ts';

const ITERATIONS = 120_000;
const KEY_LENGTH_BITS = 256;
const SALT_BYTES = 16;

export interface PinMaterial {
  saltBase64: string;
  verifierBase64: string;
}

function getCrypto(): Crypto {
  const cryptoApi = globalThis.crypto;
  if (!cryptoApi?.subtle) {
    throw new Error('WebCrypto unavailable on this runtime.');
  }
  return cryptoApi;
}

async function pbkdf2(pin: string, salt: Uint8Array): Promise<Uint8Array> {
  const cryptoApi = getCrypto();
  const keyMaterial = await cryptoApi.subtle.importKey(
    'raw',
    new TextEncoder().encode(pin),
    'PBKDF2',
    false,
    ['deriveBits'],
  );
  const bits = await cryptoApi.subtle.deriveBits(
    { name: 'PBKDF2', salt: salt as BufferSource, iterations: ITERATIONS, hash: 'SHA-256' },
    keyMaterial,
    KEY_LENGTH_BITS,
  );
  return new Uint8Array(bits);
}

/** Constant-time comparison to avoid early-exit timing signal. */
export function constantTimeEquals(a: Uint8Array, b: Uint8Array): boolean {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i += 1) diff |= a[i] ^ b[i];
  return diff === 0;
}

/** Generates fresh material for a new PIN. */
export async function createPinMaterial(pin: string): Promise<PinMaterial> {
  const salt = new Uint8Array(SALT_BYTES);
  getCrypto().getRandomValues(salt);
  const verifier = await pbkdf2(pin, salt);
  return { saltBase64: bytesToBase64(salt), verifierBase64: bytesToBase64(verifier) };
}

/** Verifies a candidate PIN against stored material (constant time). */
export async function verifyPin(pin: string, material: PinMaterial): Promise<boolean> {
  const salt = base64ToBytes(material.saltBase64);
  const expected = base64ToBytes(material.verifierBase64);
  const actual = await pbkdf2(pin, salt);
  return constantTimeEquals(actual, expected);
}
