package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
internal fun RecordEditorLayout(
    title: String,
    saveText: String,
    onSave: () -> Unit,
    onBack: () -> Unit,
    saving: Boolean,
    errorMessage: String?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = { ThTopBar(title, onBack = onBack) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                ThDivider()
                ThButton(
                    onClick = onSave, text = saveText, loading = saving, full = true, icon = ThIcons.Check,
                    modifier = Modifier.padding(horizontal = spacing.screenGutter, vertical = spacing.space2),
                )
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenGutter, vertical = spacing.space5),
            verticalArrangement = Arrangement.spacedBy(spacing.space5),
        ) {
            errorMessage?.let { ThStatusBanner(StatusVariant.ERROR, it) }
            content()
        }
    }
}
