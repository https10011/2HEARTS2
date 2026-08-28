/**
 * Notification Settings (Stage 15 — Settings + App Customization).
 *
 * LOCAL notifications only — V1 has no push/FCM. Architecture unchanged.
 */

import { useEffect, useState } from 'react';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, IconBell } from '../../components/index.ts';
import { appSettingsStore, useAppSettings } from '../../core/appSettings.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { PermissionService, type PermissionState } from '../../services/permissions/permissionService.ts';
import { SettingsScreen, SettingSwitchRow, InfoCard } from './settingsUi.tsx';
import { permissionStatusLabel, permissionStatusDescription } from './settingsPresentation.ts';

const permissions = new PermissionService();

export function NotificationSettingsScreen() {
  const settings = useAppSettings();
  const [deviceState, setDeviceState] = useState<PermissionState>('unavailable');

  useEffect(() => {
    let cancelled = false;
    permissions
      .check('notifications')
      .then((state) => {
        if (!cancelled) setDeviceState(state);
      })
      .catch(() => undefined);
    return () => {
      cancelled = true;
    };
  }, []);

  const requestPermission = async () => {
    const state = await permissions.ensure('notifications');
    setDeviceState(state);
  };

  const cancelAllScheduled = async () => {
    const notifications = coreServices.notifications;
    if (notifications) await notifications.cancelAll().catch(() => undefined);
  };

  const cancelReminderScheduled = async () => {
    const notifications = coreServices.notifications;
    if (!notifications) return;
    try {
      const entries = await notifications.list();
      for (const entry of entries) {
        if (entry.channelId === 'reminders') {
          await notifications.cancelById(entry.notificationId);
        }
      }
    } catch {
      // Best-effort
    }
  };

  const toggleMaster = (next: boolean) => {
    appSettingsStore.setNotificationsEnabled(next);
    if (!next) void cancelAllScheduled();
  };

  const toggleReminders = (next: boolean) => {
    appSettingsStore.setRemindersEnabled(next);
    if (!next) void cancelReminderScheduled();
  };

  return (
    <SettingsScreen title="Notifications" backTo={RoutePath.appMoreSettings}>
      {/* Master toggle */}
      <div className="th-settings-group--enhanced">
        <SettingSwitchRow
          icon={<IconBell size={18} />}
          label="Notifications"
          description="Allow TwoHearts to send local reminders"
          checked={settings.notificationsEnabled}
          onChange={toggleMaster}
        />
      </div>

      {/* Reminders */}
      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Reminders
      </div>
      <div className="th-settings-group--enhanced">
        <SettingSwitchRow
          icon={<IconBell size={18} />}
          label="Reminder Notifications"
          description="Get reminded about reminders you've scheduled"
          checked={settings.remindersEnabled}
          onChange={toggleReminders}
          disabled={!settings.notificationsEnabled}
        />
      </div>

      {/* Privacy note */}
      <div style={{ marginTop: 'var(--th-space-4)' }}>
        <InfoCard
          title="Private by design"
          text="Private Vault content never appears in notifications. All reminders are scheduled locally — nothing is sent to a server."
          icon={<IconBell size={16} />}
        />
      </div>

      {/* Device status */}
      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Device
      </div>
      <div className="th-settings-group--enhanced">
        <div className="th-settings-row--stage15 th-settings-row--stage15--static">
          <span className="th-settings-row--stage15__icon">
            <IconBell size={18} />
          </span>
          <span className="th-settings-row__body">
            <span className="th-settings-row__label">Device Notifications</span>
            <span className="th-settings-row__description">
              {permissionStatusDescription(deviceState)}
            </span>
          </span>
          <span className={`th-settings-status th-settings-status--${deviceState}`}>
            {permissionStatusLabel(deviceState)}
          </span>
        </div>
      </div>

      {deviceState === 'prompt' ? (
        <div style={{ marginTop: 'var(--th-space-3)' }}>
          <Button variant="secondary" full onClick={() => void requestPermission()}>
            Allow Notifications
          </Button>
        </div>
      ) : null}
    </SettingsScreen>
  );
}
