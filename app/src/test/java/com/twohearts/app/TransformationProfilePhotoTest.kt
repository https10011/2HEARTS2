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
import android.media.ExifInterface
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.repository.CoupleRelationshipRepository
import com.twohearts.app.data.repository.ProfileRepository
import com.twohearts.app.services.media.AvatarCrop
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.ProfilePhotoChange
import com.twohearts.app.services.media.ProfilePhotoStaging
import com.twohearts.app.services.media.ProfilePhotoWriter
import com.twohearts.app.services.media.decodeProfileImage
import com.twohearts.app.services.relationship.RelationshipService
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException
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
class TransformationProfilePhotoTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val staging = ProfilePhotoStaging(app)
    private val media = MediaStorage(app)

    @Test
    fun stagingSurvivesGrantRevocationAndNormalizesExifOrientation() = runBlocking {
        val file = photo(280, 160)
        ExifInterface(file.absolutePath).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
            saveAttributes()
        }
        val provider = TestPhotoProvider(file)
        val authority = "twohearts.photo.fixture"
        provider.attachInfo(app, ProviderInfo().apply {
            this.authority = authority
            exported = true
        })
        ShadowContentResolver.registerProviderInternal(authority, provider)
        val uri = Uri.parse("content://$authority/picked")
        val source = staging.stage(uri)
        provider.allowed = false
        assertTrue(runCatching { staging.stage(uri) }.exceptionOrNull() is SecurityException)
        assertTrue(file.delete())
        val image = checkNotNull(staging.preview(source))
        try {
            assertEquals(160, image.width)
            assertEquals(280, image.height)
            assertTrue(Color.red(image.getPixel(80, 30)) > 200)
            assertTrue(Color.blue(image.getPixel(80, 250)) > 200)
            val crop = staging.crop(image, AvatarCrop(zoom = 2f, vertical = 1f))
            val result = readCrop(crop)
            try {
                assertEquals(80, result.width)
                assertEquals(80, result.height)
                assertTrue(Color.blue(result.getPixel(40, 40)) > 200)
            } finally {
                result.recycle()
                staging.discard(crop)
            }
        } finally {
            image.recycle()
            staging.discard(source)
        }
    }

    @Test
    fun decodeBoundsLargeImagesAndRejectsCorruptOrMissingSources() = runBlocking {
        val large = photo(2400, 2200)
        val fileId = staging.stage(Uri.fromFile(large))
        val image = checkNotNull(staging.preview(fileId))
        assertTrue(maxOf(image.width, image.height) <= 2048)
        val crop = staging.crop(image, AvatarCrop())
        val avatar = readCrop(crop)
        assertEquals(1024, avatar.width)
        assertEquals(1024, avatar.height)
        image.recycle()
        avatar.recycle()
        staging.discard(crop)
        staging.discard(fileId)
        assertNull(staging.preview(fileId))

        val invalid = File(app.cacheDir, "invalid-photo.jpg").apply { writeText("Invalid photo content") }
        val invalidId = staging.stage(Uri.fromFile(invalid))
        assertNull(staging.preview(invalidId))
        staging.discard(invalidId)
        assertTrue(invalid.delete())
        assertNotNull(runCatching { staging.stage(Uri.fromFile(invalid)) }.exceptionOrNull())
    }

    @Test
    fun bothPartnerPhotosStayReadableAfterRoomReopens() = runBlocking {
        val name = "couple-photos.db"
        val references = Room.databaseBuilder(app, TwoHeartsDatabase::class.java, name).build().useDatabase { db ->
            val relationship = service(db)
            val owner = relationship.createOwner("Alex", "1991-02-03")
            val partner = relationship.createPartner("Avery", null)
            val origin = relationship.createRelationship(owner.id, partner.id, "2019-06-17")
            val ownerRef = editPhoto(db, owner, ProfilePhotoChange.Replace(cropped()))
            val partnerRef = editPhoto(db, partner, ProfilePhotoChange.Replace(cropped()))
            assertTrue(ownerRef != partnerRef)
            assertEquals(2, db.mediaAssetDao().count())
            Triple(requireNotNull(ownerRef), requireNotNull(partnerRef), origin.id)
        }
        try {
            Room.databaseBuilder(app, TwoHeartsDatabase::class.java, name).build().useDatabase { db ->
                val relationship = service(db)
                assertEquals(references.first, relationship.getOwner()?.photoRef)
                assertEquals(references.second, relationship.getPartner()?.photoRef)
                val current = checkNotNull(relationship.getRelationship())
                assertEquals(references.third, current.id)
                assertEquals("2019-06-17", current.startDate)
                relationship.updateRelationship(current.id, "2020-05-14")
                assertEquals("2020-05-14", relationship.getRelationship()?.startDate)
                assertEquals(1, db.coupleRelationshipDao().count())
                for (ref in listOf(references.first, references.second)) {
                    val asset = checkNotNull(db.mediaAssetDao().getById(ref))
                    assertEquals(asset.width, asset.height)
                    val bitmap = checkNotNull(decodeProfileImage(File(asset.filePath), 512))
                    bitmap.recycle()
                    assertNotNull(media.resolveUrl(ref))
                }
            }
        } finally {
            app.deleteDatabase(name)
        }
    }

    @Test
    fun interruptedWriteRollsBackAndPreservesTheDraftForRetry() = runBlocking {
        Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build().useDatabase { db ->
            val relationship = service(db)
            val initial = relationship.createOwner("Alex", null)
            val previous = requireNotNull(editPhoto(db, initial, ProfilePhotoChange.Replace(cropped())))
            val owner = checkNotNull(relationship.getOwner())
            val draft = cropped()
            val operation = ProfilePhotoChange.Replace(draft)
            val interrupted = runCatching {
                ProfilePhotoWriter(db, media, staging).commit(operation) { replacement ->
                    relationship.saveProfileDetails(owner.id, "Edited", null, replacement)
                    throw CancellationException("Test interruption")
                }
            }.exceptionOrNull()
            assertTrue(interrupted is CancellationException)
            assertEquals("Alex", relationship.getOwner()?.name)
            assertEquals(previous, relationship.getOwner()?.photoRef)
            assertEquals(setOf(previous), media.getAllMediaIds())
            assertNotNull(staging.croppedUri(draft))

            val updated = requireNotNull(editPhoto(db, owner, operation))
            assertEquals(updated, relationship.getOwner()?.photoRef)
            assertNull(db.mediaAssetDao().getById(previous))
            assertNull(media.resolveUrl(previous))
            assertNull(staging.croppedUri(draft))
        }
    }

    @Test
    fun removalKeepsSharedMediaUntilTheLastReferenceIsDetached() = runBlocking {
        Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build().useDatabase { db ->
            val relationship = service(db)
            val owner = relationship.createOwner("Alex", null)
            val ref = requireNotNull(editPhoto(db, owner, ProfilePhotoChange.Replace(cropped())))
            val partner = relationship.createPartner("Avery", null, ref)
            assertEquals(ref, editPhoto(db, checkNotNull(relationship.getOwner()), ProfilePhotoChange.Keep))
            assertNull(editPhoto(db, checkNotNull(relationship.getOwner()), ProfilePhotoChange.Remove))
            assertNotNull(media.resolveUrl(ref))
            assertNotNull(db.mediaAssetDao().getById(ref))
            assertNull(editPhoto(db, partner, ProfilePhotoChange.Remove))
            assertNull(media.resolveUrl(ref))
            assertNull(db.mediaAssetDao().getById(ref))
        }
    }

    @Test
    fun evictedDraftCannotReplaceAnExistingPhoto() = runBlocking {
        Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build().useDatabase { db ->
            val relationship = service(db)
            val owner = relationship.createOwner("Alex", null)
            val saved = requireNotNull(editPhoto(db, owner, ProfilePhotoChange.Replace(cropped())))
            val missing = cropped()
            staging.discard(missing)
            var editCalled = false
            val error = runCatching {
                ProfilePhotoWriter(db, media, staging).commit(ProfilePhotoChange.Replace(missing)) {
                    editCalled = true
                    saved
                }
            }.exceptionOrNull()
            assertTrue(error is IOException)
            assertFalse(editCalled)
            assertEquals(saved, relationship.getOwner()?.photoRef)
            assertNotNull(media.resolveUrl(saved))
        }
    }

    private suspend fun <T> TwoHeartsDatabase.useDatabase(operation: suspend (TwoHeartsDatabase) -> T): T =
        try {
            operation(this)
        } finally {
            close()
        }

    private suspend fun editPhoto(db: TwoHeartsDatabase, profile: Profile, change: ProfilePhotoChange): String? {
        val relationship = service(db)
        ProfilePhotoWriter(db, media, staging).commit(change) { replacement ->
            val ref = change.reference(profile.photoRef, replacement)
            relationship.saveProfileDetails(profile.id, profile.name, profile.birthday, ref)
            profile.photoRef?.takeUnless { it == ref }
        }
        return db.profileDao().getById(profile.id)?.photoRef
    }

    private suspend fun cropped(): String {
        val bitmap = Bitmap.createBitmap(96, 74, Bitmap.Config.ARGB_8888)
        try {
            Canvas(bitmap).drawColor(Color.MAGENTA)
            return staging.crop(bitmap, AvatarCrop())
        } finally {
            bitmap.recycle()
        }
    }

    private fun readCrop(id: String): Bitmap {
        val path = checkNotNull(staging.croppedUri(id)?.path)
        return checkNotNull(decodeProfileImage(File(path), maxEdge = 1024))
    }

    private fun photo(width: Int, height: Int): File {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            Canvas(bitmap).apply {
                drawColor(Color.BLUE)
                drawRect(0f, 0f, width / 2f, height.toFloat(), Paint().apply { color = Color.RED })
            }
            return File.createTempFile("profile-fixture", ".jpg", app.cacheDir).also { file ->
                file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.JPEG, 94, it)) }
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun service(db: TwoHeartsDatabase) = RelationshipService(
        ProfileRepository(db.profileDao()), CoupleRelationshipRepository(db.coupleRelationshipDao()),
    )

    private class TestPhotoProvider(private val photo: File) : ContentProvider() {
        var allowed = true

        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
            if (!allowed) throw SecurityException("Photo grant revoked")
            return ParcelFileDescriptor.open(photo, ParcelFileDescriptor.MODE_READ_ONLY)
        }

        override fun onCreate() = true
        override fun getType(uri: Uri) = "image/jpeg"
        override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
    }
}
