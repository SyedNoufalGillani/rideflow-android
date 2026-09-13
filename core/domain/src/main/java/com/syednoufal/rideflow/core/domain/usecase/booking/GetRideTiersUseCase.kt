package com.syednoufal.rideflow.core.domain.usecase.booking

import com.syednoufal.rideflow.core.domain.model.RideTier
import com.syednoufal.rideflow.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Returns the catalogue of vehicle tiers a rider can pick between. */
class GetRideTiersUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(): List<RideTier> = bookingRepository.getRideTiers()
}
