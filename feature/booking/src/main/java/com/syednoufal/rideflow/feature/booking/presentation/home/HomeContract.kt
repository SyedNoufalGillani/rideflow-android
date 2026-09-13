package com.syednoufal.rideflow.feature.booking.presentation.home

import com.syednoufal.rideflow.core.common.mvi.UiEffect
import com.syednoufal.rideflow.core.common.mvi.UiIntent
import com.syednoufal.rideflow.core.common.mvi.UiState
import com.syednoufal.rideflow.core.domain.model.FareEstimate
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.model.RideTier
import com.syednoufal.rideflow.core.domain.model.RideTierType
import com.syednoufal.rideflow.core.domain.model.TripStatus

/** Which part of the booking flow is currently in front, driving what the bottom sheet shows. */
enum class BookingStep {
    SELECT_LOCATIONS,
    SELECT_RIDE_TIER,
    MATCHING_DRIVER,
}

/** Which location field a tap on the saved-places list currently fills in. */
enum class LocationSlot {
    PICKUP,
    DROPOFF,
}

/** The home/booking screen's rendered state: map, location selection, fare estimates and driver matching. */
data class HomeUiState(
    val step: BookingStep = BookingStep.SELECT_LOCATIONS,
    val isLoadingSavedPlaces: Boolean = true,
    val savedPlaces: List<GeoPoint> = emptyList(),
    val editingSlot: LocationSlot = LocationSlot.PICKUP,
    val pickup: GeoPoint? = null,
    val dropoff: GeoPoint? = null,
    val rideTiers: List<RideTier> = emptyList(),
    val fareEstimates: List<FareEstimate> = emptyList(),
    val selectedRideTierType: RideTierType? = null,
    val isEstimatingFare: Boolean = false,
    val isRequestingRide: Boolean = false,
    val activeTripId: String? = null,
    val matchingStatus: TripStatus? = null,
    val errorMessage: String? = null,
) : UiState {

    val canChooseRideTier: Boolean
        get() = pickup != null && dropoff != null

    val selectedFareEstimate: FareEstimate?
        get() = fareEstimates.find { it.rideTierType == selectedRideTierType }

    val isRequestRideEnabled: Boolean
        get() = selectedFareEstimate != null && !isRequestingRide
}

sealed interface HomeIntent : UiIntent {
    data class PlaceSelected(val place: GeoPoint) : HomeIntent
    data class LocationSlotFocused(val slot: LocationSlot) : HomeIntent
    data object DropoffCleared : HomeIntent
    data class RideTierSelected(val type: RideTierType) : HomeIntent
    data object BackToLocationsClicked : HomeIntent
    data object RequestRideClicked : HomeIntent
    data object CancelRideClicked : HomeIntent
    data object ErrorDismissed : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    data class NavigateToTracking(val tripId: String) : HomeEffect
    data class ShowMessage(val message: String) : HomeEffect
}
