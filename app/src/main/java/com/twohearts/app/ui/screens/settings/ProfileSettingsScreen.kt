package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.services.media.ProfilePhotoChange
import com.twohearts.app.ui.components.ProfilePhotoChangeSaver
import com.twohearts.app.ui.components.ProfilePhotoEditor
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.ThDatePicker
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.components.profilePhotoBackAction
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate

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
    val validBirthday = birthday.isBlank() || validProfileDate(birthday)
    val valid = name.isNotBlank() && validBirthday
    RecordEditorLayout(
        "Your profile", "Save profile", onSave = {
            if (valid && !saving) onSave(name.trim(), birthday.ifBlank { null }, photo)
        }, onBack = back, saving = saving, errorMessage = error, saveEnabled = valid,
    ) {
        SettingsPortrait(name, profile?.photoRef, "Your profile photo", photo, { photo = it }, enabled = !saving)
        ThInput(name, { name = it }, label = "Your name", enabled = !saving)
        ThDatePicker(
            birthday, { birthday = it }, label = "Birthday (optional)", enabled = !saving,
            error = "Choose a valid date up to today.".takeUnless { validBirthday },
        )
    }
}

internal fun validProfileDate(value: String, today: LocalDate = LocalDate.now()): Boolean =
    runCatching { !LocalDate.parse(value).isAfter(today) }.getOrDefault(false)

@Composable
internal fun SettingsPortrait(
    name: String, photoRef: String?, label: String, change: ProfilePhotoChange,
    onChange: (ProfilePhotoChange) -> Unit, enabled: Boolean,
) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = colors.surfaceWarm,
    ) {
        Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
            ThEyebrow(label, color = colors.roseDeep)
            ProfilePhotoEditor(name, photoRef, label, change, onChange, enabled = enabled)
        }
    }
}
