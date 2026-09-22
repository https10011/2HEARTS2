package com.twohearts.app

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.repository.CoupleRelationshipRepository
import com.twohearts.app.data.repository.ImportantDateRepository
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.ProfileRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.ui.screens.home.HomeScreen

/**
 * Shared Home construction for the render harnesses.
 *
 * Home reads six repositories, and the harnesses render it from the same
 * process-wide Room database they seed. Rather than repeat that wiring at every
 * call site — and risk one of them quietly passing a subset, which is exactly
 * the defect Phase 4 exists to eliminate — the harnesses all render Home
 * through this helper.
 */
@Composable
fun TestHomeScreen(
    application: Application = ApplicationProvider.getApplicationContext(),
    onNavigate: (String) -> Unit = {},
) {
    val db = TwoHeartsDatabase.getDatabase(application)
    HomeScreen(
        relationshipService = RelationshipService(
            profileRepository = ProfileRepository(db.profileDao()),
            relationshipRepository = CoupleRelationshipRepository(db.coupleRelationshipDao()),
        ),
        noteRepository = NoteRepository(db.noteDao()),
        reminderRepository = ReminderRepository(db.reminderDao()),
        memoryRepository = MemoryRepository(db.memoryDao(), db.memoryMediaDao()),
        moodEntryRepository = MoodEntryRepository(db.moodEntryDao()),
        importantDateRepository = ImportantDateRepository(db.importantDateDao()),
        onNavigate = onNavigate,
    )
}
