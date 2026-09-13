package com.syednoufal.rideflow.core.domain.usecase.booking

import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Returns the rider's saved/recent pickup and dropoff points. */
class GetSavedPlacesUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(): List<GeoPoint> = bookingRepository.getSavedPlaces()
}
