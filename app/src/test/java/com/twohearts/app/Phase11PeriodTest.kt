package com.twohearts.app

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.data.entity.PeriodSettings
import com.twohearts.app.data.repository.PeriodEntryRepository
import com.twohearts.app.data.repository.PeriodSettingsRepository
import com.twohearts.app.ui.screens.period.loggedPeriodRange
import com.twohearts.app.ui.screens.period.nextPeriodEstimate
import com.twohearts.app.ui.screens.period.validPeriodDates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase11PeriodTest {
    private val today = LocalDate.of(2024, 3, 3)
    private val entry = PeriodEntry(
        startDate = "2024-02-28", endDate = "2024-03-02", note = "Private entry",
        profileId = "owner", id = "entry", createdAt = "2024-02-28T09:00:00Z",
        updatedAt = "2024-02-28T09:00:00Z",
    )

    @Test fun calendarIncludesLeapDayAndHandlesIncompleteEntries() {
        val leapDay = LocalDate.of(2024, 2, 29)
        assertTrue(leapDay in requireNotNull(loggedPeriodRange(entry)))
        assertFalse(leapDay in requireNotNull(loggedPeriodRange(entry.copy(endDate = null))))
        assertNull(loggedPeriodRange(entry.copy(startDate = "unknown")))
        assertEquals(LocalDate.of(2024, 3, 27), nextPeriodEstimate(entry, 28, today))
        assertNull(nextPeriodEstimate(entry, 28, LocalDate.of(2024, 4, 1)))
    }

    @Test fun logRejectsInvalidAndFutureDates() {
        assertTrue(validPeriodDates("2024-02-29", "", today))
        assertTrue(validPeriodDates("2024-02-29", "2024-02-29", today))
        assertFalse(validPeriodDates("2023-02-29", "", today))
        assertFalse(validPeriodDates("2024-03-04", "", today))
        assertFalse(validPeriodDates("2024-03-02", "2024-03-01", today))
        assertFalse(validPeriodDates("2024-03-02", "2024-03-04", today))
    }

    @Test fun settingsAndEntryEditsPreserveIdsAndCanClearOptionalFields() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Application>(), TwoHeartsDatabase::class.java,
        ).build()
        try {
            val settings = PeriodSettingsRepository(db.periodSettingsDao())
            settings.create(PeriodSettings(28, 5, "legacy", "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z"))
            settings.saveLengths(31, 6)
            val saved = requireNotNull(settings.observe().first())
            assertEquals("legacy", saved.id)
            assertEquals(31, saved.cycleLengthDays)
            assertEquals(6, saved.periodLengthDays)
            assertEquals(1, settings.count())

            val entries = PeriodEntryRepository(db.periodEntryDao())
            entries.create(entry)
            entries.update(entry.id, mapOf("endDate" to null, "note" to null))
            val updated = requireNotNull(entries.getById(entry.id))
            assertNull(updated.endDate)
            assertNull(updated.note)
            assertEquals(entry.startDate, updated.startDate)
            entries.softDelete(entry.id)
            assertTrue(entries.observeAll().first().isEmpty())
        } finally {
            db.close()
        }
    }
}
