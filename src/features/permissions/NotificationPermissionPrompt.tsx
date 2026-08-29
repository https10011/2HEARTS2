/**
 * NotificationPermissionPrompt (Stage 6 — Permission Experience).
 *
 * A reusable, emotionally-aware permission prompt for POST_NOTIFICATIONS.
 * Explains WHY notifications matter in TwoHearts terms before triggering
 * the real Android permission dialog. Handles all states:
 *
 *   prompt     → show explanation + Allow / Skip
 *   granted    → show confirmation + Continue
 *   denied     → explain + Open Settings guidance
 *   unavailable → skip silently (web/dev)
 *
 * Used in two places:
 *   1. Post-onboarding transition (SetupCompleteScreen)
 *   2. Just-in-time when creating first reminder
 */

import { useEffect, useState, useCallback } from 'react';
import {
  Button,
  IconHeart,
  IconBell,
  IconCheck,
  OnboardingArt,
  RoseLilyDecoration,
} from '../../components/index.ts';
import { OnboardingLayout } from '../onboarding/OnboardingLayout.tsx';
import { PermissionService, type PermissionState } from '../../services/permissions/permissionService.ts';

const permissions = new PermissionService();

interface NotificationPermissionPromptProps {
  /** Called when the user is done (allow, skip, or already granted). */
  onComplete: () => void;
  /** Whether this is shown as an onboarding step (has back/next chrome) or inline. */
  mode?: 'onboarding' | 'inline';
}

export function NotificationPermissionPrompt({
  onComplete,
  mode = 'onboarding',
}: NotificationPermissionPromptProps) {
  const [status, setStatus] = useState<PermissionState>('prompt');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    permissions.check('notifications').then((state) => {
      if (!cancelled) setStatus(state);
    }).catch(() => undefined);
    return () => { cancelled = true; };
  }, []);

  const handleAllow = useCallback(async () => {
    setLoading(true);
    try {
      const result = await permissions.ensure('notifications');
      setStatus(result);
    } catch {
      // Non-critical — continue regardless
    } finally {
      setLoading(false);
      // Brief delay so user sees the result before transitioning
      setTimeout(onComplete, 400);
    }
  }, [onComplete]);

  const handleSkip = useCallback(() => {
    onComplete();
  }, [onComplete]);

  const handleOpenSettings = useCallback(() => {
    // On Android, the user can enable notifications in system settings.
    // The Settings > Notifications screen has a deeper integration.
    // For now, just continue — the user can find it in Settings.
    onComplete();
  }, [onComplete]);

  // Already granted — quick confirmation
  if (status === 'granted') {
    if (mode === 'onboarding') {
      return (
        <OnboardingLayout currentPath="/onboarding/notifications" showBack={false}>
          <RoseLilyDecoration variant={3} size={100} position="top-right" opacity={0.1} />
          <div className="th-onboarding-form">
            <div className="th-stagger-item" style={{ textAlign: 'center' }}>
              <div className="th-welcome-illustration" aria-hidden="true">
                <OnboardingArt variant="paired-hearts-check" size={64} />
              </div>
              <h2 className="th-onboarding-heading">Notifications are on</h2>
              <p className="th-onboarding-description">
                You'll get gentle reminders for the moments that matter.
              </p>
            </div>
            <div className="th-onboarding-actions th-stagger-item">
              <Button variant="primary" full onClick={onComplete}>
                Continue
              </Button>
            </div>
          </div>
        </OnboardingLayout>
      );
    }
    return null; // inline mode: no UI needed if already granted
  }

  // Unavailable (web/dev) — skip silently
  if (status === 'unavailable') {
    onComplete();
    return null;
  }

  // Denied / permanently denied
  if (status === 'denied') {
    if (mode === 'onboarding') {
      return (
        <OnboardingLayout currentPath="/onboarding/notifications" showBack={false}>
          <RoseLilyDecoration variant={7} size={100} position="bottom-left" opacity={0.08} />
          <div className="th-onboarding-form">
            <div className="th-stagger-item" style={{ textAlign: 'center' }}>
              <div className="th-welcome-illustration" aria-hidden="true">
                <OnboardingArt variant="security-lock" size={64} />
              </div>
              <h2 className="th-onboarding-heading">Notifications paused</h2>
              <p className="th-onboarding-description">
                It looks like notifications are blocked. You can enable them
                anytime in Settings → Notifications if you'd like gentle
                reminders for the moments that matter.
              </p>
            </div>
            <div className="th-onboarding-actions th-stagger-item">
              <Button variant="primary" full onClick={handleOpenSettings}>
                Open Settings
              </Button>
              <Button variant="ghost" full onClick={handleSkip}>
                Skip for now
              </Button>
            </div>
          </div>
        </OnboardingLayout>
      );
    }
    // inline mode: show compact denied state
    return (
      <div className="th-permission-denied-inline">
        <p className="th-permission-denied-inline__text">
          Notifications are blocked. You can enable them in Settings → Notifications.
        </p>
        <Button variant="ghost" full onClick={handleSkip}>
          Continue without notifications
        </Button>
      </div>
    );
  }

  // Prompt (default — never requested)
  if (mode === 'onboarding') {
    return (
      <OnboardingLayout currentPath="/onboarding/notifications" showBack={false}>
        <RoseLilyDecoration variant={5} size={100} position="top-right" opacity={0.12} />
        <div className="th-onboarding-form">
          <div className="th-stagger-item" style={{ textAlign: 'center' }}>
            <div className="th-welcome-illustration" aria-hidden="true">
              <OnboardingArt variant="paired-hearts-check" size={64} />
            </div>
            <h2 className="th-onboarding-heading">Stay connected</h2>
            <p className="th-onboarding-description">
              TwoHearts can send you gentle reminders for anniversaries,
              important dates, and the little things you don't want to forget.
            </p>
          </div>

          <div className="th-card th-permission-prompt-card th-stagger-item">
            <h3 className="th-permission-prompt-card__title">What you'll get</h3>
            <ul className="th-permission-prompt-card__list">
              <li className="th-permission-prompt-card__item">
                <span className="th-permission-prompt-card__icon" aria-hidden="true"><IconHeart size={14} /></span>
                <span>Anniversary reminders</span>
              </li>
              <li className="th-permission-prompt-card__item">
                <span className="th-permission-prompt-card__icon" aria-hidden="true"><IconBell size={14} /></span>
                <span>Reminder alerts</span>
              </li>
              <li className="th-permission-prompt-card__item">
                <span className="th-permission-prompt-card__icon" aria-hidden="true"><IconCheck size={14} /></span>
                <span>Period tracker notifications</span>
              </li>
            </ul>
            <p className="th-permission-prompt-card__privacy">
              Everything stays on your device. Nothing is sent to a server.
            </p>
          </div>

          <div className="th-onboarding-actions th-stagger-item">
            <Button variant="primary" full onClick={() => void handleAllow()} disabled={loading}>
              {loading ? 'Enabling…' : 'Allow Notifications'}
            </Button>
            <Button variant="ghost" full onClick={handleSkip} disabled={loading}>
              Maybe later
            </Button>
          </div>
        </div>
      </OnboardingLayout>
    );
  }

  // Inline mode — compact prompt
  return (
    <div className="th-permission-prompt-inline">
      <div className="th-permission-prompt-inline__body">
        <p className="th-permission-prompt-inline__text">
          Enable notifications so you don't miss reminders and anniversaries?
        </p>
      </div>
      <div className="th-permission-prompt-inline__actions">
        <Button variant="primary" onClick={() => void handleAllow()} disabled={loading}>
          {loading ? 'Enabling…' : 'Allow'}
        </Button>
        <Button variant="ghost" onClick={handleSkip} disabled={loading}>
          Skip
        </Button>
      </div>
    </div>
  );
}
