package com.twohearts.app.ui.screens.yuki

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.twohearts.app.data.game.YukiAction
import com.twohearts.app.data.game.YukiState
import com.twohearts.app.data.game.YukiToy
import com.twohearts.app.services.game.YukiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class YukiPresentation(
    val state: YukiState? = null,
    val feedback: String? = null,
    val working: Boolean = false,
    val failed: Boolean = false,
    val refusal: Int = 0,
)

class YukiViewModel(application: Application) : AndroidViewModel(application) {
    private val service = YukiService(application)
    private val _presentation = MutableStateFlow(YukiPresentation())
    internal val presentation = _presentation.asStateFlow()

    fun refresh() = update { current ->
        current.copy(state = service.refresh())
    }

    fun processAction(action: YukiAction) = update { current ->
        if (current.state == null) return@update current
        val result = service.perform(action)
        current.copy(
            state = result.state, feedback = result.message,
            refusal = if (result.accepted) current.refusal else current.refusal + 1,
        )
    }

    fun equipAccessory(id: String) = update { current ->
        if (current.state == null) return@update current
        current.copy(state = service.changeAccessory(id), feedback = null)
    }

    fun selectToy(toy: YukiToy) = update { current ->
        if (current.state == null) return@update current
        current.copy(state = service.changeToy(toy), feedback = null)
    }

    private fun update(transform: (YukiPresentation) -> YukiPresentation) {
        val current = _presentation.value
        if (current.working) return
        _presentation.value = current.copy(working = true, failed = false)
        viewModelScope.launch {
            try {
                _presentation.value = withContext(Dispatchers.IO) { transform(current) }.copy(
                    working = false, failed = false,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _presentation.value = current.copy(working = false, failed = true)
            }
        }
    }
}
