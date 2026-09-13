package com.syednoufal.rideflow.core.network.interceptor

/**
 * Port implemented by the auth feature's data layer so [AuthInterceptor] can
 * read the current session token without :core:network ever depending on
 * :feature:auth (which would invert the dependency graph). Reads must be
 * synchronous and fast — this is called from an OkHttp interceptor thread.
 */
fun interface AuthTokenProvider {
    fun currentAccessToken(): String?
}
