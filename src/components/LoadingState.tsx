import { componentClassNames as cls } from '@theme/components';
import './primitives.css';

interface LoadingStateProps {
  label?: string;
}

export function LoadingState({ label = 'Loading…' }: LoadingStateProps) {
  return (
    <div className={cls.loadingState} role="status" aria-live="polite">
      <span className="th-sr-only">{label}</span>
      <span
        aria-hidden="true"
        style={{
          width: '28px',
          height: '28px',
          borderRadius: '50%',
          border: '3px solid var(--th-color-blush)',
          borderTopColor: 'var(--th-color-burgundy)',
          animation: `th-spin 0.8s linear infinite`,
        }}
      />
      <style>{`@keyframes th-spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}
