/**
 * useHomeHighlights (Stage 3) — loads the Home "From your story" previews
 * and the notification badge count.
 *
 * Sources are queried independently and every failure degrades to an
 * empty slice, so Home renders even when a feature store is unavailable.
 * The vault is never touched (MasterPrompt §46). View-model shaping and
 * route building live in the pure `homeHighlights.ts` module.
 */

import { useEffect, useState } from 'react';
import { getDatabase } from '../../data/database/connection.ts';
import { NoteService } from '../../services/note/noteService.ts';
import { ReminderRepository } from '../../repositories/reminderRepository.ts';
import { NotificationCenterRepository } from '../../repositories/notificationCenterRepository.ts';
import { systemClock } from '../../utils/time.ts';
import { useMemoryService } from '../memories/useMemoryService.ts';
import {
  selectHomeHighlights,
  type HomeHighlight,
} from './homeHighlights.ts';

export interface HomeHighlightsState {
  highlights: HomeHighlight[];
  unreadNotifications: number;
}

export function useHomeHighlights(): HomeHighlightsState {
  const memoryService = useMemoryService();
  const [state, setState] = useState<HomeHighlightsState>({
    highlights: [],
    unreadNotifications: 0,
  });

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      let highlights: HomeHighlight[] = [];
      let unreadNotifications = 0;
      try {
        const db = await getDatabase();
        const [notes, reminders, memories, unread] = await Promise.all([
          new NoteService(db, systemClock).listNotes().catch(() => []),
          new ReminderRepository(db).listUpcoming().catch(() => []),
          (memoryService ? memoryService.listMemories() : Promise.resolve([])).catch(() => []),
          new NotificationCenterRepository(db).countUnread().catch(() => 0),
        ]);
        highlights = selectHomeHighlights({ notes, reminders, memories });
        unreadNotifications = unread;
      } catch {
        // Graceful degradation — Home renders without previews.
      }
      if (!cancelled) setState({ highlights, unreadNotifications });
    };

    void load();
    return () => {
      cancelled = true;
    };
  }, [memoryService]);

  return state;
}
