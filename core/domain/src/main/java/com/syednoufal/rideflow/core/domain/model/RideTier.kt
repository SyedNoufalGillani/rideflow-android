package com.syednoufal.rideflow.core.domain.model

/** The catalogue of vehicle tiers a rider can choose from when requesting a ride. */
enum class RideTierType {
    ECONOMY,
    COMFORT,
    XL,
    PREMIUM,
}

/**
 * A selectable ride tier shown on the booking screen, combining static
 * catalogue data ([type], [displayName], [capacity]) with a per-request
 * [baseFareMultiplier] used by fare estimation.
 */
data class RideTier(
    val type: RideTierType,
    val displayName: String,
    val description: String,
    val capacity: Int,
    val baseFareMultiplier: Double,
    val etaMinutes: Int,
)
