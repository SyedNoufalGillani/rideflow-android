package com.syednoufal.rideflow.core.domain.usecase.auth

import com.syednoufal.rideflow.core.domain.repository.AuthRepository
import javax.inject.Inject

/** Clears the rider's local session. */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.logout()
}
