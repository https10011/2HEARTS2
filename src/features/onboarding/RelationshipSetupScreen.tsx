/**
 * RelationshipSetupScreen (Phase 5).
 *
 * Captures the partner's display name, optional birth date, and the
 * relationship start date. Validates required fields. Persists to the
 * couple relationship via RelationshipService.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { Button, Input } from '../../components/index.ts';
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
    if (startDate) {
      // Validate date format
      if (!/^\d{4}-\d{2}-\d{2}$/.test(startDate)) {
        newErrors.startDate = 'Please enter a valid date.';
      } else {
        const d = new Date(`${startDate}T00:00:00Z`);
        if (Number.isNaN(d.getTime()) || d.toISOString().slice(0, 10) !== startDate) {
          newErrors.startDate = 'Please enter a valid date.';
        }
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
      <div className="th-onboarding-form">
        <h2 className="th-onboarding-heading">Your relationship</h2>
        <p className="th-onboarding-description">
          Tell us about your partner and when your journey together began.
        </p>

        <div className="th-form-group">
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

        <div className="th-form-group">
          <label className="th-form-label" htmlFor="partner-birthday">
            Partner's birthday <span className="th-form-optional">(optional)</span>
          </label>
          <Input
            id="partner-birthday"
            type="date"
            value={partnerBirthDate}
            onChange={(e) => setPartnerBirthDate(e.target.value)}
            disabled={isLoading}
          />
        </div>

        <div className="th-form-group">
          <label className="th-form-label" htmlFor="start-date">
            Relationship start date <span className="th-form-optional">(optional)</span>
          </label>
          <Input
            id="start-date"
            type="date"
            value={startDate}
            onChange={(e) => {
              setStartDate(e.target.value);
              if (errors.startDate) setErrors((prev) => ({ ...prev, startDate: undefined }));
            }}
            disabled={isLoading}
            aria-invalid={!!errors.startDate}
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
