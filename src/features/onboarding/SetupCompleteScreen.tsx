/**
 * SetupCompleteScreen (Phase 5; Stage 2 visual productization; visual restoration per reference 07).
 *
 * Final onboarding screen: paired-hearts celebration, a truthful summary
 * card of what was set up (app-lock reflects the user's actual choice),
 * and the entrance into the app.
 *
 * Stage 2: Fully responsive composition — content flows naturally on any
 * viewport size, aspect ratio, text scale, or safe-area condition. No fixed
 * viewport heights or absolute positions. The layout uses flex centering
 * with comfortable padding so the celebration feels intentional on both
 * short (600px) and tall (2400px) devices.
 */

import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  Button,
  IconCheck,
  IconHeart,
  OnboardingArt,
  RoseLilyDecoration,
} from '../../components/index.ts';
import { appSettingsStore } from '../../core/appSettings.ts';
import { OnboardingLayout } from './OnboardingLayout.tsx';

export function SetupCompleteScreen() {
  const navigate = useNavigate();
  const appLockEnabled = appSettingsStore.getState().appLockEnabled;

  const handleEnter = () => {
    navigate(RoutePath.appHome);
  };

  const summaryItems: { label: string; detail?: string }[] = [
    { label: 'Your profile' },
    { label: 'Your special someone' },
    { label: 'Your connection' },
    { label: 'Your preferences' },
    { label: 'App lock', detail: appLockEnabled ? 'On' : 'Skipped' },
  ];

  return (
    <OnboardingLayout currentPath={RoutePath.onboardingComplete} showBack={false}>
      {/* Celebration florals — positioned absolutely to not affect flow */}
      <RoseLilyDecoration variant={11} size={130} position="top-right" opacity={0.2} animated />
      <RoseLilyDecoration variant={1} size={120} position="bottom-left" opacity={0.15} animated />

      <div className="th-setup-complete">
        {/* Celebration illustration — fluid sizing via CSS */}
        <div className="th-setup-complete__illustration th-stagger-item">
          <OnboardingArt variant="paired-hearts-check" size={170} />
        </div>

        <h2 className="th-onboarding-heading th-stagger-item">You're all set</h2>
        <p className="th-setup-complete__subtitle th-stagger-item">
          Your TwoHearts space is ready.
        </p>
        <p className="th-onboarding-description th-stagger-item">
          Everything is set up. Start making memories, sharing notes, and
          enjoying your space together.
        </p>

        {/* Branded summary card */}
        <div className="th-card th-setup-complete__card th-stagger-item">
          <h3 className="th-setup-complete__card-title">Your setup is complete</h3>
          <ul className="th-setup-complete__list">
            {summaryItems.map((item) => (
              <li key={item.label} className="th-setup-complete__item">
                <span className="th-setup-complete__check" aria-hidden="true">
                  <IconCheck size={14} />
                </span>
                <span className="th-setup-complete__label">{item.label}</span>
                {item.detail && (
                  <span className="th-setup-complete__detail">{item.detail}</span>
                )}
              </li>
            ))}
          </ul>
        </div>

        {/* Emotional sign-off */}
        <div className="th-setup-complete__signoff th-stagger-item">
          <IconHeart size={16} />
          <p>Welcome to your little space.</p>
        </div>

        {/* Primary action */}
        <div className="th-onboarding-actions th-stagger-item">
          <Button variant="primary" full onClick={handleEnter}>
            <IconHeart size={16} />
            Enter TwoHearts
          </Button>
        </div>
      </div>
    </OnboardingLayout>
  );
}
