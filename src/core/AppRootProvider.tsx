import { useEffect, type ReactNode } from 'react';
import { useAppSettings, applyTextSize } from './appSettings';
import { uiStore } from './uiState';
import { useAppLifecycle } from './useAppLifecycle';

/**
 * AppRootProvider — wires application-level foundation behavior:
 *   - mark mounted (enables lifecycle/back-button wiring)
 *   - apply the user text-size scale to the document root
 *   - run the Capacitor app lifecycle hook (Android back/foreground)
 *
 * This is intentionally minimal for Phase 1; feature phases extend it
 * (app-lock gate, notification reconciliation, etc.).
 */
export function AppRootProvider({ children }: { children: ReactNode }) {
  const settings = useAppSettings();
  useAppLifecycle();

  useEffect(() => {
    uiStore.setMounted(true);
    return () => uiStore.setMounted(false);
  }, []);

  useEffect(() => {
    applyTextSize(settings.textSize);
  }, [settings.textSize]);

  return <>{children}</>;
}
