package com.syednoufal.rideflow.feature.tracking.di

import com.syednoufal.rideflow.core.domain.repository.TrackingRepository
import com.syednoufal.rideflow.feature.tracking.data.repository.FakeTrackingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds [FakeTrackingRepository] as the app's only [TrackingRepository]
 * implementation. Unlike auth/booking there is no real/fake toggle here —
 * see [FakeTrackingRepository]'s KDoc for why live location streaming
 * doesn't fit the same Retrofit-shaped real implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingModule {

    @Binds
    abstract fun bindTrackingRepository(impl: FakeTrackingRepository): TrackingRepository
}
