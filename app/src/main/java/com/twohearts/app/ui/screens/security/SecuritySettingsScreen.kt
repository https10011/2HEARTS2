package com.twohearts.app.ui.screens.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.*
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
    Scaffold(topBar = { ThTopBar(title = "Privacy and security", onBack = onBack) }) { padding ->
        Column(
            modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 60.dp).toggleable(
                    value = isAppLockEnabled, role = Role.Switch,
                    onValueChange = { action = if (it) PinAction.CREATE else PinAction.DISABLE },
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("App lock", style = MaterialTheme.typography.titleMedium)
                    Text("Ask for your PIN when reopening TwoHearts.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(isAppLockEnabled, onCheckedChange = null)
            }
            HorizontalDivider()
            if (isAppLockEnabled) {
                ThButton(
                    { action = PinAction.CHANGE }, variant = ButtonVariant.SECONDARY,
                    icon = ThIcons.Lock, text = "Change PIN",
                )
            }
            Text("Private Vault", style = MaterialTheme.typography.titleMedium)
            Text(
                "Vault items stay on this device. When app lock is enabled, your PIN protects access to them and the rest of TwoHearts.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun PinEditDialog(
    action: PinAction,
    dismiss: () -> Unit,
    submit: suspend (String, String) -> Boolean,
) {
    val scope = rememberCoroutineScope()
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var running by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!running) dismiss() },
        title = { Text(action.title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (action != PinAction.CREATE) PinField("Current PIN", old, !running) { old = it }
                if (action != PinAction.DISABLE) {
                    Text("Choose 4 to 8 digits.", style = MaterialTheme.typography.bodyMedium)
                    PinField("New PIN", new, !running) { new = it }
                    PinField("Confirm new PIN", confirm, !running) { confirm = it }
                }
                error?.let { ThStatusBanner(StatusVariant.ERROR, it) }
            }
        },
        confirmButton = {
            ThButton(
                onClick = {
                    if (!running) {
                        if (action != PinAction.DISABLE && new != confirm) {
                            error = "The new PINs don’t match."
                        } else {
                            running = true
                            error = null
                            scope.launch {
                                try {
                                    if (!submit(old, new)) error = "The current PIN didn’t match."
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
                enabled = (action == PinAction.CREATE || old.length >= 4) &&
                    (action == PinAction.DISABLE || new.length >= 4 && confirm.length >= 4),
                loading = running, text = action.confirm,
            )
        },
        dismissButton = { ThButton(dismiss, enabled = !running, text = "Cancel", variant = ButtonVariant.GHOST) },
    )
}

@Composable
private fun PinField(label: String, value: String, enabled: Boolean, onChange: (String) -> Unit) {
    ThInput(
        value, { onChange(it.filter(Char::isDigit).take(8)) },
        label = label, enabled = enabled, keyboardType = KeyboardType.NumberPassword,
        visualTransformation = PasswordVisualTransformation(),
    )
}
