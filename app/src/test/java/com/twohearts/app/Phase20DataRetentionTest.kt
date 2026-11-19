package com.twohearts.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Note
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase20DataRetentionTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun anExistingLocalRecordSurvivesAReopen() = runBlocking {
        val name = "phase20-current.db"
        val time = "2026-11-19T00:00:00Z"
        val written = TwoHeartsDatabase.buildDatabase(context, name)
        try {
            written.noteDao().insert(Note("Existing note", "Local content", "general", id = "note", createdAt = time, updatedAt = time))
        } finally {
            written.close()
        }
        val reopened = TwoHeartsDatabase.buildDatabase(context, name)
        try {
            assertEquals("Local content", reopened.noteDao().getById("note")?.content)
        } finally {
            reopened.close()
            context.deleteDatabase(name)
        }
        Unit
    }

    @Test
    fun anUnsupportedVersionKeepsItsOriginalTablesAndRows() {
        val name = "phase20-retention.db"
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { existing ->
            existing.execSQL("CREATE TABLE retained (value TEXT NOT NULL)")
            existing.execSQL("INSERT INTO retained (value) VALUES ('existing record')")
            existing.version = 14
        }
        val attempted = TwoHeartsDatabase.buildDatabase(context, name)
        try {
            val problem = assertThrows(IllegalStateException::class.java) { attempted.openHelper.writableDatabase }
            assertTrue(problem.message.orEmpty().contains("14 to 13"))
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { preserved ->
                assertEquals(14, preserved.version)
                preserved.rawQuery("SELECT value FROM retained", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("existing record", cursor.getString(0))
                }
            }
        } finally {
            attempted.close()
            context.deleteDatabase(name)
        }
    }
}
