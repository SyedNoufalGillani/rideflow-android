package com.syednoufal.rideflow.feature.payments.presentation.methods

import com.syednoufal.rideflow.core.common.mvi.UiEffect
import com.syednoufal.rideflow.core.common.mvi.UiIntent
import com.syednoufal.rideflow.core.common.mvi.UiState
import com.syednoufal.rideflow.core.domain.model.PaymentMethod

/** The payment method selection / trip-charge screen's rendered state. */
data class PaymentMethodsUiState(
    val tripId: String = "",
    val amountDue: Double = 0.0,
    val methods: List<PaymentMethod> = emptyList(),
    val selectedMethodId: String? = null,
    val isLoading: Boolean = true,
    val isCharging: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null,
) : UiState {
    val isPayEnabled: Boolean
        get() = !isCharging && selectedMethodId != null
}

sealed interface PaymentMethodsIntent : UiIntent {
    data class MethodSelected(val methodId: String) : PaymentMethodsIntent
    data object PayClicked : PaymentMethodsIntent
    data object ErrorDismissed : PaymentMethodsIntent
}

sealed interface PaymentMethodsEffect : UiEffect {
    data class PaymentApproved(val tripId: String, val transactionId: String) : PaymentMethodsEffect
}
