package com.syednoufal.rideflow.feature.auth.data.repository

import com.syednoufal.rideflow.core.common.dispatcher.DispatcherProvider
import com.syednoufal.rideflow.core.domain.model.AuthSession
import com.syednoufal.rideflow.core.domain.model.DomainException
import com.syednoufal.rideflow.core.domain.model.OtpChallenge
import com.syednoufal.rideflow.core.domain.model.User
import com.syednoufal.rideflow.core.domain.repository.AuthRepository
import com.syednoufal.rideflow.feature.auth.data.local.SessionDataStore
import com.syednoufal.rideflow.feature.auth.data.local.SessionTokenHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A fully simulated [AuthRepository] used by default (see
 * `BuildConfig.USE_FAKE_DATA_SOURCE`) so the login flow is interactive with
 * zero backend configuration. Accepts any phone number and always issues the
 * fixed demo code [DEMO_OTP_CODE], with realistic network-like latency.
 */
@Singleton
class FakeAuthRepository @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val sessionTokenHolder: SessionTokenHolder,
    private val dispatcherProvider: DispatcherProvider,
) : AuthRepository {

    private val pendingChallenges = ConcurrentHashMap<String, String>()

    override suspend fun requestOtp(phoneNumber: String): OtpChallenge = withContext(dispatcherProvider.io) {
        delay(NETWORK_LATENCY_MILLIS)
        val challengeId = UUID.randomUUID().toString()
        pendingChallenges[challengeId] = phoneNumber
        OtpChallenge(
            challengeId = challengeId,
            phoneNumber = phoneNumber,
            expiresInSeconds = CHALLENGE_TTL_SECONDS,
        )
    }

    override suspend fun verifyOtp(challengeId: String, code: String): AuthSession =
        withContext(dispatcherProvider.io) {
            delay(NETWORK_LATENCY_MILLIS)
            val phoneNumber = pendingChallenges[challengeId]
                ?: throw DomainException.InvalidOtpException("This code has expired. Request a new one.")

            if (code != DEMO_OTP_CODE) {
                throw DomainException.InvalidOtpException()
            }
            pendingChallenges.remove(challengeId)

            val session = AuthSession(
                accessToken = "demo-access-${UUID.randomUUID()}",
                refreshToken = "demo-refresh-${UUID.randomUUID()}",
                user = buildDemoUser(phoneNumber),
            )
            sessionDataStore.saveSession(session)
            sessionTokenHolder.update(session.accessToken)
            session
        }

    override fun observeIsLoggedIn(): Flow<Boolean> = sessionDataStore.observeSession().map { it != null }

    override suspend fun getCurrentUser(): User? = withContext(dispatcherProvider.io) {
        sessionDataStore.getSession()?.user
    }

    override suspend fun logout() = withContext(dispatcherProvider.io) {
        sessionDataStore.clearSession()
        sessionTokenHolder.update(null)
    }

    private fun buildDemoUser(phoneNumber: String): User {
        val digitsOnly = phoneNumber.filter { it.isDigit() }
        val displayName = DEMO_FIRST_NAMES[digitsOnly.hashCode().mod(DEMO_FIRST_NAMES.size)]
        return User(
            id = "user-$digitsOnly",
            fullName = "$displayName Rider",
            phoneNumber = phoneNumber,
            email = null,
            profilePhotoUrl = null,
            rating = DEMO_RATING,
            memberSinceEpochMillis = System.currentTimeMillis(),
        )
    }

    private companion object {
        const val DEMO_OTP_CODE = "1234"
        const val NETWORK_LATENCY_MILLIS = 900L
        const val CHALLENGE_TTL_SECONDS = 300
        const val DEMO_RATING = 4.9f
        val DEMO_FIRST_NAMES = listOf("Amara", "Kai", "Zayd", "Noor", "Leila", "Idris", "Maya", "Rhea")
    }
}
