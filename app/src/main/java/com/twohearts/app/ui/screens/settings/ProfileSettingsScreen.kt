package com.twohearts.app.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.ui.components.*

@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    profile: Profile? = null,
    saving: Boolean = false,
    error: String? = null,
    onSave: (String, String?, Uri?) -> Unit = { _, _, _ -> },
) {
    var name by rememberSaveable(profile?.id) { mutableStateOf(profile?.name.orEmpty()) }
    var birthday by rememberSaveable(profile?.id) { mutableStateOf(profile?.birthday.orEmpty()) }
    var photo by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if (it != null) photo = it
    }

    Scaffold(topBar = { Header(title = "Your profile", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileAvatar(
                    name, profile?.photoRef, size = 72,
                    label = "Your profile photo", selectedPhoto = photo,
                )
                ThButton(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    enabled = !saving, variant = ButtonVariant.SECONDARY,
                    text = if (photo != null || profile?.photoRef != null) "Change photo" else "Choose photo",
                )
            }
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
