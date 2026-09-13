package com.syednoufal.rideflow.core.network.interceptor

import com.syednoufal.rideflow.core.network.NetworkConstants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Attaches the current session's bearer token, if any, to every outgoing request. */
class AuthInterceptor @Inject constructor(
    private val tokenProvider: AuthTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider.currentAccessToken()

        val authorizedRequest = if (token.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .addHeader(NetworkConstants.HEADER_AUTHORIZATION, NetworkConstants.BEARER_PREFIX + token)
                .build()
        }

        return chain.proceed(authorizedRequest)
    }
}
