import './primitives.css';

interface SwitchProps {
  checked: boolean;
  onChange: (next: boolean) => void;
  /** Accessible label when the switch stands alone. */
  label: string;
  disabled?: boolean;
}

/**
 * Switch — binary preference control (role="switch"). Used by settings
 * rows; kept primitive so settings screens stay on the component layer.
 */
export function Switch({ checked, onChange, label, disabled = false }: SwitchProps) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={label}
      disabled={disabled}
      className={`th-switch${checked ? ' th-switch--on' : ''}`}
      onClick={() => onChange(!checked)}
    >
      <span className="th-switch__thumb" aria-hidden="true" />
    </button>
  );
}
