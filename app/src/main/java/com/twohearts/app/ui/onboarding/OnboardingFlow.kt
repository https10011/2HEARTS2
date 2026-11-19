package com.twohearts.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.settings.OnboardingDraft
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.ProfilePhotoStaging
import com.twohearts.app.services.media.ProfilePhotoWriter
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.security.AppLockService
import com.twohearts.app.services.security.AppLockState
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.components.rememberNotificationPermissionControl
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Restores setup answers and saves each stage before allowing the next one. */
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
    val context = LocalContext.current.applicationContext
    val stageKey by appStateService.onboardingStage.collectAsState()
    val currentStage = OnboardingStage.fromStorageKey(stageKey)
    var draft by remember { mutableStateOf<OnboardingDraft?>(null) }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        val restored = settingsStorage.loadOnboardingDraft() ?: OnboardingDraft()
        // Older builds wrote the two completion flags separately. Recover the
        // prior commit if the process stopped between those writes.
        if (currentStage == OnboardingStage.COMPLETE && !appStateService.isOnboarded.value) {
            val owner = relationshipService.getOwner()
            val partner = relationshipService.getPartner()
            val relationship = relationshipService.getRelationship()
            if (owner != null && partner != null && relationship != null) {
                val recovered = restored.copy(
                    ownerName = owner.name, ownerBirthday = owner.birthday,
                    partnerName = partner.name, partnerBirthday = partner.birthday,
                    startDate = relationship.startDate, themeMode = appStateService.themeMode.value,
                    textSize = appStateService.textSize.value,
                )
                draft = recovered
                appStateService.finishOnboarding(recovered)
            } else {
                draft = restored
                appStateService.saveOnboardingStep(restored, OnboardingStage.OWNER.storageKey)
            }
        } else {
            val resumeStage = resumeOnboardingStage(currentStage, restored)
            if (resumeStage != currentStage) appStateService.saveOnboardingStep(restored, resumeStage.storageKey)
            draft = restored
        }
    }

    val current = draft ?: return

    fun write(message: String, action: suspend () -> Unit) {
        if (working) return
        working = true
        error = null
        retry = null
        scope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                error = message
                retry = { write(message, action) }
            } finally {
                working = false
            }
        }
    }

    fun advance(next: OnboardingDraft, target: OnboardingStage) {
        write("Couldn’t save this setup step. Please try again.") {
            appStateService.saveOnboardingStep(next, target.storageKey)
            draft = next
        }
    }

    fun saveDraft(next: OnboardingDraft) {
        val previous = draft
        write("Couldn’t save these choices. Please try again.") {
            settingsStorage.saveOnboardingDraft(next)
            draft = next
            val keep = listOfNotNull(next.ownerPhotoDraft, next.partnerPhotoDraft)
            val obsolete = listOfNotNull(previous?.ownerPhotoDraft, previous?.partnerPhotoDraft).toSet() - keep.toSet()
            val staging = ProfilePhotoStaging(context)
            obsolete.forEach { staging.discard(it) }
        }
    }

    fun finish(final: OnboardingDraft, pin: String?) {
        write("Couldn’t finish saving setup. You can try again from this step.") {
            val db = TwoHeartsDatabase.getDatabase(context)
            val staging = ProfilePhotoStaging(context)
            val writer = ProfilePhotoWriter(db, MediaStorage(context), staging)
            OnboardingDomainWriter(writer, staging, relationshipService).save(final)
            if (pin != null) withContext(Dispatchers.IO) { appLockService.createPin(pin) }
            appStateService.finishOnboarding(final)
        }
    }

    fun next(stage: OnboardingStage, answers: OnboardingData) {
        if (stage != currentStage || working) return
        val latest = draft ?: return
        when (stage) {
            OnboardingStage.FRESH -> advance(latest, OnboardingStage.OWNER)
            OnboardingStage.OWNER -> advance(
                latest.copy(ownerName = answers.ownerName, ownerBirthday = answers.ownerBirthday),
                OnboardingStage.RELATIONSHIP,
            )
            OnboardingStage.RELATIONSHIP -> advance(
                latest.copy(partnerName = answers.partnerName, partnerBirthday = answers.partnerBirthday, startDate = answers.startDate),
                OnboardingStage.PERSONALIZATION,
            )
            OnboardingStage.PERSONALIZATION -> advance(
                latest.copy(themeMode = answers.themeMode, textSize = answers.textSize),
                OnboardingStage.CAPABILITIES,
            )
            OnboardingStage.CAPABILITIES -> advance(latest, OnboardingStage.APP_LOCK)
            OnboardingStage.APP_LOCK -> finish(latest, answers.pin)
            OnboardingStage.COMPLETE -> onComplete()
        }
    }

    fun back(stage: OnboardingStage) {
        if (stage != currentStage || working) return
        val latest = draft ?: return
        stage.previous()?.let { advance(latest, it) }
    }

    BackHandler(enabled = currentStage.order in 1..OnboardingStage.LAST_SETUP_ORDER) { back(currentStage) }

    var previousOrder by remember { mutableStateOf(currentStage.order) }
    val forward = currentStage.order >= previousOrder
    LaunchedEffect(currentStage) { previousOrder = currentStage.order }
    val duration = motion.decorative(TwoHeartsTokens.Duration.normal).toInt()
    val enter = slideInHorizontally(tween(duration)) { if (forward) it / 10 else -it / 10 } + fadeIn(tween(duration))
    val exit = slideOutHorizontally(tween(duration)) { if (forward) -it / 10 else it / 10 } + fadeOut(tween(duration))

    AnimatedContent(
        targetState = currentStage,
        transitionSpec = { enter togetherWith exit },
        label = "th-onboarding-step",
    ) { stage ->
        val stepData = OnboardingData(
            ownerName = current.ownerName,
            ownerBirthday = current.ownerBirthday,
            partnerName = current.partnerName,
            partnerBirthday = current.partnerBirthday,
            startDate = current.startDate,
            themeMode = current.themeMode,
            textSize = current.textSize,
            ownerPhotoDraft = current.ownerPhotoDraft,
            partnerPhotoDraft = current.partnerPhotoDraft,
        )
        val onBack: () -> Unit = { back(stage) }
        val onNext: (OnboardingData) -> Unit = { next(stage, it) }
        when (stage) {
            OnboardingStage.FRESH -> WelcomeScreen(onGetStarted = { onNext(stepData) })
            OnboardingStage.OWNER -> ProfileSetupScreen(
                data = stepData, onBack = onBack, onNext = onNext, saving = working,
                onPhotoDraftChanged = { id ->
                    if (stage == currentStage) draft?.let { saveDraft(it.copy(ownerPhotoDraft = id)) }
                },
            )
            OnboardingStage.RELATIONSHIP -> RelationshipSetupScreen(
                data = stepData, onBack = onBack, onNext = onNext, saving = working,
                onPhotoDraftChanged = { id ->
                    if (stage == currentStage) draft?.let { saveDraft(it.copy(partnerPhotoDraft = id)) }
                },
            )
            OnboardingStage.PERSONALIZATION -> PersonalizationSetupScreen(
                data = stepData, onBack = onBack, onNext = onNext,
                onThemeChange = { scope.launch { appStateService.setThemeMode(it) } },
                onTextSizeChange = { scope.launch { appStateService.setTextSize(it) } },
            )
            OnboardingStage.CAPABILITIES -> {
                val permissions = rememberNotificationPermissionControl(
                    previouslyRequested = current.notificationPrompted,
                    onRequested = { draft?.let { saveDraft(it.copy(notificationPrompted = true)) } },
                )
                CapabilitiesSetupScreen(permissions, onBack, onNext = { onNext(stepData) })
            }
            OnboardingStage.APP_LOCK -> AppLockSetupScreen(data = stepData, onBack = onBack, onNext = onNext, saving = working)
            OnboardingStage.COMPLETE -> {
                val owner by remember(relationshipService) { relationshipService.observeOwner() }.collectAsState(initial = null)
                val partner by remember(relationshipService) { relationshipService.observePartner() }.collectAsState(initial = null)
                val lockState by appLockService.lockState.collectAsState()
                SetupCompleteScreen(
                    data = stepData,
                    onComplete = onComplete,
                    ownerPhotoRef = owner?.photoRef,
                    partnerPhotoRef = partner?.photoRef,
                    pinConfigured = lockState != AppLockState.DISABLED,
                )
            }
        }
    }

    error?.let { message ->
        OnboardingCommitErrorSheet(
            message = message,
            retrying = working,
            onRetry = { retry?.invoke() },
            onDismiss = {
                error = null
                retry = null
            },
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
