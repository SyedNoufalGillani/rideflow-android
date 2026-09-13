package com.syednoufal.rideflow.core.domain.model

/** A driver matched to a rider's trip, including live positioning data. */
data class Driver(
    val id: String,
    val fullName: String,
    val photoUrl: String?,
    val rating: Float,
    val vehicleModel: String,
    val vehiclePlate: String,
    val phoneNumber: String,
    val currentLocation: GeoPoint,
)
