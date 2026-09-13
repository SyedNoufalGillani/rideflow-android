package com.syednoufal.rideflow.feature.auth.data.local

import com.syednoufal.rideflow.core.network.interceptor.AuthTokenProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A cheap, synchronous, in-memory cache of the current access token, used to
 * implement [AuthTokenProvider] for [com.syednoufal.rideflow.core.network.interceptor.AuthInterceptor],
 * which runs on an OkHttp interceptor thread and cannot suspend to read
 * DataStore. The auth repository updates this holder whenever the
 * DataStore-backed session changes; since RideFlow's default fake flow never
 * dispatches a real request, this cache is not restored from DataStore on
 * process start — only a real backend integration would need that.
 */
@Singleton
class SessionTokenHolder @Inject constructor() : AuthTokenProvider {

    @Volatile
    private var accessToken: String? = null

    override fun currentAccessToken(): String? = accessToken

    fun update(token: String?) {
        accessToken = token
    }
}
