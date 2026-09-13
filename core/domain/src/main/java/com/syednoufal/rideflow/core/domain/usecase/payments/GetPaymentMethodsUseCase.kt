package com.syednoufal.rideflow.core.domain.usecase.payments

import com.syednoufal.rideflow.core.domain.model.PaymentMethod
import com.syednoufal.rideflow.core.domain.repository.PaymentRepository
import javax.inject.Inject

/** Returns every payment method saved to the rider's account. */
class GetPaymentMethodsUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(): List<PaymentMethod> = paymentRepository.getPaymentMethods()
}
