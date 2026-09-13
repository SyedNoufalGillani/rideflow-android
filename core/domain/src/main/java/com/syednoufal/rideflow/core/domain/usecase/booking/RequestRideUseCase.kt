package com.syednoufal.rideflow.core.domain.usecase.booking

import com.syednoufal.rideflow.core.domain.model.FareEstimate
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.model.RideTierType
import com.syednoufal.rideflow.core.domain.model.Trip
import com.syednoufal.rideflow.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Submits a ride request and kicks off driver matching for the returned [Trip]. */
class RequestRideUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(
        pickup: GeoPoint,
        dropoff: GeoPoint,
        rideTierType: RideTierType,
        fareEstimate: FareEstimate,
    ): Trip = bookingRepository.requestRide(pickup, dropoff, rideTierType, fareEstimate)
}
