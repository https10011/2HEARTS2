package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/** A modal sheet with its own window so background controls cannot receive focus. */
@Composable
fun ThModal(
    open: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Dialog",
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!open) return

    val palette = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(topStart = TwoHeartsTokens.Radius.xl, topEnd = TwoHeartsTokens.Radius.xl)
    val density = LocalDensity.current
    val foreground = MaterialTheme.colorScheme.onSurface
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) {
        val dialog = LocalView.current.parent as? DialogWindowProvider
        SideEffect { dialog?.window?.setDimAmount(0f) }

        Column(Modifier.fillMaxSize().background(palette.overlay)) {
            Spacer(
                Modifier.fillMaxWidth()
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose,
                    )
                    .clearAndSetSemantics {},
            )
            CompositionLocalProvider(LocalDensity provides density, LocalContentColor provides foreground) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(palette.surfaceElevated)
                        .border(TwoHeartsTokens.Border.hairline, palette.borderSubtle, shape)
                        .semantics { paneTitle = label }
                        .padding(TwoHeartsTokens.Spacing.space4),
                    content = content,
                )
            }
        }
    }
}
