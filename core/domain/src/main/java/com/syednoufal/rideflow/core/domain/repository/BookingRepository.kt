package com.syednoufal.rideflow.core.domain.repository

import com.syednoufal.rideflow.core.domain.model.FareEstimate
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.model.RideTier
import com.syednoufal.rideflow.core.domain.model.RideTierType
import com.syednoufal.rideflow.core.domain.model.Trip
import kotlinx.coroutines.flow.Flow

/**
 * Contract for the ride-booking flow: pricing a route, requesting a ride,
 * and observing the driver-matching state machine up to pickup. Ownership of
 * a [Trip] hands off to [TrackingRepository] once the driver arrives and the
 * trip moves in-progress.
 */
interface BookingRepository {

    /** The static catalogue of vehicle tiers riders can choose between. */
    suspend fun getRideTiers(): List<RideTier>

    /** Frequently used pickup/dropoff points the rider has saved or recently visited. */
    suspend fun getSavedPlaces(): List<GeoPoint>

    /** Prices every ride tier for the given [pickup]/[dropoff] pair. */
    suspend fun getFareEstimates(pickup: GeoPoint, dropoff: GeoPoint): List<FareEstimate>

    /** Submits a ride request, returning the newly created [Trip] in [com.syednoufal.rideflow.core.domain.model.TripStatus.SearchingForDriver]. */
    suspend fun requestRide(
        pickup: GeoPoint,
        dropoff: GeoPoint,
        rideTierType: RideTierType,
        fareEstimate: FareEstimate,
    ): Trip

    /**
     * Emits the evolving [Trip] as the matching engine searches for and
     * assigns a driver. Completes once the driver reaches the pickup point.
     */
    fun observeDriverMatching(tripId: String): Flow<Trip>

    /** Cancels an in-flight ride request or an assigned-but-not-yet-picked-up trip. */
    suspend fun cancelRide(tripId: String)
}
