/**
 * useReminderService hook (Phase 22).
 *
 * Lazily creates the shared ReminderService from the bootstrap database
 * adapter, wiring the bootstrap NotificationService when that
 * (non-critical) stage is available so reminder notifications are
 * actually scheduled/cancelled. Mirrors the established useMemoryService /
 * useVaultService pattern: returns null until resolved.
 */

import { useState, useEffect } from 'react';
import { getDatabase } from '../../data/database/connection.ts';
import { ReminderRepository } from '../../repositories/reminderRepository.ts';
import { ReminderService } from '../../services/reminder/reminderService.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';

let cachedService: ReminderService | null = null;

async function getReminderService(): Promise<ReminderService> {
  if (cachedService) return cachedService;
  const db = await getDatabase();
  cachedService = new ReminderService(
    new ReminderRepository(db),
    coreServices.notifications ?? null,
  );
  return cachedService;
}

export function useReminderService(): ReminderService | null {
  const [service, setService] = useState<ReminderService | null>(cachedService);

  useEffect(() => {
    let cancelled = false;
    getReminderService().then((s) => {
      if (!cancelled) setService(s);
    });
    return () => { cancelled = true; };
  }, []);

  return service;
}
