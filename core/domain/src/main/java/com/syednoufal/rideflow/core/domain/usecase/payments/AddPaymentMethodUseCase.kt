package com.syednoufal.rideflow.core.domain.usecase.payments

import com.syednoufal.rideflow.core.domain.model.PaymentMethod
import com.syednoufal.rideflow.core.domain.model.PaymentMethodType
import com.syednoufal.rideflow.core.domain.repository.PaymentRepository
import javax.inject.Inject

/** Adds a new payment method of [type] to the rider's account. */
class AddPaymentMethodUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(type: PaymentMethodType, displayLabel: String): PaymentMethod {
        require(displayLabel.isNotBlank()) { "Payment method label must not be blank." }
        return paymentRepository.addPaymentMethod(type, displayLabel)
    }
}
