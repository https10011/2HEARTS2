/**
 * Settings Home (Stage 15 — Settings + App Customization Visual Productization).
 *
 * Hub for all configuration with branded hero, polished profile card,
 * and improved section organization. Architecture unchanged.
 */

import { useEffect, useState } from 'react';
import { RoutePath } from '../../navigation/routes.ts';
import { IconHeart, IconBell, IconSettings, IconFileText, IconLock, IconSearch, IconChevronRight } from '../../components/index.ts';
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
      {/* Branded hero band */}
      <div className="th-settings-hero th-game-enter">
        <div className="th-settings-hero__label">TwoHearts</div>
        <h1 className="th-settings-hero__title">Settings</h1>
        <p className="th-settings-hero__desc">
          Make TwoHearts yours. Everything stays on this device.
        </p>
      </div>

      {/* Profile card */}
      <a
        href={RoutePath.appMoreSettingsProfile}
        className="th-settings-profile th-game-stagger"
        onClick={(e) => { e.preventDefault(); }}
      >
        <span className="th-settings-profile__avatar">
          {ownerName.charAt(0).toUpperCase()}
        </span>
        <span className="th-settings-profile__body">
          <span className="th-settings-profile__name">{ownerName}</span>
          <span className="th-settings-profile__subtitle">Your profile &amp; preferences</span>
        </span>
        <IconChevronRight size={18} className="th-settings-profile__chevron" />
      </a>

      {/* Sections */}
      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Personal
      </div>
      <div className="th-settings-group--enhanced th-game-stagger" style={{ animationDelay: '60ms' }}>
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
          description="Your relationship details"
        />
      </div>

      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Experience
      </div>
      <div className="th-settings-group--enhanced th-game-stagger" style={{ animationDelay: '100ms' }}>
        <SettingRow
          to={RoutePath.appMoreSettingsAppearance}
          icon={<IconSettings size={18} />}
          label="Appearance"
          description="Theme, text size, and motion"
        />
        <SettingRow
          to={RoutePath.appMoreSettingsNotifications}
          icon={<IconBell size={18} />}
          label="Notifications"
          description="Reminders and notification preferences"
        />
      </div>

      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Privacy &amp; Security
      </div>
      <div className="th-settings-group--enhanced th-game-stagger" style={{ animationDelay: '140ms' }}>
        <SettingRow
          to={RoutePath.appMoreSettingsSecurity}
          icon={<IconLock size={18} />}
          label="Security &amp; App Lock"
          description="Protect TwoHearts with an app lock"
        />
      </div>

      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Storage
      </div>
      <div className="th-settings-group--enhanced th-game-stagger" style={{ animationDelay: '180ms' }}>
        <SettingRow
          to={RoutePath.appMoreSettingsStorage}
          icon={<IconFileText size={18} />}
          label="Storage"
          description="Manage local app data"
        />
      </div>

      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        About
      </div>
      <div className="th-settings-group--enhanced th-game-stagger" style={{ animationDelay: '220ms' }}>
        <SettingRow
          to={RoutePath.appMoreAbout}
          icon={<IconSearch size={18} />}
          label="About TwoHearts"
          description="Version and acknowledgements"
        />
      </div>

      <div style={{ marginTop: 'var(--th-space-6)' }}>
        <InfoCard
          title="Your data stays with you"
          text="TwoHearts stores all app data locally on this device. Nothing is sent to a server."
          icon={<IconHeart size={16} />}
        />
      </div>
    </SettingsScreen>
  );
}

