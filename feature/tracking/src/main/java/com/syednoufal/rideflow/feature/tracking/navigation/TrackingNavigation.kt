package com.syednoufal.rideflow.feature.tracking.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.syednoufal.rideflow.feature.tracking.presentation.trip.TripTrackingScreen
import com.syednoufal.rideflow.feature.tracking.presentation.trip.TripViewModel

/** Route constants for the tracking feature's nav graph. */
object TrackingRoutes {
    private const val TRIP_PATTERN = "tracking/trip/{${TripViewModel.ARG_TRIP_ID}}"
    const val TRIP = TRIP_PATTERN

    fun tripRoute(tripId: String): String = "tracking/trip/$tripId"
}

/**
 * Registers the live trip-tracking screen. [onTripCompleted] hands off to
 * `:feature:payments` once the simulated trip reaches its destination.
 */
fun NavGraphBuilder.trackingNavGraph(onTripCompleted: (tripId: String, amount: Double) -> Unit) {
    composable(
        route = TrackingRoutes.TRIP,
        arguments = listOf(navArgument(TripViewModel.ARG_TRIP_ID) { type = NavType.StringType }),
    ) {
        TripTrackingScreen(onTripCompleted = onTripCompleted)
    }
}
