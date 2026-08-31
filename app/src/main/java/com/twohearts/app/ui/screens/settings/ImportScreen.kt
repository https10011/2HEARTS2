package com.twohearts.app.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.data.repository.generateId
import org.json.JSONArray
import org.json.JSONObject

/**
 * ImportScreen — JSON import for notes and reminders.
 *
 * Matches legacy ImportScreen with:
 * - JSON file picker
 * - Import preview
 * - Import execution with partial failure tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    noteRepository: NoteRepository,
    reminderRepository: ReminderRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var importState by remember { mutableStateOf<ImportState>(ImportState.Idle) }
    var importPreview by remember { mutableStateOf<ImportPreview?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedUri = it
            importState = ImportState.Loading
            importPreview = parseImportFile(context, it)
            importState = ImportState.Preview
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (importState) {
                is ImportState.Idle -> {
                    Spacer(modifier = Modifier.height(48.dp))

                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Import",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Import your data",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Import notes and reminders from a JSON file.\n\nSupported format: twohearts-import v1",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            filePicker.launch(arrayOf("application/json", "text/plain"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Select File",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select JSON File")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Import creates new records. Existing data is not affected.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is ImportState.Loading -> {
                    Spacer(modifier = Modifier.height(48.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Reading file...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ImportState.Preview -> {
                    val preview = importPreview

                    if (preview != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Import Preview",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Notes to import: ${preview.noteCount}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Reminders to import: ${preview.reminderCount}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (preview.errors.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Errors:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    preview.errors.forEach { error ->
                                        Text(
                                            text = "• $error",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    importState = ImportState.Idle
                                    importPreview = null
                                    selectedUri = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    importState = ImportState.Importing
                                    val result = executeImport(
                                        context,
                                        selectedUri!!,
                                        noteRepository,
                                        reminderRepository
                                    )
                                    importState = if (result.success) {
                                        ImportState.Success(result)
                                    } else {
                                        ImportState.Error(result.errors)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                enabled = preview.noteCount > 0 || preview.reminderCount > 0
                            ) {
                                Text("Import")
                            }
                        }
                    }
                }

                is ImportState.Importing -> {
                    Spacer(modifier = Modifier.height(48.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Importing data...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ImportState.Success -> {
                    Spacer(modifier = Modifier.height(48.dp))

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Import Complete!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val result = (importState as ImportState.Success).result
                    Text(
                        text = "Notes imported: ${result.notesImported}\nReminders imported: ${result.remindersImported}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (result.errors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Partial failures:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                result.errors.forEach { error ->
                                    Text(
                                        text = "• $error",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done")
                    }
                }

                is ImportState.Error -> {
                    Spacer(modifier = Modifier.height(48.dp))

                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Import Failed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val errors = (importState as ImportState.Error).errors
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            errors.forEach { error ->
                                Text(
                                    text = "• $error",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            importState = ImportState.Idle
                            importPreview = null
                            selectedUri = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    object Preview : ImportState()
    object Importing : ImportState()
    data class Success(val result: ImportResult) : ImportState()
    data class Error(val errors: List<String>) : ImportState()
}

data class ImportPreview(
    val noteCount: Int,
    val reminderCount: Int,
    val errors: List<String>
)

data class ImportResult(
    val success: Boolean,
    val notesImported: Int,
    val remindersImported: Int,
    val errors: List<String>
)

private fun parseImportFile(context: android.content.Context, uri: Uri): ImportPreview? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val content = inputStream.bufferedReader().use { it.readText() }
        inputStream.close()

        val json = JSONObject(content)
        val notes = json.optJSONArray("notes") ?: JSONArray()
        val reminders = json.optJSONArray("reminders") ?: JSONArray()

        val errors = mutableListOf<String>()

        // Validate notes
        for (i in 0 until notes.length()) {
            val note = notes.getJSONObject(i)
            if (!note.has("title") || !note.has("content")) {
                errors.add("Note ${i + 1}: missing title or content")
            }
        }

        // Validate reminders
        for (i in 0 until reminders.length()) {
            val reminder = reminders.getJSONObject(i)
            if (!reminder.has("title") || !reminder.has("scheduledDate")) {
                errors.add("Reminder ${i + 1}: missing title or scheduledDate")
            }
        }

        ImportPreview(
            noteCount = notes.length(),
            reminderCount = reminders.length(),
            errors = errors
        )
    } catch (e: Exception) {
        ImportPreview(0, 0, listOf("Failed to parse file: ${e.message}"))
    }
}

private suspend fun executeImport(
    context: android.content.Context,
    uri: Uri,
    noteRepository: NoteRepository,
    reminderRepository: ReminderRepository
): ImportResult {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Failed to open file")
        val content = inputStream.bufferedReader().use { it.readText() }
        inputStream.close()

        val json = JSONObject(content)
        val notes = json.optJSONArray("notes") ?: JSONArray()
        val reminders = json.optJSONArray("reminders") ?: JSONArray()

        var notesImported = 0
        var remindersImported = 0
        val errors = mutableListOf<String>()

        // Import notes
        for (i in 0 until notes.length()) {
            try {
                val note = notes.getJSONObject(i)
                noteRepository.create(
                    Note(
                        id = generateId(),
                        title = note.getString("title"),
                        content = note.getString("content"),
                        category = note.optString("category", "general"),
                        createdAt = DateTimeHelper.nowUtc(),
                        updatedAt = DateTimeHelper.nowUtc()
                    )
                )
                notesImported++
            } catch (e: Exception) {
                errors.add("Failed to import note ${i + 1}: ${e.message}")
            }
        }

        // Import reminders
        for (i in 0 until reminders.length()) {
            try {
                val reminder = reminders.getJSONObject(i)
                reminderRepository.create(
                    Reminder(
                        id = generateId(),
                        title = reminder.getString("title"),
                        description = reminder.optString("description", ""),
                        scheduledDate = reminder.getString("scheduledDate"),
                        scheduledTime = reminder.optString("scheduledTime", ""),
                        recurrence = reminder.optString("recurrence", "none"),
                        createdAt = DateTimeHelper.nowUtc(),
                        updatedAt = DateTimeHelper.nowUtc()
                    )
                )
                remindersImported++
            } catch (e: Exception) {
                errors.add("Failed to import reminder ${i + 1}: ${e.message}")
            }
        }

        ImportResult(
            success = errors.isEmpty(),
            notesImported = notesImported,
            remindersImported = remindersImported,
            errors = errors
        )
    } catch (e: Exception) {
        ImportResult(
            success = false,
            notesImported = 0,
            remindersImported = 0,
            errors = listOf("Import failed: ${e.message}")
        )
    }
}
