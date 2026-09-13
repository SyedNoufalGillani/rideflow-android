package com.syednoufal.rideflow.core.network

/**
 * Networking constants for the (currently unused) real backend integration.
 *
 * [BASE_URL] is a placeholder — RideFlow has no live backend. By default the
 * app runs entirely against the fake, in-memory repositories in each feature
 * module (see `BuildConfig.USE_FAKE_DATA_SOURCE` in `:app`), so nothing ever
 * actually dispatches a request to this host. It exists to demonstrate how
 * Retrofit would be configured against a real ride-hailing backend.
 */
object NetworkConstants {
    const val BASE_URL = "https://api.rideflow.example.com/"
    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 15L
    const val WRITE_TIMEOUT_SECONDS = 15L
    const val HEADER_AUTHORIZATION = "Authorization"
    const val BEARER_PREFIX = "Bearer "
}
