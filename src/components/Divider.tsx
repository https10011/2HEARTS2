import { componentClassNames as cls } from '@theme/components';
import './primitives.css';

export function Divider({ className }: { className?: string }) {
  const classes = [cls.divider, className ?? ''].filter(Boolean).join(' ');
  return <hr className={classes} />;
}
