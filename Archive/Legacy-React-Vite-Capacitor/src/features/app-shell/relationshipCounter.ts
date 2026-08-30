/**
 * relationshipCounter — pure, Node-test-safe helpers for the dedicated
 * Relationship Counter screen (Stage 4).
 *
 * Works exclusively on local `yyyy-mm-dd` date keys and whole-day counts so
 * the rendered figures exactly match the summary semantics from
 * RelationshipService (DST-safe, no floating hours).
 */

/** "September 24, 2023" from a local yyyy-mm-dd key (UTC noon-safe). */
export function formatDateKeyLong(dateKey: string): string {
  const [year, month, day] = dateKey.split('-').map(Number);
  const d = new Date(Date.UTC(year, month - 1, day));
  return d.toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

/** Days → {hours, minutes} for the stats strip (whole local days only). */
export function hoursMinutesFromDays(days: number): { hours: number; minutes: number } {
  const hours = days * 24;
  return { hours, minutes: hours * 60 };
}

/** Compose the human sentence from a decomposed age. */
export function decomposedSentence(
  age: { years: number; months: number; days: number } | null,
): string {
  if (!age) return '';
  const parts: string[] = [];
  if (age.years > 0) parts.push(`${age.years} ${age.years === 1 ? 'year' : 'years'}`);
  if (age.months > 0) parts.push(`${age.months} ${age.months === 1 ? 'month' : 'months'}`);
  if (parts.length === 0 || age.days > 0) parts.push(`${age.days} ${age.days === 1 ? 'day' : 'days'}`);
  return parts.join(', ');
}

export interface NextMilestone {
  /** Total days at the milestone (e.g. 100, 200, 1_825 for 5 years). */
  days: number;
  /** Days remaining until it (≥ 1 when today isn't the milestone). */
  daysToGo: number;
  /** `day` = round hundred-day mark, `anniversary` = round-year mark. */
  kind: 'day' | 'anniversary';
  /** Progress 0..1 of the journey toward this milestone. */
  progress: number;
}

/**
 * Next meaningful milestone after `ageDays`: the nearest of the next
 * hundred-day mark and the next round anniversary (1y, 2y, …).
 * Exactly at a milestone → daysToGo 0 and progress 1.
 */
export function nextMilestone(ageDays: number): NextMilestone | null {
  if (!Number.isFinite(ageDays) || ageDays < 0) return null;

  const nextHundred = Math.floor(ageDays / 100) * 100 + 100;
  const nextYear = Math.floor(ageDays / 365) * 365 + 365;
  const isExactHundred = ageDays > 0 && ageDays % 100 === 0;
  const isExactYear = ageDays > 0 && ageDays % 365 === 0;

  let target: number;
  let kind: NextMilestone['kind'];
  if (isExactHundred && (isExactYear || nextHundred <= nextYear)) {
    target = ageDays;
    kind = 'day';
  } else if (isExactYear && nextYear <= nextHundred) {
    target = ageDays;
    kind = 'anniversary';
  } else if (nextHundred <= nextYear) {
    target = nextHundred;
    kind = 'day';
  } else {
    target = nextYear;
    kind = 'anniversary';
  }

  const daysToGo = Math.max(0, target - ageDays);
  const span = Math.max(target, 1);
  return {
    days: target,
    daysToGo,
    kind,
    progress: Math.min(1, ageDays / span),
  };
}
