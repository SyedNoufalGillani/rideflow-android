package com.syednoufal.rideflow.core.domain.usecase.auth

import com.syednoufal.rideflow.core.domain.model.OtpChallenge
import com.syednoufal.rideflow.core.domain.repository.AuthRepository
import javax.inject.Inject

/** Requests a one-time login code for a phone number, per the `invoke()` use-case convention. */
class RequestOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phoneNumber: String): OtpChallenge {
        require(phoneNumber.length >= MIN_PHONE_NUMBER_LENGTH) { "Enter a valid phone number." }
        return authRepository.requestOtp(phoneNumber)
    }

    private companion object {
        const val MIN_PHONE_NUMBER_LENGTH = 8
    }
}
