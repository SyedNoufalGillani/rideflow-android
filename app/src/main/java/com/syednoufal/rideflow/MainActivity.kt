package com.syednoufal.rideflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syednoufal.rideflow.core.designsystem.theme.RideFlowTheme
import com.syednoufal.rideflow.feature.auth.navigation.AuthRoutes
import com.syednoufal.rideflow.feature.booking.navigation.BookingRoutes
import com.syednoufal.rideflow.navigation.RideFlowNavGraph
import com.syednoufal.rideflow.presentation.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

/** RideFlow's single activity, hosting the entire Compose Navigation graph. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { appViewModel.isLoggedIn.value == null }

        setContent {
            val isLoggedIn by appViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val knownIsLoggedIn = isLoggedIn

            RideFlowTheme {
                if (knownIsLoggedIn != null) {
                    RideFlowNavGraph(
                        startDestination = if (knownIsLoggedIn) BookingRoutes.HOME else AuthRoutes.LOGIN,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
