package com.syednoufal.rideflow.core.common.result

/**
 * A generic wrapper around any asynchronous, potentially failing operation
 * (network call, database query, or a simulated fake data source standing in
 * for one). Repository implementations emit this type so presentation-layer
 * code never has to catch exceptions directly — it only ever pattern-matches
 * on [Success], [Error] or [Loading].
 */
sealed class NetworkResult<out T> {

    data class Success<out T>(val data: T) : NetworkResult<T>()

    data class Error(
        val message: String,
        val code: Int? = null,
        val cause: Throwable? = null,
    ) : NetworkResult<Nothing>()

    data object Loading : NetworkResult<Nothing>()

    /** Returns the wrapped value, or `null` when this is not a [Success]. */
    fun getOrNull(): T? = (this as? Success)?.data

    /** Maps the success payload of this result, leaving [Error]/[Loading] untouched. */
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /** Runs [action] only when this result is [Success]. */
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }

    /** Runs [action] only when this result is [Error]. */
    inline fun onError(action: (Error) -> Unit): NetworkResult<T> {
        if (this is Error) action(this)
        return this
    }
}

/**
 * Wraps [block] in a try/catch, converting any thrown [Throwable] into a
 * [NetworkResult.Error] instead of propagating it. Intended for repository
 * implementations calling a remote/fake data source.
 */
suspend inline fun <T> safeNetworkCall(crossinline block: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(block())
} catch (throwable: java.io.IOException) {
    NetworkResult.Error(message = throwable.message ?: "Network unavailable", cause = throwable)
} catch (throwable: kotlinx.coroutines.CancellationException) {
    throw throwable
} catch (throwable: RuntimeException) {
    NetworkResult.Error(message = throwable.message ?: "Unexpected error", cause = throwable)
}
