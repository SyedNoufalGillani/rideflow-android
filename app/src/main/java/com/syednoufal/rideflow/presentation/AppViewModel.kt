package com.syednoufal.rideflow.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syednoufal.rideflow.core.domain.usecase.auth.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Determines the app's start destination. Exposed as `null` until the first
 * auth-state emission arrives, which [com.syednoufal.rideflow.MainActivity]
 * uses to keep the platform splash screen visible rather than flashing a
 * login screen the rider may not actually need.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { loggedIn ->
                _isLoggedIn.value = loggedIn
            }
        }
    }
}
