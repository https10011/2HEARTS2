/**
 * Home highlights (Stage 3) — PURE view-model builders for the Home
 * "From your story" section.
 *
 * Home shows a short, calm preview of the couple's living content:
 * the latest note, the next upcoming reminder, and the most recent
 * memory. Each preview deep-links to the EXISTING detail screen for
 * that item — never to a relationship archive root (the Us hub owns
 * the archives; Phase 24 navigation contract stays intact).
 *
 * Vault content is never queried here (MasterPrompt §46: private vault
 * must not surface in home previews).
 *
 * This module is intentionally free of React, storage, and service
 * imports so Node tests can exercise the builders directly. All routes
 * are derived from the authoritative RoutePath map.
 */

import { RoutePath } from '../../navigation/routes.ts';
import type { NavIconKey } from './navConfig.ts';
import type { NoteView } from '../../services/note/noteService.ts';
import type { Reminder } from '../../data/reminder/reminderTypes.ts';
import type { MemoryWithMedia } from '../../services/memory/memoryService.ts';

export type HomeHighlightKind = 'note' | 'reminder' | 'memory';

export interface HomeHighlight {
  /** Stable id of the underlying entity (test + key support). */
  id: string;
  kind: HomeHighlightKind;
  /** Centralized icon vocabulary key (resolved via navIcons.tsx). */
  icon: NavIconKey;
  /** Small section label, e.g. "Latest note". */
  label: string;
  /** Primary line — the item's title. */
  title: string;
  /** Secondary line — excerpt or friendly date/time. */
  meta: string;
  /** Absolute route to the item's existing detail screen. */
  to: string;
}

/** Time-of-day greeting for the Home header. */
export function greetingForHour(hour: number): string {
  if (hour < 5) return 'Good night';
  if (hour < 12) return 'Good morning';
  if (hour < 17) return 'Good afternoon';
  if (hour < 22) return 'Good evening';
  return 'Good night';
}

/** Friendly 'yyyy-mm-dd' → "May 12" style date (local calendar key). */
export function formatLocalDateKey(dateKey: string): string {
  const [year, month, day] = dateKey.split('-').map(Number);
  if (!year || !month || !day) return dateKey;
  const d = new Date(Date.UTC(year, month - 1, day));
  return d.toLocaleDateString(undefined, { month: 'long', day: 'numeric' });
}

/** ISO timestamp → short time such as "7:48 AM". */
export function formatTimeOfDay(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return d.toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' });
}

export function buildNoteHighlight(note: NoteView): HomeHighlight {
  const excerpt = note.excerpt?.trim();
  return {
    id: note.id,
    kind: 'note',
    icon: 'file-text',
    label: 'Latest note',
    title: note.title,
    meta: excerpt || formatLocalDateKey(note.updatedAt.slice(0, 10)),
    to: RoutePath.appNotesDetail.replace(':noteId', note.id),
  };
}

export function buildReminderHighlight(reminder: Reminder): HomeHighlight {
  const date = formatLocalDateKey(reminder.scheduledDate);
  return {
    id: reminder.id,
    kind: 'reminder',
    icon: 'bell',
    label: 'Upcoming',
    title: reminder.title,
    meta: `${date} · ${reminder.scheduledTime}`,
    to: RoutePath.appRemindersDetail.replace(':reminderId', reminder.id),
  };
}

export function buildMemoryHighlight(memory: MemoryWithMedia): HomeHighlight {
  const dateKey = memory.memoryDate ?? memory.createdAt.slice(0, 10);
  return {
    id: memory.id,
    kind: 'memory',
    icon: 'camera',
    label: 'Recent memory',
    title: memory.title,
    meta: formatLocalDateKey(dateKey),
    to: RoutePath.appMemoriesDetail.replace(':memoryId', memory.id),
  };
}

/**
 * Picks the most relevant item from each source (already list-ordered by
 * the repositories: notes updated_at DESC, upcoming reminders soonest
 * first). For memories the newest createdAt wins regardless of the
 * manual sort order. Missing sources are simply skipped — the Home
 * composition stays stable with 0–3 highlights.
 */
export function selectHomeHighlights(input: {
  notes?: NoteView[];
  reminders?: Reminder[];
  memories?: MemoryWithMedia[];
}): HomeHighlight[] {
  const highlights: HomeHighlight[] = [];

  const memory = input.memories
    ?.slice()
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))[0];
  if (memory) highlights.push(buildMemoryHighlight(memory));

  const note = input.notes?.[0];
  if (note) highlights.push(buildNoteHighlight(note));

  const reminder = input.reminders?.[0];
  if (reminder) highlights.push(buildReminderHighlight(reminder));

  return highlights;
}
