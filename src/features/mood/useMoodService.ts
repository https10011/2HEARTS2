/**
 * useMoodService hook (Stage 10).
 *
 * Lazily creates a MoodService backed by the bootstrap database adapter.
 * Caches the instance across renders — mood data stays on-device.
 */

import { useState, useEffect } from 'react';
import { getDatabase } from '../../data/database/connection.ts';
import { MoodRepository } from '../../repositories/moodRepository.ts';
import { MoodService } from '../../services/mood/moodService.ts';

let cachedService: MoodService | null = null;

async function getMoodService(): Promise<MoodService> {
  if (cachedService) return cachedService;
  const db = await getDatabase();
  cachedService = new MoodService(new MoodRepository(db));
  return cachedService;
}

export function useMoodService() {
  const [service, setService] = useState<MoodService | null>(null);

  useEffect(() => {
    let cancelled = false;
    getMoodService().then((s) => {
      if (!cancelled) setService(s);
    });
    return () => { cancelled = true; };
  }, []);

  return service;
}
