import type { HTMLAttributes, ReactNode } from 'react';
import { componentClassNames as cls } from '@theme/components';
import './primitives.css';

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode;
}

export function Card({ className, children, ...rest }: CardProps) {
  const classes = [cls.card, className ?? ''].filter(Boolean).join(' ');
  return (
    <div className={classes} {...rest}>
      {children}
    </div>
  );
}
