package com.twohearts.app.ui.screens.home

import android.content.Context
import com.twohearts.app.data.game.YukiMood
import com.twohearts.app.services.game.YukiService

/**
 * The one place Home reads Yuki.
 *
 * Home renders a character-led row for Yuki, and the honest version of that
 * row has to reflect Yuki's *real* persisted state rather than a fixed
 * greeting. [com.twohearts.app.ui.screens.home.HomeScreen] deliberately takes
 * the resulting [YukiPresence] as an injected value, so the UI itself never
 * touches SharedPreferences and can be rendered and tested without one; this
 * adapter is the single boundary where the companion service is consulted.
 *
 * Yuki belongs to its own phase, so nothing here changes Yuki's behaviour —
 * it only *reads* the state the companion system already maintains, and maps
 * the mood the engine already resolved to a short line for Home.
 */
fun yukiPresenceFor(context: Context): YukiPresence {
    val state = YukiService(context).loadYukiState()
    val mood = YukiMood.resolve(state.moodScore)
    return YukiPresence(
        name = state.name,
        statusLine = moodDescription(mood),
        level = state.level,
    )
}

/**
 * A short, warm status line for Home.
 *
 * The engine's own descriptions are written for the Yuki hub ("Yuki is
 * purring with joy!"); Home wants one calm clause rather than an exclamation,
 * so the mood is mapped to a compact phrase here. No new mood logic is
 * introduced — the mood itself is still resolved by the engine.
 */
private fun moodDescription(mood: YukiMood): String = when (mood) {
    YukiMood.HAPPY -> "Purring happily"
    YukiMood.CONTENT -> "Content and relaxed"
    YukiMood.NEUTRAL -> "Quietly observing"
    YukiMood.HUNGRY -> "A little hungry"
    YukiMood.SLEEPY -> "Getting sleepy"
    YukiMood.PLAYFUL -> "Wants to play"
    YukiMood.LOVED -> "Soaking up affection"
    YukiMood.SAD -> "Could use some attention"
}
