package com.twohearts.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.Card
import com.twohearts.app.ui.screens.shared.CouplePair
import com.twohearts.app.ui.screens.shared.RelationshipCounter
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * HomeScreen — main dashboard screen.
 *
 * Matches legacy HomeScreen with:
 * - Couple header with owner/partner avatars
 * - TwoHearts brand logo
 * - Personalized greeting
 * - "Our story together" counter
 * - 4 primary action cards: Notes, Reminders, Us, Yuki
 */
@Composable
fun HomeScreen(
    relationshipService: RelationshipService,
    onNavigate: (String) -> Unit
) {
    // Observe owner and partner profiles
    val owner by relationshipService.observeOwner().collectAsState(initial = null)
    val partner by relationshipService.observePartner().collectAsState(initial = null)
    val relationship by relationshipService.observeRelationship().collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand logo
        BrandLogo(
            modifier = Modifier.height(48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Couple header
        if (owner != null && partner != null) {
            CouplePair(
                ownerName = owner!!.name,
                partnerName = partner!!.name,
                ownerPhotoRef = owner!!.photoRef,
                partnerPhotoRef = partner!!.photoRef
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Personalized greeting
        val greeting = getGreeting()
        Text(
            text = "$greeting, ${owner?.name ?: "there"}!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Relationship counter
        if (relationship != null) {
            RelationshipCounter(
                startDate = relationship!!.startDate
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Notes card
            ActionCard(
                title = "Notes",
                icon = "📝",
                onClick = { onNavigate("/app/notes") },
                modifier = Modifier.weight(1f)
            )

            // Reminders card
            ActionCard(
                title = "Reminders",
                icon = "⏰",
                onClick = { onNavigate("/app/reminders") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Us card
            ActionCard(
                title = "Us",
                icon = "💕",
                onClick = { onNavigate("/app/us") },
                modifier = Modifier.weight(1f)
            )

            // Yuki card
            ActionCard(
                title = "Yuki",
                icon = "🐱",
                onClick = { onNavigate("/app/yuki") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * ActionCard — navigation card for features.
 */
@Composable
fun ActionCard(
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Get personalized greeting based on time of day.
 */
private fun getGreeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}
