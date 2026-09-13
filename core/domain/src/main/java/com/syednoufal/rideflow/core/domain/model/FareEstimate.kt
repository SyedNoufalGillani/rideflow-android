package com.syednoufal.rideflow.core.domain.model

/**
 * A priced quote for a single [rideTier] between a pickup and dropoff point.
 * All monetary fields are in [currencyCode] minor-unit-free decimal form
 * (e.g. `12.50`), matching what a real pricing backend would return.
 */
data class FareEstimate(
    val rideTierType: RideTierType,
    val currencyCode: String,
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val surgeMultiplier: Double,
    val estimatedDistanceKm: Double,
    val estimatedDurationMinutes: Int,
) {
    /** Total price the rider will be charged, surge included. */
    val total: Double
        get() = (baseFare + distanceFare + timeFare) * surgeMultiplier
}
