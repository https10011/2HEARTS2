/**
 * CouplePair (Stage 4) — the couple's two avatar circles joined by a heart
 * connector, used wherever the relationship itself is presented (Us hub,
 * Relationship Counter). One shared presentation — not a second avatar system.
 */

import { Link } from 'react-router-dom';
import type { Profile } from '../../data/relationship/relationshipTypes.ts';
import { IconHeart, ProfileAvatar } from '../../components/index.ts';
import { useProfilePhotos } from './useProfilePhotos.ts';

interface CoupleFaceProps {
  profile: Profile | null;
  fallbackRole: string; // e.g. "You" / "Partner"
  to: string;
  label: string;
  photoUrl: string | null;
}

function CoupleFace({ profile, fallbackRole, to, label, photoUrl }: CoupleFaceProps) {
  const name = profile?.displayName?.trim() ?? '';
  return (
    <Link to={to} className="th-couple-pair__face" aria-label={label}>
      <ProfileAvatar
        name={name || fallbackRole}
        photoUrl={photoUrl}
        size={56}
      />
      <span className="th-couple-pair__name">{name || fallbackRole}</span>
    </Link>
  );
}

interface CouplePairProps {
  owner: Profile | null;
  partner: Profile | null;
  ownerTo: string;
  partnerTo: string;
}

export function CouplePair({ owner, partner, ownerTo, partnerTo }: CouplePairProps) {
  const { ownerUrl, partnerUrl } = useProfilePhotos();

  return (
    <div className="th-couple-pair">
      <CoupleFace profile={owner} fallbackRole="You" to={ownerTo} label="Your profile" photoUrl={ownerUrl} />
      <span className="th-couple-pair__heart" aria-hidden="true">
        <IconHeart size={16} />
      </span>
      <CoupleFace profile={partner} fallbackRole="Partner" to={partnerTo} label="Partner profile" photoUrl={partnerUrl} />
    </div>
  );
}
