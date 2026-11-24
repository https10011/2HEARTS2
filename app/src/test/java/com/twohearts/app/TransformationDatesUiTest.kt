package com.twohearts.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.NotificationCenterEntry
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.importantdates.ImportantDatesScreen
import com.twohearts.app.ui.screens.notifications.NotificationCenterContent
import com.twohearts.app.ui.screens.reminders.CreateReminder
import com.twohearts.app.ui.screens.reminders.ReminderDetail
import com.twohearts.app.ui.screens.reminders.RemindersHome
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationDatesUiTest {
    private val now = "2026-10-31T00:15:00Z"
    private val today = LocalDate.parse("2026-10-31")
    private val late = reminder("late", "Pick up the anniversary prints", "2026-10-26").copy(status = "missed")
    private val future = reminder("next", "Call after the train gets in", LocalDate.now().plusDays(3).toString()).copy(recurrence = "weekly")
    private val anniversary = date("leap", "The day we said yes", "2024-02-29", "yearly")
    private val spring = date("spring", "Our spring picnic", "2027-03-08")
    private val past = date("past", "The first apartment", "2023-03-08")
    private val invalid = date("restore", "A date from an old backup", "2026-02-30")

    @Test
    fun nativeTaskListOpensAndCompletesSavedRowsAndRetainsTheAddAction() {
        val records = mutableStateOf(listOf(late, future))
        var opened: String? = null
        var add = 0
        render("reminders-light", RoutePath.APP_REMINDERS) {
            RemindersHome(records.value, { opened = it }, { add++ }, {}, { target ->
                records.value = records.value.map { if (it.id == target.id) it.copy(status = "completed") else it }
            })
        }.use { scene ->
            assertTrue(scene.activityText("Needs attention"))
            assertTrue(scene.activityText("Scheduled"))
            assertTrue(scene.click(future.title))
            assertEquals(future.id, opened)
            assertTrue(scene.clickDescription("Complete ${late.title}"))
            assertFalse(scene.activityText(late.title))
            assertTrue(scene.click("Completed"))
            assertTrue(scene.activityText(late.title))
            scene.capture(scene.activityView, "reminders-completed-light")
            assertTrue(scene.click("Add reminder"))
            assertEquals(1, add)
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h740dp-mdpi")
    fun nativeReminderEditorValidatesAndSavesTrimmedLocalFieldsAtLargeType() {
        val writes = mutableListOf<List<Any?>>()
        render("reminder-editor-dark-large", RoutePath.APP_REMINDERS_ADD, dark = true, fontScale = 2f) {
            CreateReminder(
                initialDate = "2026-02-30", initialTime = "25:04",
                onSave = { title, details, date, time, recurrence, notify ->
                    writes += listOf(title, details, date, time, recurrence, notify)
                },
                onBack = {},
            )
        }.use { scene ->
            assertTrue(scene.click("Save reminder"))
            assertEquals(0, writes.size)
            assertTrue(scene.activityError("Give the reminder a title."))
            scene.capture(scene.activityView, "reminder-editor-error-dark-large")
            assertTrue(scene.setText("Title", " Pick up the printed photos "))
            assertTrue(scene.setText("Date (yyyy-mm-dd)", " 2026-10-31 "))
            assertTrue(scene.setText("Time (optional)", " 18:30 "))
            assertTrue(scene.scrollActivityBy(950f))
            assertTrue(scene.click("Every month"))
            scene.capture(scene.activityView, "reminder-schedule-dark-large")
            assertTrue(scene.click("Save reminder"))
            assertEquals(listOf("Pick up the printed photos", null, "2026-10-31", "18:30", "monthly", true), writes.single())
        }
    }

    @Test
    fun nativeReminderReaderShowsActionsAndConfirmsDeletion() {
        var toggled = 0
        var deleted = 0
        var edited = 0
        render("reminder-detail-light", "${RoutePath.APP_REMINDERS}/{reminderId}") {
            ReminderDetail(future, { edited++ }, { deleted++ }, {}, { toggled++ })
        }.use { scene ->
            assertTrue(scene.clickDescription("Edit reminder"))
            assertTrue(scene.click("Complete reminder"))
            assertTrue(scene.click("Delete reminder"))
            assertTrue(scene.dialogText("Delete this reminder?"))
            scene.captureDialog("reminder-delete-light")
            assertTrue(scene.clickDialog("Delete"))
            assertEquals(listOf(1, 1, 1), listOf(edited, toggled, deleted))
        }
    }

    @Test
    fun nativeDateEditorPreservesOriginalDateAndRepairsMalformedRestoredDates() {
        val records = mutableStateOf(listOf(anniversary, spring, past, invalid))
        render("dates-light", RoutePath.APP_IMPORTANT_DATES) {
            ImportantDatesScreen(
                records.value,
                onSaveDate = { item, title, day, repeat ->
                    val replacement = date(item?.id ?: "new", title, day, repeat)
                    records.value = records.value.filterNot { it.id == replacement.id } + replacement
                },
                onDeleteDate = { item -> records.value = records.value.filterNot { it.id == item.id } },
                onBack = {}, today = today,
            )
        }.use { scene ->
            assertTrue(scene.activityText("February 28, 2027"))
            assertTrue(scene.activityText("Past dates"))
            assertTrue(scene.activityText("Saved date needs editing"))
            assertTrue(scene.click(anniversary.title))
            assertTrue(scene.dialogText("2024-02-29"))
            scene.captureDialog("date-editor-light")
            assertTrue(scene.clickDialog("Cancel"))
            assertTrue(scene.click(invalid.title))
            assertTrue(scene.setDialogText("Date (yyyy-mm-dd)", "2026-12-01"))
            assertTrue(scene.clickDialog("Save date"))
            assertTrue(scene.activityText("December 1, 2026"))
            assertEquals("2026-12-01", records.value.first { it.id == invalid.id }.date)
            assertTrue(scene.click("Add a date"))
            assertTrue(scene.setDialogText("What happened?", "A new shared milestone"))
            assertTrue(scene.clickDialog("Save date"))
            assertEquals(today.toString(), records.value.last().date)
            assertEquals("A new shared milestone", records.value.last().title)
            scene.capture(scene.activityView, "date-after-add-light")
            assertTrue(scene.click("A new shared milestone"))
            assertTrue(scene.clickDialog("Delete date"))
            assertTrue(scene.clickDialog("Delete"))
            assertFalse(records.value.any { it.id == "new" })
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h740dp-mdpi")
    fun nativeFeedShowsLocalDayAndRoutesFromAnUnreadAlertWithAndroidPermissionOff() {
        val zone = ZoneId.of("America/New_York")
        val entry = NotificationCenterEntry(
            "Call after the train gets in", "Use the local number saved in the reminder.",
            "reminder", "reminders", "call/with space", id = "delivery", createdAt = now, updatedAt = now,
        )
        val records = mutableStateOf(listOf(entry))
        var destination: String? = null
        var settings = 0
        render("notifications-dark-large", RoutePath.APP_NOTIFICATIONS, dark = true, fontScale = 2f) {
            NotificationCenterContent(
                records.value, false, {}, { settings++ },
                markAll = { records.value = records.value.map { it.copy(read = true) } },
                openAlert = { item, route ->
                    records.value = listOf(item.copy(read = true))
                    destination = route
                },
                zone = zone, today = today,
            )
        }.use { scene ->
            assertTrue(scene.activityText("Android notifications are off"))
            assertTrue(scene.activityText("Yesterday"))
            assertTrue(scene.activityText("20:15"))
            assertTrue(scene.clickDescription("Notification settings"))
            assertEquals(1, settings)
            assertTrue(scene.click("Open"))
            assertEquals("${RoutePath.APP_REMINDERS}/call%2Fwith%20space", destination)
            assertTrue(records.value.single().read)
        }
    }

    @Test
    @Config(qualifiers = "w320dp-h618dp-mdpi")
    fun emptyAndLargeDateViewsComposeAtSmallWidths() {
        render("reminders-empty-dark", RoutePath.APP_REMINDERS, dark = true, fontScale = 2f) {
            RemindersHome(emptyList(), {}, {}, {})
        }.use { scene -> assertTrue(scene.activityText("Add reminder")) }
        render("dates-dark-large", RoutePath.APP_IMPORTANT_DATES, dark = true, fontScale = 2f) {
            ImportantDatesScreen(listOf(anniversary.copy(title = "The weekend we finally got to see each other")), { _, _, _, _ -> }, {}, {}, today)
        }.use { scene ->
            assertTrue(scene.activityText("Add a date"))
            assertTrue(scene.click("The weekend we finally got to see each other"))
            scene.captureDialog("date-editor-dark-large")
        }
        render("notifications-empty-light", RoutePath.APP_NOTIFICATIONS) {
            NotificationCenterContent(emptyList(), true, {}, {}, {}, { _, _ -> }, today = today)
        }.use { scene -> assertTrue(scene.activityText("You’re all caught up")) }
    }

    private fun reminder(id: String, title: String, day: String) = Reminder(
        title, "A little time set aside on the calendar.", day, "18:30", id = id, createdAt = now, updatedAt = now,
    )

    private fun date(id: String, title: String, day: String, repeat: String = "none") =
        ImportantDate(title, day, repeat, id = id, createdAt = now, updatedAt = now)

    private fun render(
        name: String, route: String, dark: Boolean = false, fontScale: Float = 1f, content: @Composable () -> Unit,
    ): NativeComposeScene = NativeComposeScene(File("build/transformation-13")).apply {
        setContent {
            CompositionLocalProvider(LocalShellRoute provides route) {
                TwoHeartsTheme(darkTheme = dark, textSizeScale = fontScale, reduceMotion = true) { content() }
            }
        }
        capture(activityView, name)
    }
}
