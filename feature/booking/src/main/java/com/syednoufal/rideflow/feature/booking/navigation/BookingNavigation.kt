package com.syednoufal.rideflow.feature.booking.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.syednoufal.rideflow.feature.booking.presentation.home.HomeScreen

/** Route constants for the booking feature's nav graph. */
object BookingRoutes {
    const val HOME = "booking/home"
}

/**
 * Registers the booking feature's home screen. [onNavigateToTracking] hands
 * off to `:feature:tracking` once a driver has arrived at pickup.
 */
fun NavGraphBuilder.bookingNavGraph(onNavigateToTracking: (tripId: String) -> Unit) {
    composable(BookingRoutes.HOME) {
        HomeScreen(onNavigateToTracking = onNavigateToTracking)
    }
}
