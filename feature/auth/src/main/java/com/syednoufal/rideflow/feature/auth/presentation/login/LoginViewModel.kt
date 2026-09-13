package com.syednoufal.rideflow.feature.auth.presentation.login

import androidx.lifecycle.viewModelScope
import com.syednoufal.rideflow.core.common.mvi.MviViewModel
import com.syednoufal.rideflow.core.domain.usecase.auth.RequestOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives the phone-number entry screen: validates input and requests an OTP challenge. */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val requestOtpUseCase: RequestOtpUseCase,
) : MviViewModel<LoginUiState, LoginIntent, LoginEffect>(LoginUiState()) {

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged -> setState { copy(phoneNumber = intent.value, errorMessage = null) }
            is LoginIntent.SubmitClicked -> submit()
            is LoginIntent.ErrorDismissed -> setState { copy(errorMessage = null) }
        }
    }

    private fun submit() {
        if (!currentState.isSubmitEnabled) return

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            runCatching { requestOtpUseCase(currentState.phoneNumber.trim()) }
                .onSuccess { challenge ->
                    setState { copy(isLoading = false) }
                    sendEffect { LoginEffect.NavigateToOtp(challenge.challengeId, challenge.phoneNumber) }
                }
                .onFailure { throwable ->
                    setState {
                        copy(isLoading = false, errorMessage = throwable.message ?: "Something went wrong.")
                    }
                }
        }
    }
}
