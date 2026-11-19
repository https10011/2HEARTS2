package com.twohearts.app.services.datetime

import com.twohearts.app.data.entity.Reminder
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Advances from the original date so a short month does not shift later occurrences. */
internal fun nextCalendarOccurrence(base: LocalDate, recurrence: String, earliest: LocalDate): LocalDate? {
    if (!base.isBefore(earliest)) return base
    val unit = when (recurrence) {
        "daily" -> ChronoUnit.DAYS
        "weekly" -> ChronoUnit.WEEKS
        "monthly" -> ChronoUnit.MONTHS
        "yearly" -> ChronoUnit.YEARS
        else -> return null
    }
    val steps = unit.between(base, earliest)
    val candidate = base.plus(steps, unit)
    return if (candidate.isBefore(earliest)) base.plus(steps + 1, unit) else candidate
}

internal fun scheduledReminderDate(reminder: Reminder, today: LocalDate = LocalDate.now()): String {
    if (reminder.status != "pending" || reminder.recurrence == "none") return reminder.scheduledDate
    val base = runCatching { LocalDate.parse(reminder.scheduledDate) }.getOrNull() ?: return reminder.scheduledDate
    return nextCalendarOccurrence(base, reminder.recurrence, today)?.toString() ?: reminder.scheduledDate
}
