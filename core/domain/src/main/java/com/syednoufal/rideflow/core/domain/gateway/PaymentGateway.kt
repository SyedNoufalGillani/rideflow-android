package com.syednoufal.rideflow.core.domain.gateway

import com.syednoufal.rideflow.core.domain.model.PaymentMethod
import com.syednoufal.rideflow.core.domain.model.PaymentMethodType
import com.syednoufal.rideflow.core.domain.model.PaymentResult

/**
 * Provider-agnostic contract every payment integration implements, mirroring
 * how a production app isolates a specific payment SDK behind a single
 * interface so it can be swapped, mocked, or run side-by-side with another
 * provider without touching call sites.
 *
 * [feature.payments] wires one concrete, fully-simulated implementation per
 * [PaymentMethodType] (card, wallet, pay-later) behind this interface, keyed
 * by [type] via a Hilt multibinding map — the same shape a real integration
 * of a card-network SDK or a wallet SDK would take, without naming or
 * depending on any actual third-party payment product.
 */
interface PaymentGateway {

    /** The [PaymentMethodType] this gateway instance is responsible for. */
    val type: PaymentMethodType

    /**
     * Charges [amount] (in the account's currency) against [method].
     * Implementations simulate realistic latency and a small failure rate,
     * exactly as a real gateway SDK call would behave from the app's point
     * of view.
     */
    suspend fun charge(method: PaymentMethod, amount: Double): PaymentResult

    /** Tokenizes and stores a new instance of this gateway's [PaymentMethodType] on the account. */
    suspend fun addPaymentMethod(displayLabel: String): PaymentMethod
}
