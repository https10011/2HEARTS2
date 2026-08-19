import { test } from 'node:test';
import assert from 'node:assert';
import { AppLockService } from '../src/services/security/appLockService.ts';
import { MemorySecureStore, SECURE_STORE_KEYS } from '../src/services/security/secureStore.ts';
import { createPinMaterial, verifyPin, constantTimeEquals } from '../src/services/security/pinHash.ts';
import { appLifecycle } from '../src/services/lifecycle/appLifecycleService.ts';
import { bytesToBase64, base64ToBytes } from '../src/utils/base64.ts';

test('PIN material is salted PBKDF2, verifiable, never plaintext', async () => {
  const material = await createPinMaterial('2468');
  assert.notStrictEqual(material.verifierBase64, '2468');
  assert.ok((await verifyPin('2468', material)));
  assert.ok(!(await verifyPin('2469', material)));
  // different salts → different verifiers for the same PIN
  const other = await createPinMaterial('2468');
  assert.notStrictEqual(material.saltBase64, other.saltBase64);
  assert.notStrictEqual(material.verifierBase64, other.verifierBase64);
});

test('constantTimeEquals only matches identical buffers', () => {
  assert.ok(constantTimeEquals(base64ToBytes(bytesToBase64(new Uint8Array([1, 2]))), new Uint8Array([1, 2])));
  assert.ok(!constantTimeEquals(new Uint8Array([1, 2]), new Uint8Array([1, 3])));
  assert.ok(!constantTimeEquals(new Uint8Array([1]), new Uint8Array([1, 2])));
});

test('AppLockService enable → lock → unlock round trip persists only salt+verifier', async () => {
  appLifecycle.stop(); // isolate from other tests
  const store = new MemorySecureStore();
  let now = new Date(1_700_000_000_000);
  const service = new AppLockService(store, () => ({ enabled: true, timeoutSeconds: 60 }), () => now);

  await service.initialize();
  assert.strictEqual(service.currentState(), 'disabled');

  await service.enable('4321');
  assert.strictEqual(service.currentState(), 'unlocked');
  const storedKeys = await store.keys();
  assert.ok(storedKeys.includes(SECURE_STORE_KEYS.pinSalt));
  assert.ok(storedKeys.includes(SECURE_STORE_KEYS.pinVerifier));
  // The raw PIN must not appear anywhere in stored values.
  for (const key of storedKeys) {
    assert.notStrictEqual(await store.get(key), '4321');
  }

  service.lock();
  assert.strictEqual(service.currentState(), 'locked');
  assert.ok(!(await service.unlock('1111')));
  assert.strictEqual(service.currentState(), 'locked');
  assert.ok(await service.unlock('4321'));
  assert.strictEqual(service.currentState(), 'unlocked');
});

test('disabled when enabled=false in settings even with material present', async () => {
  appLifecycle.stop();
  const store = new MemorySecureStore();
  const service = new AppLockService(store, () => ({ enabled: false, timeoutSeconds: 60 }));
  await service.enable('1234');
  await service.initialize();
  assert.strictEqual(service.currentState(), 'disabled');
});

test('re-locks on foreground after inactivity timeout', async () => {
  appLifecycle.stop();
  const store = new MemorySecureStore();
  let now = new Date(1_700_000_000_000);
  const service = new AppLockService(store, () => ({ enabled: true, timeoutSeconds: 30 }), () => now);
  await service.initialize(); // binds lifecycle listener
  await service.enable('7777');
  assert.strictEqual(service.currentState(), 'unlocked');

  // Foreground within timeout → stays unlocked.
  now = new Date(now.getTime() + 10_000);
  appLifecycle.simulate('foreground');
  assert.strictEqual(service.currentState(), 'unlocked');

  // Past timeout → re-locks.
  now = new Date(now.getTime() + 120_000);
  appLifecycle.simulate('foreground');
  assert.strictEqual(service.currentState(), 'locked');
});

test('disable clears material and returns to disabled state', async () => {
  appLifecycle.stop();
  const store = new MemorySecureStore();
  const service = new AppLockService(store, () => ({ enabled: true, timeoutSeconds: 60 }));
  await service.enable('1234');
  await service.disable();
  assert.strictEqual(service.currentState(), 'disabled');
  assert.strictEqual(service.isConfigured(), false);
  assert.deepStrictEqual(await store.keys(), []);
});

test('invalid PIN shapes are rejected', async () => {
  appLifecycle.stop();
  const store = new MemorySecureStore();
  const service = new AppLockService(store, () => ({ enabled: true, timeoutSeconds: 60 }));
  await assert.rejects(service.enable('abc'), /invalid-pin-shape/);
  await assert.rejects(service.enable('12'), /invalid-pin-shape/);
  await assert.rejects(service.enable('123456789'), /invalid-pin-shape/);
});
