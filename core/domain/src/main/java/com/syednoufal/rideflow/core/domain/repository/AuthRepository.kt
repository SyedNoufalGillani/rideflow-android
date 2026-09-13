package com.syednoufal.rideflow.core.domain.repository

import com.syednoufal.rideflow.core.domain.model.AuthSession
import com.syednoufal.rideflow.core.domain.model.OtpChallenge
import com.syednoufal.rideflow.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Contract for phone-number + OTP authentication. A real implementation
 * backs this with Retrofit calls to an auth service; RideFlow ships a fully
 * simulated implementation so the login flow is interactive with zero
 * backend configuration.
 */
interface AuthRepository {

    /** Requests a one-time code be sent to [phoneNumber], returning a challenge to verify against. */
    suspend fun requestOtp(phoneNumber: String): OtpChallenge

    /** Verifies [code] against the challenge identified by [challengeId], starting a session on success. */
    suspend fun verifyOtp(challengeId: String, code: String): AuthSession

    /** Emits `true`/`false` as the rider's authentication state changes. */
    fun observeIsLoggedIn(): Flow<Boolean>

    /** Returns the currently authenticated rider, or `null` if no session is active. */
    suspend fun getCurrentUser(): User?

    /** Clears the local session. */
    suspend fun logout()
}
