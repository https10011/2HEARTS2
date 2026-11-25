package com.twohearts.app.ui.screens.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.components.UtilityPage
import com.twohearts.app.ui.components.useToast
import com.twohearts.app.ui.screens.settings.UtilityToggle
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private enum class PinAction(val title: String, val confirm: String) {
    CREATE("Set an app PIN", "Enable lock"),
    CHANGE("Change your PIN", "Save PIN"),
    DISABLE("Turn off app lock?", "Turn off"),
}

@Composable
fun SecuritySettingsScreen(
    isAppLockEnabled: Boolean,
    onCreatePin: suspend (String) -> Unit,
    onChangePin: suspend (String, String) -> Boolean,
    onDisableLock: suspend (String) -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var action by remember { mutableStateOf<PinAction?>(null) }
    val feedback = useToast()
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    UtilityPage("Privacy and security", onBack, modifier) {
        Surface(Modifier.fillMaxWidth(), color = colors.surfaceWarm, shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                Icon(ThIcons.Lock, null, tint = colors.roseDeep)
                ThEyebrow("App access", color = colors.roseDeep)
                Text(
                    if (isAppLockEnabled) "Your app lock is on" else "Your app lock is off",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text("Use your PIN when reopening TwoHearts.", color = colors.textSecondary)
            }
        }
        UtilityGroup("PIN controls") {
            UtilityToggle("App lock", "Require a PIN to open TwoHearts.", isAppLockEnabled) {
                action = if (it) PinAction.CREATE else PinAction.DISABLE
            }
            if (isAppLockEnabled) ThButton(
                { action = PinAction.CHANGE }, variant = ButtonVariant.SECONDARY,
                icon = ThIcons.Lock, text = "Change PIN",
            )
        }
        UtilityGroup("Private Vault") {
            Text(
                "Vault items stay on this device. App lock controls access to the Vault and the rest of TwoHearts.",
                modifier = Modifier.padding(top = spacing.space3), color = colors.textSecondary,
            )
        }
    }

    action?.let { current ->
        PinEditDialog(current, dismiss = { action = null }) { old, new ->
            val success = when (current) {
                PinAction.CREATE -> { onCreatePin(new); true }
                PinAction.CHANGE -> onChangePin(old, new)
                PinAction.DISABLE -> onDisableLock(old)
            }
            if (success) {
                feedback.success(when (current) {
                    PinAction.CREATE -> "App lock enabled."
                    PinAction.CHANGE -> "PIN changed."
                    PinAction.DISABLE -> "App lock turned off."
                })
                action = null
            }
            success
        }
    }
}

@Composable
private fun PinEditDialog(action: PinAction, dismiss: () -> Unit, submit: suspend (String, String) -> Boolean) {
    val scope = rememberCoroutineScope()
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var running by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    val valid = (action == PinAction.CREATE || old.length >= 4) &&
        (action == PinAction.DISABLE || new.length >= 4 && confirmation.length >= 4)
    val close = { if (!running) dismiss() }

    ThModal(open = true, onClose = close, modifier = Modifier.imePadding(), label = action.title) {
        Text(action.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(vertical = spacing.space3),
            verticalArrangement = Arrangement.spacedBy(spacing.space3),
        ) {
            if (action != PinAction.CREATE) PinField(
                "Current PIN", old, !running, imeAction = if (action == PinAction.DISABLE) ImeAction.Done else ImeAction.Next,
            ) { old = it; error = null }
            if (action != PinAction.DISABLE) {
                Text("Choose 4 to 8 digits.", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                PinField("New PIN", new, !running) { new = it; error = null }
                PinField("Confirm new PIN", confirmation, !running, imeAction = ImeAction.Done) { confirmation = it; error = null }
            }
            error?.let { ThStatusBanner(StatusVariant.ERROR, it) }
        }
        ThButton(
            onClick = {
                if (valid && !running) {
                    if (action != PinAction.DISABLE && new != confirmation) {
                        error = "The new PINs don’t match."
                    } else {
                        running = true
                        error = null
                        scope.launch {
                            try {
                                if (!submit(old, new)) {
                                    old = ""
                                    error = "The current PIN didn’t match."
                                }
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                error = "Couldn’t update app lock. Try again."
                            } finally {
                                running = false
                            }
                        }
                    }
                }
            },
            enabled = valid, loading = running, full = true, text = action.confirm,
        )
        ThButton(close, enabled = !running, text = "Cancel", variant = ButtonVariant.GHOST)
    }
}
