package com.twohearts.app.ui.screens.yuki

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.game.*
import com.twohearts.app.ui.components.*
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.Instant
import kotlinx.coroutines.delay

@Composable
fun YukiScreen(viewModel: YukiViewModel, onBack: () -> Unit) {
    val presentation by viewModel.presentation.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val visible by lifecycle.currentStateFlow.collectAsState()
    val active = visible.isAtLeast(Lifecycle.State.RESUMED)
    val state = presentation.state
    val view = LocalView.current
    var lastLevel by remember { mutableStateOf(state?.level) }

    LaunchedEffect(state?.level) {
        val level = state?.level ?: return@LaunchedEffect
        val previous = lastLevel
        lastLevel = level
        if (active && previous != null && level > previous && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }

    LaunchedEffect(active) {
        if (active) viewModel.refresh()
    }
    LaunchedEffect(active, state, presentation.failed) {
        if (!active || state == null || presentation.failed) return@LaunchedEffect
        while (true) {
            delay(state.nextUpdateDelayMillis(Instant.now()))
            viewModel.refresh()
        }
    }

    if (state == null) {
        Scaffold(topBar = { Header(title = "Yuki", onBack = onBack) }) { insets ->
            if (presentation.failed) {
                ThEmptyState(
                    title = "Yuki’s save could not load",
                    description = "Try opening the room again.",
                    modifier = Modifier.padding(insets), actionLabel = "Try again", onAction = viewModel::refresh,
                )
            } else ThLoadingState(label = "Opening Yuki’s room", modifier = Modifier.padding(insets))
        }
    } else {
        YukiHub(
            state, presentation.feedback, presentation.working, presentation.failed, active,
            onBack, viewModel::processAction, viewModel::selectToy, viewModel::equipAccessory,
            refusal = presentation.refusal,
        )
    }
}

@Composable
internal fun YukiHub(
    state: YukiState,
    feedback: String?,
    working: Boolean,
    failed: Boolean,
    animate: Boolean,
    onBack: () -> Unit,
    onAction: (YukiAction) -> Unit,
    onToy: (YukiToy) -> Unit,
    onAccessory: (String) -> Unit,
    refusal: Int = 0,
) {
    var shelf by rememberSaveable { mutableStateOf(false) }
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    val sleeping = state.activity == YukiActivity.SLEEPING
    val busy = working || state.activity.durationMs > 0
    val clothing = getAccessoryById(state.accessory)
    val mood = if (state.activity == YukiActivity.IDLE) yukiMood(state).description else state.activity.label

    UtilityPage("Yuki’s room", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
            YukiRoom(
                state, animate && !shelf, refusal = refusal,
                onPet = if (busy || sleeping) null else { { onAction(YukiAction.PET) } },
            )
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                Text(
                    state.name, style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    mood, style = MaterialTheme.typography.titleMedium, color = colors.roseDeep,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                if (clothing != null && clothing.id != "none") {
                    Text("Wearing ${clothing.name.lowercase()}", color = colors.textSecondary)
                }
                if (feedback != null) Text(
                    feedback, Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (failed) ThStatusBanner(StatusVariant.ERROR, "Couldn’t save this change. Try again.")
        YukiActions(sleeping, busy, onAction)
        YukiNeeds(state)
        UtilityGroup("Keepsakes") {
            UtilityItem("Toy basket and keepsakes", ThIcons.Star, "Companion level ${state.level}") { shelf = true }
        }
    }
    if (shelf) YukiShelf(state, !busy, onToy, onAccessory, onDismiss = { shelf = false })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YukiNeeds(state: YukiState) {
    val spacing = TwoHeartsTokens.Spacing
    val entries = listOf(
        "Food" to when { state.hunger < 40 -> "Ready to eat"; state.hunger >= 70 -> "Well fed"; else -> "Comfortable" },
        "Energy" to when { state.energy < 30 -> "Sleepy"; state.energy >= 70 -> "Rested"; else -> "Taking it easy" },
        "Mood" to when { state.happiness < 45 -> "Quiet"; state.happiness >= 75 -> "Cheerful"; else -> "Content" },
    )
    UtilityGroup("How Yuki feels") {
        FlowRow(
            Modifier.fillMaxWidth().padding(top = spacing.space3),
            horizontalArrangement = Arrangement.spacedBy(spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            entries.forEach { (name, status) ->
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                    Text(name, style = MaterialTheme.typography.labelMedium, color = LocalTwoHeartsColors.current.textSecondary)
                    Text(status, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
