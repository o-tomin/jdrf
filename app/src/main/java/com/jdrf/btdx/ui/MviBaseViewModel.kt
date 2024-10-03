package com.jdrf.btdx.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class MviBaseViewModel<S : MviBaseViewState, E : Any>(
    initialState: S
) : ViewModel() {

    val state: StateFlow<S>
        get() = _state

    val events: Flow<E>
        get() = _events.receiveAsFlow()

    private val _state = MutableStateFlow(initialState)
    private val _events = Channel<E>(Channel.UNLIMITED)
    private val stateMutex = Mutex()

    protected suspend fun setState(reducer: S.() -> S) {
        _state.value = stateMutex.withLock {
            _state.value.reducer()
        }
    }

    protected fun updateState(reducer: S.() -> S) {
        viewModelScope.launch {
            setState(reducer)
        }
    }

    protected fun sendEvent(event: E) {
        _events.trySend(event)
    }

    protected fun <T> Flow<T>.bind(reducer: S.(value: T) -> S) {
        onEach { value ->
            setState {
                reducer(value)
            }
        }.launchIn(viewModelScope)
    }
}

interface MviBaseViewState