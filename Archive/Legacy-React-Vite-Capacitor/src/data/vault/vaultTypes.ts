/**
 * Vault domain models + serializers (Phase 17).
 *
 * The Private Vault stores protected content locally. Vault metadata lives
 * in SQLite; protected media bytes live on the device filesystem with safe
 * references. Vault content must NOT appear in global search, notifications,
 * or home previews.
 *
 * Conventions:
 * - `title`       — user-provided title.
 * - `contentType` — 'photo', 'video', 'note', or 'file'.
 * - `mediaRef`    — optional safe media reference id (from media_assets).
 * - `filePath`    — relative path in the vault media directory.
 * - `content`     — optional text content for note-type vault items.
 * - `createdAt/updatedAt` — UTC ISO 8601 (Entity standard).
 * - `deletedAt`   — tombstone for soft deletes.
 */

import { assertEntityConventions, type TombstonedEntity } from '../model/entity.ts';
import {
  optionalString,
  requireEnum,
  requireString,
  type EntitySerializer,
  type Params,
  type Row,
} from '../serialization/entitySerializer.ts';

// ---------------------------------------------------------------------------
// Vault content types
// ---------------------------------------------------------------------------

export const VAULT_CONTENT_TYPES = ['photo', 'video', 'note', 'file'] as const;
export type VaultContentType = (typeof VAULT_CONTENT_TYPES)[number];

// ---------------------------------------------------------------------------
// Vault item
// ---------------------------------------------------------------------------

export interface VaultItem extends TombstonedEntity {
  /** User-provided title. */
  title: string;
  /** Content type. */
  contentType: VaultContentType;
  /** Safe media reference id (from media_assets table). */
  mediaRef: string | null;
  /** Relative file path in the vault directory. */
  filePath: string | null;
  /** Text content for note-type items. */
  content: string | null;
  /** Optional description/notes. */
  description: string | null;
  /** Profile id of the vault owner. */
  profileId: string;
}

export type NewVaultItem = Omit<VaultItem, 'id' | 'createdAt' | 'updatedAt' | 'deletedAt'>;

export function assertVaultItem(item: VaultItem): void {
  assertEntityConventions(item);
  if (item.title.trim().length === 0) {
    throw new Error('vaultItem.title must not be empty');
  }
  if (!VAULT_CONTENT_TYPES.includes(item.contentType)) {
    throw new Error(`contentType must be one of: ${VAULT_CONTENT_TYPES.join(', ')}`);
  }
  if (!item.profileId) {
    throw new Error('vaultItem.profileId is required');
  }
}

// ---------------------------------------------------------------------------
// Serializer
// ---------------------------------------------------------------------------

export const VAULT_ITEM_COLUMNS = [
  'id',
  'title',
  'content_type',
  'media_ref',
  'file_path',
  'content',
  'description',
  'profile_id',
  'created_at',
  'updated_at',
  'deleted_at',
] as const;

export const vaultItemSerializer: EntitySerializer<VaultItem> = {
  columns: VAULT_ITEM_COLUMNS,
  toParams(item: VaultItem): Params {
    return [
      item.id,
      item.title,
      item.contentType,
      item.mediaRef,
      item.filePath,
      item.content,
      item.description,
      item.profileId,
      item.createdAt,
      item.updatedAt,
      item.deletedAt,
    ];
  },
  fromRow(row: Row): VaultItem {
    return {
      id: requireString(row, 'id'),
      title: requireString(row, 'title'),
      contentType: requireEnum(row, 'content_type', VAULT_CONTENT_TYPES),
      mediaRef: optionalString(row, 'media_ref'),
      filePath: optionalString(row, 'file_path'),
      content: optionalString(row, 'content'),
      description: optionalString(row, 'description'),
      profileId: requireString(row, 'profile_id'),
      createdAt: requireString(row, 'created_at'),
      updatedAt: requireString(row, 'updated_at'),
      deletedAt: optionalString(row, 'deleted_at'),
    };
  },
};
