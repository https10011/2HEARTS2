package com.twohearts.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.twohearts.app.ui.components.ToastApi
import com.twohearts.app.ui.components.useToast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Serializes user submissions for a screen and reports the outcome of the write. */
internal class RecordAction(
    private val scope: CoroutineScope,
    private val feedback: ToastApi,
    private val worker: CoroutineDispatcher = Dispatchers.IO,
) {
    var running by mutableStateOf(false)
        private set

    fun run(
        success: String,
        failure: String,
        afterWrite: () -> Unit = {},
        warning: () -> String? = { null },
        write: suspend () -> Unit,
    ) {
        if (running) return
        running = true
        scope.launch {
            try {
                withContext(worker) { write() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                feedback.error(failure)
                return@launch
            } finally {
                running = false
            }
            val incomplete = warning()
            if (incomplete == null) feedback.success(success) else feedback.info(incomplete)
            afterWrite()
        }
    }
}

@Composable
internal fun rememberRecordAction(): RecordAction {
    val scope = rememberCoroutineScope()
    val feedback = useToast()
    val action = remember(scope, feedback) { RecordAction(scope, feedback) }
    BackHandler(action.running) {}
    return action
}
