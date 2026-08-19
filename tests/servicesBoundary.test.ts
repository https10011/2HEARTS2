import { test } from 'node:test';
import assert from 'node:assert';
import { PermissionService, type PermissionProvider } from '../src/services/permissions/permissionService.ts';
import { DeviceCapabilities, fakeDeviceProvider } from '../src/services/device/deviceCapabilities.ts';
import { validateMediaCandidate, verifyMediaBytes, sniffMimeType, extensionForMime, kindForMime } from '../src/services/media/mediaUtils.ts';
import { validate, required, textLength, validIsoDate, numericRange, validId, normalizeInput } from '../src/services/validation/validators.ts';
import { newId } from '../src/utils/ids.ts';
import { AppLifecycleService } from '../src/services/lifecycle/appLifecycleService.ts';

// --- Permissions -----------------------------------------------------------

test('permission service normalizes providers and never throws', async () => {
  const granted: PermissionProvider = {
    check: async () => 'granted',
    request: async () => 'granted',
  };
  const promptThenGranted: PermissionProvider = {
    check: async () => 'prompt',
    request: async () => 'granted',
  };
  const broken: PermissionProvider = {
    check: async () => {
      throw new Error('native crash');
    },
    request: async () => {
      throw new Error('native crash');
    },
  };
  const service = new PermissionService({ notifications: granted });
  service.register('camera', promptThenGranted);
  service.register('photos', broken);

  assert.strictEqual(await service.check('notifications'), 'granted');
  assert.strictEqual(await service.ensure('camera'), 'granted'); // prompt → request path
  assert.strictEqual(await service.check('photos'), 'unavailable'); // broken → unavailable
  assert.strictEqual(await service.check('mediaStorage'), 'unavailable'); // unregistered
});

test('ensure skips request when already denied', async () => {
  let requests = 0;
  const denied: PermissionProvider = {
    check: async () => 'denied',
    request: async () => {
      requests += 1;
      return 'denied';
    },
  };
  const service = new PermissionService({ notifications: denied });
  assert.strictEqual(await service.ensure('notifications'), 'denied');
  assert.strictEqual(requests, 0);
});

// --- Device capabilities ---------------------------------------------------

test('device capability matrix distinguishes native vs web', async () => {
  const android = new DeviceCapabilities(fakeDeviceProvider(
    { platform: 'android', osVersion: '14', model: 'Pixel', manufacturer: 'Google' },
    true,
  ));
  await android.initialize();
  assert.ok(android.isAndroid());
  assert.ok(android.has('notifications'));
  assert.ok(android.has('secureStorage'));
  assert.strictEqual(android.osVersion(), '14');

  const web = new DeviceCapabilities(fakeDeviceProvider(
    { platform: 'web', osVersion: null, model: null, manufacturer: null },
  ));
  await web.initialize();
  assert.ok(!web.isAndroid());
  assert.ok(!web.has('notifications')); // native-only in prod
  assert.ok(web.has('localStorage'));   // settings fallback
});

test('device probe failure degrades gracefully instead of blocking', async () => {
  const failing = new DeviceCapabilities({
    isNative: () => false,
    info: () => Promise.reject(new Error('device info unavailable')),
  });
  const info = await failing.initialize();
  assert.strictEqual(info.platform, 'web');
  assert.ok(!failing.has('notifications'));
});

// --- Media utilities -------------------------------------------------------

test('media candidate policy enforces kind/mime/size', () => {
  assert.ok(validateMediaCandidate({ kind: 'photo', mimeType: 'image/jpeg', sizeBytes: 1024 }).ok);
  assert.ok(!validateMediaCandidate({ kind: 'photo', mimeType: 'video/mp4', sizeBytes: 1024 }).ok);
  const tooBig = validateMediaCandidate({ kind: 'photo', mimeType: 'image/png', sizeBytes: 26 * 1024 * 1024 });
  assert.ok(!tooBig.ok);
  assert.strictEqual(extensionForMime('image/jpeg'), 'jpg');
  assert.strictEqual(extensionForMime('text/plain'), null);
  assert.strictEqual(kindForMime('video/mp4'), 'video');
});

test('magic-byte sniffing catches spoofed content types', () => {
  const png = new Uint8Array([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0, 0, 0, 0]);
  assert.strictEqual(sniffMimeType(png), 'image/png');
  const jpeg = new Uint8Array([0xff, 0xd8, 0xff, 0xe0]);
  assert.strictEqual(sniffMimeType(jpeg), 'image/jpeg');
  const mp4 = new Uint8Array([0, 0, 0, 24, 0x66, 0x74, 0x79, 0x70, 0x69, 0x73, 0x6f, 0x6d]);
  assert.strictEqual(sniffMimeType(mp4), 'video/mp4');
  assert.strictEqual(sniffMimeType(new Uint8Array([0x01, 0x02])), null);

  const okResult = verifyMediaBytes({ kind: 'photo', declaredMimeType: 'image/png', data: png });
  assert.ok(okResult.ok);
  const spoofed = verifyMediaBytes({ kind: 'photo', declaredMimeType: 'image/png', data: jpeg });
  assert.ok(!spoofed.ok);
  const garbage = verifyMediaBytes({ kind: 'photo', declaredMimeType: 'image/png', data: new Uint8Array([1, 2, 3, 4]) });
  assert.ok(!garbage.ok);
});

// --- Validation -------------------------------------------------------------

test('shared validators compose predictably', () => {
  assert.ok(validate(required('x'), textLength('hello', 2, 10)).ok);
  const bad = validate(required(''), textLength('a', 2, 10), numericRange(5, 0, 3));
  assert.ok(!bad.ok);
  assert.strictEqual(bad.errors.length, 3);
  assert.ok(validIsoDate('2024-02-29').ok); // leap year is valid
  assert.ok(!validIsoDate('2023-02-29').ok); // not a real calendar date
  assert.ok(validId(newId()).ok);
  assert.ok(!validId('not-an-id').ok);
  assert.strictEqual(normalizeInput('  a   b '), 'a b');
});

// --- Lifecycle --------------------------------------------------------------

test('lifecycle bus emits events to all subscribers and isolate failures', () => {
  const bus = new AppLifecycleService();
  const seen: string[] = [];
  bus.onEvent((e) => seen.push(`a:${e}`));
  bus.onEvent(() => {
    throw new Error('subscriber crash');
  });
  bus.onEvent((e) => seen.push(`c:${e}`));
  bus.simulate('foreground');
  bus.simulate('backButton');
  bus.simulate('background');
  assert.deepStrictEqual(seen, ['a:foreground', 'c:foreground', 'a:backButton', 'c:backButton', 'a:background', 'c:background']);
  assert.strictEqual(bus.state(), 'background');
  bus.stop();
});
