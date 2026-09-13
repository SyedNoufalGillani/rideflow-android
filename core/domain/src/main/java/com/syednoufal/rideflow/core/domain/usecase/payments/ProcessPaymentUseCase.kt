package com.syednoufal.rideflow.core.domain.usecase.payments

import com.syednoufal.rideflow.core.domain.model.PaymentResult
import com.syednoufal.rideflow.core.domain.repository.PaymentRepository
import javax.inject.Inject

/** Charges a completed trip's fare to the selected payment method. */
class ProcessPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(methodId: String, amount: Double): PaymentResult {
        require(amount > 0.0) { "Charge amount must be positive." }
        return paymentRepository.charge(methodId, amount)
    }
}
