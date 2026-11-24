package com.twohearts.app

import android.app.Application
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import androidx.concurrent.futures.ResolvableFuture
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.WorkerParameters
import androidx.work.impl.utils.taskexecutor.WorkManagerTaskExecutor
import androidx.work.workDataOf
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.NotificationCenterEntry
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.repository.ImportantDateRepository
import com.twohearts.app.data.repository.NotificationCenterRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.services.notification.OPEN_ALERTS
import com.twohearts.app.services.notification.ReminderClockChangeReceiver
import com.twohearts.app.services.notification.ReminderClockRefreshWorker
import com.twohearts.app.services.notification.ReminderDeliveryWorker
import com.twohearts.app.services.notification.claimReminderDelivery
import com.twohearts.app.services.notification.nextReminderOccurrence
import com.twohearts.app.ui.screens.home.remindersTileDetail
import com.twohearts.app.ui.screens.importantdates.nextOccurrence
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransformationReminderDeliveryTest {
    private val utc = ZoneId.of("UTC")
    private val stamp = Instant.parse("2026-01-31T00:00:00Z")
    private val reminder = Reminder(
        title = "Call after dinner", scheduledDate = "2026-01-31", scheduledTime = "18:30",
        id = "test-reminder", createdAt = stamp.toString(), updatedAt = stamp.toString(),
    )

    @Test
    fun monthEndAndLeapDayRemainAnchoredAcrossTheHomeAndDatesViews() {
        val monthly = reminder.copy(recurrence = "monthly")
        val february = LocalDate.parse("2026-02-28")
        val march = LocalDate.parse("2026-03-31")
        assertEquals("Today · Call after dinner", remindersTileDetail(listOf(monthly), february))
        assertEquals("Today · Call after dinner", remindersTileDetail(listOf(monthly), march))
        val afterFebruary = LocalDate.parse("2027-03-01")
        val nextLeapDay = LocalDate.parse("2028-02-29")
        val leap = ImportantDate("Leap day", "2024-02-29", "yearly", id = "date", createdAt = stamp.toString(), updatedAt = stamp.toString())
        assertEquals(nextLeapDay, nextOccurrence(leap, afterFebruary))
        assertEquals(
            ChronoUnit.DAYS.between(afterFebruary, nextLeapDay).toInt(),
            DateTimeHelper.daysUntilAnniversary(leap.date, afterFebruary),
        )
        assertNull(nextOccurrence(leap.copy(date = "2026-02-29"), february))
        assertEquals(LocalDate.of(2024, 2, 29), nextOccurrence(leap.copy(recurrence = "none"), february))
    }

    @Test
    fun workMovesWithTimeZoneWhileLocalReceiptAndDstGapRemainStable() {
        val local = checkNotNull(nextReminderOccurrence(reminder, stamp, utc))
        val lagos = checkNotNull(nextReminderOccurrence(reminder, stamp, ZoneId.of("Africa/Lagos")))
        assertEquals(Instant.parse("2026-01-31T18:30:00Z"), local.instant)
        assertEquals(Instant.parse("2026-01-31T17:30:00Z"), lagos.instant)
        assertNotEquals(local.workKey(reminder.id), lagos.workKey(reminder.id))
        assertEquals(local.deliveryKey(reminder.id), lagos.deliveryKey(reminder.id))

        val spring = reminder.copy(scheduledDate = "2026-03-08", scheduledTime = "02:30", recurrence = "daily")
        val ny = ZoneId.of("America/New_York")
        val gap = checkNotNull(nextReminderOccurrence(spring, Instant.parse("2026-03-08T00:00:00Z"), ny))
        assertEquals("02:30", gap.time.toString())
        assertEquals(Instant.parse("2026-03-08T07:30:00Z"), gap.instant)
        val following = checkNotNull(nextReminderOccurrence(spring, gap.instant, ny))
        assertEquals(Instant.parse("2026-03-09T06:30:00Z"), following.instant)
        assertNotEquals(gap.deliveryKey(reminder.id), following.deliveryKey(reminder.id))
    }

    @Test
    fun receiptsSurviveReopenAndConcurrentDeliveryAcrossTimeZones() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val name = "stage13-delivery-reopen"
        val first = checkNotNull(nextReminderOccurrence(reminder, stamp, utc))
        val rebased = checkNotNull(nextReminderOccurrence(reminder, stamp, ZoneId.of("Africa/Lagos")))
        var posts = 0
        try {
            Room.databaseBuilder(app, TwoHeartsDatabase::class.java, name).build().useDb { db ->
                ReminderRepository(db.reminderDao()).create(reminder)
                listOf(first, rebased).map { occurrence ->
                    async { claimReminderDelivery(db, reminder, occurrence, occurrence.instant) { posts++ } }
                }.awaitAll()
                assertEquals(1, posts)
            }
            Room.databaseBuilder(app, TwoHeartsDatabase::class.java, name).build().useDb { db ->
                val center = NotificationCenterRepository(db.notificationCenterDao())
                val reminders = ReminderRepository(db.reminderDao())
                val dates = ImportantDateRepository(db.importantDateDao())
                claimReminderDelivery(db, reminder, rebased, rebased.instant) { posts++ }
                assertEquals(1, posts)
                val id = first.deliveryKey(reminder.id)
                assertEquals(1, center.observeUnreadCount().first())
                center.markAsRead(id)
                assertEquals(0, center.observeUnreadCount().first())
                reminders.update(reminder.id, mapOf("status" to "completed"))
                assertNull(nextReminderOccurrence(checkNotNull(reminders.getById(reminder.id)), stamp, utc))
                reminders.softDelete(reminder.id)
                dates.create(ImportantDate("Our anniversary", "2024-02-29", "yearly", id = "leap", createdAt = stamp.toString(), updatedAt = stamp.toString()))
                dates.update("leap", mapOf("title" to "The day we met"))
            }
            Room.databaseBuilder(app, TwoHeartsDatabase::class.java, name).build().useDb { db ->
                assertNull(ReminderRepository(db.reminderDao()).getById(reminder.id))
                assertTrue(checkNotNull(NotificationCenterRepository(db.notificationCenterDao()).getById(first.deliveryKey(reminder.id))).read)
                val dates = ImportantDateRepository(db.importantDateDao())
                assertEquals("The day we met", dates.getById("leap")?.title)
                dates.softDelete("leap")
                assertNull(dates.getById("leap"))
            }
        } finally {
            app.deleteDatabase(name)
        }
    }

    @Test
    fun deniedSystemPostRollsBackTheReceiptAndOldReceiptsStillSuppressDelivery() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Room.inMemoryDatabaseBuilder(context, TwoHeartsDatabase::class.java).build().useDb { db ->
            db.reminderDao().insert(reminder)
            val next = checkNotNull(nextReminderOccurrence(reminder, stamp, utc))
            val failed = runCatching {
                claimReminderDelivery(db, reminder, next, next.instant) { throw SecurityException() }
            }
            assertTrue(failed.exceptionOrNull() is SecurityException)
            assertEquals(0, db.notificationCenterDao().count())
            db.notificationCenterDao().insert(
                NotificationCenterEntry(
                    reminder.title, "Reminder", "reminder", "reminders", reminder.id,
                    id = next.workKey(reminder.id), createdAt = stamp.toString(), updatedAt = stamp.toString(),
                ),
            )
            claimReminderDelivery(db, reminder, next, next.instant) { error("Would repost a legacy alert") }
            assertEquals(1, db.notificationCenterDao().count())
        }
    }

    @Test
    fun workerRespectsAndroidDenialAndAnEarlyClockAndPostsOnceWithTheExistingEntryPoint() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val manager = app.getSystemService(NotificationManager::class.java)
        val androidAlerts = shadowOf(manager)
        val db = TwoHeartsDatabase.getDatabase(app)
        val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val at = now.minusSeconds(120)
        val local = at.atZone(ZoneId.systemDefault())
        val due = reminder.copy(scheduledDate = local.toLocalDate().toString(), scheduledTime = local.toLocalTime().toString())
        db.reminderDao().insert(due)
        androidAlerts.setNotificationsEnabled(false)
        assertEquals(ListenableWorker.Result.success(), worker(app, due.id, at).doWork())
        assertEquals(0, db.notificationCenterDao().count())
        assertTrue(androidAlerts.allNotifications.isEmpty())
        androidAlerts.setNotificationsEnabled(true)
        assertEquals(ListenableWorker.Result.success(), worker(app, due.id, at).doWork())
        assertEquals(ListenableWorker.Result.success(), worker(app, due.id, at).doWork())
        assertEquals(1, db.notificationCenterDao().count())
        val posted = androidAlerts.allNotifications.single()
        assertEquals(OPEN_ALERTS, shadowOf(posted.contentIntent).savedIntent.action)
        assertEquals(MainActivity::class.java.name, shadowOf(posted.contentIntent).savedIntent.component?.className)

        val future = now.plusSeconds(300)
        val next = future.atZone(ZoneId.systemDefault())
        val notDue = due.copy(id = "future", scheduledDate = next.toLocalDate().toString(), scheduledTime = next.toLocalTime().toString())
        db.reminderDao().insert(notDue)
        assertEquals(ListenableWorker.Result.retry(), worker(app, notDue.id, future).doWork())
        assertEquals(1, db.notificationCenterDao().count())
    }

    @Test
    fun clockRefreshReplacesFutureWorkAndRebasesItsKeyAfterATimeZoneChange() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val receiver = app.packageManager.getReceiverInfo(ComponentName(app, ReminderClockChangeReceiver::class.java), 0)
        assertFalse(receiver.exported)
        val originalZone = TimeZone.getDefault()
        val work = try {
            WorkManager.getInstance(app)
        } catch (_: IllegalStateException) {
            WorkManager.initialize(app, Configuration.Builder().build())
            WorkManager.getInstance(app)
        }
        val due = reminder.copy(id = "clock-refresh-test", scheduledDate = LocalDate.now(utc).plusDays(3).toString())
        TwoHeartsDatabase.getDatabase(app).reminderDao().insert(due)

        fun enqueued(key: String) = work.getWorkInfosForUniqueWork(key).get(5, TimeUnit.SECONDS)
            .singleOrNull { it.state == WorkInfo.State.ENQUEUED }

        suspend fun refresh() {
            assertEquals(ListenableWorker.Result.success(), ReminderClockRefreshWorker(app, workerParams()).doWork())
        }

        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val initial = checkNotNull(nextReminderOccurrence(due, Instant.now(), utc)).workKey(due.id)
            refresh()
            val beforeWallChange = checkNotNull(enqueued(initial)).id
            refresh()
            assertNotEquals(beforeWallChange, checkNotNull(enqueued(initial)).id)

            val lagos = ZoneId.of("Africa/Lagos")
            TimeZone.setDefault(TimeZone.getTimeZone(lagos))
            val rebased = checkNotNull(nextReminderOccurrence(due, Instant.now(), lagos)).workKey(due.id)
            refresh()
            assertNull(enqueued(initial))
            assertTrue(enqueued(rebased) != null)
        } finally {
            TimeZone.setDefault(originalZone)
            work.cancelAllWork().await()
        }
    }

    private inline fun <T> TwoHeartsDatabase.useDb(block: (TwoHeartsDatabase) -> T): T =
        try {
            block(this)
        } finally {
            close()
        }

    private fun worker(app: Application, reminderId: String, at: Instant): ReminderDeliveryWorker =
        ReminderDeliveryWorker(app, workerParams(workDataOf("reminder" to reminderId, "occurrence" to at.toEpochMilli())))

    private fun workerParams(input: Data = Data.EMPTY): WorkerParameters {
        val executor = Executor(Runnable::run)
        return WorkerParameters(
            UUID.randomUUID(), input, emptySet(), WorkerParameters.RuntimeExtras(), 0, 0, executor, Dispatchers.Default,
            WorkManagerTaskExecutor(executor), Configuration.Builder().build().workerFactory,
            { _, _, _ -> ResolvableFuture.create<Void>().apply { set(null) } },
            { _, _, _ -> ResolvableFuture.create<Void>().apply { set(null) } },
        )
    }
}
