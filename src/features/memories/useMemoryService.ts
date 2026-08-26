/**
 * useMemoryService hook (Phase 7).
 *
 * Lazily creates a MemoryService instance from the bootstrap database
 * adapter and filesystem adapter. Caches the instance across renders.
 */

import { useState, useEffect } from 'react';
import { getDatabase } from '../../data/database/connection.ts';
import { resolveMediaFileSystem } from '../../data/media/resolveMediaFileSystem.ts';
import { MemoryService } from '../../services/memory/memoryService.ts';

let cachedService: MemoryService | null = null;

async function getMemoryService(): Promise<MemoryService> {
  if (cachedService) return cachedService;
  const db = await getDatabase();
  const fs = await resolveMediaFileSystem();
  cachedService = new MemoryService(db, fs);
  return cachedService;
}

export function useMemoryService() {
  const [service, setService] = useState<MemoryService | null>(null);

  useEffect(() => {
    let cancelled = false;
    getMemoryService().then((s) => {
      if (!cancelled) setService(s);
    });
    return () => { cancelled = true; };
  }, []);

  return service;
}
