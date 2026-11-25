package com.twohearts.app.ui.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.twohearts.app.services.device.DeviceCapabilities
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.components.UtilityPage
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val device = remember(context) { DeviceCapabilities(context) }
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current

    UtilityPage("About TwoHearts", onBack) {
        Surface(color = colors.surfaceBlush, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(spacing.space5), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                BrandLogo(variant = BrandLogoVariant.MARK, size = 76)
                Text("TwoHearts", style = MaterialTheme.typography.displayMedium)
                Text(
                    "A private place for your relationship’s everyday moments and milestones.",
                    style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary,
                )
            }
        }
        UtilityGroup("App information") {
            Column(Modifier.padding(vertical = spacing.space3), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                Text("Version ${device.getAppVersion()} · Build ${device.getAppVersionCode()}", style = MaterialTheme.typography.titleMedium)
                Text("Android ${device.getAndroidVersionName()}", color = colors.textSecondary)
            }
        }
        UtilityGroup("Privacy") {
            Text(
                "TwoHearts works offline. Your saved records stay on this device. Export can create a separate file of Notes and Reminders.",
                modifier = Modifier.padding(vertical = spacing.space3), color = colors.textSecondary,
            )
        }
        UtilityGroup("Credits") {
            Column(Modifier.padding(vertical = spacing.space3), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                Text("Design and development by the TwoHearts team.", color = colors.textSecondary)
                ThEyebrow("Companion cat", color = colors.roseDeep)
                Text("Yuki", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
