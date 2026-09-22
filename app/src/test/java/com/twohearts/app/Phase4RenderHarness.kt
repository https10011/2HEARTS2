package com.twohearts.app

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.repository.ImportantDateRepository
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.ui.screens.home.HomeContent
import com.twohearts.app.ui.screens.home.YukiPresence
import com.twohearts.app.ui.theme.TextScalingLevel
import com.twohearts.app.ui.theme.TwoHeartsTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

/**
 * Phase4RenderHarness — renders the real Home across representative states.
 *
 * ## Why a new harness rather than reusing Phase 0's
 *
 * Phase 0's Home renders were driven by a shared process-wide database, and
 * the seeded and unseeded images came out byte-identical — which is what
 * exposed the original defect. Phase 4 states cannot share that database:
 * "fresh" and "populated" have to be genuinely different, and a leftover row
 * from another harness would turn a populated render into a false pass.
 *
 * So this harness drives [HomeContent] — Home's pure view — directly, passing
 * entities it constructs. That is the closest possible analogue of what the
 * database delivers, with none of the cross-test contamination, and it means a
 * render can only look populated when the values were explicitly supplied.
 *
 * Database-backed confidence is not lost: [Phase4HomeTest] writes and reads
 * through the genuine repositories, so the two together cover both "the view
 * renders this state" and "this state is what the database actually yields".
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Phase4RenderHarness {

    private val outDir: File = File("build/phase4-home").apply { mkdirs() }

    private val now = "2026-01-15T10:00:00Z"
    private val today: LocalDate = LocalDate.parse(Phase4HomeSeed.TODAY)

    private val owner = profile("Alexandria", "owner")
    private val partner = profile("Sam", "partner")
    private val relationship = com.twohearts.app.data.entity.CoupleRelationship(
        ownerId = "o", partnerId = "p", startDate = "2023-02-14",
        id = "rel", createdAt = now, updatedAt = now,
    )

    private val notes = listOf(
        note("A letter for your birthday", "love-letter"),
        note("Weekend ideas", "idea"),
    )
    private val reminders = listOf(
        reminder("Book the anniversary dinner", "2026-02-10"),
        reminder("Renew the museum pass", "2026-04-02"),
    )
    private val memories = listOf(
        memory("Sunday morning, pancakes", "No plans, nowhere to be.", "2026-01-04"),
        memory("The night we got caught in the rain", "We ran home laughing.", "2025-11-02"),
    )
    private val date = com.twohearts.app.data.entity.ImportantDate(
        title = "The day we met", date = "2021-01-20", recurrence = "yearly",
        id = "d1", createdAt = now, updatedAt = now,
    )
    private val mood = com.twohearts.app.data.entity.MoodEntry(
        moodValue = "happy", moodEmoji = "\uD83D\uDE0A", note = "Quiet, ordinary, good.",
        profileId = "o", entryDate = Phase4HomeSeed.TODAY, id = "mo1", createdAt = now, updatedAt = now,
    )

    private val yuki = YukiPresence(name = "Yuki", statusLine = "Content and relaxed", level = 7)

    /** Render [name] at the harness geometry. */
    private fun render(
        name: String,
        dark: Boolean = false,
        scale: TextScalingLevel = TextScalingLevel.DEFAULT,
        content: @Composable () -> Unit,
    ) {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = controller.get()
        activity.setContent {
            TwoHeartsTheme(darkMode = dark, textScalingLevel = scale) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { content() }
            }
        }
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val metrics = activity.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val view = activity.window.decorView
        view.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        File(outDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        println("WROTE ${File(outDir, "$name.png").absolutePath} ${width}x$height")
        controller.pause().stop().destroy()
    }

    /** Home in its fully populated state — the everyday case. */
    @Test
    fun renderPopulated() {
        render("p4-01-populated") {
            FullHome(
                owner = owner, partner = partner, relationship = relationship,
                notes = notes, reminders = reminders, memories = memories, mood = mood,
                dates = listOf(date), yuki = yuki,
            )
        }
        // Dark mode of the same state: the hero band, tone cards and the
        // decorative accent all have to survive the theme flip.
        render("p4-02-populated-dark", dark = true) {
            FullHome(
                owner = owner, partner = partner, relationship = relationship,
                notes = notes, reminders = reminders, memories = memories, mood = mood,
                dates = listOf(date), yuki = yuki,
            )
        }
    }

    /** Home as it looks the moment onboarding finishes — names only. */
    @Test
    fun renderFresh() {
        render("p4-03-fresh") {
            FullHome(
                owner = owner, partner = partner, relationship = relationship,
                notes = emptyList(), reminders = emptyList(), memories = emptyList(),
                mood = null, dates = emptyList(), yuki = null,
            )
        }
    }

    /** Home with nothing but a profile — the sparsest honest state. */
    @Test
    fun renderMinimal() {
        render("p4-04-minimal") {
            HomeContent(
                owner = owner, partner = null, relationship = null,
                notes = emptyList(), reminders = emptyList(), memories = emptyList(),
                moods = emptyList(), dates = emptyList(),
                onNavigate = {}, today = today, yukiPresence = null,
            )
        }
    }

    /** Each section populated in isolation, so a defect cannot hide behind others. */
    @Test
    fun renderSectionsInIsolation() {
        render("p4-05-upcoming-only") {
            HomeContent(
                owner = owner, partner = partner, relationship = relationship,
                notes = emptyList(), reminders = emptyList(), memories = emptyList(),
                moods = emptyList(), dates = listOf(date),
                onNavigate = {}, today = today, yukiPresence = null,
            )
        }
        render("p4-06-notes-reminders-only") {
            HomeContent(
                owner = owner, partner = partner, relationship = relationship,
                notes = notes, reminders = reminders, memories = emptyList(),
                moods = emptyList(), dates = emptyList(),
                onNavigate = {}, today = today, yukiPresence = null,
            )
        }
        render("p4-07-memory-only") {
            HomeContent(
                owner = owner, partner = partner, relationship = relationship,
                notes = emptyList(), reminders = emptyList(), memories = memories,
                moods = emptyList(), dates = emptyList(),
                onNavigate = {}, today = today, yukiPresence = null,
            )
        }
        render("p4-08-mood-only") {
            HomeContent(
                owner = owner, partner = partner, relationship = relationship,
                notes = emptyList(), reminders = emptyList(), memories = emptyList(),
                moods = listOf(mood), dates = emptyList(),
                onNavigate = {}, today = today, yukiPresence = null,
            )
        }
    }

    /** Long names and long content — where clipping and overflow show up. */
    @Test
    fun renderLongContent() {
        render("p4-09-long-names") {
            FullHome(
                owner = profile("Alexandria-Cassandra Mwangi-Osei", "owner"),
                partner = profile("Bartholomew Fitzgerald-Wellington III", "partner"),
                relationship = relationship,
                notes = listOf(
                    note(
                        "Everything I have been meaning to say but kept putting off until the right evening",
                        "love-letter",
                    )
                ),
                reminders = listOf(
                    reminder("Book the anniversary dinner at the little place by the river", "2026-02-10")
                ),
                memories = listOf(
                    memory(
                        "The long weekend we spent doing absolutely nothing except walking and talking",
                        "We kept saying we would plan it properly next time, and then we never did, and it was perfect anyway.",
                        "2026-01-04",
                    )
                ),
                mood = mood.copy(note = "Quiet, ordinary, and exactly what I needed after a very long week."),
                dates = listOf(
                    date.copy(title = "The anniversary of the day we first met at the bookshop")
                ),
                yuki = yuki,
            )
        }
    }

    /** Extra large text — the layout has to recompose, not shrink. */
    @Test
    fun renderExtraLargeText() {
        render("p4-10-extra-large", scale = TextScalingLevel.EXTRA_LARGE) {
            FullHome(
                owner = owner, partner = partner, relationship = relationship,
                notes = notes, reminders = reminders, memories = memories, mood = mood,
                dates = listOf(date), yuki = yuki,
            )
        }
    }

    @Composable
    private fun FullHome(
        owner: com.twohearts.app.data.entity.Profile?,
        partner: com.twohearts.app.data.entity.Profile?,
        relationship: com.twohearts.app.data.entity.CoupleRelationship?,
        notes: List<com.twohearts.app.data.entity.Note>,
        reminders: List<com.twohearts.app.data.entity.Reminder>,
        memories: List<com.twohearts.app.data.entity.Memory>,
        mood: com.twohearts.app.data.entity.MoodEntry?,
        dates: List<com.twohearts.app.data.entity.ImportantDate>,
        yuki: YukiPresence?,
    ) {
        HomeContent(
            owner = owner, partner = partner, relationship = relationship,
            notes = notes, reminders = reminders, memories = memories,
            moods = mood?.let { listOf(it) } ?: emptyList(),
            dates = dates, onNavigate = {}, today = today, yukiPresence = yuki,
        )
    }

    /**
     * Also renders Home from the *real database* once, so the harness proves
     * the repository path as well as the pure view.
     */
    @Test
    fun renderFromRealDatabase() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val db = TwoHeartsDatabase.getDatabase(app)
        // Room refuses main-thread queries; the harness test method itself runs
        // on the main looper under Robolectric, so the reads hop to IO.
        val rows = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            db.clearAllTables()
            Phase4HomeSeed.seedPopulated(app)
            DbRows(
                owner = db.profileDao().getById(Phase4HomeSeed.OWNER_ID),
                partner = db.profileDao().getById(Phase4HomeSeed.PARTNER_ID),
                relationship = db.coupleRelationshipDao().getById(Phase4HomeSeed.RELATIONSHIP_ID),
                notes = NoteRepository(db.noteDao()).observeAll().first(),
                reminders = ReminderRepository(db.reminderDao()).observeAll().first(),
                memories = MemoryRepository(db.memoryDao(), db.memoryMediaDao()).observeAll().first(),
                moods = MoodEntryRepository(db.moodEntryDao()).observeAll().first(),
                dates = ImportantDateRepository(db.importantDateDao()).observeAll().first(),
            )
        }

        render("p4-11-from-database") {
            HomeContent(
                owner = rows.owner, partner = rows.partner, relationship = rows.relationship,
                notes = rows.notes, reminders = rows.reminders, memories = rows.memories,
                moods = rows.moods, dates = rows.dates,
                onNavigate = {}, today = today, yukiPresence = yuki,
            )
        }
    }

    private data class DbRows(
        val owner: com.twohearts.app.data.entity.Profile?,
        val partner: com.twohearts.app.data.entity.Profile?,
        val relationship: com.twohearts.app.data.entity.CoupleRelationship?,
        val notes: List<com.twohearts.app.data.entity.Note>,
        val reminders: List<com.twohearts.app.data.entity.Reminder>,
        val memories: List<com.twohearts.app.data.entity.Memory>,
        val moods: List<com.twohearts.app.data.entity.MoodEntry>,
        val dates: List<com.twohearts.app.data.entity.ImportantDate>,
    )

    private fun profile(name: String, role: String) =
        com.twohearts.app.data.entity.Profile(
            name = name, role = role, id = "p-$role", createdAt = now, updatedAt = now
        )

    private fun note(title: String, category: String) =
        com.twohearts.app.data.entity.Note(
            title = title, content = "", category = category, id = "n-$title",
            createdAt = now, updatedAt = now,
        )

    private fun reminder(title: String, date: String) =
        com.twohearts.app.data.entity.Reminder(
            title = title, scheduledDate = date, id = "r-$title", createdAt = now, updatedAt = now
        )

    private fun memory(title: String, caption: String, date: String) =
        com.twohearts.app.data.entity.Memory(
            title = title, caption = caption, memoryDate = date, id = "m-$title",
            createdAt = now, updatedAt = now,
        )
}

/**
 * The 360dp target geometry — where the composition is most likely to fail.
 *
 * A separate class because Robolectric's display qualifiers are per-class.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h780dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Phase4NarrowRenderHarness {

    private val outDir: File = File("build/phase4-home").apply { mkdirs() }
    private val now = "2026-01-15T10:00:00Z"
    private val today: LocalDate = LocalDate.parse(Phase4HomeSeed.TODAY)

    private fun render(
        name: String,
        dark: Boolean = false,
        scale: TextScalingLevel = TextScalingLevel.DEFAULT,
        content: @Composable () -> Unit,
    ) {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = controller.get()
        activity.setContent {
            TwoHeartsTheme(darkMode = dark, textScalingLevel = scale) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { content() }
            }
        }
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val metrics = activity.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val view = activity.window.decorView
        view.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        File(outDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        println("WROTE ${File(outDir, "$name.png").absolutePath} ${width}x$height")
        controller.pause().stop().destroy()
    }

    @Test
    fun renderNarrow() {
        val owner = com.twohearts.app.data.entity.Profile(
            name = "Alexandria", role = "owner", id = "o", createdAt = now, updatedAt = now
        )
        val partner = com.twohearts.app.data.entity.Profile(
            name = "Sam", role = "partner", id = "p", createdAt = now, updatedAt = now
        )
        val relationship = com.twohearts.app.data.entity.CoupleRelationship(
            ownerId = "o", partnerId = "p", startDate = "2023-02-14",
            id = "rel", createdAt = now, updatedAt = now,
        )
        val full = listOf(
            com.twohearts.app.data.entity.Note(
                title = "Weekend ideas", content = "", category = "idea",
                id = "n1", createdAt = now, updatedAt = now,
            )
        )
        val reminders = listOf(
            com.twohearts.app.data.entity.Reminder(
                title = "Book the anniversary dinner", scheduledDate = "2026-02-10",
                id = "r1", createdAt = now, updatedAt = now,
            )
        )
        val memories = listOf(
            com.twohearts.app.data.entity.Memory(
                title = "Sunday morning, pancakes", caption = "No plans, nowhere to be.",
                memoryDate = "2026-01-04", id = "m1", createdAt = now, updatedAt = now,
            )
        )
        val dates = listOf(
            com.twohearts.app.data.entity.ImportantDate(
                title = "The day we met", date = "2021-01-20", recurrence = "yearly",
                id = "d1", createdAt = now, updatedAt = now,
            )
        )
        val mood = com.twohearts.app.data.entity.MoodEntry(
            moodValue = "happy", moodEmoji = "\uD83D\uDE0A", note = "Ordinary, good.",
            profileId = "o", entryDate = Phase4HomeSeed.TODAY, id = "mo1", createdAt = now, updatedAt = now,
        )

        render("p4-50-narrow-populated") {
            HomeContent(
                owner = owner, partner = partner, relationship = relationship,
                notes = full, reminders = reminders, memories = memories,
                moods = listOf(mood), dates = dates, onNavigate = {}, today = today,
            )
        }
        render("p4-51-narrow-extra-large", scale = TextScalingLevel.EXTRA_LARGE) {
            HomeContent(
                owner = owner, partner = partner, relationship = relationship,
                notes = full, reminders = reminders, memories = memories,
                moods = listOf(mood), dates = dates, onNavigate = {}, today = today,
            )
        }
        render("p4-52-narrow-fresh") {
            HomeContent(
                owner = owner, partner = partner, relationship = relationship,
                notes = emptyList(), reminders = emptyList(), memories = emptyList(),
                moods = emptyList(), dates = emptyList(), onNavigate = {}, today = today,
            )
        }
    }
}
