/**
 * Mood service (Phase 15).
 *
 * Application-facing boundary for the local mood tracking system.
 * Manages: create/update mood entries, retrieve history, validation,
 * error normalization.
 *
 * Architecture: UI → MoodService → MoodRepository → Local persistence.
 */

import { AppError, normalizeAppError } from '../errors/appError.ts';
import {
  type MoodEntry,
  type MoodValue,
  MOOD_EMOJI,
  MOOD_VALUES,
} from '../../data/mood/moodTypes.ts';
import { MoodRepository } from '../../repositories/moodRepository.ts';

export interface CreateMoodInput {
  moodValue: MoodValue;
  note?: string | null;
  profileId: string;
  entryDate: string;
}

export interface UpdateMoodInput {
  moodValue?: MoodValue;
  note?: string | null;
}

export class MoodService {
  constructor(
    private readonly repository: MoodRepository,
  ) {}

  // -----------------------------------------------------------------------
  // Create / Update (one per profile per day)
  // -----------------------------------------------------------------------

  async record(input: CreateMoodInput): Promise<MoodEntry> {
    this.validateInput(input);

    const data = {
      moodValue: input.moodValue,
      moodEmoji: MOOD_EMOJI[input.moodValue],
      note: input.note?.trim() ?? null,
      profileId: input.profileId,
      entryDate: input.entryDate,
    };

    try {
      return await this.repository.createOrUpdate(data);
    } catch (cause) {
      throw normalizeAppError(cause, 'validation', 'create-failed', {
        recoverable: false,
        userMessage: 'Could not save mood.',
      });
    }
  }

  // -----------------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------------

  async update(id: string, input: UpdateMoodInput): Promise<MoodEntry> {
    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Mood entry not found.',
      });
    }

    if (input.moodValue !== undefined) {
      if (!MOOD_VALUES.includes(input.moodValue)) {
        throw new AppError('validation', 'invalid-input', {
          recoverable: false,
          userMessage: 'Invalid mood value.',
        });
      }
    }

    try {
      const updated = await this.repository.update(id, {
        moodValue: input.moodValue,
        moodEmoji: input.moodValue ? MOOD_EMOJI[input.moodValue] : undefined,
        note: input.note?.trim(),
      });
      if (!updated) throw new Error('Update returned null');
      return updated;
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'validation', 'update-failed', {
        recoverable: false,
        userMessage: 'Could not update mood.',
      });
    }
  }

  // -----------------------------------------------------------------------
  // Delete
  // -----------------------------------------------------------------------

  async delete(id: string): Promise<void> {
    const existing = await this.repository.getById(id);
    if (!existing) {
      throw new AppError('validation', 'not-found', {
        recoverable: false,
        userMessage: 'Mood entry not found.',
      });
    }

    try {
      await this.repository.delete(id);
    } catch (cause) {
      if (cause instanceof AppError) throw cause;
      throw normalizeAppError(cause, 'validation', 'delete-failed', {
        recoverable: false,
        userMessage: 'Could not delete mood entry.',
      });
    }
  }

  // -----------------------------------------------------------------------
  // Read
  // -----------------------------------------------------------------------

  async getById(id: string): Promise<MoodEntry | null> {
    return this.repository.getById(id);
  }

  async getByProfileAndDate(profileId: string, date: string): Promise<MoodEntry | null> {
    return this.repository.getByProfileAndDate(profileId, date);
  }

  async list(): Promise<MoodEntry[]> {
    return this.repository.list();
  }

  async listByProfile(profileId: string, limit?: number): Promise<MoodEntry[]> {
    return this.repository.listByProfile(profileId, limit);
  }

  async listRecent(limit?: number): Promise<MoodEntry[]> {
    return this.repository.listRecent(limit);
  }

  async count(): Promise<number> {
    return this.repository.count();
  }

  // -----------------------------------------------------------------------
  // Mood statistics
  // -----------------------------------------------------------------------

  async getMoodStats(profileId: string): Promise<{
    totalEntries: number;
    mostFrequentMood: MoodValue | null;
    averageMood?: MoodValue;
  }> {
    const entries = await this.repository.listByProfile(profileId, 100);
    if (entries.length === 0) {
      return { totalEntries: 0, mostFrequentMood: null };
    }

    // Count occurrences
    const counts: Record<string, number> = {};
    for (const entry of entries) {
      counts[entry.moodValue] = (counts[entry.moodValue] || 0) + 1;
    }

    let mostFrequent: MoodValue | null = null;
    let maxCount = 0;
    for (const [mood, count] of Object.entries(counts)) {
      if (count > maxCount) {
        maxCount = count;
        mostFrequent = mood as MoodValue;
      }
    }

    return {
      totalEntries: entries.length,
      mostFrequentMood: mostFrequent,
    };
  }

  // -----------------------------------------------------------------------
  // Validation
  // -----------------------------------------------------------------------

  private validateInput(input: CreateMoodInput): void {
    if (!MOOD_VALUES.includes(input.moodValue)) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Invalid mood value.',
      });
    }
    if (!input.profileId) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Profile ID is required.',
      });
    }
    if (!input.entryDate) {
      throw new AppError('validation', 'invalid-input', {
        recoverable: false,
        userMessage: 'Entry date is required.',
      });
    }
  }
}
