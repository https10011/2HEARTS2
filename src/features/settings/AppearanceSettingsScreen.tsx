/**
 * Appearance Settings (Stage 15 — Settings + App Customization).
 *
 * Theme selector with visual preview cards, text size selector, motion
 * preference, and appearance reset. All changes apply immediately.
 * Architecture unchanged — uses existing appSettingsStore + useSyncExternalStore.
 */

import { useState } from 'react';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Modal, IconCheck, IconSettings } from '../../components/index.ts';
import {
  appSettingsStore,
  useAppSettings,
} from '../../core/appSettings.ts';
import { SettingsScreen, SettingRow, SettingSwitchRow } from './settingsUi.tsx';
import { THEME_OPTIONS, TEXT_SIZE_OPTIONS } from './settingsPresentation.ts';

export function AppearanceSettingsScreen() {
  const settings = useAppSettings();
  const [confirmReset, setConfirmReset] = useState(false);

  return (
    <SettingsScreen title="Appearance" backTo={RoutePath.appMoreSettings}>
      <p style={{ marginTop: 0, fontSize: 'var(--th-font-size-sm)', color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-4)' }}>
        Choose the look that feels right for you.
      </p>

      {/* Theme selector — visual preview cards */}
      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Theme
      </div>
      <div className="th-settings-theme-options" role="radiogroup" aria-label="Theme">
        {THEME_OPTIONS.map((option) => (
          <button
            key={option.value}
            className={`th-settings-theme-card ${settings.themeMode === option.value ? 'th-settings-theme-card--selected' : ''}`}
            onClick={() => appSettingsStore.setThemeMode(option.value)}
            role="radio"
            aria-checked={settings.themeMode === option.value}
            aria-label={option.label}
          >
            <div className={`th-settings-theme-card__preview th-settings-theme-card__preview--${option.value}`} />
            <span className="th-settings-theme-card__label">{option.label}</span>
            {settings.themeMode === option.value && (
              <span className="th-settings-theme-card__check th-settings-theme-card__check--active">
                <IconCheck size={12} />
              </span>
            )}
          </button>
        ))}
      </div>
      {settings.themeMode === 'system' ? (
        <p style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', marginTop: 'var(--th-space-2)' }}>
          Using your device&apos;s appearance setting.
        </p>
      ) : null}

      {/* Text size selector */}
      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Text Size
      </div>
      <div className="th-settings-textsize-options" role="radiogroup" aria-label="Text size">
        {TEXT_SIZE_OPTIONS.map((option) => (
          <button
            key={option.value}
            className={`th-settings-textsize-card ${settings.textSize === option.value ? 'th-settings-textsize-card--selected' : ''}`}
            onClick={() => appSettingsStore.setTextSize(option.value)}
            role="radio"
            aria-checked={settings.textSize === option.value}
            aria-label={`${option.label} text size`}
          >
            <span className="th-settings-textsize-card__label">{option.label}</span>
            <span className="th-settings-textsize-card__desc">{option.description}</span>
            {settings.textSize === option.value && (
              <span style={{ color: 'var(--th-color-burgundy)', display: 'inline-flex', flexShrink: 0 }}>
                <IconCheck size={16} />
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Motion */}
      <div className="th-settings-section--enhanced">
        <span className="th-settings-section--enhanced__dot" />
        Motion
      </div>
      <div className="th-settings-group--enhanced">
        <SettingSwitchRow
          icon={<IconSettings size={18} />}
          label="Reduce Motion"
          description="Use simpler animations throughout TwoHearts"
          checked={settings.reduceMotion}
          onChange={(next) => appSettingsStore.setReduceMotion(next)}
        />
      </div>

      {/* Reset */}
      <div style={{ marginTop: 'var(--th-space-6)' }} className="th-settings-group--enhanced">
        <SettingRow
          label="Reset to Default"
          description="Restore the original TwoHearts appearance"
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
