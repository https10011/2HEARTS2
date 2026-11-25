package com.twohearts.app.ui.screens.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AppLockGate(onUnlock: suspend (String) -> Boolean, modifier: Modifier = Modifier) {
    var pin by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing

    Column(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(spacing.screenGutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            color = colors.surfaceWarm, contentColor = colors.charcoal, shape = MaterialTheme.shapes.large,
        ) {
            Column(
                Modifier.padding(spacing.space5), verticalArrangement = Arrangement.spacedBy(spacing.space3),
            ) {
                BrandLogo(Modifier.clearAndSetSemantics {}, BrandLogoVariant.MARK, size = 76)
                ThEyebrow("App lock", color = colors.roseDeep)
                Text(
                    "TwoHearts is locked", style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text("Enter your PIN to open TwoHearts.", color = colors.textSecondary)
                PinField("PIN", pin, !checking, failure, ImeAction.Done) {
                    pin = it
                    failure = null
                }
                ThButton(
                    onClick = {
                        if (!checking && pin.length >= 4) {
                            checking = true
                            failure = null
                            scope.launch {
                                try {
                                    if (!onUnlock(pin)) {
                                        pin = ""
                                        failure = "That PIN didn’t match."
                                    }
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (_: Exception) {
                                    pin = ""
                                    failure = "Couldn’t verify your PIN. Try again."
                                } finally {
                                    checking = false
                                }
                            }
                        }
                    },
                    enabled = pin.length >= 4, loading = checking, full = true, icon = ThIcons.Lock, text = "Unlock",
                )
            }
        }
    }
}

@Composable
internal fun PinField(
    label: String, value: String, enabled: Boolean, error: String? = null, imeAction: ImeAction = ImeAction.Next,
    onChange: (String) -> Unit,
) {
    ThInput(
        value, { onChange(it.filter(Char::isDigit).take(8)) },
        label = label, enabled = enabled, keyboardType = KeyboardType.NumberPassword,
        visualTransformation = PasswordVisualTransformation(), error = error, imeAction = imeAction,
    )
}
