package com.twohearts.app

import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.repository.ImportantDateRepository
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.ui.screens.home.HomeData
import com.twohearts.app.ui.screens.home.buildHomeData
import com.twohearts.app.ui.screens.home.countLabel
import com.twohearts.app.ui.screens.home.formatDayCount
import com.twohearts.app.ui.screens.home.homeGreeting
import com.twohearts.app.ui.screens.home.homeIdentityLine
import com.twohearts.app.ui.screens.home.notesTileDetail
import com.twohearts.app.ui.screens.home.nextOccurrence
import com.twohearts.app.ui.screens.home.nextPendingReminder
import com.twohearts.app.ui.screens.home.recentMemory
import com.twohearts.app.ui.screens.home.relativeDayLabel
import com.twohearts.app.ui.screens.home.remindersTileDetail
import com.twohearts.app.ui.screens.home.togetherPhrase
import com.twohearts.app.ui.screens.home.upcomingMoment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/**
 * Phase4HomeTest — the behaviour Home must actually have.
 *
 * Two groups of assertions, and the second is the important one:
 *
 *  1. **Pure derivation.** Greeting, identity, counting and the phrasing of
 *     time together. These are cheap and keep the copy honest.
 *  2. **Real data reaching Home.** The historical defect was that Home
 *     *appeared* populated while ignoring the relationship it was supposed to
 *     show. So this suite writes through the genuine repositories into the
 *     same Room database the app uses, reads it back through the same
 *     repository type Home depends on, and asserts that the values a person
 *     would see are the ones that were stored. If someone re-breaks the wiring
 *     — passing an empty list, a null relationship, a stale snapshot — these
 *     fail rather than silently rendering a pretty empty screen.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class Phase4HomeTest {

    // ─── Group 1: pure derivation ──────────────────────────────────────────

    @Test
    fun greetingReflectsTimeOfDay() {
        assertEquals("Good morning", homeGreeting(0))
        assertEquals("Good morning", homeGreeting(11))
        assertEquals("Good afternoon", homeGreeting(12))
        assertEquals("Good afternoon", homeGreeting(16))
        assertEquals("Good evening", homeGreeting(17))
        assertEquals("Good evening", homeGreeting(23))
    }

    @Test
    fun identityUsesRelationshipNeutralLanguageAndRealNames() {
        assertEquals("You and Sam", homeIdentityLine("Alex", "Sam"))
        // No partner name yet must not invent a gendered role.
        assertEquals("You and your special someone", homeIdentityLine("Alex", null))
        assertEquals("You and your special someone", homeIdentityLine("Alex", "   "))
    }

    @Test
    fun dayCountIsGroupedDeterministically() {
        assertEquals("0", formatDayCount(0))
        assertEquals("127", formatDayCount(127))
        assertEquals("1,014", formatDayCount(1014))
        assertEquals("12,345", formatDayCount(12345))
    }

    @Test
    fun timeTogetherIsADurationNotABareStatistic() {
        assertEquals("just beginning", togetherPhrase(0))
        assertEquals("1 day", togetherPhrase(1))
        assertEquals("12 days", togetherPhrase(12))
        assertEquals("1 month", togetherPhrase(30))
        assertEquals("1 year", togetherPhrase(365))
        assertEquals("1 year and 8 months", togetherPhrase(608))
        assertEquals("2 years", togetherPhrase(730))
    }

    @Test
    fun relativeLabelsReadNaturally() {
        assertEquals("Today", relativeDayLabel(0))
        assertEquals("Tomorrow", relativeDayLabel(1))
        assertEquals("in 5 days", relativeDayLabel(5))
        assertEquals("in 30 days", relativeDayLabel(30))
    }

    @Test
    fun tileCountsAreQuietWhenEmpty() {
        assertEquals("", countLabel(0, "note", "notes"))
        assertEquals("1 note", countLabel(1, "note", "notes"))
        assertEquals("3 notes", countLabel(3, "note", "notes"))
    }

    @Test
    fun notesTileNamesTheMostRecentNote() {
        assertEquals("Start writing", notesTileDetail(emptyList()))
        val notes = listOf(note("Weekend ideas"), note("Older"))
        assertEquals("Weekend ideas", notesTileDetail(notes))
    }

    @Test
    fun remindersTileIsCalmWithNothingPending() {
        val today = LocalDate.parse("2026-01-15")
        assertEquals("Nothing scheduled", remindersTileDetail(emptyList(), today))
    }

    @Test
    fun remindersTilePrefersTheSoonestFutureReminder() {
        val today = LocalDate.parse("2026-01-15")
        // Deliberately stored out of order: the selection must be by date, not
        // by whichever row the repository happened to return first.
        val reminders = listOf(
            reminder("Later", "2026-04-02"),
            reminder("Sooner", "2026-02-10"),
        )
        assertEquals("Feb 10, 2026 · Sooner", remindersTileDetail(reminders, today))
    }

    @Test
    fun remindersTileFallsBackToOverdueWhenNothingIsAhead() {
        val today = LocalDate.parse("2026-01-15")
        val reminders = listOf(reminder("Overdue", "2026-01-01"))
        assertEquals("Jan 1, 2026 · Overdue", remindersTileDetail(reminders, today))
    }

    @Test
    fun completedRemindersAreNotPending() {
        val today = LocalDate.parse("2026-01-15")
        val done = reminder("Done", "2026-01-16").copy(status = "completed")
        assertNull(nextPendingReminder(listOf(done), today))
    }

    @Test
    fun recurringDatesResolveForward() {
        val today = LocalDate.parse("2026-01-15")
        assertEquals(LocalDate.parse("2026-03-21"), nextOccurrence("2021-03-21", "yearly", today))
        assertEquals(LocalDate.parse("2026-01-20"), nextOccurrence("2026-01-20", "none", today))
        // A non-recurring date in the past is not upcoming.
        assertNull(nextOccurrence("2020-01-20", "none", today))
        // Daily dates old enough to be in the past collapse to today.
        assertEquals(today, nextOccurrence("2020-01-20", "daily", today))
    }

    @Test
    fun yearlyRecurrenceOnLeapDayResolvesToAFebruaryDate() {
        val today = LocalDate.parse("2026-01-15")
        val next = nextOccurrence("2020-02-29", "yearly", today)
        assertNotNull(next)
        assertEquals(2, next!!.monthValue)
        assertTrue(next.isAfter(today))
    }

    @Test
    fun monthlyRecurrenceClampsToShorterMonths() {
        val today = LocalDate.parse("2026-01-31")
        // 31 January recurring monthly into February must not throw.
        val next = nextOccurrence("2026-01-31", "monthly", today)
        assertNotNull(next)
        assertTrue(!next!!.isBefore(today))
    }

    @Test
    fun monthlyRecurrenceDoesNotDriftAfterAShorterMonth() {
        // The 31st must return to the 31st in March, not stay on February's 28th.
        val today = LocalDate.parse("2026-03-05")
        val next = nextOccurrence("2025-01-31", "monthly", today)
        assertEquals(LocalDate.parse("2026-03-31"), next)
    }

    @Test
    fun weeklyRecurrenceAdvancesInSevenDaySteps() {
        val today = LocalDate.parse("2026-01-15")
        assertEquals(LocalDate.parse("2026-01-17"), nextOccurrence("2025-12-20", "weekly", today))
        // Exactly on the weekday boundary stays put.
        assertEquals(LocalDate.parse("2026-01-15"), nextOccurrence("2026-01-15", "weekly", today))
    }

    @Test
    fun leapDayAnniversaryResolvesToFebruary28InANonLeapYear() {
        // 2027 is not a leap year, so the 29 February origin must clamp.
        val next = nextOccurrence("2020-02-29", "yearly", LocalDate.parse("2026-03-01"))
        assertEquals(LocalDate.parse("2027-02-28"), next)
    }

    @Test
    fun upcomingPrefersTheSoonestOfSavedDateAndAnniversary() {
        val today = LocalDate.parse("2026-01-15")
        val relationship = CoupleRelationship(
            ownerId = "o", partnerId = "p", startDate = "2023-06-14",
            id = "rel", createdAt = now, updatedAt = now,
        )
        // Anniversary (14 June) is ~5 months out; the saved date is closer.
        val dates = listOf(
            ImportantDate(
                title = "Sam's birthday", date = "1996-03-11", recurrence = "yearly",
                id = "d1", createdAt = now, updatedAt = now,
            )
        )
        val moment = upcomingMoment(relationship, dates, today)
        assertEquals("Sam's birthday", moment!!.title)
        assertEquals(55, moment.daysAway)
    }

    @Test
    fun theCloserOfSavedDateAndAnniversaryWins() {
        val today = LocalDate.parse("2026-01-15")
        val relationship = CoupleRelationship(
            ownerId = "o", partnerId = "p", startDate = "2023-02-14",
            id = "rel", createdAt = now, updatedAt = now,
        )
        // Anniversary is 30 days out; this saved date lands in 5.
        val dates = listOf(
            ImportantDate(
                title = "The day we met", date = "2021-01-20", recurrence = "yearly",
                id = "d1", createdAt = now, updatedAt = now,
            )
        )
        val moment = upcomingMoment(relationship, dates, today)
        assertEquals("The day we met", moment!!.title)
        assertEquals(5, moment.daysAway)
    }

    @Test
    fun anniversarySurfacesWhenNoDateIsCloser() {
        val today = LocalDate.parse("2026-01-15")
        val relationship = CoupleRelationship(
            ownerId = "o", partnerId = "p", startDate = "2023-02-01",
            id = "rel", createdAt = now, updatedAt = now,
        )
        val moment = upcomingMoment(relationship, emptyList(), today)
        assertEquals("Your anniversary", moment!!.title)
        assertEquals(17, moment.daysAway)
    }

    @Test
    fun noRelationshipAndNoDatesMeansNoUpcomingMoment() {
        assertNull(upcomingMoment(null, emptyList(), LocalDate.parse("2026-01-15")))
    }

    @Test
    fun recentMemoryIsTheNewestEntry() {
        assertNull(recentMemory(emptyList()))
        assertEquals("Newest", recentMemory(listOf(memory("Newest"), memory("Older")))!!.title)
    }

    @Test
    fun homeDataOmitsEverythingThatDoesNotExist() {
        val data = buildHomeData(
            owner = profile("Alex", "owner"),
            partner = null,
            relationship = null,
            notes = emptyList(),
            reminders = emptyList(),
            memories = emptyList(),
            moods = emptyList(),
            dates = emptyList(),
            today = LocalDate.parse("2026-01-15"),
            hour = 9,
        )
        assertNull(data.daysTogether)
        assertNull(data.upcomingMoment)
        assertNull(data.todayMood)
        assertEquals(false, data.hasRelationship)
    }

    // ─── Group 2: real data reaches Home through real repositories ─────────

    @Test
    fun relationshipDataWrittenThroughRepositoriesIsReadBackForHome() = runBlocking {
        val owner: Profile
        val partner: Profile
        val relationship: CoupleRelationship
        val data: HomeData

        withContext(Dispatchers.IO) {
            val db = freshDatabase()
            val profileRepo = com.twohearts.app.data.repository.ProfileRepository(db.profileDao())
            val coupleRepo = com.twohearts.app.data.repository.CoupleRelationshipRepository(
                db.coupleRelationshipDao()
            )
            val service = com.twohearts.app.services.relationship.RelationshipService(
                profileRepository = profileRepo,
                relationshipRepository = coupleRepo,
            )

            profileRepo.create(
                Profile(name = "Alexandria", role = "owner", id = "h-owner", createdAt = now, updatedAt = now)
            )
            profileRepo.create(
                Profile(name = "Sam", role = "partner", birthday = "1996-03-11", id = "h-partner", createdAt = now, updatedAt = now)
            )
            coupleRepo.create(
                CoupleRelationship(
                    ownerId = "h-owner", partnerId = "h-partner", startDate = "2023-02-14",
                    id = "h-rel", createdAt = now, updatedAt = now,
                )
            )

            // Read back through the exact flows HomeScreen subscribes to.
            owner = service.observeOwner().first()!!
            partner = service.observePartner().first()!!
            relationship = service.observeRelationship().first()!!
            data = buildHomeData(
                owner = owner,
                partner = partner,
                relationship = relationship,
                notes = emptyList(),
                reminders = emptyList(),
                memories = emptyList(),
                moods = emptyList(),
                dates = emptyList(),
                today = LocalDate.parse(Phase4HomeSeed.TODAY),
                hour = 20,
            )
        }

        // The names a person would actually read on Home.
        assertEquals("Alexandria", data.owner!!.name)
        assertEquals("Sam", data.partner!!.name)
        assertEquals("You and Sam", homeIdentityLine(data.owner!!.name, data.partner!!.name))

        // The counter reflects the stored start date through the app's own rule.
        assertNotNull(data.daysTogether)
        assertEquals(
            com.twohearts.app.services.datetime.DateTimeHelper
                .daysSinceStartDate("2023-02-14", LocalDate.parse(Phase4HomeSeed.TODAY)),
            data.daysTogether,
        )
        assertTrue("days together should be substantial", data.daysTogether!! > 1000)
    }

    @Test
    fun contentWrittenThroughRepositoriesIsWhatHomeShows() = runBlocking {
        lateinit var data: HomeData
        withContext(Dispatchers.IO) {
            val db = freshDatabase()
            val noteRepo = NoteRepository(db.noteDao())
            val reminderRepo = ReminderRepository(db.reminderDao())
            val memoryRepo = MemoryRepository(db.memoryDao(), db.memoryMediaDao())
            val moodRepo = MoodEntryRepository(db.moodEntryDao())
            val dateRepo = ImportantDateRepository(db.importantDateDao())

            noteRepo.create(
                Note(title = "Weekend ideas", content = "The little pasta place.", category = "idea", id = "h-n1", createdAt = now, updatedAt = now)
            )
            reminderRepo.create(
                Reminder(title = "Book the dinner", scheduledDate = "2026-02-10", id = "h-r1", createdAt = now, updatedAt = now)
            )
            memoryRepo.create(
                Memory(title = "Rainy night", caption = "We ran home laughing.", memoryDate = "2025-11-02", id = "h-m1", createdAt = now, updatedAt = now)
            )
            moodRepo.create(
                MoodEntry(
                    moodValue = "happy", moodEmoji = MOOD_EMOJI, note = "Ordinary, good.",
                    profileId = "h-owner", entryDate = Phase4HomeSeed.TODAY, id = "h-mo1", createdAt = now, updatedAt = now,
                )
            )
            dateRepo.create(
                ImportantDate(title = "The day we met", date = "2021-03-21", recurrence = "yearly", id = "h-d1", createdAt = now, updatedAt = now)
            )

            val today = LocalDate.parse(Phase4HomeSeed.TODAY)
            data = buildHomeData(
                owner = null,
                partner = null,
                relationship = null,
                notes = noteRepo.observeAll().first(),
                reminders = reminderRepo.observeAll().first(),
                memories = memoryRepo.observeAll().first(),
                moods = moodRepo.observeAll().first(),
                dates = dateRepo.observeAll().first(),
                today = today,
                hour = 9,
            )
        }

        val today = LocalDate.parse(Phase4HomeSeed.TODAY)
        assertEquals("Weekend ideas", notesTileDetail(data.notes))
        assertEquals("Feb 10, 2026 · Book the dinner", remindersTileDetail(data.reminders, today))
        assertEquals("Rainy night", recentMemory(data.memories)!!.title)
        assertEquals("happy", data.todayMood!!.moodValue)
        assertEquals("The day we met", data.upcomingMoment!!.title)
    }

    @Test
    fun homeDataIsNotPopulatedWhenNothingWasEverWritten() = runBlocking {
        lateinit var data: HomeData
        withContext(Dispatchers.IO) {
            val db = freshDatabase()
            val profileRepo = com.twohearts.app.data.repository.ProfileRepository(db.profileDao())
            val service = com.twohearts.app.services.relationship.RelationshipService(
                profileRepository = profileRepo,
                relationshipRepository = com.twohearts.app.data.repository.CoupleRelationshipRepository(
                    db.coupleRelationshipDao()
                ),
            )
            data = buildHomeData(
                owner = service.observeOwner().first(),
                partner = service.observePartner().first(),
                relationship = service.observeRelationship().first(),
                notes = NoteRepository(db.noteDao()).observeAll().first(),
                reminders = ReminderRepository(db.reminderDao()).observeAll().first(),
                memories = MemoryRepository(db.memoryDao(), db.memoryMediaDao()).observeAll().first(),
                moods = MoodEntryRepository(db.moodEntryDao()).observeAll().first(),
                dates = ImportantDateRepository(db.importantDateDao()).observeAll().first(),
                today = LocalDate.parse(Phase4HomeSeed.TODAY),
                hour = 9,
            )
        }

        // The whole point: an untouched install yields nothing to display, and
        // Home must render invitations rather than fabricated content.
        assertNull(data.owner)
        assertNull(data.partner)
        assertNull(data.relationship)
        assertNull(data.daysTogether)
        assertNull(data.upcomingMoment)
        assertNull(data.todayMood)
        assertNull(recentMemory(data.memories))
        assertTrue(data.notes.isEmpty())
        assertTrue(data.reminders.isEmpty())
    }

    /**
     * A database with no rows from any other test.
     *
     * Room's singleton is shared process-wide, so this deletes every table's
     * contents before the test writes its own — otherwise a seeded render
     * harness could make an "empty" assertion pass or fail depending on test
     * order, which would be worse than useless.
     */
    private fun freshDatabase(): TwoHeartsDatabase {
        val db = TwoHeartsDatabase.getDatabase(
            androidx.test.core.app.ApplicationProvider.getApplicationContext()
        )
        db.clearAllTables()
        return db
    }

    private fun profile(name: String, role: String) =
        Profile(name = name, role = role, id = "p-$name", createdAt = now, updatedAt = now)

    private fun note(title: String) =
        Note(title = title, content = "", category = "general", id = "n-$title", createdAt = now, updatedAt = now)

    private fun reminder(title: String, date: String) =
        Reminder(title = title, scheduledDate = date, id = "r-$title", createdAt = now, updatedAt = now)

    private fun memory(title: String) =
        Memory(title = title, memoryDate = "2025-01-01", id = "m-$title", createdAt = now, updatedAt = now)

    private companion object {
        const val now = "2026-01-15T10:00:00Z"
        const val MOOD_EMOJI = "\uD83D\uDE0A"
    }
}
