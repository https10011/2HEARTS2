package com.twohearts.app.ui.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ProfileAvatar
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * CouplePair — displays owner and partner avatars side by side.
 *
 * Matches legacy CouplePair component with:
 * - Two ProfileAvatar components
 * - Names below avatars
 * - Centered layout
 */
@Composable
fun CouplePair(
    ownerName: String,
    partnerName: String,
    ownerPhotoRef: String? = null,
    partnerPhotoRef: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Owner avatar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatar(
                name = ownerName,
                photoUrl = ownerPhotoRef,
                size = TwoHeartsTokens.Dimensions.avatarLg.value.toInt()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ownerName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        // Heart icon
        Text(
            text = "❤️",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.width(32.dp))

        // Partner avatar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatar(
                name = partnerName,
                photoUrl = partnerPhotoRef,
                size = TwoHeartsTokens.Dimensions.avatarLg.value.toInt()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = partnerName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
