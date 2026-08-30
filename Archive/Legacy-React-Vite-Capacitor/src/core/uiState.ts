/**
 * Ephemeral UI state store.
 *
 * Holds transient, non-persistent UI state (modal/sheet open state, route
 * transitions, transient banners). Separated from persistent domain
 * state (appSettings) and from the future domain data layer so storage
 * changes don't force UI rewrites (MasterPrompt §11, §16).
 */

import { useSyncExternalStore } from 'react';

interface UiState {
  isMounted: boolean;
  globalError: string | null;
}

type Listener = () => void;
const listeners = new Set<Listener>();
let current: UiState = { isMounted: false, globalError: null };

function emit() {
  listeners.forEach((l) => l());
}

export const uiStore = {
  getState: () => current,
  subscribe(listener: Listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
  },
  setMounted(mounted: boolean) {
    current = { ...current, isMounted: mounted };
    emit();
  },
  setGlobalError(message: string | null) {
    current = { ...current, globalError: message };
    emit();
  },
};

export function useUiState(): UiState {
  return useSyncExternalStore(uiStore.subscribe, uiStore.getState);
}
