package com.syednoufal.rideflow.core.domain.usecase.auth

import com.syednoufal.rideflow.core.domain.model.User
import com.syednoufal.rideflow.core.domain.repository.AuthRepository
import javax.inject.Inject

/** Returns the currently authenticated rider, or `null` when no session is active. */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): User? = authRepository.getCurrentUser()
}
