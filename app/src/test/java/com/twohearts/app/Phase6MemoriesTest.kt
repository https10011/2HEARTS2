package com.twohearts.app

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.services.media.MediaStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase6MemoriesTest {
    @Test
    fun mediaPolicyAcceptsSupportedFilesAndRejectsUnsafeInputs() {
        val storage = MediaStorage(ApplicationProvider.getApplicationContext<Application>())
        assertTrue(storage.isValidMimeType("image/jpeg"))
        assertTrue(storage.isValidMimeType("video/mp4"))
        assertFalse(storage.isValidMimeType("application/pdf"))
        assertTrue(storage.isValidFileSize(MediaStorage.MAX_PHOTO_SIZE, "image/jpeg"))
        assertFalse(storage.isValidFileSize(MediaStorage.MAX_PHOTO_SIZE + 1, "image/jpeg"))
        assertFalse(storage.isValidFileSize(MediaStorage.MAX_VIDEO_SIZE + 1, "video/mp4"))
    }

    @Test
    fun editingCanClearAnExistingCaptionWithoutChangingTheMemoryIdentity() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val database = Room.inMemoryDatabaseBuilder(context, TwoHeartsDatabase::class.java).build()
        try {
            val repository = MemoryRepository(database.memoryDao(), database.memoryMediaDao())
            repository.create(Memory("First snowfall", "The quietest morning", "2026-01-02", id = "snow", createdAt = "2026-01-02T10:00:00Z", updatedAt = "2026-01-02T10:00:00Z"))
            val updated = repository.update("snow", mapOf("caption" to null, "title" to "First snowfall"))
            assertEquals("snow", updated?.id)
            assertNull(updated?.caption)
            assertNull(repository.getById("missing"))
        } finally {
            database.close()
        }
    }
}
