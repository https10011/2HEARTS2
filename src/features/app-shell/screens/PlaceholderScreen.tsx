/**
 * PlaceholderScreen (Phase 6).
 *
 * Generic placeholder for feature sub-routes that will be implemented
 * in their designated phases. Shows a consistent "coming soon" message
 * with an appropriate icon.
 */

import { useNavigate } from 'react-router-dom';
import { IconButton, IconBack } from '../../../components/index.ts';

interface PlaceholderScreenProps {
  title: string;
  description: string;
}

export function PlaceholderScreen({ title, description }: PlaceholderScreenProps) {
  const navigate = useNavigate();

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100%' }}>
      {/* Simple header with back */}
      <header className="th-app-header">
        <div style={{ display: 'flex', alignItems: 'center', minWidth: 'var(--th-touch-target-min)' }}>
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            <IconBack />
          </IconButton>
        </div>
        <h1 className="th-app-header__title">{title}</h1>
        <div style={{ minWidth: 'var(--th-touch-target-min)' }} />
      </header>

      {/* Content */}
      <div className="th-placeholder">
        <div className="th-placeholder__icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <circle cx="12" cy="12" r="10" />
            <path d="M12 8v4M12 16h.01" />
          </svg>
        </div>
        <div className="th-placeholder__title">{title}</div>
        <div className="th-placeholder__desc">{description}</div>
      </div>
    </div>
  );
}
