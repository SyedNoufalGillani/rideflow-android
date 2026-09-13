package com.syednoufal.rideflow.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoPointDto(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("label") val label: String? = null,
    @SerialName("address") val address: String = "",
)

@Serializable
data class RideTierDto(
    @SerialName("type") val type: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("description") val description: String,
    @SerialName("capacity") val capacity: Int,
    @SerialName("base_fare_multiplier") val baseFareMultiplier: Double,
    @SerialName("eta_minutes") val etaMinutes: Int,
)

@Serializable
data class FareEstimateRequestDto(
    @SerialName("pickup") val pickup: GeoPointDto,
    @SerialName("dropoff") val dropoff: GeoPointDto,
)

@Serializable
data class FareEstimateDto(
    @SerialName("ride_tier_type") val rideTierType: String,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("base_fare") val baseFare: Double,
    @SerialName("distance_fare") val distanceFare: Double,
    @SerialName("time_fare") val timeFare: Double,
    @SerialName("surge_multiplier") val surgeMultiplier: Double,
    @SerialName("estimated_distance_km") val estimatedDistanceKm: Double,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int,
)

@Serializable
data class RideRequestDto(
    @SerialName("pickup") val pickup: GeoPointDto,
    @SerialName("dropoff") val dropoff: GeoPointDto,
    @SerialName("ride_tier_type") val rideTierType: String,
)

@Serializable
data class DriverDto(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("rating") val rating: Float,
    @SerialName("vehicle_model") val vehicleModel: String,
    @SerialName("vehicle_plate") val vehiclePlate: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("current_location") val currentLocation: GeoPointDto,
)

@Serializable
data class TripDto(
    @SerialName("id") val id: String,
    @SerialName("pickup") val pickup: GeoPointDto,
    @SerialName("dropoff") val dropoff: GeoPointDto,
    @SerialName("ride_tier_type") val rideTierType: String,
    @SerialName("fare_estimate") val fareEstimate: FareEstimateDto,
    @SerialName("status") val status: String,
    @SerialName("driver") val driver: DriverDto? = null,
    @SerialName("requested_at_epoch_millis") val requestedAtEpochMillis: Long,
)
