import { test } from 'node:test';
import assert from 'node:assert';
import { createLogger, redact, setLogLevel, setLogTransport, type LogTransport, type LogLevel } from '../src/services/logging/logger.ts';
import { AppError, safeUserMessage, normalizeAppError } from '../src/services/errors/appError.ts';
import { PersistenceError } from '../src/data/database/errors.ts';

test('redact strips sensitive keys recursively', () => {
  const payload = {
    pin: '1234',
    nested: { password: 'x', apiToken: 'abc', safe: 'kept' },
    list: [{ salt: 's', keep: 1 }],
    bytes: new Uint8Array([1, 2]),
  };
  const result = redact(payload) as Record<string, unknown>;
  assert.strictEqual(result.pin, '[redacted]');
  assert.strictEqual((result.nested as Record<string, unknown>).password, '[redacted]');
  assert.strictEqual((result.nested as Record<string, unknown>).apiToken, '[redacted]');
  assert.strictEqual((result.nested as Record<string, unknown>).safe, 'kept');
  assert.strictEqual((result.list as Array<Record<string, unknown>>)[0].salt, '[redacted]');
  assert.strictEqual(result.bytes, '[redacted]');
});

test('logger filters by level and redacts payloads', () => {
  const written: Array<{ level: LogLevel; message: string; payload?: unknown }> = [];
  const capture: LogTransport = {
    write(level, scope, message, payload) {
      written.push({ level, message: `[${scope}] ${message}`, payload });
    },
  };
  setLogTransport(capture);
  try {
    setLogLevel('warn');
    const log = createLogger('test-scope');
    log.debug('hidden');
    log.info('also hidden');
    log.warn('visible', { pin: '9999', note: 'ok' });
    log.error('bad', new Error('boom'));
    assert.strictEqual(written.length, 2);
    assert.strictEqual(written[0].message, '[test-scope] visible');
    assert.deepStrictEqual(written[0].payload, { pin: '[redacted]', note: 'ok' });
  } finally {
    setLogTransport({ write: () => {} }); // silence for other tests
    setLogLevel('debug');
  }
});

test('AppError carries category/code/recoverable and safe user messages', () => {
  const err = new AppError('notification', 'schedule-failed', { recoverable: true });
  assert.strictEqual(err.category, 'notification');
  assert.strictEqual(err.code, 'schedule-failed');
  assert.strictEqual(err.recoverable, true);
  assert.strictEqual(safeUserMessage(err), 'Notification scheduling failed.');
  assert.strictEqual(safeUserMessage(new PersistenceError('query-failed', 'x')), 'Local data could not be accessed.');
  assert.strictEqual(safeUserMessage(new Error('raw stack')), 'An unexpected error occurred.');
});

test('normalizeAppError translates persistence errors and passes AppError through', () => {
  const original = new AppError('media', 'too-large');
  assert.strictEqual(normalizeAppError(original, 'service', 'x'), original);

  const translated = normalizeAppError(
    new PersistenceError('migration-failed', 'x'),
    'service',
    'bootstrap',
  );
  assert.strictEqual(translated.category, 'persistence');
  assert.strictEqual(translated.recoverable, false); // migration failure is not retryable inline

  const wrapped = normalizeAppError(new Error('nope'), 'filesystem', 'read-failed');
  assert.strictEqual(wrapped.category, 'filesystem');
  assert.strictEqual(wrapped.code, 'read-failed');
});
