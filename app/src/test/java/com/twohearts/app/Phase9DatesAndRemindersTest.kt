package com.twohearts.app

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.ui.screens.importantdates.nextOccurrence
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase9DatesAndRemindersTest {
    @Test fun importantDatesResolveTheirNextMeaningfulOccurrence() {
        val date = ImportantDate("Anniversary", "2020-05-04", "yearly", id = "a", createdAt = "x", updatedAt = "x")
        assertEquals(LocalDate.of(2027, 5, 4), nextOccurrence(date, LocalDate.of(2026, 6, 1)))
        assertEquals(LocalDate.of(2026, 5, 4), nextOccurrence(date, LocalDate.of(2026, 5, 4)))
    }

    @Test fun remindersCanCompleteAndClearOptionalDetails() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Application>(), TwoHeartsDatabase::class.java).build()
        try {
            val repository = ReminderRepository(db.reminderDao())
            repository.create(Reminder("Bring flowers", "White roses", "2026-12-01", "18:00", id = "r", createdAt = "x", updatedAt = "x"))
            val updated = repository.update("r", mapOf("status" to "completed", "description" to null, "scheduledTime" to null))
            assertEquals("completed", updated?.status)
            assertNull(updated?.description)
            assertNull(updated?.scheduledTime)
        } finally { db.close() }
    }
}
