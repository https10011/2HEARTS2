package com.twohearts.app.services.game

import android.content.Context
import android.content.SharedPreferences
import com.twohearts.app.data.game.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/**
 * YukiService — Manages Yuki's persistent state via SharedPreferences.
 *
 * Handles: save/load, time decay, action processing, leveling, streak tracking.
 * All operations are pure side-effects against SharedPreferences — no UI.
 *
 * Architecture: UI → YukiViewModel → YukiService → SharedPreferences.
 *
 * Mirrors legacy yukiService.ts exactly.
 * Uses org.json (Android built-in) — no external JSON library needed.
 */
class YukiService(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(STORAGE_KEY, Context.MODE_PRIVATE)

    // -------------------------------------------------------------------
    // JSON Serialization (manual, using org.json)
    // -------------------------------------------------------------------

    private fun stateToJson(state: YukiState): String {
        val json = JSONObject()
        json.put("name", state.name)
        json.put("hunger", state.hunger)
        json.put("energy", state.energy)
        json.put("happiness", state.happiness)
        json.put("cleanliness", state.cleanliness)
        json.put("moodScore", state.moodScore)
        json.put("activity", state.activity.name)
        json.put("level", state.level)
        json.put("experience", state.experience)
        json.put("xpToNextLevel", state.xpToNextLevel)
        json.put("streak", state.streak)
        json.put("lastInteractionDate", state.lastInteractionDate ?: JSONObject.NULL)
        json.put("totalInteractions", state.totalInteractions)
        json.put("totalFeedings", state.totalFeedings)
        json.put("totalPets", state.totalPets)
        json.put("totalPlays", state.totalPlays)
        json.put("accessory", state.accessory ?: JSONObject.NULL)
        val arr = JSONArray()
        state.ownedAccessories.forEach { arr.put(it) }
        json.put("ownedAccessories", arr)
        json.put("createdAt", state.createdAt)
        json.put("updatedAt", state.updatedAt)
        return json.toString()
    }

    private fun jsonToState(raw: String): YukiState? {
        return try {
            val json = JSONObject(raw)
            val ownedArr = json.optJSONArray("ownedAccessories") ?: JSONArray()
            val ownedList = mutableListOf<String>()
            for (i in 0 until ownedArr.length()) {
                ownedList.add(ownedArr.getString(i))
            }
            YukiState(
                name = json.optString("name", "Yuki"),
                hunger = json.optInt("hunger", 80),
                energy = json.optInt("energy", 90),
                happiness = json.optInt("happiness", 85),
                cleanliness = json.optInt("cleanliness", 90),
                moodScore = json.optInt("moodScore", 85),
                activity = try {
                    YukiActivity.valueOf(json.optString("activity", "IDLE"))
                } catch (_: Exception) {
                    YukiActivity.IDLE
                },
                level = json.optInt("level", 1),
                experience = json.optInt("experience", 0),
                xpToNextLevel = json.optInt("xpToNextLevel", 100),
                streak = json.optInt("streak", 0),
                lastInteractionDate = if (json.isNull("lastInteractionDate")) null
                    else json.optString("lastInteractionDate"),
                totalInteractions = json.optInt("totalInteractions", 0),
                totalFeedings = json.optInt("totalFeedings", 0),
                totalPets = json.optInt("totalPets", 0),
                totalPlays = json.optInt("totalPlays", 0),
                accessory = if (json.isNull("accessory")) null
                    else json.optString("accessory"),
                ownedAccessories = ownedList,
                createdAt = json.optString("createdAt", ""),
                updatedAt = json.optString("updatedAt", "")
            )
        } catch (_: Exception) {
            null
        }
    }

    // -------------------------------------------------------------------
    // Load / Save
    // -------------------------------------------------------------------

    /** Load Yuki state from SharedPreferences. Returns default state on failure. */
    fun loadYukiState(): YukiState {
        val raw = prefs.getString(JSON_KEY, null) ?: return YukiState.createDefault()

        val parsed = jsonToState(raw) ?: return YukiState.createDefault()

        // Safe recovery: recompute mood from needs and validate ranges
        val moodScore = computeMoodScore(
            parsed.hunger, parsed.energy, parsed.happiness, parsed.cleanliness
        )

        return parsed.copy(
            hunger = parsed.hunger.coerceIn(0, 100),
            energy = parsed.energy.coerceIn(0, 100),
            happiness = parsed.happiness.coerceIn(0, 100),
            cleanliness = parsed.cleanliness.coerceIn(0, 100),
            level = parsed.level.coerceAtLeast(1),
            experience = parsed.experience.coerceAtLeast(0),
            xpToNextLevel = parsed.xpToNextLevel.coerceAtLeast(1),
            streak = parsed.streak.coerceAtLeast(0),
            totalInteractions = parsed.totalInteractions.coerceAtLeast(0),
            totalFeedings = parsed.totalFeedings.coerceAtLeast(0),
            totalPets = parsed.totalPets.coerceAtLeast(0),
            totalPlays = parsed.totalPlays.coerceAtLeast(0),
            moodScore = moodScore
        )
    }

    /** Save Yuki state to SharedPreferences. No-op on failure. */
    fun saveYukiState(state: YukiState) {
        try {
            prefs.edit()
                .putString(JSON_KEY, stateToJson(state))
                .apply()
        } catch (_: Exception) {
            // SharedPreferences may be unavailable — silently ignore
        }
    }

    // -------------------------------------------------------------------
    // Time decay
    // -------------------------------------------------------------------

    /**
     * Apply time-based decay and return the updated state.
     * Call this when the user opens the Yuki screen.
     */
    fun applyDecay(state: YukiState, now: Instant = Instant.now()): YukiState {
        val decayed = applyTimeDecay(state, now)
        val moodScore = computeMoodScore(
            decayed.hunger, decayed.energy, decayed.happiness, decayed.cleanliness
        )

        // Reset activity to idle when applying decay (user is returning)
        val newState = decayed.copy(
            moodScore = moodScore,
            activity = YukiActivity.IDLE,
            updatedAt = nowIso()
        )

        saveYukiState(newState)
        return newState
    }

    // -------------------------------------------------------------------
    // Action processing
    // -------------------------------------------------------------------

    data class ActionResult(
        val state: YukiState,
        val xpGained: Int,
        val leveledUp: Boolean,
        val newMood: YukiMood,
        val activity: YukiActivity,
        val accessoryUnlocked: String?
    )

    /**
     * Process a Yuki action (feed, pet, play, clean, sleep).
     * Returns the updated state with all effects applied.
     * Mirrors legacy processAction() exactly.
     */
    fun processAction(state: YukiState, action: YukiAction, now: Instant = Instant.now()): ActionResult {
        val changes = ACTION_NEED_CHANGES[action] ?: return ActionResult(
            state, 0, false, YukiMood.NEUTRAL, YukiActivity.IDLE, null
        )
        val xpGain = ACTION_XP[action] ?: 0

        // Apply need changes
        val hunger = (state.hunger + changes.hunger).coerceIn(0, 100)
        val energy = (state.energy + changes.energy).coerceIn(0, 100)
        val happiness = (state.happiness + changes.happiness).coerceIn(0, 100)
        val cleanliness = (state.cleanliness + changes.cleanliness).coerceIn(0, 100)

        val moodScore = computeMoodScore(hunger, energy, happiness, cleanliness)
        val newMood = YukiMood.resolve(moodScore)

        // XP and leveling
        var experience = state.experience + xpGain
        var level = state.level
        var xpToNextLevel = state.xpToNextLevel
        var leveledUp = false

        while (experience >= xpToNextLevel) {
            experience -= xpToNextLevel
            level++
            xpToNextLevel = xpForLevel(level)
            leveledUp = true
        }

        // Streak tracking
        val today = toDateKey(now)
        var streak = state.streak
        var lastInteractionDate = state.lastInteractionDate

        if (lastInteractionDate != today) {
            val yesterday = toDateKey(now.minusSeconds(24 * 3600))
            streak = when {
                lastInteractionDate == yesterday -> streak + 1
                lastInteractionDate == null -> 1
                else -> 1 // Streak broken
            }
            lastInteractionDate = today
        }

        // Accessory unlocks
        var accessoryUnlocked: String? = null
        val ownedAccessories = state.ownedAccessories.toMutableList()

        if (leveledUp && level >= 3 && !ownedAccessories.contains("bandana")) {
            ownedAccessories.add("bandana")
            accessoryUnlocked = "bandana"
        }
        if (leveledUp && level >= 5 && !ownedAccessories.contains("crown")) {
            ownedAccessories.add("crown")
            accessoryUnlocked = accessoryUnlocked ?: "crown"
        }
        if (streak >= 3 && !ownedAccessories.contains("bow-tie")) {
            ownedAccessories.add("bow-tie")
            accessoryUnlocked = accessoryUnlocked ?: "bow-tie"
        }
        if (streak >= 7 && !ownedAccessories.contains("star-badge")) {
            ownedAccessories.add("star-badge")
            accessoryUnlocked = accessoryUnlocked ?: "star-badge"
        }
        if (state.totalInteractions + 1 >= 50 && !ownedAccessories.contains("heart-collar")) {
            ownedAccessories.add("heart-collar")
            accessoryUnlocked = accessoryUnlocked ?: "heart-collar"
        }

        val activity = action.toActivity()

        val updated = state.copy(
            hunger = hunger,
            energy = energy,
            happiness = happiness,
            cleanliness = cleanliness,
            moodScore = moodScore,
            activity = activity,
            level = level,
            experience = experience,
            xpToNextLevel = xpToNextLevel,
            streak = streak,
            lastInteractionDate = lastInteractionDate,
            totalInteractions = state.totalInteractions + 1,
            totalFeedings = state.totalFeedings + if (action == YukiAction.FEED) 1 else 0,
            totalPets = state.totalPets + if (action == YukiAction.PET) 1 else 0,
            totalPlays = state.totalPlays + if (action == YukiAction.PLAY) 1 else 0,
            ownedAccessories = ownedAccessories,
            updatedAt = nowIso()
        )

        saveYukiState(updated)

        return ActionResult(
            state = updated,
            xpGained = xpGain,
            leveledUp = leveledUp,
            newMood = newMood,
            activity = activity,
            accessoryUnlocked = accessoryUnlocked
        )
    }

    // -------------------------------------------------------------------
    // Accessory management
    // -------------------------------------------------------------------

    /** Equip an accessory. Returns updated state. */
    fun equipAccessory(state: YukiState, accessoryId: String): YukiState {
        val updated = state.copy(
            accessory = if (accessoryId == "none") null else accessoryId,
            updatedAt = nowIso()
        )
        saveYukiState(updated)
        return updated
    }

    // -------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------

    /** Reset Yuki to a brand new cat. */
    fun resetYuki(name: String = "Yuki"): YukiState {
        val state = YukiState.createDefault(name)
        saveYukiState(state)
        return state
    }

    companion object {
        private const val STORAGE_KEY = "twohearts_yuki"
        private const val JSON_KEY = "yuki_state"
    }
}
