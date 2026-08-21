/**
 * About TwoHearts (Phase 19 — roadmap screen 87 area).
 *
 * Shows real application/version information (mirrored from
 * capacitor.config.ts + package.json via src/config/appInfo.ts), a short
 * description, what the app includes (features that actually exist), and
 * the local-first privacy commitment. No hardcoded marketing claims, no
 * fake acknowledgements.
 */

import { RoutePath } from '../../navigation/routes.ts';
import { IconHeart, BrandLogo } from '../../components/index.ts';
import { APP_INFO } from '../../config/appInfo.ts';
import { SettingsScreen, InfoCard } from './settingsUi.tsx';

const FEATURES = [
  'Memories and milestone timeline',
  'Notes and love letters',
  'Anniversaries, important dates, and local reminders',
  'Games to play together',
  'Mood check-ins and period tracking',
  'A private, lockable vault',
];

export function AboutScreen() {
  return (
    <SettingsScreen title="About TwoHearts" backTo={RoutePath.appMore}>
      <div style={{ textAlign: 'center', margin: 'var(--th-space-4) 0' }}>
        <div style={{ margin: '0 auto var(--th-space-3)', width: 'fit-content' }}>
          {/* Official logo — one BrandLogo component, one asset (Phase 23) */}
          <BrandLogo variant="brand" size={160} title="TwoHearts" />
        </div>
        <p style={{ margin: 'var(--th-space-1) 0 0', color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)' }}>
          Version {APP_INFO.version}
        </p>
      </div>

      <p style={{ color: 'var(--th-color-text-secondary)', fontSize: 'var(--th-font-size-sm)', lineHeight: 'var(--th-line-height-relaxed)' }}>
        A private space for two people — to keep memories, milestones, notes, and small everyday
        moments together.
      </p>

      <p className="th-settings-section">What&apos;s Inside</p>
      <div className="th-settings-group">
        {FEATURES.map((feature) => (
          <div key={feature} className="th-settings-row th-settings-row--static">
            <span className="th-settings-row__icon">
              <IconHeart size={16} />
            </span>
            <span className="th-settings-row__body">
              <span className="th-settings-row__label">{feature}</span>
            </span>
          </div>
        ))}
      </div>

      <div style={{ marginTop: 'var(--th-space-6)' }}>
        <InfoCard
          title="Private by design"
          text="TwoHearts keeps your data on this device — no accounts, no cloud sync, no tracking."
        />
      </div>

      <p
        style={{
          marginTop: 'var(--th-space-6)',
          textAlign: 'center',
          fontSize: 'var(--th-font-size-xs)',
          color: 'var(--th-color-text-secondary)',
        }}
      >
        {APP_INFO.appId} · Made for the two of you
      </p>
    </SettingsScreen>
  );
}
