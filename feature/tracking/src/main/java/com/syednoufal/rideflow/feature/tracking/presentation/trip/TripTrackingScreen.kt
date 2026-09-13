package com.syednoufal.rideflow.feature.tracking.presentation.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syednoufal.rideflow.core.designsystem.component.RideFlowLoadingState
import com.syednoufal.rideflow.core.designsystem.component.map.DemoGeoProjection
import com.syednoufal.rideflow.core.designsystem.component.map.RideFlowMapCanvas
import com.syednoufal.rideflow.core.designsystem.theme.RideFlowTheme

/**
 * The live trip screen: the driver's simulated position moving from pickup
 * to dropoff on the Canvas map, ETA, and a handoff to payment on arrival.
 */
@Composable
fun TripTrackingScreen(
    onTripCompleted: (tripId: String, amount: Double) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TripEffect.NavigateToPayment -> onTripCompleted(effect.tripId, effect.amount)
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onIntent(TripIntent.ErrorDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (state.isLoading) {
            RideFlowLoadingState(modifier = Modifier.padding(paddingValues), message = "Loading your trip...")
        } else {
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                val pickupOffset = state.pickup?.let(DemoGeoProjection::project) ?: Offset(0.3f, 0.7f)
                val dropoffOffset = state.dropoff?.let(DemoGeoProjection::project) ?: Offset(0.7f, 0.3f)
                val driverOffset = state.driver?.currentLocation?.let(DemoGeoProjection::project)

                RideFlowMapCanvas(
                    pickup = pickupOffset,
                    dropoff = dropoffOffset,
                    driverPosition = driverOffset,
                    modifier = Modifier.fillMaxSize(),
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(
                        topStart = RideFlowTheme.spacing.large,
                        topEnd = RideFlowTheme.spacing.large,
                    ),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    Column(modifier = Modifier.padding(RideFlowTheme.spacing.medium)) {
                        Text(text = state.statusHeadline, style = MaterialTheme.typography.headlineSmall)
                        state.etaMinutes?.let { eta ->
                            Text(
                                text = "$eta min to destination",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(RideFlowTheme.spacing.small))
                        LinearProgressIndicator(
                            progress = { state.progressFraction },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(RideFlowTheme.spacing.medium))

                        state.driver?.let { driver ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(text = driver.fullName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${driver.vehicleModel} · ${driver.vehiclePlate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { /* Simulated: no dialer integration in this demo. */ }) {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = "Call driver",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
