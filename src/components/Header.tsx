import type { ReactNode } from 'react';
import { componentClassNames as cls } from '@theme/components';
import './primitives.css';

interface HeaderProps {
  title: string;
  /** Leading slot — typically a back IconButton. */
  left?: ReactNode;
  /** Trailing slot — typically a contextual IconButton. */
  right?: ReactNode;
}

export function Header({ title, left, right }: HeaderProps) {
  return (
    <header className={cls.header} role="banner">
      <div style={{ display: 'flex', alignItems: 'center', minWidth: 'var(--th-touch-target-min)' }}>
        {left}
      </div>
      <h1 className="th-header__title" style={{ flex: 1, textAlign: 'center' }}>
        {title}
      </h1>
      <div style={{ display: 'flex', alignItems: 'center', minWidth: 'var(--th-touch-target-min)', justifyContent: 'flex-end' }}>
        {right}
      </div>
    </header>
  );
}
