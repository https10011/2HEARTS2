package com.twohearts.app.data.game

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// ---------------------------------------------------------------------------
// Core need levels (0–100 scale)
// ---------------------------------------------------------------------------

/** How fed Yuki is. 100 = perfectly full, 0 = starving. */
typealias NeedLevel = Int

// ---------------------------------------------------------------------------
// Mood system
// ---------------------------------------------------------------------------

enum class YukiMood(val label: String, val description: String, val emoji: String) {
    HAPPY("happy", "Yuki is purring with joy!", "\uD83D\uDE0A"),
    CONTENT("content", "Yuki looks content and relaxed.", "\uD83D\uDE0C"),
    NEUTRAL("neutral", "Yuki is quietly observing.", "\uD83D\uDE10"),
    HUNGRY("hungry", "Yuki's tummy is rumbling...", "\uD83C\uDF56"),
    SLEEPY("sleepy", "Yuki's eyelids are getting heavy.", "\uD83D\uDE34"),
    PLAYFUL("playful", "Yuki wants to play!", "\uD83D\uDC31"),
    LOVED("loved", "Yuki is soaking up the affection!", "\uD83E\uDD70"),
    SAD("sad", "Yuki needs some attention...", "\uD83D\uDE3F");

    companion object {
        /**
         * Maps a numeric mood score (0–100) to a mood name.
         * Mirrors legacy resolveMood() exactly.
         */
        fun resolve(moodScore: Int): YukiMood = when {
            moodScore >= 85 -> HAPPY
            moodScore >= 70 -> CONTENT
            moodScore >= 55 -> NEUTRAL
            moodScore >= 40 -> NEUTRAL
            moodScore >= 25 -> HUNGRY
            else -> SAD
        }
    }
}

// ---------------------------------------------------------------------------
// Activity / action types
// ---------------------------------------------------------------------------

enum class YukiAction(val label: String) {
    FEED("Feed"),
    PET("Pet"),
    PLAY("Play"),
    CLEAN("Clean"),
    SLEEP("Sleep")
}

/** What Yuki is currently doing (visual + behavioral state). */
enum class YukiActivity(val label: String) {
    IDLE("idle"),
    EATING("eating"),
    BEING_PETTED("being-petted"),
    PLAYING("playing"),
    SLEEPING("sleeping"),
    GROOMING("grooming"),
    PURRING("purring")
}

/**
 * Maps action to the activity it triggers.
 * Mirrors legacy actionToActivity() exactly.
 */
fun YukiAction.toActivity(): YukiActivity = when (this) {
    YukiAction.FEED -> YukiActivity.EATING
    YukiAction.PET -> YukiActivity.BEING_PETTED
    YukiAction.PLAY -> YukiActivity.PLAYING
    YukiAction.CLEAN -> YukiActivity.GROOMING
    YukiAction.SLEEP -> YukiActivity.SLEEPING
}

// ---------------------------------------------------------------------------
// Accessory system
// ---------------------------------------------------------------------------

data class YukiAccessory(
    val id: String,
    val name: String,
    /** Visual hint for rendering (emoji). */
    val visual: String,
    /** How it was unlocked. */
    val source: AccessorySource
)

enum class AccessorySource { DEFAULT, STREAK, LEVEL, INTERACTION }

/** All available Yuki accessories. */
val YUKI_ACCESSORIES: List<YukiAccessory> = listOf(
    YukiAccessory("none", "Nothing", "", AccessorySource.DEFAULT),
    YukiAccessory("bow-tie", "Bow Tie", "\uD83C\uDF80", AccessorySource.STREAK),
    YukiAccessory("bandana", "Bandana", "\uD83E\uDDE3", AccessorySource.LEVEL),
    YukiAccessory("crown", "Crown", "\uD83D\uDC51", AccessorySource.LEVEL),
    YukiAccessory("heart-collar", "Heart Collar", "\uD83D\uDC96", AccessorySource.INTERACTION),
    YukiAccessory("star-badge", "Star Badge", "\u2B50", AccessorySource.STREAK)
)

fun getAccessoryById(id: String?): YukiAccessory? =
    if (id == null) null else YUKI_ACCESSORIES.find { it.id == id }

// ---------------------------------------------------------------------------
// Yuki state (the full persistent model)
// ---------------------------------------------------------------------------

data class YukiState(
    /** Display name. */
    val name: String = "Yuki",

    // Needs (0–100; higher = better)
    val hunger: NeedLevel = 80,
    val energy: NeedLevel = 90,
    val happiness: NeedLevel = 85,
    val cleanliness: NeedLevel = 90,

    /** Derived mood score (0–100). Computed from needs. */
    val moodScore: Int = 85,

    /** Current visual/behavioral activity. */
    val activity: YukiActivity = YukiActivity.IDLE,

    // Progression
    val level: Int = 1,
    val experience: Int = 0,
    /** XP needed for next level. */
    val xpToNextLevel: Int = 100,

    // Streak / daily engagement
    val streak: Int = 0,
    /** ISO yyyy-MM-dd of last interaction. */
    val lastInteractionDate: String? = null,

    // Stats
    val totalInteractions: Int = 0,
    val totalFeedings: Int = 0,
    val totalPets: Int = 0,
    val totalPlays: Int = 0,

    // Customization
    val accessory: String? = null,
    val ownedAccessories: List<String> = listOf("none"),

    // Timestamps
    val createdAt: String = nowIso(),
    val updatedAt: String = nowIso()
) {
    companion object {
        /** Create default Yuki state. Mirrors legacy createDefaultYukiState(). */
        fun createDefault(name: String = "Yuki"): YukiState {
            val now = nowIso()
            return YukiState(
                name = name,
                hunger = 80,
                energy = 90,
                happiness = 85,
                cleanliness = 90,
                moodScore = 85,
                activity = YukiActivity.IDLE,
                level = 1,
                experience = 0,
                xpToNextLevel = 100,
                streak = 0,
                lastInteractionDate = null,
                totalInteractions = 0,
                totalFeedings = 0,
                totalPets = 0,
                totalPlays = 0,
                accessory = null,
                ownedAccessories = listOf("none"),
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Computed mood from needs
// ---------------------------------------------------------------------------

/**
 * Compute Yuki's mood score from current needs.
 * Weighted average with happiness having slightly more influence.
 * Mirrors legacy computeMoodScore() exactly.
 */
fun computeMoodScore(
    hunger: NeedLevel,
    energy: NeedLevel,
    happiness: NeedLevel,
    cleanliness: NeedLevel
): Int {
    val weighted = hunger * 0.25 + energy * 0.2 + happiness * 0.35 + cleanliness * 0.2
    return weighted.toInt().coerceIn(0, 100)
}

// ---------------------------------------------------------------------------
// XP / leveling
// ---------------------------------------------------------------------------

/** XP required for level N → N+1. Mirrors legacy xpForLevel(). */
fun xpForLevel(level: Int): Int = (80 + level * 20)

/** Action XP rewards. */
val ACTION_XP: Map<YukiAction, Int> = mapOf(
    YukiAction.FEED to 15,
    YukiAction.PET to 10,
    YukiAction.PLAY to 20,
    YukiAction.CLEAN to 8,
    YukiAction.SLEEP to 5
)

/** Action need changes (positive = improvement). */
data class NeedChanges(
    val hunger: Int,
    val energy: Int,
    val happiness: Int,
    val cleanliness: Int
)

val ACTION_NEED_CHANGES: Map<YukiAction, NeedChanges> = mapOf(
    YukiAction.FEED to NeedChanges(hunger = 30, energy = 5, happiness = 10, cleanliness = 0),
    YukiAction.PET to NeedChanges(hunger = 0, energy = 5, happiness = 25, cleanliness = 0),
    YukiAction.PLAY to NeedChanges(hunger = -10, energy = -20, happiness = 30, cleanliness = -5),
    YukiAction.CLEAN to NeedChanges(hunger = 0, energy = -5, happiness = 10, cleanliness = 35),
    YukiAction.SLEEP to NeedChanges(hunger = -5, energy = 40, happiness = 5, cleanliness = 0)
)

// ---------------------------------------------------------------------------
// Need decay (time-based)
// ---------------------------------------------------------------------------

/** How many hours before a full need cycle. */
private const val DECAY_CYCLE_HOURS = 8

/** Per-cycle decay amount. */
private const val DECAY_AMOUNT = 15

/**
 * Apply time-based decay to Yuki's needs based on hours since last update.
 * Returns the decayed needs (clamped 0–100).
 * Mirrors legacy applyTimeDecay() exactly.
 */
fun applyTimeDecay(
    state: YukiState,
    now: Instant
): YukiState {
    val lastUpdate = try {
        Instant.parse(state.updatedAt)
    } catch (_: Exception) {
        now.minusSeconds(0)
    }

    val secondsElapsed = now.epochSecond - lastUpdate.epochSecond
    val hoursElapsed = (secondsElapsed / 3600.0).coerceAtLeast(0.0)
    val cycles = (hoursElapsed / DECAY_CYCLE_HOURS).toInt()

    if (cycles <= 0) {
        return state
    }

    // Hunger decays fastest, then energy, then cleanliness, happiness decays slowest
    val hungerDecay = minOf(state.hunger, cycles * DECAY_AMOUNT)
    val energyDecay = minOf(state.energy, cycles * (DECAY_AMOUNT * 0.8).toInt())
    val cleanlinessDecay = minOf(state.cleanliness, cycles * (DECAY_AMOUNT * 0.6).toInt())
    val happinessDecay = minOf(state.happiness, cycles * (DECAY_AMOUNT * 0.5).toInt())

    return state.copy(
        hunger = (state.hunger - hungerDecay).coerceAtLeast(0),
        energy = (state.energy - energyDecay).coerceAtLeast(0),
        happiness = (state.happiness - happinessDecay).coerceAtLeast(0),
        cleanliness = (state.cleanliness - cleanlinessDecay).coerceAtLeast(0)
    )
}

// ---------------------------------------------------------------------------
// Activity durations (for animation timing)
// ---------------------------------------------------------------------------

/** Duration of each activity animation in ms. */
val ACTIVITY_DURATIONS: Map<YukiActivity, Long> = mapOf(
    YukiActivity.IDLE to 0L,
    YukiActivity.EATING to 2500L,
    YukiActivity.BEING_PETTED to 2000L,
    YukiActivity.PLAYING to 3000L,
    YukiActivity.SLEEPING to 0L, // persistent until manually changed
    YukiActivity.GROOMING to 2000L,
    YukiActivity.PURRING to 0L
)

// ---------------------------------------------------------------------------
// Speech lines
// ---------------------------------------------------------------------------

val ACTION_SPEECHES: Map<YukiAction, List<String>> = mapOf(
    YukiAction.FEED to listOf("Nom nom nom!", "Purrr... delicious!", "My favorite!", "Yum!"),
    YukiAction.PET to listOf("*purrrr*", "That feels nice!", "More please!", "Mrrrow~"),
    YukiAction.PLAY to listOf("Zoomies!", "I got it!", "So fun!", "*pounces*"),
    YukiAction.CLEAN to listOf("Squeaky clean!", "*shakes off*", "All fresh!", "Prrr~"),
    YukiAction.SLEEP to listOf("Zzz...", "*curls up*", "So cozy...", "*yawn*")
)

val MOOD_SPEECHES: Map<YukiMood, List<String>> = mapOf(
    YukiMood.HAPPY to listOf("I'm so happy!", "Life is good!", "I love you two!"),
    YukiMood.CONTENT to listOf("*content purr*", "Everything is nice.", "Home sweet home."),
    YukiMood.NEUTRAL to listOf("*stretches*", "Meow.", "*looks around*"),
    YukiMood.HUNGRY to listOf("Feed me please!", "My tummy rumbles...", "Is it dinner time?"),
    YukiMood.SLEEPY to listOf("*yawn*...", "So sleepy...", "Nap time?"),
    YukiMood.PLAYFUL to listOf("Let's play!", "*bounces*", "I have so much energy!"),
    YukiMood.LOVED to listOf("*purrrr*", "I feel so loved!", "*nuzzles*"),
    YukiMood.SAD to listOf("Meow...", "*looks up sadly*", "I miss you...")
)

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** ISO 8601 UTC timestamp. */
fun nowIso(): String = Instant.now().atOffset(ZoneOffset.UTC)
    .format(DateTimeFormatter.ISO_INSTANT)

/** ISO date key (yyyy-MM-dd) in UTC. */
fun toDateKey(instant: Instant): String {
    val date = instant.atZone(ZoneOffset.UTC).toLocalDate()
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
}

/** Clamp an integer value. */
fun clamp(value: Int, min: Int, max: Int): Int = value.coerceIn(min, max)
