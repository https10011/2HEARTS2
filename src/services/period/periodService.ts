/**
 * Period service (Phase 16).
 *
 * Application-facing boundary for the local period tracking system.
 * Manages: entries, settings, cycle calculations, predictions, validation.
 *
 * All data remains LOCAL. No health advice, no diagnostics.
 * Architecture: UI → PeriodService → PeriodRepository → Local persistence.
 */

import { AppError, normalizeAppError } from '../errors/appError.ts';
import {
  type PeriodEntry,
  type PeriodSettings,
  type FlowLevel,
  FLOW_LEVELS,
  DEFAULT_CYCLE_LENGTH,
  DEFAULT_PERIOD_LENGTH,
  addDays,
  diffDays,
} from '../../data/period/periodTypes.ts';
import { PeriodRepository } from '../../repositories/periodRepository.ts';

export interface LogPeriodInput {
  startDate: string;
  endDate?: string | null;
  flowLevel: FlowLevel;
  note?: string | null;
  profileId: string;
}

export interface UpdatePeriodInput {
  startDate?: string;
  endDate?: string | null;
  flowLevel?: FlowLevel;
  note?: string | null;
}

export interface CycleInfo {
  /** Current cycle day (1-indexed from last period start). */
  currentCycleDay: number;
  /** Total cycle length. */
  cycleLength: number;
  /** Estimated next period start date. */
  nextPeriodDate: string;
  /** Days until next period. */
  daysUntilNext: number;
  /** Whether currently in a period. */
  inPeriod: boolean;
  /** Current period day if in period, null otherwise. */
  currentPeriodDay: number | null;
  /** Last period start date. */
  lastPeriodStart: string;
}

export interface PeriodSummary {
  /** Most recent entry. */
  lastEntry: PeriodEntry | null;
  /** Cycle info (null if no entries). */
  cycleInfo: CycleInfo | null;
  /** Total logged cycles. */
  totalCycles: number;
  /** Average cycle length from history. */
  averageCycleLength: number | null;
  /** Average period duration from history. */
  averagePeriodDuration: number | null;
  /** Settings. */
  settings: PeriodSettings;
}

export class PeriodService {
  constructor(
    private readonly repository: PeriodRepository,
  ) {}

  // -----------------------------------------------------------------------
  // Entries
  // -----------------------------------------------------------------------

  async logPeriod(input: LogPeriodInput): Promise<PeriodEntry> {
    this.validateEntryInput(input);

    try {
      return await this.repository.createEntry({
        startDate: input.startDate,
        endDate: input.endDate ?? null,
        flowLevel: input.flowLevel,
        note: input.note?.trim() ?? null,
        profileId: input.profileId,
      });
    } catch (cause) {
      throw normalizeAppError(cause, 'validation', 'create-failed', {
        recoverable: false,
        userMessage: 'Could not log period.',
      });
    }
  }

  async updateEntry(id: string, input: UpdatePeriodInput): Promise<PeriodEntry> {
    const existing = await this.repository.getEntryById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Period entry not found.',
      });
    }

    if (input.flowLevel && !FLOW_LEVELS.includes(input.flowLevel)) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Invalid flow level.',
      });
    }

    try {
      const updated = await this.repository.updateEntry(id, {
        startDate: input.startDate,
        endDate: input.endDate,
        flowLevel: input.flowLevel,
        note: input.note?.trim(),
      });
      if (!updated) throw new Error('Update returned null');
      return updated;
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'validation', 'update-failed', {
        recoverable: false,
        userMessage: 'Could not update period entry.',
      });
    }
  }

  async deleteEntry(id: string): Promise<void> {
    const existing = await this.repository.getEntryById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Period entry not found.',
      });
    }
    await this.repository.deleteEntry(id);
  }

  async getEntryById(id: string): Promise<PeriodEntry | null> {
    return this.repository.getEntryById(id);
  }

  async listEntries(profileId: string): Promise<PeriodEntry[]> {
    return this.repository.listEntries(profileId);
  }

  async listEntriesInRange(profileId: string, startDate: string, endDate: string): Promise<PeriodEntry[]> {
    return this.repository.listEntriesInRange(profileId, startDate, endDate);
  }

  // -----------------------------------------------------------------------
  // Settings
  // -----------------------------------------------------------------------

  async getSettings(profileId: string): Promise<PeriodSettings> {
    const existing = await this.repository.getSettings(profileId);
    return existing ?? {
      id: '',
      profileId,
      cycleLengthDays: DEFAULT_CYCLE_LENGTH,
      periodLengthDays: DEFAULT_PERIOD_LENGTH,
      createdAt: '',
      updatedAt: '',
    };
  }

  async saveSettings(profileId: string, cycleDays: number, periodDays: number): Promise<PeriodSettings> {
    if (cycleDays < 20 || cycleDays > 45) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Cycle length must be between 20 and 45 days.',
      });
    }
    if (periodDays < 1 || periodDays > 10) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Period length must be between 1 and 10 days.',
      });
    }
    return this.repository.saveSettings(profileId, cycleDays, periodDays);
  }

  // -----------------------------------------------------------------------
  // Cycle calculations
  // -----------------------------------------------------------------------

  async getCycleInfo(profileId: string): Promise<CycleInfo | null> {
    const entries = await this.repository.listEntries(profileId);
    if (entries.length === 0) return null;

    const settings = await this.getSettings(profileId);
    const lastEntry = entries[0]; // Already sorted by start_date DESC
    const today = this.todayKey();

    // Calculate current cycle day
    const daysSinceStart = diffDays(lastEntry.startDate, today);
    const cycleLength = settings.cycleLengthDays;
    const currentCycleDay = (daysSinceStart % cycleLength) + 1;

    // Estimate next period
    const nextPeriodDate = addDays(lastEntry.startDate, cycleLength);
    const daysUntilNext = diffDays(today, nextPeriodDate);

    // Check if currently in period
    const inPeriod = lastEntry.endDate
      ? today >= lastEntry.startDate && today <= lastEntry.endDate
      : today >= lastEntry.startDate;

    const currentPeriodDay = inPeriod
      ? diffDays(lastEntry.startDate, today) + 1
      : null;

    return {
      currentCycleDay,
      cycleLength,
      nextPeriodDate,
      daysUntilNext: Math.max(0, daysUntilNext),
      inPeriod,
      currentPeriodDay,
      lastPeriodStart: lastEntry.startDate,
    };
  }

  async getSummary(profileId: string): Promise<PeriodSummary> {
    const entries = await this.repository.listEntries(profileId);
    const settings = await this.getSettings(profileId);
    const cycleInfo = await this.getCycleInfo(profileId);

    const lastEntry = entries.length > 0 ? entries[0] : null;

    // Calculate averages from history
    let averageCycleLength: number | null = null;
    let averagePeriodDuration: number | null = null;

    if (entries.length >= 2) {
      // Cycle lengths: differences between consecutive start dates
      let totalCycleDays = 0;
      let cycleCount = 0;
      for (let i = 0; i < entries.length - 1; i++) {
        const diff = diffDays(entries[i + 1].startDate, entries[i].startDate);
        if (diff > 15 && diff < 60) { // Filter out unreasonable values
          totalCycleDays += diff;
          cycleCount++;
        }
      }
      if (cycleCount > 0) {
        averageCycleLength = Math.round(totalCycleDays / cycleCount);
      }

      // Period durations: from entries with end dates
      let totalDuration = 0;
      let durationCount = 0;
      for (const entry of entries) {
        if (entry.endDate) {
          const dur = diffDays(entry.startDate, entry.endDate) + 1;
          if (dur > 0 && dur < 15) {
            totalDuration += dur;
            durationCount++;
          }
        }
      }
      if (durationCount > 0) {
        averagePeriodDuration = Math.round(totalDuration / durationCount);
      }
    }

    return {
      lastEntry,
      cycleInfo,
      totalCycles: entries.length,
      averageCycleLength,
      averagePeriodDuration,
      settings,
    };
  }

  // -----------------------------------------------------------------------
  // Validation
  // -----------------------------------------------------------------------

  private validateEntryInput(input: LogPeriodInput): void {
    if (!input.startDate) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Start date is required.',
      });
    }
    if (!FLOW_LEVELS.includes(input.flowLevel)) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Invalid flow level.',
      });
    }
    if (!input.profileId) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Profile ID is required.',
      });
    }
    if (input.endDate && input.endDate < input.startDate) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'End date cannot be before start date.',
      });
    }
  }

  private todayKey(): string {
    const d = new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}
