package com.syednoufal.rideflow.core.domain.usecase.payments

import com.syednoufal.rideflow.core.domain.repository.PaymentRepository
import javax.inject.Inject

/** Marks a payment method as the rider's default for future charges. */
class SetDefaultPaymentMethodUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(methodId: String) = paymentRepository.setDefaultPaymentMethod(methodId)
}
