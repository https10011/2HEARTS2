/**
 * AppShell (Phase 6).
 *
 * The main application layout: header area + scrollable content + bottom nav.
 * Handles Android/system back-button behavior: pressing back from a tab root
 * either navigates back within the tab or, if at the tab root, does nothing
 * (preventing accidental exit when navigation history exists).
 */

import { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { appLifecycle } from '../../services/lifecycle/appLifecycleService.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { router } from '../../navigation/AppRouter.tsx';
import { BottomNav } from './BottomNav.tsx';

/** Tab root paths — back button is suppressed at these paths. */
const TAB_ROOTS = new Set([
  '/app/home',
  '/app/us',
  '/app/games',
  '/app/notes',
  '/app/more',
]);

export function AppShell() {
  // Android back-button handling
  useEffect(() => {
    const unsubscribe = appLifecycle.onEvent((event) => {
      if (event === 'backButton') {
        // Reconcile notification registry on foreground return
        if (coreServices.notifications) {
          void coreServices.notifications.reconcile();
        }

        // Navigate back if we have history and aren't at a tab root
        const location = window.location.pathname;
        if (TAB_ROOTS.has(location)) {
          // At tab root — don't navigate (would exit the app)
          return;
        }
        // Use the router instance to go back
        router.navigate(-1);
      }
    });

    return unsubscribe;
  }, []);

  return (
    <div className="th-app-shell">
      <div className="th-app-content">
        <Outlet />
      </div>
      <BottomNav />
    </div>
  );
}
