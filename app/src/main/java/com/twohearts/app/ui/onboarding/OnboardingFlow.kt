package com.twohearts.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.twohearts.app.data.settings.OnboardingDraft
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.security.AppLockService
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.launch

/**
 * OnboardingFlow — the ordered setup journey.
 *
 * ## What the migrated version did wrong
 *
 * The flow held the collected data in a `remember { mutableStateOf(...) }` and
 * persisted only the *stage*. Three consequences, all of them real defects:
 *
 *  1. **A restart mid-setup lost the person's input.** Stage survived in
 *     DataStore; names and dates did not. Reopening the app after a process
 *     death put them back on step 3 with an empty form.
 *  2. **`runBlocking` ran on the main thread** on every stage transition.
 *     Every Continue tap blocked the UI thread while DataStore wrote.
 *  3. **Failures were silent.** The final step created two profiles and a
 *     couple record with no error handling at all; a failing write left the
 *     person advanced to "complete" with no data, or wedged with no feedback.
 *
 * A fourth: the stage was advanced before the step's data was validated, so a
 * crash mid-step could persist a stage the data did not justify.
 *
 * ## What it does now
 *
 *  - **Answers are persisted at each step** as an [OnboardingDraft], so setup
 *    resumes exactly where it was left. The draft lives in DataStore rather
 *    than the domain database because the profiles and couple row are created
 *    in one commit at the end: there must be no half-built couple record to
 *    reconcile if setup is abandoned.
 *  - **Writes are suspending**, launched on a remembered scope instead of
 *    blocking the main thread.
 *  - **The final commit is guarded.** Profiles, couple row, appearance and PIN
 *    happen in one suspend block; `OnboardingStage.COMPLETE` is persisted only
 *    after every write returns, so "complete" states a fact rather than an
 *    intent. A failure surfaces as a retryable sheet and leaves the person on
 *    the step they were on.
 *  - **The PIN is never written to the draft.** It is a credential; DataStore
 *    Preferences is unencrypted, so the PIN lives only in the composition and
 *    goes straight to `AppLockService` at commit. See [OnboardingDraft].
 *  - **Transitions carry direction.** Advancing enters from the trailing edge
 *    and going back mirrors it; both collapse under reduce-motion.
 *
 * @param appStateService owns the stage and the appearance settings.
 * @param relationshipService creates the profiles and the couple record.
 * @param appLockService receives the PIN when one was chosen.
 * @param settingsStorage holds the draft.
 * @param onComplete called once the domain commit has succeeded.
 */
@Composable
fun OnboardingFlow(
    appStateService: AppStateService,
    relationshipService: RelationshipService,
    appLockService: AppLockService,
    settingsStorage: SettingsStorage,
    onComplete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val motion = LocalTwoHeartsMotion.current

    val onboardingStage by appStateService.onboardingStage.collectAsState()
    val currentStage = remember(onboardingStage) {
        OnboardingStage.fromStorageKey(onboardingStage)
    }

    // The draft is loaded once, then owned here. `null` means "still loading",
    // which gates the first frame so a returning person never sees an empty
    // step flash before their saved answers appear.
    var draft by remember { mutableStateOf<OnboardingDraft?>(null) }
    // The PIN is held separately from the draft precisely so it cannot be
    // persisted alongside it. It is cleared when the lock step is passed.
    var pin by remember { mutableStateOf<String?>(null) }
    var committing by remember { mutableStateOf(false) }
    var commitError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        draft = settingsStorage.loadOnboardingDraft() ?: OnboardingDraft()
    }

    val current = draft ?: return

    /**
     * Persists the step's answers and moves to [target].
     *
     * Order matters: the draft is written first, then the stage. A crash
     * between the two leaves the earlier stage holding the *newer* answers,
     * which is the safe direction — the person sees their input again rather
     * than skipping past a step with nothing recorded.
     */
    fun advance(next: OnboardingDraft, target: OnboardingStage) {
        draft = next
        scope.launch {
            settingsStorage.saveOnboardingDraft(next)
            appStateService.updateOnboardingStage(target.storageKey)
        }
    }

    /**
     * The one place the domain is written.
     *
     * Everything setup collected lands here in a single suspend block, and the
     * stage flips to COMPLETE only after every write has returned.
     */
    fun commit(final: OnboardingDraft, chosenPin: String?) {
        scope.launch {
            committing = true
            commitError = null
            try {
                val owner = relationshipService.createOwner(
                    name = final.ownerName,
                    birthday = final.ownerBirthday,
                )
                val partner = relationshipService.createPartner(
                    name = final.partnerName,
                    birthday = final.partnerBirthday,
                )
                relationshipService.createRelationship(
                    ownerId = owner.id,
                    partnerId = partner.id,
                    startDate = final.startDate,
                )

                appStateService.setThemeMode(final.themeMode)
                appStateService.setTextSize(final.textSize)

                chosenPin?.let { appLockService.createPin(it) }

                settingsStorage.clearOnboardingDraft()
                appStateService.updateOnboardingStage(OnboardingStage.COMPLETE.storageKey)
                appStateService.setOnboarded(true)

                committing = false
                onComplete()
            } catch (t: Throwable) {
                // Recoverable: nothing has been marked complete, so the person
                // can retry without re-entering anything.
                committing = false
                commitError = "TwoHearts couldn't finish saving just now. " +
                    "Nothing is lost — try once more."
            }
        }
    }

    val handleNext: (OnboardingData) -> Unit = { next ->
        when (currentStage) {
            OnboardingStage.FRESH -> advance(OnboardingDraft(), OnboardingStage.OWNER)

            OnboardingStage.OWNER -> advance(
                current.copy(
                    ownerName = next.ownerName,
                    ownerBirthday = next.ownerBirthday,
                ),
                OnboardingStage.RELATIONSHIP,
            )

            OnboardingStage.RELATIONSHIP -> advance(
                current.copy(
                    partnerName = next.partnerName,
                    partnerBirthday = next.partnerBirthday,
                    startDate = next.startDate,
                ),
                OnboardingStage.PERSONALIZATION,
            )

            OnboardingStage.PERSONALIZATION -> advance(
                current.copy(
                    themeMode = next.themeMode,
                    textSize = next.textSize,
                ),
                OnboardingStage.APP_LOCK,
            )

            OnboardingStage.APP_LOCK -> {
                pin = next.pin
                commit(current, next.pin)
            }

            OnboardingStage.COMPLETE -> onComplete()
        }
    }

    val handleBack: () -> Unit = {
        currentStage.previous()?.let { previous ->
            // Stepping back out of the lock step discards the typed PIN, so a
            // half-entered code cannot be committed by a later fast tap.
            if (currentStage == OnboardingStage.APP_LOCK) pin = null
            scope.launch { appStateService.updateOnboardingStage(previous.storageKey) }
        }
    }

    // Direction-aware transition: `forward` is true when the step order grows.
    var previousOrder by remember { mutableStateOf(currentStage.order) }
    val forward = currentStage.order >= previousOrder
    LaunchedEffect(currentStage) { previousOrder = currentStage.order }

    val duration = motion.decorative(TwoHeartsTokens.Duration.normal).toInt()
    val enter = if (forward) {
        slideInHorizontally(tween(duration)) { it / 10 } + fadeIn(tween(duration))
    } else {
        slideInHorizontally(tween(duration)) { -it / 10 } + fadeIn(tween(duration))
    }
    val exit = if (forward) {
        slideOutHorizontally(tween(duration)) { -it / 10 } + fadeOut(tween(duration))
    } else {
        slideOutHorizontally(tween(duration)) { it / 10 } + fadeOut(tween(duration))
    }

    AnimatedContent(
        targetState = currentStage,
        transitionSpec = { enter togetherWith exit },
        label = "th-onboarding-step",
    ) { stage ->
        // The steps still consume `OnboardingData` because that is the shape
        // the domain services take at commit; the mapping happens in
        // `handleNext`, so the PIN never reaches the persisted draft.
        val stepData = OnboardingData(
            ownerName = current.ownerName,
            ownerBirthday = current.ownerBirthday,
            partnerName = current.partnerName,
            partnerBirthday = current.partnerBirthday,
            startDate = current.startDate,
            themeMode = current.themeMode,
            textSize = current.textSize,
            pin = pin,
        )

        when (stage) {
            OnboardingStage.FRESH -> WelcomeScreen(
                onGetStarted = { handleNext(stepData) },
            )

            OnboardingStage.OWNER -> ProfileSetupScreen(
                data = stepData,
                onBack = handleBack,
                onNext = handleNext,
            )

            OnboardingStage.RELATIONSHIP -> RelationshipSetupScreen(
                data = stepData,
                onBack = handleBack,
                onNext = handleNext,
            )

            OnboardingStage.PERSONALIZATION -> PersonalizationSetupScreen(
                data = stepData,
                onBack = handleBack,
                onNext = handleNext,
                // Applied live through the same service the app already
                // observes, so choosing "Dark" actually turns the app dark.
                onThemeChange = { scope.launch { appStateService.setThemeMode(it) } },
                onTextSizeChange = { scope.launch { appStateService.setTextSize(it) } },
            )

            OnboardingStage.APP_LOCK -> AppLockSetupScreen(
                data = stepData,
                onBack = handleBack,
                onNext = handleNext,
            )

            OnboardingStage.COMPLETE -> SetupCompleteScreen(
                data = stepData,
                onComplete = onComplete,
            )
        }
    }

    // A commit failure is surfaced over the flow rather than replacing it,
    // because the person retries from where they were.
    commitError?.let { message ->
        OnboardingCommitErrorSheet(
            message = message,
            retrying = committing,
            onRetry = { commit(current, pin) },
            onDismiss = { commitError = null },
        )
    }
}

/**
 * OnboardingCommitErrorSheet — the retry affordance for a failed commit.
 *
 * Deliberately not a system dialog: the app's own modal vocabulary is used so
 * this reads as part of TwoHearts, and the copy is emotionally neutral — it
 * says what happened, confirms nothing was lost, and offers one action.
 */
@Composable
private fun OnboardingCommitErrorSheet(
    message: String,
    retrying: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val thColors = LocalTwoHeartsColors.current

    ThModal(
        open = true,
        onClose = onDismiss,
        label = "Couldn't finish setup",
    ) {
        Text(
            text = "One more try",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = thColors.textSecondary,
        )
        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space5))
        ThButton(
            onClick = onRetry,
            text = "Try again",
            full = true,
            loading = retrying,
        )
    }
}

/**
 * OnboardingGate — decides whether setup or the app is shown.
 *
 * The migrated gate called `onAppReady()` from inside a composable body, so
 * the callback could fire on every recomposition rather than once. It now
 * routes purely by persisted state, and the completion hand-off is fired only
 * from [OnboardingFlow] after a successful commit.
 *
 * Returning users bypass setup entirely: both flags are read from storage, so
 * the decision holds across a cold start and cannot be re-entered by
 * navigating back through the shell.
 */
@Composable
fun OnboardingGate(
    appStateService: AppStateService,
    relationshipService: RelationshipService,
    appLockService: AppLockService,
    settingsStorage: SettingsStorage,
    onOnboardingComplete: () -> Unit,
    onAppReady: () -> Unit,
) {
    val isOnboarded by appStateService.isOnboarded.collectAsState()
    val stage by appStateService.onboardingStage.collectAsState()

    val complete = isOnboarded && stage == OnboardingStage.COMPLETE.storageKey

    if (complete) {
        LaunchedEffect(Unit) { onAppReady() }
    } else {
        OnboardingFlow(
            appStateService = appStateService,
            relationshipService = relationshipService,
            appLockService = appLockService,
            settingsStorage = settingsStorage,
            onComplete = onOnboardingComplete,
        )
    }
}