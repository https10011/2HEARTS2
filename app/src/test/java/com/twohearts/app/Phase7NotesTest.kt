package com.twohearts.app

import com.twohearts.app.data.entity.Note
import com.twohearts.app.ui.screens.notes.NoteCategory
import com.twohearts.app.ui.screens.notes.filterNotes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase7NotesTest {
    private val notes = listOf(
        Note("For you", "Remember the rainy train ride", "love_letter", id = "love", createdAt = "2026-01-01T00:00:00Z", updatedAt = "2026-01-01T00:00:00Z"),
        Note("Weekend", "Buy tea and oranges", "reminder", id = "list", createdAt = "2026-01-02T00:00:00Z", updatedAt = "2026-01-02T00:00:00Z"),
    )

    @Test fun searchLooksThroughTitlesAndTheWordsInsideNotes() {
        assertEquals("love", filterNotes(notes, "train", null).single().id)
        assertEquals("list", filterNotes(notes, "week", null).single().id)
        assertTrue(filterNotes(notes, "missing", null).isEmpty())
    }

    @Test fun categoryAndSearchComposeIntoOnePersonalView() {
        assertEquals("love", filterNotes(notes, "rainy", NoteCategory.LOVE_LETTER).single().id)
        assertTrue(filterNotes(notes, "oranges", NoteCategory.LOVE_LETTER).isEmpty())
    }
}
