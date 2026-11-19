package com.twohearts.app

import android.app.Application
import androidx.activity.compose.setContent
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.ui.components.CardTone
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.ThSurfaceCard
import com.twohearts.app.ui.screens.about.AboutScreen
import com.twohearts.app.ui.screens.importantdates.ImportantDatesScreen
import com.twohearts.app.ui.screens.memories.MemoriesHome
import com.twohearts.app.ui.screens.memories.MemoryDetail
import com.twohearts.app.ui.screens.memories.AddMemory
import com.twohearts.app.ui.screens.mood.MoodHistory
import com.twohearts.app.ui.screens.mood.MoodHome
import com.twohearts.app.ui.screens.mood.MoodEntryScreen
import com.twohearts.app.ui.screens.notes.NoteCategory
import com.twohearts.app.ui.screens.notes.NoteDetail
import com.twohearts.app.ui.screens.notes.NoteEditor
import com.twohearts.app.ui.screens.notes.NotesHome
import com.twohearts.app.ui.screens.notifications.NotificationCenterScreen
import com.twohearts.app.ui.screens.period.LogPeriod
import com.twohearts.app.ui.screens.period.PeriodCalendarScreen
import com.twohearts.app.ui.screens.period.PeriodHistoryScreen
import com.twohearts.app.ui.screens.period.PeriodHome
import com.twohearts.app.ui.screens.period.PeriodSettingsScreen
import com.twohearts.app.ui.screens.places.CreatePlace
import com.twohearts.app.ui.screens.places.PlaceDetail
import com.twohearts.app.ui.screens.places.PlacesHome
import com.twohearts.app.ui.screens.reminders.CreateReminder
import com.twohearts.app.ui.screens.reminders.ReminderDetail
import com.twohearts.app.ui.screens.reminders.RemindersHome
import com.twohearts.app.ui.screens.settings.AppearanceSettingsScreen
import com.twohearts.app.ui.screens.settings.NotificationSettingsScreen
import com.twohearts.app.ui.screens.settings.ProfileSettingsScreen
import com.twohearts.app.ui.screens.settings.RelationshipSettingsScreen
import com.twohearts.app.ui.screens.settings.SettingsHomeScreen
import com.twohearts.app.ui.screens.timeline.AddEvent
import com.twohearts.app.ui.screens.timeline.EventDetail
import com.twohearts.app.ui.screens.timeline.TimelineHome
import com.twohearts.app.ui.screens.more.MoreScreen
import com.twohearts.app.ui.screens.yuki.YukiScreen
import com.twohearts.app.ui.screens.yuki.YukiViewModel
import com.twohearts.app.ui.theme.TwoHeartsTheme
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TextScalingLevel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Phase0RenderHarness {

    private val outDir: File = File("build/phase0-screens").apply { mkdirs() }
    private val now = "2026-01-15T10:00:00Z"

    @Test
    fun renderTransformationTypeAndColor() {
        render("transformation-3-light") { TransformationThemeReference() }
        render("transformation-3-dark", dark = true) { TransformationThemeReference() }
    }

    @Test
    @Config(qualifiers = "w360dp-h780dp-mdpi")
    fun renderTransformationTypeAtTwoHundredPercent() {
        render("transformation-3-light-200", textSizeScale = 2f) { TransformationThemeReference(2f) }
        render("transformation-3-dark-200", dark = true, textSizeScale = 2f) { TransformationThemeReference(2f) }
    }

    @Test
    @Config(qualifiers = "w274dp-h488dp-mdpi")
    fun renderPhase20EmptyNotesOnShortScreen() {
        render("p20-short-notes", scale = TextScalingLevel.LARGE) {
            com.twohearts.app.ui.navigation.AppShell(
                currentRoute = com.twohearts.app.ui.navigation.RoutePath.APP_NOTES,
                onNavigate = {}, onBack = {}, canNavigateBack = false,
            ) {
                NotesHome(emptyList(), onNoteClick = {}, onAddNote = {}, onBack = {})
            }
        }
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-mdpi")
    fun renderPhase18PrivacyFlows() {
        render("p18-lock", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.screens.security.AppLockGate(onUnlock = { false })
        }
        render("p18-lock-settings", scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.screens.security.SecuritySettingsScreen(
                isAppLockEnabled = true, onCreatePin = {}, onChangePin = { _, _ -> true },
                onDisableLock = { true }, onBack = {},
            )
        }
        render("p18-vault-add", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.screens.vault.AddVaultContent({}, { _, _, _ -> })
        }
        render("p18-vault-note", scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.screens.vault.VaultContentViewer(
                com.twohearts.app.data.entity.VaultItem(
                    "A vault layout fixture", "note", "This sample exercises the private note viewer with wrapped text.",
                    id = "vault-fixture", createdAt = now, updatedAt = now,
                ),
                onBack = {}, onDelete = {},
            )
        }
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-mdpi")
    fun renderPhase17BoundedPreview() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val source = File.createTempFile("preview", ".png", app.cacheDir)
        try {
            app.resources.openRawResource(com.twohearts.app.R.drawable.brand_logo).use { input ->
                source.outputStream().use { input.copyTo(it) }
            }
            val asset = com.twohearts.app.data.entity.MediaAsset(
                source.path, "image/png", source.length(), id = "test-preview",
                createdAt = now, updatedAt = now,
            )
            kotlinx.coroutines.runBlocking {
                requireNotNull(com.twohearts.app.services.media.MediaThumbnails.load(source.path, now, 512))
            }
            render("p17-cached-preview", dark = true) {
                androidx.compose.foundation.layout.Column(
                    Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                ) {
                    com.twohearts.app.ui.components.ThHeader("Media preview")
                    com.twohearts.app.ui.screens.memories.MemoryMediaFrame(
                        asset, Modifier.fillMaxWidth().height(200.dp),
                    )
                    com.twohearts.app.ui.screens.memories.MemoryMediaFrame(
                        null, Modifier.fillMaxWidth().height(140.dp),
                    )
                }
            }
        } finally {
            source.delete()
        }
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-mdpi")
    fun renderPhase16Accessibility() {
        render("p16-mood-narrow", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.screens.mood.MoodEntryScreen(
                initialMood = "grateful",
                onSave = { _, _, _ -> }, onBack = {},
            )
        }
        render("p16-navigation-narrow", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            val density = androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides
                    androidx.compose.ui.unit.Density(density.density, density.fontScale * 1.3f),
            ) {
                com.twohearts.app.ui.navigation.AppShell(
                    currentRoute = com.twohearts.app.ui.navigation.RoutePath.APP_US,
                    onNavigate = {}, onBack = {}, canNavigateBack = false,
                ) {
                    com.twohearts.app.ui.components.ThInput(
                        value = "", onValueChange = {}, label = "Important date",
                        placeholder = "yyyy-mm-dd", error = "Enter a valid date.",
                    )
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-mdpi")
    fun renderPhase15Feedback() {
        render("p15-saved", scale = TextScalingLevel.EXTRA_LARGE) {
            androidx.compose.runtime.CompositionLocalProvider(
                com.twohearts.app.ui.theme.LocalTwoHeartsMotion provides
                    com.twohearts.app.ui.theme.TwoHeartsMotion(reduced = true),
            ) {
                com.twohearts.app.ui.navigation.AppShell(
                    currentRoute = com.twohearts.app.ui.navigation.RoutePath.APP_HOME,
                    onNavigate = {}, onBack = {}, canNavigateBack = false,
                    feedback = androidx.compose.runtime.remember {
                        com.twohearts.app.ui.components.ToastState().apply { success("Memory saved.") }
                    },
                ) {}
            }
        }
        render("p15-error", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            androidx.compose.runtime.CompositionLocalProvider(
                com.twohearts.app.ui.theme.LocalTwoHeartsMotion provides
                    com.twohearts.app.ui.theme.TwoHeartsMotion(reduced = true),
            ) {
                com.twohearts.app.ui.components.ToastProvider(
                    state = androidx.compose.runtime.remember {
                        com.twohearts.app.ui.components.ToastState().apply { error("Couldn’t save this note.") }
                    },
                    bottomInset = 0.dp,
                ) {}
            }
        }
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-mdpi")
    fun renderPhase14SharedStates() {
        val missing = com.twohearts.app.ui.navigation.RecordLookup<String>()
        val failed = com.twohearts.app.ui.navigation.RecordLookup<String>()
        kotlinx.coroutines.runBlocking {
            missing.read { null }
            failed.read { error("Synthetic storage failure") }
        }
        render("p14-unavailable") {
            com.twohearts.app.ui.navigation.RecordLookupScreen("Memory", missing) {}
        }
        render("p14-failed-large", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.navigation.RecordLookupScreen("Timeline moment", failed) {}
        }
        render("p14-header-actions", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.components.ThTopBar(
                title = "Private note",
                onBack = {},
                role = com.twohearts.app.ui.navigation.ShellSurfaces.Role.DETAIL,
                actions = {
                    androidx.compose.material3.IconButton({}) {
                        androidx.compose.material3.Icon(com.twohearts.app.ui.components.ThIcons.Edit, "Edit")
                    }
                    androidx.compose.material3.IconButton({}) {
                        androidx.compose.material3.Icon(com.twohearts.app.ui.components.ThIcons.Trash, "Delete")
                    }
                },
            )
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h780dp-mdpi")
    fun renderPhase13Yuki() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val game = com.twohearts.app.data.game.YukiState.createDefault()
        val states = listOf(
            Triple("p13-room-idle", com.twohearts.app.data.game.YukiActivity.IDLE, com.twohearts.app.ui.screens.yuki.YukiPose.SIT),
            Triple("p13-room-eating", com.twohearts.app.data.game.YukiActivity.EATING, com.twohearts.app.ui.screens.yuki.YukiPose.EAT),
            Triple("p13-room-sleeping", com.twohearts.app.data.game.YukiActivity.SLEEPING, com.twohearts.app.ui.screens.yuki.YukiPose.SLEEP),
            Triple("p13-room-playing", com.twohearts.app.data.game.YukiActivity.PLAYING, com.twohearts.app.ui.screens.yuki.YukiPose.PAW),
        )
        states.forEachIndexed { index, (name, activity, pose) ->
            requireNotNull(com.twohearts.app.ui.screens.yuki.YukiArt.load(app, pose))
            render(name, dark = index >= 2, scale = if (index >= 2) TextScalingLevel.EXTRA_LARGE else TextScalingLevel.DEFAULT) {
                com.twohearts.app.ui.screens.yuki.YukiHub(
                    state = game.copy(activity = activity),
                    feedback = if (index == 1) "Yuki enjoys the meal." else null,
                    working = false, failed = false, animate = false,
                    onBack = {}, onAction = {}, onToy = {}, onAccessory = {},
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h780dp-mdpi")
    fun renderPhase12Utilities() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val initializer = com.twohearts.app.data.database.DatabaseInitializer(app)
        val db = kotlinx.coroutines.runBlocking { initializer.initialize() }
        val notes = com.twohearts.app.data.repository.NoteRepository(db.noteDao())
        val reminders = com.twohearts.app.data.repository.ReminderRepository(db.reminderDao())
        val profile = com.twohearts.app.data.entity.Profile(
            name = "Alexandra River Montgomery Bell", birthday = "1995-03-05", role = "owner",
            id = "profile-fixture", createdAt = now, updatedAt = now,
        )
        val services = com.twohearts.app.services.datamanagement.DataManagementService(
            app, initializer, com.twohearts.app.services.media.MediaStorage(app),
            com.twohearts.app.services.media.FileService(app),
        )
        render("p12-profile-large", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            ProfileSettingsScreen({}, profile = profile)
        }
        render("p12-settings-large", dark = true, scale = TextScalingLevel.EXTRA_LARGE) { SettingsHomeScreen({}, {}) }
        render("p12-import-export", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.screens.settings.ImportScreen(notes, reminders, {})
        }
        render("p12-storage", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.screens.settings.StorageSettingsScreen(services, {})
        }
        render("p12-search", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            com.twohearts.app.ui.screens.search.SearchScreen(
                com.twohearts.app.services.search.SearchEngine(), notes,
                com.twohearts.app.data.repository.MemoryRepository(db.memoryDao(), db.memoryMediaDao()),
                reminders, com.twohearts.app.data.repository.PlaceRepository(db.placeDao()),
                com.twohearts.app.data.repository.TimelineEventRepository(db.timelineEventDao()),
                onResultClick = { _, _ -> }, onBack = {},
            )
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h780dp-mdpi")
    fun renderPhase11Period() {
        val today = java.time.LocalDate.of(2026, 11, 18)
        val entry = com.twohearts.app.data.entity.PeriodEntry(
            startDate = "2026-11-04", endDate = "2026-11-08", profileId = "self",
            id = "test-period", createdAt = now, updatedAt = now,
        )
        render("p11-home-logged") { PeriodHome(entry, {}, {}, {}, {}, {}, today = today) }
        render("p11-calendar-large", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            PeriodCalendarScreen(listOf(entry), {}, cycleLength = 21, today = today)
        }
        render("p11-history-large", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            PeriodHistoryScreen(listOf(entry), {}, onEdit = {})
        }
        render("p11-edit-large", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            LogPeriod(
                entryId = entry.id, initialStartDate = entry.startDate, initialEndDate = entry.endDate.orEmpty(),
                onSave = { _, _, _, _ -> }, onBack = {}, onDelete = {},
            )
        }
        render("p11-settings-large", dark = true, scale = TextScalingLevel.EXTRA_LARGE) {
            PeriodSettingsScreen({}, initialCycle = 31, initialLength = 6)
        }
    }

    private fun appState(): AppStateService {
        val app = ApplicationProvider.getApplicationContext<Application>()
        return AppStateService(SettingsStorage.getInstance(app))
    }

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
        dark: Boolean = false,
        scale: TextScalingLevel = TextScalingLevel.DEFAULT,
        textSizeScale: Float = scale.scale,
        content: @Composable () -> Unit,
    ) {
        val controller = org.robolectric.Robolectric.buildActivity(androidx.activity.ComponentActivity::class.java).setup()
        val activity = controller.get()
        activity.setContent {
            TwoHeartsTheme(darkTheme = dark, textSizeScale = textSizeScale) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                ) { content() }
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
        File(outDir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("WROTE ${File(outDir, "$name.png").absolutePath} ${width}x$height")
        controller.pause().stop().destroy()
    }

    @Test
    fun renderAll() {
        render("01-home") { TestHomeScreen() }
        render("01b-home-dark", dark = true) { TestHomeScreen() }
        render("02-us") { TestUsScreen(relationshipService()) }
        render("03-more") { MoreScreen({}) }

        render("04-notes-empty") { NotesHome(emptyList(), {}, {}, {}) }
        render("05-notes-list") {
            NotesHome(
                listOf(
                    Note(title = "Gratitude for today", content = "Thank you for the little things.", category = "gratitude", id = "n1", createdAt = now, updatedAt = now),
                    Note(title = "Weekend ideas", content = "Beach, then that pasta place.", category = "idea", id = "n2", createdAt = now, updatedAt = now),
                    Note(title = "My love letter", content = "My dearest...", category = "love-letter", id = "n3", createdAt = now, updatedAt = now)
                ), {}, {}, {}
            )
        }
        render("06-note-detail") {
            NoteDetail(
                Note(title = "Gratitude for today", content = "Thank you for the little things you do every day. Coffee in the morning, the way you laugh at my jokes.", category = "gratitude", id = "n1", createdAt = now, updatedAt = now),
                {}, {}, {}
            )
        }
        render("07-note-editor") { NoteEditor(initialTitle = "", initialContent = "", initialCategory = NoteCategory.GENERAL, onSave = { _, _, _ -> }, onBack = {}) }

        render("08-memories-empty") { MemoriesHome(emptyList(), {}, {}, {}) }
        render("09-memories-list") {
            MemoriesHome(
                listOf(
                    Memory(title = "First trip together", caption = "The mountains were cold and perfect.", memoryDate = "2025-11-02", id = "m1", createdAt = now, updatedAt = now),
                    Memory(title = "Sunday morning", caption = "Pancakes and no plans.", memoryDate = "2026-01-04", id = "m2", createdAt = now, updatedAt = now)
                ), {}, {}, {}
            )
        }
        render("10-memory-detail") { MemoryDetail(Memory(title = "First trip together", caption = "The mountains were cold and perfect.", memoryDate = "2025-11-02", id = "m1", createdAt = now, updatedAt = now), {}, {}, {}) }
        render("11-add-memory") { AddMemory(onSave = { _, _, _, _ -> }, onBack = {}) }

        render("12-timeline-empty") { TimelineHome(emptyList(), {}, {}, {}) }
        render("13-timeline-list") {
            TimelineHome(
                listOf(
                    TimelineEvent(title = "We met", eventDate = "2024-05-04", description = "At a friend's dinner.", id = "e1", createdAt = now, updatedAt = now),
                    TimelineEvent(title = "Moved in", eventDate = "2025-08-01", description = "Boxes everywhere.", id = "e2", createdAt = now, updatedAt = now)
                ), {}, {}, {}
            )
        }
        render("14-event-detail") { EventDetail(TimelineEvent(title = "We met", eventDate = "2024-05-04", description = "At a friend's dinner.", id = "e1", createdAt = now, updatedAt = now), {}, {}, {}) }
        render("15-add-event") { AddEvent(onSave = { _, _, _ -> }, onBack = {}) }

        render("16-reminders-empty") { RemindersHome(emptyList(), {}, {}, {}) }
        render("17-reminders-list") {
            RemindersHome(
                listOf(
                    Reminder(title = "Call Mum", description = "Sunday afternoon", scheduledDate = "2026-01-18", scheduledTime = "15:00", id = "r1", createdAt = now, updatedAt = now),
                    Reminder(title = "Anniversary dinner", scheduledDate = "2026-02-14", scheduledTime = "19:30", id = "r2", createdAt = now, updatedAt = now)
                ), {}, {}, {}
            )
        }
        render("18-reminder-detail") { ReminderDetail(Reminder(title = "Call Mum", description = "Sunday afternoon", scheduledDate = "2026-01-18", scheduledTime = "15:00", id = "r1", createdAt = now, updatedAt = now), {}, {}, {}) }
        render("19-create-reminder") { CreateReminder(onSave = { _, _, _, _, _, _ -> }, onBack = {}) }

        render("20-important-dates") {
            ImportantDatesScreen(
                listOf(
                    ImportantDate(title = "Our anniversary", date = "2024-05-04", recurrence = "yearly", id = "d1", createdAt = now, updatedAt = now),
                    ImportantDate(title = "Her birthday", date = "1996-03-11", recurrence = "yearly", id = "d2", createdAt = now, updatedAt = now)
                ), onSaveDate = { _, _, _, _ -> }, onDeleteDate = {}, onBack = {}
            )
        }

        render("21-places-empty") { PlacesHome(emptyList(), {}, {}, {}) }
        render("22-places-list") {
            PlacesHome(
                listOf(
                    Place(name = "The little cafe", city = "Lisbon", notes = "Where we had our first date.", category = "date", id = "pl1", createdAt = now, updatedAt = now),
                    Place(name = "Mountain cabin", city = "Bavaria", category = "travel", id = "pl2", createdAt = now, updatedAt = now)
                ), {}, {}, {}
            )
        }
        render("23-place-detail") { PlaceDetail(Place(name = "The little cafe", city = "Lisbon", notes = "Where we had our first date.", category = "date", id = "pl1", createdAt = now, updatedAt = now), {}, {}, {}) }
        render("24-create-place") { CreatePlace(onSave = { _, _, _, _, _, _ -> }, onBack = {}) }

        render("25-mood-home") {
            MoodHome(
                todayMood = MoodEntry(moodValue = "happy", moodEmoji = "😊", profileId = "p1", entryDate = "2026-01-15", id = "mo1", createdAt = now, updatedAt = now),
                recentMoods = emptyList(), onAddMood = {}, onViewHistory = {}, onBack = {}
            )
        }
        render("26-mood-empty") { MoodHome(null, emptyList(), {}, {}, {}) }
        render("27-mood-entry") { MoodEntryScreen(onSave = { _, _, _ -> }, onBack = {}) }
        render("28-mood-history") {
            MoodHistory(
                listOf(
                    MoodEntry(moodValue = "happy", moodEmoji = "😊", profileId = "p1", entryDate = "2026-01-15", id = "mo1", createdAt = now, updatedAt = now),
                    MoodEntry(moodValue = "calm", moodEmoji = "😌", profileId = "p1", entryDate = "2026-01-14", id = "mo2", createdAt = now, updatedAt = now)
                ), {}
            )
        }

        render("29-period-home") { PeriodHome(null, {}, {}, {}, {}, {}) }
        render("30-period-log") { LogPeriod(onSave = { _, _, _, _ -> }, onBack = {}) }
        render("31-period-calendar") { PeriodCalendarScreen(emptyList(), {}) }
        render("32-period-history") { PeriodHistoryScreen(emptyList(), {}) }
        render("33-period-settings") { PeriodSettingsScreen({}) }

        render("34-settings-home") { SettingsHomeScreen({}, {}) }
        render("35-profile-settings") { ProfileSettingsScreen({}) }
        render("36-relationship-settings") { RelationshipSettingsScreen({}) }
        render("37-appearance-settings") { AppearanceSettingsScreen(appState(), {}) }
        render("38-notification-settings") {
            NotificationSettingsScreen(SettingsStorage.getInstance(ApplicationProvider.getApplicationContext()), {})
        }
        render("39-about") { AboutScreen({}) }

        render("40-notification-center") {
            val app = ApplicationProvider.getApplicationContext<Application>()
            val db = com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase(app)
            NotificationCenterScreen(
                com.twohearts.app.data.repository.NotificationCenterRepository(db.notificationCenterDao()), {}
            )
        }

        render("41-yuki") {
            val vm: YukiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            YukiScreen(vm, {})
        }

        render("50-home-large-text", scale = TextScalingLevel.EXTRA_LARGE) {
            TestHomeScreen()
        }
        render("51-notes-list-large-text", scale = TextScalingLevel.EXTRA_LARGE) {
            NotesHome(
                listOf(Note(title = "Gratitude for today", content = "Thank you for the little things.", category = "gratitude", id = "n1", createdAt = now, updatedAt = now)),
                {}, {}, {}
            )
        }
    }

    @Test
    fun renderShellAndOnboarding() {
        render("60-onboarding-welcome") {
            com.twohearts.app.ui.onboarding.WelcomeScreen(onGetStarted = {})
        }
        render("61-onboarding-profile") {
            com.twohearts.app.ui.onboarding.ProfileSetupScreen(
                data = com.twohearts.app.ui.onboarding.OnboardingData(), onBack = {}, onNext = {}
            )
        }
        render("62-onboarding-relationship") {
            com.twohearts.app.ui.onboarding.RelationshipSetupScreen(
                data = com.twohearts.app.ui.onboarding.OnboardingData(ownerName = "Alex"),
                onBack = {}, onNext = {}
            )
        }
        render("63-onboarding-personalization") {
            com.twohearts.app.ui.onboarding.PersonalizationSetupScreen(
                data = com.twohearts.app.ui.onboarding.OnboardingData(), onBack = {}, onNext = {}
            )
        }
        render("64-onboarding-complete") {
            com.twohearts.app.ui.onboarding.SetupCompleteScreen(
                data = com.twohearts.app.ui.onboarding.OnboardingData(ownerName = "Alex", partnerName = "Sam"),
                onComplete = {}
            )
        }

        // App shell with bottom nav visible
        render("65-appshell") {
            com.twohearts.app.ui.navigation.AppShell(
                currentRoute = "/app/home",
                onNavigate = {},
                onBack = {},
                canNavigateBack = false
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    TestHomeScreen()
                }
            }
        }
        render("66-appshell-dark", dark = true) {
            com.twohearts.app.ui.navigation.AppShell(
                currentRoute = "/app/notes",
                onNavigate = {},
                onBack = {},
                canNavigateBack = false
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    NotesHome(
                        listOf(Note(title = "Gratitude for today", content = "Thank you.", category = "gratitude", id = "n1", createdAt = now, updatedAt = now)),
                        {}, {}, {}
                    )
                }
            }
        }

        // Home with real relationship data seeded in DB (couple header)
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase(app)
        kotlinx.coroutines.runBlocking {
            db.profileDao().insert(
                com.twohearts.app.data.entity.Profile(
                    name = "Alex", role = "owner", id = "p1", createdAt = now, updatedAt = now
                )
            )
            db.profileDao().insert(
                com.twohearts.app.data.entity.Profile(
                    name = "Sam", role = "partner", birthday = "1996-03-11",
                    id = "p2", createdAt = now, updatedAt = now
                )
            )
            db.coupleRelationshipDao().insert(
                com.twohearts.app.data.entity.CoupleRelationship(
                    ownerId = "p1", partnerId = "p2", startDate = "2023-02-14",
                    id = "rel1", createdAt = now, updatedAt = now
                )
            )
        }
        render("67-home-seeded") {
            TestHomeScreen()
        }
        render("68-us-seeded") { TestUsScreen(relationshipService()) }

        // Vault
        render("69-vault-locked") {
            com.twohearts.app.ui.screens.security.AppLockGate(onUnlock = { false })
        }
        render("70-vault-home") {
            com.twohearts.app.ui.screens.vault.VaultHome(
                items = listOf(
                    com.twohearts.app.data.entity.VaultItem(contentType = "note", title = "Our secret", contentText = "A private note", id = "v1", createdAt = now, updatedAt = now),
                    com.twohearts.app.data.entity.VaultItem(contentType = "photo", title = "Private photo", id = "v2", createdAt = now, updatedAt = now)
                ),
                onItemClick = {}, onAddClick = {}
            )
        }
    }

    @Test
    @Config(sdk = [34], qualifiers = "w360dp-h780dp-mdpi")
    fun renderTargetDevice() {
        render("80-home-target", scale = TextScalingLevel.DEFAULT) {
            TestHomeScreen()
        }
        render("81-notes-target") {
            NotesHome(
                listOf(
                    Note(title = "Gratitude for today", content = "Thank you for the little things.", category = "gratitude", id = "n1", createdAt = now, updatedAt = now),
                    Note(title = "A letter to us", content = "Some words I want to keep.", category = "love-letter", id = "n2", createdAt = now, updatedAt = now)
                ),
                {}, {}, {}
            )
        }
        render("82-mood-target") { MoodHome(null, emptyList(), {}, {}, {}) }
        render("83-settings-target") { SettingsHomeScreen({}, {}) }
    }
}

@Composable
private fun TransformationThemeReference(expectedScale: Float = 1f) {
    val palette = LocalTwoHeartsColors.current
    val scheme = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    check(LocalDensity.current.fontScale == expectedScale)

    val surfaces = listOf(scheme.background, scheme.surface, palette.surfaceElevated, palette.surfaceWarm, palette.surfaceBlush)
    for (surface in surfaces) {
        checkContrast("Primary text", scheme.onSurface, surface, 4.5f)
        checkContrast("Secondary text", palette.textSecondary, surface, 4.5f)
        checkContrast("Tertiary text", palette.textTertiary, surface, 4.5f)
        checkContrast("Focus indicator", scheme.primary, surface, 3f)
        checkContrast("Interactive border", palette.borderStrong, surface, 3f)
    }
    checkContrast("Primary action label", palette.onPrimaryAction, palette.primaryAction, 4.5f)
    checkContrast("Selected text", scheme.onPrimaryContainer, scheme.primaryContainer, 4.5f)
    checkContrast("Inverse action", scheme.inversePrimary, scheme.inverseSurface, 4.5f)
    checkContrast("Milestone", palette.goldInk, palette.goldBg, 4.5f)
    checkContrast("Success", palette.success, palette.successBg, 4.5f)
    checkContrast("Error", palette.error, palette.errorBg, 4.5f)
    checkContrast("Warning", palette.warning, palette.warningBg, 4.5f)
    checkContrast("Information", palette.info, palette.infoBg, 4.5f)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Our story", style = ThTextStyles.eyebrow, color = palette.roseDeep)
        Text(
            "Élodie and Sam",
            style = type.displayLarge,
            color = scheme.onBackground,
            onTextLayout = { check(!it.hasVisualOverflow) },
        )
        Text(
            "A little space for the moments we want to remember.",
            style = type.bodyLarge,
            color = palette.textSecondary,
            onTextLayout = { check(!it.hasVisualOverflow) },
        )
        ThSurfaceCard(tone = CardTone.GOLD, modifier = Modifier.fillMaxWidth()) {
            Text("Together since", style = type.labelLarge, color = palette.goldInk)
            Text("14 February 2023", style = type.headlineSmall, color = palette.goldInk)
        }
        ThSurfaceCard(tone = CardTone.BLUSH, modifier = Modifier.fillMaxWidth()) {
            Text("A note for later", style = type.titleMedium, color = scheme.onSurface)
            Text("Thank you for today.", style = ThTextStyles.reading, color = palette.textSecondary)
        }
        ThButton(onClick = {}, text = "Save note", full = true)
        ThStatusBanner(StatusVariant.SUCCESS, "Note saved.")
    }
}

private fun checkContrast(role: String, foreground: Color, background: Color, minimum: Float) {
    val first = foreground.luminance()
    val second = background.luminance()
    val ratio = (maxOf(first, second) + 0.05f) / (minOf(first, second) + 0.05f)
    check(ratio >= minimum) { "$role contrast was $ratio. Expected at least $minimum." }
}
