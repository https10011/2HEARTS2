/**
 * useNoteService hook (Phase 8).
 *
 * Provides note CRUD operations with local state management.
 * Follows the UI → State → Service → Repository → Local persistence pattern.
 */

import { useCallback, useEffect, useState } from 'react';
import { getDatabase } from '../../data/database/connection.ts';
import {
  NoteService,
  type NoteView,
  type NoteInput,
} from '../../services/note/noteService.ts';
import type { NoteCategory } from '../../data/note/noteTypes.ts';
import { systemClock } from '../../utils/time.ts';

interface NoteServiceState {
  notes: NoteView[];
  loading: boolean;
  error: string | null;
}

export function useNoteService() {
  const [state, setState] = useState<NoteServiceState>({
    notes: [],
    loading: true,
    error: null,
  });
  const [counts, setCounts] = useState<Record<string, number>>({});

  const getService = useCallback(async () => {
    const db = await getDatabase();
    return new NoteService(db, systemClock);
  }, []);

  const loadNotes = useCallback(async () => {
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const service = await getService();
      const notes = await service.listNotes();
      const c = await service.getCounts();
      setState({ notes, loading: false, error: null });
      setCounts(c);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load notes.';
      setState((prev) => ({ ...prev, loading: false, error: message }));
    }
  }, [getService]);

  useEffect(() => {
    loadNotes();
  }, [loadNotes]);

  const createNote = useCallback(
    async (input: NoteInput): Promise<NoteView> => {
      const service = await getService();
      const note = await service.createNote(input);
      await loadNotes();
      return note;
    },
    [getService, loadNotes],
  );

  const updateNote = useCallback(
    async (id: string, input: Partial<NoteInput>): Promise<NoteView> => {
      const service = await getService();
      const note = await service.updateNote(id, input);
      await loadNotes();
      return note;
    },
    [getService, loadNotes],
  );

  const deleteNote = useCallback(
    async (id: string): Promise<boolean> => {
      const service = await getService();
      const result = await service.deleteNote(id);
      await loadNotes();
      return result;
    },
    [getService, loadNotes],
  );

  const getNote = useCallback(
    async (id: string): Promise<NoteView> => {
      const service = await getService();
      return service.getNote(id);
    },
    [getService],
  );

  const listByCategory = useCallback(
    async (category: NoteCategory): Promise<NoteView[]> => {
      const service = await getService();
      return service.listByCategory(category);
    },
    [getService],
  );

  const validateNote = useCallback(
    (input: NoteInput) => {
      // Synchronous validation doesn't need the service
      if (!input.title || input.title.trim().length === 0) {
        return { ok: false, errors: ['Title is required.'] };
      }
      if (input.title.trim().length > 200) {
        return { ok: false, errors: ['Title must be at most 200 characters.'] };
      }
      if (input.content && input.content.length > 50000) {
        return { ok: false, errors: ['Content is too long.'] };
      }
      return { ok: true, errors: [] };
    },
    [],
  );

  return {
    notes: state.notes,
    loading: state.loading,
    error: state.error,
    counts,
    loadNotes,
    createNote,
    updateNote,
    deleteNote,
    getNote,
    listByCategory,
    validateNote,
  };
}
