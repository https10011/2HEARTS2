package com.twohearts.app.ui.screens.settings

import androidx.compose.runtime.Composable
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.components.UtilityItem
import com.twohearts.app.ui.components.UtilityPage
import com.twohearts.app.ui.navigation.RoutePath

@Composable
fun SettingsHomeScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    UtilityPage("Settings", onBack) {
        UtilityGroup("Personal") {
            UtilityItem("Your profile", ThIcons.Person, "Name, photo, and birthday") { onNavigate(RoutePath.APP_SETTINGS_PROFILE) }
            UtilityItem("Relationship", ThIcons.Heart, "Partner details and start date") { onNavigate(RoutePath.APP_SETTINGS_RELATIONSHIP) }
        }
        UtilityGroup("Preferences") {
            UtilityItem("Appearance", ThIcons.Palette, "Theme, text size, and motion") { onNavigate(RoutePath.APP_SETTINGS_APPEARANCE) }
            UtilityItem("Notifications", ThIcons.Bell, "Reminder and Android alert settings") { onNavigate(RoutePath.APP_SETTINGS_NOTIFICATIONS) }
            UtilityItem("Security", ThIcons.Lock, "App lock and PIN") { onNavigate(RoutePath.APP_SETTINGS_SECURITY) }
        }
        UtilityGroup("On this device") {
            UtilityItem("Storage", ThIcons.Storage, "Usage, cleanup, and reset") { onNavigate(RoutePath.APP_SETTINGS_STORAGE) }
            UtilityItem("Notes and reminders transfer", ThIcons.File, "Import or export JSON") { onNavigate(RoutePath.APP_SETTINGS_IMPORT) }
            UtilityItem("About TwoHearts", ThIcons.Info, "Version, privacy, and credits") { onNavigate(RoutePath.APP_ABOUT) }
        }
    }
}
