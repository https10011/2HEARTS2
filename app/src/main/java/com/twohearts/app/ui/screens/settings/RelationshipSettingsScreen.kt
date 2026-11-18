package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.ui.components.*
import java.time.LocalDate

@Composable
fun RelationshipSettingsScreen(
    onBack: () -> Unit,
    partner: Profile? = null,
    relationship: CoupleRelationship? = null,
    saving: Boolean = false,
    error: String? = null,
    onSave: (String, String) -> Unit = { _, _ -> },
) {
    var name by rememberSaveable(partner?.id) { mutableStateOf(partner?.name.orEmpty()) }
    var startDate by rememberSaveable(relationship?.id) { mutableStateOf(relationship?.startDate.orEmpty()) }
    val validDate = runCatching { !LocalDate.parse(startDate).isAfter(LocalDate.now()) }.getOrDefault(false)

    Scaffold(topBar = { Header(title = "Relationship", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ProfileAvatar(name, partner?.photoRef, size = 64)
            Input(
                value = name, onValueChange = { name = it }, label = "Your Special Someone",
                modifier = Modifier.fillMaxWidth(),
            )
            ThDatePicker(
                value = startDate, onValueChange = { startDate = it }, label = "Relationship start date",
                clearable = false, enabled = !saving,
                error = if (startDate.isNotBlank() && !validDate) "Choose a date up to today." else null,
            )
            if (error != null) ThStatusBanner(StatusVariant.ERROR, error)
            ThButton(
                onClick = { onSave(name.trim(), startDate) },
                full = true, enabled = name.isNotBlank() && validDate, loading = saving, text = "Save relationship",
            )
        }
    }
}
