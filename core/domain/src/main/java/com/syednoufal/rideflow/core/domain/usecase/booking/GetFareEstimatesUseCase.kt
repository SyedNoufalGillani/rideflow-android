package com.syednoufal.rideflow.core.domain.usecase.booking

import com.syednoufal.rideflow.core.domain.model.FareEstimate
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Prices every ride tier for a chosen pickup/dropoff pair. */
class GetFareEstimatesUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(pickup: GeoPoint, dropoff: GeoPoint): List<FareEstimate> {
        require(pickup != dropoff) { "Pickup and dropoff must be different locations." }
        return bookingRepository.getFareEstimates(pickup, dropoff)
    }
}
