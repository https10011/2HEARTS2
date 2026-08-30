/**
 * Centralized logging (Phase 3).
 *
 * - Levels: debug < info < warn < error; default is `debug` in dev builds
 *   and `warn` in production (import.meta.env.DEV).
 * - Console transport only — NO remote telemetry (V1 boundary).
 * - `redact()` strips keys that look sensitive (pin/password/secret/token/
 *   hash/salt/media bytes…) recursively before anything is printed.
 * - Scoped loggers via `createLogger('scope')` prefix every message.
 */

export type LogLevel = 'debug' | 'info' | 'warn' | 'error';

const LEVEL_ORDER: Record<LogLevel, number> = { debug: 0, info: 1, warn: 2, error: 3 };

const SENSITIVE_KEY = /pin|pass(word)?|secret|token|hash|salt|credential|key|media|bytes|content/i;
const REDACTED = '[redacted]';

/** Recursively redact sensitive-looking keys/values from a log payload. */
export function redact(value: unknown, depth = 0): unknown {
  if (depth > 8) return REDACTED;
  if (value === null || value === undefined) return value;
  if (value instanceof Error) {
    return { name: value.name, message: redact(value.message, depth + 1) };
  }
  if (value instanceof Uint8Array || value instanceof ArrayBuffer) return REDACTED;
  if (Array.isArray(value)) return value.map((item) => redact(item, depth + 1));
  if (typeof value === 'object') {
    const out: Record<string, unknown> = {};
    for (const [key, item] of Object.entries(value as Record<string, unknown>)) {
      out[key] = SENSITIVE_KEY.test(key) ? REDACTED : redact(item, depth + 1);
    }
    return out;
  }
  return value;
}

export interface LogTransport {
  write(level: LogLevel, scope: string, message: string, payload?: unknown): void;
}

const consoleTransport: LogTransport = {
  write(level, scope, message, payload) {
    const line = `[${scope}] ${message}`;
    const args: unknown[] = payload === undefined ? [line] : [line, payload];
    // eslint-disable-next-line no-console
    (console[level === 'debug' ? 'log' : level] as (...a: unknown[]) => void)(...args);
  },
};

let currentLevel: LogLevel = (typeof import.meta !== 'undefined' && import.meta.env?.DEV) ? 'debug' : 'warn';
let transport: LogTransport = consoleTransport;

export function setLogLevel(level: LogLevel): void {
  currentLevel = level;
}

/** Swaps the transport (tests, future diagnostics screen). */
export function setLogTransport(next: LogTransport): void {
  transport = next;
}

export interface Logger {
  debug(message: string, payload?: unknown): void;
  info(message: string, payload?: unknown): void;
  warn(message: string, payload?: unknown): void;
  error(message: string, payload?: unknown): void;
}

/** Creates a scoped logger; payloads are redacted before transport. */
export function createLogger(scope: string): Logger {
  const emit = (level: LogLevel, message: string, payload?: unknown) => {
    if (LEVEL_ORDER[level] < LEVEL_ORDER[currentLevel]) return;
    transport.write(level, scope, message, payload === undefined ? undefined : redact(payload));
  };
  return {
    debug: (message, payload) => emit('debug', message, payload),
    info: (message, payload) => emit('info', message, payload),
    warn: (message, payload) => emit('warn', message, payload),
    error: (message, payload) => emit('error', message, payload),
  };
}

/** Shared root logger for infrastructure that does not own a scope. */
export const rootLogger = createLogger('app');
