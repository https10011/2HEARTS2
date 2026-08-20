/**
 * PersonalizationSetupScreen (Phase 5).
 *
 * Captures text-size and theme-mode preferences. Persists to appSettings.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button } from '../../components/index.ts';
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
      <div className="th-onboarding-form">
        <h2 className="th-onboarding-heading">Make it yours</h2>
        <p className="th-onboarding-description">
          Choose your preferred text size and appearance. You can change these anytime.
        </p>

        {/* Text size selection */}
        <div className="th-form-group">
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
        <div className="th-form-group">
          <label className="th-form-label">Appearance</label>
          <div className="th-option-group" role="radiogroup" aria-label="Appearance">
            {THEME_MODE_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                type="button"
                role="radio"
                aria-checked={themeMode === opt.value}
                className={`th-option-chip ${themeMode === opt.value ? 'th-option-chip--active' : ''}`}
                onClick={() => setThemeMode(opt.value)}
                disabled={isLoading}
              >
                {opt.label}
              </button>
            ))}
          </div>
        </div>

        {error && (
          <p className="th-form-error th-form-error--global" role="alert">
            {error}
          </p>
        )}

        <div className="th-onboarding-actions">
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
