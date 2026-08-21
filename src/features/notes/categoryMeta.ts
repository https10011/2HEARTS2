/**
 * Shared note-category presentation metadata (Phase 22).
 *
 * Single source for category labels and accent colors so all notes
 * screens stay consistent (previously duplicated per screen).
 */

export const NOTE_CATEGORY_LABELS: Record<string, string> = {
  general: 'General',
  shared: 'Shared',
  private: 'Private',
  'love-letter': 'Love Letter',
  gratitude: 'Gratitude',
  idea: 'Idea',
  reminder: 'Reminder',
};

export const NOTE_CATEGORY_COLORS: Record<string, string> = {
  general: 'var(--th-color-burgundy)',
  shared: '#8B5E3C',
  private: '#4A5568',
  'love-letter': '#C53030',
  gratitude: '#2F855A',
  idea: '#6B46C1',
  reminder: '#D69E2E',
};
