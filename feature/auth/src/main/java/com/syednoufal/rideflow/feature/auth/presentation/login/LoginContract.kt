package com.syednoufal.rideflow.feature.auth.presentation.login

import com.syednoufal.rideflow.core.common.mvi.UiEffect
import com.syednoufal.rideflow.core.common.mvi.UiIntent
import com.syednoufal.rideflow.core.common.mvi.UiState

/** The login screen's rendered state: a single phone-number field plus request status. */
data class LoginUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : UiState {
    val isSubmitEnabled: Boolean
        get() = !isLoading && phoneNumber.trim().length >= MIN_PHONE_LENGTH

    private companion object {
        const val MIN_PHONE_LENGTH = 8
    }
}

sealed interface LoginIntent : UiIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data object SubmitClicked : LoginIntent
    data object ErrorDismissed : LoginIntent
}

sealed interface LoginEffect : UiEffect {
    data class NavigateToOtp(val challengeId: String, val phoneNumber: String) : LoginEffect
}
