/**
 * ConfirmDialog — shared destructive/confirmation modal (Stage 16).
 *
 * Replaces the inconsistent per-feature delete confirmation patterns with
 * a single, branded, token-driven component. Every destructive action
 * (delete, clear, reset, disable) uses this for visual consistency.
 *
 * Layout: title → description → action button (danger/primary) → cancel (ghost).
 * Bottom-sheet presentation via the existing Modal infrastructure.
 */

import { Modal } from './Modal.tsx';
import { Button } from './Button.tsx';
import './primitives.css';

export interface ConfirmDialogProps {
  /** Controls visibility. */
  open: boolean;
  /** Called when the user requests dismissal (overlay tap, Escape, cancel). */
  onClose: () => void;
  /** Accessible dialog label. */
  label: string;
  /** Dialog heading. */
  title: string;
  /** Explanation of what will happen. */
  description: string;
  /** Label for the action button. */
  actionLabel: string;
  /** Called when the user confirms the action. */
  onAction: () => void;
  /** Visual variant of the action button. 'danger' for destructive, 'primary' for non-destructive confirm. */
  actionVariant?: 'danger' | 'primary';
  /** Whether the action is currently in progress. */
  busy?: boolean;
  /** Label shown while busy. */
  busyLabel?: string;
}

/**
 * Standardized confirmation dialog.
 *
 * Button order (mobile convention):
 *   1. Action (top — most likely user intent)
 *   2. Cancel (bottom — safe escape)
 *
 * For horizontal layout on wider screens, both buttons are full-width
 * and stack vertically — consistent across all viewports.
 */
export function ConfirmDialog({
  open,
  onClose,
  label,
  title,
  description,
  actionLabel,
  onAction,
  actionVariant = 'danger',
  busy = false,
  busyLabel,
}: ConfirmDialogProps) {
  return (
    <Modal open={open} onClose={onClose} label={label}>
      <div className="th-confirm-dialog">
        <h3 className="th-confirm-dialog__title">{title}</h3>
        <p className="th-confirm-dialog__description">{description}</p>
        <div className="th-confirm-dialog__actions">
          <Button
            variant={actionVariant}
            full
            onClick={onAction}
            disabled={busy}
          >
            {busy ? (busyLabel ?? 'Working…') : actionLabel}
          </Button>
          <Button
            variant="ghost"
            full
            onClick={onClose}
            disabled={busy}
          >
            Cancel
          </Button>
        </div>
      </div>
    </Modal>
  );
}
