/**
 * Mood domain model + serializer (Phase 15).
 *
 * Tracks how each person in the relationship is feeling.
 * One mood entry per person per day. Follows Phase 2 Entity conventions.
 *
 * Conventions:
 * - `moodValue`   — emoji/text mood indicator (e.g. 'happy', 'sad', 'love').
 * - `moodEmoji`   — the emoji display character.
 * - `note`        — optional personal note about how they're feeling.
 * - `profileId`   — the owner/partner profile who recorded this mood.
 * - `entryDate`   — LOCAL calendar day `yyyy-mm-dd`.
 * - `createdAt/updatedAt` — UTC ISO 8601 (Entity standard).
 * - `deletedAt`   — tombstone for soft deletes.
 */

import { assertEntityConventions, type TombstonedEntity } from '../model/entity.ts';
import {
  optionalString,
  requireEnum,
  requireString,
  type EntitySerializer,
  type Params,
  type Row,
} from '../serialization/entitySerializer.ts';
import { isValidDateKey } from '../../utils/time.ts';

// ---------------------------------------------------------------------------
// Mood values — the approved mood options
// ---------------------------------------------------------------------------

export const MOOD_VALUES = [
  'happy',
  'love',
  'excited',
  'calm',
  'grateful',
  'neutral',
  'tired',
  'sad',
  'anxious',
  'stressed',
] as const;

export type MoodValue = (typeof MOOD_VALUES)[number];

/** Maps mood values to display emojis. */
export const MOOD_EMOJI: Record<MoodValue, string> = {
  happy: '😊',
  love: '❤️',
  excited: '🎉',
  calm: '😌',
  grateful: '🙏',
  neutral: '😐',
  tired: '😴',
  sad: '😢',
  anxious: '😰',
  stressed: '😤',
};

/** Maps mood values to display labels. */
export const MOOD_LABELS: Record<MoodValue, string> = {
  happy: 'Happy',
  love: 'In Love',
  excited: 'Excited',
  calm: 'Calm',
  grateful: 'Grateful',
  neutral: 'Neutral',
  tired: 'Tired',
  sad: 'Sad',
  anxious: 'Anxious',
  stressed: 'Stressed',
};

// ---------------------------------------------------------------------------
// Mood entry
// ---------------------------------------------------------------------------

export interface MoodEntry extends TombstonedEntity {
  /** The mood value (key from MOOD_VALUES). */
  moodValue: MoodValue;
  /** The emoji display character. */
  moodEmoji: string;
  /** Optional personal note. */
  note: string | null;
  /** Profile id of who recorded this mood. */
  profileId: string;
  /** Local calendar day `yyyy-mm-dd`. */
  entryDate: string;
}

export type NewMoodEntry = Omit<MoodEntry, 'id' | 'createdAt' | 'updatedAt' | 'deletedAt'>;

export function assertMoodEntry(entry: MoodEntry): void {
  assertEntityConventions(entry);
  if (!MOOD_VALUES.includes(entry.moodValue)) {
    throw new Error(`moodValue must be one of: ${MOOD_VALUES.join(', ')}`);
  }
  if (!isValidDateKey(entry.entryDate)) {
    throw new Error('moodEntry.entryDate must be a yyyy-mm-dd calendar date');
  }
  if (!entry.profileId) {
    throw new Error('moodEntry.profileId is required');
  }
}

// ---------------------------------------------------------------------------
// Serializer
// ---------------------------------------------------------------------------

export const MOOD_COLUMNS = [
  'id',
  'mood_value',
  'mood_emoji',
  'note',
  'profile_id',
  'entry_date',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const moodSerializer: EntitySerializer<MoodEntry> = {
  columns: MOOD_COLUMNS,
  toParams(entry: MoodEntry): Params {
    return [
      entry.id,
      entry.moodValue,
      entry.moodEmoji,
      entry.note,
      entry.profileId,
      entry.entryDate,
      entry.createdAt,
      entry.updatedAt,
      entry.deletedAt,
    ];
  },
  fromRow(row: Row): MoodEntry {
    return {
      id: requireString(row, 'id'),
      moodValue: requireEnum(row, 'mood_value', MOOD_VALUES),
      moodEmoji: requireString(row, 'mood_emoji'),
      note: optionalString(row, 'note'),
      profileId: requireString(row, 'profile_id'),
      entryDate: requireString(row, 'entry_date'),
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};
