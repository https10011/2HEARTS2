/**
 * CouplePair (Stage 4) — the couple's two avatar circles joined by a heart
 * connector, used wherever the relationship itself is presented (Us hub,
 * Relationship Counter). One shared presentation — not a second avatar system.
 */

import { Link } from 'react-router-dom';
import type { Profile } from '../../data/relationship/relationshipTypes.ts';
import { IconHeart, IconSmile } from '../../components/index.ts';

interface CoupleFaceProps {
  profile: Profile | null;
  fallbackRole: string; // e.g. "You" / "Partner"
  to: string;
  label: string;
}

function CoupleFace({ profile, fallbackRole, to, label }: CoupleFaceProps) {
  const name = profile?.displayName?.trim() ?? '';
  return (
    <Link to={to} className="th-couple-pair__face" aria-label={label}>
      <span className="th-couple-pair__circle">
        {name ? (
          <span className="th-couple-pair__initial">{name.charAt(0).toUpperCase()}</span>
        ) : (
          <IconSmile size={26} />
        )}
      </span>
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
  return (
    <div className="th-couple-pair">
      <CoupleFace profile={owner} fallbackRole="You" to={ownerTo} label="Your profile" />
      <span className="th-couple-pair__heart" aria-hidden="true">
        <IconHeart size={16} />
      </span>
      <CoupleFace profile={partner} fallbackRole="Partner" to={partnerTo} label="Partner profile" />
    </div>
  );
}
