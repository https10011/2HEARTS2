package com.twohearts.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.MediaThumbnails
import com.twohearts.app.ui.navigation.AppShell
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.home.HomeContent
import com.twohearts.app.ui.screens.home.YukiPresence
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import java.time.Duration
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationHomeUiTest {
    private val today = LocalDate.of(2026, 1, 15)
    private val now = "2026-01-15T10:00:00Z"
    private val storage = MediaStorage(ApplicationProvider.getApplicationContext())

    @Test
    @Config(qualifiers = "w411dp-h891dp-xxhdpi")
    fun twoPrivatePhotoFilesRenderAndMemoryOpensTheSavedRecord() {
        val ownerPhoto = createPhoto(Color.rgb(180, 88, 105), Color.rgb(255, 216, 182))
        val partnerPhoto = createPhoto(Color.rgb(58, 103, 113), Color.rgb(242, 186, 146))
        val owner = profile("Alexandria", "owner", ownerPhoto)
        val partner = profile("Sam", "partner", partnerPhoto)
        val memory = Memory(
            title = "Sunday morning, pancakes",
            caption = "No plans, nowhere to be.",
            memoryDate = "2026-01-04",
            id = "m-1", createdAt = now, updatedAt = now,
        )
        val opened = mutableListOf<String>()
        try {
            render(owner, partner, relationship(), memories = listOf(memory), opened = opened) { frame, _, nodes ->
                assertPortraitColors(frame, ownerRed = true, partnerBlue = true)
                assertTrue(nodes.any { it.hasText("1,066 days together") })
                assertTrue(nodes.any { it.hasText("The day we met") })
                val portrait = nodes.single { it.hasText(owner.name) && it.hasText(partner.name) }
                portrait.click()
                nodes.single { it.hasText(memory.title) && it.hasClick() }.click()
                assertEquals(listOf(RoutePath.APP_US, "/app/memories/m-1"), opened)
                save(frame, "both-photos")
            }
        } finally {
            storage.delete(ownerPhoto)
            storage.delete(partnerPhoto)
        }
    }

    @Test
    fun pairedPortraitsAndLongLabelsStayInsideTheNarrowLargeTypeLayout() {
        val ownerPhoto = createPhoto(Color.rgb(180, 88, 105), Color.rgb(255, 216, 182))
        val owner = profile("Alexandria Cassandra Mwangi-Osei", "owner", ownerPhoto)
        val partner = profile("Bartholomew Fitzgerald-Wellington III", "partner")
        val opened = mutableListOf<String>()
        try {
            render(owner, partner, relationship(), dark = true, fontScale = 2f, opened = opened) { frame, view, nodes ->
                assertPortraitColors(frame, ownerRed = true, partnerBlue = false)
                val ownerName = nodes.single { it.hasText(owner.name) && !it.hasText(partner.name) }
                val partnerName = nodes.single { it.hasText(partner.name) && !it.hasText(owner.name) }
                val ownerBounds = ownerName.boundsInRoot
                val partnerBounds = partnerName.boundsInRoot
                assertInViewport(ownerBounds, view)
                assertInViewport(partnerBounds, view)
                assertTrue(ownerBounds.bottom <= partnerBounds.top)
                assertEquals(ownerBounds.width, partnerBounds.width, 1f)
                nodes.single { it.hasDescription("Notifications") }.click()
                nodes.single { it.hasDescription("Search all records") }.click()
                assertEquals(listOf(RoutePath.APP_NOTIFICATIONS, RoutePath.APP_SEARCH), opened)
                save(frame, "one-photo-360-200")
            }
        } finally {
            storage.delete(ownerPhoto)
        }
    }

    @Test
    fun missingPhotoFilesAndMissingRelationshipShowHonestFirstUseActions() {
        val owner = profile("Avery", "owner", "045ca758-60c9-4778-8c1d-c244d1d8ea00")
        val opened = mutableListOf<String>()
        render(owner, null, null, opened = opened) { frame, _, nodes ->
            assertTrue(nodes.any { it.hasText("A") })
            assertTrue(nodes.any { it.hasText("Your special someone") })
            assertTrue(nodes.none { it.hasText("Coming up") })
            nodes.single { it.hasText("Set your shared date") && it.hasClick() }.click()
            nodes.single { it.hasText("Save a date that matters") && it.hasClick() }.click()
            assertEquals(listOf(RoutePath.APP_SETTINGS_RELATIONSHIP, RoutePath.APP_IMPORTANT_DATES), opened)
            save(frame, "missing-photo-and-date")
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h1400dp-mdpi")
    fun savedNotesRemindersMoodAndCompanionLeadToTheirDestinations() {
        val notes = listOf(Note("Weekend plans", "Try the new bakery", "idea", "note-1", now, now))
        val reminders = listOf(Reminder(
            title = "Book the anniversary dinner", scheduledDate = "2026-02-10",
            id = "reminder-1", createdAt = now, updatedAt = now,
        ))
        val moods = listOf(MoodEntry(
            moodValue = "calm", moodEmoji = "😌", note = "A quiet morning", profileId = "owner",
            entryDate = today.toString(), id = "mood-1", createdAt = now, updatedAt = now,
        ))
        val opened = mutableListOf<String>()
        render(
            profile("Alexandria", "owner"), profile("Sam", "partner"), relationship(), dark = true,
            notes = notes, reminders = reminders, moods = moods, yuki = YukiPresence("Yuki", "Content and relaxed", 7),
            opened = opened,
        ) { frame, _, nodes ->
            assertTrue(nodes.any { it.hasText("Weekend plans") })
            assertTrue(nodes.any { it.hasText("Feb 10, 2026 · Book the anniversary dinner") })
            assertTrue(nodes.any { it.hasText("A quiet morning") })
            assertTrue(nodes.any { it.hasText("Content and relaxed") })
            for (label in listOf("Keep your first memory", "Notes", "Reminders", "Calm", "Yuki")) {
                nodes.single { it.hasText(label) && it.hasClick() }.click()
            }
            assertEquals(
                listOf(RoutePath.APP_MEMORIES_ADD, RoutePath.APP_NOTES, RoutePath.APP_REMINDERS, RoutePath.APP_MOOD, RoutePath.APP_YUKI),
                opened,
            )
            save(frame, "story-and-actions-dark")
        }
    }

    private fun render(
        owner: Profile?,
        partner: Profile?,
        relationship: CoupleRelationship?,
        memories: List<Memory> = emptyList(),
        notes: List<Note> = emptyList(),
        reminders: List<Reminder> = emptyList(),
        moods: List<MoodEntry> = emptyList(),
        yuki: YukiPresence? = null,
        dark: Boolean = false,
        fontScale: Float = 1f,
        opened: MutableList<String>,
        check: (Bitmap, View, List<SemanticsNode>) -> Unit,
    ) {
        Robolectric.buildActivity(ComponentActivity::class.java).use { controller ->
            val activity = controller.setup().get()
            activity.enableEdgeToEdge()
            activity.setContent {
                TwoHeartsTheme(darkTheme = dark, textSizeScale = fontScale, reduceMotion = true) {
                    AppShell(
                        RoutePath.APP_HOME, {}, {}, canNavigateBack = false,
                        keyboardInsets = WindowInsets(bottom = 0),
                    ) {
                        HomeContent(
                            owner, partner, relationship,
                            notes = notes, reminders = reminders, memories = memories,
                            moods = moods, dates = if (relationship == null) emptyList() else listOf(savedDate()),
                            onNavigate = { opened += it }, today = today, yukiPresence = yuki,
                        )
                    }
                }
            }
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(64))
            val metrics = activity.resources.displayMetrics
            val view = activity.window.decorView
            view.measure(
                View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.EXACTLY),
            )
            view.layout(0, 0, metrics.widthPixels, metrics.heightPixels)
            val frame = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            try {
                view.draw(Canvas(frame))
                val semantics = checkNotNull(composeRoot(view)).semanticsOwner
                val merged = semantics.getAllSemanticsNodes(mergingEnabled = true)
                val raw = semantics.getAllSemanticsNodes(mergingEnabled = false)
                val mergedIds = merged.mapTo(mutableSetOf()) { it.id }
                check(frame, view, merged + raw.filter { it.id !in mergedIds })
            } finally {
                frame.recycle()
            }
        }
    }

    private fun assertInViewport(bounds: Rect, view: View) {
        assertTrue("Visible text has bounds $bounds", bounds.width > 0f && bounds.height > 0f)
        assertTrue("Text fits in the viewport", bounds.left >= 0f && bounds.right <= view.width)
        assertTrue("Text is above the bottom navigation", bounds.top >= 0f && bounds.bottom < view.height - 60f)
    }

    private fun assertPortraitColors(frame: Bitmap, ownerRed: Boolean, partnerBlue: Boolean) {
        // The saturated photo backdrop is absent from the app palette in both themes.
        var redPixels = 0
        var bluePixels = 0
        for (y in (frame.height / 12) until (frame.height * 2 / 5)) {
            for (x in 0 until frame.width) {
                val pixel = frame.getPixel(x, y)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                if (x < frame.width / 2 && red in 177..183 && green in 85..91 && blue in 102..108) redPixels++
                if (x > frame.width / 2 && red in 55..61 && green in 100..106 && blue in 110..116) bluePixels++
            }
        }
        assertEquals(ownerRed, redPixels > 600)
        assertEquals(partnerBlue, bluePixels > 600)
    }

    private fun createPhoto(background: Int, skin: Int): String = runBlocking {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val brush = Paint(Paint.ANTI_ALIAS_FLAG)
        val canvas = Canvas(bitmap)
        canvas.drawColor(background)
        brush.color = Color.rgb(42, 30, 38)
        canvas.drawOval(48f, 148f, 208f, 312f, brush)
        canvas.drawOval(78f, 44f, 178f, 150f, brush)
        brush.color = skin
        canvas.drawOval(87f, 76f, 169f, 164f, brush)
        brush.color = Color.rgb(42, 30, 38)
        canvas.drawCircle(110f, 116f, 3f, brush)
        canvas.drawCircle(145f, 116f, 3f, brush)
        brush.color = Color.rgb(130, 54, 65)
        canvas.drawOval(119f, 136f, 137f, 141f, brush)
        val asset = try {
            checkNotNull(storage.storeBitmap(bitmap))
        } finally {
            bitmap.recycle()
        }
        val edge = MediaThumbnails.bucket(672, 672)
        for (size in listOf(512, edge)) {
            checkNotNull(MediaThumbnails.load(asset.filePath, "", size, normalizeExif = true))
        }
        asset.id
    }

    private fun profile(name: String, role: String, photoRef: String? = null) =
        Profile(name = name, role = role, photoRef = photoRef, id = role, createdAt = now, updatedAt = now)

    private fun relationship() =
        CoupleRelationship(ownerId = "owner", partnerId = "partner", startDate = "2023-02-14",
            id = "relationship", createdAt = now, updatedAt = now)

    private fun savedDate() =
        ImportantDate(title = "The day we met", date = "2021-01-20", recurrence = "yearly",
            id = "date", createdAt = now, updatedAt = now)

    private fun save(frame: Bitmap, state: String) {
        val folder = File("build/transformation-9").apply { mkdirs() }
        File(folder, "$state.png").outputStream().use { frame.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun composeRoot(view: View): ViewRootForTest? {
        if (view is ViewRootForTest) return view
        if (view is ViewGroup) {
            repeat(view.childCount) { index -> composeRoot(view.getChildAt(index))?.let { return it } }
        }
        return null
    }

    private fun SemanticsNode.hasText(label: String) =
        config.getOrNull(SemanticsProperties.Text)?.any { it.text == label } == true

    private fun SemanticsNode.hasDescription(label: String) =
        config.getOrNull(SemanticsProperties.ContentDescription)?.contains(label) == true

    private fun SemanticsNode.hasClick() = config.getOrNull(SemanticsActions.OnClick) != null

    private fun SemanticsNode.click() {
        val action = checkNotNull(config.getOrNull(SemanticsActions.OnClick)?.action)
        val density = org.robolectric.RuntimeEnvironment.getApplication().resources.displayMetrics.density
        assertTrue("Touch target bounds are $boundsInRoot", boundsInRoot.width >= 48f * density && boundsInRoot.height >= 48f * density)
        assertTrue(action())
    }
}
