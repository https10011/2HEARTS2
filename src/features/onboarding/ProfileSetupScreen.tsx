/**
 * ProfileSetupScreen (Phase 5; Stage 2 visual productization).
 *
 * Captures the owner's display name and optional birth date.
 * Validates required fields using the existing Phase 3 validators.
 * Persists to Profile via RelationshipService.
 *
 * Stage 2: replaced native `<input type="date">` with branded DatePicker
 * and added visual polish — personal illustration, warm micro-copy,
 * staggered entrance, branded card styling.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  Button,
  Input,
  DatePicker,
  IconHeart,
  OnboardingArt,
  RoseLilyDecoration,
} from '../../components/index.ts';
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
      {/* Subtle decorative accent */}
      <RoseLilyDecoration variant={15} size={100} position="top-right" opacity={0.08} />

      <div className="th-onboarding-form">
        {/* Emotional header with personal illustration */}
        <div className="th-stagger-item" style={{ textAlign: 'center' }}>
          <div className="th-welcome-illustration" aria-hidden="true">
            <OnboardingArt variant="personal-profile" size={64} />
          </div>
          <h2 className="th-onboarding-heading">About you</h2>
          <p className="th-onboarding-description">
            Tell us a bit about yourself. This is your private space.
          </p>
        </div>

        <div className="th-form-group th-stagger-item">
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

        <div className="th-form-group th-stagger-item">
          <label className="th-form-label" htmlFor="owner-birthday">
            Birthday <span className="th-form-optional">(optional)</span>
          </label>
          <p className="th-form-hint">
            <IconHeart size={13} /> We'll remember your special day
          </p>
          <DatePicker
            value={birthDate}
            onChange={setBirthDate}
            label="Your birthday"
            placeholder="Tap to choose a date"
            disabled={isLoading}
          />
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
