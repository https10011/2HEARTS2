/**
 * useTimelineService hook (Phase 9).
 *
 * Provides timeline CRUD operations with local state management.
 * Follows the UI → State → Service → Repository → Local persistence pattern.
 */

import { useCallback, useEffect, useState } from 'react';
import { getDatabase } from '../../data/database/connection.ts';
import {
  TimelineService,
  type TimelineEventView,
  type TimelineEventInput,
} from '../../services/timeline/timelineService.ts';
import { systemClock } from '../../utils/time.ts';

interface TimelineServiceState {
  events: TimelineEventView[];
  loading: boolean;
  error: string | null;
}

export function useTimelineService() {
  const [state, setState] = useState<TimelineServiceState>({
    events: [],
    loading: true,
    error: null,
  });

  const getService = useCallback(async () => {
    const db = await getDatabase();
    return new TimelineService(db, systemClock);
  }, []);

  const loadEvents = useCallback(async () => {
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const service = await getService();
      const events = await service.listEvents();
      setState({ events, loading: false, error: null });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load timeline.';
      setState((prev) => ({ ...prev, loading: false, error: message }));
    }
  }, [getService]);

  useEffect(() => {
    loadEvents();
  }, [loadEvents]);

  const createEvent = useCallback(
    async (input: TimelineEventInput): Promise<TimelineEventView> => {
      const service = await getService();
      const event = await service.createEvent(input);
      await loadEvents();
      return event;
    },
    [getService, loadEvents],
  );

  const updateEvent = useCallback(
    async (id: string, input: Partial<TimelineEventInput>): Promise<TimelineEventView> => {
      const service = await getService();
      const event = await service.updateEvent(id, input);
      await loadEvents();
      return event;
    },
    [getService, loadEvents],
  );

  const deleteEvent = useCallback(
    async (id: string): Promise<boolean> => {
      const service = await getService();
      const result = await service.deleteEvent(id);
      await loadEvents();
      return result;
    },
    [getService, loadEvents],
  );

  const getEvent = useCallback(
    async (id: string): Promise<TimelineEventView> => {
      const service = await getService();
      return service.getEvent(id);
    },
    [getService],
  );

  return {
    events: state.events,
    loading: state.loading,
    error: state.error,
    loadEvents,
    createEvent,
    updateEvent,
    deleteEvent,
    getEvent,
  };
}
