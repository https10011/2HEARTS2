/**
 * Application state service (Phase 4).
 *
 * One owner of the first-launch / setup-completion truth:
 *
 * - `markFirstLaunchIfNeeded()` stamps `firstLaunchAt` exactly once (UTC
 *   ISO 8601) — bootstrap calls it; reset() never erases it.
 * - `reconcileOnboardingStage()` derives the persisted onboarding stage
 *   from DOMAIN TRUTH (profiles + couple row) so a killed app resumes
 *   setup where it really is, and partial installs can't claim
 *   'complete'.
 * - `completeSetup()` is the terminal setter — it verifies the domain is
 *   actually configured (owner + partner + start date) before allowing
 *   'complete'. Future onboarding screens call exactly this.
 * - `getSnapshot()` is the read model future routing/UI consumes: first
 *   launch flag, stage, theme/size preferences, lock-config summary.
 *
 * Layer discipline: this service reads via repositories ONLY and writes
 * preference state via `appSettingsStore` ONLY — no SQL, no localStorage,
 * no raw storage keys leak to callers.
 */

import type { DatabaseAdapter } from '../../data/database/adapter.ts';
import {
  appSettingsStore,
  type AppSettings,
  type OnboardingStage,
} from '../../core/appSettings.ts';
import { CoupleRepository } from '../../repositories/coupleRepository.ts';
import { ProfileRepository } from '../../repositories/profileRepository.ts';
import { nowIso, systemClock, type Clock } from '../../utils/time.ts';
import { AppError } from '../errors/appError.ts';

export interface AppStateSnapshot {
  /** True once the app has stamped a first-launch timestamp. */
  hasLaunchedBefore: boolean;
  firstLaunchAt: string | null;
  onboardingStage: OnboardingStage;
  setupComplete: boolean;
  /** Preferences surfaced for convenience (preferences, not domain data). */
  textSize: AppSettings['textSize'];
  themeMode: AppSettings['themeMode'];
  appLockEnabled: boolean;
}

export class AppStateService {
  private readonly profiles: ProfileRepository;
  private readonly couple: CoupleRepository;

  constructor(
    db: DatabaseAdapter,
    private readonly clock: Clock = systemClock,
  ) {
    this.profiles = new ProfileRepository(db, clock);
    this.couple = new CoupleRepository(db, clock);
  }

  /** Stamps the first-launch timestamp once; safe to call every boot. */
  markFirstLaunchIfNeeded(): void {
    appSettingsStore.markFirstLaunch(nowIso(this.clock));
  }

  /**
   * Recomputes the onboarding stage from domain truth:
   *   'complete' (persisted) stays 'complete';
   *   otherwise missing owner → 'owner', incomplete relationship
   *   (partner/start date) → 'relationship', everything else →
   *   'personalization'.
   * Never downgrades a completed setup (that is an explicit reset decision).
   */
  async reconcileOnboardingStage(): Promise<OnboardingStage> {
    const persisted = appSettingsStore.getState().onboardingStage;
    if (persisted === 'complete') return 'complete';

    const suggested = await this.deriveStage();
    appSettingsStore.setOnboardingStage(suggested);
    return suggested;
  }

  /** Terminal setter used by the (future) onboarding flow. */
  async completeSetup(): Promise<void> {
    const stage = await this.deriveStage();
    if (stage !== 'personalization') {
      throw new AppError('validation', 'setup-incomplete', {
        recoverable: true,
        userMessage: 'Setup is not complete yet.',
        cause: { missingStage: stage },
      });
    }
    appSettingsStore.setOnboardingStage('complete');
  }

  /** Current derived stage — what setup would need next. */
  async currentStage(): Promise<OnboardingStage> {
    if (appSettingsStore.getState().onboardingStage === 'complete') return 'complete';
    return this.deriveStage();
  }

  getSnapshot(): AppStateSnapshot {
    const s = appSettingsStore.getState();
    return {
      hasLaunchedBefore: s.firstLaunchAt !== null,
      firstLaunchAt: s.firstLaunchAt,
      onboardingStage: s.onboardingStage,
      setupComplete: s.onboardingStage === 'complete',
      textSize: s.textSize,
      themeMode: s.themeMode,
      appLockEnabled: s.appLockEnabled,
    };
  }

  private async deriveStage(): Promise<OnboardingStage> {
    const [owner, couple] = await Promise.all([this.profiles.getOwner(), this.couple.get()]);
    if (!owner) return 'owner';
    if (!couple?.partnerProfileId || !couple.startDate) return 'relationship';
    return 'personalization';
  }
}
