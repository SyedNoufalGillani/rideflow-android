package com.syednoufal.rideflow.feature.payments.di

import com.syednoufal.rideflow.core.domain.gateway.PaymentGateway
import com.syednoufal.rideflow.core.domain.repository.PaymentRepository
import com.syednoufal.rideflow.feature.payments.data.gateway.CardGateway
import com.syednoufal.rideflow.feature.payments.data.gateway.PayLaterGateway
import com.syednoufal.rideflow.feature.payments.data.gateway.WalletGateway
import com.syednoufal.rideflow.feature.payments.data.repository.FakePaymentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Registers every [PaymentGateway] implementation into a Hilt multibinding
 * [Set], and binds the repository that dispatches to them by
 * [com.syednoufal.rideflow.core.domain.model.PaymentMethodType]. Adding a
 * fourth gateway is a matter of implementing [PaymentGateway] and adding one
 * `@Binds @IntoSet` here — no other call site changes.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentsModule {

    @Binds
    @IntoSet
    abstract fun bindCardGateway(impl: CardGateway): PaymentGateway

    @Binds
    @IntoSet
    abstract fun bindWalletGateway(impl: WalletGateway): PaymentGateway

    @Binds
    @IntoSet
    abstract fun bindPayLaterGateway(impl: PayLaterGateway): PaymentGateway

    @Binds
    abstract fun bindPaymentRepository(impl: FakePaymentRepository): PaymentRepository
}
