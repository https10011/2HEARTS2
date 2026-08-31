package com.twohearts.app.ui.screens.yuki

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.twohearts.app.data.game.*
import com.twohearts.app.services.game.YukiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * YukiViewModel — Manages Yuki's UI state.
 *
 * Coordinates between YukiService (persistence) and YukiScreen (UI).
 * Handles action processing with animation timing.
 * Mirrors legacy YukiScreen state management.
 */
class YukiViewModel(application: Application) : AndroidViewModel(application) {

    private val yukiService = YukiService(application)

    // UI state
    private val _state = MutableStateFlow<YukiState?>(null)
    val state: StateFlow<YukiState?> = _state.asStateFlow()

    private val _speech = MutableStateFlow<String?>(null)
    val speech: StateFlow<String?> = _speech.asStateFlow()

    private val _activeAction = MutableStateFlow<YukiAction?>(null)
    val activeAction: StateFlow<YukiAction?> = _activeAction.asStateFlow()

    private val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()

    private val _showLevelUp = MutableStateFlow(false)
    val showLevelUp: StateFlow<Boolean> = _showLevelUp.asStateFlow()

    private val _levelUpLevel = MutableStateFlow(0)
    val levelUpLevel: StateFlow<Int> = _levelUpLevel.asStateFlow()

    private val _accessoryUnlocked = MutableStateFlow<String?>(null)
    val accessoryUnlocked: StateFlow<String?> = _accessoryUnlocked.asStateFlow()

    private val _initialized = MutableStateFlow(false)
    val initialized: StateFlow<Boolean> = _initialized.asStateFlow()

    // Timer tracking for animation cleanup
    private var speechTimerRunnable: Runnable? = null
    private var animTimerRunnable: Runnable? = null

    /**
     * Initialize Yuki state on screen entry.
     * Loads from SharedPreferences, applies time decay, shows greeting.
     */
    fun initialize() {
        val raw = yukiService.loadYukiState()
        val decayed = yukiService.applyDecay(raw)
        _state.value = decayed
        _initialized.value = true

        // Show greeting based on mood
        val mood = YukiMood.resolve(decayed.moodScore)
        val greetSpeeches = MOOD_SPEECHES[mood]
        _speech.value = greetSpeeches?.random()

        // Clear speech after delay
        clearSpeechTimer()
        speechTimerRunnable = Runnable { _speech.value = null }
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
            speechTimerRunnable!!, 3000L
        )
    }

    /**
     * Process a Yuki action (feed, pet, play, clean, sleep).
     * Manages animation state and visual feedback.
     */
    fun processAction(action: YukiAction) {
        val currentState = _state.value ?: return
        if (_isAnimating.value) return

        val result = yukiService.processAction(currentState, action)
        _state.value = result.state

        // Set active action for visual feedback
        _activeAction.value = action
        _isAnimating.value = true

        // Show speech
        val speeches = ACTION_SPEECHES[action]
        _speech.value = speeches?.random()

        // Clear speech after delay
        clearSpeechTimer()
        speechTimerRunnable = Runnable { _speech.value = null }
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
            speechTimerRunnable!!, 2500L
        )

        // Show level up if applicable
        if (result.leveledUp) {
            _levelUpLevel.value = result.state.level
            _showLevelUp.value = true
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                _showLevelUp.value = false
            }, 2500L)
        }

        // Show accessory unlock
        if (result.accessoryUnlocked != null) {
            _accessoryUnlocked.value = result.accessoryUnlocked
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                _accessoryUnlocked.value = null
            }, 3000L)
        }

        // Reset animation after activity duration
        val duration = ACTIVITY_DURATIONS[result.activity] ?: 2500L
        if (duration > 0) {
            clearAnimTimer()
            animTimerRunnable = Runnable {
                _activeAction.value = null
                _isAnimating.value = false
                _state.value = _state.value?.copy(activity = YukiActivity.IDLE)
            }
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                animTimerRunnable!!, duration
            )
        } else {
            // Sleeping persists until another action
            _activeAction.value = null
            _isAnimating.value = false
        }
    }

    /**
     * Equip an accessory.
     */
    fun equipAccessory(accessoryId: String) {
        val currentState = _state.value ?: return
        val updated = yukiService.equipAccessory(currentState, accessoryId)
        _state.value = updated
    }

    private fun clearSpeechTimer() {
        speechTimerRunnable?.let {
            android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(it)
        }
        speechTimerRunnable = null
    }

    private fun clearAnimTimer() {
        animTimerRunnable?.let {
            android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(it)
        }
        animTimerRunnable = null
    }

    override fun onCleared() {
        super.onCleared()
        clearSpeechTimer()
        clearAnimTimer()
    }
}
