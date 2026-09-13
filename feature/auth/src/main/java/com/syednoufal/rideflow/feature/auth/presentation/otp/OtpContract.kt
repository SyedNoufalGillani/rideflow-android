package com.syednoufal.rideflow.feature.auth.presentation.otp

import com.syednoufal.rideflow.core.common.mvi.UiEffect
import com.syednoufal.rideflow.core.common.mvi.UiIntent
import com.syednoufal.rideflow.core.common.mvi.UiState

/** The OTP verification screen's rendered state. */
data class OtpUiState(
    val challengeId: String = "",
    val phoneNumber: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null,
) : UiState {
    val isSubmitEnabled: Boolean
        get() = !isLoading && code.length == OTP_LENGTH

    companion object {
        const val OTP_LENGTH = 4
    }
}

sealed interface OtpIntent : UiIntent {
    data class CodeChanged(val value: String) : OtpIntent
    data object SubmitClicked : OtpIntent
    data object ResendClicked : OtpIntent
    data object ErrorDismissed : OtpIntent
}

sealed interface OtpEffect : UiEffect {
    data object NavigateToHome : OtpEffect
    data class ChallengeRefreshed(val challengeId: String) : OtpEffect
}
