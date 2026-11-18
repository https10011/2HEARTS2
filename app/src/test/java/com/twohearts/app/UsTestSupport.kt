package com.twohearts.app

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.repository.ImportantDateRepository
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.PlaceRepository
import com.twohearts.app.data.repository.TimelineEventRepository
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.ui.screens.us.UsScreen

@Composable
internal fun TestUsScreen(
    relationshipService: RelationshipService,
    db: TwoHeartsDatabase = TwoHeartsDatabase.getDatabase(ApplicationProvider.getApplicationContext<Application>()),
    onNavigate: (String) -> Unit = {},
) {
    UsScreen(
        relationshipService = relationshipService,
        importantDateRepository = ImportantDateRepository(db.importantDateDao()),
        memoryRepository = MemoryRepository(db.memoryDao(), db.memoryMediaDao()),
        timelineEventRepository = TimelineEventRepository(db.timelineEventDao()),
        placeRepository = PlaceRepository(db.placeDao()),
        moodEntryRepository = MoodEntryRepository(db.moodEntryDao()),
        onNavigate = onNavigate,
    )
}
