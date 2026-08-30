/**
 * AppShell (Phase 6, refined Phase 24).
 *
 * The main application layout: scrollable content + five-position bottom nav.
 *
 * Android/system back-button behavior (Phase 24 contract):
 *   - Deep/nested screen → navigate(-1), returning to the previous context
 *     (Home → Notes → back → Home; Us → Memories → back → Us).
 *   - Home root → suppressed (never accidentally exit the application).
 *   - Degenerate entry state (no in-app history at a non-home route) →
 *     fall forward to Home instead of exiting.
 *
 * Route changes get a subtle token-driven entrance transition (fade + small
 * rise) keyed on the pathname; scroll resets to the top on each navigation.
 */

import { useEffect, useRef } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { appLifecycle } from '../../services/lifecycle/appLifecycleService.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { router } from '../../navigation/AppRouter.tsx';
import { RoutePath } from '../../navigation/routes.ts';
import { BottomNav } from './BottomNav.tsx';
import { ToastProvider } from '../../components/toast.tsx';

/** History index threshold: idx > 0 means an in-app back target exists. */
function hasInAppHistory(): boolean {
  const idx = (window.history.state as { idx?: number } | null)?.idx;
  return typeof idx === 'number' && idx > 0;
}

export function AppShell() {
  const location = useLocation();
  const contentRef = useRef<HTMLDivElement | null>(null);

  // Android back-button handling
  useEffect(() => {
    const unsubscribe = appLifecycle.onEvent((event) => {
      if (event === 'backButton') {
        // Reconcile notification registry on foreground return
        if (coreServices.notifications) {
          void coreServices.notifications.reconcile();
        }

        const path = window.location.pathname;
        if (path === RoutePath.appHome) {
          // At the Home root — don't navigate (would exit the app)
          return;
        }
        if (hasInAppHistory()) {
          router.navigate(-1);
        } else {
          router.navigate(RoutePath.appHome);
        }
      }
    });

    return unsubscribe;
  }, []);

  // Each navigation starts at the top of the new destination.
  useEffect(() => {
    contentRef.current?.scrollTo({ top: 0 });
  }, [location.pathname]);

  return (
    <div className="th-app-shell">
      {/* One toast host for the whole app (Phase 25) — screens publish via
          useToast(); the toast survives the navigation it triggered. */}
      <ToastProvider>
        <div className="th-app-content" ref={contentRef}>
          <div className="th-route-transition" key={location.pathname}>
            <Outlet />
          </div>
        </div>
      </ToastProvider>
      <BottomNav />
    </div>
  );
}
