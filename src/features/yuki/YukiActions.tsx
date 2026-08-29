/**
 * YukiActions (Stage 8).
 *
 * The action bar for interacting with Yuki.
 * Four actions: Feed, Pet, Play, Clean.
 * Each action has an icon, label, and visual feedback.
 * Buttons are disabled during activity animations.
 */

import type { YukiAction } from '../../data/game/yukiTypes.ts';

interface YukiActionsProps {
  onAction: (action: YukiAction) => void;
  disabled: boolean;
  activeAction: YukiAction | null;
}

import { IconHeart, IconSparkle, IconSmile, IconLotus } from '../../components/index.ts';
import type { IconProps } from '../../components/index.ts';

interface ActionDef {
  id: YukiAction;
  IconComponent: (props: IconProps) => JSX.Element;
  label: string;
}

const ACTIONS: ActionDef[] = [
  { id: 'feed', IconComponent: IconHeart, label: 'Feed' },
  { id: 'pet', IconComponent: IconSparkle, label: 'Pet' },
  { id: 'play', IconComponent: IconSmile, label: 'Play' },
  { id: 'clean', IconComponent: IconLotus, label: 'Clean' },
];

export function YukiActions({ onAction, disabled, activeAction }: YukiActionsProps) {
  return (
    <div
      className="yuki-actions"
      role="toolbar"
      aria-label="Interact with Yuki"
    >
      {ACTIONS.map((action) => (
        <button
          key={action.id}
          className={`yuki-action-btn ${
            activeAction === action.id ? 'yuki-action-btn--active' : ''
          } ${disabled && activeAction !== action.id ? 'yuki-action-btn--disabled' : ''}`}
          onClick={() => onAction(action.id)}
          disabled={disabled}
          aria-label={`${action.label} Yuki`}
          type="button"
        >
          <span className="yuki-action-btn__icon" aria-hidden="true">
            <action.IconComponent size={22} />
          </span>
          <span className="yuki-action-btn__label">{action.label}</span>
        </button>
      ))}
    </div>
  );
}
