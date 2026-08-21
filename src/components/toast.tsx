/**
 * Toast — the ONE system-wide transient feedback channel (Phase 25).
 *
 * Screens publish `Saved / Deleted / Updated / Error` (and friends) through
 * the `useToast()` hook; a single host mounted in AppShell renders the
 * message above the bottom navigation. This keeps feedback consistent and
 * prevents screen-local overlay hacks.
 *
 * Behavior:
 *  - Auto-dismiss: the toast exits after TOAST_DURATION (never stale).
 *  - Exit: a short fade before removal; reduced-motion users get a
 *    near-instant exit via the tokenized `--th-motion-exit`.
 *  - Variants: success / error / info — state color + stable icon, never
 *    animation-only feedback.
 */

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { IconCheck, IconClose, IconInfo } from './Icon.tsx';
import './primitives.css';

export type ToastVariant = 'success' | 'error' | 'info';

export interface ToastMessage {
  text: string;
  variant: ToastVariant;
}

export interface ToastApi {
  /** Show a toast; replaces any currently visible toast. */
  show(text: string, variant?: ToastVariant): void;
  success(text: string): void;
  error(text: string): void;
  info(text: string): void;
}

/** Time a toast stays fully visible (exit is additionally animated). */
export const TOAST_DURATION_MS = 2400;
/** Budget for the fade-out before unmount (fast-exit token is ≤120ms). */
export const TOAST_EXIT_MS = 140;

const ToastContext = createContext<ToastApi | null>(null);

function useToastInternal(setMessage: (m: ToastMessage | null) => void): ToastApi {
  const show = useCallback(
    (text: string, variant: ToastVariant = 'info') => {
      setMessage({ text, variant });
    },
    [setMessage],
  );
  return {
    show,
    success: useCallback((text: string) => show(text, 'success'), [show]),
    error: useCallback((text: string) => show(text, 'error'), [show]),
    info: useCallback((text: string) => show(text, 'info'), [show]),
  };
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [message, setMessage] = useState<ToastMessage | null>(null);
  const [exiting, setExiting] = useState(false);
  const exitTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const removeTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const api = useToastInternal(setMessage);

  useEffect(() => {
    if (!message) return;
    setExiting(false);
    exitTimer.current = setTimeout(() => setExiting(true), TOAST_DURATION_MS);
    removeTimer.current = setTimeout(
      () => setMessage(null),
      TOAST_DURATION_MS + TOAST_EXIT_MS,
    );
    return () => {
      if (exitTimer.current) clearTimeout(exitTimer.current);
      if (removeTimer.current) clearTimeout(removeTimer.current);
    };
  }, [message]);

  return (
    <ToastContext.Provider value={api}>
      {children}
      {message ? (
        <div className="th-toast-viewport" role="status" aria-live="polite">
          <div
            className={`th-toast th-toast--${message.variant}${
              exiting ? ' th-toast--exiting' : ''
            }`}
          >
            <span className="th-toast__icon" aria-hidden="true">
              {message.variant === 'success' ? (
                <IconCheck size={16} />
              ) : message.variant === 'error' ? (
                <IconClose size={16} />
              ) : (
                <IconInfo size={16} />
              )}
            </span>
            <span>{message.text}</span>
          </div>
        </div>
      ) : null}
    </ToastContext.Provider>
  );
}
export function useToast(): ToastApi {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    // Screens outside the shell host (onboarding/error gates) may not have a
    // provider; degrade to a no-op so callers never crash.
    return {
      show: () => undefined,
      success: () => undefined,
      error: () => undefined,
      info: () => undefined,
    };
  }
  return ctx;
}
