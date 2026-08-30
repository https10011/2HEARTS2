/**
 * Category → icon mapping for the Notes experience (Stage 6).
 *
 * One icon per category — drives the circular card badge and the
 * detail-screen category label. Typed as Record<NoteCategory, …> so
 * TypeScript enforces completeness when categories change. Kept out
 * of categoryMeta.ts because Node tests cannot load .tsx modules.
 */

import type { ComponentType } from 'react';
import {
  IconBell,
  IconCheck,
  IconFile,
  IconFileText,
  IconHeart,
  IconLock,
  IconSmile,
  type IconProps,
} from '../../components/Icon.tsx';
import type { NoteCategory } from '../../data/note/noteTypes.ts';

export const NOTE_CATEGORY_ICONS: Record<NoteCategory, ComponentType<IconProps>> = {
  general: IconFileText,
  shared: IconSmile,
  private: IconLock,
  'love-letter': IconHeart,
  gratitude: IconCheck,
  idea: IconFile,
  reminder: IconBell,
};
