package com.twohearts.app.services.datamanagement

import androidx.room.withTransaction
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.data.repository.generateId
import com.twohearts.app.services.datetime.DateTimeHelper
import java.io.Reader
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

internal data class TransferBatch(
    val notes: List<Note>,
    val reminders: List<Reminder>,
    val skipped: Int,
    val examples: List<String>,
) {
    val canImport: Boolean get() = notes.isNotEmpty() || reminders.isNotEmpty()
}

/** The preview and the write use the same parsed records, even if the provider file changes. */
internal fun prepareTransfer(reader: Reader, timestamp: String = DateTimeHelper.nowUtc()): TransferBatch {
    val document = readTransferDocument(reader)
    val notes = mutableListOf<Note>()
    val reminders = mutableListOf<Reminder>()
    val examples = mutableListOf<String>()
    var skipped = 0
    fun skip(message: String) {
        skipped++
        if (examples.size < 6) examples += message
    }

    val noteRows = document.optJSONArray("notes") ?: JSONArray()
    for (index in 0 until noteRows.length()) {
        val row = noteRows.optJSONObject(index)
        val title = row?.savedText("title")
        val content = row?.savedText("content")
        if (title == null || content == null) {
            skip("Note ${index + 1} needs a title and content.")
        } else {
            notes += Note(title, content, row.savedText("category") ?: "general", generateId(), timestamp, timestamp)
        }
    }

    val reminderRows = document.optJSONArray("reminders") ?: JSONArray()
    for (index in 0 until reminderRows.length()) {
        val row = reminderRows.optJSONObject(index)
        val title = row?.savedText("title")
        val date = row?.savedText("scheduledDate")
        if (title == null || date == null || runCatching { LocalDate.parse(date) }.isFailure) {
            skip("Reminder ${index + 1} needs a title and valid date.")
        } else {
            reminders += Reminder(
                title = title, description = row.savedText("description"), scheduledDate = date,
                scheduledTime = row.savedText("scheduledTime"), recurrence = row.savedText("recurrence") ?: "none",
                status = row.savedText("status")?.takeIf { it in reminderStatuses } ?: "pending",
                notificationEnabled = row.optBoolean("notificationEnabled", true),
                id = generateId(), createdAt = timestamp, updatedAt = timestamp,
            )
        }
    }

    return TransferBatch(notes, reminders, skipped, examples)
}

internal suspend fun writeTransfer(
    database: TwoHeartsDatabase,
    batch: TransferBatch,
    notes: NoteRepository,
    reminders: ReminderRepository,
) = database.withTransaction {
    batch.notes.forEach { notes.create(it) }
    batch.reminders.forEach { reminders.create(it) }
}

private fun JSONObject.savedText(key: String): String? = (opt(key) as? String)?.takeIf(String::isNotBlank)

private val reminderStatuses = setOf("pending", "completed", "missed")
