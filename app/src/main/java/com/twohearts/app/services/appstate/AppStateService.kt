package com.twohearts.app.services.appstate

import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.logger.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow

/**
 * AppStateService — application state management.
 *
 * Matches legacy AppStateService with:
 * - Theme mode management
 * - Text size management
 * - Onboarding state
 * - App settings coordination
 */
class AppStateService(private val settingsStorage: SettingsStorage) {

    private val logger = Logger("AppState")

    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _textSize = MutableStateFlow("default")
    val textSize: StateFlow<String> = _textSize.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _onboardingStage = MutableStateFlow("fresh")
    val onboardingStage: StateFlow<String> = _onboardingStage.asStateFlow()

    /**
     * Initialize app state from settings.
     */
    suspend fun initialize() {
        settingsStorage.settings.first().let { settings ->
            _themeMode.value = settings.themeMode
            _textSize.value = settings.textSize
            _isOnboarded.value = settings.onboarded
            _onboardingStage.value = settings.onboardingStage
        }
        logger.info("App state initialized")
    }

    /**
     * Update theme mode.
     */
    suspend fun setThemeMode(mode: String) {
        settingsStorage.updateThemeMode(mode)
        _themeMode.value = mode
        logger.info("Theme mode updated: $mode")
    }

    /**
     * Update text size.
     */
    suspend fun setTextSize(size: String) {
        settingsStorage.updateTextSize(size)
        _textSize.value = size
        logger.info("Text size updated: $size")
    }

    /**
     * Mark onboarding as complete.
     */
    suspend fun setOnboarded(onboarded: Boolean) {
        settingsStorage.setOnboarded(onboarded)
        _isOnboarded.value = onboarded
        logger.info("Onboarded state updated: $onboarded")
    }

    /**
     * Update onboarding stage.
     */
    suspend fun updateOnboardingStage(stage: String) {
        settingsStorage.updateOnboardingStage(stage)
        _onboardingStage.value = stage
        logger.info("Onboarding stage updated: $stage")
    }

    /**
     * Check if onboarding is complete.
     */
    fun isOnboardingComplete(): Boolean {
        return _onboardingStage.value == "complete"
    }

    /**
     * Get current theme mode.
     */
    fun getThemeMode(): String = _themeMode.value

    /**
     * Get current text size.
     */
    fun getTextSize(): String = _textSize.value

    /**
     * Reset app state.
     */
    suspend fun reset() {
        settingsStorage.reset()
        _themeMode.value = "system"
        _textSize.value = "default"
        _isOnboarded.value = false
        _onboardingStage.value = "fresh"
        logger.info("App state reset")
    }
}
