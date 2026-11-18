package com.twohearts.app.services.datamanagement

import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Reminder
import org.json.JSONArray
import org.json.JSONObject
import java.io.Reader

internal fun encodeTransferDocument(notes: List<Note>, reminders: List<Reminder>): String =
    JSONObject().apply {
        put("format", "twohearts-import")
        put("version", 1)
        put("notes", JSONArray().apply {
            notes.forEach {
                put(JSONObject().put("title", it.title).put("content", it.content).put("category", it.category))
            }
        })
        put("reminders", JSONArray().apply {
            reminders.forEach {
                put(JSONObject().apply {
                    put("title", it.title)
                    put("scheduledDate", it.scheduledDate)
                    put("description", it.description)
                    put("scheduledTime", it.scheduledTime)
                    put("recurrence", it.recurrence)
                    put("status", it.status)
                    put("notificationEnabled", it.notificationEnabled)
                })
            }
        })
    }.toString(2)

private const val MaxTransferChars = 4_194_304

internal fun readTransferDocument(reader: Reader): JSONObject {
    val content = StringBuilder()
    val buffer = CharArray(8_192)
    while (true) {
        val read = reader.read(buffer)
        if (read < 0) break
        require(content.length + read <= MaxTransferChars) { "This JSON file is too large." }
        content.append(buffer, 0, read)
    }
    val json = JSONObject(content.toString())
    // Earlier Android versions accepted unlabelled files in the same shape.
    require(!json.has("format") || json.optString("format") == "twohearts-import") { "Unsupported file format." }
    require(!json.has("version") || json.optInt("version") == 1) { "Unsupported file version." }
    require(json.optJSONArray("notes") != null || json.optJSONArray("reminders") != null) {
        "The file must contain notes or reminders."
    }
    return json
}
