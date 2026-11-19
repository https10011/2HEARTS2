package com.twohearts.app.ui.screens.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AppLockGate(onUnlock: suspend (String) -> Boolean, modifier: Modifier = Modifier) {
    var pin by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
    ) {
        Icon(ThIcons.Lock, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
        Text(
            "TwoHearts is locked", style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "Enter your PIN to open your space.", textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ThInput(
            pin, { pin = it.filter(Char::isDigit).take(8); failure = null },
            label = "PIN", enabled = !checking, keyboardType = KeyboardType.NumberPassword,
            visualTransformation = PasswordVisualTransformation(), error = failure,
            imeAction = ImeAction.Done,
        )
        ThButton(
            onClick = {
                if (!checking) {
                    checking = true
                    scope.launch {
                        try {
                            if (!onUnlock(pin)) {
                                pin = ""
                                failure = "That PIN didn’t match."
                            }
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            failure = "Couldn’t verify your PIN. Try again."
                        } finally {
                            checking = false
                        }
                    }
                }
            },
            enabled = pin.length >= 4, loading = checking, full = true, text = "Unlock",
        )
    }
}
