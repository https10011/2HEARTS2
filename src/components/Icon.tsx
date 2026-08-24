import type { SVGProps } from 'react';

/**
 * TwoHearts icon set — vector icons, NOT Unicode emoji (MasterPrompt §22).
 *
 * Each icon is a small inline SVG component so the owner can replace
 * individual glyphs at src/assets/icons/*.svg and re-import, or swap the
 * whole set. Stroke-based for crisp scaling at any text-size setting.
 */

export type IconProps = SVGProps<SVGSVGElement> & { size?: number };

function base({ size = 24, ...rest }: IconProps) {
  return {
    width: size,
    height: size,
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.8,
    strokeLinecap: 'round' as const,
    strokeLinejoin: 'round' as const,
    ...rest,
  };
}

export function IconBack(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M15 18l-6-6 6-6" />
    </svg>
  );
}

export function IconClose(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M6 6l12 12M18 6L6 18" />
    </svg>
  );
}

/** Selection/state checkmark. */
export function IconCheck(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <polyline points="20 6 9 17 4 12" />
    </svg>
  );
}

/** Info (toast/status badge). */
export function IconInfo(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <circle cx="12" cy="12" r="10" />
      <path d="M12 11V16" />
      <circle cx="12" cy="8" r="0.8" fill="currentColor" stroke="none" />
    </svg>
  );
}

export function IconChevronRight(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M9 6l6 6-6 6" />
    </svg>
  );
}

export function IconHome(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M3 11l9-8 9 8" />
      <path d="M5 10v10h14V10" />
    </svg>
  );
}

export function IconSearch(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <circle cx="11" cy="11" r="6" />
      <path d="M20 20l-3.5-3.5" />
    </svg>
  );
}

export function IconMenu(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M4 7h16M4 12h16M4 17h16" />
    </svg>
  );
}

export function IconPlus(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M12 5v14M5 12h14" />
    </svg>
  );
}

export function IconHeart(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
    </svg>
  );
}

/** Mood feature icon. */
export function IconSmile(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <circle cx="12" cy="12" r="10" />
      <path d="M8 14s1.5 2 4 2 4-2 4-2" />
      <line x1="9" y1="9" x2="9.01" y2="9" />
      <line x1="15" y1="9" x2="15.01" y2="9" />
    </svg>
  );
}

export function IconImage(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <circle cx="8.5" cy="8.5" r="1.5" />
      <polyline points="21 15 16 10 5 21" />
    </svg>
  );
}

export function IconGamepad(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <rect x="2" y="6" width="20" height="12" rx="3" />
      <line x1="6" y1="10" x2="6" y2="14" />
      <line x1="4" y1="12" x2="8" y2="12" />
      <circle cx="16" cy="10" r="0.8" fill="currentColor" stroke="none" />
      <circle cx="18" cy="12" r="0.8" fill="currentColor" stroke="none" />
    </svg>
  );
}

export function IconFileText(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <polyline points="14 2 14 8 20 8" />
      <line x1="8" y1="13" x2="16" y2="13" />
      <line x1="8" y1="17" x2="13" y2="17" />
    </svg>
  );
}

/** Generic file (Vault 'file' type) — plain document variant. */
export function IconFile(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <polyline points="14 2 14 8 20 8" />
    </svg>
  );
}

export function IconTrash(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <polyline points="3 6 5 6 21 6" />
      <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
      <path d="M10 11v6" />
      <path d="M14 11v6" />
      <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
    </svg>
  );
}

export function IconEdit(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
  );
}

export function IconCalendar(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
      <line x1="16" y1="2" x2="16" y2="6" />
      <line x1="8" y1="2" x2="8" y2="6" />
      <line x1="3" y1="10" x2="21" y2="10" />
    </svg>
  );
}

export function IconMapPin(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
      <circle cx="12" cy="10" r="3" />
    </svg>
  );
}

export function IconBellOff(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      <path d="m18.63 13.39l.86 2.9a9.84 9.84 0 0 0-2.86.28l-2.29-2.57" />
      <path d="M18.86 9.45a6 6 0 0 0-6-7.45 6 6 0 0 0-6 5.45c0 2.13-.43 4.02-1.43 5.46L4.23 13.63" />
      <line x1="1" y1="1" x2="23" y2="23" />
    </svg>
  );
}

export function IconCamera(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M4 7h3l2-3h6l2 3h3v14H4V7z" />
      <circle cx="12" cy="13" r="3.5" />
    </svg>
  );
}

export function IconVideo(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <rect x="2" y="6" width="13" height="12" rx="2" />
      <polyline points="15 10 22 6 22 18 15 14" />
    </svg>
  );
}

export function IconLock(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <rect x="4" y="10" width="16" height="11" rx="2" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
    </svg>
  );
}

export function IconBell(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
      <path d="M13.73 21a2 2 0 0 1-3.46 0" />
    </svg>
  );
}

/** Clock (reminder times). */
export function IconClock(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <circle cx="12" cy="12" r="9" />
      <polyline points="12 7 12 12 15.5 14" />
    </svg>
  );
}

/** Repeat / recurrence. */
export function IconRepeat(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <polyline points="17 1 21 5 17 9" />
      <path d="M3 11V9a4 4 0 0 1 4-4h14" />
      <polyline points="7 23 3 19 7 15" />
      <path d="M21 13v2a4 4 0 0 1-4 4H3" />
    </svg>
  );
}

export function IconSettings(props: IconProps) {
  return (
    <svg {...base(props)} aria-hidden="true">
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
    </svg>
  );
}
