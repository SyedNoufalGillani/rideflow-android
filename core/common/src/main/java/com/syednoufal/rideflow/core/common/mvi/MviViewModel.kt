package com.syednoufal.rideflow.core.common.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Generic base class implementing the MVI (Model-View-Intent) unidirectional
 * data flow used by every feature ViewModel in RideFlow.
 *
 * - [state] is the single source of truth the UI renders, exposed as a
 *   [StateFlow] so recompositions only happen when the state actually changes.
 * - [onIntent] is the single entry point through which the UI communicates
 *   user actions back to the ViewModel.
 * - [effect] carries one-shot events (navigation, toasts) that must not be
 *   replayed on configuration change, backed by a [Channel] rather than a
 *   [StateFlow].
 *
 * @param S the immutable [UiState] rendered by the screen.
 * @param I the sealed hierarchy of [UiIntent]s the screen can dispatch.
 * @param E the sealed hierarchy of one-shot [UiEffect]s the screen can consume.
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val effectChannel = Channel<E>(capacity = Channel.BUFFERED, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effect: Flow<E> = effectChannel.receiveAsFlow()

    /** Current value of [state], safe to read synchronously (e.g. inside a reducer). */
    protected val currentState: S
        get() = _state.value

    /** Single entry point for the UI to dispatch a [UiIntent]. */
    abstract fun onIntent(intent: I)

    /** Applies [reducer] to the current state, publishing the result to [state]. */
    protected fun setState(reducer: S.() -> S) {
        _state.value = currentState.reducer()
    }

    /** Sends a one-shot [UiEffect] to be collected exactly once by the UI. */
    protected fun sendEffect(builder: () -> E) {
        viewModelScope.launch {
            effectChannel.send(builder())
        }
    }
}
