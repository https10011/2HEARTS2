/**
 * PersonalizationSetupScreen (Phase 5; Stage 2 visual productization).
 *
 * Captures text-size and theme-mode preferences. Persists to appSettings.
 *
 * Stage 2: added visual polish — personal illustration, warm micro-copy,
 * staggered entrance, improved option chip styling with preview context.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  Button,
  OnboardingArt,
  RoseLilyDecoration,
} from '../../components/index.ts';
import { OnboardingLayout } from './OnboardingLayout.tsx';
import { useOnboarding } from './useOnboarding.ts';
import { appSettingsStore, type ThemeMode } from '../../core/appSettings.ts';
import { TEXT_SIZE_LABELS, type TextSizeKey } from '../../theme/tokens.ts';

const TEXT_SIZE_OPTIONS: TextSizeKey[] = ['small', 'default', 'large', 'extra-large'];
const THEME_MODE_OPTIONS: { value: ThemeMode; label: string }[] = [
  { value: 'light', label: 'Light' },
  { value: 'dark', label: 'Dark' },
  { value: 'system', label: 'System' },
];

export function PersonalizationSetupScreen() {
  const navigate = useNavigate();
  const { savePersonalization, isLoading, error } = useOnboarding();

  const currentSettings = appSettingsStore.getState();
  const [textSize, setTextSize] = useState<TextSizeKey>(currentSettings.textSize);
  const [themeMode, setThemeMode] = useState<ThemeMode>(currentSettings.themeMode);

  const handleContinue = async () => {
    const result = await savePersonalization({ textSize, themeMode });
    if (result) {
      navigate(RoutePath.onboardingAppLock);
    }
  };

  return (
    <OnboardingLayout currentPath={RoutePath.onboardingPersonalization}>
      {/* Subtle decorative accent */}
      <RoseLilyDecoration variant={16} size={100} position="bottom-left" opacity={0.08} />

      <div className="th-onboarding-form">
        {/* Emotional header */}
        <div className="th-stagger-item" style={{ textAlign: 'center' }}>
          <div className="th-welcome-illustration" aria-hidden="true">
            <OnboardingArt variant="personalization-card" size={64} />
          </div>
          <h2 className="th-onboarding-heading">Make it yours</h2>
          <p className="th-onboarding-description">
            Choose how TwoHearts looks and feels. You can always change these later.
          </p>
        </div>

        {/* Text size selection */}
        <div className="th-form-group th-stagger-item">
          <label className="th-form-label">Text size</label>
          <div className="th-option-group" role="radiogroup" aria-label="Text size">
            {TEXT_SIZE_OPTIONS.map((size) => (
              <button
                key={size}
                type="button"
                role="radio"
                aria-checked={textSize === size}
                className={`th-option-chip ${textSize === size ? 'th-option-chip--active' : ''}`}
                onClick={() => setTextSize(size)}
                disabled={isLoading}
              >
                {TEXT_SIZE_LABELS[size]}
              </button>
            ))}
          </div>
        </div>

        {/* Theme mode selection */}
        <div className="th-form-group th-stagger-item">
          <label className="th-form-label">Appearance</label>
          <div className="th-personal-themes" role="radiogroup" aria-label="Appearance">
            {THEME_MODE_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                type="button"
                role="radio"
                aria-checked={themeMode === opt.value}
                className={`th-personal-theme ${themeMode === opt.value ? 'th-personal-theme--active' : ''}`}
                onClick={() => setThemeMode(opt.value)}
                disabled={isLoading}
              >
                <span className="th-personal-theme__label">{opt.label}</span>
              </button>
            ))}
          </div>
        </div>

        {error && (
          <p className="th-form-error th-form-error--global" role="alert">
            {error}
          </p>
        )}

        <div className="th-onboarding-actions th-stagger-item">
          <Button
            variant="primary"
            full
            onClick={handleContinue}
            disabled={isLoading}
          >
            {isLoading ? 'Saving…' : 'Continue'}
          </Button>
        </div>
      </div>
    </OnboardingLayout>
  );
}
