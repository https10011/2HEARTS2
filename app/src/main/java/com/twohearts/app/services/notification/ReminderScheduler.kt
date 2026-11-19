package com.twohearts.app.services.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.twohearts.app.MainActivity
import com.twohearts.app.R
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.NotificationCenterEntry
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.datetime.nextCalendarOccurrence
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

private const val ALERT_WORK = "twohearts.reminders"
private const val REMINDER_ID = "reminder"
private const val OCCURRENCE = "occurrence"
internal const val OPEN_ALERTS = "com.twohearts.app.OPEN_ALERTS"
private val MAX_DELIVERY_DELAY = java.time.Duration.ofDays(1)

internal fun nextReminderAlert(reminder: Reminder, after: Instant, zone: ZoneId): Instant? {
    if (!reminder.notificationEnabled || reminder.status != "pending" || reminder.deletedAt != null) return null
    val base = runCatching { LocalDate.parse(reminder.scheduledDate) }.getOrNull() ?: return null
    val time = runCatching { LocalTime.parse(reminder.scheduledTime) }.getOrNull() ?: return null
    val day = after.atZone(zone).toLocalDate()
    val date = nextCalendarOccurrence(base, reminder.recurrence, day) ?: return null
    val candidate = date.atTime(time).atZone(zone).toInstant()
    if (candidate.isAfter(after)) return candidate
    return nextCalendarOccurrence(base, reminder.recurrence, date.plusDays(1))?.atTime(time)?.atZone(zone)?.toInstant()
}

private data class Alert(val id: String, val at: Instant) {
    val key = UUID.nameUUIDFromBytes("$id@${at.toEpochMilli()}".toByteArray(Charsets.UTF_8)).toString()
}

internal class ReminderScheduler(context: Context) {
    private val work = WorkManager.getInstance(context.applicationContext)

    fun reconcile(reminders: List<Reminder>, enabled: Boolean) {
        if (!enabled) {
            work.cancelAllWorkByTag(ALERT_WORK)
            return
        }
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val wanted = reminders.mapNotNull { reminder ->
            nextReminderAlert(reminder, now, zone)?.let { Alert(reminder.id, it) }
        }
        val active = work.getWorkInfos(WorkQuery.Builder.fromTags(listOf(ALERT_WORK))
            .addStates(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED))
            .build()).get()
        val expected = wanted.mapTo(mutableSetOf()) { it.key }
        // Allow a valid due occurrence to finish after Android resumes the job.
        for (reminder in reminders) {
            val due = nextReminderAlert(reminder, now.minus(MAX_DELIVERY_DELAY), zone)
            if (due != null && !due.isAfter(now)) expected += Alert(reminder.id, due).key
        }
        for (request in active) {
            if (request.tags.none { it in expected }) work.cancelWorkById(request.id)
        }
        wanted.forEach(::enqueue)
    }

    fun enqueue(alert: Reminder, after: Instant, zone: ZoneId) {
        nextReminderAlert(alert, after, zone)?.let { enqueue(Alert(alert.id, it)) }
    }

    private fun enqueue(alert: Alert) {
        val delay = (alert.at.toEpochMilli() - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ReminderDeliveryWorker>()
            .setInputData(workDataOf(REMINDER_ID to alert.id, OCCURRENCE to alert.at.toEpochMilli()))
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(ALERT_WORK).addTag(alert.key)
            .build()
        work.enqueueUniqueWork(alert.key, ExistingWorkPolicy.KEEP, request)
    }
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
            if (nextReminderAlert(reminder, at.minusMillis(1), zone) != at) {
                ReminderScheduler(context).enqueue(reminder, now, zone)
                return Result.success()
            }
            val notifications = NotificationManagerCompat.from(context)
            if (notifications.areNotificationsEnabled() && !now.isAfter(at.plus(MAX_DELIVERY_DELAY))) {
                val alert = Alert(id, at)
                val entry = NotificationCenterEntry(
                    title = reminder.title,
                    body = reminder.description.orEmpty().ifBlank { "Your scheduled reminder is due." },
                    kind = "reminder", originFeature = "reminders", originId = reminder.id,
                    channelId = NotificationService.CHANNEL_REMINDERS, id = alert.key,
                    createdAt = now.toString(), updatedAt = now.toString(),
                )
                val inserted = db.notificationCenterDao().insertIfAbsent(entry)
                NotificationService(context, db.notificationRegistryDao()).initialize()
                if (inserted != -1L) {
                    val open = Intent(context, MainActivity::class.java).apply {
                        action = OPEN_ALERTS
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    val contentIntent = PendingIntent.getActivity(
                        context, 0, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    )
                    val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_REMINDERS)
                        .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(entry.title).setContentText(entry.body)
                        .setContentIntent(contentIntent).setAutoCancel(true)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET).build()
                    try {
                        notifications.notify(alert.key, 1, notification)
                    } catch (_: SecurityException) {
                        // Android notification permission may change while this job runs.
                    }
                }
            }
            ReminderScheduler(context).enqueue(reminder, maxOf(at, now), zone)
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
