package com.twohearts.app.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.components.UtilityItem
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.widthIn(max = 620.dp).fillMaxWidth().padding(horizontal = spacing.screenGutter, vertical = spacing.space5),
            verticalArrangement = Arrangement.spacedBy(spacing.space5),
        ) {
            Text("More", style = MaterialTheme.typography.displayMedium, modifier = Modifier.semantics { heading() })
            Surface(shape = MaterialTheme.shapes.large, color = LocalTwoHeartsColors.current.surfaceBlush) {
                Column(Modifier.fillMaxWidth().padding(horizontal = spacing.space4)) {
                    UtilityItem("Search", ThIcons.Search, "Find a saved record") { onNavigate(RoutePath.APP_SEARCH) }
                    UtilityItem("Notifications", ThIcons.Bell, "Delivered alerts") { onNavigate(RoutePath.APP_NOTIFICATIONS) }
                }
            }
            UtilityGroup("Settings") {
                UtilityItem("App settings", ThIcons.Settings, "Appearance, notifications, and security") { onNavigate(RoutePath.APP_SETTINGS) }
                UtilityItem("Your profile", ThIcons.Person, "Name, photo, and birthday") { onNavigate(RoutePath.APP_SETTINGS_PROFILE) }
                UtilityItem("Relationship", ThIcons.Heart, "Partner details and start date") { onNavigate(RoutePath.APP_SETTINGS_RELATIONSHIP) }
            }
            UtilityGroup("On this device") {
                UtilityItem("Notes and reminders transfer", ThIcons.File, "Import or export JSON") { onNavigate(RoutePath.APP_SETTINGS_IMPORT) }
                UtilityItem("Storage", ThIcons.Storage, "Space used and temporary files") { onNavigate(RoutePath.APP_SETTINGS_STORAGE) }
                UtilityItem("About TwoHearts", ThIcons.Info) { onNavigate(RoutePath.APP_ABOUT) }
            }
        }
    }
}
