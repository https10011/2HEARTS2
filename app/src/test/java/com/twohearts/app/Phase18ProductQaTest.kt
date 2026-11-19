package com.twohearts.app

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.NotificationCenterEntry
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.repository.VaultItemRepository
import com.twohearts.app.services.datetime.nextCalendarOccurrence
import com.twohearts.app.services.lifecycle.LifecycleListener
import com.twohearts.app.services.lifecycle.LifecycleService
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.VaultStorage
import com.twohearts.app.services.notification.nextReminderAlert
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase18ProductQaTest {
    private val timestamp = "2026-11-19T00:00:00Z"

    @Test
    fun recurrenceAnchorsToTheOriginalCalendarDate() {
        val january = LocalDate.of(2026, 1, 31)
        assertEquals(LocalDate.of(2026, 2, 28), nextCalendarOccurrence(january, "monthly", january.plusDays(1)))
        assertEquals(LocalDate.of(2026, 3, 31), nextCalendarOccurrence(january, "monthly", LocalDate.of(2026, 3, 1)))
        val leap = LocalDate.of(2024, 2, 29)
        assertEquals(LocalDate.of(2028, 2, 29), nextCalendarOccurrence(leap, "yearly", LocalDate.of(2028, 1, 1)))
        assertEquals(january.plusDays(7), nextCalendarOccurrence(january, "weekly", january.plusDays(1)))
    }

    @Test
    fun reminderAlertsRespectTimeZoneCompletionAndPresenceOfATime() {
        val reminder = Reminder(
            title = "Test reminder", scheduledDate = "2026-11-19", scheduledTime = "10:30",
            id = "fixture", createdAt = timestamp, updatedAt = timestamp,
        )
        val zone = ZoneId.of("Africa/Lagos")
        val now = Instant.parse(timestamp)
        val at = Instant.parse("2026-11-19T09:30:00Z")
        assertEquals(at, nextReminderAlert(reminder, now, zone))
        assertNull(nextReminderAlert(reminder, at, zone))
        assertNull(nextReminderAlert(reminder.copy(status = "completed"), now, zone))
        assertNull(nextReminderAlert(reminder.copy(notificationEnabled = false), now, zone))
        assertNull(nextReminderAlert(reminder.copy(scheduledTime = null), now, zone))
        assertEquals(Instant.parse("2026-11-20T09:30:00Z"), nextReminderAlert(reminder.copy(recurrence = "daily"), at, zone))
    }

    @Test
    fun foregroundListenersCanReadTimeAwayBeforeItIsCleared() {
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry(this)
        }
        var clock = 500L
        val service = LifecycleService(owner.lifecycle) { clock }
        val foreground = mutableListOf<Boolean>()
        service.addListener(object : LifecycleListener {
            override fun onForeground() {
                foreground += service.hasExceededTimeout(60)
            }
        })
        service.start()
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        clock += 60_000
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        assertEquals(listOf(false, true), foreground)
        assertFalse(service.isInBackground())
        service.stop()
    }

    @Test
    fun privateNotesAreReadThroughVaultAndDeliveryEntriesAreIdempotent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, TwoHeartsDatabase::class.java).build()
        try {
            val items = VaultItemRepository(db.vaultItemDao())
            val vault = VaultStorage(context, db, MediaStorage(context), items)
            val saved = vault.add(" Test vault item ", "note", "Test content")
            assertEquals("Test vault item", items.observeAll().first().single().title)
            assertEquals("Test content", items.getById(saved.id)?.contentText)
            assertTrue(db.noteDao().getAll().first().isEmpty())
            items.softDelete(saved.id)
            assertNull(items.getById(saved.id))
            val center = db.notificationCenterDao()
            val entry = NotificationCenterEntry(
                title = "Test reminder", body = "Test notice", kind = "reminder", originFeature = "reminders",
                originId = "fixture", id = "occurrence", createdAt = timestamp, updatedAt = timestamp,
            )
            assertTrue(center.insertIfAbsent(entry) > 0)
            assertEquals(-1L, center.insertIfAbsent(entry))
            assertEquals(1, center.getUnreadCount().first())
        } finally {
            db.close()
        }
    }
}
