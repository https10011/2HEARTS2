/**
 * YukiCharacter (Stage 8).
 *
 * Renders the Yuki cat character with:
 * - Activity-based CSS animation classes
 * - Mood-based expression via speech bubble
 * - Accessory overlay
 * - Heart particles for petting
 * - ZZZ particles for sleeping
 * - Level-up celebration overlay
 */

import type { YukiActivity, YukiMood } from '../../data/game/yukiTypes.ts';
import { MOOD_DESCRIPTIONS } from '../../data/game/yukiTypes.ts';
import { getAccessoryById } from '../../data/game/yukiTypes.ts';
import yukiCatSvg from '../../assets/yuki/yuki-cat.svg';

interface YukiCharacterProps {
  activity: YukiActivity;
  mood: YukiMood;
  accessory: string | null;
  showLevelUp: boolean;
  levelUpLevel: number;
}

export function YukiCharacter({
  activity,
  mood,
  accessory,
  showLevelUp,
  levelUpLevel,
}: YukiCharacterProps) {
  const accessoryData = getAccessoryById(accessory);

  return (
    <div className="yuki-character-container">
      {/* Activity-based animation wrapper */}
      <div className={`yuki-character yuki-character--${activity}`}>
        <img
          className="yuki-character__img"
          src={yukiCatSvg}
          alt={`Yuki the cat — ${MOOD_DESCRIPTIONS[mood]}`}
          draggable={false}
          width={160}
          height={160}
        />

        {/* Accessory overlay */}
        {accessoryData && accessoryData.visual && (
          <span className="yuki-accessory-overlay" aria-hidden="true">
            {accessoryData.visual}
          </span>
        )}

        {/* Heart particles for petting — CSS-drawn, no emoji */}
        {activity === 'being-petted' && (
          <div className="yuki-hearts" aria-hidden="true">
            <span className="yuki-heart yuki-heart--1" />
            <span className="yuki-heart yuki-heart--2" />
            <span className="yuki-heart yuki-heart--3" />
          </div>
        )}

        {/* ZZZ for sleeping */}
        {activity === 'sleeping' && (
          <div className="yuki-zzz" aria-hidden="true">
            <span>z</span>
            <span>z</span>
            <span>z</span>
          </div>
        )}
      </div>

      {/* Mood speech bubble */}
      <div className="yuki-mood-bubble" role="status" aria-live="polite">
        {MOOD_DESCRIPTIONS[mood]}
      </div>

      {/* Level-up overlay */}
      {showLevelUp && (
        <div className="yuki-levelup-overlay" role="alert" aria-live="assertive">
          <div className="yuki-levelup-text">
            Level {levelUpLevel}!
          </div>
        </div>
      )}
    </div>
  );
}
