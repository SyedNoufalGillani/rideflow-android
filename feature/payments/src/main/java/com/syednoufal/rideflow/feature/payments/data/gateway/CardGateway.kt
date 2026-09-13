package com.syednoufal.rideflow.feature.payments.data.gateway

import com.syednoufal.rideflow.core.common.dispatcher.DispatcherProvider
import com.syednoufal.rideflow.core.domain.gateway.PaymentGateway
import com.syednoufal.rideflow.core.domain.model.DomainException
import com.syednoufal.rideflow.core.domain.model.PaymentMethod
import com.syednoufal.rideflow.core.domain.model.PaymentMethodType
import com.syednoufal.rideflow.core.domain.model.PaymentResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/**
 * A simulated card-network integration. Stands in for wherever a real app
 * would embed a specific card-processing SDK behind this same
 * [PaymentGateway] contract — no such SDK is named or depended on here.
 */
class CardGateway @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
) : PaymentGateway {

    override val type: PaymentMethodType = PaymentMethodType.CARD

    override suspend fun charge(method: PaymentMethod, amount: Double): PaymentResult =
        withContext(dispatcherProvider.io) {
            delay(CHARGE_LATENCY_MILLIS)
            if (Random.nextFloat() < DECLINE_CHANCE) {
                PaymentResult.Declined(reason = "Card declined by issuing bank.")
            } else {
                PaymentResult.Approved(transactionId = "card-txn-${UUID.randomUUID()}", amountCharged = amount)
            }
        }

    override suspend fun addPaymentMethod(displayLabel: String): PaymentMethod =
        withContext(dispatcherProvider.io) {
            delay(TOKENIZE_LATENCY_MILLIS)
            if (displayLabel.isBlank()) throw DomainException.PaymentDeclinedException("Card details are incomplete.")
            PaymentMethod(
                id = "pm-card-${UUID.randomUUID()}",
                type = type,
                displayLabel = displayLabel,
                isDefault = false,
            )
        }

    private companion object {
        const val CHARGE_LATENCY_MILLIS = 900L
        const val TOKENIZE_LATENCY_MILLIS = 700L
        const val DECLINE_CHANCE = 0.08f
    }
}
