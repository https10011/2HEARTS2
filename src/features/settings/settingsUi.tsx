/**
 * Shared building blocks for the Settings screens (Phase 19).
 *
 * Feature-local helpers only — primitives (Header, IconButton, Switch…)
 * stay in src/components; these compose them into the repeating settings
 * row/group patterns so each screen stays declarative.
 */

import type { ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Header, IconButton, Switch, IconBack, IconChevronRight } from '../../components/index.ts';

/** Screen scaffold: back header + padded content column. */
export function SettingsScreen({
  title,
  backTo,
  children,
}: {
  title: string;
  backTo: string;
  children: ReactNode;
}) {
  const navigate = useNavigate();
  return (
    <div>
      <Header
        title={title}
        left={
          <IconButton label="Back" onClick={() => navigate(backTo)}>
            <IconBack size={20} />
          </IconButton>
        }
      />
      <div className="th-content-pad">{children}</div>
    </div>
  );
}

interface SettingRowProps {
  icon?: ReactNode;
  label: string;
  description?: string;
  /** Navigation target — renders a link row with chevron. */
  to?: string;
  /** Action — renders a button row. */
  onClick?: () => void;
  /** Trailing content (switch, value, custom). */
  trailing?: ReactNode;
  danger?: boolean;
  /** Non-interactive display row. */
  static?: boolean;
}

/** One grouped settings row. */
export function SettingRow({
  icon,
  label,
  description,
  to,
  onClick,
  trailing,
  danger = false,
  static: isStatic = false,
}: SettingRowProps) {
  const className = `th-settings-row${danger ? ' th-settings-row--danger' : ''}${isStatic ? ' th-settings-row--static' : ''}`;
  const inner = (
    <>
      {icon ? <span className="th-settings-row__icon">{icon}</span> : null}
      <span className="th-settings-row__body">
        <span className="th-settings-row__label">{label}</span>
        {description ? <span className="th-settings-row__description">{description}</span> : null}
      </span>
      <span className="th-settings-row__trailing">
        {trailing}
        {to ? <IconChevronRight size={18} /> : null}
      </span>
    </>
  );

  if (to) {
    return (
      <Link to={to} className={className}>
        {inner}
      </Link>
    );
  }
  if (isStatic) {
    return <div className={className}>{inner}</div>;
  }
  return (
    <button type="button" className={className} onClick={onClick}>
      {inner}
    </button>
  );
}

/** Settings row whose trailing control is a Switch. */
export function SettingSwitchRow({
  icon,
  label,
  description,
  checked,
  onChange,
  disabled = false,
}: {
  icon?: ReactNode;
  label: string;
  description?: string;
  checked: boolean;
  onChange: (next: boolean) => void;
  disabled?: boolean;
}) {
  return (
    <div className="th-settings-row th-settings-row--static">
      {icon ? <span className="th-settings-row__icon">{icon}</span> : null}
      <span className="th-settings-row__body">
        <span className="th-settings-row__label">{label}</span>
        {description ? <span className="th-settings-row__description">{description}</span> : null}
      </span>
      <span className="th-settings-row__trailing">
        <Switch checked={checked} onChange={onChange} label={label} disabled={disabled} />
      </span>
    </div>
  );
}

/** Soft informational card used for privacy/storage notes. */
export function InfoCard({ title, text }: { title: string; text: string }) {
  return (
    <div className="th-settings-info">
      <div>
        <p className="th-settings-info__title">{title}</p>
        <p className="th-settings-info__text">{text}</p>
      </div>
    </div>
  );
}
