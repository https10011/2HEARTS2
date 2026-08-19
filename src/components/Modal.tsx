import { useEffect, type ReactNode } from 'react';
import './primitives.css';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  children: ReactNode;
  /** Accessible label for the sheet. */
  label?: string;
}

/**
 * Modal — bottom-sheet foundation (mobile-native pattern).
 * Closes on overlay tap and Android back (handled globally in AppRoot).
 * Not a full-screen route; use it for short confirmations / pickers.
 */
export function Modal({ open, onClose, children, label = 'Dialog' }: ModalProps) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="th-modal-overlay"
      role="dialog"
      aria-modal="true"
      aria-label={label}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="th-modal-sheet" onClick={(e) => e.stopPropagation()}>
        {children}
      </div>
    </div>
  );
}
