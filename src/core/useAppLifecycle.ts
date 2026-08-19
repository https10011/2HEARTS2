/**
 * Android back-button + app lifecycle foundation (Phase 1), wired onto the
 * Phase 3 lifecycle service bus (`services/lifecycle/appLifecycleService`).
 *
 * - The service singleton owns the native `appStateChange` listener (one
 *   registration, shared by app-lock auto-lock, notification
 *   reconciliation, and future feature subscribers).
 * - This hook adds the UI-facing concerns only: back-button dismissal of
 *   transient UI errors and notification-registry reconciliation on
 *   foreground (keeps the DB registry aligned with the OS pending list).
 * - In a web/dev context the service falls back to document visibility
 *   events, so semantics are identical without a device (MasterPrompt §39).
 */

import { useEffect } from 'react';
import { App as CapacitorApp } from '@capacitor/app';
import { uiStore } from './uiState';
import { useUiState } from './uiState';
import { appLifecycle } from '../services/lifecycle/appLifecycleService';
import { coreServices } from '../services/bootstrap/appBootstrap';

export function useAppLifecycle() {
  const { isMounted } = useUiState();

  useEffect(() => {
    if (!isMounted) return;

    let backListener: { remove: () => void } | undefined;

    // Android back button: dismiss transient UI errors, then defer to the
    // system when nothing intercepts (feature phases add route-aware back).
    CapacitorApp.addListener('backButton', () => {
      uiStore.setGlobalError(null);
      appLifecycle.notifyBackButton();
    })
      .then((h) => {
        backListener = h;
      })
      .catch(() => {
        // Not running on a native platform during dev — safe to ignore.
      });

    // Reconcile the notification registry against the OS pending list each
    // time the app returns to the foreground (delivered/cleared schedules).
    const unsubscribeReconcile = appLifecycle.onEvent((event) => {
      if (event === 'foreground' && coreServices.notifications) {
        void coreServices.notifications.reconcile();
      }
    });

    return () => {
      backListener?.remove();
      unsubscribeReconcile();
    };
  }, [isMounted]);
}
