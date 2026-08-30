/**
 * usePlaceService hook (Stage 9).
 *
 * Lazily creates a PlaceService backed by the bootstrap database adapter
 * and the shared local MediaStorage boundary (place photos stay on-device).
 * Caches the instance across renders.
 */

import { useState, useEffect } from 'react';
import { getDatabase } from '../../data/database/connection.ts';
import { MediaStorage } from '../../data/media/mediaStorage.ts';
import { resolveMediaFileSystem } from '../../data/media/resolveMediaFileSystem.ts';
import { PlaceRepository } from '../../repositories/placeRepository.ts';
import { PlaceService } from '../../services/place/placeService.ts';

let cachedService: PlaceService | null = null;

async function getPlaceService(): Promise<PlaceService> {
  if (cachedService) return cachedService;
  const db = await getDatabase();
  const fs = await resolveMediaFileSystem();
  cachedService = new PlaceService(new PlaceRepository(db), new MediaStorage(db, fs));
  return cachedService;
}

export function usePlaceService() {
  const [service, setService] = useState<PlaceService | null>(null);

  useEffect(() => {
    let cancelled = false;
    getPlaceService().then((s) => {
      if (!cancelled) setService(s);
    });
    return () => { cancelled = true; };
  }, []);

  return service;
}
