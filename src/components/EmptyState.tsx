import type { ReactNode } from 'react';
import { componentClassNames as cls } from '@theme/components';
import { Button } from './Button';
import './primitives.css';

interface EmptyStateProps {
  /** Short, relationship-neutral headline, e.g. "No memories yet". */
  title: string;
  /** Optional supporting copy. */
  description?: string;
  /** Optional illustration node (SVG/asset). */
  visual?: ReactNode;
  /** Optional primary action. */
  actionLabel?: string;
  onAction?: () => void;
}

export function EmptyState({
  title,
  description,
  visual,
  actionLabel,
  onAction,
}: EmptyStateProps) {
  return (
    <div className={cls.emptyState}>
      {visual ? <div aria-hidden="true">{visual}</div> : null}
      <h2 style={{ fontSize: 'var(--th-font-size-lg)', color: 'var(--th-color-text-primary)' }}>
        {title}
      </h2>
      {description ? (
        <p style={{ maxWidth: '32ch' }}>{description}</p>
      ) : null}
      {actionLabel && onAction ? (
        <Button variant="primary" onClick={onAction} style={{ marginTop: 'var(--th-space-2)' }}>
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
