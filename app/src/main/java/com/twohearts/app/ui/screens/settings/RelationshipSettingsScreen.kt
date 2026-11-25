package com.twohearts.app.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.services.media.ProfilePhotoChange
import com.twohearts.app.ui.components.ProfilePhotoChangeSaver
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.ThDatePicker
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.components.profilePhotoBackAction

@Composable
fun RelationshipSettingsScreen(
    onBack: () -> Unit,
    partner: Profile? = null,
    relationship: CoupleRelationship? = null,
    saving: Boolean = false,
    error: String? = null,
    onSave: (String, String, ProfilePhotoChange) -> Unit = { _, _, _ -> },
) {
    var name by rememberSaveable(partner?.id) { mutableStateOf(partner?.name.orEmpty()) }
    var startDate by rememberSaveable(relationship?.id) { mutableStateOf(relationship?.startDate.orEmpty()) }
    var photo by rememberSaveable(partner?.id, stateSaver = ProfilePhotoChangeSaver) {
        mutableStateOf<ProfilePhotoChange>(ProfilePhotoChange.Keep)
    }
    val validDate = validProfileDate(startDate)
    val valid = name.isNotBlank() && validDate
    val back = profilePhotoBackAction(photo, saving, onBack)

    RecordEditorLayout(
        "Relationship", "Save relationship", onSave = {
            if (valid && !saving) onSave(name.trim(), startDate, photo)
        }, onBack = back, saving = saving, errorMessage = error, saveEnabled = valid,
    ) {
        SettingsPortrait(name, partner?.photoRef, "Partner’s profile photo", photo, { photo = it }, enabled = !saving)
        ThInput(name, { name = it }, label = "Partner’s name", enabled = !saving)
        ThDatePicker(
            startDate, { startDate = it }, label = "Relationship start date", clearable = false, enabled = !saving,
            error = "Choose a valid date up to today.".takeIf { startDate.isNotBlank() && !validDate },
        )
    }
}
