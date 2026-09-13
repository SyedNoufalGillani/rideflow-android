package com.syednoufal.rideflow.core.domain.usecase.booking

import com.syednoufal.rideflow.core.domain.model.Trip
import com.syednoufal.rideflow.core.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the driver-matching state machine for a requested [Trip] until pickup. */
class ObserveDriverMatchUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    operator fun invoke(tripId: String): Flow<Trip> = bookingRepository.observeDriverMatching(tripId)
}
