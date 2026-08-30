/**
 * Import Service (Stage 7 — Import / Data Portability System).
 *
 * Parses TwoHearts JSON import files and imports data into the local
 * database. Handles notes and reminders. Profile photos and Vault items
 * use their existing dedicated flows.
 *
 * Design decisions:
 * - JSON format: simple, human-readable, extensible
 * - One file per import (no ZIP bundles for V1)
 * - All imports create NEW records (no deduplication in V1)
 * - Partial failure: track per-record success/failure, report summary
 */

import { getDatabase } from '../../data/database/connection.ts';
import { NoteRepository } from '../../repositories/noteRepository.ts';
import { ReminderRepository } from '../../repositories/reminderRepository.ts';
import { NoteCategory, NOTE_CATEGORIES } from '../../data/note/noteTypes.ts';
import { ReminderRecurrence, REMINDER_RECURRENCES } from '../../data/reminder/reminderTypes.ts';
import { createLogger } from '../logging/logger.ts';

const log = createLogger('import');

// ---------------------------------------------------------------------------
// Import file format
// ---------------------------------------------------------------------------

export interface ImportNote {
  title: string;
  content?: string;
  category?: string;
}

export interface ImportReminder {
  title: string;
  description?: string;
  scheduledDate: string;
  scheduledTime: string;
  recurrence?: string;
  notificationEnabled?: boolean;
}

export interface ImportFile {
  format: 'twohearts-import';
  version: 1;
  exportedAt?: string;
  content: {
    notes?: ImportNote[];
    reminders?: ImportReminder[];
  };
}

// ---------------------------------------------------------------------------
// Validation
// ---------------------------------------------------------------------------

export interface ValidationError {
  field: string;
  message: string;
}

export function validateImportFile(data: unknown): { valid: boolean; errors: ValidationError[]; parsed?: ImportFile } {
  const errors: ValidationError[] = [];

  if (!data || typeof data !== 'object') {
    return { valid: false, errors: [{ field: 'root', message: 'Invalid file: not a JSON object' }] };
  }

  const obj = data as Record<string, unknown>;

  if (obj.format !== 'twohearts-import') {
    errors.push({ field: 'format', message: 'Unknown format. Expected "twohearts-import".' });
  }

  if (obj.version !== 1) {
    errors.push({ field: 'version', message: `Unsupported version: ${obj.version}. Expected 1.` });
  }

  const content = obj.content as Record<string, unknown> | undefined;
  if (!content || typeof content !== 'object') {
    errors.push({ field: 'content', message: 'Missing or invalid "content" section.' });
  }

  if (errors.length > 0) {
    return { valid: false, errors };
  }

  const typedContent = content as Record<string, unknown>;
  const notes = typedContent.notes as unknown[] | undefined;
  const reminders = typedContent.reminders as unknown[] | undefined;

  // Validate notes
  if (notes && Array.isArray(notes)) {
    notes.forEach((note, i) => {
      if (!note || typeof note !== 'object') {
        errors.push({ field: `content.notes[${i}]`, message: 'Invalid note entry.' });
        return;
      }
      const n = note as Record<string, unknown>;
      if (!n.title || typeof n.title !== 'string' || (n.title as string).trim().length === 0) {
        errors.push({ field: `content.notes[${i}].title`, message: 'Note must have a title.' });
      }
      if (n.category && typeof n.category === 'string' && !NOTE_CATEGORIES.includes(n.category as NoteCategory)) {
        errors.push({ field: `content.notes[${i}].category`, message: `Invalid category: ${n.category}` });
      }
    });
  }

  // Validate reminders
  if (reminders && Array.isArray(reminders)) {
    reminders.forEach((reminder, i) => {
      if (!reminder || typeof reminder !== 'object') {
        errors.push({ field: `content.reminders[${i}]`, message: 'Invalid reminder entry.' });
        return;
      }
      const r = reminder as Record<string, unknown>;
      if (!r.title || typeof r.title !== 'string' || (r.title as string).trim().length === 0) {
        errors.push({ field: `content.reminders[${i}].title`, message: 'Reminder must have a title.' });
      }
      if (!r.scheduledDate || typeof r.scheduledDate !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(r.scheduledDate as string)) {
        errors.push({ field: `content.reminders[${i}].scheduledDate`, message: 'Reminder must have a valid date (yyyy-mm-dd).' });
      }
      if (!r.scheduledTime || typeof r.scheduledTime !== 'string' || !/^\d{2}:\d{2}$/.test(r.scheduledTime as string)) {
        errors.push({ field: `content.reminders[${i}].scheduledTime`, message: 'Reminder must have a valid time (HH:mm).' });
      }
      if (r.recurrence && typeof r.recurrence === 'string' && !REMINDER_RECURRENCES.includes(r.recurrence as ReminderRecurrence)) {
        errors.push({ field: `content.reminders[${i}].recurrence`, message: `Invalid recurrence: ${r.recurrence}` });
      }
    });
  }

  if (errors.length > 0) {
    return { valid: false, errors };
  }

  return {
    valid: true,
    errors: [],
    parsed: data as ImportFile,
  };
}

// ---------------------------------------------------------------------------
// Import execution
// ---------------------------------------------------------------------------

export interface ImportResult {
  notes: { imported: number; failed: number; errors: string[] };
  reminders: { imported: number; failed: number; errors: string[] };
}

export async function importNotes(notes: ImportNote[]): Promise<{ imported: number; failed: number; errors: string[] }> {
  const db = await getDatabase();
  const repo = new NoteRepository(db);
  let imported = 0;
  let failed = 0;
  const errors: string[] = [];

  for (const note of notes) {
    try {
      await repo.create({
        title: note.title.trim(),
        content: (note.content ?? '').trim(),
        category: (NOTE_CATEGORIES.includes(note.category as NoteCategory) ? note.category : 'general') as NoteCategory,
        deletedAt: null,
      });
      imported++;
    } catch (cause) {
      failed++;
      const msg = cause instanceof Error ? cause.message : 'Unknown error';
      errors.push(`Note "${note.title}": ${msg}`);
      log.warn('Failed to import note.', { title: note.title, cause });
    }
  }

  return { imported, failed, errors };
}

export async function importReminders(reminders: ImportReminder[]): Promise<{ imported: number; failed: number; errors: string[] }> {
  const db = await getDatabase();
  const repo = new ReminderRepository(db);
  let imported = 0;
  let failed = 0;
  const errors: string[] = [];

  for (const reminder of reminders) {
    try {
      await repo.create({
        title: reminder.title.trim(),
        description: (reminder.description ?? '').trim() || null,
        scheduledDate: reminder.scheduledDate,
        scheduledTime: reminder.scheduledTime,
        recurrence: (REMINDER_RECURRENCES.includes(reminder.recurrence as ReminderRecurrence) ? reminder.recurrence : 'none') as ReminderRecurrence,
        status: 'active' as ReminderRecurrence extends string ? 'active' : never,
        notificationOwnerRef: null,
        notificationEnabled: reminder.notificationEnabled !== false,
      });
      imported++;
    } catch (cause) {
      failed++;
      const msg = cause instanceof Error ? cause.message : 'Unknown error';
      errors.push(`Reminder "${reminder.title}": ${msg}`);
      log.warn('Failed to import reminder.', { title: reminder.title, cause });
    }
  }

  return { imported, failed, errors };
}

export async function executeImport(file: ImportFile): Promise<ImportResult> {
  const result: ImportResult = {
    notes: { imported: 0, failed: 0, errors: [] },
    reminders: { imported: 0, failed: 0, errors: [] },
  };

  if (file.content.notes && file.content.notes.length > 0) {
    result.notes = await importNotes(file.content.notes);
  }

  if (file.content.reminders && file.content.reminders.length > 0) {
    result.reminders = await importReminders(file.content.reminders);
  }

  log.info('Import completed.', {
    notesImported: result.notes.imported,
    notesFailed: result.notes.failed,
    remindersImported: result.reminders.imported,
    remindersFailed: result.reminders.failed,
  });

  return result;
}
