/**
 * ProfileSetupScreen (Phase 5).
 *
 * Captures the owner's display name and optional birth date.
 * Validates required fields using the existing Phase 3 validators.
 * Persists to Profile via RelationshipService.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Input } from '../../components/index.ts';
import { OnboardingLayout } from './OnboardingLayout.tsx';
import { useOnboarding } from './useOnboarding.ts';

export function ProfileSetupScreen() {
  const navigate = useNavigate();
  const { saveOwnerProfile, isLoading, error } = useOnboarding();

  const [displayName, setDisplayName] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [fieldError, setFieldError] = useState<string | null>(null);

  const handleContinue = async () => {
    const name = displayName.trim();
    if (!name) {
      setFieldError('Please enter your name.');
      return;
    }
    if (name.length > 40) {
      setFieldError('Name must be 40 characters or fewer.');
      return;
    }
    setFieldError(null);

    const result = await saveOwnerProfile({
      displayName: name,
      birthDate: birthDate || null,
    });
    if (result) {
      navigate(RoutePath.onboardingRelationship);
    }
  };

  return (
    <OnboardingLayout currentPath={RoutePath.onboardingProfile}>
      <div className="th-onboarding-form">
        <h2 className="th-onboarding-heading">About you</h2>
        <p className="th-onboarding-description">
          Tell us a bit about yourself. This is your private profile.
        </p>

        <div className="th-form-group">
          <label className="th-form-label" htmlFor="owner-name">
            Your name
          </label>
          <Input
            id="owner-name"
            placeholder="Enter your name"
            value={displayName}
            onChange={(e) => {
              setDisplayName(e.target.value);
              if (fieldError) setFieldError(null);
            }}
            autoComplete="given-name"
            maxLength={40}
            disabled={isLoading}
            aria-invalid={!!fieldError}
            aria-describedby={fieldError ? 'owner-name-error' : undefined}
          />
          {fieldError && (
            <p className="th-form-error" id="owner-name-error" role="alert">
              {fieldError}
            </p>
          )}
        </div>

        <div className="th-form-group">
          <label className="th-form-label" htmlFor="owner-birthday">
            Birthday <span className="th-form-optional">(optional)</span>
          </label>
          <Input
            id="owner-birthday"
            type="date"
            value={birthDate}
            onChange={(e) => setBirthDate(e.target.value)}
            disabled={isLoading}
          />
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
