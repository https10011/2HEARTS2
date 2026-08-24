/**
 * Timeline storytelling helpers (Stage 7).
 *
 * Pure presentation logic for turning the flat chronological event list
 * into a story-shaped render model: year anchors when the story spans
 * multiple years, 1-based chapter numbers, and the "latest moment"
 * emphasis. Kept pure so Node tests can cover the composition rules.
 */

export interface StoryEventLike {
  id: string;
  eventDate: string;
}

export type StoryRow<T extends StoryEventLike> =
  | { type: 'year'; year: string; key: string }
  | { type: 'event'; event: T; chapter: number; isLatest: boolean; key: string };

/** Formats a yyyy-mm-dd calendar key as "August 8, 2025" (UTC-safe). */
export function formatEventDate(dateKey: string): string {
  const [year, month, day] = dateKey.split('-').map(Number);
  if (!year || !month || !day) return dateKey;
  const date = new Date(Date.UTC(year, month - 1, day));
  return date.toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    timeZone: 'UTC',
  });
}

/**
 * Builds the render model for the timeline list.
 *
 * Events arrive in chronological (ascending) order from the service.
 * Chapter numbers follow that order (Chapter 1 = the first moment ever).
 * Year anchor rows appear before each year group only when the story
 * spans more than one calendar year. The final event is flagged as the
 * latest moment for the emphasized card treatment.
 */
export function buildStoryRows<T extends StoryEventLike>(events: T[]): StoryRow<T>[] {
  const years = new Set(events.map((e) => e.eventDate.slice(0, 4)));
  const showYearAnchors = years.size > 1;
  const rows: StoryRow<T>[] = [];
  let lastYear = '';

  events.forEach((event, index) => {
    const year = event.eventDate.slice(0, 4);
    if (showYearAnchors && year !== lastYear) {
      rows.push({ type: 'year', year, key: `year-${year}` });
    }
    lastYear = year;
    rows.push({
      type: 'event',
      event,
      chapter: index + 1,
      isLatest: index === events.length - 1,
      key: event.id,
    });
  });

  return rows;
}

/** 1-based chapter number for an event id, or null when not found. */
export function chapterOf<T extends StoryEventLike>(events: T[], id: string): number | null {
  const index = events.findIndex((e) => e.id === id);
  return index === -1 ? null : index + 1;
}
