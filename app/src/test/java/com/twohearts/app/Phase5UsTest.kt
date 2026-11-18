package com.twohearts.app

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.data.repository.CoupleRelationshipRepository
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.ProfileRepository
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.ui.navigation.AppShell
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.theme.TextScalingLevel
import com.twohearts.app.ui.theme.TwoHeartsTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Phase5UsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: TwoHeartsDatabase
    private var destination: String? = null
    private val today = LocalDate.now()
    private val now = "2026-01-15T10:00:00Z"

    @After
    fun closeDatabase() {
        if (::database.isInitialized) database.close()
    }

    private fun prepare(longContent: Boolean = false) {
        database = Room.inMemoryDatabaseBuilder(compose.activity, TwoHeartsDatabase::class.java).build()
        relationship = RelationshipService(
            ProfileRepository(database.profileDao()),
            CoupleRelationshipRepository(database.coupleRelationshipDao()),
        )
        if (longContent) {
            runBlocking {
                addStory(
                    "Alexandria River Montgomery Bell",
                    "Samira Florence Del Rosario Bennett",
                    "An afternoon walking the long way along the coast before it started raining",
                )
            }
        }
    }

    private lateinit var relationship: RelationshipService

    private suspend fun addStory(
        ownerName: String = "Alex",
        partnerName: String = "Sam",
        title: String = "An afternoon at the coast",
    ) {
        val owner = relationship.createOwner(ownerName, null)
        val partner = relationship.createPartner(partnerName, null)
        val start = today.minusYears(2).plusDays(3)
        relationship.createRelationship(owner.id, partner.id, start.toString())
        database.importantDateDao().insert(
            ImportantDate(
                title = "The day we met",
                date = today.minusYears(3).toString(),
                recurrence = "yearly",
                id = "met", createdAt = now, updatedAt = now,
            ),
        )
        database.memoryDao().insert(
            Memory(
                title = title, memoryDate = today.minusDays(7).toString(),
                id = "coast", createdAt = now, updatedAt = now,
            ),
        )
        database.timelineEventDao().insert(
            TimelineEvent(
                title = "Our first trip", eventDate = today.minusMonths(6).toString(),
                id = "trip", createdAt = now, updatedAt = now,
            ),
        )
        database.placeDao().insert(Place(name = "Harbour café", id = "cafe", createdAt = now, updatedAt = now))
        database.moodEntryDao().insert(
            MoodEntry(
                moodValue = "calm", moodEmoji = "😌", profileId = owner.id,
                entryDate = today.toString(), id = "calm", createdAt = now, updatedAt = now,
            ),
        )
    }

    private fun open(dark: Boolean = false, scale: TextScalingLevel = TextScalingLevel.DEFAULT) {
        compose.setContent {
            TwoHeartsTheme(darkMode = dark, textScalingLevel = scale, reduceMotion = true) {
                AppShell(RoutePath.APP_US, onNavigate = {}, onBack = {}, canNavigateBack = false) {
                    TestUsScreen(relationship, database) { destination = it }
                }
            }
        }
    }

    private fun waitForText(text: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun capture(name: String) {
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            try {
                view.draw(Canvas(bitmap))
                val directory = File("build/phase5-us").apply { mkdirs() }
                File(directory, "$name.png").outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            } finally {
                bitmap.recycle()
            }
        }
    }

    private fun routes(label: String, route: String) {
        compose.onNodeWithText(label).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(route, destination) }
    }

    @Test
    fun emptyActionsLeadToExistingFeatures() {
        prepare()
        open()
        compose.onNodeWithText("Relationship date").assertIsDisplayed()
        capture("empty")
        routes("Save an important date", RoutePath.APP_IMPORTANT_DATES)
        routes("Add a memory", RoutePath.APP_MEMORIES_ADD)
        routes("Memories", RoutePath.APP_MEMORIES)
        routes("Timeline", RoutePath.APP_TIMELINE)
        routes("Places", RoutePath.APP_PLACES)
        routes("Mood", RoutePath.APP_MOOD)
        capture("places-and-mood")
        routes("Period tracker", RoutePath.APP_PERIOD)
        routes("Vault", RoutePath.APP_VAULT)
    }

    @Test
    fun observesSavedStoryAndRefreshesAfterDeletingItsLatestMemory() {
        prepare()
        open()
        runBlocking { addStory() }
        waitForText("The day we met")
        compose.onNodeWithText("Alex").assertIsDisplayed()
        compose.onNodeWithText("Sam").assertIsDisplayed()
        capture("populated")
        routes("An afternoon at the coast", "${RoutePath.APP_MEMORIES}/coast")
        compose.onNodeWithText("1 memory saved").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Harbour café · 1 place saved").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Today · Calm").performScrollTo().assertIsDisplayed()

        runBlocking { MemoryRepository(database.memoryDao(), database.memoryMediaDao()).softDelete("coast") }
        waitForText("Add a memory")
        compose.onNodeWithText("An afternoon at the coast").assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w360dp-h780dp-mdpi")
    fun darkNarrowScreenAccommodatesLargeTextAndLongContent() {
        prepare(longContent = true)
        open(dark = true, scale = TextScalingLevel.EXTRA_LARGE)
        val name = "Samira Florence Del Rosario Bennett"
        waitForText(name)
        compose.onNodeWithText(name).assertIsDisplayed()
        capture("narrow-dark-extra-large")
        routes(
            "An afternoon walking the long way along the coast before it started raining",
            "${RoutePath.APP_MEMORIES}/coast",
        )
        capture("narrow-dark-extra-large-scroll")
        routes("Vault", RoutePath.APP_VAULT)
    }
}
