package com.twohearts.app.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.services.datamanagement.TransferBatch
import com.twohearts.app.services.datamanagement.encodeTransferDocument
import com.twohearts.app.services.datamanagement.prepareTransfer
import com.twohearts.app.services.datamanagement.writeTransfer
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThLoadingState
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.components.UtilityPage
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImportScreen(
    noteRepository: NoteRepository,
    reminderRepository: ReminderRepository,
    onBack: () -> Unit,
    database: TwoHeartsDatabase,
) {
    val resolver = LocalContext.current.contentResolver
    val scope = rememberCoroutineScope()
    var batch by remember { mutableStateOf<TransferBatch?>(null) }
    var operation by remember { mutableStateOf<TransferOperation?>(null) }
    var status by remember { mutableStateOf<Pair<StatusVariant, String>?>(null) }

    fun run(operationValue: TransferOperation, error: String, block: suspend () -> Unit) {
        if (operation != null) return
        status = null
        operation = operationValue
        scope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                status = StatusVariant.ERROR to error
            } finally {
                operation = null
            }
        }
    }

    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) run(TransferOperation.EXPORT, "Couldn’t write the export. Choose another location and try again.") {
            val notes = noteRepository.observeAll().first()
            val reminders = reminderRepository.observeAll().first()
            withContext(Dispatchers.IO) {
                val json = encodeTransferDocument(notes, reminders)
                checkNotNull(resolver.openOutputStream(uri, "wt")).bufferedWriter(Charsets.UTF_8).use { it.write(json) }
            }
            status = StatusVariant.SUCCESS to "Exported ${transferCounts(notes.size, reminders.size)}."
        }
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) run(TransferOperation.READ, "Couldn’t read a TwoHearts Notes and Reminders JSON file from that location.") {
            batch = withContext(Dispatchers.IO) {
                checkNotNull(resolver.openInputStream(uri)).bufferedReader(Charsets.UTF_8).use { prepareTransfer(it) }
            }
        }
    }
    val back = {
        if (operation == null) {
            if (batch != null) {
                batch = null
                status = null
            } else onBack()
        }
    }
    BackHandler(operation != null || batch != null) { back() }

    TransferContent(
        batch, operation, status, back,
        chooseImport = { importPicker.launch(arrayOf("application/json", "text/plain")) },
        chooseExport = { exportPicker.launch("twohearts-notes-and-reminders.json") },
        cancel = { batch = null; status = null },
        commitImport = {
            val selected = batch
            if (selected != null && selected.canImport) {
                run(TransferOperation.IMPORT, "Couldn’t save these records. The file preview is still available.") {
                    writeTransfer(database, selected, noteRepository, reminderRepository)
                    batch = null
                    val imported = "Imported ${transferCounts(selected.notes.size, selected.reminders.size)}."
                    status = if (selected.skipped == 0) StatusVariant.SUCCESS to imported
                    else StatusVariant.WARNING to "$imported Skipped ${selected.skipped} invalid ${if (selected.skipped == 1) "entry" else "entries"}."
                }
            }
        },
    )
}

internal enum class TransferOperation(val label: String) {
    READ("Reading Notes and Reminders…"), IMPORT("Saving imported records…"), EXPORT("Writing the export…"),
}

@Composable
internal fun TransferContent(
    batch: TransferBatch?, operation: TransferOperation?, status: Pair<StatusVariant, String>?,
    onBack: () -> Unit, chooseImport: () -> Unit, chooseExport: () -> Unit, cancel: () -> Unit, commitImport: () -> Unit,
) {
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    UtilityPage("Import and export", onBack) {
        status?.let { (variant, message) -> ThStatusBanner(variant, message) }
        if (operation != null) {
            ThLoadingState(label = operation.label)
        } else if (batch == null) {
            Surface(color = colors.surfaceBlush, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    Text("Notes and Reminders", style = MaterialTheme.typography.headlineMedium)
                    Text("Move these records with a TwoHearts JSON file.", color = colors.textSecondary)
                }
            }
            UtilityGroup("Import") {
                Column(Modifier.padding(top = spacing.space3), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    Text("Review the file before adding new records to this device.", color = colors.textSecondary)
                    ThButton(chooseImport, text = "Choose JSON file", icon = ThIcons.Folder, full = true)
                }
            }
            UtilityGroup("Export") {
                Column(Modifier.padding(top = spacing.space3), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    Text("Anyone with access to the export file can read it.", color = colors.textSecondary)
                    ThButton(chooseExport, text = "Export Notes and Reminders", icon = ThIcons.File, variant = ButtonVariant.SECONDARY, full = true)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                ThEyebrow("Review file", color = colors.roseDeep)
                Text("Import Notes and Reminders", style = MaterialTheme.typography.headlineMedium)
                Text("${transferCounts(batch.notes.size, batch.reminders.size)} ready to add", color = colors.textSecondary)
                if (!batch.canImport) ThStatusBanner(StatusVariant.INFO, "This file has no valid records to import.")
            }
            if (batch.skipped > 0) {
                UtilityGroup("Skipped entries") {
                    Column(Modifier.padding(vertical = spacing.space3), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                        Text("${batch.skipped} invalid ${if (batch.skipped == 1) "entry" else "entries"}", style = MaterialTheme.typography.titleMedium)
                        batch.examples.forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary) }
                    }
                }
            }
            ThButton(commitImport, text = "Add records", icon = ThIcons.Check, full = true, enabled = batch.canImport)
            ThButton(cancel, text = "Cancel", variant = ButtonVariant.GHOST)
        }
    }
}

private fun transferCounts(notes: Int, reminders: Int): String =
    "$notes ${if (notes == 1) "note" else "notes"} and $reminders ${if (reminders == 1) "reminder" else "reminders"}"
