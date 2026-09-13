package com.syednoufal.rideflow.core.domain.usecase.auth

import com.syednoufal.rideflow.core.domain.model.AuthSession
import com.syednoufal.rideflow.core.domain.repository.AuthRepository
import javax.inject.Inject

/** Verifies an OTP code against a previously requested challenge, starting a rider session. */
class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(challengeId: String, code: String): AuthSession {
        require(code.length == OTP_LENGTH) { "Enter the $OTP_LENGTH-digit code." }
        return authRepository.verifyOtp(challengeId, code)
    }

    private companion object {
        const val OTP_LENGTH = 4
    }
}
