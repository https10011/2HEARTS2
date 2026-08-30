/**
 * Onboarding hook (Phase 5).
 *
 * Consumes AppStateService + appSettingsStore for navigation and persistence.
 * UI screens call the returned helpers; they never touch repositories or
 * localStorage directly.
 */

import { useState, useCallback } from 'react';
import { coreServices } from '../../services/bootstrap/appBootstrap.ts';
import { appSettingsStore, type OnboardingStage } from '../../core/appSettings.ts';
import type { RelationshipService } from '../../services/relationship/relationshipService.ts';
import { safeUserMessage } from '../../services/errors/appError.ts';

export interface OnboardingState {
  stage: OnboardingStage;
  isLoading: boolean;
  error: string | null;
}

function getRelationshipService(): RelationshipService | undefined {
  return coreServices.relationship;
}

function getAppState() {
  return coreServices.appState;
}

export function useOnboarding() {
  const [state, setState] = useState<OnboardingState>({
    stage: appSettingsStore.getState().onboardingStage,
    isLoading: false,
    error: null,
  });

  const setLoading = useCallback((loading: boolean) => {
    setState((prev) => ({ ...prev, isLoading: loading }));
  }, []);

  const setError = useCallback((error: string | null) => {
    setState((prev) => ({ ...prev, error }));
  }, []);

  const advanceStage = useCallback((nextStage: OnboardingStage) => {
    appSettingsStore.setOnboardingStage(nextStage);
    setState((prev) => ({ ...prev, stage: nextStage, error: null }));
  }, []);

  /** Save owner profile — delegates to RelationshipService. */
  const saveOwnerProfile = useCallback(async (input: {
    displayName: string;
    birthDate?: string | null;
  }) => {
    const svc = getRelationshipService();
    if (!svc) {
      setError('Service not available. Please restart the app.');
      return false;
    }
    setLoading(true);
    setError(null);
    try {
      await svc.saveOwner(input);
      advanceStage('relationship');
      return true;
    } catch (err) {
      const message = safeUserMessage(err);
      setError(message);
      return false;
    } finally {
      setLoading(false);
    }
  }, [advanceStage, setError, setLoading]);

  /** Save partner profile and set relationship start date. */
  const saveRelationship = useCallback(async (input: {
    partnerDisplayName: string;
    partnerBirthDate?: string | null;
    startDate: string | null;
  }) => {
    const svc = getRelationshipService();
    if (!svc) {
      setError('Service not available. Please restart the app.');
      return false;
    }
    setLoading(true);
    setError(null);
    try {
      await svc.savePartner({
        displayName: input.partnerDisplayName,
        birthDate: input.partnerBirthDate,
      });
      if (input.startDate) {
        await svc.setStartDate(input.startDate);
      }
      advanceStage('personalization');
      return true;
    } catch (err) {
      const message = safeUserMessage(err);
      setError(message);
      return false;
    } finally {
      setLoading(false);
    }
  }, [advanceStage, setError, setLoading]);

  /** Save personalization preferences. */
  const savePersonalization = useCallback(async (input: {
    textSize: 'small' | 'default' | 'large' | 'extra-large';
    themeMode: 'light' | 'dark' | 'system';
  }) => {
    setLoading(true);
    setError(null);
    try {
      appSettingsStore.setTextSize(input.textSize);
      appSettingsStore.setThemeMode(input.themeMode);
      advanceStage('personalization');
      return true;
    } catch (err) {
      const message = safeUserMessage(err);
      setError(message);
      return false;
    } finally {
      setLoading(false);
    }
  }, [advanceStage, setError, setLoading]);

  /** Skip app-lock and complete setup. */
  const skipAppLock = useCallback(async () => {
    const appState = getAppState();
    if (appState) {
      setLoading(true);
      setError(null);
      try {
        await appState.completeSetup();
        advanceStage('complete');
        return true;
      } catch (err) {
        const message = safeUserMessage(err);
        setError(message);
        return false;
      } finally {
        setLoading(false);
      }
    }
    // Fallback: directly set complete
    appSettingsStore.completeOnboarding();
    advanceStage('complete');
    return true;
  }, [advanceStage, setError, setLoading]);

  /** Enable app-lock and complete setup. */
  const enableAppLock = useCallback(async (pin: string) => {
    const appLock = coreServices.appLock;
    const appState = getAppState();
    if (!appLock) {
      setError('App lock service not available.');
      return false;
    }
    setLoading(true);
    setError(null);
    try {
      await appLock.enable(pin);
      appSettingsStore.set({ appLockEnabled: true });
      if (appState) {
        await appState.completeSetup();
      } else {
        appSettingsStore.completeOnboarding();
      }
      advanceStage('complete');
      return true;
    } catch (err) {
      const message = safeUserMessage(err);
      setError(message);
      return false;
    } finally {
      setLoading(false);
    }
  }, [advanceStage, setError, setLoading]);

  return {
    ...state,
    advanceStage,
    saveOwnerProfile,
    saveRelationship,
    savePersonalization,
    skipAppLock,
    enableAppLock,
    setError,
  };
}
