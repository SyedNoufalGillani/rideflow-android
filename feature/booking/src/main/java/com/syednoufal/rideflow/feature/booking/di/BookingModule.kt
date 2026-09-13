package com.syednoufal.rideflow.feature.booking.di

import com.syednoufal.rideflow.core.domain.repository.BookingRepository
import com.syednoufal.rideflow.feature.booking.BuildConfig
import com.syednoufal.rideflow.feature.booking.data.repository.FakeBookingRepository
import com.syednoufal.rideflow.feature.booking.data.repository.RealBookingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Selects the [BookingRepository] implementation — fake by default, real when configured. */
@Module
@InstallIn(SingletonComponent::class)
object BookingModule {

    @Provides
    @Singleton
    fun provideBookingRepository(
        fakeBookingRepository: FakeBookingRepository,
        realBookingRepository: RealBookingRepository,
    ): BookingRepository = if (BuildConfig.USE_FAKE_DATA_SOURCE) fakeBookingRepository else realBookingRepository
}
