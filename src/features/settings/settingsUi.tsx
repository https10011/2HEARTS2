/**
 * Shared building blocks for the Settings screens (Stage 15 enhanced).
 *
 * Composes primitives (Header, IconButton, Switch, etc.) into the
 * repeating settings patterns. Stage 15 adds enhanced visual treatment
 * while preserving the same API surface.
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

/** One grouped settings row — Stage 15 enhanced version. */
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
  const className = [
    'th-settings-row--stage15',
    danger ? 'th-settings-row--danger' : '',
    isStatic ? 'th-settings-row--stage15--static' : '',
  ].filter(Boolean).join(' ');
  const inner = (
    <>
      {icon ? <span className="th-settings-row--stage15__icon">{icon}</span> : null}
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

/** Settings row whose trailing control is a Switch — Stage 15 enhanced. */
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
    <div className="th-settings-row--stage15 th-settings-row--stage15--static">
      {icon ? <span className="th-settings-row--stage15__icon">{icon}</span> : null}
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

/** Soft informational card — Stage 15 enhanced version. */
export function InfoCard({ title, text, icon }: { title: string; text: string; icon?: ReactNode }) {
  return (
    <div className="th-settings-info--enhanced">
      {icon && <div className="th-settings-info--enhanced__icon">{icon}</div>}
      <div className="th-settings-info--enhanced__body">
        <p className="th-settings-info--enhanced__title">{title}</p>
        <p className="th-settings-info--enhanced__text">{text}</p>
      </div>
    </div>
  );
}
