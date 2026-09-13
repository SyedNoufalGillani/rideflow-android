package com.syednoufal.rideflow.core.domain.usecase.tracking

import com.syednoufal.rideflow.core.domain.model.Trip
import com.syednoufal.rideflow.core.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams live location, ETA, and status updates for an in-progress trip. */
class ObserveActiveTripUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository,
) {
    operator fun invoke(tripId: String): Flow<Trip> = trackingRepository.observeActiveTrip(tripId)
}
