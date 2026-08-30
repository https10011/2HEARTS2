/**
 * Centralized meta for Vault content types (Phase 23 icon consolidation).
 * All vault screens import from here instead of emoji strings.
 */

import type { ComponentType } from 'react';
import {
  IconImage,
  IconVideo,
  IconFileText,
  IconFile,
  type IconProps,
} from '../../components/index.ts';
import type { VaultContentType } from '../../data/vault/vaultTypes.ts';

export interface ContentTypeMeta {
  label: string;
  Icon: ComponentType<IconProps>;
  description: string;
}

export const CONTENT_TYPE_META: Record<VaultContentType, ContentTypeMeta> = {
  note: { label: 'Private Note', Icon: IconFileText, description: 'Text content stored securely' },
  photo: { label: 'Photo', Icon: IconImage, description: 'Protected photo (coming soon)' },
  video: { label: 'Video', Icon: IconVideo, description: 'Protected video (coming soon)' },
  file: { label: 'File', Icon: IconFile, description: 'Any file type (coming soon)' },
};

export const CONTENT_TYPE_ORDER: VaultContentType[] = ['note', 'photo', 'video', 'file'];
