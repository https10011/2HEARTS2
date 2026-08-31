package com.twohearts.app.ui.screens.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.twohearts.app.services.datamanagement.DataManagementService

/**
 * StorageSettingsScreen — storage report, cache clear, and data reset.
 *
 * Matches legacy StorageSettingsScreen with:
 * - Storage usage report (row counts, media bytes)
 * - Cache clear (orphan media sweep)
 * - Full reset (destructive, with confirmation)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageSettingsScreen(
    dataManagementService: DataManagementService,
    onBack: () -> Unit
) {
    var storageReport by remember { mutableStateOf<com.twohearts.app.services.datamanagement.StorageReport?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var cacheCleared by remember { mutableStateOf(false) }

    // Load storage report on first composition
    LaunchedEffect(Unit) {
        storageReport = dataManagementService.getStorageReport()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage") },
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
        ) {
            // Storage Report Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Storage",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Storage Usage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (storageReport != null) {
                        val report = storageReport!!
                        StorageReportItem("Profiles", "${report.profileCount}")
                        StorageReportItem("Memories", "${report.memoryCount}")
                        StorageReportItem("Notes", "${report.noteCount}")
                        StorageReportItem("Timeline Events", "${report.timelineEventCount}")
                        StorageReportItem("Reminders", "${report.reminderCount}")
                        StorageReportItem("Places", "${report.placeCount}")
                        StorageReportItem("Mood Entries", "${report.moodEntryCount}")
                        StorageReportItem("Period Entries", "${report.periodEntryCount}")
                        StorageReportItem("Vault Items", "${report.vaultItemCount}")
                        StorageReportItem("Notifications", "${report.notificationCount}")
                        StorageReportItem("Media Files", "${report.mediaAssetCount}")
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        StorageReportItem("Media Size", formatBytes(report.mediaBytes))
                        StorageReportItem("Total App Size", formatBytes(report.totalAppSize))
                    } else {
                        Text(
                            text = "Loading storage report...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Cache Clear Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear Cache",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Remove orphaned media files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (cacheCleared) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Cleared",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                kotlinx.coroutines.runBlocking {
                                    dataManagementService.clearCache()
                                    cacheCleared = true
                                    isLoading = false
                                }
                            },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Clear")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Danger Zone
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFA33A2A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Danger Zone",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA33A2A)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Reset all app data. This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA33A2A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Reset",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Data")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Data?") },
            text = {
                Text("This will permanently delete all your data including notes, memories, reminders, photos, and settings. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        kotlinx.coroutines.runBlocking {
                            dataManagementService.fullReset()
                        }
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFA33A2A)
                    )
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StorageReportItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
