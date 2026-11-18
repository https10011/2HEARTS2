package com.twohearts.app

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.PlaceRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase10PlacesAndMoodTest {
    @Test fun quickCheckInKeepsLegacyNoteAndLeavesPartnerEntryAlone() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Application>(), TwoHeartsDatabase::class.java,
        ).build()
        try {
            val moods = MoodEntryRepository(db.moodEntryDao())
            val date = "2026-11-18"
            moods.create(MoodEntry("calm", "😌", "Quiet morning", "owner", date, "legacy", "x", "x"))
            moods.create(MoodEntry("sad", "😢", null, "partner", date, "partner", "x", "x"))
            moods.saveOwnerCheckIn("self", date, "happy", "😊", keepNote = true)
            moods.saveOwnerCheckIn("self", date, "tired", "😴", keepNote = true)
            val saved = requireNotNull(moods.getById("legacy"))
            assertEquals("self", saved.profileId)
            assertEquals("tired", saved.moodValue)
            assertEquals("Quiet morning", saved.note)
            assertEquals("sad", moods.getById("partner")?.moodValue)
            assertEquals(2, moods.count())
        } finally {
            db.close()
        }
    }

    @Test fun optionalStoriesCanBeClearedAndMoodCanChangeInPlace() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Application>(), TwoHeartsDatabase::class.java).build()
        try {
            val places = PlaceRepository(db.placeDao())
            places.create(Place("Harbour café", address = "Pier 4", city = "Brighton", notes = "First rainy lunch", id = "p", createdAt = "x", updatedAt = "x"))
            val place = places.update("p", mapOf("address" to null, "city" to null, "notes" to null))
            assertNull(place?.address); assertNull(place?.city); assertNull(place?.notes)

            val moods = MoodEntryRepository(db.moodEntryDao())
            moods.create(MoodEntry("calm", "😌", "Quiet morning", "owner", "2026-11-18", id = "m", createdAt = "x", updatedAt = "x"))
            val mood = moods.update("m", mapOf("moodValue" to "happy", "moodEmoji" to "😊", "note" to null))
            assertEquals("happy", mood?.moodValue); assertNull(mood?.note)
        } finally { db.close() }
    }
}
