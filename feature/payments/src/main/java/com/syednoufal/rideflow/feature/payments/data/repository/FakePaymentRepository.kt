package com.syednoufal.rideflow.feature.payments.data.repository

import com.syednoufal.rideflow.core.common.dispatcher.DispatcherProvider
import com.syednoufal.rideflow.core.domain.gateway.PaymentGateway
import com.syednoufal.rideflow.core.domain.model.DomainException
import com.syednoufal.rideflow.core.domain.model.PaymentMethod
import com.syednoufal.rideflow.core.domain.model.PaymentMethodType
import com.syednoufal.rideflow.core.domain.model.PaymentResult
import com.syednoufal.rideflow.core.domain.repository.PaymentRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A fully simulated [PaymentRepository]. Holds the rider's payment methods
 * in memory (a real implementation would persist them locally and sync them
 * with a backend) and delegates every charge or tokenization to the
 * [PaymentGateway] registered for that method's [PaymentMethodType] — the
 * repository itself never knows how a card, wallet or pay-later charge is
 * actually processed.
 */
@Singleton
class FakePaymentRepository @Inject constructor(
    private val gateways: Set<@JvmSuppressWildcards PaymentGateway>,
    private val dispatcherProvider: DispatcherProvider,
) : PaymentRepository {

    private val gatewaysByType: Map<PaymentMethodType, PaymentGateway> by lazy { gateways.associateBy { it.type } }
    private val mutex = Mutex()
    private val paymentMethods = mutableListOf(
        PaymentMethod(
            id = "pm-seed-card",
            type = PaymentMethodType.CARD,
            displayLabel = "Visa •••• 4242",
            isDefault = true,
        ),
        PaymentMethod(
            id = "pm-seed-wallet",
            type = PaymentMethodType.WALLET,
            displayLabel = "RideFlow Wallet",
            isDefault = false,
        ),
    )

    override suspend fun getPaymentMethods(): List<PaymentMethod> = withContext(dispatcherProvider.io) {
        mutex.withLock { paymentMethods.toList() }
    }

    override suspend fun addPaymentMethod(type: PaymentMethodType, displayLabel: String): PaymentMethod =
        withContext(dispatcherProvider.io) {
            val gateway = gatewayFor(type)
            val method = gateway.addPaymentMethod(displayLabel)
            mutex.withLock { paymentMethods.add(method) }
            method
        }

    override suspend fun setDefaultPaymentMethod(methodId: String) = withContext(dispatcherProvider.io) {
        mutex.withLock {
            for (index in paymentMethods.indices) {
                paymentMethods[index] = paymentMethods[index].copy(isDefault = paymentMethods[index].id == methodId)
            }
        }
    }

    override suspend fun charge(methodId: String, amount: Double): PaymentResult = withContext(dispatcherProvider.io) {
        val method = mutex.withLock { paymentMethods.find { it.id == methodId } }
            ?: throw DomainException.PaymentDeclinedException("That payment method is no longer available.")
        gatewayFor(method.type).charge(method, amount)
    }

    private fun gatewayFor(type: PaymentMethodType): PaymentGateway = gatewaysByType[type]
        ?: throw DomainException.PaymentDeclinedException("$type is not a supported payment method.")
}
