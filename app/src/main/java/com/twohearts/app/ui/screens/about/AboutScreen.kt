package com.twohearts.app.ui.screens.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.twohearts.app.services.device.DeviceCapabilities
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.components.ThTopBar

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val metadata = remember(context) { DeviceCapabilities(context) }
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(topBar = { ThTopBar(title = "About TwoHearts", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BrandLogo(Modifier.align(Alignment.CenterHorizontally), variant = BrandLogoVariant.MARK, size = 96)
            Text("TwoHearts", style = MaterialTheme.typography.headlineSmall)
            Text("A private place for your relationship’s everyday moments and milestones.", color = secondary)
            HorizontalDivider()
            Text("App information", style = MaterialTheme.typography.titleMedium)
            Text("Version ${metadata.getAppVersion()} · Build ${metadata.getAppVersionCode()}", color = secondary)
            Text("Android ${metadata.getAndroidVersionName()}", color = secondary)
            HorizontalDivider()
            Text("Privacy", style = MaterialTheme.typography.titleMedium)
            Text("TwoHearts works offline. Records stay on this device unless you export them.", color = secondary)
            Text("TwoHearts uses no online account or analytics.", color = secondary)
            HorizontalDivider()
            Text("Credits", style = MaterialTheme.typography.titleMedium)
            Text("Design and development by the TwoHearts team.", color = secondary)
            Text("Companion cat · Yuki", color = secondary)
        }
    }
}
