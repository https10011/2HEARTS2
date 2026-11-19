package com.twohearts.app

import com.twohearts.app.ui.components.ToastApi
import com.twohearts.app.ui.components.ToastMessage
import com.twohearts.app.ui.components.ToastState
import com.twohearts.app.ui.components.ToastVariant
import com.twohearts.app.ui.navigation.RecordAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase15FeedbackTest {
    private class Feedback : ToastApi {
        val notices = mutableListOf<ToastMessage>()
        override fun show(text: String, variant: ToastVariant) {
            notices += ToastMessage(text, variant)
        }
    }

    @Test
    fun repeatingTheSameFeedbackStartsASeparatePresentation() {
        val state = ToastState()
        state.success("Saved")
        val first = state.revision
        state.dismiss()
        assertFalse(state.visible)
        state.success("Saved")
        assertTrue(state.visible)
        assertEquals(first + 1, state.revision)
    }

    @Test
    fun successfulWriteReportsAfterPersistenceAndIgnoresRepeatedSubmission() = runBlocking {
        val feedback = Feedback()
        val action = RecordAction(this, feedback, Dispatchers.Unconfined)
        val gate = CompletableDeferred<Unit>()
        var completed = false
        action.run("Saved", "Failed", afterWrite = { completed = true }) { gate.await() }
        action.run("Duplicate", "Failed") { error("Second write should not run") }
        yield()
        assertTrue(action.running)
        assertTrue(feedback.notices.isEmpty())
        assertFalse(completed)
        gate.complete(Unit)
        yield()
        assertFalse(action.running)
        assertEquals(listOf(ToastMessage("Saved", ToastVariant.SUCCESS)), feedback.notices)
        assertTrue(completed)
    }

    @Test
    fun failedAndCancelledWritesDoNotNavigateAndPartialSuccessReportsWhatWasSkipped() = runBlocking {
        val feedback = Feedback()
        val action = RecordAction(this, feedback, Dispatchers.Unconfined)
        val unexpected = { error("Failed or cancelled writes should not navigate") }
        action.run("Saved", "Try again", unexpected) { error("Storage failure") }
        yield()
        action.run("Saved", "Unexpected error", unexpected) { throw CancellationException() }
        yield()
        assertFalse(action.running)
        assertEquals(listOf(ToastMessage("Try again", ToastVariant.ERROR)), feedback.notices)

        action.run("Saved", "Try again", warning = { "Saved without one photo" }) {}
        yield()
        assertEquals(ToastMessage("Saved without one photo", ToastVariant.INFO), feedback.notices.last())
    }
}
