package com.syednoufal.rideflow.feature.auth.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.syednoufal.rideflow.feature.auth.presentation.login.LoginScreen
import com.syednoufal.rideflow.feature.auth.presentation.otp.OtpViewModel
import com.syednoufal.rideflow.feature.auth.presentation.otp.OtpScreen

/** Route constants for the auth feature's nav graph, kept internal to how routes are built. */
object AuthRoutes {
    const val LOGIN = "auth/login"
    private const val OTP_PATTERN = "auth/otp/{${OtpViewModel.ARG_CHALLENGE_ID}}/{${OtpViewModel.ARG_PHONE_NUMBER}}"
    const val OTP = OTP_PATTERN

    fun otpRoute(challengeId: String, phoneNumber: String): String =
        "auth/otp/$challengeId/${Uri.encode(phoneNumber)}"
}

/**
 * Registers the auth feature's screens (login → OTP) on [navController]'s
 * graph. [onAuthComplete] fires once verification succeeds, letting the app
 * graph decide where to go next (the booking home screen).
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onAuthComplete: () -> Unit,
) {
    composable(AuthRoutes.LOGIN) {
        LoginScreen(
            onNavigateToOtp = { challengeId, phoneNumber ->
                navController.navigate(AuthRoutes.otpRoute(challengeId, phoneNumber))
            },
        )
    }

    composable(
        route = AuthRoutes.OTP,
        arguments = listOf(
            navArgument(OtpViewModel.ARG_CHALLENGE_ID) { type = NavType.StringType },
            navArgument(OtpViewModel.ARG_PHONE_NUMBER) { type = NavType.StringType },
        ),
    ) {
        OtpScreen(onVerified = onAuthComplete)
    }
}
