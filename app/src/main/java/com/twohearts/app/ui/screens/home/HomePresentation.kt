package com.twohearts.app.ui.screens.home

import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.services.datetime.DateTimeHelper
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * HomePresentation — the pure layer behind the Home screen.
 *
 * Home answers one emotional question: *"what is our space like today?"*
 * That is a question about real state — who is here, how long we have been
 * together, what is waiting for us — so this file contains no UI and no
 * fabrication. Every value it produces is derived from an entity that was
 * actually read from the local database, and every "nothing here yet" case is
 * represented as `null` so the screen can render an honest invitation instead
 * of pretending the space is populated.
 *
 * Keeping the derivation pure is what makes the historical defect — *Home
 * looked populated but did not reflect real relationship data* — testable
 * without a device. See `Phase4HomeTest`.
 */

// ─── Snapshot ──────────────────────────────────────────────────────────────

/**
 * Everything Home needs, resolved from real persisted state.
 *
 * Nullable fields mean "this genuinely does not exist yet", which the screen
 * treats as an invitation. They must never be filled with placeholder text.
 */
data class HomeData(
    val owner: Profile?,
    val partner: Profile?,
    val relationship: CoupleRelationship?,

    /** Days since the shared start date. Null when no relationship exists. */
    val daysTogether: Int?,
    /** Days until the next anniversary of the start date. */
    val daysUntilAnniversary: Int?,

    /** The single most relevant upcoming important date, resolved. */
    val upcomingMoment: UpcomingMoment?,

    val notes: List<Note>,
    val reminders: List<Reminder>,
    val memories: List<Memory>,
    val todayMood: MoodEntry?,

    /** Local hour, used only to choose the greeting. */
    val hour: Int,
) {
    val hasRelationship: Boolean get() = relationship != null && daysTogether != null
}

/**
 * One upcoming event, resolved to an actual date.
 *
 * @param title what the couple called it.
 * @param dateKey the next occurrence, as a local `yyyy-mm-dd` calendar key.
 * @param daysAway whole days from today (0 = today).
 * @param recurring true when the source date repeats.
 */
data class UpcomingMoment(
    val title: String,
    val dateKey: String,
    val daysAway: Int,
    val recurring: Boolean,
)

// ─── Derivation ────────────────────────────────────────────────────────────

/**
 * Build the Home snapshot from the entities Home actually reads.
 *
 * [today] is injected so the caller — and the tests — control the clock;
 * production passes the device's local date.
 */
fun buildHomeData(
    owner: Profile?,
    partner: Profile?,
    relationship: CoupleRelationship?,
    notes: List<Note>,
    reminders: List<Reminder>,
    memories: List<Memory>,
    moods: List<MoodEntry>,
    dates: List<ImportantDate>,
    today: LocalDate,
    hour: Int = LocalTime.now().hour,
): HomeData {
    val startDate = relationship?.startDate
    return HomeData(
        owner = owner,
        partner = partner,
        relationship = relationship,
        daysTogether = startDate?.let { DateTimeHelper.daysSinceStartDate(it, today) },
        daysUntilAnniversary = startDate?.let { DateTimeHelper.daysUntilAnniversary(it, today) },
        upcomingMoment = upcomingMoment(relationship, dates, today),
        notes = notes,
        reminders = reminders,
        memories = memories,
        todayMood = moods.firstOrNull { it.entryDate == today.toString() },
        hour = hour,
    )
}

/**
 * The most relevant upcoming moment.
 *
 * Candidates are the couple's own saved important dates *and* the
 * relationship's anniversary, and the soonest wins. Treating the anniversary
 * as just another candidate matters: a user who saved a birthday eight months
 * out should still see their anniversary first when it is closer, and a user
 * who saved nothing at all should still see the one date the app already
 * knows about.
 *
 * Returns null when there is genuinely nothing to look forward to yet.
 */
fun upcomingMoment(
    relationship: CoupleRelationship?,
    dates: List<ImportantDate>,
    today: LocalDate,
): UpcomingMoment? {
    val candidates = dates.mapNotNull { date ->
        nextOccurrence(date.date, date.recurrence, today)?.let { next ->
            UpcomingMoment(
                title = date.title,
                dateKey = next.toString(),
                daysAway = ChronoUnit.DAYS.between(today, next).toInt(),
                recurring = date.recurrence != "none",
            )
        }
    }.toMutableList()

    // The anniversary uses the app's existing rule rather than a second
    // implementation, so Home and the reminder worker can never disagree.
    relationship?.startDate?.let { startDate ->
        val daysAway = DateTimeHelper.daysUntilAnniversary(startDate, today)
        candidates += UpcomingMoment(
            title = "Your anniversary",
            dateKey = today.plusDays(daysAway.toLong()).toString(),
            daysAway = daysAway,
            recurring = true,
        )
    }

    return candidates.minByOrNull { it.daysAway }
}

/**
 * Resolve a stored date to its next occurrence on or after [today].
 *
 * Recurrence semantics match the values the ImportantDate entity documents
 * (`none`, `yearly`, `monthly`, `weekly`, `daily`). A date that has already
 * passed and does not repeat is not upcoming, and is therefore dropped.
 */
fun nextOccurrence(dateKey: String, recurrence: String, today: LocalDate): LocalDate? {
    val parsed = runCatching { LocalDate.parse(dateKey) }.getOrNull() ?: return null
    return when (recurrence) {
        "daily" -> if (parsed.isBefore(today)) today else parsed
        "weekly" -> advanceWeeks(parsed, today)
        "monthly" -> advanceMonths(parsed, today)
        "yearly" -> advanceYears(parsed, today)
        else -> parsed.takeIf { !it.isBefore(today) }
    }
}

private fun advanceWeeks(origin: LocalDate, today: LocalDate): LocalDate {
    if (!origin.isBefore(today)) return origin
    val gap = ChronoUnit.DAYS.between(origin, today)
    return origin.plusDays(((gap + 6) / 7) * 7)
}

/**
 * Month arithmetic anchored on the origin's day-of-month.
 *
 * Stepping one month at a time from the previous result would let a 31st drift
 * permanently to the 28th after its first February. Re-deriving from the
 * origin's day each time avoids that: 31 January → 28 February → 31 March.
 */
private fun advanceMonths(origin: LocalDate, today: LocalDate): LocalDate {
    if (!origin.isBefore(today)) return origin
    var monthsAhead = ChronoUnit.MONTHS.between(origin, today).toInt()
    if (monthsAhead < 1) monthsAhead = 1
    var candidate = origin.plusMonths(monthsAhead.toLong())
    // plusMonths clamps, so a candidate can land before `today` only when the
    // origin's day overflowed; step forward until it does not.
    while (candidate.isBefore(today)) {
        monthsAhead += 1
        candidate = origin.plusMonths(monthsAhead.toLong())
    }
    return candidate
}

/**
 * Year arithmetic with the leap-day rule.
 *
 * A 29 February origin resolves to 28 February in a non-leap year, matching the
 * app's existing anniversary convention, rather than throwing.
 */
private fun advanceYears(origin: LocalDate, today: LocalDate): LocalDate {
    if (!origin.isBefore(today)) return origin
    var yearsAhead = ChronoUnit.YEARS.between(origin, today).toInt()
    if (yearsAhead < 1) yearsAhead = 1
    var candidate = origin.plusYears(yearsAhead.toLong())
    while (candidate.isBefore(today)) {
        yearsAhead += 1
        candidate = origin.plusYears(yearsAhead.toLong())
    }
    return candidate
}

// ─── Copy ──────────────────────────────────────────────────────────────────

/** Time-of-day greeting. Only the hour is considered. */
fun homeGreeting(hour: Int): String = when {
    hour < 12 -> "Good morning"
    hour < 17 -> "Good afternoon"
    else -> "Good evening"
}

/**
 * The relationship identity line, in TwoHearts' relationship-neutral
 * vocabulary. It never assumes boyfriend/girlfriend/husband/wife — it uses
 * the names onboarding actually collected.
 */
fun homeIdentityLine(ownerName: String?, partnerName: String?): String {
    val partner = partnerName?.takeIf { it.isNotBlank() }
    return when {
        partner != null -> "You and $partner"
        else -> "You and your special someone"
    }
}

/** The greeting line, personalised when a name is known. */
fun homeGreetingLine(hour: Int, ownerName: String?): String {
    val name = ownerName?.takeIf { it.isNotBlank() }
    return if (name != null) "${homeGreeting(hour)}, $name" else homeGreeting(hour)
}

/**
 * A plain, grouped day count — "1,234".
 *
 * Grouping is applied by hand rather than by a locale formatter so the value
 * is stable in tests and identical across devices.
 */
fun formatDayCount(days: Int): String {
    val digits = days.toString()
    if (digits.length <= 3) return digits
    return digits.reversed().chunked(3).joinToString(",").reversed()
}

/**
 * Shared time expressed as a duration rather than a bare statistic.
 *
 * "1 year and 8 months" reads as time lived; "608 days" reads as an analytics
 * metric, which is what the directive asks Home to avoid.
 */
fun togetherPhrase(days: Int): String {
    if (days <= 0) return "just beginning"
    val years = days / 365
    val months = (days % 365) / 30
    return when {
        years >= 1 && months >= 1 -> "$years ${plural(years, "year")} and $months ${plural(months, "month")}"
        years >= 1 -> "$years ${plural(years, "year")}"
        months >= 1 -> "$months ${plural(months, "month")}"
        days == 1 -> "1 day"
        else -> "$days days"
    }
}

private fun plural(count: Int, noun: String): String = if (count == 1) noun else "${noun}s"

/** Human label for an upcoming day count: "Today", "Tomorrow", "in 5 days". */
fun relativeDayLabel(daysAway: Int): String = when (daysAway) {
    0 -> "Today"
    1 -> "Tomorrow"
    else -> "in $daysAway days"
}

/** A calm absolute date for the same moment, used as supporting context. */
fun momentDateLabel(dateKey: String): String = DateTimeHelper.formatShortDisplay(dateKey)

/** "since 14 February 2023" context for the counter. */
fun sinceLabel(startDate: String): String = "since ${DateTimeHelper.formatDisplay(startDate)}"

/**
 * The Notes tile's supporting line. It names the most recent note so the tile
 * carries real content rather than a count alone.
 */
fun notesTileDetail(notes: List<Note>): String = when {
    notes.isEmpty() -> "Start writing"
    else -> notes.first().title
}

/** The Reminders tile's supporting line — the next pending thing, or calm. */
fun remindersTileDetail(reminders: List<Reminder>, today: LocalDate): String {
    val next = nextPendingReminder(reminders, today)
    return when {
        next == null -> "Nothing scheduled"
        next.scheduledDate == today.toString() -> "Today · ${next.title}"
        else -> "${momentDateLabel(next.scheduledDate)} · ${next.title}"
    }
}

/**
 * The next reminder worth surfacing.
 *
 * The soonest pending reminder *on or after* today wins. Only when nothing is
 * ahead does it fall back to the least-overdue one, so an overdue item never
 * displaces today's. Comparing the repository's list in its own order would
 * pick whichever row came back first, which is why the selection is by date.
 */
fun nextPendingReminder(reminders: List<Reminder>, today: LocalDate): Reminder? {
    val pending = reminders.filter { it.status == "pending" }
    val todayKey = today.toString()
    val ahead = pending.filter { it.scheduledDate >= todayKey }
    return ahead.minByOrNull { it.scheduledDate } ?: pending.minByOrNull { it.scheduledDate }
}

/** Tile count copy: deliberately plain, never a fake statistic. */
fun countLabel(count: Int, singular: String, plural: String): String =
    when (count) {
        0 -> ""
        1 -> "1 $singular"
        else -> "$count $plural"
    }

/**
 * Summarise the memory list for Home's one-slot recent-memory section.
 *
 * Memories arrive newest-first from the repository, so the first entry is the
 * recent one. The count is real and is used only for the section's context
 * line.
 */
fun recentMemory(memories: List<Memory>): Memory? = memories.firstOrNull()
