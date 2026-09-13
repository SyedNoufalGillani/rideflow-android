package com.syednoufal.rideflow.feature.auth.presentation.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syednoufal.rideflow.core.designsystem.component.RideFlowPrimaryButton
import com.syednoufal.rideflow.core.designsystem.component.RideFlowTextField
import com.syednoufal.rideflow.core.designsystem.theme.RideFlowTheme

/** Verifies the one-time code sent to the rider's phone number. */
@Composable
fun OtpScreen(
    onVerified: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OtpEffect.NavigateToHome -> onVerified()
                is OtpEffect.ChallengeRefreshed -> snackbarHostState.showSnackbar("We sent you a new code.")
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onIntent(OtpIntent.ErrorDismissed)
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
                text = "Enter the code",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(RideFlowTheme.spacing.small))
            Text(
                text = "We sent a ${OtpUiState.OTP_LENGTH}-digit code to ${state.phoneNumber}. " +
                    "(Demo tip: the code is always 1234.)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(RideFlowTheme.spacing.extraLarge))
            RideFlowTextField(
                value = state.code,
                onValueChange = { viewModel.onIntent(OtpIntent.CodeChanged(it)) },
                label = "Verification code",
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done,
            )
            Spacer(modifier = Modifier.height(RideFlowTheme.spacing.large))
            RideFlowPrimaryButton(
                label = "Verify",
                onClick = { viewModel.onIntent(OtpIntent.SubmitClicked) },
                enabled = state.isSubmitEnabled,
                isLoading = state.isLoading,
                modifier = Modifier,
            )
            Spacer(modifier = Modifier.height(RideFlowTheme.spacing.medium))
            TextButton(
                onClick = { viewModel.onIntent(OtpIntent.ResendClicked) },
                enabled = !state.isResending,
            ) {
                Text(if (state.isResending) "Sending..." else "Resend code")
            }
        }
    }
}
