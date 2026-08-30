/**
 * usePeriodService hook (Stage 11).
 *
 * Lazily creates a PeriodService backed by the bootstrap database adapter
 * and caches it across renders. Mirrors useMoodService for the other
 * productized features. All period data remains on-device.
 */

import { useState, useEffect } from 'react';
import { getDatabase } from '../../data/database/connection.ts';
import { PeriodRepository } from '../../repositories/periodRepository.ts';
import { PeriodService } from '../../services/period/periodService.ts';

let cachedService: PeriodService | null = null;

async function getPeriodService(): Promise<PeriodService> {
  if (cachedService) return cachedService;
  const db = await getDatabase();
  cachedService = new PeriodService(new PeriodRepository(db));
  return cachedService;
}

export function usePeriodService() {
  const [service, setService] = useState<PeriodService | null>(null);

  useEffect(() => {
    let cancelled = false;
    getPeriodService().then((s) => {
      if (!cancelled) setService(s);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  return service;
}