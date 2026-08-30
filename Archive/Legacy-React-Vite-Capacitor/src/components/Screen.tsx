import type { ReactNode } from 'react';
import '../styles/global.css';

/**
 * Screen — the foundational mobile screen container.
 *
 * Encapsulates the portrait-first, safe-area-aware, keyboard-aware layout.
 * Children render inside a scrollable region. The `footer` slot stays
 * pinned above the keyboard/navigation bar — appropriate for primary
 * actions like Save / Delete (MasterPrompt §51: buttons must work).
 */
interface ScreenProps {
  children: ReactNode;
  footer?: ReactNode;
  /** When true, the body does not scroll independently (e.g. full-card layouts). */
  noScroll?: boolean;
}

export function Screen({ children, footer, noScroll = false }: ScreenProps) {
  return (
    <div className="th-screen">
      <div className={noScroll ? '' : 'th-scroll'}>{children}</div>
      {footer ? (
        <div
          style={{
            flex: '0 0 auto',
            padding: `var(--th-space-4) var(--th-space-4) calc(var(--th-space-4) + var(--th-safe-area-bottom))`,
            background: 'var(--th-color-surface)',
            borderTop: '1px solid var(--th-color-divider)',
          }}
        >
          {footer}
        </div>
      ) : null}
    </div>
  );
}
