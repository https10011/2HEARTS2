import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { componentClassNames as cls } from '@theme/components';
import './primitives.css';

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  full?: boolean;
  children: ReactNode;
}

const variantClass: Record<Variant, string> = {
  primary: cls.buttonPrimary,
  secondary: cls.buttonSecondary,
  ghost: cls.buttonGhost,
  danger: cls.buttonDanger,
};

export function Button({
  variant = 'primary',
  full = false,
  className,
  children,
  ...rest
}: ButtonProps) {
  const classes = [
    cls.button,
    variantClass[variant],
    full ? cls.buttonFull : '',
    className ?? '',
  ]
    .filter(Boolean)
    .join(' ');
  return (
    <button className={classes} {...rest}>
      {children}
    </button>
  );
}
