package com.syednoufal.rideflow.presentation.profile

import com.syednoufal.rideflow.core.common.mvi.UiEffect
import com.syednoufal.rideflow.core.common.mvi.UiIntent
import com.syednoufal.rideflow.core.common.mvi.UiState
import com.syednoufal.rideflow.core.domain.model.User

/** The rider's profile screen: their details plus the entry point to logging out. */
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface ProfileIntent : UiIntent {
    data object LogoutClicked : ProfileIntent
    data object ErrorDismissed : ProfileIntent
}

sealed interface ProfileEffect : UiEffect {
    data object NavigateToLogin : ProfileEffect
}
