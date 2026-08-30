import { componentClassNames as cls } from '@theme/components';
import './primitives.css';

interface LoadingStateProps {
  label?: string;
}

/**
 * Uses the single `.th-spinner` primitive (one `th-spin` keyframe app-wide);
 * the caption is visible (not only screen-reader) so reduced-motion users —
 * whose spinner freezes — still see status.
 */
export function LoadingState({ label = 'Loading…' }: LoadingStateProps) {
  return (
    <div className={cls.loadingState} role="status" aria-live="polite">
      <span className="th-spinner" aria-hidden="true" />
      <p className="th-loading-state__label">{label}</p>
    </div>
  );
}
