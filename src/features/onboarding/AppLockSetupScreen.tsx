/**
 * AppLockSetupScreen (Phase 5).
 *
 * Optional app-lock setup. Users can skip or set a 4-8 digit PIN.
 * Delegates to the existing AppLockService (Phase 3/4).
 */

import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Input } from '../../components/index.ts';
import { OnboardingLayout } from './OnboardingLayout.tsx';
import { useOnboarding } from './useOnboarding.ts';

export function AppLockSetupScreen() {
  const navigate = useNavigate();
  const { enableAppLock, skipAppLock, isLoading, error } = useOnboarding();

  const [pin, setPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');
  const [fieldError, setFieldError] = useState<string | null>(null);

  const handleEnable = useCallback(async () => {
    // Validate PIN shape: 4-8 digits
    if (!/^\d{4,8}$/.test(pin)) {
      setFieldError('PIN must be 4–8 digits.');
      return;
    }
    if (pin !== confirmPin) {
      setFieldError('PINs do not match.');
      return;
    }
    setFieldError(null);

    const result = await enableAppLock(pin);
    if (result) {
      navigate(RoutePath.onboardingComplete);
    }
  }, [pin, confirmPin, enableAppLock, navigate]);

  const handleSkip = useCallback(async () => {
    const result = await skipAppLock();
    if (result) {
      navigate(RoutePath.onboardingComplete);
    }
  }, [skipAppLock, navigate]);

  return (
    <OnboardingLayout
      currentPath={RoutePath.onboardingAppLock}
      showSkip
      onSkip={handleSkip}
      skipLoading={isLoading}
    >
      <div className="th-onboarding-form">
        <h2 className="th-onboarding-heading">Lock your app</h2>
        <p className="th-onboarding-description">
          Protect your private space with a PIN. You can set this up later in Settings.
        </p>

        <div className="th-form-group">
          <label className="th-form-label" htmlFor="lock-pin">
            Create a PIN
          </label>
          <Input
            id="lock-pin"
            type="password"
            inputMode="numeric"
            pattern="[0-9]*"
            placeholder="4–8 digits"
            value={pin}
            onChange={(e) => {
              const val = e.target.value.replace(/\D/g, '').slice(0, 8);
              setPin(val);
              if (fieldError) setFieldError(null);
            }}
            autoComplete="new-password"
            disabled={isLoading}
            aria-invalid={!!fieldError}
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
            pattern="[0-9]*"
            placeholder="Re-enter your PIN"
            value={confirmPin}
            onChange={(e) => {
              const val = e.target.value.replace(/\D/g, '').slice(0, 8);
              setConfirmPin(val);
              if (fieldError) setFieldError(null);
            }}
            autoComplete="new-password"
            disabled={isLoading}
          />
        </div>

        {fieldError && (
          <p className="th-form-error" role="alert">
            {fieldError}
          </p>
        )}

        {error && (
          <p className="th-form-error th-form-error--global" role="alert">
            {error}
          </p>
        )}

        <div className="th-onboarding-actions">
          <Button
            variant="primary"
            full
            onClick={handleEnable}
            disabled={isLoading || !pin || !confirmPin}
          >
            {isLoading ? 'Setting up…' : 'Enable app lock'}
          </Button>
          <Button
            variant="ghost"
            full
            onClick={handleSkip}
            disabled={isLoading}
          >
            Skip for now
          </Button>
        </div>
      </div>
    </OnboardingLayout>
  );
}
