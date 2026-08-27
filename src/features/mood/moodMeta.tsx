/**
 * Mood visual identity (Stage 10).
 *
 * Maps each approved mood value to its centralized SVG icon — replacing
 * the emoji wall with the app's own expressive, mature icon language.
 * Icons come from the single shared icon system in components/Icon.tsx.
 */

import type { ReactNode } from 'react';
import type { MoodValue } from '../../data/mood/moodTypes.ts';
import {
  IconFrown,
  IconHeart,
  IconLotus,
  IconMeh,
  IconMoon,
  IconPulse,
  IconSmile,
  IconSparkle,
  IconSun,
  IconSwirl,
} from '../../components/index.ts';

const MOOD_ICONS: Record<MoodValue, (size: number) => ReactNode> = {
  happy: (size) => <IconSmile size={size} />,
  love: (size) => <IconHeart size={size} />,
  excited: (size) => <IconSparkle size={size} />,
  calm: (size) => <IconLotus size={size} />,
  grateful: (size) => <IconSun size={size} />,
  neutral: (size) => <IconMeh size={size} />,
  tired: (size) => <IconMoon size={size} />,
  sad: (size) => <IconFrown size={size} />,
  anxious: (size) => <IconSwirl size={size} />,
  stressed: (size) => <IconPulse size={size} />,
};

/** Renders the centralized icon for a mood value. */
export function MoodIcon({ mood, size = 22 }: { mood: MoodValue; size?: number }) {
  return <>{MOOD_ICONS[mood](size)}</>;
}
