package com.syednoufal.rideflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.syednoufal.rideflow.feature.auth.navigation.AuthRoutes
import com.syednoufal.rideflow.feature.auth.navigation.authNavGraph
import com.syednoufal.rideflow.feature.booking.navigation.BookingRoutes
import com.syednoufal.rideflow.feature.booking.navigation.bookingNavGraph
import com.syednoufal.rideflow.feature.payments.navigation.PaymentsRoutes
import com.syednoufal.rideflow.feature.payments.navigation.paymentsNavGraph
import com.syednoufal.rideflow.feature.tracking.navigation.TrackingRoutes
import com.syednoufal.rideflow.feature.tracking.navigation.trackingNavGraph
import com.syednoufal.rideflow.presentation.profile.ProfileScreen

/**
 * RideFlow's single top-level nav graph, composed entirely out of each
 * feature module's own graph builder: auth → booking → tracking → payments
 * → profile. No feature module references another directly — each only
 * receives the plain lambda callback it needs to hand off to the next step,
 * wired together here in the one place that is allowed to know about every
 * feature.
 */
@Composable
fun RideFlowNavGraph(
    startDestination: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        authNavGraph(
            navController = navController,
            onAuthComplete = {
                navController.navigate(BookingRoutes.HOME) {
                    popUpTo(AuthRoutes.LOGIN) { inclusive = true }
                }
            },
        )

        bookingNavGraph(
            onNavigateToTracking = { tripId ->
                navController.navigate(TrackingRoutes.tripRoute(tripId))
            },
        )

        trackingNavGraph(
            onTripCompleted = { tripId, amount ->
                navController.navigate(PaymentsRoutes.methodsRoute(tripId, amount))
            },
        )

        paymentsNavGraph(
            onPaymentApproved = {
                navController.navigate(AppRoutes.PROFILE)
            },
        )

        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                onLoggedOut = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
