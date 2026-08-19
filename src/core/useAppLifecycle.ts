/**
 * Android back-button + app lifecycle foundation.
 *
 * Uses @capacitor/app for native Android back button handling and
 * lifecycle (foreground/background) events. In a web/dev context these
 * listeners are simply inert, so the foundation degrades gracefully
 * (MasterPrompt §39, §5).
 *
 * Feature-specific back behavior is added in Phase 3+; this hook only
 * establishes the global wiring + a fallback (close any open modal, else
 * let the system handle back).
 */

import { useEffect } from 'react';
import { App as CapacitorApp } from '@capacitor/app';
import { uiStore } from './uiState';
import { useUiState } from './uiState';

export function useAppLifecycle() {
  const { isMounted } = useUiState();

  useEffect(() => {
    if (!isMounted) return;

    let backListener: { remove: () => void } | undefined;

    // Android back button: the default behavior here is to let the OS
    // handle back when there's nothing for the app to intercept.
    // Feature phases wire modal/route-aware back behavior.
    CapacitorApp.addListener('backButton', () => {
      uiStore.setGlobalError(null); // dismiss any global error sheet
      // Defer to native by not calling preventDefault in V1 foundation.
    })
      .then((h) => {
        backListener = h;
      })
      .catch(() => {
        // Not running on a native platform during dev — safe to ignore.
      });

    const lifecycle = CapacitorApp.addListener('appStateChange', ({ isActive }) => {
      // Foreground/background transitions; feature phases use this for
      // app-lock auto-lock (Phase 17) and notification reconciliation.
      void isActive;
    })
      .then((h) => h)
      .catch(() => undefined);

    return () => {
      backListener?.remove();
      lifecycle.then((h) => h?.remove()).catch(() => undefined);
    };
  }, [isMounted]);
}
