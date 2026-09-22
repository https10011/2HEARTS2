package com.twohearts.app

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.entity.Reminder

/**
 * Phase4HomeSeed — deterministic local data for the Home render harness.
 *
 * The Phase 4 requirement is that Home be validated against *real* local data
 * created through the application's own mechanisms, not mocked composables.
 * This seeds the same Room database the app uses, through the same DAOs the
 * repositories use, so the harness renders the genuine query path.
 *
 * The data mirrors what a real couple would plausibly have — two people, a
 * start date, a birthday, a couple of notes, an upcoming anniversary, a
 * memory, and a check-in — so the populated renders are representative rather
 * than a stress fixture.
 *
 * Seeding is idempotent: the database is a process-wide singleton shared with
 * the other harnesses, and `profiles.id` is a primary key, so every insert is
 * guarded.
 */
object Phase4HomeSeed {

    const val OWNER_ID = "p4-owner"
    const val PARTNER_ID = "p4-partner"
    const val RELATIONSHIP_ID = "p4-rel"

    // A fixed date in the past so the day count is large and non-trivial.
    const val START_DATE = "2023-02-14"
    const val PARTNER_BIRTHDAY = "1996-03-11"

    private const val NOW = "2026-01-15T10:00:00Z"

    /** Mood emoji for the seeded check-in. */
    private const val MOOD_EMOJI = "\uD83D\uDE0A"

    /**
     * Seed the full populated scenario.
     *
     * Suspending rather than blocking: Room refuses queries from the main
     * thread, and a harness that pinned itself to the main looper would trip
     * that guard. The caller supplies the dispatcher.
     */
    suspend fun seedPopulated(
        application: Application = ApplicationProvider.getApplicationContext(),
    ): TwoHeartsDatabase {
        val db = TwoHeartsDatabase.getDatabase(application)
        insertIfAbsent(db) {
            db.profileDao().insert(
                Profile(
                    name = "Alexandria", role = "owner",
                    id = OWNER_ID, createdAt = NOW, updatedAt = NOW,
                )
            )
            db.profileDao().insert(
                Profile(
                    name = "Sam", role = "partner", birthday = PARTNER_BIRTHDAY,
                    id = PARTNER_ID, createdAt = NOW, updatedAt = NOW,
                )
            )
            db.coupleRelationshipDao().insert(
                CoupleRelationship(
                    ownerId = OWNER_ID, partnerId = PARTNER_ID, startDate = START_DATE,
                    id = RELATIONSHIP_ID, createdAt = NOW, updatedAt = NOW,
                )
            )

            // Notes: a love letter and an ordinary note, so the tile can be
            // seen to name the most recent one.
            db.noteDao().insert(
                Note(
                    title = "A letter for your birthday",
                    content = "I have been saving this one for a while.",
                    category = "love-letter", id = "p4-n1",
                    createdAt = NOW, updatedAt = NOW,
                )
            )
            db.noteDao().insert(
                Note(
                    title = "Weekend ideas",
                    content = "The little pasta place, then the long walk.",
                    category = "idea", id = "p4-n2",
                    createdAt = NOW, updatedAt = NOW,
                )
            )

            // Reminders: one soon, one much later.
            db.reminderDao().insert(
                Reminder(
                    title = "Book the anniversary dinner",
                    scheduledDate = "2026-02-10", scheduledTime = "19:30",
                    id = "p4-r1", createdAt = NOW, updatedAt = NOW,
                )
            )
            db.reminderDao().insert(
                Reminder(
                    title = "Renew the museum pass",
                    scheduledDate = "2026-04-02",
                    id = "p4-r2", createdAt = NOW, updatedAt = NOW,
                )
            )

            // Memories: the newest one is what Home should surface.
            db.memoryDao().insert(
                Memory(
                    title = "The night we got caught in the rain",
                    caption = "We ran the whole way home and laughed the entire time.",
                    memoryDate = "2025-11-02", id = "p4-m1",
                    createdAt = NOW, updatedAt = NOW,
                )
            )
            db.memoryDao().insert(
                Memory(
                    title = "Sunday morning, pancakes",
                    caption = "No plans, nowhere to be.",
                    memoryDate = "2026-01-04", id = "p4-m2",
                    createdAt = NOW, updatedAt = NOW,
                )
            )

            // An anniversary saved as an important date, so the upcoming
            // section can resolve a real recurring date.
            db.importantDateDao().insert(
                ImportantDate(
                    title = "The day we met",
                    date = "2021-01-20", recurrence = "yearly",
                    id = "p4-d1", createdAt = NOW, updatedAt = NOW,
                )
            )

            // A check-in for today.
            db.moodEntryDao().insert(
                MoodEntry(
                    moodValue = "happy", moodEmoji = MOOD_EMOJI,
                    note = "Quiet, ordinary, good.",
                    profileId = OWNER_ID, entryDate = TODAY,
                    id = "p4-mo1", createdAt = NOW, updatedAt = NOW,
                )
            )
        }
        return db
    }

    /**
     * The local date the harness and the seeded check-in both use.
     *
     * Pinned rather than "now" so renders and assertions are stable, and shared
     * by the seed and the harness so the "today's mood" row is genuinely
     * today's rather than a stale row that merely happens to be the newest.
     */
    const val TODAY = "2026-01-15"

    private suspend fun insertIfAbsent(db: TwoHeartsDatabase, block: suspend () -> Unit) {
        if (db.profileDao().getById(OWNER_ID) != null) return
        block()
    }
}
