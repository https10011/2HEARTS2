package com.twohearts.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.delay

enum class ToastVariant { SUCCESS, ERROR, INFO }

data class ToastMessage(val text: String, val variant: ToastVariant)

interface ToastApi {
    fun show(text: String, variant: ToastVariant = ToastVariant.INFO)
    fun success(text: String) = show(text, ToastVariant.SUCCESS)
    fun error(text: String) = show(text, ToastVariant.ERROR)
    fun info(text: String) = show(text, ToastVariant.INFO)
}

private val LocalToastApi = compositionLocalOf<ToastApi> {
    object : ToastApi {
        override fun show(text: String, variant: ToastVariant) = Unit
    }
}

internal class ToastState : ToastApi {
    var message by mutableStateOf<ToastMessage?>(null)
        private set
    var visible by mutableStateOf(false)
        private set
    var revision by mutableIntStateOf(0)
        private set

    override fun show(text: String, variant: ToastVariant) {
        message = ToastMessage(text, variant)
        visible = true
        revision++
    }

    fun dismiss() { visible = false }
}

/** Keeps operation feedback visible as the current destination changes. */
@Composable
internal fun ToastProvider(
    state: ToastState,
    bottomInset: Dp,
    content: @Composable () -> Unit,
) {
    val accessibility = LocalAccessibilityManager.current
    val reduced = LocalTwoHeartsMotion.current.reduced

    LaunchedEffect(state, state.revision) {
        val current = state.message ?: return@LaunchedEffect
        val duration = if (current.variant == ToastVariant.SUCCESS) TwoHeartsTokens.Duration.toastVisible else 4800L
        delay(accessibility?.calculateRecommendedTimeoutMillis(
            duration, containsIcons = true, containsText = true,
        ) ?: duration)
        state.dismiss()
    }

    CompositionLocalProvider(LocalToastApi provides state) {
        Box(Modifier.fillMaxSize()) {
            content()
            AnimatedVisibility(
                visible = state.visible,
                modifier = Modifier.align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(bottom = bottomInset)
                    .padding(horizontal = TwoHeartsTokens.Spacing.screenGutter, vertical = TwoHeartsTokens.Spacing.space3),
                enter = if (reduced) EnterTransition.None else fadeIn(tween(TwoHeartsTokens.Duration.fast.toInt())),
                exit = if (reduced) ExitTransition.None else fadeOut(tween(TwoHeartsTokens.Duration.fast.toInt())),
            ) {
                state.message?.let { FeedbackBanner(it) }
            }
        }
    }
}

@Composable
private fun FeedbackBanner(message: ToastMessage) {
    val variant = when (message.variant) {
        ToastVariant.SUCCESS -> StatusVariant.SUCCESS
        ToastVariant.ERROR -> StatusVariant.ERROR
        ToastVariant.INFO -> StatusVariant.INFO
    }
    ThStatusBanner(
        variant, message.text,
        Modifier
            .semantics {
                liveRegion = if (variant == StatusVariant.ERROR) LiveRegionMode.Assertive else LiveRegionMode.Polite
            }
            .thSoftShadow(RoundedCornerShape(TwoHeartsTokens.Radius.md), TwoHeartsTokens.Elevation.raise),
    )
}

@Composable
fun useToast(): ToastApi = LocalToastApi.current
