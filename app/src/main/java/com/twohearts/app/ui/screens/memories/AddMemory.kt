package com.twohearts.app.ui.screens.memories

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemory(
    memoryId: String? = null, initialTitle: String = "", initialCaption: String = "", initialDate: String = DateTimeHelper.todayLocal(),
    onSave: (String, String?, String, List<Uri>) -> Unit, onBack: () -> Unit, saving: Boolean = false,
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var caption by remember(initialCaption) { mutableStateOf(initialCaption) }
    var memoryDate by remember(initialDate) { mutableStateOf(initialDate) }
    var selected by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var attempted by remember { mutableStateOf(false) }
    val validDate = runCatching { LocalDate.parse(memoryDate) }.isSuccess
    val canSave = title.isNotBlank() && validDate && !saving
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { selected = it }
    val colors = LocalTwoHeartsColors.current
    val save = { attempted = true; if (canSave) onSave(title.trim(), caption.trim().ifEmpty { null }, memoryDate, selected) }

    Scaffold(topBar = { Header(title = if (memoryId == null) "New memory" else "Edit memory", onBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Keep the feeling, not just the facts.", style = MaterialTheme.typography.titleLarge)
            Text("Photos and videos stay in TwoHearts on this device.", color = colors.textSecondary)
            OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }, modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp)) {
                Icon(ThIcons.PhotoLibrary, null); Spacer(Modifier.width(10.dp)); Text(if (selected.isEmpty()) "Add photos or videos" else "${selected.size} selected · Change")
            }
            if (selected.isNotEmpty()) Text("Selected media is copied into private app storage when you save.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            Input(title, { title = it }, Modifier.fillMaxWidth(), label = "Title", placeholder = "A moment worth remembering")
            Input(caption, { caption = it }, Modifier.fillMaxWidth(), label = "What do you remember? (optional)", placeholder = "The little details…", multiline = true)
            Input(memoryDate, { memoryDate = it }, Modifier.fillMaxWidth(), label = "Date", placeholder = "yyyy-mm-dd")
            if (attempted && title.isBlank()) Text("Give this memory a title.", color = MaterialTheme.colorScheme.error)
            if (attempted && !validDate) Text("Use a valid date in yyyy-mm-dd format.", color = MaterialTheme.colorScheme.error)
            Button(onClick = save, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Icon(ThIcons.Check, null); Spacer(Modifier.width(8.dp)); Text(if (memoryId == null) "Save memory" else "Save changes") }
        }
    }
}
