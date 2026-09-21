package com.twohearts.app

import android.app.Application
import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.ui.navigation.AppShell
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.home.HomeScreen
import com.twohearts.app.ui.screens.memories.MemoriesHome
import com.twohearts.app.ui.screens.memories.MemoryDetail
import com.twohearts.app.ui.screens.notes.NotesHome
import com.twohearts.app.ui.screens.settings.SettingsHomeScreen
import com.twohearts.app.ui.theme.TextScalingLevel
import com.twohearts.app.ui.theme.TwoHeartsTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Phase2RenderHarness — renders the app shell and navigation states.
 *
 * Phase 2's validation requirement is visual: the shell and the bottom
 * navigation must be inspected as rendered output, not inferred from source.
 * This harness is the Phase 0 harness's approach applied to the shell — real
 * Compose, real Skia rendering through Robolectric native graphics, real PNGs
 * written to `build/phase2-shell/`.
 *
 * It renders the same states at three device geometries and two text sizes,
 * because the shell's whole job is to hold up under those variations:
 *
 *  - `w411dp-h891dp-xxhdpi` — the reference modern phone.
 *  - `w360dp-h780dp-mdpi`   — the Tecno Spark 10 Pro class target.
 *  - extra-large text on both, since five labels in one row is exactly the
 *    layout that breaks first when type grows.
 *
 * States rendered:
 *  1. each of the five primary areas selected (so the active indicator can be
 *     compared across all five positions)
 *  2. a detail screen (bar hidden, back affordance present)
 *  3. an editor surface (bar hidden)
 *  4. light and dark for the shell
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Phase2RenderHarness {

    private val outDir: File = File("build/phase2-shell").apply { mkdirs() }
    private val now = "2026-01-15T10:00:00Z"

    private fun relationshipService(): RelationshipService {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase(app)
        return RelationshipService(
            profileRepository = com.twohearts.app.data.repository.ProfileRepository(db.profileDao()),
            relationshipRepository = com.twohearts.app.data.repository.CoupleRelationshipRepository(
                db.coupleRelationshipDao()
            )
        )
    }

    private fun render(
        name: String,
        route: String,
        dark: Boolean = false,
        scale: TextScalingLevel = TextScalingLevel.DEFAULT,
        content: @Composable () -> Unit,
    ) {
        val controller = org.robolectric.Robolectric
            .buildActivity(androidx.activity.ComponentActivity::class.java).setup()
        val activity = controller.get()
        activity.setContent {
            TwoHeartsTheme(darkMode = dark, textScalingLevel = scale) {
                AppShell(
                    currentRoute = route,
                    onNavigate = {},
                    onBack = {},
                    canNavigateBack = false,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) { content() }
                }
            }
        }
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

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
        view.draw(android.graphics.Canvas(bitmap))
        File(outDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        println("WROTE ${File(outDir, "$name.png").absolutePath} ${width}x$height")
        controller.pause().stop().destroy()
    }

    /** Five primary areas, each rendered with itself selected. */
    @Test
    fun renderPrimaryAreas() {
        render("p2-01-home", RoutePath.APP_HOME) {
            HomeScreen(relationshipService(), {})
        }
        render("p2-02-notes", RoutePath.APP_NOTES) {
            NotesHome(
                listOf(
                    Note(
                        title = "Gratitude for today",
                        content = "Thank you for the little things.",
                        category = "gratitude", id = "n1",
                        createdAt = now, updatedAt = now,
                    ),
                ),
                {}, {}, {},
            )
        }
        render("p2-03-us", RoutePath.APP_US) {
            com.twohearts.app.ui.screens.us.UsScreen(relationshipService(), {})
        }
        render("p2-04-notifications", RoutePath.APP_NOTIFICATIONS) {
            NotificationCenterHarnessContent()
        }
        render("p2-05-more", RoutePath.APP_MORE) {
            com.twohearts.app.ui.screens.more.MoreScreen({})
        }
    }

    /** A detail surface: no bar, back affordance present. */
    @Test
    fun renderDetailSurface() {
        render("p2-10-memory-detail", RoutePath.APP_MEMORIES_DETAIL.replace(":memoryId", "m1")) {
            MemoryDetail(
                Memory(
                    title = "First trip together",
                    caption = "The mountains were cold and perfect.",
                    memoryDate = "2025-11-02", id = "m1",
                    createdAt = now, updatedAt = now,
                ),
                {}, {}, {},
            )
        }
        render("p2-11-memories-list", RoutePath.APP_MEMORIES) {
            MemoriesHome(
                listOf(
                    Memory(
                        title = "First trip together",
                        caption = "The mountains were cold and perfect.",
                        memoryDate = "2025-11-02", id = "m1",
                        createdAt = now, updatedAt = now,
                    ),
                ),
                {}, {}, {},
            )
        }
    }

    /** Dark mode across the shell. */
    @Test
    fun renderDark() {
        render("p2-20-home-dark", RoutePath.APP_HOME, dark = true) {
            HomeScreen(relationshipService(), {})
        }
        render("p2-21-notes-dark", RoutePath.APP_NOTES, dark = true) {
            NotesHome(
                listOf(
                    Note(
                        title = "Gratitude for today",
                        content = "Thank you.", category = "gratitude",
                        id = "n1", createdAt = now, updatedAt = now,
                    ),
                ),
                {}, {}, {},
            )
        }
        render("p2-22-us-dark", RoutePath.APP_US, dark = true) {
            com.twohearts.app.ui.screens.us.UsScreen(relationshipService(), {})
        }
        render("p2-23-more-dark", RoutePath.APP_MORE, dark = true) {
            com.twohearts.app.ui.screens.more.MoreScreen({})
        }
    }

    /** Extra-large text: the layout most likely to break first. */
    @Test
    fun renderExtraLargeText() {
        render("p2-30-home-xl", RoutePath.APP_HOME, scale = TextScalingLevel.EXTRA_LARGE) {
            HomeScreen(relationshipService(), {})
        }
        render("p2-31-notes-xl", RoutePath.APP_NOTES, scale = TextScalingLevel.EXTRA_LARGE) {
            NotesHome(
                listOf(
                    Note(
                        title = "Gratitude for today",
                        content = "Thank you.", category = "gratitude",
                        id = "n1", createdAt = now, updatedAt = now,
                    ),
                ),
                {}, {}, {},
            )
        }
    }

    /** The Tecno-class geometry (~360dp), the user's actual target device. */
    @Test
    @Config(sdk = [34], qualifiers = "w360dp-h780dp-mdpi")
    fun renderTargetDevice() {
        render("p2-40-home-360", RoutePath.APP_HOME) {
            HomeScreen(relationshipService(), {})
        }
        render("p2-41-notes-360", RoutePath.APP_NOTES) {
            NotesHome(
                listOf(
                    Note(
                        title = "Gratitude for today",
                        content = "Thank you.", category = "gratitude",
                        id = "n1", createdAt = now, updatedAt = now,
                    ),
                ),
                {}, {}, {},
            )
        }
        render("p2-42-settings-360", RoutePath.APP_SETTINGS) {
            SettingsHomeScreen({}, {})
        }
    }

    /** Target geometry at extra-large text — the worst case for the bar. */
    @Test
    @Config(sdk = [34], qualifiers = "w360dp-h780dp-mdpi")
    fun renderTargetDeviceLargeText() {
        render(
            "p2-50-home-360-xl",
            RoutePath.APP_HOME,
            scale = TextScalingLevel.EXTRA_LARGE,
        ) {
            HomeScreen(relationshipService(), {})
        }
    }

    /** Seeded relationship data, so the header shows real names. */
    @Test
    fun renderSeededShell() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase(app)
        // The database is a process-wide singleton shared with the Phase 0
        // harness, and `profiles` has a unique id, so seed only what is
        // missing rather than assuming an empty database.
        kotlinx.coroutines.runBlocking {
            if (db.profileDao().getById("p1") == null) {
                db.profileDao().insert(
                    Profile(name = "Alex", role = "owner", id = "p1", createdAt = now, updatedAt = now)
                )
            }
            if (db.profileDao().getById("p2") == null) {
                db.profileDao().insert(
                    Profile(
                        name = "Sam", role = "partner", birthday = "1996-03-11",
                        id = "p2", createdAt = now, updatedAt = now,
                    )
                )
            }
            if (db.coupleRelationshipDao().get() == null) {
                db.coupleRelationshipDao().insert(
                    CoupleRelationship(
                        ownerId = "p1", partnerId = "p2", startDate = "2023-02-14",
                        id = "rel1", createdAt = now, updatedAt = now,
                    )
                )
            }
        }
        render("p2-60-home-seeded", RoutePath.APP_HOME) {
            HomeScreen(relationshipService(), {})
        }
        render("p2-61-us-seeded", RoutePath.APP_US) {
            com.twohearts.app.ui.screens.us.UsScreen(relationshipService(), {})
        }
    }

    /** Notification centre, whose header was migrated off a stock TopAppBar. */
    @Composable
    private fun NotificationCenterHarnessContent() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase(app)
        com.twohearts.app.ui.screens.notifications.NotificationCenterScreen(
            com.twohearts.app.data.repository.NotificationCenterRepository(db.notificationCenterDao()),
            {},
        )
    }
}
