package com.syednoufal.rideflow.feature.payments.presentation.methods

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.syednoufal.rideflow.core.common.mvi.MviViewModel
import com.syednoufal.rideflow.core.domain.model.PaymentResult
import com.syednoufal.rideflow.core.domain.usecase.payments.GetPaymentMethodsUseCase
import com.syednoufal.rideflow.core.domain.usecase.payments.ProcessPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives payment method selection and charges the rider once a trip completes. */
@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<PaymentMethodsUiState, PaymentMethodsIntent, PaymentMethodsEffect>(
    PaymentMethodsUiState(
        tripId = checkNotNull(savedStateHandle[ARG_TRIP_ID]) { "tripId is required" },
        amountDue = readAmountDue(savedStateHandle),
    ),
) {

    init {
        loadPaymentMethods()
    }

    override fun onIntent(intent: PaymentMethodsIntent) {
        when (intent) {
            is PaymentMethodsIntent.MethodSelected -> setState { copy(selectedMethodId = intent.methodId) }
            is PaymentMethodsIntent.PayClicked -> pay()
            is PaymentMethodsIntent.ErrorDismissed -> setState { copy(errorMessage = null) }
        }
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            runCatching { getPaymentMethodsUseCase() }
                .onSuccess { methods ->
                    setState {
                        copy(
                            isLoading = false,
                            methods = methods,
                            selectedMethodId = selectedMethodId ?: methods.find { it.isDefault }?.id
                                ?: methods.firstOrNull()?.id,
                        )
                    }
                }
                .onFailure { throwable ->
                    setState {
                        copy(isLoading = false, errorMessage = throwable.message ?: "Couldn't load payment methods.")
                    }
                }
        }
    }

    private fun pay() {
        val methodId = currentState.selectedMethodId ?: return
        viewModelScope.launch {
            setState { copy(isCharging = true, errorMessage = null) }
            runCatching { processPaymentUseCase(methodId, currentState.amountDue) }
                .onSuccess { result ->
                    when (result) {
                        is PaymentResult.Approved -> {
                            setState { copy(isCharging = false, isCompleted = true) }
                            sendEffect {
                                PaymentMethodsEffect.PaymentApproved(currentState.tripId, result.transactionId)
                            }
                        }
                        is PaymentResult.Declined -> setState {
                            copy(isCharging = false, errorMessage = result.reason)
                        }
                    }
                }
                .onFailure { throwable ->
                    setState { copy(isCharging = false, errorMessage = throwable.message ?: "Payment failed.") }
                }
        }
    }

    companion object {
        const val ARG_TRIP_ID = "tripId"
        const val ARG_AMOUNT = "amount"

        /** Nav arguments carry a `Float` (navigation-compose has no `Double` NavType); the domain layer works in `Double`. */
        private fun readAmountDue(savedStateHandle: SavedStateHandle): Double {
            val amount: Float? = savedStateHandle[ARG_AMOUNT]
            return checkNotNull(amount) { "amount is required" }.toDouble()
        }
    }
}
