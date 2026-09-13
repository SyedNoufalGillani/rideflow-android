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
import kotlin.random.Random

/**
 * A simulated in-app wallet integration (a prepaid or stored-value balance
 * held on the rider's account) — the second of RideFlow's interchangeable
 * [PaymentGateway] implementations.
 */
class WalletGateway @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
) : PaymentGateway {

    override val type: PaymentMethodType = PaymentMethodType.WALLET

    override suspend fun charge(method: PaymentMethod, amount: Double): PaymentResult =
        withContext(dispatcherProvider.io) {
            delay(CHARGE_LATENCY_MILLIS)
            if (amount > WALLET_BALANCE_LIMIT) {
                PaymentResult.Declined(reason = "Wallet balance is too low for this trip.")
            } else if (Random.nextFloat() < DECLINE_CHANCE) {
                PaymentResult.Declined(reason = "Wallet charge could not be completed.")
            } else {
                PaymentResult.Approved(transactionId = "wallet-txn-${UUID.randomUUID()}", amountCharged = amount)
            }
        }

    override suspend fun addPaymentMethod(displayLabel: String): PaymentMethod = withContext(dispatcherProvider.io) {
        delay(TOKENIZE_LATENCY_MILLIS)
        PaymentMethod(
            id = "pm-wallet-${UUID.randomUUID()}",
            type = type,
            displayLabel = displayLabel.ifBlank { "RideFlow Wallet" },
            isDefault = false,
        )
    }

    private companion object {
        const val CHARGE_LATENCY_MILLIS = 500L
        const val TOKENIZE_LATENCY_MILLIS = 400L
        const val DECLINE_CHANCE = 0.03f
        const val WALLET_BALANCE_LIMIT = 500.0
    }
}
