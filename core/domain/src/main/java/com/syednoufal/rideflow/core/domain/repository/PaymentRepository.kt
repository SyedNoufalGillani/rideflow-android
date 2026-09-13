package com.syednoufal.rideflow.core.domain.repository

import com.syednoufal.rideflow.core.domain.model.PaymentMethod
import com.syednoufal.rideflow.core.domain.model.PaymentMethodType
import com.syednoufal.rideflow.core.domain.model.PaymentResult

/**
 * Contract for managing and charging payment methods. Implementations
 * delegate the actual charge to a [com.syednoufal.rideflow.core.domain.gateway.PaymentGateway]
 * selected by [PaymentMethodType], keeping this repository provider-agnostic.
 */
interface PaymentRepository {

    /** All payment methods saved to the rider's account. */
    suspend fun getPaymentMethods(): List<PaymentMethod>

    /** Adds a new payment method of [type], delegating tokenization to the matching gateway. */
    suspend fun addPaymentMethod(type: PaymentMethodType, displayLabel: String): PaymentMethod

    /** Marks [methodId] as the rider's default payment method. */
    suspend fun setDefaultPaymentMethod(methodId: String)

    /** Charges [amount] to the payment method identified by [methodId] for a completed trip. */
    suspend fun charge(methodId: String, amount: Double): PaymentResult
}
