package com.twohearts.app.ui.screens.period

import com.twohearts.app.data.entity.PeriodEntry
import java.time.LocalDate

internal fun periodDate(value: String?): LocalDate? =
    value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

internal fun loggedPeriodRange(entry: PeriodEntry): ClosedRange<LocalDate>? {
    val start = periodDate(entry.startDate) ?: return null
    val end = periodDate(entry.endDate)?.takeIf { !it.isBefore(start) } ?: start
    return start..end
}

internal fun nextPeriodEstimate(entry: PeriodEntry?, cycleLength: Int, today: LocalDate): LocalDate? {
    val start = periodDate(entry?.startDate) ?: return null
    if (start.isAfter(today) || cycleLength !in 1..365) return null
    return start.plusDays(cycleLength.toLong()).takeUnless { it.isBefore(today) }
}

internal fun validPeriodDates(startValue: String, endValue: String, today: LocalDate): Boolean {
    val start = periodDate(startValue) ?: return false
    if (start.isAfter(today)) return false
    if (endValue.isBlank()) return true
    val end = periodDate(endValue) ?: return false
    return end in start..today
}

internal fun PeriodEntry.belongsToPeriodOwner(ownerId: String?): Boolean = profileId == ownerId || profileId == "owner"

internal fun latestLoggedPeriod(entries: List<PeriodEntry>, today: LocalDate): PeriodEntry? =
    entries.mapNotNull { entry ->
        periodDate(entry.startDate)?.takeIf { !it.isAfter(today) }?.let { date -> entry to date }
    }.maxWithOrNull(compareBy<Pair<PeriodEntry, LocalDate>> { it.second }.thenBy { it.first.updatedAt })?.first

internal fun periodFlowLabel(value: String): String = value.replaceFirstChar(Char::titlecase).ifBlank { "Flow unspecified" }
