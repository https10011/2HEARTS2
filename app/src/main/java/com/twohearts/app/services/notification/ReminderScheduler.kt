package com.twohearts.app.services.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import androidx.work.await
import androidx.work.workDataOf
import com.twohearts.app.MainActivity
import com.twohearts.app.R
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.NotificationCenterEntry
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.datetime.nextCalendarOccurrence
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val ALERT_WORK = "twohearts.reminders"
private const val REMINDER_ID = "reminder"
private const val OCCURRENCE = "occurrence"
internal const val OPEN_ALERTS = "com.twohearts.app.OPEN_ALERTS"
private const val REFRESH_WORK = "twohearts.reminders.refresh"
private val MAX_DELIVERY_DELAY = Duration.ofDays(1)

// Keep the requested local slot even if a daylight-saving gap shifts its resolved time.
internal data class ReminderOccurrence(val date: LocalDate, val time: LocalTime, val instant: Instant) {
    fun deliveryKey(id: String): String = alertKey("$id@local:$date:$time")
    fun workKey(id: String): String = alertKey("$id@${instant.toEpochMilli()}")
}

private fun alertKey(value: String): String = UUID.nameUUIDFromBytes(value.toByteArray(Charsets.UTF_8)).toString()

internal fun nextReminderAlert(reminder: Reminder, after: Instant, zone: ZoneId): Instant? =
    nextReminderOccurrence(reminder, after, zone)?.instant

internal fun nextReminderOccurrence(reminder: Reminder, after: Instant, zone: ZoneId): ReminderOccurrence? {
    if (!reminder.notificationEnabled || reminder.status != "pending" || reminder.deletedAt != null) return null
    val base = runCatching { LocalDate.parse(reminder.scheduledDate) }.getOrNull() ?: return null
    val time = runCatching { LocalTime.parse(reminder.scheduledTime) }.getOrNull() ?: return null
    val date = nextCalendarOccurrence(base, reminder.recurrence, after.atZone(zone).toLocalDate()) ?: return null
    val instant = date.atTime(time).atZone(zone).toInstant().truncatedTo(ChronoUnit.MILLIS)
    if (instant.isAfter(after)) return ReminderOccurrence(date, time, instant)
    val next = nextCalendarOccurrence(base, reminder.recurrence, date.plusDays(1)) ?: return null
    return ReminderOccurrence(next, time, next.atTime(time).atZone(zone).toInstant().truncatedTo(ChronoUnit.MILLIS))
}

internal class ReminderScheduler(context: Context) {
    private val work = WorkManager.getInstance(context.applicationContext)

    suspend fun reconcile(reminders: List<Reminder>, enabled: Boolean, clockChanged: Boolean = false) =
        reconcileLock.withLock {
            if (!enabled) {
                work.cancelAllWorkByTag(ALERT_WORK).await()
                return@withLock
            }
            val now = Instant.now()
            val zone = ZoneId.systemDefault()
            val wanted = reminders.mapNotNull { reminder ->
                nextReminderOccurrence(reminder, now, zone)?.let { reminder.id to it }
            }
            val active = work.getWorkInfos(WorkQuery.Builder.fromTags(listOf(ALERT_WORK))
                .addStates(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED))
                .build()).get()
            val expected = wanted.mapTo(mutableSetOf()) { (id, occurrence) -> occurrence.workKey(id) }
            // Allow valid due work to finish after Android resumes it.
            for (reminder in reminders) {
                val due = nextReminderOccurrence(reminder, now.minus(MAX_DELIVERY_DELAY).minusMillis(1), zone)
                if (due != null && !due.instant.isAfter(now)) expected += due.workKey(reminder.id)
            }
            for (request in active) {
                if (request.tags.none { it in expected }) work.cancelWorkById(request.id).await()
            }
            val policy = if (clockChanged) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
            wanted.forEach { (id, occurrence) -> enqueue(id, occurrence, policy) }
        }

    suspend fun enqueue(reminder: Reminder, after: Instant, zone: ZoneId) {
        nextReminderOccurrence(reminder, after, zone)?.let { enqueue(reminder.id, it, ExistingWorkPolicy.KEEP) }
    }

    private suspend fun enqueue(id: String, occurrence: ReminderOccurrence, policy: ExistingWorkPolicy) {
        val key = occurrence.workKey(id)
        val delay = (occurrence.instant.toEpochMilli() - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ReminderDeliveryWorker>()
            .setInputData(workDataOf(REMINDER_ID to id, OCCURRENCE to occurrence.instant.toEpochMilli()))
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(ALERT_WORK).addTag(key)
            .build()
        work.enqueueUniqueWork(key, policy, request).await()
    }

    private companion object {
        val reconcileLock = Mutex()
    }
}

class ReminderClockChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_TIME_CHANGED && intent.action != Intent.ACTION_TIMEZONE_CHANGED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                WorkManager.getInstance(context).enqueueUniqueWork(
                    REFRESH_WORK, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<ReminderClockRefreshWorker>().build(),
                ).await()
            } catch (error: Exception) {
                Log.w("TwoHearts", "Could not refresh reminder schedules after a clock change", error)
            } finally {
                pending.finish()
            }
        }
    }
}

class ReminderClockRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val context = applicationContext
            val settings = SettingsStorage.getInstance(context).settings.first()
            val reminders = TwoHeartsDatabase.getDatabase(context).reminderDao().getAll().first()
            ReminderScheduler(context).reconcile(reminders, settings.notificationsEnabled && settings.remindersEnabled, clockChanged = true)
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

internal fun reminderChannelEnabled(context: Context): Boolean {
    val manager = context.getSystemService(NotificationManager::class.java)
    return NotificationManagerCompat.from(context).areNotificationsEnabled() &&
        manager.getNotificationChannel(NotificationService.CHANNEL_REMINDERS)?.importance != NotificationManager.IMPORTANCE_NONE
}

class ReminderDeliveryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getString(REMINDER_ID) ?: return Result.failure()
        val epoch = inputData.getLong(OCCURRENCE, -1)
        if (epoch <= 0) return Result.failure()
        return try {
            val at = Instant.ofEpochMilli(epoch)
            val context = applicationContext
            val settings = SettingsStorage.getInstance(context).settings.first()
            if (!settings.notificationsEnabled || !settings.remindersEnabled) return Result.success()
            val db = TwoHeartsDatabase.getDatabase(context)
            val reminder = db.reminderDao().getById(id) ?: return Result.success()
            val zone = ZoneId.systemDefault()
            val now = Instant.now()
            val occurrence = nextReminderOccurrence(reminder, at.minusMillis(1), zone)
            if (occurrence == null || occurrence.instant != at) {
                ReminderScheduler(context).enqueue(reminder, now, zone)
                return Result.success()
            }
            if (now.isBefore(at)) return Result.retry()
            if (!now.isAfter(at.plus(MAX_DELIVERY_DELAY))) {
                NotificationService(context).initialize()
                if (reminderChannelEnabled(context)) {
                    try {
                        deliverReminder(context, db, reminder, occurrence, now)
                    } catch (_: SecurityException) {
                        // Permission was withdrawn before the system accepted the notification.
                    }
                }
            }
            if (reminder.recurrence != "none") ReminderScheduler(context).enqueue(reminder, now, zone)
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

internal suspend fun claimReminderDelivery(
    db: TwoHeartsDatabase,
    reminder: Reminder,
    occurrence: ReminderOccurrence,
    now: Instant,
    post: (NotificationCenterEntry) -> Unit,
) = db.withTransaction {
    if (db.reminderDao().getById(reminder.id) != reminder) return@withTransaction
    val dao = db.notificationCenterDao()
    val entry = NotificationCenterEntry(
        title = reminder.title,
        body = reminder.description.orEmpty().ifBlank { "Your scheduled reminder is due." },
        kind = "reminder", originFeature = "reminders", originId = reminder.id,
        channelId = NotificationService.CHANNEL_REMINDERS, id = occurrence.deliveryKey(reminder.id),
        createdAt = now.toString(), updatedAt = now.toString(),
    )
    // Respect receipts written by versions that used the scheduled instant as their key.
    if (dao.getById(occurrence.workKey(reminder.id)) == null && dao.insertIfAbsent(entry) != -1L) post(entry)
}

private suspend fun deliverReminder(
    context: Context,
    db: TwoHeartsDatabase,
    reminder: Reminder,
    occurrence: ReminderOccurrence,
    now: Instant,
) = claimReminderDelivery(db, reminder, occurrence, now) { entry ->
    val open = Intent(context, MainActivity::class.java).apply {
        action = OPEN_ALERTS
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val contentIntent = PendingIntent.getActivity(
        context, 0, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_REMINDERS)
        .setSmallIcon(R.drawable.brand_logo_mark).setContentTitle(entry.title).setContentText(entry.body)
        .setContentIntent(contentIntent).setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_SECRET).build()
    NotificationManagerCompat.from(context).notify(entry.id, 1, notification)
}
