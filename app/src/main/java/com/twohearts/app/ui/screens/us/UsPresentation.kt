package com.twohearts.app.ui.screens.us

import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.screens.home.UpcomingMoment
import com.twohearts.app.ui.screens.home.upcomingMoment
import java.time.LocalDate

internal data class UsHighlights(
    val startDate: LocalDate?,
    val daysTogether: Int?,
    val nextDate: UpcomingMoment?,
    val memory: Memory?,
    val memoryCount: Int,
    val timelineEvent: TimelineEvent?,
    val place: Place?,
    val placeCount: Int,
    val todayMood: MoodEntry?,
)

internal fun usHighlights(
    relationship: CoupleRelationship?,
    dates: List<ImportantDate>,
    memories: List<Memory>,
    events: List<TimelineEvent>,
    places: List<Place>,
    moods: List<MoodEntry>,
    today: LocalDate,
): UsHighlights {
    val start = relationship?.startDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val started = start?.takeUnless { it.isAfter(today) }
    return UsHighlights(
        startDate = start,
        daysTogether = started?.let { DateTimeHelper.daysSinceStartDate(it.toString(), today) },
        nextDate = upcomingMoment(relationship.takeIf { started != null }, dates, today),
        memory = memories.firstOrNull(),
        memoryCount = memories.size,
        timelineEvent = events.firstOrNull(),
        place = places.firstOrNull(),
        placeCount = places.size,
        todayMood = moods.firstOrNull { it.entryDate == today.toString() },
    )
}
