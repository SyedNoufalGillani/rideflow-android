package com.syednoufal.rideflow.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Indirection over [kotlinx.coroutines.Dispatchers] so classes never call
 * `Dispatchers.IO` directly. Injecting this interface lets tests substitute a
 * single [kotlinx.coroutines.test.TestDispatcher] for every dispatcher.
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}
