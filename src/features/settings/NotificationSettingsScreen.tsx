/**
 * Notification Settings (Phase 19 — roadmap screen 84).
 *
 * LOCAL notifications only — V1 has no push/FCM. The master switch and the
 * reminders category switch persist in appSettings and gate the existing
 * NotificationService/ReminderService scheduling path; turning a switch
 * off also cancels already-scheduled notifications so nothing stale fires.
 * Device permission status is reported honestly (never claimed delivered).
 */

import { useEffect, useState } from 'react';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, IconBell } from '../../components/index.ts';
import { appSettingsStore, useAppSettings } from '../../core/appSettings.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { PermissionService, type PermissionState } from '../../services/permissions/permissionService.ts';
import { SettingsScreen, SettingSwitchRow, InfoCard } from './settingsUi.tsx';

const permissions = new PermissionService();

const PERMISSION_LABEL: Record<PermissionState, string> = {
  granted: 'Allowed',
  denied: 'Denied',
  prompt: 'Not requested',
  unavailable: 'Not available',
};

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

  /** Cancel every pending local notification (master switch off). */
  const cancelAllScheduled = async () => {
    const notifications = coreServices.notifications;
    if (notifications) await notifications.cancelAll().catch(() => undefined);
  };

  /** Cancel pending reminder notifications (category switch off). */
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
      // Cancellation is best-effort; scheduling is gated regardless.
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
      <div className="th-settings-group">
        <SettingSwitchRow
          icon={<IconBell size={18} />}
          label="Notifications"
          description="Allow TwoHearts to send reminders on this device."
          checked={settings.notificationsEnabled}
          onChange={toggleMaster}
        />
      </div>

      <p className="th-settings-section">Reminders</p>
      <div className="th-settings-group">
        <SettingSwitchRow
          icon={<IconBell size={18} />}
          label="Reminder Notifications"
          description="Get reminded about reminders you've scheduled."
          checked={settings.remindersEnabled}
          onChange={toggleReminders}
          disabled={!settings.notificationsEnabled}
        />
      </div>

      <div style={{ marginTop: 'var(--th-space-4)' }}>
        <InfoCard
          title="Keep private moments private"
          text="Private Vault content never appears in notifications. All reminders are scheduled locally on this device — nothing is sent to a server."
        />
      </div>

      <p className="th-settings-section">Device</p>
      <div className="th-settings-group">
        <div className="th-settings-row th-settings-row--static">
          <span className="th-settings-row__body">
            <span className="th-settings-row__label">Device Notifications</span>
            <span className="th-settings-row__description">
              {deviceState === 'granted'
                ? 'TwoHearts can send notifications on this device.'
                : deviceState === 'denied'
                  ? 'Notifications are blocked in your device settings. Reminders will not appear until allowed.'
                  : deviceState === 'prompt'
                    ? 'TwoHearts has not asked for notification permission yet.'
                    : 'Notifications are not available on this device.'}
            </span>
          </span>
          <span
            className="th-settings-row__trailing"
            style={{ color: deviceState === 'granted' ? 'var(--th-color-success)' : 'inherit' }}
          >
            {PERMISSION_LABEL[deviceState]}
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
