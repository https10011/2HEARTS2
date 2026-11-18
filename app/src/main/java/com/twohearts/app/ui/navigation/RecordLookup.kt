package com.twohearts.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThLoadingState
import com.twohearts.app.ui.components.ThTopBar
import kotlinx.coroutines.CancellationException

internal sealed interface RecordState<out T> {
    data object Loading : RecordState<Nothing>
    data object Missing : RecordState<Nothing>
    data object Failed : RecordState<Nothing>
    data class Ready<T>(val record: T) : RecordState<T>
}

internal class RecordLookup<T> {
    var state: RecordState<T> by mutableStateOf(RecordState.Loading)
        private set
    var generation by mutableIntStateOf(0)
        private set
    val record: T? get() = (state as? RecordState.Ready<T>)?.record

    fun refresh() { generation++ }

    suspend fun read(load: suspend () -> T?) {
        state = try {
            load()?.let { RecordState.Ready(it) } ?: RecordState.Missing
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            RecordState.Failed
        }
    }
}

@Composable
internal fun <T> rememberRecordLookup(key: String, load: suspend () -> T?): RecordLookup<T> {
    val lookup = remember(key) { RecordLookup<T>() }
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val currentLoad by rememberUpdatedState(load)
    LaunchedEffect(lookup, lifecycle, lookup.generation) {
        if (lifecycle == Lifecycle.State.RESUMED) lookup.read(currentLoad)
    }
    return lookup
}

@Composable
internal fun RecordLookupScreen(title: String, lookup: RecordLookup<*>, back: () -> Unit) {
    Scaffold(topBar = { ThTopBar(title = title, onBack = back) }) { insets ->
        val modifier = Modifier.fillMaxSize().padding(insets).padding(top = 20.dp)
        when (lookup.state) {
            RecordState.Loading -> ThLoadingState(modifier = modifier, label = "Opening ${title.lowercase()}…")
            RecordState.Missing -> ThEmptyState(
                title = "$title unavailable", modifier = modifier,
                description = "This item may have been removed.", actionLabel = "Go back", onAction = back,
            )
            RecordState.Failed -> ThEmptyState(
                title = "Couldn’t open this ${title.lowercase()}", modifier = modifier,
                description = "Try loading it again.", actionLabel = "Try again", onAction = lookup::refresh,
            )
            is RecordState.Ready<*> -> Unit
        }
    }
}
