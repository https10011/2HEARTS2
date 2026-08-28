/**
 * StatusBanner — consistent inline error / success / info messages (Stage 16).
 *
 * Replaces the inconsistent inline `<p style={{ color: ... }}>` patterns
 * with a single, token-driven, accessible component. Each variant uses
 * existing design tokens and keeps the same semantic roles.
 *
 * Variants:
 *   - error: red background tint, error icon, role="alert"
 *   - success: green background tint, check icon, role="status"
 *   - info: neutral background tint, info icon, role="status"
 */

import { IconCheck, IconClose, IconInfo } from './Icon.tsx';
import './primitives.css';

export type StatusVariant = 'error' | 'success' | 'info';

export interface StatusBannerProps {
  /** Visual variant. */
  variant: StatusVariant;
  /** The message text. */
  children: string;
}

const variantConfig: Record<
  StatusVariant,
  { className: string; Icon: typeof IconCheck }
> = {
  error: { className: 'th-status-banner--error', Icon: IconClose },
  success: { className: 'th-status-banner--success', Icon: IconCheck },
  info: { className: 'th-status-banner--info', Icon: IconInfo },
};

export function StatusBanner({ variant, children }: StatusBannerProps) {
  const { className, Icon } = variantConfig[variant];
  const role = variant === 'error' ? 'alert' : 'status';

  return (
    <div className={`th-status-banner ${className}`} role={role}>
      <span className="th-status-banner__icon" aria-hidden="true">
        <Icon size={14} />
      </span>
      <span className="th-status-banner__text">{children}</span>
    </div>
  );
}
