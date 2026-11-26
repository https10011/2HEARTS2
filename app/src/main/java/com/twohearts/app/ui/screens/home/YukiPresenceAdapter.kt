package com.twohearts.app.ui.screens.home

import android.content.Context
import com.twohearts.app.data.game.YukiActivity
import com.twohearts.app.data.game.applyTimeDecay
import com.twohearts.app.data.game.yukiMood
import com.twohearts.app.services.game.YukiService
import java.time.Instant

/** Home reads the save once per resume and shares the room's body pose. */
internal fun yukiPresenceFor(context: Context, now: Instant = Instant.now()): YukiPresence? {
    val saved = YukiService(context).readSavedYukiState(now) ?: return null
    val state = applyTimeDecay(saved, now)
    val mood = yukiMood(state)
    return YukiPresence(
        name = state.name,
        statusLine = if (state.activity == YukiActivity.IDLE) mood.description else state.activity.label,
        level = state.level,
        activity = state.activity,
        mood = mood,
        accessory = state.accessory,
    )
}
