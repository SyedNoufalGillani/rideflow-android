package com.syednoufal.rideflow.core.domain.usecase.auth

import com.syednoufal.rideflow.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the rider's authentication state, used to drive the app's start destination. */
class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<Boolean> = authRepository.observeIsLoggedIn()
}
