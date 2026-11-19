package com.twohearts.app

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MemoryMedia
import com.twohearts.app.data.entity.Place
import com.twohearts.app.services.media.MediaThumbnails
import com.twohearts.app.services.media.copyWithinLimit
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Phase17MediaPerformanceTest {
    private val now = "2026-11-19T00:00:00Z"

    @Test
    fun streamsEnforceTheCopyLimitWithoutATrustedSizeHint() {
        val output = ByteArrayOutputStream()
        assertEquals(8192L, ByteArray(8192).inputStream().copyWithinLimit(output, 8192))
        assertEquals(8192, output.size())
        val oversizeOutput = ByteArrayOutputStream()
        assertThrows(IOException::class.java) {
            ByteArray(8193).inputStream().copyWithinLimit(oversizeOutput, 8192)
        }
        assertEquals(8192, oversizeOutput.size())
    }

    @Test
    fun largePhotosAreSampledAndReusedWithoutRewritingTheOriginal() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val source = File.createTempFile("preview-source", ".jpg", context.cacheDir)
        try {
            val original = Bitmap.createBitmap(4096, 3072, Bitmap.Config.ARGB_8888)
            val originalBytes = original.allocationByteCount
            source.outputStream().use { assertTrue(original.compress(Bitmap.CompressFormat.JPEG, 95, it)) }
            original.recycle()
            val encodedLength = source.length()
            val preview = checkNotNull(MediaThumbnails.load(source.path, now, 1024))
            assertEquals(1024, maxOf(preview.width, preview.height))
            assertTrue(preview.allocationByteCount <= originalBytes / 16)
            assertSame(preview, MediaThumbnails.load(source.path, now, 1024))
            assertEquals(encodedLength, source.length())
            assertNotNull(MediaThumbnails.load(source.path, "new revision", 512))
            assertNull(MediaThumbnails.load(source.path + ".missing", now, 1024))
        } finally {
            source.delete()
        }
        Unit
    }

    @Test
    fun attachmentReadsKeepOrderAndExcludeRemovedRecordsWithoutChangingTheLinks() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, TwoHeartsDatabase::class.java).build()
        try {
            db.memoryDao().insert(Memory("Walk", memoryDate = now.take(10), id = "memory", createdAt = now, updatedAt = now))
            val assets = db.mediaAssetDao()
            for (id in listOf("a", "b")) {
                assets.insert(MediaAsset("/tmp/$id.jpg", "image/jpeg", 12, id = id, createdAt = now, updatedAt = now))
            }
            val links = db.memoryMediaDao()
            links.insert(MemoryMedia("memory", "a", sortOrder = 1))
            links.insert(MemoryMedia("memory", "b", sortOrder = 0))
            db.placeDao().insert(Place("Park", photoRef = "a", id = "place", createdAt = now, updatedAt = now))
            assertEquals(listOf("b", "a"), links.getAssets("memory").map { it.id })
            assertEquals(listOf("b", "a"), links.observeAttachments().first().map { it.asset.id })
            assertEquals("a", assets.observePlaceAttachments().first().single().asset.id)
            assets.softDelete("a", now, now)
            assertEquals(listOf("b"), links.observeAttachments().first().map { it.asset.id })
            assertTrue(assets.observePlaceAttachments().first().isEmpty())
            db.memoryDao().softDelete("memory", now, now)
            assertTrue(links.observeAttachments().first().isEmpty())
            assertEquals(2, links.getByMemoryId("memory").size)
        } finally {
            db.close()
        }
    }
}
