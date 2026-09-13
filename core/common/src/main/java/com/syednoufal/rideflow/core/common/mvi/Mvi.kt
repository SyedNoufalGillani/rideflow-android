package com.syednoufal.rideflow.core.common.mvi

/**
 * Marker interface for a screen's rendered state in the MVI unidirectional
 * data flow used across every feature. Implementations are immutable data
 * classes exposed as a single [kotlinx.coroutines.flow.StateFlow].
 */
interface UiState

/**
 * Marker interface for user intents (a.k.a. UI events) dispatched from a
 * Composable into a [MviViewModel] via [MviViewModel.onIntent].
 */
interface UiIntent

/**
 * Marker interface for one-shot side effects (navigation, snackbars, haptics)
 * emitted from a [MviViewModel] and collected exactly once by the UI, as
 * opposed to [UiState] which is re-rendered on every collection.
 */
interface UiEffect
