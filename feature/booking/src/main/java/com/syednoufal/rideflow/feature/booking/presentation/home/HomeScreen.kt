package com.syednoufal.rideflow.feature.booking.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syednoufal.rideflow.core.designsystem.component.RideFlowPrimaryButton
import com.syednoufal.rideflow.core.designsystem.component.RideFlowSecondaryButton
import com.syednoufal.rideflow.core.designsystem.component.RideFlowSelectableRow
import com.syednoufal.rideflow.core.designsystem.component.map.DemoGeoProjection
import com.syednoufal.rideflow.core.designsystem.component.map.RideFlowMapCanvas
import com.syednoufal.rideflow.core.designsystem.component.map.RideFlowRoute
import com.syednoufal.rideflow.core.designsystem.theme.RideFlowTheme
import com.syednoufal.rideflow.core.domain.model.FareEstimate
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.model.RideTierType
import com.syednoufal.rideflow.core.domain.model.TripStatus
import java.util.Locale

private val IDLE_PICKUP = Offset(0.3f, 0.7f)
private val IDLE_DROPOFF = Offset(0.7f, 0.32f)

/** RideFlow's home screen: the stylized map, location selection, fare quotes and driver matching. */
@Composable
fun HomeScreen(
    onNavigateToTracking: (tripId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToTracking -> onNavigateToTracking(effect.tripId)
                is HomeEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onIntent(HomeIntent.ErrorDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            RideFlowMapCanvas(
                pickup = state.pickup?.let(DemoGeoProjection::project) ?: IDLE_PICKUP,
                dropoff = state.dropoff?.let(DemoGeoProjection::project) ?: IDLE_DROPOFF,
                showSearchingPulse = state.matchingStatus is TripStatus.SearchingForDriver,
                driverPosition = matchingDriverOffset(state.matchingStatus),
                driverHeadingDegrees = driverHeading(state),
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
                    when (state.step) {
                        BookingStep.SELECT_LOCATIONS -> LocationSelectionSheet(
                            state = state,
                            onIntent = viewModel::onIntent,
                        )
                        BookingStep.SELECT_RIDE_TIER -> RideTierSelectionSheet(
                            state = state,
                            onIntent = viewModel::onIntent,
                        )
                        BookingStep.MATCHING_DRIVER -> DriverMatchingSheet(
                            state = state,
                            onIntent = viewModel::onIntent,
                        )
                    }
                }
            }
        }
    }
}

private fun matchingDriverOffset(status: TripStatus?): Offset? = when (status) {
    is TripStatus.DriverAssigned -> DemoGeoProjection.project(status.driver.currentLocation)
    is TripStatus.DriverArrived -> DemoGeoProjection.project(status.driver.currentLocation)
    else -> null
}

private fun driverHeading(state: HomeUiState): Float {
    val driverOffset = matchingDriverOffset(state.matchingStatus) ?: return 0f
    val pickupOffset = state.pickup?.let(DemoGeoProjection::project) ?: IDLE_PICKUP
    return RideFlowRoute.pointAt(driverOffset, pickupOffset, progress = 0.01f).headingDegrees
}

@Composable
private fun LocationSelectionSheet(state: HomeUiState, onIntent: (HomeIntent) -> Unit) {
    Text(text = "Where to?", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(RideFlowTheme.spacing.medium))

    LocationSlotRow(
        icon = Icons.Filled.Circle,
        label = "Pickup",
        value = state.pickup?.label ?: state.pickup?.address,
        placeholder = "Tap to choose a pickup point",
        isFocused = state.editingSlot == LocationSlot.PICKUP,
        onFocus = { onIntent(HomeIntent.LocationSlotFocused(LocationSlot.PICKUP)) },
        onClear = { onIntent(HomeIntent.BackToLocationsClicked) }.takeIf { state.pickup != null },
    )
    Spacer(modifier = Modifier.height(RideFlowTheme.spacing.small))
    LocationSlotRow(
        icon = Icons.Filled.Place,
        label = "Dropoff",
        value = state.dropoff?.label ?: state.dropoff?.address,
        placeholder = "Tap to choose a dropoff point",
        isFocused = state.editingSlot == LocationSlot.DROPOFF,
        onFocus = { onIntent(HomeIntent.LocationSlotFocused(LocationSlot.DROPOFF)) },
        onClear = { onIntent(HomeIntent.DropoffCleared) }.takeIf { state.dropoff != null },
    )

    Spacer(modifier = Modifier.height(RideFlowTheme.spacing.medium))

    if (state.isLoadingSavedPlaces) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.padding(RideFlowTheme.spacing.large))
        }
    } else {
        LazyColumn(modifier = Modifier.height(220.dp)) {
            items(items = state.savedPlaces, key = GeoPoint::hashCode) { place ->
                SavedPlaceRow(
                    place = place,
                    onClick = { onIntent(HomeIntent.PlaceSelected(place)) },
                )
            }
        }
    }
}

@Composable
private fun LocationSlotRow(
    icon: ImageVector,
    label: String,
    value: String?,
    placeholder: String,
    isFocused: Boolean,
    onFocus: () -> Unit,
    onClear: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFocus)
            .background(
                color = if (isFocused) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = RideFlowTheme.spacing.small, vertical = RideFlowTheme.spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(16.dp),
        )
        Column(modifier = Modifier.padding(start = RideFlowTheme.spacing.small).weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = value ?: placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = if (value != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onClear != null) {
            IconButton(onClick = onClear) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear $label")
            }
        }
    }
}

@Composable
private fun SavedPlaceRow(place: GeoPoint, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = RideFlowTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.padding(start = RideFlowTheme.spacing.small)) {
            Text(text = place.label ?: place.address, style = MaterialTheme.typography.bodyLarge)
            if (place.label != null && place.address.isNotBlank()) {
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RideTierSelectionSheet(state: HomeUiState, onIntent: (HomeIntent) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "Choose a ride", style = MaterialTheme.typography.headlineSmall)
        IconButton(onClick = { onIntent(HomeIntent.BackToLocationsClicked) }) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = "Change locations")
        }
    }
    Spacer(modifier = Modifier.height(RideFlowTheme.spacing.small))

    if (state.isEstimatingFare) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.padding(RideFlowTheme.spacing.large))
        }
    } else {
        state.rideTiers.forEach { tier ->
            val estimate = state.fareEstimates.find { it.rideTierType == tier.type }
            RideFlowSelectableRow(
                selected = state.selectedRideTierType == tier.type,
                onClick = { onIntent(HomeIntent.RideTierSelected(tier.type)) },
                modifier = Modifier.padding(vertical = RideFlowTheme.spacing.extraSmall),
            ) {
                Column {
                    Text(text = tier.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${tier.capacity} seats · ${tier.etaMinutes} min away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = estimate?.let { formatCurrency(it) } ?: "--",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(RideFlowTheme.spacing.medium))
    RideFlowPrimaryButton(
        label = "Request ${state.selectedRideTierType?.let { formatTierName(it) } ?: "ride"}",
        onClick = { onIntent(HomeIntent.RequestRideClicked) },
        enabled = state.isRequestRideEnabled,
        isLoading = state.isRequestingRide,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DriverMatchingSheet(state: HomeUiState, onIntent: (HomeIntent) -> Unit) {
    val (title, subtitle) = matchingCopy(state.matchingStatus)

    Text(text = title, style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(RideFlowTheme.spacing.extraSmall))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    val driver = (state.matchingStatus as? TripStatus.DriverAssigned)?.driver
        ?: (state.matchingStatus as? TripStatus.DriverArrived)?.driver

    if (driver != null) {
        Spacer(modifier = Modifier.height(RideFlowTheme.spacing.medium))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Column(modifier = Modifier.padding(start = RideFlowTheme.spacing.small)) {
                Text(text = driver.fullName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${driver.vehicleModel} · ${driver.vehiclePlate} · ★ ${driver.rating}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(RideFlowTheme.spacing.large))
    RideFlowSecondaryButton(
        label = "Cancel ride",
        onClick = { onIntent(HomeIntent.CancelRideClicked) },
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun matchingCopy(status: TripStatus?): Pair<String, String> = when (status) {
    is TripStatus.SearchingForDriver, null -> "Finding you a driver" to "This usually takes less than a minute."
    is TripStatus.DriverAssigned -> "Driver on the way" to "Arriving in about ${status.etaMinutes} min."
    is TripStatus.DriverArrived -> "Your driver has arrived" to "Head to the pickup point."
    else -> "Trip update" to ""
}

private fun formatTierName(type: RideTierType): String = type.name.lowercase(Locale.US)
    .replaceFirstChar { it.uppercase(Locale.US) }

private fun formatCurrency(estimate: FareEstimate): String =
    "$${String.format(Locale.US, "%.2f", estimate.total)}"
