package com.twohearts.app.ui.screens.yuki

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.game.*
import com.twohearts.app.ui.components.*
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
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

@OptIn(ExperimentalLayoutApi::class)
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
    val sleeping = state.activity == YukiActivity.SLEEPING
    val busy = working || state.activity.durationMs > 0
    val clothing = getAccessoryById(state.accessory)
    val mood = if (state.activity == YukiActivity.IDLE) yukiMood(state).description else state.activity.label

    Scaffold(topBar = { Header(title = "Yuki’s room", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            YukiRoom(state, animate, refusal = refusal, onPet = if (busy || sleeping) null else { { onAction(YukiAction.PET) } })
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(state.name, style = MaterialTheme.typography.headlineMedium)
                Text(mood, style = MaterialTheme.typography.titleMedium, color = colors.roseDeep)
                if (clothing != null) Text("Wearing ${clothing.name.lowercase()}", color = colors.textSecondary)
            }
            if (failed) ThStatusBanner(StatusVariant.ERROR, "Couldn’t save this change. Try again.")
            if (feedback != null) Text(
                feedback, Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium,
            )

            YukiActions(sleeping, busy, onAction)
            HorizontalDivider()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                NeedLabel("Food", state.hunger, "Ready to eat", "Well fed")
                NeedLabel("Energy", state.energy, "Tired", "Rested")
                NeedLabel("Mood", state.happiness, "Quiet", "Cheerful")
            }
            TextButton(onClick = { shelf = true }) {
                Icon(ThIcons.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Toy basket and keepsakes")
            }
        }
    }
    if (shelf) {
        YukiShelf(
            state = state, enabled = !busy, onToy = onToy, onAccessory = onAccessory,
            onDismiss = { shelf = false },
        )
    }
}

@Composable
private fun NeedLabel(label: String, value: Int, low: String, high: String) {
    Surface(shape = MaterialTheme.shapes.small, color = LocalTwoHeartsColors.current.surfaceWarm) {
        Text(
            "$label · ${if (value < 40) low else high}",
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun YukiShelf(
    state: YukiState,
    enabled: Boolean,
    onToy: (YukiToy) -> Unit,
    onAccessory: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yuki’s keepsakes") },
        text = {
            Column(
                Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Companion level ${state.level}", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = { (state.experience.toFloat() / state.xpToNextLevel).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.totalPets > 0) Text("You’ve petted Yuki ${state.totalPets} ${if (state.totalPets == 1) "time" else "times"}.")
                Text("Choose a toy", style = MaterialTheme.typography.titleMedium)
                Column(Modifier.selectableGroup()) {
                    YukiToy.entries.forEach { toy ->
                        val unlocked = toy.unlockLevel <= state.level
                        ShelfChoice(
                            if (unlocked) toy.label else "${toy.label} · Level ${toy.unlockLevel}",
                            state.selectedToy == toy, enabled && unlocked,
                        ) { onToy(toy) }
                    }
                }
                Text("Accessories", style = MaterialTheme.typography.titleMedium)
                Column(Modifier.selectableGroup()) {
                    YUKI_ACCESSORIES.filter { it.id in state.ownedAccessories }.forEach { accessory ->
                        ShelfChoice(
                            if (accessory.visual.isBlank()) accessory.name else "${accessory.visual} ${accessory.name}",
                            (state.accessory ?: "none") == accessory.id, enabled,
                        ) { onAccessory(accessory.id) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

@Composable
private fun ShelfChoice(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(
            selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick,
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Text(label, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
