/**
 * RelationshipSetupScreen (Phase 5; Stage 2 visual productization).
 *
 * Captures the partner's display name, optional birth date, and the
 * relationship start date. Validates required fields. Persists to the
 * couple relationship via RelationshipService.
 *
 * Stage 2: replaced native `<input type="date">` with branded DatePicker
 * and added visual polish — decorative illustration, branded card layout,
 * emotional micro-copy.
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

export function RelationshipSetupScreen() {
  const navigate = useNavigate();
  const { saveRelationship, isLoading, error } = useOnboarding();

  const [partnerName, setPartnerName] = useState('');
  const [partnerBirthDate, setPartnerBirthDate] = useState('');
  const [startDate, setStartDate] = useState('');
  const [errors, setErrors] = useState<{
    partnerName?: string;
    startDate?: string;
  }>({});

  const validate = (): boolean => {
    const newErrors: typeof errors = {};
    const name = partnerName.trim();
    if (!name) {
      newErrors.partnerName = "Please enter your partner's name.";
    } else if (name.length > 40) {
      newErrors.partnerName = 'Name must be 40 characters or fewer.';
    }
    // The app-state machine gates setup completion on the couple start date
    // (appStateService.deriveStage), so it is required here — the previous
    // "(optional)" label dead-ended onboarding at the final step.
    if (!startDate) {
      newErrors.startDate = 'Please enter when your journey together began.';
    } else if (!/^\d{4}-\d{2}-\d{2}$/.test(startDate)) {
      newErrors.startDate = 'Please enter a valid date.';
    } else {
      const d = new Date(`${startDate}T00:00:00Z`);
      if (Number.isNaN(d.getTime()) || d.toISOString().slice(0, 10) !== startDate) {
        newErrors.startDate = 'Please enter a valid date.';
      }
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleContinue = async () => {
    if (!validate()) return;

    const result = await saveRelationship({
      partnerDisplayName: partnerName.trim(),
      partnerBirthDate: partnerBirthDate || null,
      startDate: startDate || null,
    });
    if (result) {
      navigate(RoutePath.onboardingPersonalization);
    }
  };

  return (
    <OnboardingLayout currentPath={RoutePath.onboardingRelationship}>
      {/* Subtle decorative accent */}
      <RoseLilyDecoration variant={7} size={100} position="bottom-right" opacity={0.08} />

      <div className="th-onboarding-form">
        {/* Emotional header with illustration */}
        <div className="th-stagger-item" style={{ textAlign: 'center' }}>
          <div className="th-welcome-illustration" aria-hidden="true">
            <OnboardingArt variant="relationship-hearts" size={64} />
          </div>
          <h2 className="th-onboarding-heading">Your relationship</h2>
          <p className="th-onboarding-description">
            Tell us about the person who makes your heart feel at home.
          </p>
        </div>

        <div className="th-form-group th-stagger-item">
          <label className="th-form-label" htmlFor="partner-name">
            Partner's name
          </label>
          <Input
            id="partner-name"
            placeholder="Enter their name"
            value={partnerName}
            onChange={(e) => {
              setPartnerName(e.target.value);
              if (errors.partnerName) setErrors((prev) => ({ ...prev, partnerName: undefined }));
            }}
            autoComplete="given-name"
            maxLength={40}
            disabled={isLoading}
            aria-invalid={!!errors.partnerName}
            aria-describedby={errors.partnerName ? 'partner-name-error' : undefined}
          />
          {errors.partnerName && (
            <p className="th-form-error" id="partner-name-error" role="alert">
              {errors.partnerName}
            </p>
          )}
        </div>

        <div className="th-form-group th-stagger-item">
          <label className="th-form-label" htmlFor="partner-birthday">
            Partner's birthday <span className="th-form-optional">(optional)</span>
          </label>
          <DatePicker
            value={partnerBirthDate}
            onChange={setPartnerBirthDate}
            label="Partner's birthday"
            placeholder="Tap to choose a date"
            disabled={isLoading}
          />
        </div>

        <div className="th-form-group th-stagger-item">
          <label className="th-form-label" htmlFor="start-date">
            Relationship start date
          </label>
          <p className="th-form-hint">
            <IconHeart size={13} /> When your journey together began
          </p>
          <DatePicker
            value={startDate}
            onChange={(val) => {
              setStartDate(val);
              if (errors.startDate) setErrors((prev) => ({ ...prev, startDate: undefined }));
            }}
            label="Relationship start date"
            placeholder="Tap to choose a date"
            disabled={isLoading}
            error={!!errors.startDate}
            aria-describedby={errors.startDate ? 'start-date-error' : undefined}
          />
          {errors.startDate && (
            <p className="th-form-error" id="start-date-error" role="alert">
              {errors.startDate}
            </p>
          )}
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
