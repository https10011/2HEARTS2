/**
 * Relationship service (Phase 4).
 *
 * The application-facing boundary over the identity foundation: profiles,
 * the couple singleton, and important dates. Repositories never leak to
 * consumers; all input is validated with the shared Phase 3 validators and
 * failures surface as typed `AppError`s (validation category) — never raw
 * SQL, never silent writes.
 *
 * Anniversary/age math delegates to the Phase 3 datetime helpers, so the
 * leap-day rule (Feb 29 → Feb 28 in non-leap years) and DST-safe local-day
 * diffs stay centralized.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import {
  assertImportantDate,
  assertProfile,
  type ImportantDate,
  type Profile,
  type Recurrence,
} from '../../data/relationship/relationshipTypes.ts';
import { CoupleRepository } from '../../repositories/coupleRepository.ts';
import { ImportantDateRepository } from '../../repositories/importantDateRepository.ts';
import { ProfileRepository } from '../../repositories/profileRepository.ts';
import { isValidDateKey, systemClock, type Clock } from '../../utils/time.ts';
import {
  anniversaryInYear,
  daysUntilAnniversary,
  decomposeCalendarAge,
  parseDate,
  relationshipAgeDays,
  startOfLocalDay,
  toLocalDateKey,
} from '../datetime/datetime.ts';
import { AppError } from '../errors/appError.ts';
import { normalizeInput, textLength, validIsoDate, validate } from '../validation/validators.ts';

const MAX_NAME_LENGTH = 40;
const MAX_TITLE_LENGTH = 80;

export interface ProfileInput {
  displayName: string;
  birthDate?: string | null;
}

export interface RelationshipSummary {
  owner: Profile | null;
  partner: Profile | null;
  startDate: string | null;
  /** Whole local days since the start date, or null when unset/in future. */
  ageDays: number | null;
  /** Decomposed calendar age (years/months/days), or null when unavailable. */
  decomposedAge: { years: number; months: number; days: number } | null;
  /** Local `yyyy-mm-dd` of the anniversary in the current/next year. */
  nextAnniversary: string | null;
  daysUntilNextAnniversary: number | null;
  /** Upcoming important dates with days-until info. */
  upcomingDates: Array<{ title: string; date: string; daysUntil: number; recurrence: Recurrence }>
}

function validationFailure(errors: string[]): never {
  throw new AppError('validation', 'invalid-input', {
    recoverable: true,
    userMessage: 'Please check the highlighted fields.',
    cause: { errors }, // logger redacts; never surfaced raw
  });
}

function assertValidDateKey(value: string, field: string): void {
  if (!isValidDateKey(value)) {
    validationFailure([`${field} must be a valid yyyy-mm-dd date.`]);
  }
}

export class RelationshipService {
  private readonly profiles: ProfileRepository;
  private readonly couple: CoupleRepository;
  private readonly dates: ImportantDateRepository;

  constructor(
    private readonly db: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {
    this.profiles = new ProfileRepository(db, clock);
    this.couple = new CoupleRepository(db, clock);
    this.dates = new ImportantDateRepository(db, clock);
  }

  // -- Profiles -------------------------------------------------------------

  async getOwner(): Promise<Profile | null> {
    return this.profiles.getOwner();
  }

  async getPartner(): Promise<Profile | null> {
    return this.profiles.getPartner();
  }

  /**
   * Creates or updates the owner profile and links it to the couple row
   * in one transaction (profile + couple write must not split).
   */
  async saveOwner(input: ProfileInput): Promise<Profile> {
    return this.saveProfile('owner', input);
  }

  /** Same transaction semantics as saveOwner, for the partner side. */
  async savePartner(input: ProfileInput): Promise<Profile> {
    return this.saveProfile('partner', input);
  }

  private async saveProfile(role: 'owner' | 'partner', input: ProfileInput): Promise<Profile> {
    const displayName = normalizeInput(input.displayName);
    const birthDate = input.birthDate ?? null;
    const result = validate(
      textLength(displayName, 1, MAX_NAME_LENGTH, 'Display name'),
      birthDate === null ? { ok: true, errors: [] } : validIsoDate(birthDate),
    );
    if (!result.ok) validationFailure(result.errors);

    // Profile write + couple-singleton link must be atomic: both go through
    // the same adapter inside one outer transaction (per the adapter's
    // nesting rule).
    return this.db.transaction(async () => {
      const existing =
        role === 'owner' ? await this.profiles.getOwner() : await this.profiles.getPartner();
      let saved: Profile;
      if (existing) {
        saved = await this.profiles.update(existing.id, { displayName, birthDate });
      } else {
        saved = await this.profiles.create({ role, displayName, birthDate, deletedAt: null });
      }
      assertProfile(saved);

      const couple = await this.couple.get();
      await this.couple.save({
        ownerProfileId: role === 'owner' ? saved.id : (couple?.ownerProfileId ?? null),
        partnerProfileId: role === 'partner' ? saved.id : (couple?.partnerProfileId ?? null),
        startDate: couple?.startDate ?? null,
      });
      return saved;
    });
  }

  // -- Couple relationship ---------------------------------------------------

  /** Sets or clears the relationship start date on the couple singleton. */
  async setStartDate(startDate: string | null): Promise<void> {
    if (startDate !== null) assertValidDateKey(startDate, 'startDate');
    const couple = await this.couple.get();
    await this.couple.save({
      ownerProfileId: couple?.ownerProfileId ?? null,
      partnerProfileId: couple?.partnerProfileId ?? null,
      startDate,
    });
  }

  async getSummary(): Promise<RelationshipSummary> {
    const [owner, partner, couple] = await Promise.all([
      this.profiles.getOwner(),
      this.profiles.getPartner(),
      this.couple.get(),
    ]);
    const startDate = couple?.startDate ?? null;
    let ageDays: number | null = null;
    let nextAnniversary: string | null = null;
    let daysUntilNextAnniversary: number | null = null;

    let decomposedAge: RelationshipSummary['decomposedAge'] = null;

    if (startDate !== null) {
      const start = parseDate(startDate, true);
      const now = this.clock();
      if (start && start <= now) {
        ageDays = relationshipAgeDays(start, now);
        decomposedAge = decomposeCalendarAge(start, now);
        const next = anniversaryInYear(start, now.getFullYear());
        const candidate = next >= startOfToday(now) ? next : anniversaryInYear(start, now.getFullYear() + 1);
        nextAnniversary = toLocalDateKey(candidate);
        daysUntilNextAnniversary = daysUntilAnniversary(start, now);
      } else if (start) {
        // Start date is in the future: anniversary math still applies.
        const next = anniversaryInYear(start, now.getFullYear());
        const candidate = next >= now ? next : anniversaryInYear(start, now.getFullYear() + 1);
        nextAnniversary = toLocalDateKey(candidate);
        daysUntilNextAnniversary = daysUntilAnniversary(start, now);
      }
    }

    // Upcoming important dates with days-until
    const allDates = await this.dates.list();
    const now2 = this.clock();
    const todayKey = toLocalDateKey(startOfLocalDay(now2));
    const upcomingDates: RelationshipSummary['upcomingDates'] = [];
    for (const d of allDates) {
      const dateKey = d.date;
      const dateObj = parseDate(dateKey, true);
      if (!dateObj) continue;
      if (d.recurrence === 'yearly') {
        // For yearly dates, calculate next occurrence
        const thisYearAnniv = anniversaryInYear(dateObj, now2.getFullYear());
        const nextOccurrence = thisYearAnniv >= startOfLocalDay(now2)
          ? thisYearAnniv
          : anniversaryInYear(dateObj, now2.getFullYear() + 1);
        const daysUntil = relationshipAgeDays(startOfLocalDay(now2), startOfLocalDay(nextOccurrence));
        upcomingDates.push({
          title: d.title,
          date: toLocalDateKey(nextOccurrence),
          daysUntil,
          recurrence: d.recurrence,
        });
      } else {
        // Non-recurring: only show if in the future
        if (dateKey > todayKey) {
          const daysUntil = relationshipAgeDays(startOfLocalDay(now2), startOfLocalDay(dateObj));
          upcomingDates.push({
            title: d.title,
            date: dateKey,
            daysUntil,
            recurrence: d.recurrence,
          });
        }
      }
    }
    upcomingDates.sort((a, b) => a.daysUntil - b.daysUntil || a.date.localeCompare(b.date));

    return { owner, partner, startDate, ageDays, decomposedAge, nextAnniversary, daysUntilNextAnniversary, upcomingDates };
  }

  // -- Important dates --------------------------------------------------------

  async addImportantDate(input: {
    title: string;
    date: string;
    recurrence?: Recurrence;
    profileId?: string | null;
  }): Promise<ImportantDate> {
    const title = normalizeInput(input.title);
    const result = validate(textLength(title, 1, MAX_TITLE_LENGTH, 'Title'));
    if (!result.ok) validationFailure(result.errors);
    assertValidDateKey(input.date, 'date');
    if (input.profileId) {
      const profile = await this.profiles.getById(input.profileId);
      if (!profile) validationFailure(['profileId does not reference a known profile.']);
    }
    const created = await this.dates.create({
      title,
      date: input.date,
      recurrence: input.recurrence ?? 'none',
      profileId: input.profileId ?? null,
      deletedAt: null,
    });
    assertImportantDate(created);
    return created;
  }

  async listImportantDates(): Promise<ImportantDate[]> {
    return this.dates.list();
  }

  async updateImportantDate(id: string, input: { title?: string; date?: string; recurrence?: Recurrence }): Promise<ImportantDate> {
    const existing = await this.dates.getById(id);
    if (!existing) {
      throw new AppError('persistence', 'not-found', {
        recoverable: false,
        userMessage: 'Date not found.',
      });
    }
    const changes: Record<string, unknown> = {};
    if (input.title !== undefined) {
      const title = normalizeInput(input.title);
      const result = validate(textLength(title, 1, MAX_TITLE_LENGTH, 'Title'));
      if (!result.ok) validationFailure(result.errors);
      changes.title = title;
    }
    if (input.date !== undefined) {
      assertValidDateKey(input.date, 'date');
      changes.date = input.date;
    }
    if (input.recurrence !== undefined) {
      changes.recurrence = input.recurrence;
    }
    const updated = await this.dates.update(id, changes);
    assertImportantDate(updated);
    return updated;
  }

  async removeImportantDate(id: string): Promise<boolean> {
    return this.dates.delete(id);
  }

  async getImportantDateById(id: string): Promise<ImportantDate | null> {
    return this.dates.getById(id);
  }
}

// Local midnight, needed so "anniversary is today" doesn't roll to next year.
function startOfToday(now: Date): Date {
  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}


