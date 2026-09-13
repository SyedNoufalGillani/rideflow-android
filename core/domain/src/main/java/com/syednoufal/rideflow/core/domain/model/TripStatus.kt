package com.syednoufal.rideflow.core.domain.model

/**
 * The lifecycle of a single trip, from the moment a rider requests a ride to
 * its completion or cancellation. Modeled as a sealed hierarchy (rather than
 * a plain enum) because [DriverAssigned] and its downstream states carry the
 * matched [Driver] alongside them.
 */
sealed class TripStatus {

    /** No driver has been matched yet; the matching engine is searching nearby. */
    data object SearchingForDriver : TripStatus()

    /** A driver accepted the request and is en route to the pickup point. */
    data class DriverAssigned(val driver: Driver, val etaMinutes: Int) : TripStatus()

    /** The driver has arrived at the pickup point and is waiting for the rider. */
    data class DriverArrived(val driver: Driver) : TripStatus()

    /**
     * The rider is in the vehicle and the trip toward the dropoff is
     * underway. [progressFraction] (`0f..1f`) is how far along the route the
     * vehicle currently is, used to place it on the live tracking map.
     */
    data class InProgress(val driver: Driver, val etaMinutes: Int, val progressFraction: Float) : TripStatus()

    /** The trip finished normally at the dropoff point. */
    data class Completed(val driver: Driver, val finalFare: FareEstimate) : TripStatus()

    /** The trip was cancelled by the rider, the driver, or the matching engine. */
    data class Cancelled(val reason: String) : TripStatus()
}
