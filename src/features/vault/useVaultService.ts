/**
 * useVaultService hook (Phase 21).
 *
 * Lazily creates the shared VaultService from the bootstrap database
 * adapter and the bootstrap AppLockService, mirroring the established
 * useMemoryService pattern. Returns null until resolved — and stays null
 * when the app-lock stage is unavailable, in which case the vault must
 * remain closed.
 */

import { useState, useEffect } from 'react';
import { getDatabase } from '../../data/database/connection.ts';
import { VaultRepository } from '../../repositories/vaultRepository.ts';
import { VaultService } from '../../services/vault/vaultService.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';

let cachedService: VaultService | null = null;

async function getVaultService(): Promise<VaultService | null> {
  const appLock = coreServices.appLock;
  if (!appLock) return null;
  if (cachedService) return cachedService;
  const db = await getDatabase();
  cachedService = new VaultService(new VaultRepository(db), appLock);
  return cachedService;
}

export function useVaultService(): VaultService | null {
  const [service, setService] = useState<VaultService | null>(cachedService);

  useEffect(() => {
    let cancelled = false;
    getVaultService().then((s) => {
      if (!cancelled) setService(s);
    });
    return () => { cancelled = true; };
  }, []);

  return service;
}
