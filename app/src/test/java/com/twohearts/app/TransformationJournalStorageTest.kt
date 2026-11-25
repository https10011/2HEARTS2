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
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.PeriodEntryRepository
import com.twohearts.app.data.repository.PeriodSettingsRepository
import com.twohearts.app.data.repository.PlaceRepository
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.PlacePhotoSelection
import com.twohearts.app.services.media.PlaceWriter
import com.twohearts.app.ui.screens.mood.MoodWindow
import com.twohearts.app.ui.screens.mood.belongsToOwner
import com.twohearts.app.ui.screens.mood.moodHistoryGroups
import com.twohearts.app.ui.screens.period.belongsToPeriodOwner
import com.twohearts.app.ui.screens.period.latestLoggedPeriod
import com.twohearts.app.ui.screens.period.nextPeriodEstimate
import com.twohearts.app.ui.screens.places.placeCategoryKey
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowContentResolver

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationJournalStorageTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val stamp = "2024-02-29T10:00:00Z"
    private val today = LocalDate.parse("2024-02-29")
    private val place = Place(
        name = "Quiet lookout", city = "Porto", state = "Porto District", country = "Portugal",
        category = " Date ", latitude = 41.142, longitude = -8.621, memoryId = "existing-memory",
        id = "lookout", createdAt = stamp, updatedAt = stamp,
    )
    private val mood = MoodEntry("calm", "😌", "With the window open.", "owner", today.toString(), "mood", stamp, stamp)
    private val period = PeriodEntry(
        startDate = "2024-02-26", endDate = today.toString(), flowLevel = "medium", note = "Private log",
        profileId = "owner", id = "period", createdAt = stamp, updatedAt = stamp,
    )

    @Test
    fun placePhotoAndSavedFieldsSurviveReopenWithSafeDetachAndSharedReferences() = runBlocking {
        val name = "stage14-places"
        val storage = MediaStorage(app)
        val photo = placeTestPhoto(app)
        val before = storage.getAllMediaIds()
        try {
            val pick = PlacePhotoSelection.Pick(registerPhoto("twohearts.place.reopen", photo))
            val ref = withDb(name) { db ->
                val places = PlaceRepository(db.placeDao())
                PlaceWriter(db, places, storage, app.contentResolver).save(place, null, pick)
                val id = checkNotNull(places.getById(place.id)?.photoRef)
                assertNotNull(db.mediaAssetDao().getById(id))
                places.create(place.copy(id = "shared-place", photoRef = id))
                id
            }
            withDb(name) { db ->
                val places = PlaceRepository(db.placeDao())
                val writer = PlaceWriter(db, places, storage, app.contentResolver)
                val restored = checkNotNull(places.getById(place.id))
                assertEquals(place.copy(photoRef = ref), restored)
                assertEquals("date", placeCategoryKey(restored.category))
                writer.save(restored.copy(address = "North stairs"), restored, PlacePhotoSelection.Remove)
                val edited = checkNotNull(places.getById(place.id))
                assertEquals("North stairs", edited.address)
                assertNull(edited.photoRef)
                assertEquals(place.memoryId, edited.memoryId)
                assertEquals(place.latitude, edited.latitude)
                assertTrue(ref in storage.getAllMediaIds())
                val shared = checkNotNull(places.getById("shared-place"))
                writer.save(shared, shared, pick)
                val replaced = checkNotNull(places.getById(shared.id))
                val nextRef = checkNotNull(replaced.photoRef)
                assertTrue(nextRef != ref && nextRef in storage.getAllMediaIds())
                assertFalse(ref in storage.getAllMediaIds())
                assertNull(db.mediaAssetDao().getById(ref))
                writer.save(replaced, replaced, PlacePhotoSelection.Remove)
                assertFalse(nextRef in storage.getAllMediaIds())
                places.softDelete(place.id)
            }
            withDb(name) { db -> assertNull(PlaceRepository(db.placeDao()).getById(place.id)) }
        } finally {
            (storage.getAllMediaIds() - before).forEach(storage::delete)
            photo.delete()
            app.deleteDatabase(name)
        }
    }

    @Test
    fun corruptPhotoAndStalePlaceEditLeaveExistingRecordAndMediaIntact() = runBlocking {
        val name = "stage14-place-failures"
        val storage = MediaStorage(app)
        val good = placeTestPhoto(app)
        val bad = File.createTempFile("corrupt-place", ".jpg", app.cacheDir).apply { writeText("not a photo") }
        val before = storage.getAllMediaIds()
        try {
            withDb(name) { db ->
                val places = PlaceRepository(db.placeDao())
                val writer = PlaceWriter(db, places, storage, app.contentResolver)
                val chosen = PlacePhotoSelection.Pick(registerPhoto("twohearts.place.good", good))
                writer.save(place, null, chosen)
                val saved = checkNotNull(places.getById(place.id))
                val expected = storage.getAllMediaIds()
                val corrupt = PlacePhotoSelection.Pick(registerPhoto("twohearts.place.corrupt", bad))
                assertTrue(runCatching { writer.save(saved.copy(name = "Broken"), saved, corrupt) }.isFailure)
                assertEquals(saved, places.getById(place.id))
                val newer = checkNotNull(places.update(place.id, mapOf("notes" to "A later edit")))
                assertTrue(runCatching { writer.save(saved.copy(name = "Stale"), saved, chosen) }.isFailure)
                assertEquals(newer, places.getById(place.id))
                assertEquals(expected, storage.getAllMediaIds())
                assertEquals(1, db.mediaAssetDao().count())
            }
        } finally {
            (storage.getAllMediaIds() - before).forEach(storage::delete)
            good.delete()
            bad.delete()
            app.deleteDatabase(name)
        }
    }

    @Test
    fun journalDatesUseLocalBoundariesAndKeepOtherProfilesOutOfOwnerEdits() {
        val older = mood.copy(id = "older", entryDate = "2024-02-23")
        val outsideWeek = mood.copy(id = "outside-week", entryDate = "2024-02-22")
        val future = mood.copy(id = "future", entryDate = "2024-03-01")
        val malformed = mood.copy(id = "invalid", entryDate = "2023-02-29")
        val samples = listOf(older, future, mood, outsideWeek, malformed)
        assertEquals(listOf(mood, older), moodHistoryGroups(samples, MoodWindow.WEEK, today).values.flatten())
        assertEquals(listOf(mood, older, outsideWeek), moodHistoryGroups(samples, MoodWindow.MONTH, today).values.flatten())
        assertEquals(listOf(YearMonth.of(2024, 3), YearMonth.of(2024, 2), null), moodHistoryGroups(samples, MoodWindow.ALL, today).keys.toList())
        assertTrue(mood.belongsToOwner("self"))
        assertFalse(mood.copy(profileId = "partner").belongsToOwner("self"))
        assertTrue(period.belongsToPeriodOwner("self"))
        assertFalse(period.copy(profileId = "partner").belongsToPeriodOwner("self"))
        val latest = latestLoggedPeriod(listOf(period.copy(startDate = "unknown"), period.copy(startDate = "2024-03-02"), period), today)
        assertEquals(period, latest)
        assertEquals(LocalDate.of(2024, 3, 1), nextPeriodEstimate(latest, 4, today))
    }

    @Test
    fun moodEditsAndPrivatePeriodSettingsAndDeletionSurviveRoomReopen() = runBlocking {
        val name = "stage14-journal"
        try {
            withDb(name) { db ->
                val moods = MoodEntryRepository(db.moodEntryDao())
                moods.create(mood)
                moods.create(mood.copy(id = "partner", profileId = "partner"))
                moods.saveOwnerCheckIn("self", today.toString(), "happy", "😊", keepNote = true)
                val periods = PeriodEntryRepository(db.periodEntryDao())
                periods.create(period)
                periods.create(period.copy(id = "other-cycle", profileId = "partner"))
                periods.update(period.id, mapOf("endDate" to null, "note" to "Edited private note"))
                PeriodSettingsRepository(db.periodSettingsDao()).saveLengths(27, 6)
            }
            withDb(name) { db ->
                val moods = MoodEntryRepository(db.moodEntryDao())
                val saved = checkNotNull(moods.getById(mood.id))
                assertEquals("self", saved.profileId)
                assertEquals(mood.note, saved.note)
                assertEquals("happy", saved.moodValue)
                assertEquals("calm", moods.getById("partner")?.moodValue)
                moods.softDelete(saved.id)
                val periods = PeriodEntryRepository(db.periodEntryDao())
                val owner = periods.observeAll().first().filter { it.belongsToPeriodOwner("self") }.single()
                assertNull(owner.endDate)
                assertEquals("Edited private note", owner.note)
                val lengths = checkNotNull(PeriodSettingsRepository(db.periodSettingsDao()).observe().first())
                assertEquals(27 to 6, lengths.cycleLengthDays to lengths.periodLengthDays)
                periods.softDelete(owner.id)
            }
            withDb(name) { db ->
                assertNull(MoodEntryRepository(db.moodEntryDao()).getById(mood.id))
                assertNull(PeriodEntryRepository(db.periodEntryDao()).getById(period.id))
                assertNotNull(PeriodEntryRepository(db.periodEntryDao()).getById("other-cycle"))
            }
        } finally {
            app.deleteDatabase(name)
        }
    }

    private suspend fun <T> withDb(name: String, block: suspend (TwoHeartsDatabase) -> T): T {
        val db = Room.databaseBuilder(app, TwoHeartsDatabase::class.java, name).build()
        return try { block(db) } finally { db.close() }
    }

    private fun registerPhoto(authority: String, photo: File): Uri {
        val provider = PhotoFixtureProvider(photo)
        provider.attachInfo(app, ProviderInfo().apply { this.authority = authority })
        ShadowContentResolver.registerProviderInternal(authority, provider)
        return Uri.parse("content://$authority/photo")
    }

    private class PhotoFixtureProvider(private val photo: File) : ContentProvider() {
        override fun onCreate() = true
        override fun getType(uri: Uri) = "image/jpeg"
        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor =
            ParcelFileDescriptor.open(photo, ParcelFileDescriptor.MODE_READ_ONLY)

        override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
    }
}

internal fun placeTestPhoto(app: Application): File {
    val file = File.createTempFile("local-place", ".jpg", app.cacheDir)
    val bitmap = Bitmap.createBitmap(480, 280, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    try {
        Canvas(bitmap).apply {
            drawColor(Color.rgb(219, 180, 162))
            paint.color = Color.rgb(250, 219, 161)
            drawCircle(348f, 62f, 30f, paint)
            paint.color = Color.rgb(136, 103, 117)
            drawOval(-190f, 115f, 390f, 550f, paint)
            paint.color = Color.rgb(70, 86, 89)
            drawOval(174f, 143f, 610f, 560f, paint)
            paint.color = Color.rgb(201, 151, 126)
            drawLine(270f, 243f, 390f, 194f, paint.apply { strokeWidth = 7f })
        }
        file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) }
    } finally {
        bitmap.recycle()
    }
    return file
}
