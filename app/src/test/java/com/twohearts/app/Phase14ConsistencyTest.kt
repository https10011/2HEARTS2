package com.twohearts.app

import com.twohearts.app.ui.navigation.RecordLookup
import com.twohearts.app.ui.navigation.RecordState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class Phase14ConsistencyTest {
    @Test fun recordLookupDistinguishesMissingFailedAndReady() = runBlocking {
        val lookup = RecordLookup<String>()
        assertSame(RecordState.Loading, lookup.state)
        lookup.read { null }
        assertSame(RecordState.Missing, lookup.state)
        lookup.read { error("Storage unavailable") }
        assertSame(RecordState.Failed, lookup.state)
        lookup.refresh()
        assertEquals(1, lookup.generation)
        lookup.read { "Saved note" }
        assertEquals("Saved note", lookup.record)
        lookup.read { "Edited note" }
        assertEquals("Edited note", lookup.record)
    }

    @Test fun cancellingAnOffscreenReadDoesNotShowAnError() {
        val lookup = RecordLookup<String>()
        assertThrows(CancellationException::class.java) {
            runBlocking { lookup.read { throw CancellationException() } }
        }
        assertSame(RecordState.Loading, lookup.state)
    }
}
