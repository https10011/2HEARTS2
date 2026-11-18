package com.twohearts.app

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.data.repository.CoupleRelationshipRepository
import com.twohearts.app.data.repository.ImportantDateRepository
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.PlaceRepository
import com.twohearts.app.data.repository.ProfileRepository
import com.twohearts.app.data.repository.TimelineEventRepository
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.ui.navigation.AppShell
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.us.UsContent
import com.twohearts.app.ui.screens.us.UsHighlights
import com.twohearts.app.ui.screens.us.usHighlights
import com.twohearts.app.ui.theme.TextScalingLevel
import com.twohearts.app.ui.theme.TwoHeartsTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(InternalComposeUiApi::class)
class Phase5UsTest {
    private val today = LocalDate.of(2026, 11, 18)
    private val now = "2026-01-15T10:00:00Z"
    private var destination: String? = null

    @Test
    fun emptyActionsLeadToExistingFeatures() {
        val empty = usHighlights(null, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), today)
        render("empty", Story(null, null, empty)) { nodes ->
            assertTrue(nodes.node("Relationship date").boundsInRoot.height > 0)
            mapOf(
                "Save an important date" to RoutePath.APP_IMPORTANT_DATES,
                "Add a memory" to RoutePath.APP_MEMORIES_ADD,
                "Memories" to RoutePath.APP_MEMORIES,
                "Timeline" to RoutePath.APP_TIMELINE,
                "Places" to RoutePath.APP_PLACES,
                "Mood" to RoutePath.APP_MOOD,
                "Period tracker" to RoutePath.APP_PERIOD,
                "Vault" to RoutePath.APP_VAULT,
            ).forEach { (label, route) -> assertDestination(nodes, label, route) }
        }
    }

    @Test
    fun savedStoryRendersAndTheMemoryStreamUpdatesAfterDeletion() {
        val db = makeDatabase()
        try {
            val repository = MemoryRepository(db.memoryDao(), db.memoryMediaDao())
            val before = runBlocking { seedStory(db) }
            render("populated", before) { nodes ->
                assertTrue(nodes.node("Alex").boundsInRoot.height > 0)
                assertTrue(nodes.node("Sam").boundsInRoot.height > 0)
                nodes.node("The day we met")
                nodes.node("1 memory saved")
                nodes.node("Harbour café · 1 place saved")
                nodes.node("Today · Calm")
                assertDestination(nodes, "An afternoon at the coast", "${RoutePath.APP_MEMORIES}/coast")
            }
            val after = runBlocking {
                withTimeout(10_000) {
                    val changes = Channel<List<Memory>>(Channel.CONFLATED)
                    val observer = launch { repository.observeAll().collect { changes.send(it) } }
                    try {
                        assertEquals("coast", changes.receive().single().id)
                        repository.softDelete("coast")
                        val memories = changes.receive()
                        assertTrue(memories.isEmpty())
                        before.copy(highlights = before.highlights.copy(memory = null, memoryCount = memories.size))
                    } finally {
                        observer.cancel()
                        changes.cancel()
                    }
                }
            }
            render("after-delete", after) { nodes ->
                assertFalse(nodes.any { it.hasText("An afternoon at the coast") })
                assertDestination(nodes, "Add a memory", RoutePath.APP_MEMORIES_ADD)
            }
        } finally {
            db.close()
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h780dp-mdpi")
    fun darkNarrowScreenAccommodatesLargeTextAndLongContent() {
        val partnerName = "Samira Florence Del Rosario Bennett"
        val title = "An afternoon walking the long way along the coast before it started raining"
        val db = makeDatabase()
        val story = try {
            runBlocking { seedStory(db, "Alexandria River Montgomery Bell", partnerName, title) }
        } finally {
            db.close()
        }
        render("narrow-dark-extra-large", story, dark = true, scale = TextScalingLevel.EXTRA_LARGE) { nodes ->
            assertTrue(nodes.node(partnerName).boundsInRoot.height > 0)
            assertDestination(nodes, title, "${RoutePath.APP_MEMORIES}/coast")
            assertDestination(nodes, "Vault", RoutePath.APP_VAULT)
        }
    }

    private fun makeDatabase(): TwoHeartsDatabase {
        val app = ApplicationProvider.getApplicationContext<Application>()
        return Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build()
    }

    private suspend fun seedStory(
        db: TwoHeartsDatabase,
        ownerName: String = "Alex",
        partnerName: String = "Sam",
        title: String = "An afternoon at the coast",
    ): Story {
        val relationship = RelationshipService(ProfileRepository(db.profileDao()), CoupleRelationshipRepository(db.coupleRelationshipDao()))
        val owner = relationship.createOwner(ownerName, null)
        val partner = relationship.createPartner(partnerName, null)
        relationship.createRelationship(owner.id, partner.id, today.minusYears(2).plusDays(3).toString())
        db.importantDateDao().insert(
            ImportantDate(
                title = "The day we met", date = today.minusYears(3).toString(), recurrence = "yearly",
                id = "met", createdAt = now, updatedAt = now,
            ),
        )
        db.memoryDao().insert(
            Memory(title = title, memoryDate = today.minusDays(7).toString(), id = "coast", createdAt = now, updatedAt = now),
        )
        db.timelineEventDao().insert(
            TimelineEvent(
                title = "Our first trip", eventDate = today.minusMonths(6).toString(),
                id = "trip", createdAt = now, updatedAt = now,
            ),
        )
        db.placeDao().insert(Place(name = "Harbour café", id = "cafe", createdAt = now, updatedAt = now))
        db.moodEntryDao().insert(
            MoodEntry(
                moodValue = "calm", moodEmoji = "😌", profileId = owner.id, entryDate = today.toString(),
                id = "calm", createdAt = now, updatedAt = now,
            ),
        )
        return Story(
            owner = relationship.observeOwner().first(),
            partner = relationship.observePartner().first(),
            highlights = usHighlights(
                relationship = relationship.observeRelationship().first(),
                dates = ImportantDateRepository(db.importantDateDao()).observeAll().first(),
                memories = MemoryRepository(db.memoryDao(), db.memoryMediaDao()).observeAll().first(),
                events = TimelineEventRepository(db.timelineEventDao()).observeAll().first(),
                places = PlaceRepository(db.placeDao()).observeAll().first(),
                moods = MoodEntryRepository(db.moodEntryDao()).observeAll().first(),
                today = today,
            ),
        )
    }

    private fun render(
        filename: String,
        story: Story,
        dark: Boolean = false,
        scale: TextScalingLevel = TextScalingLevel.DEFAULT,
        assertions: (List<SemanticsNode>) -> Unit,
    ) {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        try {
            val activity = controller.get()
            activity.setContent {
                TwoHeartsTheme(darkMode = dark, textScalingLevel = scale, reduceMotion = true) {
                    AppShell(RoutePath.APP_US, onNavigate = {}, onBack = {}, canNavigateBack = false) {
                        UsContent(story.owner, story.partner, story.highlights) { destination = it }
                    }
                }
            }
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            val view = activity.window.decorView
            val metrics = activity.resources.displayMetrics
            view.measure(
                View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.EXACTLY),
            )
            view.layout(0, 0, metrics.widthPixels, metrics.heightPixels)
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            try {
                view.draw(Canvas(bitmap))
                val directory = File("build/phase5-us").apply { mkdirs() }
                File(directory, "$filename.png").outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            } finally {
                bitmap.recycle()
            }
            assertions(checkNotNull(composeRoot(view)).semanticsOwner.getAllSemanticsNodes(mergingEnabled = true))
        } finally {
            controller.pause().stop().destroy()
        }
    }

    private fun assertDestination(nodes: List<SemanticsNode>, label: String, route: String) {
        val action = checkNotNull(nodes.node(label).config.getOrNull(SemanticsActions.OnClick)?.action)
        assertTrue(action())
        assertEquals(route, destination)
    }

    private fun List<SemanticsNode>.node(label: String) = single { it.hasText(label) }

    private fun SemanticsNode.hasText(label: String) =
        config.getOrNull(SemanticsProperties.Text)?.any { it.text == label } == true

    private fun composeRoot(view: View): ViewRootForTest? {
        if (view is ViewRootForTest) return view
        if (view is ViewGroup) {
            repeat(view.childCount) { index ->
                composeRoot(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private data class Story(val owner: Profile?, val partner: Profile?, val highlights: UsHighlights)
}
