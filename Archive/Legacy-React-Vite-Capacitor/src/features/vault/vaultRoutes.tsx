/**
 * Vault route wrappers (Phase 21).
 *
 * The router instance is created at module scope — before bootstrap
 * completes — so route elements must resolve core services at RENDER time,
 * never at router-creation time. These wrappers inject the AppLockService
 * and VaultService into the vault screens, and degrade to a calm
 * unavailable state (instead of crashing) if the non-critical app-lock
 * bootstrap stage failed: a vault without its lock service stays closed.
 */

import { EmptyState } from '../../components/index.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { VaultEntry } from './VaultEntry.tsx';
import { AddVaultContent } from './AddVaultContent.tsx';
import { VaultContentViewer } from './VaultContentViewer.tsx';
import { useVaultService } from './useVaultService.ts';

function VaultUnavailable() {
  return (
    <div className="th-content-pad">
      <EmptyState
        title="Vault unavailable"
        description="The vault lock could not be started. Please restart the app and try again."
      />
    </div>
  );
}

export function VaultEntryRoute() {
  const appLock = coreServices.appLock;
  const vaultService = useVaultService();
  if (!appLock) return <VaultUnavailable />;
  return <VaultEntry appLockService={appLock} vaultService={vaultService ?? undefined} />;
}

export function AddVaultContentRoute() {
  const vaultService = useVaultService();
  if (!coreServices.appLock) return <VaultUnavailable />;
  return <AddVaultContent service={vaultService ?? undefined} />;
}

export function VaultContentViewerRoute() {
  const vaultService = useVaultService();
  if (!coreServices.appLock) return <VaultUnavailable />;
  return <VaultContentViewer service={vaultService ?? undefined} />;
}
