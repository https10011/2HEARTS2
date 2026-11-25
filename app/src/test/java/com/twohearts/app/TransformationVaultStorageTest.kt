package com.twohearts.app

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.repository.PlaceRepository
import com.twohearts.app.data.repository.VaultItemRepository
import com.twohearts.app.services.datamanagement.encodeTransferDocument
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.VaultStorage
import com.twohearts.app.services.media.deleteUnreferencedMedia
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowContentResolver

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationVaultStorageTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val stamp = "2026-11-25T00:00:00Z"

    @Test
    fun vaultPersistsLocalDocumentsAndNotesAndExportsOnlyTheSelectedFile() = runBlocking {
        val name = "stage16-vault"
        val storage = MediaStorage(app)
        val before = storage.getAllMediaIds()
        val file = File.createTempFile("vault-note-file", ".txt", app.cacheDir).apply { writeText("This is a local file fixture.") }
        try {
            val source = registerFile("twohearts.vault.document", file, "text/plain")
            var docId = ""
            withDb(name) { db ->
                val items = VaultItemRepository(db.vaultItemDao())
                val vault = VaultStorage(app, db, storage, items)
                vault.add("Private fixture note", "note", "Local journal content.")
                vault.add("Stored document", "file", source.toString())
                docId = items.observeAll().first().single { it.contentType == "file" }.id
            }
            withDb(name) { db ->
                val items = VaultItemRepository(db.vaultItemDao())
                val vault = VaultStorage(app, db, storage, items)
                assertEquals("Local journal content.", items.observeAll().first().single { it.contentType == "note" }.contentText)
                val item = checkNotNull(items.getById(docId))
                val asset = checkNotNull(vault.availableAsset(checkNotNull(item.mediaRef)))
                assertTrue(asset.filePath.startsWith(File(app.filesDir, "media").absolutePath))
                db.mediaAssetDao().insert(asset.copy(id = "missing-file", filePath = asset.filePath + ".missing"))
                assertNull(vault.availableAsset("missing-file"))
                val destination = Uri.parse("content://twohearts.vault.export/output")
                val output = ByteArrayOutputStream()
                shadowOf(app.contentResolver).registerOutputStream(destination, output)
                vault.export(asset, destination)
                assertArrayEquals(file.readBytes(), output.toByteArray())
                assertTrue(runCatching { vault.export(asset.copy(filePath = file.absolutePath), destination) }.isFailure)
                val transfer = encodeTransferDocument(db.noteDao().getAll().first(), db.reminderDao().getAll().first())
                assertFalse(transfer.contains("Local journal content"))
                assertFalse(transfer.contains("Stored document"))
                vault.delete(item.id)
                assertFalse(asset.id in storage.getAllMediaIds())
            }
            withDb(name) { db -> assertNull(VaultItemRepository(db.vaultItemDao()).getById(docId)) }
        } finally {
            (storage.getAllMediaIds() - before).forEach(storage::delete)
            file.delete()
            app.deleteDatabase(name)
        }
    }

    @Test
    fun corruptCopiesAreRemovedAndSharedVaultPhotosStayUntilTheLastReferenceIsCleared() = runBlocking {
        val name = "stage16-vault-shared"
        val storage = MediaStorage(app)
        val before = storage.getAllMediaIds()
        val valid = placeTestPhoto(app)
        val corrupt = File.createTempFile("vault-corrupt", ".jpg", app.cacheDir).apply { writeText("Not image bytes.") }
        try {
            withDb(name) { db ->
                val items = VaultItemRepository(db.vaultItemDao())
                val vault = VaultStorage(app, db, storage, items)
                val bad = registerFile("twohearts.vault.corrupt", corrupt, "image/jpeg")
                assertTrue(runCatching { vault.add("Bad attachment", "photo", bad.toString()) }.isFailure)
                assertEquals(before, storage.getAllMediaIds())
                assertEquals(0, items.count())
                assertEquals(0, db.mediaAssetDao().count())

                val good = registerFile("twohearts.vault.photo", valid, "image/jpeg")
                vault.add("Shared private photo", "photo", good.toString())
                val item = items.observeAll().first().single()
                val ref = checkNotNull(item.mediaRef)
                assertTrue(ref in db.mediaAssetDao().retainedIds())
                deleteUnreferencedMedia(db, storage, ref)
                assertTrue(ref in storage.getAllMediaIds())

                val places = PlaceRepository(db.placeDao())
                places.create(Place("Lookout", photoRef = ref, id = "place", createdAt = stamp, updatedAt = stamp))
                vault.delete(item.id)
                assertNull(items.getById(item.id))
                assertTrue(ref in storage.getAllMediaIds())
                places.update("place", mapOf("photoRef" to null))
                deleteUnreferencedMedia(db, storage, ref)
                assertFalse(ref in storage.getAllMediaIds())
                assertEquals(0, db.mediaAssetDao().count())
            }
        } finally {
            (storage.getAllMediaIds() - before).forEach(storage::delete)
            valid.delete()
            corrupt.delete()
            app.deleteDatabase(name)
        }
    }

    private suspend fun <T> withDb(name: String, block: suspend (TwoHeartsDatabase) -> T): T {
        val db = Room.databaseBuilder(app, TwoHeartsDatabase::class.java, name).build()
        return try { block(db) } finally { db.close() }
    }

    private fun registerFile(authority: String, file: File, type: String): Uri {
        val provider = LocalFixtureProvider(file, type)
        provider.attachInfo(app, ProviderInfo().apply { this.authority = authority })
        ShadowContentResolver.registerProviderInternal(authority, provider)
        return Uri.parse("content://$authority/input")
    }

    private class LocalFixtureProvider(private val file: File, private val type: String) : ContentProvider() {
        override fun onCreate() = true
        override fun getType(uri: Uri) = type
        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor =
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
    }
}
