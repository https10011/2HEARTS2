/**
 * VaultEntry — lock/unlock lifecycle controller (Stage 12).
 *
 * Manages the vault access flow: locked state → PIN entry → unlocked state.
 * Subscribes to AppLockService for reactive lock state changes.
 * Uses VaultService.unlock for PIN verification.
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
    // Mark the vault as accessible in the current session
    if (appLockService.currentState() === 'unlocked') {
      setLockState('unlocked');
    }
  }, [appLockService]);

  const handleVerifyPin = useCallback(async (pin: string): Promise<boolean> => {
    return vaultService?.unlock(pin) ?? false;
  }, [vaultService]);

  // If lock is disabled or unlocked, show vault content
  if (lockState === 'disabled' || lockState === 'unlocked') {
    return <VaultHome service={vaultService} />;
  }

  // Show locked screen
  return <VaultLocked onUnlock={handleUnlock} verifyPin={handleVerifyPin} />;
}
