/**
 * Appearance Settings (Phase 19 — roadmap screen 83).
 *
 * Connects the EXISTING personalization preferences (appSettings schema,
 * established in Phase 4/5): theme mode (light/dark/system — dark tokens
 * landed in Phase 19), text size, and reduce motion. Changes apply
 * immediately via AppRootProvider and persist across restarts.
 */

import { useState } from 'react';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Modal } from '../../components/index.ts';
import {
  appSettingsStore,
  useAppSettings,
  type ThemeMode,
} from '../../core/appSettings.ts';
import { TEXT_SIZE_LABELS, type TextSizeKey } from '../../theme/tokens.ts';
import { SettingsScreen, SettingSwitchRow, SettingRow } from './settingsUi.tsx';

const THEME_OPTIONS: { value: ThemeMode; label: string; description: string }[] = [
  { value: 'light', label: 'Light', description: 'Warm and bright' },
  { value: 'dark', label: 'Dark', description: 'Soft and low-light' },
  { value: 'system', label: 'System', description: 'Follow your device' },
];

const TEXT_SIZE_OPTIONS: TextSizeKey[] = ['small', 'default', 'large', 'extra-large'];

export function AppearanceSettingsScreen() {
  const settings = useAppSettings();
  const [confirmReset, setConfirmReset] = useState(false);

  return (
    <SettingsScreen title="Appearance" backTo={RoutePath.appMoreSettings}>
      <p style={{ marginTop: 0, fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)' }}>
        Choose the look that feels right for you.
      </p>

      <p className="th-settings-section">Theme</p>
      <div className="th-settings-group" role="radiogroup" aria-label="Theme">
        {THEME_OPTIONS.map((option) => (
          <SettingRow
            key={option.value}
            label={option.label}
            description={option.description}
            onClick={() => appSettingsStore.setThemeMode(option.value)}
            trailing={
              <span
                role="radio"
                aria-checked={settings.themeMode === option.value}
                aria-label={option.label}
                style={{
                  width: '20px',
                  height: '20px',
                  borderRadius: 'var(--th-radius-circle)',
                  border: `var(--th-border-width-medium) solid ${
                    settings.themeMode === option.value
                      ? 'var(--th-color-burgundy)'
                      : 'var(--th-color-border)'
                  }`,
                  background:
                    settings.themeMode === option.value ? 'var(--th-color-burgundy)' : 'transparent',
                  boxShadow:
                    settings.themeMode === option.value
                      ? 'inset 0 0 0 3px var(--th-color-surface)'
                      : 'none',
                }}
              />
            }
          />
        ))}
      </div>
      {settings.themeMode === 'system' ? (
        <p style={{ fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)' }}>
          Using your device&apos;s appearance setting.
        </p>
      ) : null}

      <p className="th-settings-section">Text Size</p>
      <div className="th-settings-group" role="radiogroup" aria-label="Text size">
        {TEXT_SIZE_OPTIONS.map((size) => (
          <SettingRow
            key={size}
            label={TEXT_SIZE_LABELS[size]}
            onClick={() => appSettingsStore.setTextSize(size)}
            trailing={
              settings.textSize === size ? (
                <span
                  role="radio"
                  aria-checked="true"
                  aria-label={`${TEXT_SIZE_LABELS[size]} selected`}
                  style={{ color: 'var(--th-color-burgundy)', fontWeight: 'bold' }}
                >
                  ✓
                </span>
              ) : null
            }
          />
        ))}
      </div>

      <p className="th-settings-section">Motion</p>
      <div className="th-settings-group">
        <SettingSwitchRow
          label="Reduce Motion"
          description="Use simpler animations throughout TwoHearts."
          checked={settings.reduceMotion}
          onChange={(next) => appSettingsStore.setReduceMotion(next)}
        />
      </div>

      <div style={{ marginTop: 'var(--th-space-6)' }} className="th-settings-group">
        <SettingRow
          label="Reset to Default"
          description="Restore the original TwoHearts appearance."
          onClick={() => setConfirmReset(true)}
        />
      </div>

      <Modal open={confirmReset} onClose={() => setConfirmReset(false)} label="Reset appearance">
        <h2 style={{ marginTop: 0 }}>Reset appearance?</h2>
        <p style={{ color: 'var(--th-color-text-secondary)' }}>
          Theme, text size, and motion return to the TwoHearts defaults. Your data is not affected.
        </p>
        <div style={{ display: 'flex', gap: 'var(--th-space-3)' }}>
          <Button variant="ghost" full onClick={() => setConfirmReset(false)}>
            Cancel
          </Button>
          <Button
            variant="primary"
            full
            onClick={() => {
              appSettingsStore.setThemeMode('light');
              appSettingsStore.setTextSize('default');
              appSettingsStore.setReduceMotion(false);
              setConfirmReset(false);
            }}
          >
            Reset
          </Button>
        </div>
      </Modal>
    </SettingsScreen>
  );
}
