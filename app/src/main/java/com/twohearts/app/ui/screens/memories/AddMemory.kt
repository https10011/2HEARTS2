package com.twohearts.app.ui.screens.memories

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.services.media.MemoryAttachmentChoice
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.components.ThQuietButton
import com.twohearts.app.ui.components.ThSectionHeader
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate

private val attachmentChoicesSaver = listSaver<List<MemoryAttachmentChoice>, String>(
    save = { entries -> entries.map {
        when (it) {
            is MemoryAttachmentChoice.Saved -> "saved:${it.id}"
            is MemoryAttachmentChoice.Picked -> "picked:${it.uri}"
        }
    } },
    restore = { entries -> entries.map {
        if (it.startsWith("saved:")) MemoryAttachmentChoice.Saved(it.removePrefix("saved:"))
        else MemoryAttachmentChoice.Picked(Uri.parse(it.removePrefix("picked:")))
    } },
)

internal fun mergeMemoryPicks(
    choices: List<MemoryAttachmentChoice>,
    picked: List<Uri>,
): List<MemoryAttachmentChoice> = (choices + picked.map { MemoryAttachmentChoice.Picked(it) }).distinct()

internal fun replaceMemoryPick(
    choices: List<MemoryAttachmentChoice>, index: Int?, picked: Uri?,
): List<MemoryAttachmentChoice> {
    if (index == null || index !in choices.indices || picked == null) return choices
    return choices.mapIndexed { position, choice ->
        if (position == index) MemoryAttachmentChoice.Picked(picked) else choice
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddMemory(
    memoryId: String? = null,
    initialTitle: String = "",
    initialCaption: String = "",
    initialDate: String = DateTimeHelper.todayLocal(),
    onSave: (String, String?, String, List<MemoryAttachmentChoice>) -> Unit,
    onBack: () -> Unit,
    saving: Boolean = false,
    initialMedia: List<MediaAsset> = emptyList(),
    errorMessage: String? = null,
) {
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    val resolver = LocalContext.current.contentResolver
    var title by rememberSaveable(memoryId) { mutableStateOf(initialTitle) }
    var caption by rememberSaveable(memoryId) { mutableStateOf(initialCaption) }
    var date by rememberSaveable(memoryId) { mutableStateOf(initialDate) }
    var attempted by rememberSaveable(memoryId) { mutableStateOf(false) }
    var replaceIndex by rememberSaveable(memoryId) { mutableStateOf<Int?>(null) }
    var previewIndex by rememberSaveable(memoryId) { mutableStateOf<Int?>(null) }
    var attachments by rememberSaveable(memoryId, stateSaver = attachmentChoicesSaver) {
        mutableStateOf(initialMedia.map<MediaAsset, MemoryAttachmentChoice> { MemoryAttachmentChoice.Saved(it.id) })
    }
    val validDate = runCatching { LocalDate.parse(date.trim()) }.isSuccess
    val picker = rememberLauncherForActivityResult(PickMultipleVisualMedia(10)) {
        attachments = mergeMemoryPicks(attachments, it)
    }
    val replacement = rememberLauncherForActivityResult(PickVisualMedia()) {
        attachments = replaceMemoryPick(attachments, replaceIndex, it)
        replaceIndex = null
    }
    val request = PickVisualMediaRequest(PickVisualMedia.ImageAndVideo)
    val editing = memoryId != null
    fun sourceOf(choice: MemoryAttachmentChoice): MemoryMediaSource = when (choice) {
        is MemoryAttachmentChoice.Saved -> MemoryMediaSource(asset = initialMedia.firstOrNull { it.id == choice.id })
        is MemoryAttachmentChoice.Picked -> MemoryMediaSource(
            uri = choice.uri,
            mimeType = runCatching { resolver.getType(choice.uri) }.getOrNull(),
        )
    }

    Scaffold(
        topBar = { ThTopBar(if (editing) "Edit memory" else "New memory", onBack = onBack) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenGutter, vertical = spacing.space5),
            verticalArrangement = Arrangement.spacedBy(spacing.space5),
        ) {
            ThInput(
                title, { title = it }, label = "Title", placeholder = "A moment worth remembering",
                enabled = !saving, error = "Give this memory a title.".takeIf { attempted && title.isBlank() },
            )
            ThInput(
                caption, { caption = it }, label = "Caption (optional)", placeholder = "The little details",
                enabled = !saving, multiline = true,
            )
            ThInput(
                date, { date = it }, label = "Date (YYYY-MM-DD)", enabled = !saving,
                error = "Enter a valid date.".takeIf { attempted && !validDate },
            )
            ThDivider()
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                ThSectionHeader("Photos and videos")
                if (attachments.isEmpty()) {
                    Text(
                        "Optional. TwoHearts keeps copies on this device.",
                        style = MaterialTheme.typography.bodySmall, color = colors.textSecondary,
                    )
                }
                attachments.forEachIndexed { index, attachment ->
                    val source = remember(attachment, initialMedia, resolver) { sourceOf(attachment) }
                    val kind = when {
                        source.isVideo -> "Video"
                        source.isPhoto -> "Photo"
                        else -> "Attachment"
                    }
                    Row(
                        Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                        verticalAlignment = Alignment.Top,
                    ) {
                        MemoryMediaFrame(source, Modifier.size(68.dp))
                        Column(Modifier.weight(1f)) {
                            Text("$kind ${index + 1}", color = MaterialTheme.colorScheme.onBackground)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.space1)) {
                                ThQuietButton(onClick = { previewIndex = index }, text = "Preview", enabled = !saving)
                                ThQuietButton(onClick = {
                                    replaceIndex = index
                                    replacement.launch(request)
                                }, text = "Replace", enabled = !saving)
                                ThQuietButton(onClick = {
                                    attachments = attachments.filterIndexed { position, _ -> position != index }
                                }, text = "Remove", enabled = !saving)
                            }
                        }
                    }
                    ThDivider()
                }
                ThButton(
                    onClick = { picker.launch(request) }, variant = ButtonVariant.SECONDARY,
                    icon = ThIcons.PhotoLibrary, text = "Add photos or videos", enabled = !saving, full = true,
                )
            }
            errorMessage?.let { ThStatusBanner(StatusVariant.ERROR, it) }
            ThButton(
                onClick = {
                    attempted = true
                    if (title.isNotBlank() && validDate) {
                        onSave(title.trim(), caption.trim().ifEmpty { null }, date.trim(), attachments)
                    }
                },
                full = true, loading = saving, icon = ThIcons.Check,
                text = if (editing) "Save changes" else "Save memory",
            )
        }
    }
    previewIndex?.let { index ->
        attachments.getOrNull(index)?.let { MemoryAttachmentPreview(sourceOf(it)) { previewIndex = null } }
    }
}
