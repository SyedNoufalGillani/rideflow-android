package com.syednoufal.rideflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A locally cached snapshot of a trip's request details and current status.
 *
 * Beyond powering the rider's ride-history list, this row is the hand-off
 * mechanism between the booking and tracking feature modules: booking writes
 * the full request (pickup/dropoff coordinates, tier, priced fare) here when
 * a ride is requested, and tracking reads it back by [tripId] to resume the
 * trip's simulation from wherever booking's driver-matching phase left off —
 * without the two feature modules depending on each other directly.
 */
@Entity(tableName = "ride_history")
data class RideHistoryEntity(
    @PrimaryKey val tripId: String,
    val pickupLabel: String,
    val pickupAddress: String,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val dropoffLabel: String,
    val dropoffAddress: String,
    val dropoffLatitude: Double,
    val dropoffLongitude: Double,
    val rideTierType: String,
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val surgeMultiplier: Double,
    val estimatedDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val currencyCode: String,
    val status: String,
    val requestedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
)
