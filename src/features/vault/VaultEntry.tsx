/**
 * VaultEntry (Phase 17).
 *
 * Main vault component that manages the lock/unlock lifecycle.
 * When locked, shows VaultLocked. When unlocked, shows VaultHome.
 * Integrates with AppLockService for PIN-based access control.
 */

import { useState, useEffect, useCallback } from 'react';
import { VaultService } from '../../services/vault/vaultService.ts';
import { VaultLocked } from './VaultLocked.tsx';
import { VaultHome } from './VaultHome.tsx';
import type { AppLockService, LockState } from '../../services/security/appLockService.ts';

interface VaultEntryProps {
  appLockService: AppLockService;
  vaultService?: VaultService;
}

export function VaultEntry({ appLockService, vaultService }: VaultEntryProps) {
  const [lockState, setLockState] = useState<LockState>(appLockService.currentState());

  // Subscribe to lock state changes
  useEffect(() => {
    const unsub = appLockService.onLockChange((state) => {
      setLockState(state);
    });
    return unsub;
  }, [appLockService]);

  const handleUnlock = useCallback(async () => {
    // The VaultLocked component handles PIN entry and calls AppLockService.unlock()
    // This callback is invoked on successful unlock
    if (appLockService.currentState() === 'unlocked') {
      setLockState('unlocked');
    }
  }, [appLockService]);

  // If lock is disabled or unlocked, show vault content
  if (lockState === 'disabled' || lockState === 'unlocked') {
    return <VaultHome service={vaultService} />;
  }

  // Show locked screen
  return <VaultLocked onUnlock={handleUnlock} />;
}
