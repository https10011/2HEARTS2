package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.services.media.ProfilePhotoChange
import com.twohearts.app.ui.components.*

@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    profile: Profile? = null,
    saving: Boolean = false,
    error: String? = null,
    onSave: (String, String?, ProfilePhotoChange) -> Unit = { _, _, _ -> },
) {
    var name by rememberSaveable(profile?.id) { mutableStateOf(profile?.name.orEmpty()) }
    var birthday by rememberSaveable(profile?.id) { mutableStateOf(profile?.birthday.orEmpty()) }
    var photo by rememberSaveable(profile?.id, stateSaver = ProfilePhotoChangeSaver) {
        mutableStateOf<ProfilePhotoChange>(ProfilePhotoChange.Keep)
    }
    val back = profilePhotoBackAction(photo, saving, onBack)

    Scaffold(topBar = { Header(title = "Your profile", onBack = back) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ProfilePhotoEditor(name, profile?.photoRef, "Your profile photo", photo, { photo = it }, enabled = !saving)
            Input(value = name, onValueChange = { name = it }, label = "Your name", modifier = Modifier.fillMaxWidth())
            ThDatePicker(
                value = birthday, onValueChange = { birthday = it },
                label = "Birthday (optional)", enabled = !saving,
                support = "Used to remember your birthday in TwoHearts.",
            )
            if (error != null) ThStatusBanner(StatusVariant.ERROR, error)
            ThButton(
                onClick = { onSave(name.trim(), birthday.ifBlank { null }, photo) },
                full = true, enabled = name.isNotBlank(), loading = saving, text = "Save profile",
            )
        }
    }
}
