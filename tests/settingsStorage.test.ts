import { test, afterEach } from 'node:test';
import assert from 'node:assert';
import { LocalStorageSettingsStorage } from '../src/data/settings/settingsStorage.ts';

const originalLocalStorage = (globalThis as { localStorage?: Storage }).localStorage;

afterEach(() => {
  (globalThis as { localStorage?: Storage }).localStorage = originalLocalStorage;
});

function fakeLocalStorage(): Storage {
  const map = new Map<string, string>();
  return {
    getItem: (key: string) => map.get(key) ?? null,
    setItem: (key: string, value: string) => void map.set(key, value),
    removeItem: (key: string) => void map.delete(key),
    clear: () => map.clear(),
    key: () => null,
    get length() {
      return map.size;
    },
  } as Storage;
}

test('reads and writes through to localStorage when present', () => {
  (globalThis as { localStorage?: Storage }).localStorage = fakeLocalStorage();
  const storage = new LocalStorageSettingsStorage();
  storage.set('key1', 'value1');
  assert.strictEqual(localStorage.getItem('key1'), 'value1');
  assert.strictEqual(new LocalStorageSettingsStorage().get('key1'), 'value1');
  storage.remove('key1');
  assert.strictEqual(localStorage.getItem('key1'), null);
});

test('falls back to memory when localStorage is unavailable', () => {
  (globalThis as { localStorage?: Storage }).localStorage = undefined as unknown as Storage;
  const storage = new LocalStorageSettingsStorage();
  storage.set('key', 'session-value');
  assert.strictEqual(storage.get('key'), 'session-value');
});

test('falls back to memory when localStorage throws', () => {
  (globalThis as { localStorage?: Storage }).localStorage = {
    getItem: () => {
      throw new Error('denied');
    },
    setItem: () => {
      throw new Error('denied');
    },
    removeItem: () => undefined,
  } as unknown as Storage;
  const storage = new LocalStorageSettingsStorage();
  storage.set('key', 'session-value');
  assert.strictEqual(storage.get('key'), 'session-value');
});

test('prefixes isolate consumers', () => {
  const a = new LocalStorageSettingsStorage('a.');
  a.set('k', 'va');
  const b = new LocalStorageSettingsStorage('b.');
  assert.strictEqual(b.get('k'), null);
  b.set('k', 'vb');
  assert.strictEqual(a.get('k'), 'va');
  a.remove('k');
  b.remove('k');
});
