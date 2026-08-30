/**
 * WelcomeScreen — the First Launch experience (reference 02-Welcome-FirstLaunch).
 *
 * A full-screen branded welcome, intentionally composed WITHOUT the standard
 * onboarding header/dots: first launch is the emotional front door of the
 * product, not a form step. The official brand lockup, the replaceable
 * onboarding welcome photo (MasterPrompt §20 — src/assets/images/), and
 * Rose/Lily florals establish the TwoHearts identity before setup begins.
 *
 * Flow is unchanged: Get Started advances to the owner-profile step.
 */

import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  BrandLogo,
  Button,
  IconHeart,
  RoseLilyDecoration,
} from '../../components/index.ts';
import welcomePhotoUrl from '../../assets/images/onboarding-welcome-photo.svg';
import { useOnboarding } from './useOnboarding.ts';

export function WelcomeScreen() {
  const navigate = useNavigate();
  const { advanceStage } = useOnboarding();

  const handleGetStarted = () => {
    advanceStage('owner');
    navigate(RoutePath.onboardingProfile);
  };

  return (
    <div className="th-screen th-welcome">
      {/* Rose/Lily signature florals framing the moment */}
      <RoseLilyDecoration variant={11} size={150} position="top-right" opacity={0.55} animated />
      <RoseLilyDecoration variant={1} size={130} position="bottom-left" opacity={0.4} animated />

      <div className="th-scroll th-welcome__inner">
        {/* Official brand lockup (includes the YOURS. ALWAYS. tagline) */}
        <header className="th-welcome__brand th-stagger-item">
          <BrandLogo variant="brand" size={160} title="TwoHearts" />
        </header>

        {/* Replaceable owner asset — MasterPrompt §20 */}
        <img
          className="th-welcome__hero th-stagger-item"
          src={welcomePhotoUrl}
          alt="A scrapbook of couple memories — photos, a sealed letter, and a rose"
        />

        <h1 className="th-welcome__title th-stagger-item">
          Welcome to <span className="th-welcome__title-brand">TwoHearts</span>
        </h1>

        <div className="th-welcome__divider th-stagger-item" aria-hidden="true">
          <span className="th-welcome__divider-line" />
          <IconHeart size={14} />
          <span className="th-welcome__divider-line" />
        </div>

        <p className="th-welcome__text th-stagger-item">
          This is our private space, just for the two of us. Keep memories,
          notes, moments, reminders, games, and meaningful experiences together.
        </p>

        <div className="th-welcome__actions th-stagger-item">
          <Button variant="primary" full onClick={handleGetStarted}>
            <IconHeart size={18} />
            Get Started
          </Button>
          <p className="th-welcome__privacy">Private · Offline · On this device</p>
        </div>
      </div>
    </div>
  );
}
