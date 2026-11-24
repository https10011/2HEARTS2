package com.twohearts.app

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.TimelineEventRepository
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.RecordLookup
import com.twohearts.app.ui.navigation.RecordLookupScreen
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.notes.NoteCategory
import com.twohearts.app.ui.screens.notes.NoteDetail
import com.twohearts.app.ui.screens.notes.NoteEditor
import com.twohearts.app.ui.screens.notes.NotesHome
import com.twohearts.app.ui.screens.notes.filterNotes
import com.twohearts.app.ui.screens.timeline.AddEvent
import com.twohearts.app.ui.screens.timeline.EventDetail
import com.twohearts.app.ui.screens.timeline.TimelineHome
import com.twohearts.app.ui.screens.timeline.timelineMonths
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import java.time.YearMonth
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
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationWritingTest {
    private val now = "2026-10-26T12:00:00Z"
    private val body = "We missed the last train and walked home.\n\nThe lights on the river stayed with me."
    private val letter = Note("Between the stations", body, "love-letter", id = "letter", createdAt = now, updatedAt = now)
    private val plan = Note("Weekend kitchen plans", "Pick up a new tin of tea.", "idea", id = "plan", createdAt = now, updatedAt = now)
    private val event = moment("river", "An evening on the river", "2026-10-26", body)
    private val earlier = moment("station", "The night train", "2026-09-09")

    @Test
    fun legacyAndNativeLoveLetterKeysComposeWithTrimmedLocalSearch() {
        assertEquals(NoteCategory.LOVE_LETTER, NoteCategory.fromString(" love-letter "))
        assertEquals(NoteCategory.LOVE_LETTER, NoteCategory.fromString("love_letter"))
        val current = letter.copy(id = "current", category = "love_letter")
        assertEquals(
            listOf(letter.id, current.id),
            filterNotes(listOf(letter, plan, current), "  last train ", NoteCategory.LOVE_LETTER).map { it.id },
        )
    }

    @Test
    fun monthsUseRealDateOrderAndKeepInvalidLegacyRowsReachable() {
        val sameDay = event.copy(id = "earlier-write", createdAt = "2026-10-25T00:00:00Z")
        val invalid = moment("legacy", "A keepsake with a recovered date", "2026-02-30")
        val groups = timelineMonths(listOf(earlier, invalid, sameDay, event))
        assertEquals(listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 9), null), groups.keys.toList())
        assertEquals(listOf(event.id, sameDay.id), groups.getValue(YearMonth.of(2026, 10)).map { it.event.id })
        assertEquals(invalid.id, groups.getValue(null).single().event.id)
    }

    @Test
    fun notesAndMomentsRetainUpdatesAndDeletionAcrossDatabaseReopen() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val name = "writing-stage-reopen"
        fun open() = Room.databaseBuilder(app, TwoHeartsDatabase::class.java, name).build()
        try {
            val first = open()
            try {
                val notes = NoteRepository(first.noteDao())
                val timeline = TimelineEventRepository(first.timelineEventDao())
                notes.create(letter)
                timeline.create(event)
                notes.update(letter.id, mapOf("content" to "$body\n\nA line added later."))
                timeline.update(event.id, mapOf("eventDate" to "2026-10-27", "description" to null))
            } finally {
                first.close()
            }
            val reopened = open()
            try {
                val notes = NoteRepository(reopened.noteDao())
                val timeline = TimelineEventRepository(reopened.timelineEventDao())
                val saved = filterNotes(notes.observeAll().first(), "added later", NoteCategory.LOVE_LETTER).single()
                assertEquals(letter.category, saved.category)
                assertTrue(saved.content.startsWith(body))
                assertEquals("2026-10-27", timeline.getById(event.id)?.eventDate)
                assertNull(timeline.getById(event.id)?.description)
                notes.softDelete(saved.id)
                timeline.softDelete(event.id)
            } finally {
                reopened.close()
            }
            val afterDelete = open()
            try {
                assertNull(NoteRepository(afterDelete.noteDao()).getById(letter.id))
                assertNull(TimelineEventRepository(afterDelete.timelineEventDao()).getById(event.id))
            } finally {
                afterDelete.close()
            }
        } finally {
            app.deleteDatabase(name)
        }
    }

    @Test
    fun nativeNotesFilterReadsOldCategoriesAndOpensTheMatchingRecord() {
        var opened: String? = null
        render("notes-light", RoutePath.APP_NOTES) {
            NotesHome(listOf(letter, plan), { opened = it }, {}, {}, {})
        }.use { scene ->
            assertTrue(scene.click("Category · All notes"))
            scene.captureDialog("note-filters-light")
            assertTrue(scene.clickDialog("Love Letter"))
            assertTrue(scene.setText("Search your notes", "  last train  "))
            assertTrue(scene.activityText("1 note"))
            assertFalse(scene.activityText(plan.title))
            assertTrue(scene.click(letter.title))
            assertEquals(letter.id, opened)
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-mdpi")
    fun narrowLargeEditorsKeepSaveReachableAndPreserveValidInput() {
        var saved: Triple<String, String, NoteCategory>? = null
        render("note-editor-dark-200", RoutePath.APP_NOTES_ADD, dark = true, fontScale = 2f) {
            NoteEditor(onSave = { title, content, category -> saved = Triple(title, content, category) }, onBack = {})
        }.use { scene ->
            assertTrue(scene.click("Keep this note"))
            assertTrue(scene.activityError("Give this note a title."))
            assertNull(saved)
            assertTrue(scene.setText("Title", letter.title))
            assertTrue(scene.setText("Note", body))
            assertTrue(scene.click("Category · General"))
            assertTrue(scene.clickDialog("Love Letter"))
            assertTrue(scene.click("Keep this note"))
            assertEquals(Triple(letter.title, body, NoteCategory.LOVE_LETTER), saved)
        }

        var date: String? = null
        render("moment-editor-dark-200", RoutePath.APP_TIMELINE_ADD, dark = true, fontScale = 2f) {
            AddEvent(initialTitle = event.title, initialDate = "2026-02-30", initialDescription = body,
                onSave = { _, edited, _ -> date = edited }, onBack = {})
        }.use { scene ->
            assertTrue(scene.click("Add to our story"))
            assertTrue(scene.activityError("Enter a valid date."))
            assertNull(date)
            assertTrue(scene.setText("Date (YYYY-MM-DD)", event.eventDate))
            assertTrue(scene.click("Add to our story"))
            assertEquals(event.eventDate, date)
        }
    }

    @Test
    fun timelineSelectionDetailsAndConfirmationsUseTheExistingDestinations() {
        var opened: String? = null
        render("timeline-light", RoutePath.APP_TIMELINE) {
            TimelineHome(listOf(earlier, event), { opened = it }, {}, {})
        }.use { scene ->
            assertTrue(scene.activityText("October 2026"))
            assertTrue(scene.activityText("September 2026"))
            assertTrue(scene.click(event.title))
            assertEquals(event.id, opened)
        }
        var removed = false
        render("note-detail-dark", RoutePath.APP_NOTES_DETAIL, dark = true) {
            NoteDetail(letter, {}, { removed = true }, {})
        }.use { scene ->
            assertTrue(scene.activityText(body))
            assertTrue(scene.click("Delete note"))
            assertTrue(scene.dialogText("Delete this note?"))
            assertTrue(scene.clickDialog("Cancel"))
            assertFalse(removed)
        }
        render("timeline-detail-light", RoutePath.APP_TIMELINE_DETAIL) {
            EventDetail(event, {}, { removed = true }, {})
        }.use { scene ->
            assertTrue(scene.activityText(body))
            assertTrue(scene.click("Delete moment"))
            scene.captureDialog("delete-moment-light")
            assertTrue(scene.clickDialog("Delete"))
            assertTrue(removed)
        }
    }

    @Test
    fun readingRetainsLongParagraphsAndShowsUnreadableDatesAsStored() {
        val longBody = List(12) { body }.joinToString("\n\n")
        render("long-note-dark", RoutePath.APP_NOTES_DETAIL, dark = true) {
            NoteDetail(letter.copy(content = longBody), {}, {}, {})
        }.use { scene -> assertTrue(scene.activityText(longBody)) }
        val invalid = event.copy(eventDate = "2026-02-30")
        render("unreadable-moment-date", RoutePath.APP_TIMELINE_DETAIL) {
            EventDetail(invalid, {}, {}, {})
        }.use { scene -> assertTrue(scene.activityText(invalid.eventDate)) }
    }

    @Test
    fun missingNotesAndMomentsOfferBackThroughTheSharedLookupScreen() {
        val missing = RecordLookup<Any>()
        runBlocking { missing.read { null } }
        val labels = mapOf("Note" to RoutePath.APP_NOTES_DETAIL, "Timeline moment" to RoutePath.APP_TIMELINE_DETAIL)
        labels.forEach { (label, route) ->
            var returned = false
            render("missing-${label.lowercase().replace(' ', '-')}", route, dark = true) {
                RecordLookupScreen(label, missing) { returned = true }
            }.use { scene ->
                assertTrue(scene.activityText("$label unavailable"))
                assertTrue(scene.click("Go back"))
                assertTrue(returned)
            }
        }
    }

    private fun moment(id: String, title: String, date: String, description: String? = null) =
        TimelineEvent(title = title, eventDate = date, description = description, id = id, createdAt = now, updatedAt = now)

    private fun render(
        name: String, route: String, dark: Boolean = false, fontScale: Float = 1f, content: @Composable () -> Unit,
    ): NativeComposeScene = NativeComposeScene(File("build/transformation-12")).apply {
        setContent {
            CompositionLocalProvider(LocalShellRoute provides route) {
                TwoHeartsTheme(darkTheme = dark, textSizeScale = fontScale, reduceMotion = true) { content() }
            }
        }
        capture(activityView, name)
    }
}
