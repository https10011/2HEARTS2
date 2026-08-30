/**
 * Application lifecycle integration (Phase 3).
 *
 * Extends the Phase 1 wiring with a shared, service-level event bus:
 * `AppLifecycleService` owns the native listeners (@capacitor/app) and
 * broadcasts `foreground`/`background`/`backButton` events to subscribers
 * (app lock auto-lock, notification reconciliation, …). The React hook in
 * src/core/useAppLifecycle stays as the UI-facing mount and simply delegates
 * to the service singleton — one listener chain, not N plugin registrations.
 *
 * In a web/dev context the service falls back to document visibility events
 * so the same semantics are testable without a device.
 */

import { App as CapacitorApp } from '@capacitor/app';

export type LifecycleEvent = 'foreground' | 'background' | 'backButton';
export type LifecycleListener = (event: LifecycleEvent) => void;

export class AppLifecycleService {
  private listeners = new Set<LifecycleListener>();
  private removal: (() => void) | null = null;
  private current: 'foreground' | 'background' = 'background';

  /** Registers a subscriber; returns an unsubscribe function. */
  onEvent(listener: LifecycleListener): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  state(): 'foreground' | 'background' {
    return this.current;
  }

  /** Starts native (or web fallback) listeners exactly once. */
  async start(): Promise<void> {
    if (this.removal) return;
    try {
      const handle = await CapacitorApp.addListener('appStateChange', ({ isActive }) => {
        this.current = isActive ? 'foreground' : 'background';
        this.emit(this.current);
      });
      this.removal = () => void handle.remove();
      return;
    } catch {
      // Non-native context: fall back to document visibility.
    }
    if (typeof document !== 'undefined') {
      const handler = () => {
        this.current = document.visibilityState === 'hidden' ? 'background' : 'foreground';
        this.emit(this.current);
      };
      document.addEventListener('visibilitychange', handler);
      this.removal = () => document.removeEventListener('visibilitychange', handler);
    }
  }

  /** Notifies the system back event (Phase 1 wires this from useAppLifecycle). */
  notifyBackButton(): void {
    this.emit('backButton');
  }

  /** Test support: broadcast an event without a native trigger. */
  simulate(event: LifecycleEvent): void {
    if (event !== 'backButton') this.current = event;
    this.emit(event);
  }

  /** Stops listening (+ clears subscribers) — used by tests. */
  stop(): void {
    this.removal?.();
    this.removal = null;
    this.listeners.clear();
  }

  private emit(event: LifecycleEvent): void {
    for (const listener of [...this.listeners]) {
      try {
        listener(event);
      } catch {
        // A subscriber failure must never break other subscribers.
      }
    }
  }
}

/** Singleton bus; services subscribe here. */
export const appLifecycle = new AppLifecycleService();
