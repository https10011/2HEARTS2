/**
 * Nav icon bridge (Phase 24) — resolves the string icon keys declared in
 * navConfig.ts to the centralized Icon set (components/Icon.tsx).
 *
 * One renderer for every navigation surface (bottom nav, Home cards, couple
 * hub, More menu) so iconography stays consistent and the design-token guard
 * can prove no feature renders ad-hoc SVG or emoji glyphs.
 */

import type { NavIconKey } from './navConfig.ts';
import {
  IconHome,
  IconBell,
  IconFileText,
  IconMenu,
  IconGamepad,
  IconHeart,
  IconCalendar,
  IconMapPin,
  IconSmile,
  IconLock,
  IconSettings,
  IconSearch,
  IconCamera,
  IconCat,
  type IconProps,
} from '../../components/index.ts';

const NAV_ICONS: Record<NavIconKey, (props: IconProps) => JSX.Element> = {
  home: IconHome,
  bell: IconBell,
  'file-text': IconFileText,
  menu: IconMenu,
  gamepad: IconGamepad,
  heart: IconHeart,
  calendar: IconCalendar,
  'map-pin': IconMapPin,
  smile: IconSmile,
  lock: IconLock,
  settings: IconSettings,
  search: IconSearch,
  camera: IconCamera,
  cat: IconCat,
};

/** All icon keys guaranteed to resolve (compile-time + test assertion). */
export const NAV_ICON_KEYS = Object.keys(NAV_ICONS) as NavIconKey[];

export function NavIcon({ icon, size = 22 }: { icon: NavIconKey; size?: number }) {
  const Icon = NAV_ICONS[icon];
  return <Icon size={size} />;
}
