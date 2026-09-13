package com.syednoufal.rideflow.core.domain.model

/** A single ride, from request through to completion, and its current [status]. */
data class Trip(
    val id: String,
    val pickup: GeoPoint,
    val dropoff: GeoPoint,
    val rideTierType: RideTierType,
    val fareEstimate: FareEstimate,
    val status: TripStatus,
    val requestedAtEpochMillis: Long,
)
