package com.twohearts.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.delay

/**
 * Toast — the ONE system-wide transient feedback channel.
 *
 * Matches legacy toast.tsx behavior:
 * - Auto-dismiss after TOAST_DURATION (2400ms)
 * - Variants: success / error / info
 * - State color + stable icon
 */

/** Time a toast stays fully visible. */
const val TOAST_DURATION_MS = 2400L

enum class ToastVariant {
    SUCCESS,
    ERROR,
    INFO,
}

data class ToastMessage(
    val text: String,
    val variant: ToastVariant,
)

interface ToastApi {
    fun show(text: String, variant: ToastVariant = ToastVariant.INFO)
    fun success(text: String)
    fun error(text: String)
    fun info(text: String)
}

val LocalToastApi = compositionLocalOf<ToastApi> {
    // No-op fallback for screens outside the toast host
    object : ToastApi {
        override fun show(text: String, variant: ToastVariant) {}
        override fun success(text: String) {}
        override fun error(text: String) {}
        override fun info(text: String) {}
    }
}

/**
 * ToastProvider — provides toast API to the composable tree.
 * Mount ONE instance in AppShell (above bottom nav).
 */
@Composable
fun ToastProvider(
    content: @Composable () -> Unit,
) {
    var message by remember { mutableStateOf<ToastMessage?>(null) }

    val api = remember {
        object : ToastApi {
            override fun show(text: String, variant: ToastVariant) {
                message = ToastMessage(text, variant)
            }
            override fun success(text: String) = show(text, ToastVariant.SUCCESS)
            override fun error(text: String) = show(text, ToastVariant.ERROR)
            override fun info(text: String) = show(text, ToastVariant.INFO)
        }
    }

    // Auto-dismiss after duration
    LaunchedEffect(message) {
        if (message != null) {
            delay(TOAST_DURATION_MS)
            message = null
        }
    }

    CompositionLocalProvider(LocalToastApi provides api) {
        content()

        // Toast viewport
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            message?.let { msg ->
                ToastViewport(msg)
            }
        }
    }
}

@Composable
private fun ToastViewport(message: ToastMessage) {
    val thColors = LocalTwoHeartsColors.current
    val bgColor = when (message.variant) {
        ToastVariant.SUCCESS -> thColors.success
        ToastVariant.ERROR -> thColors.error
        ToastVariant.INFO -> MaterialTheme.colorScheme.primary
    }
    val icon = when (message.variant) {
        ToastVariant.SUCCESS -> "✓"
        ToastVariant.ERROR -> "✕"
        ToastVariant.INFO -> "ℹ"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TwoHeartsTokens.Spacing.space4)
            .background(bgColor, RoundedCornerShape(TwoHeartsTokens.Radius.md))
            .padding(TwoHeartsTokens.Spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space2),
    ) {
        Text(
            text = icon,
            color = Color.White,
        )
        Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
    }
}

/**
 * Composable accessor for the toast API.
 * Usage: val toast = LocalToastApi.current; toast.success("Saved!")
 */
@Composable
fun useToast(): ToastApi = LocalToastApi.current
