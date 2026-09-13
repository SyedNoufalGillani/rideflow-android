package com.syednoufal.rideflow.core.domain.usecase.tracking

import com.syednoufal.rideflow.core.domain.model.Trip
import com.syednoufal.rideflow.core.domain.repository.TrackingRepository
import javax.inject.Inject

/** One-shot fetch of a trip's current snapshot. */
class GetTripUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository,
) {
    suspend operator fun invoke(tripId: String): Trip = trackingRepository.getTrip(tripId)
}
