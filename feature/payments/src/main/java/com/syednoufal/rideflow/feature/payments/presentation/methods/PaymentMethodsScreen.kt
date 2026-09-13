package com.syednoufal.rideflow.feature.payments.presentation.methods

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syednoufal.rideflow.core.designsystem.component.RideFlowLoadingState
import com.syednoufal.rideflow.core.designsystem.component.RideFlowPrimaryButton
import com.syednoufal.rideflow.core.designsystem.component.RideFlowSelectableRow
import com.syednoufal.rideflow.core.designsystem.theme.RideFlowTheme
import com.syednoufal.rideflow.core.domain.model.PaymentMethod
import com.syednoufal.rideflow.core.domain.model.PaymentMethodType
import java.util.Locale

/** Lets the rider pick a payment method and charges it for the completed trip's fare. */
@Composable
fun PaymentMethodsScreen(
    onPaymentApproved: (tripId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentMethodsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PaymentMethodsEffect.PaymentApproved -> onPaymentApproved(effect.tripId)
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onIntent(PaymentMethodsIntent.ErrorDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (state.isLoading) {
            RideFlowLoadingState(modifier = Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(RideFlowTheme.spacing.large),
            ) {
                Text(text = "Trip complete", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(RideFlowTheme.spacing.small))
                Text(
                    text = "Total due: ${formatCurrency(state.amountDue)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(RideFlowTheme.spacing.large))
                Text(text = "Pay with", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(RideFlowTheme.spacing.small))

                state.methods.forEach { method ->
                    PaymentMethodRow(
                        method = method,
                        selected = state.selectedMethodId == method.id,
                        onClick = { viewModel.onIntent(PaymentMethodsIntent.MethodSelected(method.id)) },
                    )
                    Spacer(modifier = Modifier.height(RideFlowTheme.spacing.extraSmall))
                }

                Spacer(modifier = Modifier.height(RideFlowTheme.spacing.large))
                RideFlowPrimaryButton(
                    label = "Pay ${formatCurrency(state.amountDue)}",
                    onClick = { viewModel.onIntent(PaymentMethodsIntent.PayClicked) },
                    enabled = state.isPayEnabled,
                    isLoading = state.isCharging,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(method: PaymentMethod, selected: Boolean, onClick: () -> Unit) {
    RideFlowSelectableRow(selected = selected, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = iconFor(method.type),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = method.displayLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = RideFlowTheme.spacing.small),
        )
        if (method.isDefault) {
            Text(
                text = "Default",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun iconFor(type: PaymentMethodType): ImageVector = when (type) {
    PaymentMethodType.CARD -> Icons.Filled.CreditCard
    PaymentMethodType.WALLET -> Icons.Filled.AccountBalanceWallet
    PaymentMethodType.PAY_LATER -> Icons.Filled.Schedule
    PaymentMethodType.CASH -> Icons.Filled.AccountBalanceWallet
}

private fun formatCurrency(amount: Double): String = "$${String.format(Locale.US, "%.2f", amount)}"
