package com.twohearts.app.ui.screens.us

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Card
import com.twohearts.app.ui.screens.shared.CouplePair
import com.twohearts.app.services.relationship.RelationshipService

/**
 * UsScreen — relationship hub screen.
 *
 * Matches legacy UsScreen with:
 * - Couple pair display
 * - "Our Story" group: Memories, Timeline, Important Dates
 * - "Our World" group: Places, Mood, Period Tracker, Vault
 */
@Composable
fun UsScreen(
    relationshipService: RelationshipService,
    onNavigate: (String) -> Unit
) {
    // Observe profiles
    val owner by relationshipService.observeOwner().collectAsState(initial = null)
    val partner by relationshipService.observePartner().collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Couple pair display
        if (owner != null && partner != null) {
            CouplePair(
                ownerName = owner!!.name,
                partnerName = partner!!.name,
                ownerPhotoRef = owner!!.photoRef,
                partnerPhotoRef = partner!!.photoRef
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Our Story group
        Text(
            text = "Our Story",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Memories card
            FeatureCard(
                title = "Memories",
                icon = "📸",
                onClick = { onNavigate("/app/memories") },
                modifier = Modifier.weight(1f)
            )

            // Timeline card
            FeatureCard(
                title = "Timeline",
                icon = "📅",
                onClick = { onNavigate("/app/timeline") },
                modifier = Modifier.weight(1f)
            )

            // Important Dates card
            FeatureCard(
                title = "Dates",
                icon = "🎂",
                onClick = { onNavigate("/app/us/important-dates") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Our World group
        Text(
            text = "Our World",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Places card
            FeatureCard(
                title = "Places",
                icon = "📍",
                onClick = { onNavigate("/app/places") },
                modifier = Modifier.weight(1f)
            )

            // Mood card
            FeatureCard(
                title = "Mood",
                icon = "😊",
                onClick = { onNavigate("/app/mood") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period Tracker card
            FeatureCard(
                title = "Period",
                icon = "🗓️",
                onClick = { onNavigate("/app/period") },
                modifier = Modifier.weight(1f)
            )

            // Vault card
            FeatureCard(
                title = "Vault",
                icon = "🔒",
                onClick = { onNavigate("/app/vault") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * FeatureCard — navigation card for features.
 */
@Composable
fun FeatureCard(
    title: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
