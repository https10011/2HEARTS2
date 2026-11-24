package com.twohearts.app

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.MediaThumbnails
import com.twohearts.app.services.media.MemoryAttachmentChoice
import com.twohearts.app.services.media.MemoryAttachmentChoice.Picked
import com.twohearts.app.services.media.MemoryAttachmentChoice.Saved
import com.twohearts.app.services.media.MemoryWriter
import com.twohearts.app.ui.components.VideoPreviewDialog
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.memories.AddMemory
import com.twohearts.app.ui.screens.memories.MemoriesHome
import com.twohearts.app.ui.screens.memories.MemoryAttachmentPreview
import com.twohearts.app.ui.screens.memories.MemoryDetail
import com.twohearts.app.ui.screens.memories.MemoryMediaSource
import com.twohearts.app.ui.screens.memories.mergeMemoryPicks
import com.twohearts.app.ui.screens.memories.replaceMemoryPick
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import java.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowContentResolver
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationMemoryTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val storage = MediaStorage(app)
    private val sampleFiles = mutableListOf<File>()
    private val date = "2026-10-26"
    private val now = "2026-10-26T12:00:00Z"
    private val memory = Memory("The long way home", "We stopped by the water until the lights came on.", date,
        id = "first", createdAt = now, updatedAt = now)

    @After
    fun clearFixtures() {
        storage.getAllMediaIds().forEach(storage::delete)
        sampleFiles.forEach(File::delete)
    }

    @Test
    fun orderedFilesSurviveReopeningAndReplacementKeepsSharedReferences() = runBlocking {
        val photo = photoFile()
        val video = clipFile()
        val picked = register(photo, video)
        val databaseName = "memory-writer-reopen"
        val originalIds = withDatabase(openDatabase(databaseName)) { db, repo, writer ->
            writer.save(memory, null, emptyList(), picked)
            val assets = repo.getAssets(memory.id)
            assertEquals(listOf("image/jpeg", "video/mp4"), assets.map { it.mimeType })
            assertTrue(File(assets[0].filePath).readBytes().contentEquals(photo.readBytes()))
            assertTrue(File(assets[1].filePath).readBytes().contentEquals(video.readBytes()))
            val shared = memory.copy(id = "shared")
            repo.create(shared)
            repo.addMedia(shared.id, assets[1].id)
            assertEquals(2, db.mediaAssetDao().count())
            assets.map { it.id }
        }
        try {
            withDatabase(openDatabase(databaseName)) { db, repo, writer ->
                assertEquals(originalIds, repo.getAssets(memory.id).map { it.id })
                val previous = repo.getById(memory.id)
                writer.save(memory.copy(title = "An evening by the water"), previous, originalIds, listOf(picked.first()))
                val replacement = repo.getAssets(memory.id).single().id
                assertTrue(replacement !in originalIds)
                assertNull(storage.resolveUrl(originalIds[0]))
                assertNotNull(storage.resolveUrl(originalIds[1]))
                writer.delete(memory.id)
                assertNull(repo.getById(memory.id))
                assertEquals(listOf(originalIds[1]), repo.getAssets("shared").map { it.id })
                assertNull(storage.resolveUrl(replacement))
                assertEquals(1, db.mediaAssetDao().count())
            }
        } finally {
            app.deleteDatabase(databaseName)
        }
    }

    @Test
    fun failedImportRollsBackTheRecordLinksAndFilesAndCanBeRetried() = runBlocking {
        val photo = photoFile()
        val video = clipFile()
        val picked = register(photo, video)
        val invalid = Picked(Uri.parse("content://twohearts.memory.fixture/denied"))
        withDatabase(Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build()) { db, repo, writer ->
            assertTrue(runCatching { writer.save(memory, null, emptyList(), listOf(picked[0], invalid)) }.isFailure)
            assertNull(repo.getById(memory.id))
            assertEquals(emptySet<String>(), storage.getAllMediaIds())
            writer.save(memory, null, emptyList(), listOf(picked[1]))
            val previous = checkNotNull(repo.getById(memory.id))
            val savedId = repo.getAssets(memory.id).single().id
            val edited = memory.copy(title = "After sunset")
            assertTrue(runCatching {
                writer.save(edited, previous, listOf(savedId), listOf(picked[0], invalid))
            }.isFailure)
            assertEquals(previous, repo.getById(memory.id))
            assertEquals(listOf(savedId), repo.getAssets(memory.id).map { it.id })
            assertEquals(setOf(savedId), storage.getAllMediaIds())
            assertEquals(1, db.mediaAssetDao().count())
            writer.save(edited, previous, listOf(savedId), listOf(picked[0], Saved(savedId)))
            assertEquals(edited.title, repo.getById(memory.id)?.title)
            assertEquals(listOf("image/jpeg", "video/mp4"), repo.getAssets(memory.id).map { it.mimeType })
            assertTrue(runCatching { writer.save(edited, previous, listOf(savedId), listOf(picked[0])) }.isFailure)
        }
    }

    @Test
    fun pickerCancellationKeepsTheExistingDraftAndReplacementKeepsPosition() {
        val first = Uri.parse("content://twohearts.memory.fixture/photo")
        val replacement = Uri.parse("content://twohearts.memory.fixture/video")
        val draft = listOf(Saved("previous"), Picked(first))
        assertEquals(draft, mergeMemoryPicks(draft, emptyList()))
        assertEquals(draft, mergeMemoryPicks(draft, listOf(first)))
        assertEquals(draft, replaceMemoryPick(draft, 0, null))
        assertEquals(listOf(Picked(replacement), Picked(first)), replaceMemoryPick(draft, 0, replacement))
    }

    @Test
    fun galleryAndDetailRenderLocalPhotoAndOpenTheRightRecord() {
        val photo = cachePhoto()
        val video = asset(clipFile(), "local-clip", "video/mp4")
        var opened: String? = null
        render("gallery") {
            MemoriesHome(listOf(memory, memory.copy(id = "b", title = "Our first market day")),
                { opened = it }, {}, {}, mapOf(memory.id to listOf(photo, video)))
        }.use { scene ->
            assertTrue(scene.click("The long way home"))
            assertEquals(memory.id, opened)
            assertTrue(scene.click("Our first market day"))
            assertEquals("b", opened)
        }
        render("detail-dark", dark = true, route = RoutePath.APP_MEMORIES_DETAIL) {
            MemoryDetail(memory, {}, {}, {}, listOf(photo, video))
        }.use { scene ->
            assertTrue(scene.click("View photo"))
            scene.captureDialog("photo-preview-dark")
            assertTrue(scene.dialogText("Photo preview"))
            scene.closeDialog()
            assertTrue(scene.clickDescription("Video 2"))
            assertTrue(scene.activityText("Play video"))
            assertTrue(scene.clickDescription("Photo 1"))
            assertTrue(scene.activityText("View photo"))
            assertTrue(scene.click("Delete memory"))
            scene.captureDialog("delete-confirmation-dark")
            assertTrue(scene.dialogText("Delete this memory?"))
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h1240dp-mdpi")
    fun editorRemovesASavedAttachmentFromTheDraftBeforeSubmission() {
        val photo = cachePhoto()
        val video = asset(clipFile(), "local-clip", "video/mp4")
        var saved: List<MemoryAttachmentChoice>? = null
        render("editor-dark-large", dark = true, scale = 1.5f, route = RoutePath.APP_MEMORIES_EDIT) {
            AddMemory(memory.id, memory.title, memory.caption.orEmpty(), memory.memoryDate,
                onSave = { _, _, _, entries -> saved = entries }, onBack = {}, initialMedia = listOf(photo, video))
        }.use { scene ->
            assertTrue(scene.click("Remove", occurrence = 0))
            assertNull(saved)
            assertTrue(scene.click("Save changes"))
            assertEquals(listOf(Saved(video.id)), saved)
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-mdpi")
    fun newEditorRemainsScrollableAtTwoHundredPercentAndShowsTitleValidation() {
        var submitted = false
        render("new-memory-narrow-200", dark = true, scale = 2f, route = RoutePath.APP_MEMORIES_ADD) {
            AddMemory(onSave = { _, _, _, _ -> submitted = true }, onBack = {})
        }.use { scene ->
            assertTrue(scene.click("Save memory"))
            assertFalse(submitted)
            assertTrue(scene.activityError("Give this memory a title."))
            scene.capture(scene.activityView, "new-memory-validation-200")
        }
    }

    @Test
    fun localVideoPassesItsUriToThePlayerAndPausesWhenTheActivityLosesFocus() {
        val clip = clipFile()
        val uri = Uri.fromFile(clip)
        render(null) { VideoPreviewDialog(uri) {} }.use { scene ->
            val player = checkNotNull(scene.dialogView().findViewOfType(VideoView::class.java))
            val shadow = shadowOf(player)
            assertEquals(uri.toString(), shadow.videoURIString)
            shadow.onPreparedListener.onPrepared(MediaPlayer())
            assertTrue(player.isPlaying)
            scene.controller.pause()
            assertFalse(player.isPlaying)
        }
        render(null, dark = true) {
            MemoryAttachmentPreview(MemoryMediaSource(asset(File(clip.parent, "absent.mp4"), "gone", "video/mp4"))) {}
        }.use { scene ->
            scene.captureDialog("video-unavailable-dark")
            assertTrue(scene.dialogText("Couldn’t play this video."))
        }
    }

    private fun openDatabase(name: String): TwoHeartsDatabase =
        Room.databaseBuilder(app, TwoHeartsDatabase::class.java, name).build()

    private suspend fun <T> withDatabase(
        db: TwoHeartsDatabase, test: suspend (TwoHeartsDatabase, MemoryRepository, MemoryWriter) -> T,
    ): T = try {
        val repo = MemoryRepository(db.memoryDao(), db.memoryMediaDao())
        test(db, repo, MemoryWriter(db, repo, storage, app.contentResolver))
    } finally {
        db.close()
    }

    private fun register(photo: File, video: File): List<Picked> {
        val provider = FixtureProvider(photo, video)
        val authority = "twohearts.memory.fixture"
        provider.attachInfo(app, ProviderInfo().apply { this.authority = authority })
        ShadowContentResolver.registerProviderInternal(authority, provider)
        return listOf("photo", "video").map { Picked(Uri.parse("content://$authority/$it")) }
    }

    private fun cachePhoto(): MediaAsset {
        val asset = asset(photoFile(), "saved-image", "image/jpeg")
        runBlocking { checkNotNull(MediaThumbnails.load(asset.filePath, asset.updatedAt, 512, normalizeExif = true)) }
        return asset
    }

    private fun asset(file: File, id: String, mimeType: String) =
        MediaAsset(file.absolutePath, mimeType, file.length(), id = id, createdAt = now, updatedAt = now)

    private fun photoFile(): File {
        val file = File.createTempFile("memory-photo", ".jpg", app.cacheDir).also(sampleFiles::add)
        val frame = Bitmap.createBitmap(400, 260, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        try {
            Canvas(frame).apply {
                drawColor(Color.rgb(230, 168, 149))
                paint.color = Color.rgb(254, 228, 178)
                drawCircle(276f, 82f, 37f, paint)
                paint.color = Color.rgb(108, 83, 114)
                drawRect(0f, 133f, 400f, 260f, paint)
                paint.color = Color.rgb(54, 67, 81)
                drawOval(-65f, 174f, 293f, 402f, paint)
                paint.color = Color.rgb(200, 145, 146)
                drawRect(245f, 154f, 307f, 158f, paint)
                drawRect(204f, 170f, 326f, 174f, paint)
            }
            file.outputStream().use { check(frame.compress(Bitmap.CompressFormat.JPEG, 94, it)) }
        } finally {
            frame.recycle()
        }
        return file
    }

    private fun clipFile(): File = File.createTempFile("memory-clip", ".mp4", app.cacheDir).also { file ->
        sampleFiles += file
        checkNotNull(javaClass.getResourceAsStream("/memories/local-preview.mp4")).use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
    }

    private fun render(
        name: String?,
        dark: Boolean = false,
        scale: Float = 1f,
        route: String = RoutePath.APP_MEMORIES,
        content: @Composable () -> Unit,
    ): Scene {
        val scene = Scene(Robolectric.buildActivity(ComponentActivity::class.java).setup())
        scene.setContent {
            CompositionLocalProvider(LocalShellRoute provides route) {
                TwoHeartsTheme(darkTheme = dark, textSizeScale = scale, reduceMotion = true) { content() }
            }
        }
        if (name != null) scene.capture(scene.activityView, name) else scene.layout(scene.dialogView())
        return scene
    }

    private class Scene(val controller: org.robolectric.android.controller.ActivityController<ComponentActivity>) : AutoCloseable {
        // Drive recomposition after invoking a native semantics action in the paused Android looper.
        private val frameClock = BroadcastFrameClock()
        private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob() + frameClock)
        private val recomposer = Recomposer(scope.coroutineContext)
        private val composeView = ComposeView(controller.get())
        private var frameTime = 0L
        val activityView: View get() = controller.get().window.decorView

        fun setContent(content: @Composable () -> Unit) {
            scope.launch { recomposer.runRecomposeAndApplyChanges() }
            composeView.setParentCompositionContext(recomposer)
            composeView.setContent(content)
            controller.get().setContentView(composeView)
            advanceFrames()
        }

        private fun advanceFrames() {
            repeat(4) {
                Snapshot.sendApplyNotifications()
                shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(16))
                frameTime += 16_000_000L
                frameClock.sendFrame(frameTime)
            }
        }

        fun dialogView(): View {
            advanceFrames()
            layout(activityView)
            return checkNotNull(ShadowDialog.getLatestDialog()?.window) { "Expected an open dialog" }.decorView
        }

        fun closeDialog() {
            val dialog = dialogView()
            layout(dialog)
            val close = nodes(dialog).single {
                it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == "Close" } == true &&
                    it.config.getOrNull(SemanticsActions.OnClick) != null
            }
            assertTrue(checkNotNull(close.config.getOrNull(SemanticsActions.OnClick)?.action).invoke())
            advanceFrames()
        }

        fun layout(view: View) {
            val metrics = controller.get().resources.displayMetrics
            advanceFrames()
            view.measure(View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.EXACTLY))
            view.layout(0, 0, metrics.widthPixels, metrics.heightPixels)
            advanceFrames()
        }

        fun click(label: String, occurrence: Int = 0): Boolean =
            clickNode(occurrence) { it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == label } == true }

        fun clickDescription(label: String): Boolean =
            clickNode(0) { it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(label) == true }

        private fun clickNode(occurrence: Int, matches: (SemanticsNode) -> Boolean): Boolean {
            layout(activityView)
            val actions = nodes(activityView).filter(matches).mapNotNull { it.config.getOrNull(SemanticsActions.OnClick)?.action }
            val handled = actions.getOrNull(occurrence)?.invoke() == true
            advanceFrames()
            layout(activityView)
            return handled
        }

        fun dialogText(label: String) = hasText(dialogView(), label)
        fun activityText(label: String) = hasText(activityView, label)
        fun activityError(message: String): Boolean = nodes(activityView).any {
            it.config.getOrNull(SemanticsProperties.Error) == message
        }

        private fun hasText(view: View, label: String): Boolean = nodes(view).any { node ->
            node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == label } == true
        }

        private fun nodes(view: View) = checkNotNull(view.findViewOfType(ViewRootForTest::class.java))
            .semanticsOwner.getAllSemanticsNodes(mergingEnabled = true)

        fun captureDialog(name: String) = capture(dialogView(), name)

        fun capture(view: View, name: String) {
            layout(view)
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            try {
                view.draw(Canvas(bitmap))
                val dir = File("build/transformation-11").apply { mkdirs() }
                File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            } finally {
                bitmap.recycle()
            }
        }

        override fun close() {
            composeView.disposeComposition()
            recomposer.cancel()
            scope.cancel()
            controller.close()
        }
    }

    private class FixtureProvider(private val photo: File, private val video: File) : ContentProvider() {
        override fun onCreate() = true
        override fun getType(uri: Uri) = if (uri.lastPathSegment == "video") "video/mp4" else "image/jpeg"
        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor = when (uri.lastPathSegment) {
            "photo" -> ParcelFileDescriptor.open(photo, ParcelFileDescriptor.MODE_READ_ONLY)
            "video" -> ParcelFileDescriptor.open(video, ParcelFileDescriptor.MODE_READ_ONLY)
            else -> throw SecurityException("Permission expired")
        }

        override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
    }
}

private fun <T : Any> View.findViewOfType(type: Class<T>): T? {
    if (type.isInstance(this)) return type.cast(this)
    if (this is ViewGroup) repeat(childCount) { getChildAt(it).findViewOfType(type)?.let { found -> return found } }
    return null
}
