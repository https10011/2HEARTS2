package com.twohearts.app.services.game

import android.content.Context
import com.twohearts.app.data.game.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/**
 * Persists the original Yuki save key. New fields are optional when restoring older saves.
 * Reads and writes for an active hub are serialized by its ViewModel.
 */
class YukiService(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(STORAGE_KEY, Context.MODE_PRIVATE)

    fun loadYukiState(now: Instant = Instant.now()): YukiState {
        val raw = runCatching { prefs.getString(JSON_KEY, null) }.getOrNull()
            ?: return YukiState.createDefault(now = now)
        val parsed = runCatching { JSONObject(raw) }.getOrNull()
            ?: return YukiState.createDefault(now = now)
        val hunger = parsed.optInt("hunger", 80).coerceIn(0, 100)
        val energy = parsed.optInt("energy", 90).coerceIn(0, 100)
        val happiness = parsed.optInt("happiness", 85).coerceIn(0, 100)
        val cleanliness = parsed.optInt("cleanliness", 90).coerceIn(0, 100)
        val level = parsed.optInt("level", 1).coerceIn(1, 10_000)
        val nextXp = xpForLevel(level)
        val savedAccessories = parsed.optJSONArray("ownedAccessories")
        val owned = buildSet {
            add("none")
            if (savedAccessories != null) repeat(savedAccessories.length()) { index ->
                val id = savedAccessories.optString(index)
                if (getAccessoryById(id) != null) add(id)
            }
        }.toList()
        val toy = enumOrDefault(parsed.optString("selectedToy"), YukiToy.FEATHER)
            .takeIf { it.unlockLevel <= level } ?: YukiToy.FEATHER
        return YukiState(
            name = parsed.optString("name", "Yuki").ifBlank { "Yuki" },
            hunger = hunger, energy = energy, happiness = happiness, cleanliness = cleanliness,
            moodScore = computeMoodScore(hunger, energy, happiness, cleanliness),
            activity = enumOrDefault(parsed.optString("activity"), YukiActivity.IDLE),
            level = level, experience = parsed.optInt("experience").coerceIn(0, nextXp - 1),
            xpToNextLevel = nextXp, streak = parsed.optInt("streak").coerceAtLeast(0),
            lastInteractionDate = parsed.optionalString("lastInteractionDate"),
            totalInteractions = parsed.optInt("totalInteractions").coerceAtLeast(0),
            totalFeedings = parsed.optInt("totalFeedings").coerceAtLeast(0),
            totalPets = parsed.optInt("totalPets").coerceAtLeast(0),
            totalPlays = parsed.optInt("totalPlays").coerceAtLeast(0),
            accessory = parsed.optionalString("accessory")?.takeIf { it in owned && it != "none" },
            ownedAccessories = owned,
            createdAt = parsed.optString("createdAt", now.toString()),
            updatedAt = parsed.optString("updatedAt", now.toString()),
            activityStartedAt = parsed.optionalString("activityStartedAt"),
            selectedToy = toy,
        )
    }

    fun saveYukiState(state: YukiState) {
        val json = JSONObject().apply {
            put("version", 2)
            put("name", state.name)
            put("hunger", state.hunger)
            put("energy", state.energy)
            put("happiness", state.happiness)
            put("cleanliness", state.cleanliness)
            put("moodScore", state.moodScore)
            put("activity", state.activity.name)
            put("activityStartedAt", state.activityStartedAt)
            put("level", state.level)
            put("experience", state.experience)
            put("xpToNextLevel", state.xpToNextLevel)
            put("streak", state.streak)
            put("lastInteractionDate", state.lastInteractionDate)
            put("totalInteractions", state.totalInteractions)
            put("totalFeedings", state.totalFeedings)
            put("totalPets", state.totalPets)
            put("totalPlays", state.totalPlays)
            put("accessory", state.accessory)
            put("ownedAccessories", JSONArray(state.ownedAccessories))
            put("selectedToy", state.selectedToy.name)
            put("createdAt", state.createdAt)
            put("updatedAt", state.updatedAt)
        }
        check(prefs.edit().putString(JSON_KEY, json.toString()).commit()) { "Could not save Yuki" }
    }

    fun applyDecay(state: YukiState, now: Instant = Instant.now()): YukiState {
        val advanced = applyTimeDecay(state, now)
        // Temporary animation completion is derived from the persisted activity start.
        if (advanced.updatedAt != state.updatedAt || state.activity == YukiActivity.SLEEPING && advanced.activity != state.activity) {
            saveYukiState(advanced)
        }
        return advanced
    }

    data class ActionResult(val state: YukiState, val message: String)

    fun processAction(state: YukiState, action: YukiAction, now: Instant = Instant.now()): ActionResult {
        val current = applyTimeDecay(state, now)
        if (current.activity == YukiActivity.SLEEPING && action != YukiAction.WAKE) {
            return ActionResult(current, "Yuki is napping. Wake Yuki to interact.")
        }
        if (action == YukiAction.WAKE) {
            val awake = current.copy(activity = YukiActivity.IDLE, activityStartedAt = null)
            saveYukiState(awake)
            return ActionResult(awake, "Yuki wakes and looks around.")
        }
        if (action == YukiAction.FEED && current.hunger >= 95) return ActionResult(current, "Yuki has had enough to eat.")
        if (action == YukiAction.PLAY && current.energy < current.selectedToy.energyCost + 10) {
            return ActionResult(current, "Yuki is ready for a rest.")
        }

        val effects = when (action) {
            YukiAction.FEED -> Effects(food = 30, joy = 8, xp = 15, message = "Yuki enjoys the meal.")
            YukiAction.PET -> Effects(joy = 16, xp = 9, message = "Yuki leans into your hand.")
            YukiAction.PLAY -> Effects(
                food = -8, energy = -current.selectedToy.energyCost, joy = current.selectedToy.joy,
                xp = 20, message = "Yuki chases the ${current.selectedToy.label.lowercase()}.",
            )
            YukiAction.CLEAN -> Effects(clean = 30, joy = 5, xp = 8, message = "Yuki settles into the brushing.")
            YukiAction.SLEEP -> Effects(energy = 8, joy = 2, xp = 3, message = "Yuki curls up for a nap.")
            YukiAction.DRINK -> Effects(energy = 4, joy = 3, xp = 0, message = "Yuki has a little drink.")
            YukiAction.WAKE -> error("Handled before selecting effects")
        }
        val hunger = (current.hunger + effects.food).coerceIn(0, 100)
        val energy = (current.energy + effects.energy).coerceIn(0, 100)
        val happiness = (current.happiness + effects.joy).coerceIn(0, 100)
        val cleanliness = (current.cleanliness + effects.clean).coerceIn(0, 100)
        val xp = current.experience + effects.xp
        val leveledUp = xp >= current.xpToNextLevel && current.level < 10_000
        val level = current.level + if (leveledUp) 1 else 0
        val total = current.totalInteractions.increment()
        val owned = buildSet {
            addAll(current.ownedAccessories)
            if (total >= 6) add("bow-tie")
            if (level >= 3) add("bandana")
            if (level >= 5) add("crown")
            if (total >= 30) add("star-badge")
            if (total >= 50) add("heart-collar")
        }.toList()
        val updated = current.copy(
            hunger = hunger, energy = energy, happiness = happiness, cleanliness = cleanliness,
            moodScore = computeMoodScore(hunger, energy, happiness, cleanliness),
            activity = action.toActivity(), activityStartedAt = now.toString(), updatedAt = now.toString(),
            experience = if (leveledUp) xp - current.xpToNextLevel else xp.coerceAtMost(xpForLevel(level) - 1),
            xpToNextLevel = xpForLevel(level), level = level,
            totalInteractions = total, lastInteractionDate = interactionDate(now),
            totalFeedings = if (action == YukiAction.FEED) current.totalFeedings.increment() else current.totalFeedings,
            totalPets = if (action == YukiAction.PET) current.totalPets.increment() else current.totalPets,
            totalPlays = if (action == YukiAction.PLAY) current.totalPlays.increment() else current.totalPlays,
            ownedAccessories = owned,
        )
        saveYukiState(updated)
        val newAccessory = (owned - current.ownedAccessories.toSet()).firstOrNull()?.let(::getAccessoryById)
        val message = when {
            newAccessory != null -> "${effects.message} ${newAccessory.name} unlocked."
            leveledUp && level == YukiToy.BALL.unlockLevel -> "Level $level. Yuki has a new rolling ball."
            leveledUp -> "${effects.message} Level $level reached."
            else -> effects.message
        }
        return ActionResult(updated, message)
    }

    fun equipAccessory(state: YukiState, id: String): YukiState {
        if (id !in state.ownedAccessories || getAccessoryById(id) == null) return state
        return state.copy(accessory = id.takeUnless { it == "none" }).also(::saveYukiState)
    }

    fun selectToy(state: YukiState, toy: YukiToy): YukiState {
        if (state.level < toy.unlockLevel) return state
        return state.copy(selectedToy = toy).also(::saveYukiState)
    }

    fun resetYuki(name: String = "Yuki"): YukiState = YukiState.createDefault(name).also(::saveYukiState)

    private data class Effects(
        val food: Int = 0, val energy: Int = 0, val joy: Int = 0, val clean: Int = 0,
        val xp: Int, val message: String,
    )

    companion object {
        private const val STORAGE_KEY = "twohearts_yuki"
        private const val JSON_KEY = "yuki_state"
    }
}

private fun Int.increment(): Int = if (this == Int.MAX_VALUE) this else this + 1

private fun JSONObject.optionalString(key: String): String? =
    if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

private inline fun <reified T : Enum<T>> enumOrDefault(value: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback
