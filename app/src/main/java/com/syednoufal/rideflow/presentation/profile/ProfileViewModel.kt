package com.syednoufal.rideflow.presentation.profile

import androidx.lifecycle.viewModelScope
import com.syednoufal.rideflow.core.common.mvi.MviViewModel
import com.syednoufal.rideflow.core.domain.usecase.auth.GetCurrentUserUseCase
import com.syednoufal.rideflow.core.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives the rider's profile screen: loading their details and signing them out. */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
) : MviViewModel<ProfileUiState, ProfileIntent, ProfileEffect>(ProfileUiState()) {

    init {
        loadUser()
    }

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LogoutClicked -> logout()
            is ProfileIntent.ErrorDismissed -> setState { copy(errorMessage = null) }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            runCatching { getCurrentUserUseCase() }
                .onSuccess { user -> setState { copy(isLoading = false, user = user) } }
                .onFailure { throwable ->
                    setState {
                        copy(isLoading = false, errorMessage = throwable.message ?: "Couldn't load your profile.")
                    }
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            setState { copy(isLoggingOut = true) }
            logoutUseCase()
            setState { copy(isLoggingOut = false) }
            sendEffect { ProfileEffect.NavigateToLogin }
        }
    }
}
