/**
 * Vault presentation helpers (Stage 12).
 *
 * Pure, testable functions for Vault UI formatting.
 * No React, no DOM, no service imports.
 */

import type { VaultContentType } from '../../data/vault/vaultTypes.ts';

// ---------------------------------------------------------------------------
// Content type display helpers
// ---------------------------------------------------------------------------

/** Map content type to a human-readable security label. */
export function securityLabel(type: VaultContentType): string {
  switch (type) {
    case 'note': return 'Private note';
    case 'photo': return 'Protected photo';
    case 'video': return 'Protected video';
    case 'file': return 'Secured file';
    default: return 'Vault item';
  }
}

/** Friendly singular count text. */
export function itemCountText(count: number): string {
  if (count === 0) return 'No items';
  if (count === 1) return '1 item';
  return `${count} items`;
}

/** Format a vault item date for display. */
export function formatVaultDate(isoString: string): string {
  try {
    const d = new Date(isoString);
    if (isNaN(d.getTime())) return '';
    return d.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  } catch {
    return '';
  }
}

/** Relative time: "Today", "Yesterday", or formatted date. */
export function relativeVaultDate(isoString: string): string {
  try {
    const d = new Date(isoString);
    if (isNaN(d.getTime())) return '';
    const now = new Date();
    const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const startOfItem = new Date(d.getFullYear(), d.getMonth(), d.getDate());
    const diffMs = startOfToday.getTime() - startOfItem.getTime();
    const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24));
    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    return formatVaultDate(isoString);
  } catch {
    return '';
  }
}

/** Content type filter option with label. */
export interface FilterOption {
  value: VaultContentType | 'all';
  label: string;
}

export const VAULT_FILTER_OPTIONS: FilterOption[] = [
  { value: 'all', label: 'All' },
  { value: 'note', label: 'Notes' },
  { value: 'photo', label: 'Photos' },
  { value: 'video', label: 'Videos' },
  { value: 'file', label: 'Files' },
];
