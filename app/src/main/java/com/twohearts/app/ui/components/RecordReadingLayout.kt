package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
internal fun RecordReadingLayout(
    id: String,
    screenTitle: String,
    title: String,
    headingDetail: String,
    body: String?,
    createdAt: String,
    updatedAt: String,
    recordLabel: String,
    deletionDescription: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    deleting: Boolean,
    errorMessage: String?,
) {
    var confirm by rememberSaveable(id) { mutableStateOf(false) }
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Scaffold(topBar = {
        ThTopBar(screenTitle, onBack = onBack, actions = {
            ThIconButton(onEdit, label = "Edit $recordLabel", enabled = !deleting) {
                Icon(ThIcons.Edit, null, tint = MaterialTheme.colorScheme.primary)
            }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(horizontal = spacing.screenGutter, vertical = spacing.space6),
                verticalArrangement = Arrangement.spacedBy(spacing.space5),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    ThEyebrow(headingDetail, color = MaterialTheme.colorScheme.primary)
                    Text(
                        title, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                body?.takeIf(String::isNotBlank)?.let { Text(it, style = ThTextStyles.reading, color = colors.textSecondary) }
                ThDivider()
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                    Text(
                        "Created ${DateTimeHelper.formatDisplay(createdAt)}",
                        style = MaterialTheme.typography.bodySmall, color = colors.textTertiary,
                    )
                    if (updatedAt != createdAt) {
                        Text(
                            "Updated ${DateTimeHelper.formatDisplay(updatedAt)}",
                            style = MaterialTheme.typography.bodySmall, color = colors.textTertiary,
                        )
                    }
                }
                errorMessage?.let { ThStatusBanner(StatusVariant.ERROR, it) }
                ThButton(
                    onClick = { confirm = true }, text = "Delete $recordLabel", loading = deleting,
                    variant = ButtonVariant.DANGER, icon = ThIcons.Trash,
                )
            }
        }
    }
    if (confirm) ConfirmDialog(
        title = "Delete this $recordLabel?", message = deletionDescription, confirmText = "Delete",
        onConfirm = { confirm = false; onDelete() }, onDismiss = { confirm = false },
    )
}
