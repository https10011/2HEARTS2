package com.twohearts.app

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.notification.ReminderScheduler
import com.twohearts.app.ui.components.VideoPreviewDialog
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class ReleaseRuntimeTest {
    @get:Rule
    val compose = createComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app = instrumentation.targetContext

    @Test
    fun videoPreviewDisplaysPixelsFromTheAndroidDecoder() {
        val video = File(app.cacheDir, "rehearsal-video.mp4")
        instrumentation.context.assets.open(video.name).use { fixture ->
            video.outputStream().use { fixture.copyTo(it) }
        }
        try {
            compose.setContent {
                TwoHeartsTheme { VideoPreviewDialog(video.absolutePath) {} }
            }
            compose.waitUntil(timeoutMillis = 20_000) {
                val screen = instrumentation.uiAutomation.takeScreenshot() ?: return@waitUntil false
                try {
                    val visible = blueSamples(screen) > 150
                    if (visible) {
                        File(app.filesDir, "rehearsal-video.png").outputStream().use {
                            screen.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }
                    }
                    visible
                } finally {
                    screen.recycle()
                }
            }
        } finally {
            video.delete()
        }
    }

    @Test
    fun scheduledReminderAppearsInAndroidAndTheLocalNotificationCenter() = runBlocking {
        instrumentation.uiAutomation.grantRuntimePermission(app.packageName, Manifest.permission.POST_NOTIFICATIONS)
        val settings = SettingsStorage.getInstance(app)
        settings.setNotificationsEnabled(true)
        settings.setRemindersEnabled(true)
        val db = TwoHeartsDatabase.getDatabase(app)
        val due = ZonedDateTime.now().plusSeconds(12).truncatedTo(ChronoUnit.SECONDS)
        val now = Instant.now()
        val reminder = Reminder(
            title = "Android release notification fixture",
            scheduledDate = due.toLocalDate().toString(),
            scheduledTime = due.toLocalTime().toString(),
            id = UUID.randomUUID().toString(),
            createdAt = now.toString(),
            updatedAt = now.toString(),
        )
        val notifications = app.getSystemService(NotificationManager::class.java)
        db.reminderDao().insert(reminder)
        try {
            ReminderScheduler(app).enqueue(reminder, now, due.zone)
            withTimeout(90_000) {
                while (notifications.activeNotifications.none {
                    it.notification.extras.getCharSequence(Notification.EXTRA_TITLE) == reminder.title
                }) {
                    delay(250)
                }
            }
            val delivered = notifications.activeNotifications.single {
                it.notification.extras.getCharSequence(Notification.EXTRA_TITLE) == reminder.title
            }
            assertNotNull(delivered.notification.contentIntent)
            val entries = withTimeout(5_000) {
                db.notificationCenterDao().getAll().first { list -> list.any { it.originId == reminder.id } }
            }.filter { it.originId == reminder.id }
            assertEquals(listOf(reminder.title), entries.map { it.title })
            notifications.cancel(delivered.tag, delivered.id)
        } finally {
            val deletedAt = Instant.now().toString()
            db.reminderDao().softDelete(reminder.id, deletedAt, deletedAt)
        }
    }

    private fun blueSamples(bitmap: Bitmap): Int {
        var matching = 0
        for (y in 0 until bitmap.height step 8) {
            for (x in 0 until bitmap.width step 8) {
                val pixel = bitmap.getPixel(x, y)
                if (Color.blue(pixel) > 180 && Color.red(pixel) < 70 && Color.green(pixel) < 130) matching++
            }
        }
        return matching
    }
}
