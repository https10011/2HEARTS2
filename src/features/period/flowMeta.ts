/**
 * Centralized period-flow metadata (Phase 23 icon consolidation).
 * Flow levels use a semantic color scale instead of emoji glyphs.
 */

import type { FlowLevel } from '../../data/period/periodTypes.ts';

export interface FlowMeta {
  label: string;
  colorVar: string;
}

export const FLOW_META: Record<FlowLevel, FlowMeta> = {
  light: { label: 'Light', colorVar: 'var(--th-color-blush)' },
  medium: { label: 'Medium', colorVar: 'var(--th-color-rose-muted)' },
  heavy: { label: 'Heavy', colorVar: 'var(--th-color-burgundy)' },
};

export const FLOW_ORDER: FlowLevel[] = ['light', 'medium', 'heavy'];
