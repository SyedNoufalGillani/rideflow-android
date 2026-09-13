package com.syednoufal.rideflow.core.domain.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A geographic coordinate paired with an optional human-readable [label] (e.g. "Home"). */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val label: String? = null,
    val address: String = "",
) {

    /**
     * Great-circle distance to [other] in kilometers, using the haversine
     * formula. Accurate enough for fare-estimation purposes over short urban
     * distances without pulling in a maps SDK.
     */
    fun distanceKmTo(other: GeoPoint): Double {
        val earthRadiusKm = 6371.0
        val deltaLat = Math.toRadians(other.latitude - latitude)
        val deltaLon = Math.toRadians(other.longitude - longitude)
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) *
            sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
