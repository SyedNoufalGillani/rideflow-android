package com.syednoufal.rideflow.feature.payments.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.syednoufal.rideflow.feature.payments.presentation.methods.PaymentMethodsScreen
import com.syednoufal.rideflow.feature.payments.presentation.methods.PaymentMethodsViewModel

/** Route constants for the payments feature's nav graph. */
object PaymentsRoutes {
    private const val METHODS_PATTERN =
        "payments/methods/{${PaymentMethodsViewModel.ARG_TRIP_ID}}/{${PaymentMethodsViewModel.ARG_AMOUNT}}"
    const val METHODS = METHODS_PATTERN

    fun methodsRoute(tripId: String, amount: Double): String = "payments/methods/$tripId/$amount"
}

/**
 * Registers the payment method / charge screen. [onPaymentApproved] hands
 * off to the app's profile screen once the trip's fare is settled.
 */
fun NavGraphBuilder.paymentsNavGraph(onPaymentApproved: (tripId: String) -> Unit) {
    composable(
        route = PaymentsRoutes.METHODS,
        arguments = listOf(
            navArgument(PaymentMethodsViewModel.ARG_TRIP_ID) { type = NavType.StringType },
            navArgument(PaymentMethodsViewModel.ARG_AMOUNT) { type = NavType.FloatType },
        ),
    ) {
        PaymentMethodsScreen(onPaymentApproved = onPaymentApproved)
    }
}
