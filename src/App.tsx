import { AppRootProvider } from './core/AppRootProvider';
import { ErrorBoundary } from './core/ErrorBoundary';
import { AppRouter } from './navigation/AppRouter';
import { useAppSettings } from './core/appSettings';
import { applyTextSize, applyThemeMode } from './core/appSettings';
import { useEffect } from 'react';

/**
 * TwoHearts application root.
 *
 * Architecture (MasterPrompt §2 preferred):
 *   React UI → Feature Logic → Repositories/Services → Local Persistence
 *                                     ↓
 *                          Capacitor Native APIs
 *
 * Phase 1 establishes the foundation only: error boundary, app root
 * provider (lifecycle + text-size), and the routing foundation. No
 * feature screens are implemented yet.
 */
export function App() {
  const settings = useAppSettings();
  useEffect(() => {
    applyTextSize(settings.textSize);
    applyThemeMode(settings.themeMode);
  }, [settings.textSize, settings.themeMode]);

  return (
    <ErrorBoundary>
      <AppRootProvider>
        <AppRouter />
      </AppRootProvider>
    </ErrorBoundary>
  );
}
