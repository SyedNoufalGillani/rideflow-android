package com.syednoufal.rideflow.feature.booking.presentation.home

import androidx.lifecycle.viewModelScope
import com.syednoufal.rideflow.core.common.mvi.MviViewModel
import com.syednoufal.rideflow.core.domain.model.GeoPoint
import com.syednoufal.rideflow.core.domain.model.TripStatus
import com.syednoufal.rideflow.core.domain.usecase.booking.CancelRideUseCase
import com.syednoufal.rideflow.core.domain.usecase.booking.GetFareEstimatesUseCase
import com.syednoufal.rideflow.core.domain.usecase.booking.GetRideTiersUseCase
import com.syednoufal.rideflow.core.domain.usecase.booking.GetSavedPlacesUseCase
import com.syednoufal.rideflow.core.domain.usecase.booking.ObserveDriverMatchUseCase
import com.syednoufal.rideflow.core.domain.usecase.booking.RequestRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the home/booking screen: location selection, fare estimation across
 * ride tiers, ride requests, and the driver-matching state machine up to
 * pickup, at which point [HomeEffect.NavigateToTracking] hands off to
 * `:feature:tracking`.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSavedPlacesUseCase: GetSavedPlacesUseCase,
    private val getRideTiersUseCase: GetRideTiersUseCase,
    private val getFareEstimatesUseCase: GetFareEstimatesUseCase,
    private val requestRideUseCase: RequestRideUseCase,
    private val observeDriverMatchUseCase: ObserveDriverMatchUseCase,
    private val cancelRideUseCase: CancelRideUseCase,
) : MviViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {

    private var matchingJob: Job? = null

    init {
        loadInitialData()
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.PlaceSelected -> onPlaceSelected(intent.place)
            is HomeIntent.LocationSlotFocused -> onSlotFocused(intent.slot)
            is HomeIntent.DropoffCleared -> setState {
                copy(
                    dropoff = null,
                    editingSlot = LocationSlot.DROPOFF,
                    fareEstimates = emptyList(),
                    selectedRideTierType = null,
                    step = BookingStep.SELECT_LOCATIONS,
                )
            }
            is HomeIntent.RideTierSelected -> setState { copy(selectedRideTierType = intent.type) }
            is HomeIntent.BackToLocationsClicked -> resetToLocationSelection()
            is HomeIntent.RequestRideClicked -> requestRide()
            is HomeIntent.CancelRideClicked -> cancelRide()
            is HomeIntent.ErrorDismissed -> setState { copy(errorMessage = null) }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            setState { copy(isLoadingSavedPlaces = true) }
            runCatching {
                val savedPlaces = getSavedPlacesUseCase()
                val rideTiers = getRideTiersUseCase()
                savedPlaces to rideTiers
            }.onSuccess { (savedPlaces, rideTiers) ->
                setState { copy(isLoadingSavedPlaces = false, savedPlaces = savedPlaces, rideTiers = rideTiers) }
            }.onFailure { throwable ->
                setState {
                    copy(isLoadingSavedPlaces = false, errorMessage = throwable.message ?: "Couldn't load RideFlow.")
                }
            }
        }
    }

    /** Focuses [slot], so the next tap on a saved place fills that field instead of the other one. */
    private fun onSlotFocused(slot: LocationSlot) {
        setState { copy(editingSlot = slot) }
    }

    private fun onPlaceSelected(place: GeoPoint) {
        when (currentState.editingSlot) {
            LocationSlot.PICKUP -> setState {
                copy(pickup = place, editingSlot = if (dropoff == null) LocationSlot.DROPOFF else editingSlot)
            }
            LocationSlot.DROPOFF -> setState { copy(dropoff = place) }
        }

        val updated = currentState
        if (updated.pickup != null && updated.dropoff != null) {
            fetchFareEstimates(updated.pickup, updated.dropoff)
        }
    }

    private fun fetchFareEstimates(pickup: GeoPoint, dropoff: GeoPoint) {
        viewModelScope.launch {
            setState { copy(isEstimatingFare = true, step = BookingStep.SELECT_RIDE_TIER) }
            runCatching { getFareEstimatesUseCase(pickup, dropoff) }
                .onSuccess { estimates ->
                    setState {
                        copy(
                            isEstimatingFare = false,
                            fareEstimates = estimates,
                            selectedRideTierType = selectedRideTierType ?: estimates.firstOrNull()?.rideTierType,
                        )
                    }
                }
                .onFailure { throwable ->
                    setState {
                        copy(isEstimatingFare = false, errorMessage = throwable.message ?: "Couldn't price this trip.")
                    }
                }
        }
    }

    private fun resetToLocationSelection() {
        matchingJob?.cancel()
        setState {
            copy(
                step = BookingStep.SELECT_LOCATIONS,
                editingSlot = LocationSlot.PICKUP,
                pickup = null,
                dropoff = null,
                fareEstimates = emptyList(),
                selectedRideTierType = null,
                activeTripId = null,
                matchingStatus = null,
            )
        }
    }

    private fun requestRide() {
        val state = currentState
        val pickup = state.pickup ?: return
        val dropoff = state.dropoff ?: return
        val tierType = state.selectedRideTierType ?: return
        val fareEstimate = state.selectedFareEstimate ?: return

        viewModelScope.launch {
            setState { copy(isRequestingRide = true, errorMessage = null) }
            runCatching { requestRideUseCase(pickup, dropoff, tierType, fareEstimate) }
                .onSuccess { trip ->
                    setState {
                        copy(
                            isRequestingRide = false,
                            step = BookingStep.MATCHING_DRIVER,
                            activeTripId = trip.id,
                            matchingStatus = trip.status,
                        )
                    }
                    observeMatching(trip.id)
                }
                .onFailure { throwable ->
                    setState {
                        copy(isRequestingRide = false, errorMessage = throwable.message ?: "Couldn't request a ride.")
                    }
                }
        }
    }

    private fun observeMatching(tripId: String) {
        matchingJob?.cancel()
        matchingJob = viewModelScope.launch {
            observeDriverMatchUseCase(tripId).collect { trip ->
                setState { copy(matchingStatus = trip.status) }
                if (trip.status is TripStatus.DriverArrived) {
                    sendEffect { HomeEffect.NavigateToTracking(tripId) }
                }
            }
        }
    }

    private fun cancelRide() {
        val tripId = currentState.activeTripId ?: return
        matchingJob?.cancel()
        viewModelScope.launch {
            runCatching { cancelRideUseCase(tripId) }
            resetToLocationSelection()
            sendEffect { HomeEffect.ShowMessage("Ride cancelled.") }
        }
    }
}
