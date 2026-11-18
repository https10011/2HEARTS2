package com.twohearts.app

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.data.repository.TimelineEventRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase8TimelineTest {
    @Test fun timelineStaysChronologicalAndEditingCanClearDetails() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Application>(), TwoHeartsDatabase::class.java).build()
        try {
            val repository = TimelineEventRepository(db.timelineEventDao())
            repository.create(TimelineEvent("First", "2024-01-02", "A beginning", id = "first", createdAt = "2024-01-02T00:00:00Z", updatedAt = "2024-01-02T00:00:00Z"))
            repository.create(TimelineEvent("Latest", "2026-08-03", null, id = "latest", createdAt = "2026-08-03T00:00:00Z", updatedAt = "2026-08-03T00:00:00Z"))
            assertEquals(listOf("latest", "first"), repository.observeAll().first().map { it.id })
            assertNull(repository.update("first", mapOf("description" to null))?.description)
        } finally { db.close() }
    }
}
