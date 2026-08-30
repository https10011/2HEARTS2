/**
 * DatePicker — branded TwoHearts date selection control (Stage 2).
 *
 * Replaces native `<input type="date">` with a warm, modal-based picker:
 * month / day / year columns rendered as selectable chips in a bottom sheet.
 * The display trigger is a styled branded button that shows the formatted
 * date or a placeholder — never generic browser chrome.
 *
 * Fully accessible (keyboard nav, ARIA roles, screen-reader labels),
 * dark-mode compatible, reduced-motion safe, and responsive.
 */

import { useState, useCallback, useMemo } from 'react';
import { Modal } from './Modal.tsx';
import { Button } from './Button.tsx';
import './primitives.css';

/* ------------------------------------------------------------------ */
/*  Helpers                                                           */
/* ------------------------------------------------------------------ */

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

/** How many days exist in a given month (1-indexed month). */
function daysInMonth(year: number, month: number): number {
  return new Date(year, month, 0).getDate();
}

function pad(n: number): string {
  return n < 10 ? `0${n}` : `${n}`;
}

/** Build ISO date string from components. */
function toISODate(year: number, month: number, day: number): string {
  return `${year}-${pad(month)}-${pad(day)}`;
}

/** Parse an ISO date string back to components. Returns null on failure. */
function parseISODate(iso: string | undefined | null): {
  year: number;
  month: number;
  day: number;
} | null {
  if (!iso || !/^\d{4}-\d{2}-\d{2}$/.test(iso)) return null;
  const [y, m, d] = iso.split('-').map(Number);
  const date = new Date(y, m - 1, d);
  if (
    date.getFullYear() !== y ||
    date.getMonth() !== m - 1 ||
    date.getDate() !== d
  )
    return null;
  return { year: y, month: m, day: d };
}

/** Friendly display: "June 14, 2022". */
function formatDateDisplay(iso: string | null | undefined): string {
  const parsed = parseISODate(iso);
  if (!parsed) return '';
  return `${MONTH_NAMES[parsed.month - 1]} ${parsed.day}, ${parsed.year}`;
}

/* ------------------------------------------------------------------ */
/*  Props                                                             */
/* ------------------------------------------------------------------ */

export interface DatePickerProps {
  /** ISO date string (yyyy-mm-dd) or empty/null. */
  value: string | null;
  /** Called with ISO string when user confirms. */
  onChange: (isoDate: string) => void;
  /** Accessible label for the trigger button. */
  label?: string;
  /** Placeholder when empty. */
  placeholder?: string;
  /** Disabled state. */
  disabled?: boolean;
  /** Error state — adds visual ring. */
  error?: boolean;
  /** Accessible error description. */
  'aria-describedby'?: string;
  /** Minimum selectable year. Default: 1950. */
  minYear?: number;
  /** Maximum selectable year. Default: current year. */
  maxYear?: number;
}

/* ------------------------------------------------------------------ */
/*  Component                                                         */
/* ------------------------------------------------------------------ */

export function DatePicker({
  value,
  onChange,
  label = 'Select date',
  placeholder = 'Tap to choose a date',
  disabled = false,
  error = false,
  'aria-describedby': ariaDescribedBy,
  minYear = 1950,
  maxYear = new Date().getFullYear(),
}: DatePickerProps) {
  const [open, setOpen] = useState(false);

  /* Determine current or default selections. */
  const parsed = parseISODate(value);
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;
  const currentDay = new Date().getDate();

  const [selMonth, setSelMonth] = useState(parsed?.month ?? currentMonth);
  const [selDay, setSelDay] = useState(parsed?.day ?? currentDay);
  const [selYear, setSelYear] = useState(parsed?.year ?? currentYear);

  /* Valid year range. */
  const years = useMemo(() => {
    const arr: number[] = [];
    for (let y = maxYear; y >= minYear; y--) arr.push(y);
    return arr;
  }, [minYear, maxYear]);

  /* Days available for the selected month/year. */
  const maxDay = daysInMonth(selYear, selMonth);
  const days = useMemo(() => {
    const arr: number[] = [];
    for (let d = 1; d <= maxDay; d++) arr.push(d);
    return arr;
  }, [maxDay]);

  /* Clamp day when month/year changes. */
  const clampDay = useCallback(
    (d: number, y: number, m: number) => {
      const max = daysInMonth(y, m);
      return d > max ? max : d;
    },
    [],
  );

  const handleOpen = () => {
    if (disabled) return;
    /* Reset picker to current value (or today). */
    setSelMonth(parsed?.month ?? currentMonth);
    setSelDay(parsed?.day ?? currentDay);
    setSelYear(parsed?.year ?? currentYear);
    setOpen(true);
  };

  const handleConfirm = () => {
    onChange(toISODate(selYear, selMonth, selDay));
    setOpen(false);
  };

  const handleClear = () => {
    onChange('');
    setOpen(false);
  };

  const displayText = formatDateDisplay(value);

  return (
    <>
      {/* Trigger — branded display button (never native chrome) */}
      <button
        type="button"
        className={`th-date-trigger ${value ? 'th-date-trigger--filled' : ''} ${error ? 'th-date-trigger--error' : ''}`}
        onClick={handleOpen}
        disabled={disabled}
        aria-label={label}
        aria-describedby={ariaDescribedBy}
        aria-haspopup="dialog"
      >
        {displayText ? (
          <span className="th-date-trigger__text">{displayText}</span>
        ) : (
          <span className="th-date-trigger__placeholder">{placeholder}</span>
        )}
        <span className="th-date-trigger__icon" aria-hidden="true">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
          </svg>
        </span>
      </button>

      {/* Modal picker */}
      <Modal open={open} onClose={() => setOpen(false)} label={label}>
        <div className="th-date-picker">
          <h3 className="th-date-picker__title">{label}</h3>

          <div className="th-date-picker__columns">
            {/* Month column */}
            <div className="th-date-picker__column">
              <span className="th-date-picker__column-label">Month</span>
              <div className="th-date-picker__wheel" role="listbox" aria-label="Month">
                {MONTH_NAMES.map((name, i) => {
                  const monthNum = i + 1;
                  const active = monthNum === selMonth;
                  return (
                    <button
                      key={monthNum}
                      type="button"
                      role="option"
                      aria-selected={active}
                      className={`th-date-picker__item ${active ? 'th-date-picker__item--active' : ''}`}
                      onClick={() => {
                        setSelMonth(monthNum);
                        setSelDay((d) => clampDay(d, selYear, monthNum));
                      }}
                    >
                      {name.slice(0, 3)}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Day column */}
            <div className="th-date-picker__column">
              <span className="th-date-picker__column-label">Day</span>
              <div className="th-date-picker__wheel" role="listbox" aria-label="Day">
                {days.map((d) => {
                  const active = d === selDay;
                  return (
                    <button
                      key={d}
                      type="button"
                      role="option"
                      aria-selected={active}
                      className={`th-date-picker__item th-date-picker__item--day ${active ? 'th-date-picker__item--active' : ''}`}
                      onClick={() => setSelDay(d)}
                    >
                      {d}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Year column */}
            <div className="th-date-picker__column">
              <span className="th-date-picker__column-label">Year</span>
              <div className="th-date-picker__wheel" role="listbox" aria-label="Year">
                {years.map((y) => {
                  const active = y === selYear;
                  return (
                    <button
                      key={y}
                      type="button"
                      role="option"
                      aria-selected={active}
                      className={`th-date-picker__item th-date-picker__item--year ${active ? 'th-date-picker__item--active' : ''}`}
                      onClick={() => {
                        setSelYear(y);
                        setSelDay((d) => clampDay(d, y, selMonth));
                      }}
                    >
                      {y}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Preview of selected date */}
          <p className="th-date-picker__preview">
            {formatDateDisplay(toISODate(selYear, selMonth, selDay))}
          </p>

          <div className="th-date-picker__actions">
            <Button variant="primary" full onClick={handleConfirm}>
              Confirm
            </Button>
            {value && (
              <Button variant="ghost" full onClick={handleClear}>
                Clear date
              </Button>
            )}
          </div>
        </div>
      </Modal>
    </>
  );
}
