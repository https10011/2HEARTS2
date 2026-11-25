package com.twohearts.app

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.data.entity.Place
import com.twohearts.app.services.media.MediaThumbnails
import com.twohearts.app.services.media.PlacePhotoSelection
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.mood.MoodEntryScreen
import com.twohearts.app.ui.screens.mood.MoodHistory
import com.twohearts.app.ui.screens.mood.MoodHome
import com.twohearts.app.ui.screens.period.LogPeriod
import com.twohearts.app.ui.screens.period.PeriodCalendarScreen
import com.twohearts.app.ui.screens.period.PeriodHistoryScreen
import com.twohearts.app.ui.screens.period.PeriodHome
import com.twohearts.app.ui.screens.period.PeriodSettingsScreen
import com.twohearts.app.ui.screens.places.CreatePlace
import com.twohearts.app.ui.screens.places.PlaceDetail
import com.twohearts.app.ui.screens.places.PlacesHome
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationJournalUiTest {
    private val stamp = "2024-02-29T10:00:00Z"
    private val today = LocalDate.parse("2024-02-29")
    private val lookout = Place(
        "A lookout above the old harbour", city = "Porto", country = "Portugal", address = "North stairs",
        category = " Date ", notes = "We took the long way home while the lights turned on across the river.",
        photoRef = "place-photo", id = "lookout", createdAt = stamp, updatedAt = stamp,
    )
    private val cafe = Place(
        "Corner café", city = "Porto", category = "restaurant",
        notes = "A table outside when the sun came back.", id = "cafe", createdAt = stamp, updatedAt = stamp,
    )
    private val mood = MoodEntry("calm", "😌", "The quiet was welcome today.", "self", today.toString(), "calm", stamp, stamp)
    private val otherMood = mood.copy(moodValue = "tired", moodEmoji = "😴", note = "A long trip.", profileId = "other", id = "other", entryDate = "2024-02-26")
    private val period = PeriodEntry(
        startDate = "2024-02-26", endDate = today.toString(), flowLevel = "medium", note = "The note I saved for myself.",
        profileId = "owner", id = "saved-cycle", createdAt = stamp, updatedAt = stamp,
    )

    @Test
    fun nativePlacesListFiltersOldCategoryAndOpensTheLocalPlace() = withPlacePhoto { media ->
        var selected: String? = null
        var add = 0
        render("places-light", RoutePath.APP_PLACES) {
            PlacesHome(listOf(lookout, cafe), { selected = it }, { add++ }, {}, mapOf(lookout.id to media))
        }.use { scene ->
            assertTrue(scene.click("Category · All places"))
            scene.captureDialog("place-categories-light")
            assertTrue(scene.clickDialog("Restaurant"))
            assertFalse(scene.activityText(lookout.name))
            assertTrue(scene.click(cafe.name))
            assertEquals(cafe.id, selected)
            assertTrue(scene.click("Category · Restaurant"))
            assertTrue(scene.clickDialog("Date"))
            assertTrue(scene.click(lookout.name))
            assertEquals(lookout.id, selected)
            assertTrue(scene.click("Save a place"))
            assertEquals(1, add)
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h740dp-mdpi")
    fun nativePlaceReaderAndEditorHandleLongTextAndRemovingARestoredPhoto() = withPlacePhoto { media ->
        var edited = false
        var deleted = false
        render("place-detail-dark-large", RoutePath.APP_PLACES_DETAIL, dark = true, scale = 2f) {
            PlaceDetail(lookout, { edited = true }, { deleted = true }, {}, media)
        }.use { scene ->
            assertTrue(scene.activityText("North stairs\nPorto, Portugal"))
            assertTrue(scene.clickDescription("Edit place"))
            assertTrue(edited)
            assertTrue(scene.scrollActivityBy(950f))
            assertTrue(scene.click("Delete place"))
            scene.captureDialog("place-delete-dark-large")
            assertTrue(scene.clickDialog("Delete"))
            assertTrue(deleted)
        }
        val writes = mutableListOf<List<Any?>>()
        render("place-editor-dark-large", RoutePath.APP_PLACES_EDIT, dark = true, scale = 2f) {
            CreatePlace(
                placeId = lookout.id, initialName = lookout.name, initialCategory = lookout.category,
                initialNotes = lookout.notes.orEmpty(), initialCity = lookout.city.orEmpty(),
                onSave = { name, _, city, story, category, photo -> writes += listOf(name, city, story, category, photo) },
                onBack = {}, initialMedia = media,
            )
        }.use { scene ->
            assertTrue(scene.setText("Name", "  Evening at the lookout  "))
            assertTrue(scene.scrollActivityBy(1200f))
            scene.capture(scene.activityView, "place-editor-photo-dark-large")
            assertTrue(scene.click("Remove photo"))
            assertTrue(scene.click("Save place"))
            assertEquals(
                listOf("Evening at the lookout", "Porto", lookout.notes, lookout.category, PlacePhotoSelection.Remove),
                writes.single(),
            )
        }
    }

    @Test
    fun nativeMoodQuickCheckInAndHistoryPreserveOwnershipAndWindow() {
        val saved = mutableStateOf<MoodEntry?>(null)
        var opened = 0
        render("mood-home-empty-light", RoutePath.APP_MOOD) {
            MoodHome(saved.value, listOf(mood, otherMood.copy(profileId = "self", id = "earlier")), {}, { opened++ }, {}, { choice ->
                saved.value = mood.copy(moodValue = choice.value, moodEmoji = choice.emoji)
            }, today = today)
        }.use { scene ->
            assertTrue(scene.click("Calm"))
            assertEquals("calm", saved.value?.moodValue)
            assertTrue(scene.activityText(mood.note!!))
            scene.capture(scene.activityView, "mood-home-saved-light")
            assertTrue(scene.click("Mood history"))
            assertEquals(1, opened)
        }
        var openedId: String? = null
        render("mood-history-light", RoutePath.APP_MOOD_HISTORY) {
            MoodHistory(
                listOf(mood, otherMood, mood.copy(id = "january", entryDate = "2024-01-17", moodValue = "happy", moodEmoji = "😊")),
                {}, ownerId = "self", onEdit = { openedId = it.id }, today = today,
            )
        }.use { scene ->
            assertTrue(scene.activityText("Feb 26, 2024 · Other profile"))
            assertFalse(scene.activityText("January 2024"))
            assertTrue(scene.click("Edit check-in"))
            assertEquals(mood.id, openedId)
            assertFalse(scene.click("Edit check-in", occurrence = 1))
            assertTrue(scene.click("All"))
            scene.capture(scene.activityView, "mood-history-all-light")
            assertTrue(scene.activityText("January 2024"))
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h740dp-mdpi")
    fun nativeMoodEditSavesAndConfirmsDeletionAtLargeType() {
        var change: Pair<String, String?>? = null
        var deleted = false
        render("mood-edit-dark-large", RoutePath.APP_MOOD_EDIT, dark = true, scale = 2f) {
            MoodEntryScreen(
                initialMood = mood.moodValue, initialNote = mood.note.orEmpty(), onBack = {},
                onSave = { value, _, note -> change = value to note }, entryDate = mood.entryDate, entryId = mood.id,
                onDelete = { deleted = true },
            )
        }.use { scene ->
            assertTrue(scene.click("Grateful"))
            assertTrue(scene.scrollActivityBy(1400f))
            assertTrue(scene.setText("Private note (optional)", "  A pause after lunch.  "))
            scene.capture(scene.activityView, "mood-edit-note-dark-large")
            assertTrue(scene.click("Save check-in"))
            assertEquals("grateful" to "A pause after lunch.", change)
            assertTrue(scene.click("Delete check-in"))
            assertTrue(scene.clickDialog("Delete"))
            assertTrue(deleted)
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w320dp-h618dp-mdpi")
    fun nativePeriodCalendarAnnouncesLoggedDatesAndNextMonthEstimatesAtLargeType() {
        render("period-calendar-logged-dark-large", RoutePath.APP_PERIOD_CALENDAR, dark = true, scale = 2f) {
            PeriodCalendarScreen(
                listOf(period.copy(id = "malformed", startDate = "unknown"), period), {},
                cycleLength = 4, periodLength = 2, today = today,
            )
        }.use { scene ->
            assertTrue(scene.activityDescription("February 29, 2024, today, logged period"))
            assertTrue(scene.clickDescription("Next month"))
            assertTrue(scene.activityText("March 2024"))
            assertTrue(scene.activityDescription("March 1, 2024, estimated period"))
            assertFalse(scene.activityDescription("March 1, 2024, logged period"))
            scene.capture(scene.activityView, "period-calendar-estimated-dark-large")
            assertTrue(scene.clickDescription("Previous month"))
            assertTrue(scene.activityText("February 2024"))
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w320dp-h618dp-mdpi")
    fun nativeDatePickerStartsWithTheSelectedLeapDayVisibleAtLargeType() {
        render("period-log-dark-large", RoutePath.APP_PERIOD_LOG_EDIT, dark = true, scale = 2f) {
            LogPeriod(
                entryId = period.id, initialStartDate = period.startDate, initialEndDate = period.endDate.orEmpty(),
                onBack = {}, today = today, onSave = { _, _, _, _ -> },
            )
        }.use { scene ->
            assertTrue(scene.clickDescription("End date (optional): 29 February 2024"))
            scene.captureDialog("period-date-picker-dark-large")
            assertTrue(scene.dialogText("29"))
            assertTrue(scene.dialogText("2024"))
            assertTrue(scene.clickDialog("Use 29 February 2024"))
            assertTrue(scene.clickDescription("End date (optional): 29 February 2024"))
            assertTrue(scene.clickDialog("Clear date"))
            assertTrue(scene.activityDescription("End date (optional): Choose when it ended"))
        }
    }

    @Test
    fun nativePeriodTrackerAndHistoryKeepPrivateEntryActionsDiscoverable() {
        val selected = mutableListOf<String>()
        render("period-home-light", RoutePath.APP_PERIOD) {
            PeriodHome(period.copy(endDate = null), { selected += "log" }, { selected += "calendar" },
                { selected += "history" }, { selected += "settings" }, {},
                cycleLength = 4, onEditLatest = { selected += "edit" }, today = today)
        }.use { scene ->
            assertTrue(scene.activityText("March 1, 2024"))
            listOf("Add end date", "Log a period", "Calendar", "History", "Cycle settings").forEach { assertTrue(scene.click(it)) }
            assertEquals(listOf("edit", "log", "calendar", "history", "settings"), selected)
        }
        render("period-history-light", RoutePath.APP_PERIOD_HISTORY) {
            PeriodHistoryScreen(listOf(period, period.copy(id = "damaged", startDate = "unknown", endDate = null, note = null)), {}, { selected += it.id })
        }.use { scene ->
            assertTrue(scene.activityText("Ended February 29, 2024 · 4 days"))
            assertTrue(scene.activityText("Check these dates"))
            assertTrue(scene.click("Edit entry"))
            assertEquals(period.id, selected.last())
        }
    }

    @Test
    fun nativePeriodLogAndSettingsValidateAndPersistEdits() {
        var saved: List<String?>? = null
        var deleted = false
        render("period-log-light", RoutePath.APP_PERIOD_LOG_EDIT) {
            LogPeriod(
                entryId = period.id, initialStartDate = period.startDate, initialEndDate = period.endDate.orEmpty(),
                initialNote = period.note.orEmpty(), onBack = {}, today = today,
                onSave = { start, end, flow, note -> saved = listOf(start, end, flow, note) }, onDelete = { deleted = true },
            )
        }.use { scene ->
            assertTrue(scene.clickDescription("End date (optional): 29 February 2024"))
            scene.captureDialog("period-date-picker-light")
            assertTrue(scene.clickDialog("Clear date"))
            assertTrue(scene.click("Light"))
            assertTrue(scene.click("Save period"))
            assertEquals(listOf(period.startDate, null, "light", period.note), saved)
            assertTrue(scene.click("Delete entry"))
            assertTrue(scene.clickDialog("Delete"))
            assertTrue(deleted)
        }
        var lengths: Pair<Int, Int>? = null
        render("period-settings-light", RoutePath.APP_PERIOD_SETTINGS) {
            PeriodSettingsScreen({}, onSave = { cycle, days -> lengths = cycle to days })
        }.use { scene ->
            assertTrue(scene.setText("Typical cycle length (days)", "3"))
            assertTrue(scene.activityError("Enter 1–3 days."))
            scene.capture(scene.activityView, "period-settings-error-light")
            scene.click("Save settings")
            assertEquals(null, lengths)
            assertTrue(scene.setText("Typical cycle length (days)", "31"))
            assertTrue(scene.click("Save settings"))
            assertEquals(31 to 5, lengths)
        }
    }

    private fun withPlacePhoto(block: (MediaAsset) -> Unit) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val fixture = placeTestPhoto(app)
        try {
            val media = MediaAsset(fixture.absolutePath, "image/jpeg", fixture.length(), id = "place-photo", createdAt = stamp, updatedAt = stamp)
            runBlocking { checkNotNull(MediaThumbnails.load(media.filePath, media.updatedAt, 512, normalizeExif = true)) }
            block(media)
        } finally {
            fixture.delete()
        }
    }

    private fun render(
        name: String, route: String, dark: Boolean = false, scale: Float = 1f, content: @Composable () -> Unit,
    ): NativeComposeScene = NativeComposeScene(File("build/transformation-14")).apply {
        setContent {
            CompositionLocalProvider(LocalShellRoute provides route) {
                TwoHeartsTheme(darkTheme = dark, textSizeScale = scale, reduceMotion = true) { content() }
            }
        }
        capture(activityView, name)
    }
}
