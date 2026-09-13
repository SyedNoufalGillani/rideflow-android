package com.syednoufal.rideflow.core.domain.model

/**
 * The provider-agnostic category of a payment method. Concrete gateway
 * integrations (card processor, in-app wallet, buy-now-pay-later provider)
 * are selected behind [com.syednoufal.rideflow.core.domain.gateway.PaymentGateway]
 * implementations keyed by this type — the domain layer never knows which
 * concrete SDK, if any, backs a given type.
 */
enum class PaymentMethodType {
    CARD,
    WALLET,
    PAY_LATER,
    CASH,
}

/** A payment method saved to (or available on) a rider's account. */
data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val displayLabel: String,
    val isDefault: Boolean,
)

/** The outcome of attempting to charge a [PaymentMethod] for a completed trip. */
sealed class PaymentResult {
    data class Approved(val transactionId: String, val amountCharged: Double) : PaymentResult()
    data class Declined(val reason: String) : PaymentResult()
}
