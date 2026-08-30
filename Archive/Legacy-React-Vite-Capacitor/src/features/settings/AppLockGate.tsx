/**
 * AppLockGate (Phase 19).
 *
 * The app-wide lock surface the Settings App Lock protects: whenever the
 * AppLockService state is 'locked' (cold start with lock enabled,
 * foreground timeout, or "Lock TwoHearts Now"), the whole app is covered
 * by this unlock overlay until the PIN verifies. PIN verification goes
 * through AppLockService.unlock only — the gate never sees or stores any
 * PIN material. The Vault listens to the same lock bus, so unlocking the
 * app also unlocks the vault session.
 */

import { useEffect, useState, type ReactNode } from 'react';
import { Button, Input, BrandLogo } from '../../components/index.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import type { LockState } from '../../services/security/appLockService.ts';

export function AppLockGate({ children }: { children: ReactNode }) {
  const appLock = coreServices.appLock;
  const [state, setState] = useState<LockState>(appLock?.currentState() ?? 'disabled');
  const [pin, setPin] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!appLock) return;
    setState(appLock.currentState());
    return appLock.onLockChange((next) => {
      setState(next);
      setPin('');
      setError(null);
    });
  }, [appLock]);

  if (!appLock || state !== 'locked') return <>{children}</>;

  const unlock = async () => {
    setBusy(true);
    setError(null);
    try {
      const ok = await appLock.unlock(pin);
      if (!ok) setError('That PIN did not work. Please try again.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <main
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 'var(--th-z-lock)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 'var(--th-space-4)',
        padding: 'var(--th-space-6)',
        background: 'var(--th-color-bg)',
      }}
      aria-label="TwoHearts is locked"
    >
      {/* Official hearts mark — one BrandLogo component, one asset (Phase 23) */}
      <BrandLogo variant="mark" size={88} title="TwoHearts" />
      <p style={{ margin: 0, color: 'var(--th-color-text-secondary)' }}>Enter your PIN to unlock.</p>
      <form
        style={{ width: '100%', maxWidth: '320px' }}
        onSubmit={(e) => {
          e.preventDefault();
          void unlock();
        }}
      >
        <Input
          type="password"
          inputMode="numeric"
          value={pin}
          onChange={(e) => setPin(e.target.value)}
          placeholder="PIN"
          aria-label="App lock PIN"
          autoComplete="off"
          autoFocus
        />
        {error ? (
          <p role="alert" style={{ color: 'var(--th-color-error)', fontSize: 'var(--th-font-size-sm)' }}>
            {error}
          </p>
        ) : null}
        <div style={{ marginTop: 'var(--th-space-3)' }}>
          <Button variant="primary" full type="submit" disabled={busy || pin.length < 4}>
            {busy ? 'Unlocking…' : 'Unlock'}
          </Button>
        </div>
      </form>
    </main>
  );
}
