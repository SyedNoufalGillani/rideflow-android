package com.syednoufal.rideflow.feature.auth.data.repository

import com.syednoufal.rideflow.core.common.dispatcher.DispatcherProvider
import com.syednoufal.rideflow.core.domain.model.AuthSession
import com.syednoufal.rideflow.core.domain.model.DomainException
import com.syednoufal.rideflow.core.domain.model.OtpChallenge
import com.syednoufal.rideflow.core.domain.model.User
import com.syednoufal.rideflow.core.domain.repository.AuthRepository
import com.syednoufal.rideflow.core.network.dto.AuthSessionDto
import com.syednoufal.rideflow.core.network.dto.OtpChallengeDto
import com.syednoufal.rideflow.core.network.dto.UserDto
import com.syednoufal.rideflow.feature.auth.data.local.SessionDataStore
import com.syednoufal.rideflow.feature.auth.data.local.SessionTokenHolder
import com.syednoufal.rideflow.feature.auth.data.remote.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A real, Retrofit-backed [AuthRepository] implementation. Not selected by
 * default — see `BuildConfig.USE_FAKE_DATA_SOURCE` and [FakeAuthRepository]
 * — but demonstrates exactly how this repository would be wired against a
 * live backend, sharing the same [SessionDataStore]-based session storage.
 */
@Singleton
class RealAuthRepository @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionDataStore: SessionDataStore,
    private val sessionTokenHolder: SessionTokenHolder,
    private val dispatcherProvider: DispatcherProvider,
) : AuthRepository {

    override suspend fun requestOtp(phoneNumber: String): OtpChallenge = withContext(dispatcherProvider.io) {
        runCatching { remoteDataSource.requestOtp(phoneNumber) }
            .map { it.toDomain() }
            .getOrElse { throw it.toDomainException() }
    }

    override suspend fun verifyOtp(challengeId: String, code: String): AuthSession =
        withContext(dispatcherProvider.io) {
            val sessionDto = runCatching { remoteDataSource.verifyOtp(challengeId, code) }
                .getOrElse { throw it.toDomainException() }
            val session = sessionDto.toDomain()
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

    private fun Throwable.toDomainException(): Throwable = when (this) {
        is IOException -> DomainException.InvalidOtpException("Couldn't reach RideFlow. Check your connection.")
        is HttpException -> DomainException.InvalidOtpException()
        else -> this
    }

    private fun OtpChallengeDto.toDomain() = OtpChallenge(
        challengeId = challengeId,
        phoneNumber = phoneNumber,
        expiresInSeconds = expiresInSeconds,
    )

    private fun AuthSessionDto.toDomain() = AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toDomain(),
    )

    private fun UserDto.toDomain() = User(
        id = id,
        fullName = fullName,
        phoneNumber = phoneNumber,
        email = email,
        profilePhotoUrl = profilePhotoUrl,
        rating = rating,
        memberSinceEpochMillis = memberSinceEpochMillis,
    )
}
