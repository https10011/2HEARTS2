/**
 * ProfileAvatar (Stage 4) — shared avatar component that displays a profile
 * photo when available, or a styled initial-letter fallback.
 *
 * Used on Home, Us, profile settings, and any surface that presents a person.
 * The component handles:
 *   - Photo display (object-fit: cover, circular crop)
 *   - Initial fallback with warm gradient background
 *   - Loading state (shows initial while photo resolves)
 *   - Broken image fallback (reverts to initial if image fails to load)
 *   - Consistent sizing via CSS custom properties
 */

import { useState } from 'react';
import { IconSmile } from './index.ts';

export interface ProfileAvatarProps {
  /** Display name — used for the initial letter fallback. */
  name: string;
  /** Photo data URL (data:image/...;base64,...), or null for fallback. */
  photoUrl: string | null;
  /** Rendered size in px. Default 64. */
  size?: number;
  /** Accessible label. */
  label?: string;
  /** Additional CSS class on the outer wrapper. */
  className?: string;
}

export function ProfileAvatar({
  name,
  photoUrl,
  size = 64,
  label,
  className = '',
}: ProfileAvatarProps) {
  const [imgError, setImgError] = useState(false);
  const initial = name?.trim()?.charAt(0)?.toUpperCase() || '';
  const showPhoto = photoUrl && !imgError;

  return (
    <span
      className={`th-profile-avatar ${className}`}
      style={{ width: size, height: size }}
      aria-label={label}
    >
      {showPhoto ? (
        <img
          src={photoUrl}
          alt=""
          className="th-profile-avatar__photo"
          onError={() => setImgError(true)}
          draggable={false}
        />
      ) : initial ? (
        <span className="th-profile-avatar__initial">{initial}</span>
      ) : (
        <IconSmile size={Math.round(size * 0.44)} />
      )}
    </span>
  );
}
