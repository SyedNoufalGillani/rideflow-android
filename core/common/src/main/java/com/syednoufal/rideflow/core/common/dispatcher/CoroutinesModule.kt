package com.syednoufal.rideflow.core.common.dispatcher

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds the production [DispatcherProvider] for the lifetime of the app. */
@Module
@InstallIn(SingletonComponent::class)
abstract class CoroutinesModule {

    @Binds
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}
