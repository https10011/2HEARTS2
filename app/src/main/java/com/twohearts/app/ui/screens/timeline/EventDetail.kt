package com.twohearts.app.ui.screens.timeline

import androidx.compose.runtime.Composable
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.RecordReadingLayout
import java.time.LocalDate

@Composable
fun EventDetail(
    event: TimelineEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    deleting: Boolean = false,
    errorMessage: String? = null,
) {
    val date = runCatching { LocalDate.parse(event.eventDate) }.getOrNull()
    val dateLabel = date?.let { DateTimeHelper.formatDisplay(it.toString()) }
        ?: event.eventDate.ifBlank { "Date unavailable" }
    RecordReadingLayout(
        id = event.id, screenTitle = "Timeline moment", title = event.title,
        headingDetail = dateLabel, body = event.description,
        createdAt = event.createdAt, updatedAt = event.updatedAt, recordLabel = "moment",
        deletionDescription = "This permanently removes the moment from your timeline.",
        onEdit = onEdit, onDelete = onDelete, onBack = onBack, deleting = deleting, errorMessage = errorMessage,
    )
}
