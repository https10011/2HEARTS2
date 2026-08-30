package com.twohearts.app.ui.onboarding

import androidx.compose.runtime.*
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.security.AppLockService
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.data.repository.generateId

/**
 * OnboardingFlow — complete onboarding flow management.
 *
 * Matches legacy OnboardingGate + useOnboarding hook with:
 * - State evaluation
 * - Stage progression
 * - Data persistence
 * - Navigation control
 */
@Composable
fun OnboardingFlow(
    appStateService: AppStateService,
    relationshipService: RelationshipService,
    appLockService: AppLockService,
    onComplete: () -> Unit
) {
    // Get current onboarding stage
    val onboardingStage by appStateService.onboardingStage.collectAsState()
    val currentStage = remember(onboardingStage) {
        when (onboardingStage) {
            "fresh" -> OnboardingStage.FRESH
            "owner" -> OnboardingStage.OWNER
            "relationship" -> OnboardingStage.RELATIONSHIP
            "personalization" -> OnboardingStage.PERSONALIZATION
            "app-lock" -> OnboardingStage.APP_LOCK
            "complete" -> OnboardingStage.COMPLETE
            else -> OnboardingStage.FRESH
        }
    }

    // Onboarding data
    var onboardingData by remember { mutableStateOf(OnboardingData()) }

    // Handle stage transitions
    val handleNext: (OnboardingData) -> Unit = remember(currentStage) {
        { data ->
            onboardingData = data

            when (currentStage) {
                OnboardingStage.FRESH -> {
                    // Move to owner setup
                    kotlinx.coroutines.runBlocking {
                        appStateService.updateOnboardingStage("owner")
                    }
                }
                OnboardingStage.OWNER -> {
                    // Move to relationship setup
                    kotlinx.coroutines.runBlocking {
                        appStateService.updateOnboardingStage("relationship")
                    }
                }
                OnboardingStage.RELATIONSHIP -> {
                    // Move to personalization
                    kotlinx.coroutines.runBlocking {
                        appStateService.updateOnboardingStage("personalization")
                    }
                }
                OnboardingStage.PERSONALIZATION -> {
                    // Move to app lock
                    kotlinx.coroutines.runBlocking {
                        appStateService.updateOnboardingStage("app-lock")
                    }
                }
                OnboardingStage.APP_LOCK -> {
                    // Complete onboarding
                    kotlinx.coroutines.runBlocking {
                        // Create owner profile
                        val owner = relationshipService.createOwner(
                            name = data.ownerName,
                            birthday = data.ownerBirthday
                        )

                        // Create partner profile
                        val partner = relationshipService.createPartner(
                            name = data.partnerName,
                            birthday = data.partnerBirthday
                        )

                        // Create relationship
                        relationshipService.createRelationship(
                            ownerId = owner.id,
                            partnerId = partner.id,
                            startDate = data.startDate
                        )

                        // Set theme and text size
                        appStateService.setThemeMode(data.themeMode)
                        appStateService.setTextSize(data.textSize)

                        // Set up app lock if PIN provided
                        if (data.pin != null) {
                            appLockService.createPin(data.pin)
                        }

                        // Mark onboarding as complete
                        appStateService.updateOnboardingStage("complete")
                        appStateService.setOnboarded(true)
                    }

                    onComplete()
                }
                OnboardingStage.COMPLETE -> {
                    onComplete()
                }
            }
        }
    }

    // Handle back navigation
    val handleBack: () -> Unit = remember(currentStage) {
        {
            val previousStage = currentStage.previous()
            if (previousStage != null) {
                kotlinx.coroutines.runBlocking {
                    appStateService.updateOnboardingStage(
                        when (previousStage) {
                            OnboardingStage.FRESH -> "fresh"
                            OnboardingStage.OWNER -> "owner"
                            OnboardingStage.RELATIONSHIP -> "relationship"
                            OnboardingStage.PERSONALIZATION -> "personalization"
                            OnboardingStage.APP_LOCK -> "app-lock"
                            OnboardingStage.COMPLETE -> "complete"
                        }
                    )
                }
            }
        }
    }

    // Render current screen
    when (currentStage) {
        OnboardingStage.FRESH -> {
            WelcomeScreen(
                onGetStarted = {
                    handleNext(onboardingData)
                }
            )
        }
        OnboardingStage.OWNER -> {
            ProfileSetupScreen(
                data = onboardingData,
                onBack = handleBack,
                onNext = handleNext
            )
        }
        OnboardingStage.RELATIONSHIP -> {
            RelationshipSetupScreen(
                data = onboardingData,
                onBack = handleBack,
                onNext = handleNext
            )
        }
        OnboardingStage.PERSONALIZATION -> {
            PersonalizationSetupScreen(
                data = onboardingData,
                onBack = handleBack,
                onNext = handleNext
            )
        }
        OnboardingStage.APP_LOCK -> {
            AppLockSetupScreen(
                data = onboardingData,
                onBack = handleBack,
                onNext = handleNext
            )
        }
        OnboardingStage.COMPLETE -> {
            SetupCompleteScreen(
                data = onboardingData,
                onComplete = onComplete
            )
        }
    }
}

/**
 * OnboardingGate — determines whether to show onboarding or app.
 *
 * Matches legacy OnboardingGate with:
 * - Check if onboarding is complete
 * - Route to appropriate screen
 */
@Composable
fun OnboardingGate(
    appStateService: AppStateService,
    relationshipService: RelationshipService,
    appLockService: AppLockService,
    onOnboardingComplete: () -> Unit,
    onAppReady: () -> Unit
) {
    val isOnboarded by appStateService.isOnboarded.collectAsState()
    val onboardingStage by appStateService.onboardingStage.collectAsState()

    // Check if onboarding is complete
    LaunchedEffect(isOnboarded, onboardingStage) {
        if (isOnboarded && onboardingStage == "complete") {
            onAppReady()
        }
    }

    // Show onboarding or app
    if (!isOnboarded || onboardingStage != "complete") {
        OnboardingFlow(
            appStateService = appStateService,
            relationshipService = relationshipService,
            appLockService = appLockService,
            onComplete = onOnboardingComplete
        )
    } else {
        onAppReady()
    }
}
