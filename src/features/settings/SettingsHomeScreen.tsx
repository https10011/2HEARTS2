/**
 * Settings Home (Phase 19 — roadmap screen 80).
 *
 * Hub for all configuration: personal (profile / relationship), app
 * experience (appearance / notifications), privacy & security (app lock),
 * storage, about. Every entry is a real, working capability — no fake
 * settings (roadmap SETTINGS section).
 */

import { useEffect, useState } from 'react';
import { RoutePath } from '../../navigation/routes.ts';
import { IconHeart, IconBell, IconSettings, IconFileText, IconCalendar } from '../../components/index.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { SettingsScreen, SettingRow, InfoCard } from './settingsUi.tsx';

export function SettingsHomeScreen() {
  const [ownerName, setOwnerName] = useState<string>('You');

  useEffect(() => {
    let cancelled = false;
    const relationship = coreServices.relationship;
    if (relationship) {
      relationship
        .getOwner()
        .then((owner) => {
          if (!cancelled && owner?.displayName) setOwnerName(owner.displayName);
        })
        .catch(() => undefined);
    }
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <SettingsScreen title="Settings" backTo={RoutePath.appMore}>
      {/* Profile summary → profile settings */}
      <SettingRow
        to={RoutePath.appMoreSettingsProfile}
        icon={<IconHeart size={20} />}
        label={ownerName}
        description="Your relationship"
      />

      <p className="th-settings-section">Personal</p>
      <div className="th-settings-group">
        <SettingRow
          to={RoutePath.appMoreSettingsProfile}
          icon={<IconFileText size={18} />}
          label="Profile"
          description="Your name and personal details"
        />
        <SettingRow
          to={RoutePath.appMoreSettingsRelationship}
          icon={<IconHeart size={18} />}
          label="Relationship"
          description="Your relationship details and preferences"
        />
      </div>

      <p className="th-settings-section">App Experience</p>
      <div className="th-settings-group">
        <SettingRow
          to={RoutePath.appMoreSettingsAppearance}
          icon={<IconSettings size={18} />}
          label="Appearance"
          description="Theme, text size, and motion preferences"
        />
        <SettingRow
          to={RoutePath.appMoreSettingsNotifications}
          icon={<IconBell size={18} />}
          label="Notifications"
          description="Reminders and notification preferences"
        />
      </div>

      <p className="th-settings-section">Privacy &amp; Security</p>
      <div className="th-settings-group">
        <SettingRow
          to={RoutePath.appMoreSettingsSecurity}
          icon={<IconHeart size={18} />}
          label="Security &amp; App Lock"
          description="Protect TwoHearts with an app lock"
        />
      </div>

      <p className="th-settings-section">Storage</p>
      <div className="th-settings-group">
        <SettingRow
          to={RoutePath.appMoreSettingsStorage}
          icon={<IconFileText size={18} />}
          label="Storage"
          description="Manage local app data and storage"
        />
      </div>

      <p className="th-settings-section">About</p>
      <div className="th-settings-group">
        <SettingRow
          to={RoutePath.appMoreAbout}
          icon={<IconCalendar size={18} />}
          label="About TwoHearts"
          description="Version, information, and acknowledgements"
        />
      </div>

      <div style={{ marginTop: 'var(--th-space-6)' }}>
        <InfoCard
          title="Your data stays with you"
          text="TwoHearts stores your app data locally on this device."
        />
      </div>
    </SettingsScreen>
  );
}
