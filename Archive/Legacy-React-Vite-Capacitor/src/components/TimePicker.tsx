/**
 * TimePicker — branded TwoHearts time selection control (Stage 8).
 *
 * Companion to DatePicker: replaces native `<input type="time">` with a
 * warm bottom-sheet wheel picker (hour / minute / AM-PM) rendered through
 * the centralized Modal and the shared date-picker wheel vocabulary. The
 * display trigger reuses the branded `.th-date-trigger` look with a clock
 * glyph — never generic browser chrome.
 *
 * Value convention matches the domain model: "HH:mm" (24-hour local wall
 * clock). Fully accessible, dark-mode compatible, reduced-motion safe.
 */

import { useState } from 'react';
import { Modal } from './Modal.tsx';
import { Button } from './Button.tsx';
import { IconClock } from './Icon.tsx';
import './primitives.css';

function pad(n: number): string {
  return n < 10 ? `0${n}` : `${n}`;
}

/** Parse "HH:mm" into 12-hour parts. Returns null on failure. */
function parseTime(value: string | null | undefined): {
  hour12: number;
  minute: number;
  period: 'AM' | 'PM';
} | null {
  if (!value) return null;
  const match = /^(\d{2}):(\d{2})$/.exec(value);
  if (!match) return null;
  const hours = Number(match[1]);
  const minute = Number(match[2]);
  if (hours > 23 || minute > 59) return null;
  return {
    hour12: hours % 12 === 0 ? 12 : hours % 12,
    minute,
    period: hours < 12 ? 'AM' : 'PM',
  };
}

/** Friendly display: "9:00 AM". */
function formatTimeDisplay(value: string | null | undefined): string {
  const parsed = parseTime(value);
  if (!parsed) return '';
  return `${parsed.hour12}:${pad(parsed.minute)} ${parsed.period}`;
}

export interface TimePickerProps {
  /** "HH:mm" (24h) or empty/null. */
  value: string | null;
  /** Called with "HH:mm" when user confirms. */
  onChange: (time: string) => void;
  /** Accessible label for the trigger button. */
  label?: string;
  /** Placeholder when empty. */
  placeholder?: string;
  /** Disabled state. */
  disabled?: boolean;
}

const HOURS_12 = [12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];
const MINUTES = Array.from({ length: 60 }, (_, i) => i);
const PERIODS = ['AM', 'PM'] as const;

export function TimePicker({
  value,
  onChange,
  label = 'Select time',
  placeholder = 'Tap to choose a time',
  disabled = false,
}: TimePickerProps) {
  const [open, setOpen] = useState(false);

  const parsed = parseTime(value);
  const now = new Date();
  const defaultHour24 = now.getHours();

  const [selHour, setSelHour] = useState(parsed?.hour12 ?? (defaultHour24 % 12 === 0 ? 12 : defaultHour24 % 12));
  const [selMinute, setSelMinute] = useState(parsed?.minute ?? 0);
  const [selPeriod, setSelPeriod] = useState<'AM' | 'PM'>(parsed?.period ?? (defaultHour24 < 12 ? 'AM' : 'PM'));

  const handleOpen = () => {
    if (disabled) return;
    const current = parseTime(value);
    const h24 = new Date().getHours();
    setSelHour(current?.hour12 ?? (h24 % 12 === 0 ? 12 : h24 % 12));
    setSelMinute(current?.minute ?? 0);
    setSelPeriod(current?.period ?? (h24 < 12 ? 'AM' : 'PM'));
    setOpen(true);
  };

  const handleConfirm = () => {
    const hour24 = selPeriod === 'AM' ? selHour % 12 : (selHour % 12) + 12;
    onChange(`${pad(hour24)}:${pad(selMinute)}`);
    setOpen(false);
  };

  const displayText = formatTimeDisplay(value);

  return (
    <>
      {/* Trigger — same branded field treatment as DatePicker */}
      <button
        type="button"
        className={`th-date-trigger ${value ? 'th-date-trigger--filled' : ''}`}
        onClick={handleOpen}
        disabled={disabled}
        aria-label={label}
        aria-haspopup="dialog"
      >
        {displayText ? (
          <span className="th-date-trigger__text">{displayText}</span>
        ) : (
          <span className="th-date-trigger__placeholder">{placeholder}</span>
        )}
        <span className="th-date-trigger__icon" aria-hidden="true">
          <IconClock size={18} />
        </span>
      </button>

      {/* Modal wheel picker */}
      <Modal open={open} onClose={() => setOpen(false)} label={label}>
        <div className="th-date-picker">
          <h3 className="th-date-picker__title">{label}</h3>

          <div className="th-date-picker__columns th-time-picker__columns">
            {/* Hour column */}
            <div className="th-date-picker__column">
              <span className="th-date-picker__column-label">Hour</span>
              <div className="th-date-picker__wheel" role="listbox" aria-label="Hour">
                {HOURS_12.map((h) => {
                  const active = h === selHour;
                  return (
                    <button
                      key={h}
                      type="button"
                      role="option"
                      aria-selected={active}
                      className={`th-date-picker__item th-date-picker__item--day ${active ? 'th-date-picker__item--active' : ''}`}
                      onClick={() => setSelHour(h)}
                    >
                      {h}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Minute column */}
            <div className="th-date-picker__column">
              <span className="th-date-picker__column-label">Minute</span>
              <div className="th-date-picker__wheel" role="listbox" aria-label="Minute">
                {MINUTES.map((m) => {
                  const active = m === selMinute;
                  return (
                    <button
                      key={m}
                      type="button"
                      role="option"
                      aria-selected={active}
                      className={`th-date-picker__item th-date-picker__item--day ${active ? 'th-date-picker__item--active' : ''}`}
                      onClick={() => setSelMinute(m)}
                    >
                      {pad(m)}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* AM / PM column */}
            <div className="th-date-picker__column">
              <span className="th-date-picker__column-label">&nbsp;</span>
              <div className="th-date-picker__wheel" role="listbox" aria-label="AM or PM">
                {PERIODS.map((p) => {
                  const active = p === selPeriod;
                  return (
                    <button
                      key={p}
                      type="button"
                      role="option"
                      aria-selected={active}
                      className={`th-date-picker__item ${active ? 'th-date-picker__item--active' : ''}`}
                      onClick={() => setSelPeriod(p)}
                    >
                      {p}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Preview of selected time */}
          <p className="th-date-picker__preview">
            {formatTimeDisplay(`${pad(selPeriod === 'AM' ? selHour % 12 : (selHour % 12) + 12)}:${pad(selMinute)}`)}
          </p>

          <div className="th-date-picker__actions">
            <Button variant="primary" full onClick={handleConfirm}>
              Confirm
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}
