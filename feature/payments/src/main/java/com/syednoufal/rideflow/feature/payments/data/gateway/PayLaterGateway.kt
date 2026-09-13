package com.syednoufal.rideflow.feature.payments.data.gateway

import com.syednoufal.rideflow.core.common.dispatcher.DispatcherProvider
import com.syednoufal.rideflow.core.domain.gateway.PaymentGateway
import com.syednoufal.rideflow.core.domain.model.PaymentMethod
import com.syednoufal.rideflow.core.domain.model.PaymentMethodType
import com.syednoufal.rideflow.core.domain.model.PaymentResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

/**
 * A simulated buy-now-pay-later integration: charges are always "approved"
 * immediately (the rider is billed on a later statement rather than at trip
 * completion) — the third of RideFlow's interchangeable [PaymentGateway]
 * implementations, included to show the abstraction accommodating a
 * fundamentally different settlement model without any call-site changes.
 */
class PayLaterGateway @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
) : PaymentGateway {

    override val type: PaymentMethodType = PaymentMethodType.PAY_LATER

    override suspend fun charge(method: PaymentMethod, amount: Double): PaymentResult =
        withContext(dispatcherProvider.io) {
            delay(APPROVAL_LATENCY_MILLIS)
            PaymentResult.Approved(transactionId = "later-txn-${UUID.randomUUID()}", amountCharged = amount)
        }

    override suspend fun addPaymentMethod(displayLabel: String): PaymentMethod = withContext(dispatcherProvider.io) {
        delay(APPROVAL_LATENCY_MILLIS)
        PaymentMethod(
            id = "pm-later-${UUID.randomUUID()}",
            type = type,
            displayLabel = displayLabel.ifBlank { "Pay Later · Net 15" },
            isDefault = false,
        )
    }

    private companion object {
        const val APPROVAL_LATENCY_MILLIS = 350L
    }
}
