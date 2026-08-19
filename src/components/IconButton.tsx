import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { componentClassNames as cls } from '@theme/components';
import './primitives.css';

interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  label: string; // accessible label — never visual-only
  children: ReactNode;
}

export function IconButton({ label, className, children, ...rest }: IconButtonProps) {
  const classes = [cls.iconButton, className ?? ''].filter(Boolean).join(' ');
  return (
    <button type="button" aria-label={label} className={classes} {...rest}>
      {children}
    </button>
  );
}
