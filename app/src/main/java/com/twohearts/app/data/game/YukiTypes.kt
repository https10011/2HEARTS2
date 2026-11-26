package com.twohearts.app.data.game

import java.time.Duration
import java.time.Instant
import java.time.ZoneId

enum class YukiMood(val label: String, val description: String) {
    HAPPY("happy", "Purring happily"),
    CONTENT("content", "Content and relaxed"),
    NEUTRAL("neutral", "Quietly looking around"),
    HUNGRY("hungry", "Ready for something to eat"),
    SLEEPY("sleepy", "Ready for a little rest"),
    PLAYFUL("playful", "In the mood for play"),
    LOVED("loved", "Soaking up the affection"),
    SAD("sad", "Would enjoy some company");

    companion object {
        fun resolve(score: Int): YukiMood = when {
            score >= 85 -> HAPPY
            score >= 70 -> CONTENT
            score >= 55 -> NEUTRAL
            else -> SAD
        }
    }
}

enum class YukiAction(val label: String) {
    FEED("Feed"), PET("Pet"), PLAY("Play"), CLEAN("Brush"), SLEEP("Nap"), DRINK("Water"), WAKE("Wake")
}

enum class YukiActivity(val label: String, val durationMs: Long) {
    IDLE("At home", 0),
    EATING("Having a meal", 2400),
    DRINKING("Taking a drink", 2000),
    BEING_PETTED("Enjoying a pet", 1700),
    PLAYING("Playing", 3000),
    SLEEPING("Taking a nap", 0),
    GROOMING("Being brushed", 2300),
    PURRING("Purring", 1500),
}

fun YukiAction.toActivity(): YukiActivity = when (this) {
    YukiAction.FEED -> YukiActivity.EATING
    YukiAction.PET -> YukiActivity.BEING_PETTED
    YukiAction.PLAY -> YukiActivity.PLAYING
    YukiAction.CLEAN -> YukiActivity.GROOMING
    YukiAction.SLEEP -> YukiActivity.SLEEPING
    YukiAction.DRINK -> YukiActivity.DRINKING
    YukiAction.WAKE -> YukiActivity.IDLE
}

data class YukiAccessory(val id: String, val name: String, val visual: String)

val YUKI_ACCESSORIES = listOf(
    YukiAccessory("none", "No accessory", ""),
    YukiAccessory("bow-tie", "Bow tie", "🎀"),
    YukiAccessory("bandana", "Bandana", "🧣"),
    YukiAccessory("crown", "Crown", "👑"),
    YukiAccessory("heart-collar", "Heart collar", "💖"),
    YukiAccessory("star-badge", "Star badge", "⭐"),
)

fun getAccessoryById(id: String?): YukiAccessory? = YUKI_ACCESSORIES.find { it.id == id }

enum class YukiToy(val label: String, val unlockLevel: Int, val energyCost: Int, val joy: Int) {
    FEATHER("Feather wand", 1, 10, 19),
    BALL("Rolling ball", 2, 18, 31),
}

data class YukiState(
    val name: String = "Yuki",
    val hunger: Int = 80,
    val energy: Int = 90,
    val happiness: Int = 85,
    val cleanliness: Int = 90,
    val moodScore: Int = 85,
    val activity: YukiActivity = YukiActivity.IDLE,
    val level: Int = 1,
    val experience: Int = 0,
    val xpToNextLevel: Int = 100,
    val streak: Int = 0,
    val lastInteractionDate: String? = null,
    val totalInteractions: Int = 0,
    val totalFeedings: Int = 0,
    val totalPets: Int = 0,
    val totalPlays: Int = 0,
    val accessory: String? = null,
    val ownedAccessories: List<String> = listOf("none"),
    val createdAt: String = Instant.now().toString(),
    val updatedAt: String = createdAt,
    val activityStartedAt: String? = null,
    val selectedToy: YukiToy = YukiToy.FEATHER,
) {
    companion object {
        fun createDefault(name: String = "Yuki", now: Instant = Instant.now()): YukiState =
            YukiState(name = name, createdAt = now.toString(), updatedAt = now.toString())
    }
}

fun computeMoodScore(hunger: Int, energy: Int, happiness: Int, cleanliness: Int): Int =
    (hunger * 0.25 + energy * 0.2 + happiness * 0.35 + cleanliness * 0.2).toInt().coerceIn(0, 100)

fun yukiMood(state: YukiState): YukiMood = when {
    state.activity == YukiActivity.SLEEPING || state.energy < 30 -> YukiMood.SLEEPY
    state.hunger < 40 -> YukiMood.HUNGRY
    state.activity == YukiActivity.BEING_PETTED -> YukiMood.LOVED
    state.activity == YukiActivity.PLAYING -> YukiMood.PLAYFUL
    else -> YukiMood.resolve(state.moodScore)
}

fun xpForLevel(level: Int): Int = 80 + level.coerceIn(1, 10_000) * 20

private fun parseYukiTime(value: String?): Instant? =
    value?.let { runCatching { Instant.parse(it) }.getOrNull() }

private const val HOUR_SECONDS = 3_600L
private const val MAX_ABSENCE_SECONDS = 72 * HOUR_SECONDS
private const val NAP_SECONDS = 8 * HOUR_SECONDS

internal fun YukiState.activityElapsedMillis(now: Instant): Long {
    val start = parseYukiTime(activityStartedAt) ?: return Long.MAX_VALUE
    return Duration.between(start, now).coerceIn(Duration.ZERO, Duration.ofDays(3)).toMillis()
}

internal fun YukiState.nextUpdateDelayMillis(now: Instant): Long {
    val last = parseYukiTime(updatedAt)?.takeUnless { it.isAfter(now) } ?: now
    val hour = last.plusSeconds(HOUR_SECONDS)
    val start = parseYukiTime(activityStartedAt)?.takeUnless { it.isAfter(now) } ?: last
    val activityEnd = when {
        activity == YukiActivity.SLEEPING -> start.plusSeconds(NAP_SECONDS)
        activity.durationMs > 0 -> start.plusMillis(activity.durationMs)
        else -> hour
    }
    return Duration.between(now, minOf(hour, activityEnd))
        .coerceIn(Duration.ofMillis(100), Duration.ofHours(1)).toMillis()
}

fun applyTimeDecay(state: YukiState, now: Instant): YukiState {
    val savedTime = parseYukiTime(state.updatedAt)
    val last = savedTime?.takeUnless { it.isAfter(now) } ?: now
    val elapsed = Duration.between(last, now).seconds.coerceIn(0, MAX_ABSENCE_SECONDS)
    val hours = (elapsed / HOUR_SECONDS).toInt()
    val completed = last.plusSeconds(hours * HOUR_SECONDS)
    val activityStart = parseYukiTime(state.activityStartedAt)?.takeUnless { it.isAfter(now) } ?: last
    val sinceActivity = Duration.between(activityStart, now).seconds
    val sleeping = state.activity == YukiActivity.SLEEPING
    val overlapStart = maxOf(activityStart, last)
    val overlapEnd = minOf(activityStart.plusSeconds(NAP_SECONDS), completed)
    val hoursAsleep = if (sleeping) (Duration.between(overlapStart, overlapEnd).seconds / HOUR_SECONDS).coerceAtLeast(0).toInt() else 0
    val activity = when {
        sleeping && sinceActivity < NAP_SECONDS -> YukiActivity.SLEEPING
        state.activity.durationMs > 0 && Duration.between(activityStart, now) < Duration.ofMillis(state.activity.durationMs) -> state.activity
        else -> YukiActivity.IDLE
    }
    val hunger = (state.hunger - hours * 3).coerceAtLeast(minOf(state.hunger, 24))
    val energy = (state.energy + hours * 2 + hoursAsleep * 10).coerceAtMost(100)
    val happiness = (state.happiness - elapsedIntervals(last, completed, 3)).coerceAtLeast(minOf(state.happiness, 45))
    val cleanliness = (state.cleanliness - elapsedIntervals(last, completed, 2)).coerceAtLeast(minOf(state.cleanliness, 32))
    val advancedTo = when {
        savedTime != last || elapsed >= MAX_ABSENCE_SECONDS -> now
        else -> completed
    }
    return state.copy(
        hunger = hunger, energy = energy, happiness = happiness, cleanliness = cleanliness,
        moodScore = computeMoodScore(hunger, energy, happiness, cleanliness),
        activity = activity,
        activityStartedAt = if (activity == YukiActivity.IDLE) null else activityStart.toString(),
        updatedAt = advancedTo.toString(),
    )
}

private fun elapsedIntervals(start: Instant, end: Instant, hours: Int): Int {
    val interval = hours * HOUR_SECONDS
    return (Math.floorDiv(end.epochSecond, interval) - Math.floorDiv(start.epochSecond, interval)).toInt()
}

internal fun interactionDate(now: Instant): String = now.atZone(ZoneId.systemDefault()).toLocalDate().toString()
