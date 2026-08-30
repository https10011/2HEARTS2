/**
 * Private App Lock foundation (Phase 3) — service boundary ONLY.
 *
 * - PIN material (salt + verifier) lives in SecureStore — never plaintext,
 *   never in settings, never in UI state.
 * - Settings (`core/appSettings.ts`) holds only NON-sensitive configuration:
 *   `appLockEnabled`, `lockTimeoutSeconds` (query via getLockSettings).
 * - Lock STATE is memory-only on purpose: the app re-locks on every cold
 *   start when enabled (secure-by-default; no persisted "unlocked" bit).
 * - Background→foreground re-lock: subscribes to the Phase 3 lifecycle bus;
 *   unlock age exceeding the configured timeout re-locks on foreground.
 * - The unlock UI arrives with the feature phase; `onLockChange` is the
 *   seam a future lock gate component subscribes to.
 */

import { AppError, normalizeAppError } from '../errors/appError.ts';
import { createLogger } from '../logging/logger.ts';
import { appLifecycle } from '../lifecycle/appLifecycleService.ts';
import { createPinMaterial, verifyPin, type PinMaterial } from './pinHash.ts';
import { SECURE_STORE_KEYS, type SecureStore } from './secureStore.ts';

const log = createLogger('app-lock');

export type LockState = 'disabled' | 'unlocked' | 'locked';

export interface LockSettings {
  enabled: boolean;
  /** Seconds of background inactivity after which the app re-locks. */
  timeoutSeconds: number;
}

export type LockListener = (state: LockState) => void;

export class AppLockService {
  private state: LockState = 'disabled';
  private listeners = new Set<LockListener>();
  private material: PinMaterial | null = null;
  private unlockedAt: Date | null = null;
  private lifecycleUnsub: (() => void) | null = null;
  private readonly now: () => Date;

  constructor(
    private readonly store: SecureStore,
    private readonly getSettings: () => LockSettings,
    now: () => Date = () => new Date(),
  ) {
    this.now = now;
  }

  /** Loads persisted material; binds to lifecycle once. Safe to re-call. */
  async initialize(): Promise<LockState> {
    try {
      const [salt, verifier] = await Promise.all([
        this.store.get(SECURE_STORE_KEYS.pinSalt),
        this.store.get(SECURE_STORE_KEYS.pinVerifier),
      ]);
      this.material = salt && verifier ? { saltBase64: salt, verifierBase64: verifier } : null;
    } catch (cause) {
      log.warn('Failed to load lock material; treating as not configured.', { cause });
      this.material = null;
    }
    this.lifecycleUnsub ??= appLifecycle.onEvent((event) => {
      if (event === 'foreground') this.onForeground();
    });
    this.state = this.resolveState();
    return this.state;
  }

  currentState(): LockState {
    return this.state;
  }

  isConfigured(): boolean {
    return this.material !== null;
  }

  onLockChange(listener: LockListener): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  /** Sets up app lock with a PIN. Enables it in the current session. */
  async enable(pin: string): Promise<void> {
    this.assertPinShape(pin);
    try {
      const material = await createPinMaterial(pin);
      await this.store.set(SECURE_STORE_KEYS.pinSalt, material.saltBase64);
      await this.store.set(SECURE_STORE_KEYS.pinVerifier, material.verifierBase64);
      this.material = material;
      this.transition('unlocked');
    } catch (cause) {
      throw normalizeAppError(cause, 'security', 'lock-enable-failed', { recoverable: true });
    }
  }

  /** Verifies a PIN candidate; on success the session is unlocked. */
  async unlock(pin: string): Promise<boolean> {
    if (!this.material) return false;
    try {
      const matched = await verifyPin(pin, this.material);
      if (matched) {
        this.unlockedAt = this.now();
        this.transition('unlocked');
      }
      return matched;
    } catch (cause) {
      throw normalizeAppError(cause, 'security', 'lock-verify-failed', { recoverable: true });
    }
  }

  /** Explicit lock (also used by future UI affordances). */
  lock(): void {
    if (this.state !== 'disabled') this.transition('locked');
  }

  /** Removes PIN material entirely; disables locking. */
  async disable(): Promise<void> {
    try {
      await Promise.all([
        this.store.remove(SECURE_STORE_KEYS.pinSalt),
        this.store.remove(SECURE_STORE_KEYS.pinVerifier),
      ]);
    } finally {
      this.material = null;
      this.unlockedAt = null;
      this.transition('disabled');
    }
  }

  private resolveState(): LockState {
    if (!this.getSettings().enabled || !this.material) return 'disabled';
    return 'locked';
  }

  private onForeground(): void {
    if (this.state !== 'unlocked') return;
    const timeout = this.getSettings().timeoutSeconds;
    const age = this.unlockedAt ? (this.now().getTime() - this.unlockedAt.getTime()) / 1000 : Number.MAX_VALUE;
    if (age > Math.max(0, timeout)) this.transition('locked');
  }

  private transition(next: LockState): void {
    if (this.state === next) return;
    this.state = next;
    if (next === 'unlocked') this.unlockedAt = this.now();
    for (const listener of [...this.listeners]) {
      try {
        listener(next);
      } catch {
        // Subscriber failures must never leave the lock in an odd state.
      }
    }
    log.debug('Lock state changed.', { state: next });
  }

  private assertPinShape(pin: string): void {
    if (!/^\d{4,8}$/.test(pin)) {
      throw new AppError('security', 'invalid-pin-shape', {
        recoverable: false,
        userMessage: 'The PIN must be 4–8 digits.',
      });
    }
  }
}
