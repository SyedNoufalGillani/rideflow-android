package com.syednoufal.rideflow.feature.auth.presentation.otp

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.syednoufal.rideflow.core.common.mvi.MviViewModel
import com.syednoufal.rideflow.core.domain.usecase.auth.RequestOtpUseCase
import com.syednoufal.rideflow.core.domain.usecase.auth.VerifyOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives OTP entry: verifies the code, and lets the rider request a fresh one. */
@HiltViewModel
class OtpViewModel @Inject constructor(
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val requestOtpUseCase: RequestOtpUseCase,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<OtpUiState, OtpIntent, OtpEffect>(
    OtpUiState(
        challengeId = checkNotNull(savedStateHandle[ARG_CHALLENGE_ID]) { "challengeId is required" },
        phoneNumber = Uri.decode(checkNotNull(savedStateHandle[ARG_PHONE_NUMBER]) { "phoneNumber is required" }),
    ),
) {

    override fun onIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.CodeChanged -> {
                val digitsOnly = intent.value.filter { it.isDigit() }.take(OtpUiState.OTP_LENGTH)
                setState { copy(code = digitsOnly, errorMessage = null) }
            }
            is OtpIntent.SubmitClicked -> submit()
            is OtpIntent.ResendClicked -> resend()
            is OtpIntent.ErrorDismissed -> setState { copy(errorMessage = null) }
        }
    }

    private fun submit() {
        if (!currentState.isSubmitEnabled) return

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            runCatching { verifyOtpUseCase(currentState.challengeId, currentState.code) }
                .onSuccess {
                    setState { copy(isLoading = false) }
                    sendEffect { OtpEffect.NavigateToHome }
                }
                .onFailure { throwable ->
                    setState {
                        copy(isLoading = false, code = "", errorMessage = throwable.message ?: "Verification failed.")
                    }
                }
        }
    }

    private fun resend() {
        viewModelScope.launch {
            setState { copy(isResending = true, errorMessage = null) }
            runCatching { requestOtpUseCase(currentState.phoneNumber) }
                .onSuccess { challenge ->
                    setState { copy(isResending = false, challengeId = challenge.challengeId, code = "") }
                    sendEffect { OtpEffect.ChallengeRefreshed(challenge.challengeId) }
                }
                .onFailure { throwable ->
                    setState { copy(isResending = false, errorMessage = throwable.message ?: "Couldn't resend code.") }
                }
        }
    }

    companion object {
        const val ARG_CHALLENGE_ID = "challengeId"
        const val ARG_PHONE_NUMBER = "phoneNumber"
    }
}
