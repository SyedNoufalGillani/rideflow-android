package com.syednoufal.rideflow.feature.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syednoufal.rideflow.core.designsystem.component.RideFlowPrimaryButton
import com.syednoufal.rideflow.core.designsystem.component.RideFlowTextField
import com.syednoufal.rideflow.core.designsystem.theme.RideFlowTheme

/**
 * The rider's entry point: enter a phone number to receive a login code.
 * [onNavigateToOtp] is invoked once the OTP challenge is created.
 */
@Composable
fun LoginScreen(
    onNavigateToOtp: (challengeId: String, phoneNumber: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToOtp -> onNavigateToOtp(effect.challengeId, effect.phoneNumber)
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onIntent(LoginIntent.ErrorDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(RideFlowTheme.spacing.large),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Welcome to RideFlow",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(RideFlowTheme.spacing.small))
            Text(
                text = "Enter your phone number and we'll send you a one-time code.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(RideFlowTheme.spacing.extraLarge))
            RideFlowTextField(
                value = state.phoneNumber,
                onValueChange = { viewModel.onIntent(LoginIntent.PhoneNumberChanged(it)) },
                label = "Phone number",
                placeholder = "+1 555 123 4567",
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
            )
            Spacer(modifier = Modifier.height(RideFlowTheme.spacing.large))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                RideFlowPrimaryButton(
                    label = "Send code",
                    onClick = { viewModel.onIntent(LoginIntent.SubmitClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.isSubmitEnabled,
                    isLoading = state.isLoading,
                )
            }
        }
    }
}
