/**
 * Security & App Lock Settings (Phase 19 — roadmap screen 85).
 *
 * Integrates the EXISTING AppLockService + SecureStore architecture (Phase
 * 3/17): PIN material never touches settings, UI state, or logs — only the
 * non-sensitive flags (appLockEnabled, lockTimeoutSeconds) persist in
 * appSettings. Lock state stays memory-only, so a cold start is always
 * locked when the lock is enabled; the Vault reacts to the same lock bus.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Input, Modal, IconHeart , IconCheck } from '../../components/index.ts';
import { appSettingsStore, useAppSettings } from '../../core/appSettings.ts';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { safeUserMessage } from '../../services/errors/appError.ts';
import { SettingsScreen, SettingRow, SettingSwitchRow, InfoCard } from './settingsUi.tsx';

const PIN_PATTERN = /^\d{4,8}$/;

const TIMEOUT_OPTIONS: { seconds: number; label: string }[] = [
  { seconds: 0, label: 'Immediately' },
  { seconds: 60, label: 'After 1 minute' },
  { seconds: 300, label: 'After 5 minutes' },
];

type PinDialog = 'enable' | 'change' | null;

export function SecuritySettingsScreen() {
  const navigate = useNavigate();
  const settings = useAppSettings();
  const appLock = coreServices.appLock;

  const [pinDialog, setPinDialog] = useState<PinDialog>(null);
  const [pin, setPin] = useState('');
  const [pinConfirm, setPinConfirm] = useState('');
  const [pinError, setPinError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [confirmDisable, setConfirmDisable] = useState(false);
  const [timeoutPicker, setTimeoutPicker] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const lockEnabled = settings.appLockEnabled;
  const timeoutLabel =
    TIMEOUT_OPTIONS.find((o) => o.seconds === settings.lockTimeoutSeconds)?.label ??
    `After ${settings.lockTimeoutSeconds} seconds`;

  const resetPinDialog = () => {
    setPinDialog(null);
    setPin('');
    setPinConfirm('');
    setPinError(null);
  };

  const submitPin = async () => {
    if (!appLock) return;
    if (!PIN_PATTERN.test(pin)) {
      setPinError('The PIN must be 4–8 digits.');
      return;
    }
    if (pin !== pinConfirm) {
      setPinError('The PINs do not match.');
      return;
    }
    setBusy(true);
    setPinError(null);
    try {
      await appLock.enable(pin); // writes salt+verifier to SecureStore only
      appSettingsStore.set({ appLockEnabled: true });
      setMessage(pinDialog === 'change' ? 'App Lock updated.' : 'App Lock enabled.');
      resetPinDialog();
    } catch (cause) {
      setPinError(safeUserMessage(cause));
    } finally {
      setBusy(false);
    }
  };

  const disableLock = async () => {
    if (!appLock) return;
    setBusy(true);
    try {
      await appLock.disable(); // removes PIN material from SecureStore
      appSettingsStore.set({ appLockEnabled: false });
      setMessage('App Lock turned off.');
      setConfirmDisable(false);
    } catch (cause) {
      setMessage(safeUserMessage(cause));
    } finally {
      setBusy(false);
    }
  };

  const lockNow = () => {
    appLock?.lock();
    navigate(RoutePath.appHome);
  };

  return (
    <SettingsScreen title="Security &amp; App Lock" backTo={RoutePath.appMoreSettings}>
      <div style={{ marginBottom: 'var(--th-space-4)' }}>
        <InfoCard
          title="Your privacy matters"
          text="Protect your TwoHearts memories and private spaces on this device."
        />
      </div>

      {!appLock ? (
        <InfoCard
          title="App Lock unavailable"
          text="The security service did not start on this device. Your data remains local."
        />
      ) : (
        <>
          <div className="th-settings-group">
            <SettingSwitchRow
              icon={<IconHeart size={18} />}
              label="App Lock"
              description="Require a lock before opening TwoHearts."
              checked={lockEnabled}
              onChange={(next) => {
                setMessage(null);
                if (next) setPinDialog('enable');
                else setConfirmDisable(true);
              }}
            />
            <SettingRow
              label="Lock Method"
              description="Choose how TwoHearts is unlocked."
              static
              trailing={<span>PIN</span>}
            />
            {lockEnabled ? (
              <SettingRow
                label="Change Lock"
                description="Update your current App Lock PIN."
                onClick={() => {
                  setMessage(null);
                  setPinDialog('change');
                }}
              />
            ) : null}
          </div>

          {lockEnabled ? (
            <>
              <p className="th-settings-section">Auto-Lock</p>
              <div className="th-settings-group">
                <SettingRow
                  label="Lock automatically"
                  description="Choose when TwoHearts should require your lock again."
                  onClick={() => setTimeoutPicker(true)}
                  trailing={<span>{timeoutLabel}</span>}
                />
                <SettingRow
                  label="Lock TwoHearts Now"
                  description="Immediately lock the app."
                  danger
                  onClick={lockNow}
                />
              </div>

              <div style={{ marginTop: 'var(--th-space-4)' }}>
                <InfoCard
                  title="Protected areas"
                  text="Private Vault, personal information, and relationship information."
                />
              </div>
            </>
          ) : null}

          {message ? (
            <p role="status" style={{ color: 'var(--th-color-success)', fontSize: 'var(--th-font-size-sm)' }}>
              {message}
            </p>
          ) : null}

          <div style={{ marginTop: 'var(--th-space-4)' }}>
            <InfoCard
              title="Private by design"
              text="Your App Lock settings stay on this device. TwoHearts locks again after a fresh start."
            />
          </div>
        </>
      )}

      {/* PIN entry (enable / change) */}
      <Modal open={pinDialog !== null} onClose={resetPinDialog} label="Set App Lock PIN">
        <h2 style={{ marginTop: 0 }}>{pinDialog === 'change' ? 'Change your PIN' : 'Choose a PIN'}</h2>
        <div className="th-form-group">
          <label className="th-form-label" htmlFor="lock-pin">
            PIN (4–8 digits)
          </label>
          <Input
            id="lock-pin"
            type="password"
            inputMode="numeric"
            value={pin}
            onChange={(e) => setPin(e.target.value)}
            autoComplete="off"
          />
        </div>
        <div className="th-form-group">
          <label className="th-form-label" htmlFor="lock-pin-confirm">
            Confirm PIN
          </label>
          <Input
            id="lock-pin-confirm"
            type="password"
            inputMode="numeric"
            value={pinConfirm}
            onChange={(e) => setPinConfirm(e.target.value)}
            autoComplete="off"
          />
        </div>
        {pinError ? (
          <p role="alert" style={{ color: 'var(--th-color-error)', fontSize: 'var(--th-font-size-sm)' }}>
            {pinError}
          </p>
        ) : null}
        <div style={{ display: 'flex', gap: 'var(--th-space-3)' }}>
          <Button variant="ghost" full onClick={resetPinDialog} disabled={busy}>
            Cancel
          </Button>
          <Button variant="primary" full onClick={() => void submitPin()} disabled={busy}>
            {busy ? 'Saving…' : 'Save PIN'}
          </Button>
        </div>
      </Modal>

      {/* Disable confirmation */}
      <Modal open={confirmDisable} onClose={() => setConfirmDisable(false)} label="Turn off App Lock">
        <h2 style={{ marginTop: 0 }}>Turn Off App Lock?</h2>
        <p style={{ color: 'var(--th-color-text-secondary)' }}>
          TwoHearts will no longer require a lock when opening the app. Your PIN is removed from
          this device.
        </p>
        <div style={{ display: 'flex', gap: 'var(--th-space-3)' }}>
          <Button variant="ghost" full onClick={() => setConfirmDisable(false)} disabled={busy}>
            Cancel
          </Button>
          <Button variant="primary" full onClick={() => void disableLock()} disabled={busy}>
            {busy ? 'Turning off…' : 'Turn Off'}
          </Button>
        </div>
      </Modal>

      {/* Auto-lock timeout picker */}
      <Modal open={timeoutPicker} onClose={() => setTimeoutPicker(false)} label="Auto-lock timing">
        <h2 style={{ marginTop: 0 }}>Lock automatically</h2>
        <div className="th-settings-group" role="radiogroup" aria-label="Auto-lock timing">
          {TIMEOUT_OPTIONS.map((option) => (
            <SettingRow
              key={option.seconds}
              label={option.label}
              onClick={() => {
                appSettingsStore.set({ lockTimeoutSeconds: option.seconds });
                setTimeoutPicker(false);
              }}
              trailing={
                settings.lockTimeoutSeconds === option.seconds ? (
                  <span style={{ color: 'var(--th-color-burgundy)', display: 'inline-flex' }}><IconCheck size={16} /></span>
                ) : null
              }
            />
          ))}
        </div>
      </Modal>
    </SettingsScreen>
  );
}
