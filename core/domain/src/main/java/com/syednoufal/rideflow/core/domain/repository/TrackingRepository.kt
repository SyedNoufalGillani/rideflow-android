package com.syednoufal.rideflow.core.domain.repository

import com.syednoufal.rideflow.core.domain.model.Trip
import kotlinx.coroutines.flow.Flow

/**
 * Contract for live trip tracking once a driver has arrived at pickup: the
 * moving vehicle location, ETA updates, and the transition through
 * [com.syednoufal.rideflow.core.domain.model.TripStatus.InProgress] to
 * [com.syednoufal.rideflow.core.domain.model.TripStatus.Completed].
 *
 * A production backend would push these updates over a WebSocket or
 * Firebase Realtime Database channel; the fake implementation simulates the
 * same shape with a repeating, timed [Flow].
 */
interface TrackingRepository {

    /** Streams live location and status updates for [tripId] until the trip completes or is cancelled. */
    fun observeActiveTrip(tripId: String): Flow<Trip>

    /** One-shot fetch of a trip's current snapshot, for screens that don't need the live stream. */
    suspend fun getTrip(tripId: String): Trip
}
