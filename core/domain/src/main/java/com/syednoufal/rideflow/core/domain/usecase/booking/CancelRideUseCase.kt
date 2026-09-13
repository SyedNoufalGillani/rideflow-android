package com.syednoufal.rideflow.core.domain.usecase.booking

import com.syednoufal.rideflow.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Cancels an in-flight ride request or a not-yet-picked-up trip. */
class CancelRideUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(tripId: String) = bookingRepository.cancelRide(tripId)
}
