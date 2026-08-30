/**
 * Place presentation helpers (Stage 9).
 *
 * Pure, Node-testable derivation for the Places experience: location lines,
 * category vocabulary, in-memory filtering, newest-first ordering, and the
 * warm "Added … ago" phrasing used on place cards. No persistence, no React.
 */

import type { Place } from '../../data/place/placeTypes.ts';

/** "Quezon City" / "Batangas, PH" — joins the populated location parts. */
export function formatLocationLine(
  place: Pick<Place, 'city' | 'state' | 'country'>,
): string {
  return [place.city, place.state, place.country].filter(Boolean).join(', ');
}

/** Distinct categories present in the list, alphabetized (filter chips). */
export function collectCategories(places: readonly Place[]): string[] {
  const seen = new Set<string>();
  for (const place of places) {
    const category = place.category?.trim();
    if (category) seen.add(category);
  }
  return [...seen].sort((a, b) => a.localeCompare(b));
}

export interface PlaceFilter {
  /** Active category chip; null/empty means all. */
  category?: string | null;
  /** Free-text query matched against name/location/notes (case-insensitive). */
  query?: string;
}

/** In-memory category + text filtering (place lists are small and local). */
export function filterPlaces(
  places: readonly Place[],
  filter: PlaceFilter,
): Place[] {
  const category = filter.category?.trim() ?? '';
  const query = filter.query?.trim().toLowerCase() ?? '';
  return places.filter((place) => {
    if (category && place.category !== category) return false;
    if (!query) return true;
    const haystack = [
      place.name,
      place.address,
      place.city,
      place.state,
      place.country,
      place.notes,
      place.category,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();
    return haystack.includes(query);
  });
}

/** Newest first by createdAt (hero = most recently saved place). */
export function byNewestFirst(places: readonly Place[]): Place[] {
  return [...places].sort((a, b) => b.createdAt.localeCompare(a.createdAt));
}

const MINUTE_MS = 60_000;
const HOUR_MS = 60 * MINUTE_MS;
const DAY_MS = 24 * HOUR_MS;

/**
 * Warm relative phrasing for place cards: "Added just now", "Added today",
 * "Added yesterday", "Added 5 days ago", "Added 3 weeks ago",
 * "Added 8 months ago", "Added 1 year ago". Empty string for invalid input.
 */
export function formatAddedAgo(iso: string, now: Date = new Date()): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '';
  const diffMs = now.getTime() - date.getTime();
  if (diffMs < HOUR_MS) return 'Added just now';
  if (diffMs < DAY_MS) return 'Added today';
  const days = Math.floor(diffMs / DAY_MS);
  if (days === 1) return 'Added yesterday';
  if (days < 7) return `Added ${days} days ago`;
  const weeks = Math.floor(days / 7);
  if (days < 30) return weeks === 1 ? 'Added 1 week ago' : `Added ${weeks} weeks ago`;
  const months = Math.floor(days / 30);
  if (months < 12) return months === 1 ? 'Added 1 month ago' : `Added ${months} months ago`;
  const years = Math.floor(days / 365);
  return years === 1 ? 'Added 1 year ago' : `Added ${years} years ago`;
}

/** "August 12, 2025" — quiet metadata on the detail screen. */
export function formatPlaceDate(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleDateString('en-US', {
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  });
}
