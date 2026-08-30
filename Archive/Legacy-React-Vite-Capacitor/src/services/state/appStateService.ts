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

/**
 * Canonical ordering of onboarding stages. `reconcileOnboardingStage`
 * only upgrades (moves forward) — never downgrades a stage that has
 * already been reached. This is the single source of truth for the
 * progression sequence used by both the gate and the reconciler.
 */
const STAGE_ORDER: readonly OnboardingStage[] = [
  'fresh',
  'owner',
  'relationship',
  'personalization',
  'complete',
] as const;

function stageIndex(stage: OnboardingStage): number {
  return STAGE_ORDER.indexOf(stage);
}

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
   * Recomputes the onboarding stage from domain truth, but only ever
   * **advances** the stage — never downgrades. This is critical for two
   * reasons:
   *
   * 1. `deriveStage()` checks what domain data exists in the database.
   *    For a brand-new install there is no owner profile, so it returns
   *    `'fresh'`. If we unconditionally overwrote the persisted stage,
   *    the Welcome screen would be skipped (the original Stage 1 bug).
   *
   * 2. During development, in-memory databases (sql.js) can be reset by
   *    a page reload while localStorage persists. The persisted stage
   *    may be ahead of what the database currently shows. Advancing-only
   *    prevents a confused gate from sending the user backwards.
   *
   * The `'complete'` guard stays: once setup is done, it never reverts
   * (that is an explicit reset decision, not a reconciliation).
   */
  async reconcileOnboardingStage(): Promise<OnboardingStage> {
    const persisted = appSettingsStore.getState().onboardingStage;
    if (persisted === 'complete') return 'complete';

    const suggested = await this.deriveStage();
    const suggestedIdx = stageIndex(suggested);
    const persistedIdx = stageIndex(persisted);

    // Only advance: the domain truth is at least as far along as our
    // last known stage.  This preserves the Welcome screen for fresh
    // installs (deriveStage returns 'fresh', which equals persisted
    // 'fresh', so no overwrite happens) while still catching up a
    // stalled persisted state when real domain data exists.
    if (suggestedIdx > persistedIdx) {
      appSettingsStore.setOnboardingStage(suggested);
    }

    return suggestedIdx > persistedIdx ? suggested : persisted;
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

  /**
   * Derives the onboarding stage from **domain truth** only — what actually
   * exists in the database right now. The returned stage represents the
   * *minimum* stage the user needs to reach (or has reached) based on
   * their real data:
   *
   *   `'fresh'`         — no owner profile yet (brand-new install)
   *   `'owner'`          — owner profile exists, relationship incomplete
   *   `'relationship'`   — owner + couple, personalisation not done
   *   `'personalization'` — everything set up, ready to complete
   *
   * The caller (reconcileOnboardingStage) compares this against the
   * persisted stage and only advances — it never downgrades.
   */
  private async deriveStage(): Promise<OnboardingStage> {
    const [owner, couple] = await Promise.all([this.profiles.getOwner(), this.couple.get()]);
    if (!owner) return 'fresh';
    if (!couple?.partnerProfileId || !couple.startDate) return 'relationship';
    return 'personalization';
  }
}
